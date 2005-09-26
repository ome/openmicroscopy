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

//Application-internal dependencies
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Experimenter;
import ome.model.Image;
import ome.model.ModuleExecution;
import ome.model.Project;
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

	private RulesEngine re;

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"WEB-INF/drools.xml"};
	}

	public void setEngine(RulesEngine eng){
		this.re = eng;
	}
	
	public void testAssertBaseObject() throws Exception{
		re.evaluate(new Object());
	}
	
	public void testTimeInTheFuture() throws Exception {
		Date d = new Date(System.currentTimeMillis()+1000000);
		Date d2 = new Date(d.getTime());
		re.evaluate(d);
		assertTrue(d.before(d2));
		
	}

	public void testWithGraph() throws Exception {
		Project p = new Project();
		Experimenter e = new Experimenter();
		ModuleExecution mex = new ModuleExecution();
		Date d = new Date(System.currentTimeMillis());
		String description = "blah blah";
		p.setExperimenter(e);
		p.setDescription(description);
		e.setModuleExecution(mex);
		mex.setTimestamp(d);
		
		re.evaluate(p);
		
	}

	public void testSameObjectTwice() throws Exception {
		Object o = new Object();
		re.evaluate(o,o);
	}
	
	public void testClassificationExclusivity() throws Exception {
		CategoryGroup cg = new CategoryGroup();
		Category c1 = new Category();
		Category c2 = new Category();
		Classification cla1 = new Classification();
		Classification cla2 = new Classification();
		Image i = new Image();
		
		cla1.setCategory(c1);
		cla1.setImage(i);
		
		cla2.setCategory(c2);
		cla2.setImage(i);

		c1.setCategoryGroup(cg);
		Set c1_cla = new HashSet();
		c1_cla.add(cla1);
		c1.setClassifications(c1_cla);
		c2.setCategoryGroup(cg);
		Set c2_cla = new HashSet();
		c2_cla.add(cla2);
		c2.setClassifications(c2_cla);
		
		Set s = new HashSet();
		s.add(c1);
		s.add(c2);
		
		cg.setCategories(s);
		
		try { 
			re.evaluate(cla1);
			fail("Rule did not catch the error.");
		} catch (ConsequenceException e){
			// good.
		}
		
		
	}
	
}


