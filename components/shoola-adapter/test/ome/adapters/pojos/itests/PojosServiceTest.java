package ome.adapters.pojos.itests;
/*
 * ome.adapters.pojos.utests.Model2PojosMapper
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
//Java imports

//Third-party libraries
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.framework.ProxyFactory;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.Pojos;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.testing.AbstractPojosServiceTest;
import ome.util.builders.PojoOptions;
import pojos.ImageData;


/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class PojosServiceTest
        extends
            AbstractPojosServiceTest {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class);
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
    	return new String[]{
    			"ome/client/spring.xml"};
    }    
    
    public void testMappingFindContainerHierarchies(){
    	Model2PojosMapper mapper = new Model2PojosMapper(); // TODO doc mem-leak
    	Set s = psrv.findContainerHierarchies(Project.class,ids,null);
    	log.info(mapper.map(s));
    }
    
    /* exaple of how to use */
    /* must pass in Image not ImageData.class */
    public void testMappingFindAnnotations(){
    	Map m = new Model2PojosMapper().map(psrv.findAnnotations(Image.class,ids,null)); 
    	log.info(m);
    }
    
    /* for a while repositories weren't being returned for images. now
     * they must be */
    public void testMappingImageServerUrl(){
    	List l = new ArrayList();
    	Collection c = new Model2PojosMapper().map(psrv.getUserImages(new PojoOptions().exp(new Integer(1)).map()));
    	for (Iterator it = c.iterator(); it.hasNext();) {
			ImageData img = (ImageData) it.next();
			if (img.getDefaultPixels().getImageServerURL()==null){
				l.add(img);
			}
		}
    	assertTrue("Images without repositories!",l.size()==0);
    }
    
}

