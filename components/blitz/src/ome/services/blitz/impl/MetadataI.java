/*
 * ome.services.blitz.impl.MetadataI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.services.blitz.impl;


//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import ome.api.IMetadata;
import ome.services.blitz.util.BlitzExecutor;
import omero.RInt;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IMetadata_loadAnnotations;
import omero.api.AMD_IMetadata_loadChannelAcquisitionData;
import omero.api.AMD_IMetadata_loadSpecifiedAnnotations;
import omero.api._IMetadataOperations;
import Ice.Current;

/** 
 * Implementation of the <code>IMetadata</code> service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MetadataI 
	extends AbstractAmdServant 
	implements _IMetadataOperations
{

	/**
	 * Creates a new instance.
	 * 
	 * @param service Reference to the service.
	 * @param be      The executor.
	 */
	 public MetadataI(IMetadata service, BlitzExecutor be)
	 {
		 super(service, be);
	 }

	 public void loadChannelAcquisitionData_async(
			 AMD_IMetadata_loadChannelAcquisitionData __cb, 
			 List<Long> ids, Current __current)
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, ids);
	 }

	 public void loadAnnotations_async(AMD_IMetadata_loadAnnotations __cb,
			 String rootType, List<Long> rootIds, List<String> annotationTypes, 
			 List<Long> annotatorIds, Current __current) 
	 throws ServerError {
		 callInvokerOnRawArgs(__cb, __current, rootType, rootIds, 
				 annotationTypes, annotatorIds);
	 }
	    
	 public void loadSpecifiedAnnotations_async(AMD_IMetadata_loadSpecifiedAnnotations __cb,
			 String annotationType, String nameSpace, List<Long> annotatorIds, 
			 boolean linkObjects, Current __current) 
	 throws ServerError {
		 callInvokerOnRawArgs(__cb, __current, annotationType, nameSpace, annotatorIds,
				 linkObjects);
	 }
}
