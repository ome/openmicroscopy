package ome.dsl.utests;

import java.io.FileWriter;
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
	
	protected void setUp() throws Exception {
		sr = new SaxReader("type.xml");
	}
	
	protected void tearDown() throws Exception {
		sr = null;
	}
	
	public void testONE() {
		Set set = sr.parse();
		log.info("Results of parse:"+set);
		for (Iterator it = set.iterator(); it.hasNext();) {
			SemanticType st = (SemanticType) it.next();
			VelocityHelper vh = new VelocityHelper();
			vh.put("type",st);
			Map h = new HashMap();
			h.put("test", "this was a dynamic key test");
			vh.put("test", h);
			String s = vh.invoke("ome/dsl/mapping.vm");
			log.info("Results of invoke:"+s);
		}

	}
	
	public void testWithWriting() throws Exception{
		Set set = sr.parse();
		for (Iterator it = set.iterator(); it.hasNext();) {
			SemanticType st = (SemanticType) it.next();
			VelocityHelper vh = new VelocityHelper();
			vh.put("type",st);
			FileWriter fw = new FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
			vh.invoke("ome/dsl/mapping.vm",fw);
			fw.flush();
			fw.close();
		}
		
	}
	
	public void testReal() throws Exception {
		SaxReader nsr = new SaxReader("Mappings.xml");
		for (Iterator it = nsr.parse().iterator(); it.hasNext();) {
			SemanticType st = (SemanticType) it.next();
			VelocityHelper vh = new VelocityHelper();
			vh.put("type",st);
			FileWriter fw = new FileWriter("/tmp/"+st.getId().replaceAll("[.]","_")+".hbm.xml");
			vh.invoke("ome/dsl/mapping.vm",fw);
			fw.flush();
			fw.close();
		}
	}
	
}
