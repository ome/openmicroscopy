/*
 * ome.server.itests.LeftOuterJoinTest
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
import java.util.Set;

//Third-party libraries
import ome.api.IAnalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies

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
    IAnalysis ae;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        ae = (IAnalysis) applicationContext.getBean("analysisService");
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

// TODO    
//	public void testChainExecutionsForDataset() {
//		Set s = ae.getChainExecutionsForDataset(1);
//		assertTrue(notNull,s.size()>0);
//	}
	
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
