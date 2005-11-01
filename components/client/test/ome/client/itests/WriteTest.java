package ome.client.itests;

import ome.api.Write;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class WriteTest extends AbstractDependencyInjectionSpringContextTests {

	    /**
	     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	     */
	    protected String[] getConfigLocations() {
	        return new String[]{
	                "ome/client/spring.xml",
	                "ome/client/itests/test.xml",
	                "ome/client/itests/data.xml"
	                }; 
	    }
	    
	    Write w;
	    public void setWrite(Write write){
	    	w=write;
	    }
	    
	    public void testWriteWithRoleUser(){
	    	w.createDatasetAnnotation(new Integer(1),"Client-Side call");
	    }
	    
}
