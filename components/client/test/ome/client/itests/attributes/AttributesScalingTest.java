/*
 *   $Id$
 *   
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.attributes;

import java.util.ArrayList;
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
import ome.testing.Report;

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
@Test(enabled = true, groups = { "client", "integration", "attributes",
        "performance", "manual" })
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
                    annotation.setNs("scalingtest");
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
            Map<Long, Set<IObject>> map;

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

    private void assertAllAnnotations(Map<Long, Set<IObject>> map,
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
