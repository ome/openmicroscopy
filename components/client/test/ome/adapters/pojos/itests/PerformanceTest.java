/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.adapters.pojos.itests;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import ome.api.IPojos;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.security.SecuritySystem;
import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.spring.ManagedServiceFactory;
import ome.util.builders.PojoOptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.2
 */
@Test(groups = { "client", "integration", "manual" })
public class PerformanceTest extends TestCase {

    protected static Log log = LogFactory.getLog(PerformanceTest.class);

    ServiceFactory factory = new ServiceFactory(new Login("root", "ome"));
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        factory = new ManagedServiceFactory();
        ((ManagedServiceFactory) factory).setApplicationContext(ctx);
        ((SecuritySystem) ctx.getBean("securitySystem")).login(new Principal(
                "root", "system", "Test"));
    }
    IPojos iPojos;
    Map options = new PojoOptions().countFields(
            new String[] { ImageAnnotation.IMAGE }).map();
    Map leaves = new PojoOptions(options).leaves().map();

    @Override
    @Configuration(beforeTestClass = true)
    protected void setUp() throws Exception {
        iPojos = factory.getPojosService();

    }

    @Test
    public void testSingle() throws Exception {
        dataset57();
    }

    //
    // @Test
    // public void testWithNamedMethods() throws Exception {
    // image1();
    // image1leaves();
    // dataset57();
    // dataset57leaves();
    // dataset64();
    // dataset64leaves();
    // }
    //    
    void image1() {
        iPojos.getImages(Image.class, Collections.singleton(1L), options);
    }

    void image1leaves() {
        iPojos.getImages(Image.class, Collections.singleton(1L), leaves);
    }

    void dataset57() {
        iPojos.getImages(Dataset.class, Collections.singleton(57L), options);
    }

    void dataset57leaves() {
        iPojos.getImages(Dataset.class, Collections.singleton(57L), leaves);
    }

    void dataset64() {
        iPojos.getImages(Dataset.class, Collections.singleton(64L), options);
    }

    void dataset64leaves() {
        iPojos.getImages(Dataset.class, Collections.singleton(64L), leaves);
    }

    //
    //    
    // @Test
    // public void testGetSomethingThatsAlwaysThere() throws Exception {
    //
    // Monitor img = null, dsM = null, dsL = null;
    // for (int i = 0; i < 1; i++) {
    //
    // System.out.println(i);
    //            
    // img = MonitorFactory.start("img");
    // iPojos.getImages(Image.class, Collections.singleton(1L), null);
    // img.stop();
    //
    // dsM = MonitorFactory.start("ds.most");
    // iPojos.getImages(Dataset.class, Collections.singleton(57L), null);
    // dsM.stop();
    //
    // dsL = MonitorFactory.start("ds.least"); // JUST 1
    // iPojos.getImages(Dataset.class, Collections.singleton(64L), null);
    // dsL.stop();
    // }
    //
    // System.out.println(str(img));
    // System.out.println(str(dsM));
    // System.out.println(str(dsL));
    //
    // }
    //
    // String str(Monitor m) {
    // if (m == null) {
    // return "";
    // }
    // return m.toString();
    // }
}
