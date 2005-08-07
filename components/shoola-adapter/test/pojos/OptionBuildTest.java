package pojos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

public class OptionBuildTest extends TestCase {

	private static Log log = LogFactory.getLog(OptionBuildTest.class);
	
	OptionBuilder ob;
	
	protected void setUp() throws Exception {
		ob=new OptionBuilder();
	}
	
	public void testDefaults(){
		log.info(ob.map());
	}
	
	public void testAllMethods(){
		log.info(
		ob.allAnnotations()
		.annotationsFor(new Integer(1))
		.noAnnotations()
		.exp(new Integer(3))
		.allExps()
		.noLeaves()
		.map()
		);
	}
	
}
