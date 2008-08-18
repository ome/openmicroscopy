/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import java.util.List;
import java.util.Map;

import ome.api.IPojos;
import ome.services.blitz.util.BlitzExecutor;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IPojos_createDataObject;
import omero.api.AMD_IPojos_createDataObjects;
import omero.api.AMD_IPojos_deleteDataObject;
import omero.api.AMD_IPojos_deleteDataObjects;
import omero.api.AMD_IPojos_findAnnotations;
import omero.api.AMD_IPojos_findCGCPaths;
import omero.api.AMD_IPojos_findContainerHierarchies;
import omero.api.AMD_IPojos_getCollectionCount;
import omero.api.AMD_IPojos_getImages;
import omero.api.AMD_IPojos_getImagesByOptions;
import omero.api.AMD_IPojos_getUserDetails;
import omero.api.AMD_IPojos_getUserImages;
import omero.api.AMD_IPojos_link;
import omero.api.AMD_IPojos_loadContainerHierarchy;
import omero.api.AMD_IPojos_retrieveCollection;
import omero.api.AMD_IPojos_unlink;
import omero.api.AMD_IPojos_updateDataObject;
import omero.api.AMD_IPojos_updateDataObjects;
import omero.api._IPojosOperations;
import omero.model.IObject;
import omero.util.IceMapper;
import Ice.Current;

/**
 * Implementation of the IPojos service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IPojos
 */
public class PojosI extends AbstractAmdServant implements _IPojosOperations {

    public PojosI(IPojos service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void createDataObject_async(AMD_IPojos_createDataObject __cb,
            IObject obj, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, options);

    }

    public void createDataObjects_async(AMD_IPojos_createDataObjects __cb,
            List<IObject> dataObjects, Map<String, RType> options,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dataObjects, options);

    }

    public void deleteDataObject_async(AMD_IPojos_deleteDataObject __cb,
            IObject obj, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, options);

    }

    public void deleteDataObjects_async(AMD_IPojos_deleteDataObjects __cb,
            List<IObject> objs, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, objs, options);

    }

    public void findAnnotations_async(AMD_IPojos_findAnnotations __cb,
            String rootType, List<Long> rootIds, List<Long> annotatorIds,
            Map<String, RType> options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, annotatorIds,
                options);

    }

    public void findCGCPaths_async(AMD_IPojos_findCGCPaths __cb,
            List<Long> imageIds, String algo, Map<String, RType> options,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, imageIds, algo, options);

    }

    public void findContainerHierarchies_async(
            AMD_IPojos_findContainerHierarchies __cb, String rootType,
            List<Long> imageIds, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, imageIds, options);

    }

    public void getCollectionCount_async(AMD_IPojos_getCollectionCount __cb,
            String type, String property, List<Long> ids,
            Map<String, RType> options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, type, property, ids, options);

    }

    public void getImagesByOptions_async(AMD_IPojos_getImagesByOptions __cb,
            Map<String, RType> options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, options);

    }

    public void getImages_async(AMD_IPojos_getImages __cb, String rootType,
            List<Long> rootIds, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, options);

    }

    public void getUserDetails_async(AMD_IPojos_getUserDetails __cb,
            List<String> names, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, names, options);

    }

    public void getUserImages_async(AMD_IPojos_getUserImages __cb,
            Map<String, RType> options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, options);

    }

    public void link_async(AMD_IPojos_link __cb, List<IObject> links,
            Map<String, RType> options, Current __current) throws ServerError {

        IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_ARRAY);
        ome.model.ILink[] array;
        if (links != null) {
            array = new ome.model.ILink[0];
        } else {
            array = new ome.model.ILink[links.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (ome.model.ILink) mapper.reverse(links.get(i));
            }
        }
        Object map = mapper.reverse(options);
        callInvokerOnMappedArgs(mapper, __cb, __current, array, map);
    }

    public void loadContainerHierarchy_async(
            AMD_IPojos_loadContainerHierarchy __cb, String rootType,
            List<Long> rootIds, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, options);

    }

    public void retrieveCollection_async(AMD_IPojos_retrieveCollection __cb,
            IObject obj, String collectionName, Map<String, RType> options,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, collectionName, options);

    }

    public void unlink_async(AMD_IPojos_unlink __cb, List<IObject> links,
            Map<String, RType> options, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.VOID);
        ome.model.ILink[] array;
        if (links != null) {
            array = new ome.model.ILink[0];
        } else {
            array = new ome.model.ILink[links.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (ome.model.ILink) mapper.reverse(links.get(i));
            }
        }
        Object map = mapper.reverse(options);
        callInvokerOnMappedArgs(mapper, __cb, __current, array, map);
    }

    public void updateDataObject_async(AMD_IPojos_updateDataObject __cb,
            IObject obj, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, options);

    }

    public void updateDataObjects_async(AMD_IPojos_updateDataObjects __cb,
            List<IObject> objs, Map<String, RType> options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, objs, options);

    }

}
