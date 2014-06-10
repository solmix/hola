/*
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.hola.osgi.rsa.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月2日
 */

public class EndpointDescriptionParser
{
    private static List<String> multiValueTypes;

    static {
          multiValueTypes = Arrays.asList(new String[] { "String", "Long", 
                      "long", "Double", "double", "float", "Float", "int", "Integer",
                      "byte", "Byte", "char", "Character", "boolean", "Boolean", 
                      "short", "Short" }); 
    }
    private static final String ENDPOINT_DESCRIPTIONS = "endpoint-descriptions"; 
    private static final String ENDPOINT_DESCRIPTION = "endpoint-description"; 
    private static final String ENDPOINT_PROPERTY = "property"; 
    private static final String ENDPOINT_PROPERTY_NAME = "name";
    private static final String ENDPOINT_PROPERTY_VALUE = "value";
    private static final String ENDPOINT_PROPERTY_VALUETYPE = "value-type";
    private static final String ENDPOINT_PROPERTY_ARRAY = "array"; 
    private static final String ENDPOINT_PROPERTY_LIST = "list";
    private static final String ENDPOINT_PROPERTY_SET = "set";
    private static final String ENDPOINT_PROPERTY_XML = "xml";

    public static String[] noAttributes = new String[0];
    private List<Map<String,Object>> endpointDescriptions;
    private XMLReader xmlReader;

    class IgnoringHandler extends AbstractHandler {
          public IgnoringHandler(AbstractHandler parent) {
                super(parent);
                this.elementHandled = "IgnoringAll"; 
          }

          @Override
        public void startElement(String name, Attributes attributes) {
                noSubElements(name, attributes);
          }
    }

    /**
     * Abstract base class for content handlers
     */
    abstract class AbstractHandler extends DefaultHandler {

          protected ContentHandler parentHandler = null;
          protected String elementHandled = null;

          protected StringBuffer characters = null; // character data inside an
                                                                            // element

          public AbstractHandler() {
                // Empty constructor for a root handler
          }

          public AbstractHandler(ContentHandler parentHandler) {
                this.parentHandler = parentHandler;
                xmlReader.setContentHandler(this);
          }

          public AbstractHandler(ContentHandler parentHandler,
                      String elementHandled) {
                this.parentHandler = parentHandler;
                xmlReader.setContentHandler(this);
                this.elementHandled = elementHandled;
          }

          @Override
        public void startElement(String uri, String localName, String qName,
                      Attributes attributes) throws SAXException {
                finishCharacters();
                String name = makeSimpleName(localName, qName);
                startElement(name, attributes);
          }

          public abstract void startElement(String name, Attributes attributes)
                      throws SAXException;

          public void invalidElement(String name, Attributes attributes) {
                unexpectedElement(this, name, attributes);
                new IgnoringHandler(this);
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                finishCharacters();
                finished();
                // Restore the parent content handler
                xmlReader.setContentHandler(parentHandler);
          }

          /**
           * An implementation for startElement when there are no sub-elements
           */
          protected void noSubElements(String name, Attributes attributes) {
                unexpectedElement(this, name, attributes);
                // Create a new handler to ignore subsequent nested elements
                new IgnoringHandler(this);
          }

          /*
           * Save up character data until endElement or nested startElement
           * 
           * @see org.xml.sax.ContentHandler#characters
           */
          @Override
        public void characters(char[] chars, int start, int length) {
                if (this.characters == null) {
                      this.characters = new StringBuffer();
                }
                this.characters.append(chars, start, length);
          }

          // Consume the characters accumulated in this.characters.
          // Called before startElement or endElement
          private String finishCharacters() {
                // common case -- no characters or only whitespace
                if (this.characters == null || this.characters.length() == 0) {
                      return null;
                }
                if (allWhiteSpace(this.characters)) {
                      this.characters.setLength(0);
                      return null;
                }

                // process the characters
                try {
                      String trimmedChars = this.characters.toString().trim();
                      if (trimmedChars.length() == 0) {
                            // this shouldn't happen due to the test for allWhiteSpace
                            // above
                            System.err.println("Unexpected non-whitespace characters: " 
                                        + trimmedChars);
                            return null;
                      }
                      processCharacters(trimmedChars);
                      return trimmedChars;
                } finally {
                      this.characters.setLength(0);
                }
          }

