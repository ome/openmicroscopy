package ome.client.utests;


//Java imports
import java.util.List;

//Third-party libraries
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

//Application-internal dependencies


/**
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class PreferencesTest extends AbstractDependencyInjectionSpringContextTests {

	static {
		System.getProperties().setProperty("test.system.value","This was set at class load.");
		System.getProperties().setProperty("test.file.value","An attempt to override.");
	}
	
	protected String[] getConfigLocations() {
		System.getProperties().setProperty("test.system.value","This was set at config.");
		return new String[]{"ome/client/utests/spring.xml"};
	}
	
	List l;
	public void setList(List list){
		this.l=list;
	}
	
    @Test
	public void testArePreferencesSet(){
		// if this doesn't explod it's fine
	}
    
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    void adapterSetup() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    void adapterTearDown() throws Exception
    {
        super.tearDown();
    }
	
	
	
}

