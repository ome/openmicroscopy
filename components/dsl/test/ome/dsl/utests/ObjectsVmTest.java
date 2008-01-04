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
            String s = vh.invoke("ome/dsl/object.vm");
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