          // Method to override in the handler of an element with CDATA.
          protected void processCharacters(String data) {
                if (data.length() > 0)
                      unexpectedCharacterData(this, data);
          }

          private boolean allWhiteSpace(StringBuffer sb) {
                int length = sb.length();
                for (int i = 0; i < length; i += 1)
                      if (!Character.isWhitespace(sb.charAt(i)))
                            return false;
                return true;
          }

          /**
           * Called when this element and all elements nested into it have been
           * handled.
           */
          protected void finished() {
                // Do nothing by default
          }

          /*
           * A name used to identify the handler.
           */
          public String getName() {
                return (elementHandled != null ? elementHandled : "NoName"); 
          }

          /**
           * Parse the attributes of an element with only required attributes.
           */
          protected String[] parseRequiredAttributes(Attributes attributes,
                      String[] required) {
                return parseAttributes(attributes, required, noAttributes);
          }

          /**
           * Parse the attributes of an element with a single optional attribute.
           */
          protected String parseOptionalAttribute(Attributes attributes,
                      String name) {
                return parseAttributes(attributes, noAttributes,
                            new String[] { name })[0];
          }

          /**
           * Parse the attributes of an element, given the list of required and
           * optional ones. Return values in same order, null for those not
           * present. Log warnings for extra attributes or missing required
           * attributes.
           */
          protected String[] parseAttributes(Attributes attributes,
                      String[] required, String[] optional) {
                String[] result = new String[required.length + optional.length];
                for (int i = 0; i < attributes.getLength(); i += 1) {
                      String name = attributes.getLocalName(i);
                      String value = attributes.getValue(i).trim();
                      int j;
                      if ((j = indexOf(required, name)) >= 0)
                            result[j] = value;
                      else if ((j = indexOf(optional, name)) >= 0)
                            result[required.length + j] = value;
                      else
                            unexpectedAttribute(elementHandled, name, value);
                }
                for (int i = 0; i < required.length; i += 1)
                      checkRequiredAttribute(elementHandled, required[i], result[i]);
                return result;
          }

    }

