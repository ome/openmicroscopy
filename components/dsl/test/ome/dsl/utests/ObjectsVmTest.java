/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.dsl.utests;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

public class ObjectsVmTest extends TestCase {

    private static Log log = LogFactory.getLog(ObjectsVmTest.class);

    SaxReader sr;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        File f = ResourceUtils.getFile("classpath:type.xml");
        sr = new SaxReader(f);
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        sr = null;
    }

    @Test
    public void testSimple() {
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
    public void testListItem() {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        SemanticType item = map.get("list.Item");
        assertTrue(item.getProperties().get(0).getInverse() != null);
    }

    @Test
    public void testAnnotations() {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        int counts = 0;
        
        SemanticType ann = map.get("ome.model.annotations.Annotation");
        for (Property property: ann.getPropertyClosure()) {
            if (isDetailsField(property)) {
                continue;
            }
            assertNotNull(property.toString(), property.getActualType());
            if (property.getName().equals("ns")) {
                assertEquals(ann, property.getActualType());
                counts++;
            }
        }
        
        SemanticType boo= map.get("ome.model.annotations.BooAnnotation");
        for (Property property: boo.getPropertyClosure()) {
            if (isDetailsField(property)) {
                continue;
            }
            assertNotNull(property.getActualType());
            if (property.getName().equals("ns")) {
                assertEquals(ann, property.getActualType());
                counts++;
            }
            if (property.getName().equals("boo")) {
                assertEquals(boo, property.getActualType());
                counts++;
            }
        }
        
        assertEquals(3, counts);
    }
    
    @Test
    public void testJobs() {
        sr.parse();
        List<SemanticType> list = sr.process();
        Map<String, SemanticType> map = toMap(list);
        int counts = 0;
        
        SemanticType job = map.get("ome.model.jobs.Job");
        for (Property property: job.getPropertyClosure()) {
            if (isDetailsField(property)) {
                continue;
            }
            assertNotNull(property.toString(), property.getActualType());
            if (property.getName().equals("originalFileLinks")) {
                assertEquals(job, property.getActualType());
                counts++;
            }
        }
        
        SemanticType script= map.get("ome.model.jobs.ScriptJob");
        for (Property property: script.getPropertyClosure()) {
            if (isDetailsField(property)) {
                continue;
            }
            assertNotNull(property.getActualType());
            if (property.getName().equals("originalFileLinks")) {
                assertEquals(job, property.getActualType());
                counts++;
            }
            if (property.getName().equals("description")) {
                assertEquals(script, property.getActualType());
                counts++;
            }
        }
        
        assertEquals(3, counts);
    }

    private boolean isDetailsField(Property property) {
        return property.getClass().getName().endsWith("DetailsField");
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
