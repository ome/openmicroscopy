/*
 *   $Id$
 *   
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;
import ome.api.IPojos;
import ome.model.IObject;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Test(groups = { "client", "integration", "attributes", "performance", "manual" })
public class AttributesScalingTest extends TestCase {

    protected static Log log = LogFactory.getLog(AttributesScalingTest.class);

    ServiceFactory factory = new ServiceFactory("ome.client.test");

    IPojos iPojos;

    Random random = new Random();

    @Override
    @BeforeClass
    protected void setUp() throws Exception {

        MonitorFactory.reset();

        iPojos = factory.getPojosService();
        // iPojos = (IPojos) MonProxyFactory.monitor(iPojos);

        Login rootLogin = (Login) factory.getContext().getBean("rootLogin");
        ServiceFactory rootFactory = new ServiceFactory(rootLogin);
        // Called since we are regularly reseting the DB for this.
        rootFactory.getAdminService().synchronizeLoginCache();

    }

    @Test
    public void testImagesAndAnnotations() throws Exception {

        // design
        // 1. creating ever more images and image annotation
        // 2. make calls to findAnnotations
        // 3. see where the performance drops are

        for (int i = 0; i < 1000; i++) {

            List<Image> images = new ArrayList<Image>();
            for (int j = 0; j < 1000; j++) {

                String loop = String.format("scaling test (i:%d,j:%d)", i, j);

                Image image = new Image();
                image.setName(loop);

                for (int k = 0; k < random.nextInt(5); k++) {

                    TextAnnotation annotation = new TextAnnotation();
                    annotation.setName("scalingtest");
                    annotation.setTextValue(loop);
                    image.linkAnnotation(annotation);
                }

                images.add(image);
            }

            // WRITE
            Monitor write = MonitorFactory.start("write");
            IObject[] rv = iPojos.createDataObjects(images
                    .toArray(new Image[] {}), null);
            write.stop();

            Set<Long> ids;
            Map<Long, Set<? extends IObject>> map;

            // ALL
            ids = idSet(rv);
            Monitor findAll = MonitorFactory.start("findAll");
            map = iPojos.findAnnotations(Image.class, ids, null, null);
            findAll.stop();

            assertAllAnnotations(map, rv);

            // EACH
            for (IObject object2 : rv) {
                Image image = (Image) object2;
                ids = idSet(image);
                Monitor findEach = MonitorFactory.start("findEach");
                map = iPojos.findAnnotations(Image.class, ids, null, null);
                findEach.stop();
                assertAllAnnotations(map, image);
            }

            System.out.println(i + " ======================");
            System.out.println(new Report());

        }
    }

    private void assertAllAnnotations(Map<Long, Set<? extends IObject>> map,
            IObject... rv) {
        for (IObject object : rv) {
            Image image = (Image) object;
            int size = image.sizeOfAnnotationLinks();
            if (size == 0) {
                assertFalse(map.containsKey(image.getId()));
            } else {
                assertEquals(size, map.get(image.getId()).size());
            }
        }
    }

    // Helpers ~
    // =========================================================================

    protected <T extends IObject> Set<Long> idSet(T... arr) {
        Set<Long> rv = new HashSet<Long>();
        for (IObject object : arr) {
            rv.add(object.getId());
        }
        return rv;
    }

}

class Report {

    static int LABEL = 0;
    static int HITS = 1;
    static int AVG = 2;
    static int TOTAL = 3;
    static int STDDEV = 4;
    static int LASTVALUE = 5;
    static int MIN = 6;
    static int MAX = 7;
    static int ACTIVE = 8;
    static int AVGACTIVE = 9;
    static int MAXACTIVE = 10;
    static int FIRSTACCESS = 11;
    static int LASTACCESS = 12;

    String[] header;
    Object[][] data;

    /**
     * Saves the current data from {@link MonitorFactory} and then resets all
     * values.
     */
    public Report() {
        header = MonitorFactory.getHeader();
        data = MonitorFactory.getData();
        MonitorFactory.reset();
    }

    @Override
    public String toString() {
        int[] labels = new int[] { AVG, TOTAL, MIN, MAX, LABEL, HITS };
        StringBuilder sb = new StringBuilder();
        for (int l : labels) {
            sb.append(header[l]);
            for (int i = 0; i < 8 - header[l].length(); i++) {
                sb.append(" ");
            }
            sb.append("\t");
        }
        sb.append("\n");

        Map<String, String> ordering = new HashMap<String, String>();
        for (int i = 0; i < data.length; i++) {
            StringBuilder line = new StringBuilder();
            for (int l : labels) {
                Object d = data[i][l];
                if (d instanceof Double) {
                    line.append(String.format("%3.2e\t", (Double) d));
                } else {
                    line.append(d + "\t");
                }
            }
            line.append("\n");
            ordering.put((String) data[i][LABEL], line.toString());
        }
        List<String> keys = new ArrayList<String>(ordering.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            sb.append(ordering.get(key));
        }

        return sb.toString();
    }

}
