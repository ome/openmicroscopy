/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;

import omero.api.IMetadataPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.Channel;
import omero.model.IObject;
import omero.model.Pixels;
import omero.sys.ParametersI;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;
import omero.gateway.util.PojoMapper;


/**
 * A {@link Facility} to access the metadata.
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class MetadataFacility extends Facility {

    private BrowseFacility browse;

    /**
     * Creates a new instance.
     *
     * @param gateway Reference to the gateway.
     * @throws ExecutionException
     */
    MetadataFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Loads the {@link ImageAcquisitionData} for a specific image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service. 
     */
    public ImageAcquisitionData getImageAcquisitionData(SecurityContext ctx,
            long imageId) throws DSOutOfServiceException, DSAccessException {
        ParametersI params = new ParametersI();
        params.acquisitionData();
        ImageData img = browse.getImage(ctx, imageId, params);
        return new ImageAcquisitionData(img.asImage());
    }

    /**
     * Get the {@link ChannelData} for a specific image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId to get the ChannelData for
     * @return List of ChannelData
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service. 
     */
    public List<ChannelData> getChannelData(SecurityContext ctx, long imageId)
            throws DSOutOfServiceException, DSAccessException {
        List<ChannelData> result = new ArrayList<ChannelData>();

        try {
            ImageData img = browse.getImage(ctx, imageId);

            long pixelsId = img.getDefaultPixels().getId();
            Pixels pixels = gateway.getPixelsService(ctx)
                    .retrievePixDescription(pixelsId);
            List<Channel> l = pixels.copyChannels();
            for (int i = 0; i < l.size(); i++)
                result.add(new ChannelData(i, l.get(i)));

        } catch (Throwable t) {
            handleException(this, t, "Cannot load channel data.");
        }

        return result;
    }
    
    /**
     * Get all annotations for the given {@link DataObject}
     * @param ctx The {@link SecurityContext}
     * @param object The {@link DataObject} to load the annotations for
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<AnnotationData> getAnnotations(SecurityContext ctx,
            DataObject object) throws DSOutOfServiceException,
            DSAccessException {
        return getAnnotations(ctx, object, null, null);
    }

    /**
     * Get the annotations for the given {@link DataObject}
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param object
     *            The {@link DataObject} to load the annotations for
     * @param annotationTypes
     *            The type of annotations to load (can be <code>null</code>)
     * @param userIds
     *            Only load annotations of certain users (can be
     *            <code>null</code>, i. e. all users)
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<AnnotationData> getAnnotations(SecurityContext ctx,
            DataObject object,
            List<Class<? extends AnnotationData>> annotationTypes,
            List<Long> userIds) throws DSOutOfServiceException,
            DSAccessException {
        Map<DataObject, List<AnnotationData>> result = getAnnotations(ctx,
                Arrays.asList(new DataObject[] { object }), annotationTypes,
                userIds);
        return result.get(object);
    }

    /**
     * Get the annotations for the given {@link DataObject}s
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param objects
     *            The {@link DataObject}s to load the annotations for (have to
     *            be all of the same type)
     * @param annotationTypes
     *            The type of annotations to load (can be <code>null</code>)
     * @param userIds
     *            Only load annotations of certain users (can be
     *            <code>null</code>, i. e. all users)
     * @return Lists of {@link AnnotationData}s mapped to the {@link DataObject}
     *         s they are attached to.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Map<DataObject, List<AnnotationData>> getAnnotations(
            SecurityContext ctx, List<? extends DataObject> objects,
            List<Class<? extends AnnotationData>> annotationTypes,
            List<Long> userIds) throws DSOutOfServiceException,
            DSAccessException {
        Map<DataObject, List<AnnotationData>> result = new HashMap<DataObject, List<AnnotationData>>();

        String type = null;
        List<Long> ids = new ArrayList<Long>();
        for (DataObject obj : objects) {
            if (type == null)
                type = PojoMapper.getModelType(obj.getClass()).getName();
            else if (!type.equals(PojoMapper.getModelType(obj.getClass())
                    .getName()))
                throw new IllegalArgumentException(
                        "All objects have to be the same type");
            ids.add(obj.getId());
        }

        try {
            IMetadataPrx proxy = gateway.getMetadataService(ctx);
            List<String> annoTypes = null;
            if (annotationTypes != null) {
                annoTypes = new ArrayList<String>(annotationTypes.size());
                for (Class c : annotationTypes)
                    annoTypes.add(PojoMapper.getModelType(c).getName());
            }
            Map<Long, List<IObject>> annos = proxy.loadAnnotations(type, ids,
                    annoTypes, userIds, null);
            for (Entry<Long, List<IObject>> e : annos.entrySet()) {
                long id = e.getKey();
                DataObject dobj = null;
                for (DataObject o : objects) {
                    if (o.getId() == id) {
                        dobj = o;
                        break;
                    }
                }
                List<AnnotationData> list = new ArrayList<AnnotationData>();
                for (IObject a : e.getValue()) {
                    list.add((AnnotationData) PojoMapper.asDataObject(a));
                }
                result.put(dobj, list);
            }
        } catch (Throwable t) {
            handleException(this, t, "Cannot get annotations.");
        }

        return result;
    }

}
