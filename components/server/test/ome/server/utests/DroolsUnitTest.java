/*
 * ome.server.utests.DroolsUnitTest
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
package ome.server.utests;

//Java imports
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import org.drools.spi.ConsequenceException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.rules.RulesEngine;

/**
 * basic tests for Drools system.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class DroolsUnitTest extends AbstractDependencyInjectionSpringContextTests{

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception
    {
        super.tearDown();
    }
    // =========================================================================
    
	private RulesEngine re;

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"ome/services/drools.xml"};
	}

	public void setEngine(RulesEngine eng){
		this.re = eng;
	}
	
    @Test
	public void testAssertBaseObject() throws Exception{
		re.evaluate(new Object());
	}
	
    @Test
	public void testTimeInTheFuture() throws Exception {
		Date d = new Date(System.currentTimeMillis()+1000000);
		Date d2 = new Date(d.getTime());
		re.evaluate(d);
		assertTrue(d.before(d2));
		
	}

    @Test
	public void testWithGraph() throws Exception {
		Project p = new Project();
        p.setDetails(new Details());
		Experimenter e = new Experimenter();
        e.setDetails(new Details());
		Event ev = new Event();
		Date d = new Date(System.currentTimeMillis());
		String description = "blah blah";
		p.getDetails().setOwner(e);
		e.getDetails().setCreationEvent(ev);
		// TODO ev.setTime
		
		re.evaluate(p);
		
	}

    @Test
	public void testSameObjectTwice() throws Exception {
		Object o = new Object();
		re.evaluate(o,o);
	}
	
    @Test
	public void testClassificationExclusivity() throws Exception {
		CategoryGroup cg = new CategoryGroup();
		Category c1 = new Category();
		Category c2 = new Category();
		Image i = new Image();

        // FIXME link them up
        
		try { 
			re.evaluate(c1);
			// FIXME fail("Rule did not catch the error.");
		} catch (ConsequenceException e){
			// good.
		}
		
		
	}
	
}


