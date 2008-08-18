/*
 * blitzgateway.service.gateway.IPixels 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.services.blitz.gateway;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.DSAccessException;
import omero.gateway.DSOutOfServiceException;

import omero.RObject;
import omero.model.Pixels;
import omero.model.PixelsType;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IPixelsGateway
{	
	/**
	 * Get the pixels description for pixId.
	 * @param pixId see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	 Pixels retrievePixDescription(long pixId) 
	 						throws  DSOutOfServiceException, DSAccessException;
	 
	 /**
	  * Get the bit depth of the pixels.
	  * @param type pixel type.
	  * @return see above.
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	 int getBitDepth(PixelsType type) 
	 						throws  DSOutOfServiceException, DSAccessException;
	 
	 /**
	  * Get the enumeration for thepixel.
	  * @param enumClass see above.
	  * @param value see above.
	  * @return see above
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	 RObject getEnumeration(String enumClass, String value) 
	 						throws  DSOutOfServiceException, DSAccessException;
	 
	 /**
	  * Get all the nums for tyoe enumClass.
	  * @param <T> see above.
	  * @param enumClass see above.
	  * @return see above.
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	 <T extends omero.model.IObject>List<T> getAllEnumerations(String enumClass) 
	 						throws  DSOutOfServiceException, DSAccessException;
	/**
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsId pixels id to copy.
	 * @param sizeX width of plane.
	 * @param sizeY height of plane.
	 * @param sizeZ num Z sections
	 * @param sizeT num timepoints
	 * @param channelList list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	 long copyAndResizePixels(long pixelsId,
	                                 int sizeX,
	                                 int sizeY,
	                                 int sizeZ,
	                                 int sizeT,
									 List<Integer> channelList,
	                                 String methodology) 
	 						throws  DSOutOfServiceException, DSAccessException;
	
	 /**
		 * Copy the Image and its pixels from the copied image to a new image 
		 * and return to the .
		 * @param imageId pixels id to copy.
		 * @param sizeX width of plane.
		 * @param sizeY height of plane.
		 * @param sizeZ num Z sections
		 * @param sizeT num timepoints
		 * @param channelList list of channels to copy.
		 * @param methodology what created the pixels.
		 * @return new id.
		 * @throws DSOutOfServiceException
		 * @throws DSAccessException
		 */
		 long copyAndResizeImage(long imageId,
		                                 int sizeX,
		                                 int sizeY,
		                                 int sizeZ,
		                                 int sizeT,
										 List<Integer> channelList,
		                                 String methodology) 
		 						throws  DSOutOfServiceException, DSAccessException;
		
		 /**
		  * Create a new image of specified X,Y, Z, T and channels plus pixelsType
		  * with name and description 
		  * 
		  * @param sizeX
		  * @param sizeY
		  * @param sizeZ
		  * @param sizeT
		  * @param channelList
		  * @param pixelsType
		  * @param name
		  * @param description
		  * @return new image id.
		  * @throws DSOutOfServiceException
		  * @throws DSAccessException
		  */
		 Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                 List<Integer> channelList, 
                 omero.model.PixelsType pixelsType,
                 String name, String description) 	throws  DSOutOfServiceException, DSAccessException;

	 /*
	 * UpdateService java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IUpdate service. 
	 * As the are created in the IUpdateServiceGateway they will be marked
	 * as done.
	 * 
	 * 
	 * 
	 * 
	 *
DONE Pixels retrievePixDescription(long pixId) throws  DSOutOfServiceException, DSAccessException;
	 RenderingDef retrieveRndSettings(long pixId) throws  DSOutOfServiceException, DSAccessException;
	 void saveRndSettings(RenderingDef rndSettings) throws  DSOutOfServiceException, DSAccessException;
DONE int getBitDepth(PixelsType type) throws  DSOutOfServiceException, DSAccessException;
DONE RObject getEnumeration(String enumClass, String value) throws  DSOutOfServiceException, DSAccessException;
DONE List<IObject> getAllEnumerations(String enumClass) throws  DSOutOfServiceException, DSAccessException;
DONE RLong copyAndResizePixels(long pixelsId,
	                                 RInt sizeX,
	                                 RInt sizeY,
	                                 RInt sizeZ,
	                                 RInt sizeT,
									 List<RInt> channelList,
	                                 String methodology, boolean copyStatsinfo) throws  DSOutOfServiceException, DSAccessException;
DONE RLong copyAndResizeImage(long imageId,
	                                 RInt sizeX,
	                                 RInt sizeY,
	                                 RInt sizeZ,
	                                 RInt sizeT,
									 List<RInt> channelList,
	                                 String methodology, boolean copyStatsinfo) throws  DSOutOfServiceException, DSAccessException;
	 */
}


