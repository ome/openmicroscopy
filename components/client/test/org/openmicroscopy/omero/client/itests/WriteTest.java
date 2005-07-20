package org.openmicroscopy.omero.client.itests;

import org.openmicroscopy.omero.interfaces.Write;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class WriteTest extends AbstractDependencyInjectionSpringContextTests {

	    /**
	     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	     */
	    protected String[] getConfigLocations() {
	        return new String[]{
	                "org/openmicroscopy/omero/client/spring.xml",
	                "org/openmicroscopy/omero/client/itests/test.xml",
	                "org/openmicroscopy/omero/client/itests/data.xml"
	                }; 
	    }
	    
	    Write w;
	    public void setWrite(Write write){
	    	w=write;
	    }
	    
	    public void testWriteWithRoleUser(){
	    	w.createDatasetAnnotation(1,"Client-Side call");
	    }
	    
}
