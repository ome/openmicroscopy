/*
 * omeds.tests.TestImage
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
import omeds.LoadRowCommand;
import omeds.OMEDSTestCase;
import omeds.SQLCommand;
import omeds.dbrows.ExperimenterRow;
import omeds.dbrows.GroupRow;
import omeds.dbrows.ImageRow;
import omeds.dbrows.PixelsRow;
import omeds.dbrows.RepositoryRow;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.st.Pixels;
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
public class TestImage
	extends OMEDSTestCase
{

	private GroupRow			groupRow;
	private ExperimenterRow		expRow;
	private ImageRow			imageRow;
	private PixelsRow			pixelsRow;
	private RepositoryRow		repRow;
	/* (non-Javadoc)
	 * @see omeds.OMEDSTestCase#prepareFixture(java.sql.Connection)
	 */
	protected DBFixture prepareFixture()
	{	
		DBFixture dbFixture = new DBFixture();
		//Create an experimenter row in DB.
		expRow = new ExperimenterRow("tester", "tester","/ome_files",
									"tester", "tester@toto.org", null);
		//Then a group row in DB.							
		groupRow = new GroupRow("ome 2", expRow);
	
		//Create a repository row in DB.
		repRow = new RepositoryRow("http://runemaster.openmicroscopy.org");
		
		//Create an image row in DB.
		Timestamp created, inserted;
		created = new Timestamp(System.currentTimeMillis());
		inserted = new Timestamp(System.currentTimeMillis());
		imageRow = new ImageRow(created, groupRow, inserted,
								 "insert image", expRow, null, "image ");
		pixelsRow = new PixelsRow(imageRow, new Integer(256), new Integer(256), 
									new Integer(30), new Integer(2), 
									new Integer(1), new Integer(16), repRow);
									
		SQLCommand lrc1, lrc2, lrc3, lrc4, lrc5, lrc6, lrc7;
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
		lrc4 = new LoadRowCommand(imageRow, dbFixture);
		lrc5 = new LoadRowCommand(repRow, dbFixture);
		lrc6 = new LoadRowCommand(pixelsRow, dbFixture);
		lrc7 = new SQLCommand(){
					public void execute()
						throws Exception
					{
						imageRow.setPixelID(new Integer(pixelsRow.getID()));
						imageRow.update();
					}
					//Do nothing b/c expRow and groupRow will be deleted.
					public void undo(){}
				};
		dbFixture.enlist(lrc1);
		dbFixture.enlist(lrc2);
		dbFixture.enlist(lrc3);
		dbFixture.enlist(lrc4);
		dbFixture.enlist(lrc5);
		dbFixture.enlist(lrc6);	
		dbFixture.enlist(lrc7);		
									 							 							 						
		return dbFixture;
	}
	
	public void testRetrieveImage()
	{
	
		Criteria c = ImageCriteriaFactory.buildImageCriteria();
	
		int imageID = imageRow.getID();
		Image i = (Image) omeds.load(Image.class, imageID, c);
	
		//project data
		assertEquals(imageID, i.getID());
		assertEquals(imageRow.getName(), i.getName());
		assertEquals(imageRow.getDescription(), i.getDescription());
		assertEquals(imageRow.getCreatedtoString(), i.getCreated());
		assertEquals(imageRow.getInsertedtoString(), i.getInserted());
		
		
		//owner data.
		assertEquals(imageRow.getExperimenterRow().getID(), 
					i.getOwner().getID());
		assertEquals(imageRow.getExperimenterRow().getFirstName(),
					i.getOwner().getFirstName());
		assertEquals(imageRow.getExperimenterRow().getLastName(), 
					i.getOwner().getLastName());
		assertEquals(imageRow.getExperimenterRow().getEmail(),
					i.getOwner().getEmail());
		assertEquals(imageRow.getExperimenterRow().getInstitution(), 
					i.getOwner().getInstitution());
						
		//group data.
		assertEquals(imageRow.getGroupRow().getID(),
					i.getOwner().getGroup().getID());
		assertEquals(imageRow.getGroupRow().getName(), 
					i.getOwner().getGroup().getName());
					
		//pixels data.
		
		Pixels px = i.getDefaultPixels();
		assertEquals(pixelsRow.getID(), px.getID());
		assertEquals(pixelsRow.getSizeX().intValue(), 
					(px.getSizeX()).intValue());
		assertEquals(pixelsRow.getSizeY().intValue(), 
					(px.getSizeY()).intValue());
		assertEquals(pixelsRow.getSizeZ().intValue(), 
					(px.getSizeZ()).intValue());
		assertEquals(pixelsRow.getSizeC().intValue(), 
					(px.getSizeC()).intValue());
		assertEquals(pixelsRow.getSizeT().intValue(), 
					(px.getSizeT()).intValue());
		assertEquals(pixelsRow.getBitsPerPixel().intValue(), 
					(px.getBitsPerPixel()).intValue());
		assertEquals(repRow.getImageServerURL(), 
					px.getRepository().getImageServerURL());
	}

}
