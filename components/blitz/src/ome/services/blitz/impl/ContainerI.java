/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IContainer;
import ome.services.blitz.util.BlitzExecutor;
import omero.ApiUsageException;
import omero.RClass;
import omero.ServerError;
import omero.rtypes;
import omero.api.AMD_IContainer_createDataObject;
import omero.api.AMD_IContainer_createDataObjects;
//import omero.api.AMD_IContainer_findAnnotations;
import omero.api.AMD_IContainer_findContainerHierarchies;
import omero.api.AMD_IContainer_getCollectionCount;
import omero.api.AMD_IContainer_getImages;
import omero.api.AMD_IContainer_getImagesByOptions;
import omero.api.AMD_IContainer_getImagesBySplitFilesets;
import omero.api.AMD_IContainer_getUserImages;
import omero.api.AMD_IContainer_link;
import omero.api.AMD_IContainer_loadContainerHierarchy;
import omero.api.AMD_IContainer_retrieveCollection;
import omero.api.AMD_IContainer_unlink;
import omero.api.AMD_IContainer_updateDataObject;
import omero.api.AMD_IContainer_updateDataObjects;
import omero.api._IContainerOperations;
import omero.model.IObject;
import omero.sys.Parameters;
import omero.util.IceMapper;
import Ice.Current;
import Ice.UserException;

/**
 * Implementation of the IContainer service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IContainer
 */
public class ContainerI extends AbstractAmdServant implements _IContainerOperations {

    public ContainerI(IContainer service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void createDataObject_async(AMD_IContainer_createDataObject __cb,
            IObject obj, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, options);

    }

    public void createDataObjects_async(AMD_IContainer_createDataObjects __cb,
            List<IObject> dataObjects, Parameters options,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dataObjects, options);

    }

    /*
    public void findAnnotations_async(AMD_IContainer_findAnnotations __cb,
            String rootType, List<Long> rootIds, List<Long> annotatorIds,
            Parameters options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, annotatorIds,
                options);

    }
*/
    public void findContainerHierarchies_async(
            AMD_IContainer_findContainerHierarchies __cb, String rootType,
            List<Long> imageIds, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, imageIds, options);

    }

    public void getCollectionCount_async(AMD_IContainer_getCollectionCount __cb,
            String type, String property, List<Long> ids,
            Parameters options, Current __current) throws ServerError {

        // This is a bit weird. The CountMap type in omero/Collections.ice
        // specifies <Long, Long> which makes sense, but ContainerImpl is returning,
        // Long, Integer. So we're working around that here with the hope that
        // it'll eventually get fixed. :)
        
        IceMapper mapper = new IceMapper(new IceMapper.ReturnMapping(){

            public Object mapReturnValue(IceMapper mapper, Object value)
                    throws UserException {
                Map<Long, Integer> map = (Map<Long, Integer>) value;
                Map<Long, Long> rv = new HashMap<Long, Long>();
                for (Long k : map.keySet()) {
                    Integer v = map.get(k);
                    rv.put(k, Long.valueOf(v.longValue()));
                }
                return rv;
            }});

        Class<?> omeroType = IceMapper.omeroClass(type, false);
        String omeroStr = omeroType == null ? null : omeroType.getName();
        Set<Long> _ids = new HashSet<Long>(ids);
        callInvokerOnMappedArgs(mapper, __cb, __current, omeroStr, property, _ids, null);

    }

    public void getImagesByOptions_async(AMD_IContainer_getImagesByOptions __cb,
            Parameters options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, options);

    }

    public void getImages_async(AMD_IContainer_getImages __cb, String rootType,
            List<Long> rootIds, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, options);

    }

    public void getImagesBySplitFilesets_async(
            AMD_IContainer_getImagesBySplitFilesets __cb,
            Map<java.lang.String, List<Long>> included, Parameters options,
            Current __current) throws ServerError {
        final Map<RClass, List<Long>> includedWithClasses =
                new HashMap<RClass, List<Long>>(included.size());
        for (final Map.Entry<String, List<Long>> entry : included.entrySet()) {
            includedWithClasses.put(rtypes.rclass(entry.getKey()), entry.getValue());
        }
        callInvokerOnRawArgs(__cb, __current, includedWithClasses, options);
    }

    public void getUserImages_async(AMD_IContainer_getUserImages __cb,
            Parameters options, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, options);

    }

    public void link_async(AMD_IContainer_link __cb, List<IObject> links,
            Parameters options, Current __current) throws ServerError {

        IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_ARRAY);
        ome.model.ILink[] array;
        if (links == null) {
            array = new ome.model.ILink[0];
        } else {
            array = new ome.model.ILink[links.size()];
            for (int i = 0; i < array.length; i++) {
                try {
                    mapToLinkArrayOrThrow(links, mapper, array, i);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                    return; // EARLY EXIT !
                }
            }
        }
        Object map = mapper.reverse(options);
        callInvokerOnMappedArgs(mapper, __cb, __current, array, map);
    }

    public void loadContainerHierarchy_async(
            AMD_IContainer_loadContainerHierarchy __cb, String rootType,
            List<Long> rootIds, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rootType, rootIds, options);

    }

    public void retrieveCollection_async(AMD_IContainer_retrieveCollection __cb,
            IObject obj, String collectionName, Parameters options,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, collectionName, options);

    }

    public void unlink_async(AMD_IContainer_unlink __cb, List<IObject> links,
            Parameters options, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.VOID);
        ome.model.ILink[] array;
        if (links == null) {
            array = new ome.model.ILink[0];
        } else {
            array = new ome.model.ILink[links.size()];
            for (int i = 0; i < array.length; i++) {
                try {
                    mapToLinkArrayOrThrow(links, mapper, array, i);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                    return; // EARLY EXIT!
                }
            }
        }
        Object map = mapper.reverse(options);
        callInvokerOnMappedArgs(mapper, __cb, __current, array, map);
    }

    public void updateDataObject_async(AMD_IContainer_updateDataObject __cb,
            IObject obj, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, options);

    }

    public void updateDataObjects_async(AMD_IContainer_updateDataObjects __cb,
            List<IObject> objs, Parameters options, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, objs, options);

    }

    // Helpers
    // =========================================================================

    private void mapToLinkArrayOrThrow(
            List<IObject> links, IceMapper mapper, ome.model.ILink[] array,
            int i) throws ApiUsageException {
        try {
            array[i] = (ome.model.ILink) mapper.reverse(links.get(i));
        } catch (ClassCastException cce) {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            IceMapper.fillServerError(aue, cce);
            aue.message = "ClassCastException: " + cce.getMessage();
            throw aue;
        }
    }
}
