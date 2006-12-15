package ome.dsl.utests;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.*;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.dsl.SaxReader;
import ome.dsl.SemanticType;
import ome.dsl.VelocityHelper;
import junit.framework.TestCase;

public class ExampleUsageTest extends TestCase {

    private static Log log = LogFactory.getLog(ExampleUsageTest.class);

    SaxReader sr;

    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        File f = ResourceUtils.getFile("classpath:type.xml");
        sr = new SaxReader(f);
    }

    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception {
        sr = null;
    }

    @Test
    public void testONE() {
        sr.parse();
        Set set = sr.process();
        log.info("Results of parse:" + set);
        for (Iterator it = set.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            Map h = new HashMap();
            h.put("test", "this was a dynamic key test");
            vh.put("test", h);
            String s = vh.invoke("ome/dsl/mapping.vm");
            log.info("Results of invoke:" + s);
        }

    }

    @Test
    public void testWithWriting() throws Exception {
        sr.parse();
        Set set = sr.process();
        for (Iterator it = set.iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            // FileWriter fw = new
            // FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
            StringWriter sw = new StringWriter();
            vh.invoke("ome/dsl/mapping.vm", sw);
            sw.flush();
            sw.close();
            // fw.flush();
            // fw.close();
        }

    }

    @Test
    public void testPostProcessingInverse() throws Exception {
        sr.parse();
        Set<SemanticType> set = sr.process();
        Map<String, SemanticType> map = setToMap(set);
        SemanticType thumbnail = map.get("ome.Thumbnail");
        assertTrue(thumbnail.getProperties().iterator().next().getInverse()
                .equals("thumbnails"));
    }

    /** disabling; need proper logic to find common/ component FIXME */
    public void DISABLEDtestReal() throws Exception {
        File currentDir = new File(System.getProperty("user.dir"));// TODO Util
        File mappings = new File(currentDir.getParent() + File.separator
                + "common" + File.separator + "resources" + File.separator
                + "Mappings.ome.xml"); // FIXME circular deps.
        log.error(mappings);
        SaxReader nsr = new SaxReader(mappings);
        nsr.parse();
        for (Iterator it = nsr.process().iterator(); it.hasNext();) {
            SemanticType st = (SemanticType) it.next();
            VelocityHelper vh = new VelocityHelper();
            vh.put("type", st);
            // FileWriter fw = new
            // FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
            StringWriter sw = new StringWriter();
            vh.invoke("ome/dsl/mapping.vm", sw);
            sw.flush();
            sw.close();
            // fw.flush();
            // fw.close();
        }
    }

    // ~ Helpers
    // =========================================================================
    private Map<String, SemanticType> setToMap(Set<SemanticType> set) {
        Map<String, SemanticType> map = new HashMap<String, SemanticType>();
        for (SemanticType type : set) {
            map.put(type.getId(), type);
        }
        return map;
    }
}
