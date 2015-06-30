/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import omero.api.IContainerPrx;
import omero.api.IUpdatePrx;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.util.Requests;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.sys.Parameters;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.util.PojoMapper;

/**
 * A {@link Facility} for saving, deleting and updating data objects
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class DataManagerFacility extends Facility {

    /** Reference to the {@link BrowseFacility} */
    private BrowseFacility browse;

    /**
     * Creates a new instance
     * 
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    DataManagerFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Deletes the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to delete.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Response deleteObject(SecurityContext ctx, IObject object)
            throws DSOutOfServiceException, DSAccessException {
        return deleteObjects(ctx, Collections.singletonList(object));
    }

    /**
     * Deletes the specified objects.
     *
     * @param ctx
     *            The security context.
     * @param objects
     *            The objects to delete.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Response deleteObjects(SecurityContext ctx, List<IObject> objects)
            throws DSOutOfServiceException, DSAccessException {
        try {
            /*
             * convert the list of objects to lists of IDs by OMERO model class
             * name
             */
            final Map<String, List<Long>> objectIds = new HashMap<String, List<Long>>();
            for (final IObject object : objects) {
                /* determine actual model class name for this object */
                Class<? extends IObject> objectClass = object.getClass();
                while (true) {
                    final Class<?> superclass = objectClass.getSuperclass();
                    if (IObject.class == superclass) {
                        break;
                    } else {
                        objectClass = superclass.asSubclass(IObject.class);
                    }
                }
                final String objectClassName = objectClass.getSimpleName();
                /* then add the object's ID to the list for that class name */
                final Long objectId = object.getId().getValue();
                List<Long> idsThisClass = objectIds.get(objectClassName);
                if (idsThisClass == null) {
                    idsThisClass = new ArrayList<Long>();
                    objectIds.put(objectClassName, idsThisClass);
                }
                idsThisClass.add(objectId);
            }
            /* now delete the objects */
            final Request request = Requests.delete(objectIds);
            return gateway.submit(ctx, request).loop(50, 250);
        } catch (Throwable t) {
            handleException(this, t, "Cannot delete the object.");
        }
        return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @param options
     *            Options to update the data.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx, IObject object,
            Map options) throws DSOutOfServiceException, DSAccessException {
        try {
            IUpdatePrx service = gateway.getUpdateService(ctx);
            if (options == null)
                return service.saveAndReturnObject(object);
            return service.saveAndReturnObject(object, options);
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public DataObject saveAndReturnObject(SecurityContext ctx, DataObject object)
            throws DSOutOfServiceException, DSAccessException {
        return PojoMapper.asDataObject(saveAndReturnObject(ctx, object.asIObject()));
    }
    
    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx, IObject object)
            throws DSOutOfServiceException, DSAccessException {
        try {
            IUpdatePrx service = gateway.getUpdateService(ctx);
            IObject result = service.saveAndReturnObject(object);
            return result;
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @param options
     *            Options to update the data.
     * @param userName
     *            The name of the user to create the data for.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx, IObject object,
            Map options, String userName) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IUpdatePrx service = gateway.getUpdateService(ctx, userName);

            if (options == null)
                return service.saveAndReturnObject(object);
            return service.saveAndReturnObject(object, options);
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @param userName
     *            The name of the user to create the data for.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public DataObject saveAndReturnObject(SecurityContext ctx,
            DataObject object, String userName) throws DSOutOfServiceException,
            DSAccessException {
        return PojoMapper.asDataObject(saveAndReturnObject(ctx, object.asIObject(), userName));
    }
    
    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @param userName
     *            The name of the user to create the data for.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx,
            IObject object, String userName) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IUpdatePrx service = gateway.getUpdateService(ctx, userName);
            IObject result = service.saveAndReturnObject(object);
            return result;
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param objects
     *            The objects to update.
     * @param options
     *            Options to update the data.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public List<IObject> saveAndReturnObject(SecurityContext ctx,
            List<IObject> objects, Map options, String userName)
            throws DSOutOfServiceException, DSAccessException {
        try {
            IUpdatePrx service = gateway.getUpdateService(ctx, userName);
            return service.saveAndReturnArray(objects);
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return new ArrayList<IObject>();
    }

    /**
     * Updates the specified object.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            The object to update.
     * @param options
     *            Options to update the data.
     * @return The updated object.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject updateObject(SecurityContext ctx, IObject object,
            Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);
            IObject r = service.updateDataObject(object, options);
            return browse.findIObject(ctx, r);
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return null;
    }

    /**
     * Updates the specified <code>IObject</code>s and returned the updated
     * <code>IObject</code>s.
     *
     * @param ctx
     *            The security context.
     * @param objects
     *            The array of objects to update.
     * @param options
     *            Options to update the data.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#updateDataObjects(IObject[], Map)
     */
    public List<IObject> updateObjects(SecurityContext ctx,
            List<IObject> objects, Parameters options)
            throws DSOutOfServiceException, DSAccessException {
        try {
            IContainerPrx service = gateway.getPojosService(ctx);
            List<IObject> l = service.updateDataObjects(objects, options);
            if (l == null)
                return l;
            Iterator<IObject> i = l.iterator();
            List<IObject> r = new ArrayList<IObject>(l.size());
            IObject io;
            while (i.hasNext()) {
                io = browse.findIObject(ctx, i.next());
                if (io != null)
                    r.add(io);
            }
            return r;
        } catch (Throwable t) {
            handleException(this, t, "Cannot update the object.");
        }
        return new ArrayList<IObject>();
    }

    /**
     * Adds the {@link ImageData} to the given {@link DatasetData}
     * 
     * @param ctx
     *            The security context.
     * @param image
     *            The image to add to the dataset
     * @param ds
     *            The dataset to add the image to
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public void addImageToDataset(SecurityContext ctx, ImageData image,
            DatasetData ds) throws DSOutOfServiceException, DSAccessException {
        List<ImageData> l = new ArrayList<ImageData>(1);
        l.add(image);
        addImagesToDataset(ctx, l, ds);
    }

    /**
     * Adds the {@link ImageData}s to the given {@link DatasetData}
     * 
     * @param ctx
     *            The security context.
     * @param images
     *            The images to add to the dataset
     * @param ds
     *            The dataset to add the images to
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public void addImagesToDataset(SecurityContext ctx,
            Collection<ImageData> images, DatasetData ds)
            throws DSOutOfServiceException, DSAccessException {
        List<IObject> links = new ArrayList<IObject>();
        for (ImageData img : images) {
            DatasetImageLink l = new DatasetImageLinkI();
            l.setParent(ds.asDataset());
            l.setChild(img.asImage());
            links.add(l);
        }
        updateObjects(ctx, links, null);
    }

}
