/*
 * org.openmicroscopy.omero.server.itests.LeftOuterJoinTest
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
package org.openmicroscopy.omero.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries
import ome.api.Analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jmock.Mock;
import org.jmock.core.constraint.IsSame;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.jmock.core.stub.ReturnStub;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import org.openmicroscopy.omero.OMEModel;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.DaoCleanUpHibernate;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.logic.ReturnLogger;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.util.Utils;

import sun.security.krb5.internal.i;

/** 
 * tests for a HQL join bug.
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class AnalysisLogicTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(AnalysisLogicTest.class);
    Analysis ae;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        ae = (Analysis) applicationContext.getBean("analysisService");
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations();
    }

    public void testGetProjectsForUser(){
    	Set s = ae.getProjectsForUser(1);
    	assertTrue(notNull,s.size()>0);
    }
 
    public void testAllDatasets(){
    	Set s = ae.getAllDatasets();
    	assertTrue(notNull,s.size()>0);
    }
    
	public void testChainExecutionsForDataset() {
		Set s = ae.getChainExecutionsForDataset(1);
		assertTrue(notNull,s.size()>0);
	}
	
	public void testDsFromPs(){
		Set s = ae.getDatasetsForProject(1);
		assertTrue(notNull, s.size()>0);
	}

	public void testPsFromDs(){
		Set s = ae.getProjectsForDataset(1);
		assertTrue(notNull, s.size()>0);
	}

	public void testIsFromDs(){
		Set s = ae.getImagesForDataset(1);
		assertTrue(notNull, s.size()>0);
	}
	
	private final static String notNull = "There has to be something";
	
}
