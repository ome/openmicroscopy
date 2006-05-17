/*
 * ome.server.itests.PojosServiceTest
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
package ome.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.Pojos;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.model.VirtualMexMap;
import ome.security.Utils;
import ome.testing.OMEData;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.builders.PojoOptions;

/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.5
 */
public class VirtualMexTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    protected static Log log = LogFactory.getLog(VirtualMexTest.class); 
    
    protected OMEData data;
    
    protected SessionFactory sf;
    
    protected HibernateTemplate ht;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations(); 
    }

    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	Utils.setUserAuth();
        data = (OMEData) applicationContext.getBean("data");
        sf = (SessionFactory) applicationContext.getBean("sessionFactory");
        ht = new HibernateTemplate(sf);
    }

    public void test_getVMexWithMex() throws Exception
    {
        VirtualMexMap v = (VirtualMexMap)
            ht.find("from VirtualMexMap v join fetch v.id.moduleExecution m " +
                    "where m.moduleExecutionId = 18").get(0);
        System.out.println(v.getId().getModuleExecution().getStatus());
    }
    
    public void test_getAllVMexes() throws Exception
    {
        List vmexes = ht.loadAll(VirtualMexMap.class);
    }
    
    public void test_getVMexByMex() throws Exception
    {
        List v = 
            ht.find("from VirtualMexMap v where v.id.moduleExecution.moduleExecutionId = 18");
    }


}
