/*
 * ome.server.itests.query.pojos.AnalysisLogicTests
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
package ome.server.itests.query.pojos;

//Java imports
import java.util.Set;

//Third-party libraries
import ome.server.itests.AbstractInternalContextTest;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.CreatePojosFixture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

//Application-internal dependencies

/** 
 * 
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
@Test(
        groups = "integration"
)
public class AnalysisLogicTest
        extends
            AbstractInternalContextTest {

    private static Log log = LogFactory.getLog(AnalysisLogicTest.class);

    CreatePojosFixture DATA;
    
    @Configuration( beforeTestClass = true )
    public void makePojos() throws Exception
    {
    	try {
    		setUp();
    		DATA = new CreatePojosFixture( this.serviceFactory );
    		DATA.pdi();
    	} finally {
    		tearDown();
    	}
    }
    
    @Test
    public void testGetProjectsForUser(){
    	Set s = iAnalysis.getProjectsForUser(1);
    	assertTrue(notNull,s.size()>0);
    }
    
    @Test
    public void testAllDatasets(){
    	Set s = iAnalysis.getAllDatasets();
    	assertTrue(notNull,s.size()>0);
    }

// TODO   
//  @Test
//	public void testChainExecutionsForDataset() {
//		Set s = iAnalysis.getChainExecutionsForDataset(1);
//		assertTrue(notNull,s.size()>0);
//	}
	
    @Test
	public void testDsFromPs(){
		Set s = iAnalysis.getDatasetsForProject(DATA.pu9992.getId());
		assertTrue(notNull, s.size()>0);
	}

    @Test
	public void testPsFromDs(){
		Set s = iAnalysis.getProjectsForDataset(DATA.du7772.getId());
		assertTrue(notNull, s.size()>0);
	}

    @Test
	public void testIsFromDs(){
		Set s = iAnalysis.getImagesForDataset(DATA.du7772.getId());
		assertTrue(notNull, s.size()>0);
	}
	
	private final static String notNull = "There has to be something";
	
}