    void initReader() throws ParserConfigurationException, SAXException {
          if(xmlReader==null){
              SAXParserFactory saxParserFactory = SAXParserFactory.newInstance(); 
              
              saxParserFactory.setNamespaceAware(true);
              saxParserFactory.setValidating(false);
              try {
                  saxParserFactory.setFeature(
                              "http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
            } catch (SAXException se) {
                  // some parsers may not support string interning
            }
              SAXParser saxParser = saxParserFactory.newSAXParser(); 
              if (saxParser == null) {
                  throw new SAXException("Unable to create sax parser"); 
            }
             
            xmlReader = saxParser.getXMLReader();
          }
    }

    abstract class RootHandler extends AbstractHandler {

          public RootHandler() {
                super();
          }

          public void initialize(DocHandler document, String rootName,
                      Attributes attributes) {
                this.parentHandler = document;
                this.elementHandled = rootName;
                handleRootAttributes(attributes);
          }

          protected abstract void handleRootAttributes(Attributes attributes);

    }

    class DocHandler extends AbstractHandler {

          RootHandler rootHandler;

          public DocHandler(String rootName, RootHandler rootHandler) {
                super(null, rootName);
                this.rootHandler = rootHandler;
          }

          @Override
        public void startElement(String name, Attributes attributes) {
                if (name.equals(elementHandled)) {
                      rootHandler.initialize(this, name, attributes);
                      xmlReader.setContentHandler(rootHandler);
                } else
                      noSubElements(name, attributes);
          }

    }

    class EndpointDescriptionDocHandler extends DocHandler {

          public EndpointDescriptionDocHandler(String rootName,
                      RootHandler rootHandler) {
                super(rootName, rootHandler);
          }

          @Override
        public void processingInstruction(String target, String data)
                      throws SAXException {
                // do nothing
          }
    }

    class EndpointDescriptionsHandler extends RootHandler {

          private final List<Map<String,Object>> endpointDescriptions = new ArrayList<Map<String,Object>>();

          @Override
        protected void handleRootAttributes(Attributes attributes) {
          }

          @Override
        public void startElement(String name, Attributes attributes)
                      throws SAXException {
                if (ENDPOINT_DESCRIPTION.equals(name))
                      new EndpointDescriptionHandler(this, attributes,
                                  endpointDescriptions);
                else
                      invalidElement(name, attributes);
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                if (elementHandled.equals(qName))
                      super.endElement(namespaceURI, localName, qName);
          }

          public List<Map<String,Object>> getEndpointDescriptions() {
                return endpointDescriptions;
          }
    }

    class EndpointDescriptionHandler extends AbstractHandler {

          private final Map<String, Object> properties;
          private final List<Map<String,Object>> descriptions;

          public EndpointDescriptionHandler(ContentHandler parentHandler,
                      Attributes attributes, List<Map<String,Object>> descriptions) {
                super(parentHandler, ENDPOINT_DESCRIPTION);
                this.properties = new TreeMap<String, Object>(
                            String.CASE_INSENSITIVE_ORDER);
                this.descriptions = descriptions;
          }

          @Override
        public void startElement(String name, Attributes attributes)
                      throws SAXException {
                if (ENDPOINT_PROPERTY.equals(name))
                      new EndpointPropertyHandler(this, attributes, properties);
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                if (elementHandled.equals(qName)) {
                      descriptions.add(properties);
                      super.endElement(namespaceURI, localName, qName);
                }
          }

    }

    private Object createValue(String valueType, String value) {
          if (value == null)
                return null;
          if (valueType.equals("String")) {
                return value;
          } else if (valueType.equals("long") || valueType.equals("Long")) {  
                return Long.valueOf(value);
          } else if (valueType.equals("double") || valueType.equals("Double")) {  
                return Double.valueOf(value);
          } else if (valueType.equals("float") || valueType.equals("Float")) {  
                return Float.valueOf(value);
          } else if (valueType.equals("int") || valueType.equals("Integer")) {  
                return Integer.valueOf(value);
          } else if (valueType.equals("byte") || valueType.equals("Byte")) {  
                return Byte.valueOf(value);
          } else if (valueType.equals("char") 
                      || valueType.equals("Character")) { 
                char[] chars = new char[1];
                value.getChars(0, 1, chars, 0);
                return Character.valueOf(chars[0]);
          } else if (valueType.equals("boolean") 
                      || valueType.equals("Boolean")) { 
                return Boolean.valueOf(value);
          } else if (valueType.equals("short") || valueType.equals("Short")) {  
                return Short.valueOf(value);
          }
          return null;
    }

    abstract class MultiValueHandler extends AbstractHandler {

          protected String valueType;

          public MultiValueHandler(ContentHandler parentHandler,
                      String elementHandled, String valueType) {
                super(parentHandler, elementHandled);
                this.valueType = valueType;
          }

          @Override
        public void startElement(String name, Attributes attributes)
                      throws SAXException {
                if (ENDPOINT_PROPERTY_VALUE.equals(name))
                      characters = new StringBuffer();
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                if (ENDPOINT_PROPERTY_VALUE.equals(qName)) {
                      Object value = createValue(
                                  valueType,
                                  processValue((characters == null) ? null : characters
                                              .toString()));
                      if (value != null)
                            addValue(value);
                      characters = null;
                } else if (elementHandled.equals(qName))
                      super.endElement(namespaceURI, localName, qName);
          }

          private String processValue(String characters) {
                if (characters == null || characters.length() == 0)
                      return null;
                if (valueType.equals("String")) 
                      return characters;
                return characters.trim();
          }

          protected abstract void addValue(Object value);

          public abstract Object getValues();
    }

    class ArrayMultiValueHandler extends MultiValueHandler {

          private final List<Object> values = new ArrayList<Object>();

          public ArrayMultiValueHandler(ContentHandler parentHandler,
                      String elementHandled, String valueType) {
                super(parentHandler, elementHandled, valueType);
          }

          protected Object[] createEmptyArrayOfType() {
                if (valueType.equals("String")) 
                      return new String[] {};
                else if (valueType.equals("long") || valueType.equals("Long"))  
                      return new Long[] {};
                else if (valueType.equals("double") || valueType.equals("Double"))  
                      return new Double[] {};
                else if (valueType.equals("float") || valueType.equals("Float"))  
                      return new Double[] {};
                else if (valueType.equals("int") || valueType.equals("Integer"))  
                      return new Integer[] {};
                else if (valueType.equals("byte") || valueType.equals("Byte"))  
                      return new Byte[] {};
                else if (valueType.equals("char") 
                            || valueType.equals("Character")) 
                      return new Character[] {};
                else if (valueType.equals("boolean") 
                            || valueType.equals("Boolean")) 
                      return new Boolean[] {};
                else if (valueType.equals("short") || valueType.equals("Short"))  
                      return new Short[] {};
                else
                      return null;
          }

          @Override
        public Object getValues() {
                return values.toArray(createEmptyArrayOfType());
          }

          @Override
        protected void addValue(Object value) {
                values.add(value);
          }
    }

    class ListMultiValueHandler extends MultiValueHandler {

          private final List<Object> values = new ArrayList<Object>();

          public ListMultiValueHandler(ContentHandler parentHandler,
                      String elementHandled, String valueType) {
                super(parentHandler, elementHandled, valueType);
          }

          @Override
        public Object getValues() {
                return values;
          }

          @Override
        protected void addValue(Object value) {
                values.add(value);
          }
    }

    class SetMultiValueHandler extends MultiValueHandler {

          private final Set<Object> values = new HashSet<Object>();

          public SetMultiValueHandler(ContentHandler parentHandler,
                      String elementHandled, String valueType) {
                super(parentHandler, elementHandled, valueType);
          }

          @Override
        public Object getValues() {
                return values;
          }

          @Override
        protected void addValue(Object value) {
                values.add(value);
          }
    }

    class XMLValueHandler extends AbstractHandler {

          private final Map<String, String> nsPrefixMap = new HashMap<String, String>();
          private final StringBuffer buf;

          public XMLValueHandler(ContentHandler parentHandler) {
                super(parentHandler, ENDPOINT_PROPERTY_XML);
                buf = new StringBuffer();
          }

          @Override
        public void startPrefixMapping(String prefix, String uri)
                      throws SAXException {
                nsPrefixMap.put(uri, prefix);
          }

          @Override
        public void startElement(String uri, String localName, String qName,
                      Attributes attributes) throws SAXException {
                buf.append("<").append(qName); 
                for (Iterator<String> i = nsPrefixMap.keySet().iterator(); i
                            .hasNext();) {
                      String nsURI = i.next();
                      String prefix = nsPrefixMap.get(nsURI);
                      i.remove();
                      if (nsURI != null) {
                            buf.append(" xmlns"); 
                            if (prefix != null)
                                  buf.append(":").append(prefix); 
                            buf.append("=\"").append(nsURI).append("\"");  
                      }
                }
                for (int i = 0; i < attributes.getLength(); i++) {
                      buf.append(" "); 
                      buf.append(attributes.getQName(i))
                                  .append("=\"").append(attributes.getValue(i)).append("\"");  
                }
                buf.append(">"); 
                characters = new StringBuffer();
          }

          @Override
        public void startElement(String name, Attributes attributes)
                      throws SAXException {
                // not used
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                if (elementHandled.equals(qName)) {
                      super.endElement(namespaceURI, localName, qName);
                } else {
                      if (characters != null)
                            buf.append(characters);
                      buf.append("</").append(qName).append(">");  
                      characters = null;
                }
          }

          public String getXML() {
                return buf.toString();
          }
    }

    class EndpointPropertyHandler extends AbstractHandler {

          private final Map<String, Object> properties;
          private final String name;
          private String valueType = "String"; 
          private Object value;
          private MultiValueHandler multiValueHandler;
          private XMLValueHandler xmlValueHandler;

          public EndpointPropertyHandler(ContentHandler parentHandler,
                      Attributes attributes, Map<String, Object> properties)
                      throws SAXException {
                super(parentHandler, ENDPOINT_PROPERTY);
                name = parseRequiredAttributes(attributes,
                            new String[] { ENDPOINT_PROPERTY_NAME })[0];
                String strValue = parseOptionalAttribute(attributes,
                            ENDPOINT_PROPERTY_VALUE);
                String vt = parseOptionalAttribute(attributes,
                            ENDPOINT_PROPERTY_VALUETYPE);
                if (vt != null) {
                      if (!multiValueTypes.contains(vt))
                            throw new SAXException("property element valueType=" + vt 
                                        + " not allowed"); 
                      this.valueType = vt;
                }
                this.properties = properties;
                if (strValue != null) {
                      value = createValue(this.valueType, strValue);
                      if (isValidProperty(name, value))
                            this.properties.put(name, value);
                }
          }

          @Override
        public void startElement(String name, Attributes attributes)
                      throws SAXException {
                // Should not happen if value is non-null
                if (value != null)
                      throw new SAXException(
                                  "property element has both value attribute and sub-element"); 
                if (ENDPOINT_PROPERTY_ARRAY.equals(name)) {
                      if (multiValueHandler == null)
                            multiValueHandler = new ArrayMultiValueHandler(this,
                                        ENDPOINT_PROPERTY_ARRAY, valueType);
                      else
                            duplicateElement(this, name, attributes);

                } else if (ENDPOINT_PROPERTY_LIST.equals(name)) {
                      if (multiValueHandler == null)
                            multiValueHandler = new ListMultiValueHandler(this,
                                        ENDPOINT_PROPERTY_LIST, valueType);
                      else
                            duplicateElement(this, name, attributes);
                } else if (ENDPOINT_PROPERTY_SET.equals(name)) {
                      if (multiValueHandler == null)
                            multiValueHandler = new SetMultiValueHandler(this,
                                        ENDPOINT_PROPERTY_SET, valueType);
                      else
                            duplicateElement(this, name, attributes);
                } else if (ENDPOINT_PROPERTY_XML.equals(name)) {
                      // xml
                      if (xmlValueHandler == null)
                            xmlValueHandler = new XMLValueHandler(this);
                      else
                            duplicateElement(this, name, attributes);

                } else
                      invalidElement(name, attributes);
          }

          @Override
        public void endElement(String namespaceURI, String localName,
                      String qName) {
                if (elementHandled.equals(qName)) {
                      if (multiValueHandler != null) {
                            properties.put(name, multiValueHandler.getValues());
                            multiValueHandler = null;
                      } else if (xmlValueHandler != null) {
                            properties.put(name, xmlValueHandler.getXML());
                            xmlValueHandler = null;
                      }
                      super.endElement(namespaceURI, localName, qName);
                }
          }

          private boolean isValidProperty(String name, Object value) {
                return (name != null && value != null);
          }
    }


    public synchronized void parse(InputStream input) throws IOException {
          try {
                initReader();
                EndpointDescriptionsHandler endpointDescriptionsHandler = new EndpointDescriptionsHandler();
                xmlReader.setContentHandler(new EndpointDescriptionDocHandler(
                            ENDPOINT_DESCRIPTIONS, endpointDescriptionsHandler));
                xmlReader.parse(new InputSource(input));
                endpointDescriptions = endpointDescriptionsHandler
                            .getEndpointDescriptions();
          } catch (SAXException e) {
              e.printStackTrace();
                throw new IOException(e.getMessage());
          } catch (ParserConfigurationException e) {
                throw new IOException(e.getMessage());
          } finally {
                input.close();
          }
    }
public List<Map<String,Object>> getEndpointDescriptions(){
    return endpointDescriptions;
}
    public static String makeSimpleName(String localName, String qualifiedName) {
          if (localName != null && localName.length() > 0)
                return localName;
          int nameSpaceIndex = qualifiedName.indexOf(":"); 
          return (nameSpaceIndex == -1 ? qualifiedName : qualifiedName
                      .substring(nameSpaceIndex + 1));
    }

    public void unexpectedElement(AbstractHandler handler, String element,
                Attributes attributes) {
    }

    public void unexpectedCharacterData(AbstractHandler handler, String cdata) {
    }

    public void unexpectedAttribute(String element, String attribute,
                String value) {
    }

    static int indexOf(String[] array, String value) {
          for (int i = 0; i < array.length; i += 1) {
                if (value == null ? array[i] == null : value.equals(array[i])) {
                      return i;
                }
          }
          return -1;
    }

    public void checkRequiredAttribute(String element, String name, Object value) {
    }

    public void duplicateElement(AbstractHandler handler, String element,
                Attributes attributes) {
          // ignore the duplicate element entirely because we have already logged
          // it
          new IgnoringHandler(handler);
    }


}
