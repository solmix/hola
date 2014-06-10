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
import java.util.List;
import java.util.Map;

import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.osgi.rsa.EndpointDescriptionReader;
import org.solmix.hola.osgi.rsa.EndpointDescriptionReaderException;
import org.solmix.hola.osgi.rsa.HolaEndpointDescription;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月13日
 */

public class EndpointDescriptionReaderImpl implements EndpointDescriptionReader
{
private static final Logger LOG= LoggerFactory.getLogger(EndpointDescriptionReaderImpl.class.getName());
   
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.EndpointDescriptionReader#readEndpointDescriptions(java.io.InputStream)
     */
    @Override
    public EndpointDescription[] readEndpointDescriptions(InputStream input)
        throws IOException, EndpointDescriptionReaderException {
        EndpointDescriptionParser parser = new EndpointDescriptionParser();
        // Parse input stream
        parser.parse(input);
        // Get possible endpoint descriptions
        List<Map<String,Object>> parsedDescriptions = parser.getEndpointDescriptions();
        List<HolaEndpointDescription> results = new ArrayList<HolaEndpointDescription>();
        for (Map<String,Object> parsedProperties : parsedDescriptions) {
              try {
                    results.add(new HolaEndpointDescription(parsedProperties));
              } catch (Exception e) {
                  LOG.error("Exception parsing endpoint description properties", e); 
                    throw new IOException("Error creating endpoint description: " + e.getMessage());
              }
        }
        return results.toArray(new HolaEndpointDescription[results.size()]);
    }

}
