package org.solmix.hola.core.identity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.common.identity.ID;
import org.solmix.hola.common.identity.IDCreateException;
import org.solmix.hola.common.identity.Namespace;
import org.solmix.hola.common.identity.support.GUID;
import org.solmix.hola.common.internal.DefaultIDFactory;


public class GUIDTest extends AbstractIDTestCase
{
    
    @Test
    public void testCreate() {
        ID id= createID();
        Assert.assertNotNull(id);
    }
    @Test
    public void testCreatefixed() {
        ID id= createID(80);
        Assert.assertNotNull(id);
    }

    @Test
    public void testGetName() {
        ID id= createID();
        Assert.assertNotNull(id.getName());
    }

    @Test
    public void testToQueryString() {
        ID id= createID();
        Assert.assertNotNull(id.toQueryString());
    }

    @Test
    public void testToString() {
        ID id= createID();
        Assert.assertNotNull(id.toString());
    }
    @Test
    public void testHashCode() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        assertTrue(id1.hashCode() != id2.hashCode());
  }
    @Test
    public void testCompareToNotEqual() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        assertTrue(id1.compareTo(id2) != 0);
        assertTrue(id2.compareTo(id1) != 0);
  }
    @Test
  public void testGetNamespace() throws Exception {
        final ID id = createID();
        final Namespace ns = id.getNamespace();
        assertNotNull(ns);
  }
    @Test
  public void testEqualNamespaces() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        final Namespace ns1 = id1.getNamespace();
        final Namespace ns2 = id2.getNamespace();
        assertTrue(ns1.equals(ns2));
        assertTrue(ns2.equals(ns2));
  }
    @Test
    public void testSerializable() throws Exception {
          final ByteArrayOutputStream buf = new ByteArrayOutputStream();
          final ObjectOutputStream out = new ObjectOutputStream(buf);
          try {
                out.writeObject(createID());
          } catch (final NotSerializableException ex) {
                fail(ex.getLocalizedMessage());
          } finally {
                out.close();
          }
    }
    @Test
    public void testCreateFromQueryString() throws Exception {
          final ID id1 = createID();
          final String externalForm = id1.toQueryString();
          final ID id2 = DefaultIDFactory.getDefault().createID(id1.getNamespace(),
                      externalForm);
          assertTrue(id1.equals(id2));
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.AbstractIDTestCase#createID()
     */
    @Override
    protected ID createID() throws IDCreateException {
        return createID(GUID.DEFAULT_BYTE_LENGTH);
    }
    protected ID createID(int length) throws IDCreateException {
        
        return DefaultIDFactory.getDefault().createGUID(length);
    }
}
