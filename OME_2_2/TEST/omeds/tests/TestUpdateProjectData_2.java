/*
 * omeds.tests.TestUpdateProjectData_2
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package omeds.tests;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.DataInterface;
import org.openmicroscopy.ds.dto.Project;
import omeds.DBFixture;
import omeds.LoadRowCommand;
import omeds.OMEDSTestCase;
import omeds.SQLCommand;
import omeds.dbrows.ExperimenterRow;
import omeds.dbrows.GroupRow;
import omeds.dbrows.ProjectRow;

/** 
 * Update fields of a specified project.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TestUpdateProjectData_2
	extends OMEDSTestCase
{
	private GroupRow					groupRow;
	private ExperimenterRow				expRow;
	private ProjectRow					projectRow;
	
	protected DBFixture prepareFixture()
	{
		DBFixture dbFixture = new DBFixture();
		//Then create a experimenter.
		expRow = new ExperimenterRow("tester", "tester","/ome_files",
									"tester", "tester@toto.org", null);
		//Then a group.							
		groupRow = new GroupRow("ome 2", expRow);
		
		//create project.
		projectRow = new ProjectRow(groupRow, null, "test_update", expRow, 
									"test_update");
	
		SQLCommand lrc1, lrc2, lrc3, lrc4;
		lrc1 = new LoadRowCommand(expRow, dbFixture);
		lrc2 = new LoadRowCommand(groupRow, dbFixture);
		lrc3 = new SQLCommand(){
					public void execute()
						throws Exception
					{
						expRow.setGroupID(new Integer(groupRow.getID()));
						expRow.update();
					}
					//Do nothing b/c expRow and groupRow will be deleted.
					public void undo(){}
				};
		lrc4 = new LoadRowCommand(projectRow, dbFixture);
		dbFixture.enlist(lrc1);
		dbFixture.enlist(lrc2);
		dbFixture.enlist(lrc3);
		dbFixture.enlist(lrc4);
		return dbFixture;
	}
	
	
	public void testUpdateProjectData()
	{	
		int projectID = projectRow.getID();
		Criteria c = ProjectCriteriaFactory.buildProjectCriteria(projectID);
		Project p = (Project) omeds.createNew(Project.class);
		
		p.setID(projectID);
		String name = "test_update_name";
		String description = "test_update_description";
		p.setName(name);
		p.setDescription(description);
		omeds.update(p);
		Project p1 = (Project) omeds.retrieve(Project.class, c);
		//project data
		assertEquals(projectID, p1.getID());
		assertEquals(name, p1.getName());
		assertEquals(description, p1.getDescription());
	}
	
}

