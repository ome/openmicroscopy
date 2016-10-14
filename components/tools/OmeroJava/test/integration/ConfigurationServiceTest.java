/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import omero.api.IConfigPrx;
import omero.api.ITypesPrx;
import omero.model.Format;
import omero.model.IObject;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
            Set<String> values = new HashSet<String>();
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
            Assert.assertTrue(values.size() <= objects.size());
            for (int i = 0; i < objects.size(); i++) {
                Format o = (Format) objects.get(i);
                values.remove(o.getValue().getValue());
            }
            Assert.assertEquals(0, values.size());
        }
    }

    /**
     * Loads the enumeration.
     * @return Object[][] data.
     */
    @DataProvider(name = "loadEnumData")
    public Object[][] loadEnumData() throws Exception {
        List<TestEnum> testParams = new ArrayList<TestEnum>();
        Object[][] data = null;
        ITypesPrx svc =  root.getSession().getTypesService();
        List<IObject> objects = svc.getOriginalEnumerations();
        List<String> types = svc.getEnumerationTypes();
        Assert.assertTrue(types.size() > 0);
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            //tested via testSupportedFormats
            if (type.equals("Format")) {
                continue;
            }
            List<IObject> original = new ArrayList<IObject>();
            type = "omero.model."+type+"I";
            for (int j = 0; j < objects.size(); j++) {
                IObject ho = objects.get(j);
                if (ho.getClass().getName().equals(type)) {
                    original.add(ho);
                }
            }
            testParams.add(new TestEnum(type, original));
        }
        int index = 0;
        Iterator<TestEnum> j = testParams.iterator();
        data = new Object[testParams.size()][1];
        while (j.hasNext()) {
            data[index][0] = j.next();
            index++;
        }
        return data;
    }

    /**
     * Tests the retrieval of the various enumerations.
     * @throws Exception
     */
    @Test(dataProvider = "loadEnumData")
    public void testSupportedEnumerations(TestEnum param) throws Exception {
    
       ITypesPrx svc =  root.getSession().getTypesService();
       String type = param.getType();
       List<IObject> original = param.getOriginal();
       List<IObject> fromDB = svc.allEnumerations(type);
       if (type.endsWith("EventTypeI")) {
           //Bootstrap event is added to the DB during the init process
           //see psql-footer.vm
           Assert.assertEquals(fromDB.size()-1, original.size());
       } else {
           Assert.assertEquals(fromDB.size(), original.size());

       }
    }

    /**
     * Inner class hosting information about object to move.
     *
     */
    class TestEnum {

        /** Hold information about the object to move.*/
        private String type;

        private List<IObject> original;
        /**
         * Creates a new instance.
         *
         * @param chgrp Hold information about the object to move.
         * @param user The user to log as.
         * @param password The user's password.
         * @param srcID The identifier of the group to move the data from.
         */
        TestEnum(String type, List<IObject> original) {
            this.type = type;
            this.original = original;
        }

        /**
         * Returns the information object to move.
         *
         * @return See above.
         */
        String getType() {
            return type;
        }

        /**
         * Returns the user to log as.
         *
         * @return See above.
         */
        List<IObject> getOriginal() {
            return original;
        }

    }
}
