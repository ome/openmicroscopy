/*
 * omeds.TestProject
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
import omeds.DBFixture;
import omeds.OMEDSTestCase;
import omeds.dbrows.ProjectRow;
import omeds.dbrows.DatasetRow;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Project;

/** 
 * 
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
public class TestProject
	extends OMEDSTestCase
{

	private ProjectRow		projectRow;
	private DatasetRow		datasetRow;
	
	/* (non-Javadoc)
	 * @see omeds.OMEDSTestCase#prepareFixture(java.sql.Connection)
	 */
	protected DBFixture prepareFixture()
	{
		// TODO Auto-generated method stub
		DBFixture dbFixture = new DBFixture();
		projectRow = new ProjectRow(null, null, "project insert", 1, 
									"lundi matin");
		datasetRow = new DatasetRow(false, null," dataset insert", 1,
									 "toujours le vieux discours. ");							
		dbFixture.add(projectRow);
		dbFixture.add(datasetRow);
		
		return dbFixture;
	}
	
	public void testRetrieveProject()
	{
		
		
		Criteria c = ProjectCriteriaFactory.buildProjectCriteria();
		int projectID = projectRow.getID();
		Project x = (Project) omeds.load(Project.class, projectID, c);
		System.out.println("ID: "+x.getID());
		System.out.println("Name: "+x.getName());
		System.out.println("description: "+x.getDescription());
		
		/*
		System.out.println("owner id: "+x.getOwner().getID());
		System.out.println("owner First Name: "+x.getOwner().getFirstName());
		System.out.println("owner Last Name: "+x.getOwner().getLastName());
		System.out.println("owner e-mail: "+x.getOwner().getEmail());
		System.out.println("owner institution: "+x.getOwner().getInstitution());
		
		
		System.out.println("group owner id: "+x.getOwner().getGroup().getID());
		System.out.println("group owner name: "+x.getOwner().getGroup().getName());
		Iterator i = x.getDatasets().iterator();

		while (i.hasNext()) {
			Dataset d = (Dataset) i.next();
			System.out.println("dataset id: "+d.getID());
			System.out.println("dataset name: "+d.getName());
		}
		*/
	}
	
	/**
	 * Counts the number of datasets linked to a given project.
	 *
	 */
	public void testDatasetsInProject()
	{
		Criteria c = new Criteria();
		c.addWantedField("#datasets");
	}
	
	
	
	public static void main(String[] args) 
	{
		TestProject x = new TestProject();
		x.setUp();
		x.testRetrieveProject();
		//x.tearDown();
	}
}
