/*
 * integration.BasicTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package integration;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import omero.model.IObject;
import omero.model.Image;
import omero.model.LightSourceSettings;
import omero.model.Pixels;
import omero.sys.Parameters;
import omero.sys.ParametersI;

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class BasicTest 
extends AbstractServerTest
{

	/**
	 * Creates a basic image. Pixels need to be created.
	 * @throws Exception
	 */
	public void testImage() 
	throws Exception
	{
		Image image = mmFactory.createImage();
		Pixels pixels = (Pixels) iUpdate.saveAndReturnObject(image.getPixels());
		String sql = "select pix from Pixels as pix " +
		"join fetch pix.image " +
		"join fetch pix.type " +
		"join fetch pix.channels as c " +
		"where pix.id in (:ids)";
		ParametersI p = new ParametersI();
		p.addIds(Arrays.asList(pixels.getId().getValue()));
		
	    List<IObject> l = iQuery.findAllByQuery(sql, p);
	    assertTrue(l.size() == 1);
	    assertTrue(l.get(0).getId().getValue() == pixels.getId().getValue());
	
	}
	
	/**
	 * Creates a basic image. Pixels need to be created.
	 * @throws Exception
	 */
	public void testLightSource() 
	throws Exception
	{
		LightSourceSettings settings = mmFactory.createLightSettings(
				mmFactory.createLaser());
		iUpdate.saveAndReturnObject(settings);
	}
	
	/** Creates a basic pixels. Image needs to be created.
	 * @throws Exception
	 */
	public void testPixels() 
	throws Exception
	{
		iUpdate.saveAndReturnObject(mmFactory.createPixels());
	}
	
}
