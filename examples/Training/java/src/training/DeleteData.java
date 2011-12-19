/*
 * training.DeleteData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package training;



//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.api.delete.DeleteReport;
import omero.grid.DeleteCallbackI;
import omero.model.Image;
import omero.model.ImageI;

/** 
 * Sample code showing how to delete data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class DeleteData 
	extends ConnectToOMERO
{

	/** 
	 * Delete Image.
	 * 
	 * In the following example, we create an image and delete it.
	 */
	private void deleteImage()
		throws Exception
	{
		//First create an image.
		Image img = new ImageI();
		img.setName(omero.rtypes.rstring("image1"));
		img.setDescription(omero.rtypes.rstring("descriptionImage1"));
		img.setAcquisitionDate(omero.rtypes.rtime(1000000));
		img = (Image) entryUnencrypted.getUpdateService().saveAndReturnObject(img);
		
		DeleteCommand[] cmds = new DeleteCommand[1];
		cmds[0] = new DeleteCommand("/Image", img.getId().getValue(), null);
		DeleteHandlePrx handle = entryUnencrypted.getDeleteService().queueDelete(cmds);
        DeleteCallbackI cb = new DeleteCallbackI(client, handle);
        
        int count = 10 * cmds.length;
        while (null == cb.block(500)) {
            count--;
            if (count == 0) {
                throw new RuntimeException("Waiting on delete timed out");
            }
        }
        DeleteReport[] reports = handle.report();
        for (int i = 0; i < reports.length; i++) {
        	DeleteReport report = reports[i];
        	String error = report.error;
        }
	}
	/**
	 * Connects and invokes the various methods.
	 */
	DeleteData()
	{
		try {
			connect();
			deleteImage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new DeleteData();
		System.exit(0);
	}

}
