/*
 * blitzgateway.service.gateway.IRenderingSettingsGateway 
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

//Third-party libraries

//Application-internal dependencies

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
public interface IRenderingSettingsGateway
{	
	
/*
 * 
 *
 *
 *
 * boolean sanityCheckPixels(omero.model.Pixels pFrom, omero.model.Pixels pTo) throws ServerError;
   omero.model.RenderingDef getRenderingSettings(long pixelsId) throws ServerError;
   omero.model.RenderingDef createNewRenderingDef(omero.model.Pixels pixels) throws ServerError;
   void resetDefaults(omero.model.RenderingDef def, omero.model.Pixels pixels) throws ServerError;
   void resetDefaultsNoSave(omero.model.RenderingDef def, omero.model.Pixels pixels) throws ServerError;
   void resetDefaultsInImage(long imageId) throws ServerError;
   omero.sys.LongList resetDefaultsInCategory(long categoryId) throws ServerError;
   omero.sys.LongList resetDefaultsInDataset(long dataSetId) throws ServerError;
   omero.sys.LongList resetDefaultsInSet(string type, omero::sys::LongList noteIds) throws ServerError;
   void applySettingsToSet(long from, string toType, IObjectList to) throws ServerError;
   BooleanIdListMap applySettingsToProject(long from, long to) throws ServerError;
   BooleanIdListMap applySettingsToDataset(long from, long to) throws ServerError;
   BooleanIdListMap applySettingsToCategory(long from, long to) throws ServerError;
   boolean applySettingsToImage(long from, long to) throws ServerError;
   boolean applySettingsToPixels(long from, long to) throws ServerError;
   */
}


