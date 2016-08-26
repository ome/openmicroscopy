/*
 *  $Id$
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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

import java.util.List;

import ome.api.IMetadata;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IMetadata_countAnnotationsUsedNotOwned;
import omero.api.AMD_IMetadata_countSpecifiedAnnotations;
import omero.api.AMD_IMetadata_getTaggedObjectsCount;
import omero.api.AMD_IMetadata_loadAnnotation;
import omero.api.AMD_IMetadata_loadAnnotations;
import omero.api.AMD_IMetadata_loadAnnotationsUsedNotOwned;
import omero.api.AMD_IMetadata_loadChannelAcquisitionData;
import omero.api.AMD_IMetadata_loadInstrument;
import omero.api.AMD_IMetadata_loadLogFiles;
import omero.api.AMD_IMetadata_loadSpecifiedAnnotations;
import omero.api.AMD_IMetadata_loadSpecifiedAnnotationsLinkedTo;
import omero.api.AMD_IMetadata_loadTagContent;
import omero.api.AMD_IMetadata_loadTagSets;
import omero.api.AMD_IMetadata_loadAnnotationCounts;
import omero.api._IMetadataOperations;
import omero.sys.Parameters;
import omero.util.IceMapper;
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
			 List<Long> annotatorIds, Parameters options, Current __current) 
	 throws ServerError {
		 try {
			 map(annotationTypes);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, rootType, rootIds, 
				 annotationTypes, annotatorIds, options);
	 }
	    
	 public void loadSpecifiedAnnotations_async(AMD_IMetadata_loadSpecifiedAnnotations __cb,
			 String annotationType, List<String> include, List<String> exclude,
			Parameters options, Current __current) 
	 throws ServerError {
		 try
		 {
			 annotationType = map(annotationType);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, annotationType, include, exclude, 
				 options);
	 }
	 
	 public void countSpecifiedAnnotations_async(AMD_IMetadata_countSpecifiedAnnotations __cb,
			 String annotationType, List<String> include, List<String> exclude,
			Parameters options, Current __current) 
	 throws ServerError {
		 try
		 {
			 annotationType = map(annotationType);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, annotationType, include, exclude, 
				 options);
	 }
	 
	 public void loadTagSets_async(AMD_IMetadata_loadTagSets __cb,  Parameters options,
			 Current __current) 
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, options);
	 }
	 
	 public void loadTagContent_async(AMD_IMetadata_loadTagContent __cb, 
			 List<Long> ids, Parameters options, Current __current) 
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, ids, options);
	 }
	 
	 public void getTaggedObjectsCount_async(AMD_IMetadata_getTaggedObjectsCount __cb, 
			 List<Long> ids, Parameters options, Current __current) 
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, ids, options);
	 }
	 
	 public void loadAnnotation_async(AMD_IMetadata_loadAnnotation __cb, 
			 List<Long> ids, Current __current) 
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, ids);
	 }

	 public void loadInstrument_async(AMD_IMetadata_loadInstrument __cb, 
			 long id, Current __current) 
	 throws ServerError 
	 {
		 callInvokerOnRawArgs(__cb, __current, id);
	 }
	 
	 public void countAnnotationsUsedNotOwned_async(AMD_IMetadata_countAnnotationsUsedNotOwned __cb,
			 String annotationType, long userID, Current __current) 
	 throws ServerError {
		 try
		 {
			 annotationType = map(annotationType);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, annotationType, userID);
	 }
	 
	 public void loadAnnotationsUsedNotOwned_async(AMD_IMetadata_loadAnnotationsUsedNotOwned __cb,  
			 String annotationType, long userID, Current __current) 
	 throws ServerError 
	 {
		 try
		 {
			 annotationType = map(annotationType);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, annotationType, userID);
	 }


	 public void loadSpecifiedAnnotationsLinkedTo_async(AMD_IMetadata_loadSpecifiedAnnotationsLinkedTo __cb,
			 String annotationType, List<String> include, List<String> exclude,
			String rootNodeType, List<Long> nodeIds, Parameters options, Current __current) 
	 throws ServerError {
		 try
		 {
			 annotationType = map(annotationType);
		 } catch (ServerError sr) {
			 __cb.ice_exception(sr);
			 return;
		 }
		 callInvokerOnRawArgs(__cb, __current, annotationType, include, exclude, 
				 rootNodeType, nodeIds, options);
	 }

    @Override
    public void loadLogFiles_async(AMD_IMetadata_loadLogFiles __cb,
            String rootType, List<Long> ids, Current __current)
                    throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, ids);
    }

    @Override
    public void loadAnnotationCounts_async(
            AMD_IMetadata_loadAnnotationCounts __cb, String rootType,
            List<Long> ids, List<Long> userIds, Parameters options,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current, rootType, ids, userIds, options);
    }

	protected void map(List<String> annotationTypes) throws ServerError
	{
		if (annotationTypes == null)
		{
			return; // No result is fine
		}

		for (int i = 0; i < annotationTypes.size(); i++)
		{
			String in = annotationTypes.get(i);
			Class<?> out = IceMapper.omeroClass(in, true);
			annotationTypes.set(i, out.getName());
		}
	}

	protected String map(String annotationType) throws ServerError
	{
		Class<?> out = IceMapper.omeroClass(annotationType, true);
		return out.getName();
	}
}
