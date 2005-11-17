package ome.client.utests;


//Java imports
import java.util.List;

//Third-party libraries
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

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
public class Preferences2Test extends AbstractDependencyInjectionSpringContextTests {

    static {
        System.getProperties().setProperty("test.system.value","This was set at class load.");
        System.getProperties().setProperty("test.file.value","An attempt to override.");
    }
	
	protected String[] getConfigLocations() {
		return new String[]{"ome/client/utests/no_props.xml"};
	}
	
	List l;
	public void setList(List list){
		this.l=list;
	}
	
	public void testArePreferencesSet(){
		// if this doesn't explode it's fine.
	}
	
	
	
}

