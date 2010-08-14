/*
 * integration.RawPixelsStoreTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports

//Third-party libraries
import java.util.ArrayList;
import java.util.List;

import omero.RLong;
import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

//Application-internal dependencies

/** 
 * Collections of tests for the <code>RawPixelsStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class RawPixelsStoreTest 
	extends AbstractTest
{

	/**
	 * Creates an image. This method has been tested in 
	 * <code>PixelsServiceTest</code>.
	 * 
	 * @return See above.
	 * @throws Exception Thrown if an error occurred.
	 */
	private Image createImage()
		throws Exception
	{
		IPixelsPrx svc = factory.getPixelsService();

    	List<IObject> types = 
    		svc.getAllEnumerations(PixelsType.class.getName());
    	List<Integer> channels = new ArrayList<Integer>();
    	for (int i = 0; i < 1; i++) {
			channels.add(i);
		}
    	
    	RLong id = svc.createImage(1, 1, 1, 1, channels, 
    			(PixelsType) types.get(1), "test", "");
    	//Retrieve the image.
    	ParametersI param = new ParametersI();
    	param.addId(id.getValue());
    	Image img = (Image) iQuery.findByQuery(
    			"select i from Image i where i.id = :id", param);
    	return (Image) iUpdate.saveAndReturnObject(img);
	}
	
    /**
     * Tests to set a plane and retrieve it, this method will test the 
     * <code>setPlane</code> and <code>getPlane</code>.
     * 
     * @throws Exception Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testSetGetPlane() 
    	throws Exception 
    {
    	Image image = createImage();
    	int v = 16;
    	Pixels pixels = image.getPrimaryPixels();
    	pixels.setSizeX(omero.rtypes.rint(v));
    	pixels.setSizeY(omero.rtypes.rint(v));
    	pixels.setSizeZ(omero.rtypes.rint(1));
    	pixels.setSizeT(omero.rtypes.rint(1));
    	pixels.setSizeC(omero.rtypes.rint(1));
    	pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
    	
    	RawPixelsStorePrx svc = factory.createRawPixelsStore();
    	svc.setPixelsId(pixels.getId().getValue(), false);
    	int size = svc.getPlaneSize();
    	assertTrue(size >= v*v);
    	byte[] data = new byte[size];
    	svc.setPlane(data, 0, 0, 0);
    	
    	byte[] r = svc.getPlane(0, 0, 0);
    	assertNotNull(r);
    	assertTrue(r.length == data.length);
    }
    
}
