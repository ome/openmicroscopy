package ome.util.utests;

import org.testng.annotations.*;
import java.util.HashMap;

import ome.util.builders.PojoOptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

public class PojoOptionsTest extends TestCase {

	private static Log log = LogFactory.getLog(PojoOptionsTest.class);
	
	PojoOptions ob;
	
  @Configuration(beforeTestMethod = true)
	protected void setUp() throws Exception {
		ob=new PojoOptions();
	}
	
  @Test
	public void testDefaults(){
		log.info(ob.map());
	}
	
  @Test
	public void testAllMethods(){
		log.info(
		ob.allCounts()
		.countsFor(new Long(1))
		.noCounts()
		.exp(new Long(3))
		.allExps()
		.noLeaves()
		.map()
		);
	}
	
  @Test
	public void testNullMap(){
		ob = new PojoOptions(null);
        PojoOptions test = new PojoOptions();
		assertTrue("Should have default keys",
                ob.map().keySet().containsAll(test.map().keySet()));
		
		ob = new PojoOptions(new HashMap());
		assertTrue("Should be empty; this is the only way.",ob.map().keySet().size()==0);
	}
}
