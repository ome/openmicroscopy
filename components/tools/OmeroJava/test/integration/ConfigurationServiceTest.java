/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.List;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import omero.api.IConfigPrx;
import omero.model.Format;
import omero.model.IObject;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>IConfig</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ConfigurationServiceTest extends AbstractServerTest {

    /** Helper reference to the <code>IConfig</code> service. */
    private IConfigPrx iConfig;

    /**
     * Initializes the various services.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        iConfig = factory.getConfigService();
    }

    /**
     * Tests the <code>getServerTime</code> method. Access the method as a non
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testServerTime() throws Exception {
        Assert.assertNotNull(iConfig.getServerTime());
    }

    /**
     * Tests the <code>getDatabaseTime</code> method. Access the method as a non
     * administrator.s
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDatabaseTime() throws Exception {
        Assert.assertNotNull(iConfig.getDatabaseTime());
    }

    /**
     * Tests the <code>getServerTime</code> method. Access the method as an
     * administrator
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testServerTimeAsAdmin() throws Exception {
        IConfigPrx svc = root.getSession().getConfigService();
        Assert.assertNotNull(svc.getServerTime());
    }

    /**
     * Tests the <code>getDatabaseTime</code> method. Access the method as an
     * administrator
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDatabaseTimeAsAdmin() throws Exception {
        IConfigPrx svc = root.getSession().getConfigService();
        Assert.assertNotNull(svc.getDatabaseTime());
    }

    /**
     * Tests the <code>getDatabaseTime</code> method. Access the method as an
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDatabaseUUIDAsAdmin() throws Exception {
        IConfigPrx svc = root.getSession().getConfigService();
        Assert.assertNotNull(svc.getDatabaseUuid());
    }

    /**
     * Tests the <code>getDatabaseUuid</code> method. Access the method as a non
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDatabaseUUID() throws Exception {
        Assert.assertNotNull(iConfig.getDatabaseUuid());
    }

    /**
     * Tests the <code>setConfigValue</code> method. Access the method as an
     * administrator
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSetConfigValueAsAdmin() throws Exception {
        String key = "test";
        String value = "test2";
        IConfigPrx svc = root.getSession().getConfigService();
        svc.setConfigValue(key, value);
        Assert.assertNotNull(svc.getConfigValue(key));
        Assert.assertEquals(svc.getConfigValue(key), value);
    }

    /**
     * Tests the <code>setConfigValue</code> method. Access the method as a non
     * admin user.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSetConfigValue() throws Exception {
        String key = "test1";
        String value = "test3";
        try {
            iConfig.setConfigValue(key, value);
            Assert.fail("A non admin user cannot configure the server");
        } catch (Exception e) {
        }
    }

    /**
     * Tests that the list of supported formats in the DB matches
     * what is currently supported by BioFormats.
     * This does not include the <code>FakeReader</code> since it is solely
     * used for testing.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSupportedFormats() throws Exception {
        final String ref = "Reader";
        List<String> toExclude = new ArrayList<String>();
        toExclude.add("Fake");
        try (final ImageReader reader = new ImageReader()) {
            IFormatReader[] readers = reader.getReaders();
            List<String> values = new ArrayList<String>();
            for (int i = 0; i < readers.length; i++) {
                IFormatReader r = readers[i];;
                String name = r.getClass().getSimpleName();
                if (name.endsWith(ref)) {
                    name = name.substring(0, name.length() - ref.length());
                    if (!toExclude.contains(name)) {
                        values.add(name);
                        if (r.hasCompanionFiles()) {
                            values.add("Companion/"+name);
                        }
                    }
                }
            }
            //Load from DB
            ParametersI param = new ParametersI();
            String sql = "select f from Format as f";
            List<IObject> objects = iQuery.findAllByQuery(sql, param);
            Assert.assertEquals(objects.size(), values.size());
            for (int i = 0; i < objects.size(); i++) {
                Format o = (Format) objects.get(i);
                String v = o.getValue().getValue();
                if (values.contains(v)) {
                    values.remove(v);
                }
            }
            Assert.assertEquals(0, values.size());
        }
    }
}
