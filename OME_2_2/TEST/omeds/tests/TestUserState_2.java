/*
 * omeds.tests.TestUserState2
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
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import omeds.DBFixture;
import omeds.DBManager;
import omeds.OMEDSTestCase;
import omeds.dbrows.ExperimenterRow;
import omeds.dbrows.UserStateRow;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.UserState;
import org.openmicroscopy.ds.st.Experimenter;

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
public class TestUserState_2
	extends OMEDSTestCase
{
	private ExperimenterRow				expRow;
	private UserStateRow				userStateRow;
	
	/* (non-Javadoc)
	 * @see omeds.OMEDSTestCase#prepareFixture(java.sql.Connection)
	 */
	protected DBFixture prepareFixture()
	{
		return null;
	}
	
	public void testUserState_2()
		throws Exception
	{
		
		//Build the criteria.
		Criteria criteria = new Criteria();

		//Specify which fields we want for the ome_sessions.
		criteria.addWantedField("id");
		criteria.addWantedField("experimenter");
		
		//Specify which field we want for the experimenter.
		criteria.addWantedField("experimenter", "id");

		UserState us = (UserState) omeds.getUserState(criteria);
		Experimenter e = us.getExperimenter();
		
		DBManager dbm = DBManager.getInstance();
		
		//Check if ID retrieve via the remote framework is the same as the 
		//one in DBManager.
		assertTrue(e.getID() == dbm.getUserID());
		
		//create an experimenterRow and an userStateRow object.
		expRow = new ExperimenterRow("tester", "tester","/ome_files",
									  "tester", "tester@toto.org", null);
		Timestamp lastAccess, started;
		lastAccess = new Timestamp(System.currentTimeMillis());
		started = new Timestamp(System.currentTimeMillis());
		userStateRow = new UserStateRow(expRow, lastAccess, started);
		userStateRow.fillFromDB(us.getID());
		
		//compare userID one retrieved via RF and one retrieve by a DB call.
		assertTrue(e.getID() == userStateRow.getExperimenterRow().getID());
		
	}

}