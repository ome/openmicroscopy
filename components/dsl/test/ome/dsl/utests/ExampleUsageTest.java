/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl.utests;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ome.dsl.DSLTask;
import ome.dsl.Property;
import ome.dsl.SaxReader;
import ome.dsl.SemanticType;
import ome.dsl.VelocityHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExampleUsageTest extends TestCase {

    private static Log log = LogFactory.getLog(ExampleUsageTest.class);

    SaxReader sr;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        File f = ResourceUtils.getFile("classpath:type.xml");
        sr = new SaxReader("psql", f);
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        sr = null;
    }

    @Test
    public void testONE() {
        sr.parse();
        List list = sr.process();
        log.info("Results of parse:" + list);
        for (Iterator it = list.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            Map h = new HashMap();
            h.put("test", "this was a dynamic key test");
            vh.put("test", h);
            String s = vh.invoke(DSLTask.getStream("ome/dsl/object.vm"));
            log.info("Results of invoke:" + s);
        }

    }

    @Test
    public void testWithWriting() throws Exception {
        sr.parse();
        List list = sr.process();
        for (Iterator it = list.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            // FileWriter fw = new
            // FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
            StringWriter sw = new StringWriter();
            vh.invoke(DSLTask.getStream("ome/dsl/object.vm"), sw);
            sw.flush();
            sw.close();
            // fw.flush();
            // fw.close();
        }

    }

    @Test
    public void testPostProcessingInverse() throws Exception {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        SemanticType thumbnail = map.get("ome.Thumbnail");
        assertTrue(thumbnail.getProperties().iterator().next().getInverse()
                .equals("thumbnails"));
    }

    @Test
    public void testPostProcessingBidirectional() throws Exception {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        SemanticType job = map.get("ome.Job");
        for (Property p : job.getProperties()) {
            if (p.getName().equals("jobThingLink")) {
                assertFalse(p.getBidirectional());
            } else if (p.getName().equals("jobDoohickeyLink")) {
                assertTrue(p.getBidirectional());
            } else if (p.getName().equals("details")) {
                // Ignore this generated property
            } else {
                fail("Unknown property:" + p);
            }
        }
    }

    @Test
    public void testOrder() throws Exception {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        SemanticType ot = map.get("pkg.ordertest");
        List<String> order = new LinkedList<String>(Arrays.asList("pixels",
                "cccccc", "aaaaaa", "bbbbbb", "images"));
        for (Property p : ot.getProperties()) {
            if (p.getName().equals("details")) {
                // skip this generated property
            } else if (p.getName().equals(order.get(0))) {
                order.remove(0);
            } else {
                fail(p.getName() + "!=" + order.get(0));
            }
        }
    }

    /** disabling; need proper logic to find common/ component FIXME */
    public void DISABLEDtestReal() throws Exception {
        File currentDir = new File(System.getProperty("user.dir"));// TODO Util
        File mappings = new File(currentDir.getParent() + File.separator
                + "common" + File.separator + "resources" + File.separator
                + "Mappings.ome.xml"); // FIXME circular deps.
        log.error(mappings.toString());
        SaxReader nsr = new SaxReader("psql", mappings);
        nsr.parse();
        for (Iterator it = nsr.process().iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            // FileWriter fw = new
            // FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
            StringWriter sw = new StringWriter();
            vh.invoke(DSLTask.getStream("ome/dsl/mapping.vm"), sw);
            sw.flush();
            sw.close();
            // fw.flush();
            // fw.close();
        }
    }

    // ~ Helpers
    // =========================================================================
    private Map<String, SemanticType> toMap(Collection<SemanticType> coll) {
        Map<String, SemanticType> map = new HashMap<String, SemanticType>();
        for (SemanticType type : coll) {
            map.put(type.getId(), type);
        }
        return map;
    }
}
