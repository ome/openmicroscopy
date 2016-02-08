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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.CollectionUtils;

import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.IRoiPrx;
import omero.api.IUpdatePrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ROIResult;
import omero.gateway.util.ModelMapper;
import omero.gateway.util.PyTablesUtils;
import omero.model.FolderRoiLink;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Line;
import omero.model.Polyline;
import omero.model.Roi;
import omero.model.Shape;
import omero.sys.ParametersI;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROICoordinate;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.gateway.util.PojoMapper;


/**
 * A {@link Facility} for ROI.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ROIFacility extends Facility {

    /** Reference to the DataManagerFacility */
    private DataManagerFacility dm;
    
    /** Reference to the BrowseFacility */
    private BrowseFacility browse;
    
    /**
     * Creates a new instance
     * @param gateway Reference to the {@link Gateway}
     * @throws ExecutionException
     */
    ROIFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.dm = gateway.getFacility(DataManagerFacility.class);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Loads the ROI related to the specified image.
     *
     * @param ctx
     *            The security context.
     * @param roiId
     *            The ROI's id.
     * @return See above.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public ROIResult loadROI(SecurityContext ctx, long roiId)
            throws DSOutOfServiceException, DSAccessException {
        try {
            IRoiPrx svc = gateway.getROIService(ctx);
            RoiOptions options = new RoiOptions();
            RoiResult rr = svc.findByRoi(roiId, options);
            ROIResult result = new ROIResult(
                    PojoMapper.<ROIData> asCastedDataObjects(rr.rois));
            return result;
        } catch (ServerError e) {
            handleException(this, e, "Couldn't get ROIs by plane.");
        }
        return null;
    }
    
    /**
     * Loads the ROI related to the specified image.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param z
     *          The selection z-section.
     * @param t
     *          The selection timepoint.
     * @return See above.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<ROIResult> loadROIsByPlane(SecurityContext ctx, long imageID, int z, int t)
            throws DSOutOfServiceException, DSAccessException {
        List<ROIResult> results = new ArrayList<ROIResult>();
        try {
            IRoiPrx svc = gateway.getROIService(ctx);
            RoiOptions options = new RoiOptions();
            RoiResult r = svc.findByPlane(imageID, z, t, options);
            ROIResult result = new ROIResult(PojoMapper.<ROIData>asCastedDataObjects(r.rois));
            results.add(result);
        } catch (ServerError e) {
            handleException(this, e, "Couldn't get ROIs by plane.");
        }
        return results;
    }

    
    /**
     * Loads the ROI related to the specified image.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @return See above.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<ROIResult> loadROIs(SecurityContext ctx, long imageID)
            throws DSOutOfServiceException, DSAccessException {
        return loadROIs(ctx, imageID, null, gateway.getLoggedInUser().getId());
    }

    /**
     * Loads the ROI related to the specified image.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param measurements The measurements IDs linked to the image if any.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public List<ROIResult> loadROIs(SecurityContext ctx, long imageID,
            List<Long> measurements) throws DSOutOfServiceException,
            DSAccessException {
        return loadROIs(ctx, imageID, measurements, -1);
    }

    /**
     * Loads the ROI related to the specified image.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param measurements The measurements IDs linked to the image if any.
     * @param userID
     *            The user's ID.
     * @return See above.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<ROIResult> loadROIs(SecurityContext ctx, long imageID,
            List<Long> measurements, long userID)
            throws DSOutOfServiceException, DSAccessException {
        List<ROIResult> results = new ArrayList<ROIResult>();

        try {
            IRoiPrx svc = gateway.getROIService(ctx);
            RoiOptions options = new RoiOptions();
            if (userID >= 0)
                options.userId = omero.rtypes.rlong(userID);
            RoiResult r;
            ROIResult result;
            if (CollectionUtils.isEmpty(measurements)) {
                options = new RoiOptions();
                r = svc.findByImage(imageID, options);
                if (r == null)
                    return results;
                results.add(new ROIResult(PojoMapper.<ROIData>asCastedDataObjects(r.rois)));
            } else { // measurements
                Map<Long, RoiResult> map = svc.getMeasuredRoisMap(imageID,
                        measurements, options);
                if (map == null)
                    return results;
                for (final Entry<Long, RoiResult> entry : map.entrySet()) {
                    final Long id = entry.getKey();
                    r = entry.getValue();
                    // get the table
                    result = new ROIResult(PojoMapper.<ROIData>asCastedDataObjects(r.rois), id);
                    result.setResult(PyTablesUtils.createTableResult(
                            svc.getTable(id), "Image", imageID));
                    results.add(result);
                }
            }
            
            // load the ROI folders
            Collection<FolderData> folders = browse.getFolders(ctx);
            Map<Long, FolderData> folderById = new HashMap<Long, FolderData>();
            for(FolderData f : folders) {
                folderById.put(f.getId(), f);
            }
            
            for(ROIResult rr : results) {
                for(ROIData roi : rr.getROIs()) {
                    for(FolderData folder : roi.getFolders()) {
                        initFolders(folder, folderById);
                    }
                }
            }
        } catch (Exception e) {
            handleException(this, e, "Cannot load the ROI for image: "
                    + imageID);
        }
        return results;
    }

    /**
     * Helper method for properly initializing a FolderData; i. e. making sure
     * parent folders are loaded
     * 
     * @param f
     *            The Folder
     * @param folders
     *            All available, loaded Folders
     */
    private void initFolders(FolderData f, Map<Long, FolderData> folders) {
        FolderData f2 = folders.get(f.getId());
        if (f2 != null) {
            f.setFolder(f2.asFolder());
        }

        if (f.getParentFolder() != null) {
            initFolders(f.getParentFolder(), folders);
        }
    }
    
    /**
     * Save the ROI for the image to the server.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param roiList
     *            The list of ROI to save.
     * @return updated list of ROIData objects.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<ROIData> saveROIs(SecurityContext ctx, long imageID,
            Collection<ROIData> roiList) throws DSOutOfServiceException,
            DSAccessException {
        return saveROIs(ctx, imageID, -1,
                roiList);
    }

    /**
     * Save the ROI for the image to the server.
     *
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param userID
     *            The user's ID.
     * @param roiList
     *            The list of ROI to save.
     * @return updated list of ROIData objects.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<ROIData> saveROIs(SecurityContext ctx, long imageID,
            long userID, Collection<ROIData> roiList) throws DSOutOfServiceException,
            DSAccessException {

        try {
            IUpdatePrx updateService = gateway.getUpdateService(ctx);
            IRoiPrx svc = gateway.getROIService(ctx);
            RoiOptions options = new RoiOptions();
            if (userID >= 0)
                options.userId = omero.rtypes.rlong(userID);
            RoiResult serverReturn;
            serverReturn = svc.findByImage(imageID, new RoiOptions());
            Map<Long, Roi> roiMap = new HashMap<Long, Roi>();
            List<Roi> serverRoiList = serverReturn.rois;

            /* Create a map of all the client roi with id as key */
            Map<Long, ROIData> clientROIMap = new HashMap<Long, ROIData>();
            for (ROIData roi : roiList) {
                if (roi != null)
                    clientROIMap.put(roi.getId(), roi);
            }


            /* Create a map of the <id, serverROI>, but remove any roi from
             * the server that should be deleted, before creating map.
             * To delete an roi we first must delete all the roiShapes in
             * the roi. */
            for (Roi r : serverRoiList) {
                if (r != null) {
                    //rois are now deleted using the roi service.
                    if (clientROIMap.containsKey(r.getId().getValue()))
                        roiMap.put(r.getId().getValue(), r);
                }
            }

            /* For each roi in the client, see what should be done:
             * 1. Create a new roi if it does not exist.
             * 2. build a map of the roiShapes in the clientROI with
             * ROICoordinate as a key.
             * 3. as above but for server roiShapes.
             * 4. iterate through the maps to see if the shapes have been
             * deleted in the roi on the client, if so then delete the shape on
             * the server.
             * 5. Somehow the server roi becomes stale on the client so we have
             * to retrieve the roi again from the server before updating it.
             * 6. Check to see if the roi in the cleint has been updated
             */
            List<ShapeData> shapeList;
            ShapeData shape;
            Map<ROICoordinate, ShapeData> clientCoordMap;
            Roi serverRoi;
            Iterator<List<ShapeData>> shapeIterator;
            Map<ROICoordinate, Shape>serverCoordMap;
            Shape s;
            ROICoordinate coord;
            int shapeIndex;

            Collection<ROIData> updated = new ArrayList<ROIData>();
            List<Long> deleted = new ArrayList<Long>();
            Image unloaded = new ImageI(imageID, false);
            Roi rr;
            int z, t;
            for (ROIData roi : roiList)
            {
                /*
                 * Step 1. Add new ROI to the server.
                 */
                if (!roiMap.containsKey(roi.getId()))
                {
                    rr = (Roi) roi.asIObject();
                    rr.setImage(unloaded);
                    rr = (Roi) updateService.saveAndReturnObject(rr);
                    updated.add(new ROIData(rr));
                    continue;
                }

                /*
                 * Step 2. create the client roiShape map.
                 */
                serverRoi = roiMap.get(roi.getId());
                shapeIterator  = roi.getIterator();

                clientCoordMap = new HashMap<ROICoordinate, ShapeData>();
                while (shapeIterator.hasNext()) {
                    shapeList = shapeIterator.next();
                    shape = shapeList.get(0);
                    if (shape != null)
                        clientCoordMap.put(shape.getROICoordinate(), shape);
                }

                /*
                 * Step 3. create the server roiShape map.
                 */
                serverCoordMap  = new HashMap<ROICoordinate, Shape>();
                if (serverRoi != null) {
                    for (int i = 0 ; i < serverRoi.sizeOfShapes(); i++) {
                        s = serverRoi.getShape(i);
                        if (s != null) {
                            z = 0;
                            t = 0;
                            if (s.getTheZ() != null) z = s.getTheZ().getValue();
                            if (s.getTheT() != null) t = s.getTheT().getValue();
                            serverCoordMap.put(new ROICoordinate(z, t), s);
                        }
                    }
                }
                /*
                 * Step 4. delete any shapes in the server that have been deleted
                 * in the client.
                 */
                Iterator si = serverCoordMap.entrySet().iterator();
                Entry entry;
                List<ROICoordinate> removed = new ArrayList<ROICoordinate>();
                while (si.hasNext()) {
                    entry = (Entry) si.next();
                    coord = (ROICoordinate) entry.getKey();
                    if (!clientCoordMap.containsKey(coord)) {
                        s = (Shape) entry.getValue();
                        if (s != null) {
                            serverRoi.removeShape(s);
                            serverRoi = (Roi) updateService.saveAndReturnObject(serverRoi);
                            updated.add(new ROIData(serverRoi));
                        }
                    } else {
                        s = (Shape) entry.getValue();
                        if (s instanceof Line || s instanceof Polyline) {
                            shape = clientCoordMap.get(coord);
                            if ((s instanceof Line &&
                                    shape.asIObject() instanceof Polyline) ||
                                (s instanceof Polyline &&
                                    shape.asIObject() instanceof Line)) {
                                removed.add(coord);
                                serverRoi.removeShape(s);
                                serverRoi = (Roi) updateService.saveAndReturnObject(serverRoi);
                                updated.add(new ROIData(serverRoi));
                                deleted.add(s.getId().getValue());
                            }
                        }
                    }
                }

                /*
                 * Step 6. Check to see if the roi in the client has been updated
                 * if so replace the server roiShape with the client one.
                 */
                si = clientCoordMap.entrySet().iterator();
                Shape serverShape;
                long sid;
                Shape sh;
                while (si.hasNext()) {
                    entry = (Entry) si.next();
                    coord = (ROICoordinate) entry.getKey();
                    shape = (ShapeData) entry.getValue();
                    sh = (Shape) shape.asIObject();
                    ModelMapper.unloadCollections(sh);
                    if (shape != null) {
                        if (!serverCoordMap.containsKey(coord))
                            serverRoi.addShape(sh);
                        else if (shape.isDirty()) {
                            shapeIndex = -1;
                            if (deleted.contains(shape.getId())) {
                                serverRoi.addShape(sh);
                                break;
                            }
                            for (int j = 0 ; j < serverRoi.sizeOfShapes() ; j++)
                            {
                                if (serverRoi != null) {
                                    serverShape = serverRoi.getShape(j);
                                    if (serverShape != null &&
                                            serverShape.getId() != null) {
                                        sid = serverShape.getId().getValue();
                                        if (sid == shape.getId()) {
                                            shapeIndex = j;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (shapeIndex == -1) {
                                serverShape = null;
                                shapeIndex = -1;
                                for (int j = 0 ; j < serverRoi.sizeOfShapes() ;
                                j++)
                                {
                                    if (serverRoi != null)
                                    {
                                        z = 0;
                                        t = 0;
                                        serverShape = serverRoi.getShape(j);
                                        if (serverShape != null) {
                                            if (serverShape.getTheT() != null)
                                                t =
                                                serverShape.getTheT().getValue();
                                            if (serverShape.getTheZ() != null)
                                                z =
                                                serverShape.getTheZ().getValue();
                                            if (t == shape.getT() &&
                                                z == shape.getZ())
                                            {
                                                shapeIndex = j;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (shapeIndex !=-1) {
                                    if (!removed.contains(coord))
                                        dm.deleteObject(ctx, serverShape);
                                    serverRoi.addShape(sh);
                                } else {
                                    throw new Exception("serverRoi.shapeList " +
                                        "is corrupted");
                                }
                            }
                            else {
                                serverRoi.setShape(shapeIndex, sh);
                            }
                        }
                    }
                }

                /*
                 * Step 7. update properties of ROI, if they are changed.
                 *
                 */
                if (serverRoi != null) {
                    Roi ri = (Roi) roi.asIObject();
                    serverRoi.setDescription(ri.getDescription());
                    serverRoi.setImage(unloaded);
                    ri = (Roi) updateService.saveAndReturnObject(serverRoi);
                    updated.add(new ROIData(ri));
                }

            }
            return updated;
        } catch (Exception e) {
            handleException(this, e, "Cannot Save the ROI for image: "
                    + imageID);
        }
        return new ArrayList<ROIData>();
    }
    
    /**
     * Get all ROI folders for a certain image
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The image id
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public Collection<FolderData> getROIFolders(SecurityContext ctx,
            long imageId) throws DSOutOfServiceException, DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            StringBuilder sb = new StringBuilder();
            ParametersI param = new ParametersI();
            param.addLong("imageId", imageId);

            sb.append("select roilink from FolderRoiLink as roilink ");
            sb.append("left outer join fetch roilink.parent as folder ");
            sb.append("left outer join fetch roilink.child as roi ");
            sb.append("left outer join fetch roi.image as image ");
            sb.append("where image.id = :imageId ");

            List<IObject> links = qs.findAllByQuery(sb.toString(), param);
            Collection<FolderData> result = new ArrayList<FolderData>();

            for (IObject l : links) {
                FolderRoiLink link = (FolderRoiLink) l;
                result.add(new FolderData(link.getParent()));
            }

            // filter out duplicate FolderData
            // TODO: Check if this can be done in the query itself (select
            // distinct)
            Set<Long> ids = new HashSet<Long>();
            Iterator<FolderData> it = result.iterator();
            while (it.hasNext()) {
                FolderData next = it.next();
                if (ids.contains(next.getId())) {
                    it.remove();
                    continue;
                }
                ids.add(next.getId());
            }

            return result;
        } catch (Throwable e) {
            handleException(this, e, "Cannot load ROI folders.");
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * Get all ROIs which are part of a certain folder
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The image id
     * @param folderId
     *            The folder id
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public Collection<ROIResult> loadROIsForFolder(SecurityContext ctx,
            long imageId, long folderId) throws DSOutOfServiceException,
            DSAccessException {
        try {
            // TODO: This should actually happen on the server; replace
            //      with server-side method when available
            
            // get all ROIResults
            List<ROIResult> roiresults = loadROIs(ctx, imageId);

            // get the ROIs of the specified folder
            IQueryPrx qs = gateway.getQueryService(ctx);
            StringBuilder sb = new StringBuilder();
            ParametersI param = new ParametersI();
            param.addLong("folderId", folderId);

            sb.append("select roilink from FolderRoiLink as roilink ");
            sb.append("left outer join fetch roilink.parent as folder ");
            sb.append("left outer join fetch roilink.child as roi ");
            sb.append("where folder.id = :folderId ");

            List<IObject> links = qs.findAllByQuery(sb.toString(), param);

            Set<Long> roiIds = new HashSet<Long>();
            for (IObject l : links) {
                FolderRoiLink link = (FolderRoiLink) l;
                roiIds.add(link.getChild().getId().getValue());
            }

            // filter the ROIResults
            Iterator<ROIResult> it = roiresults.iterator();
            while (it.hasNext()) {
                ROIResult r = it.next();
                Iterator<ROIData> it2 = r.getROIs().iterator();
                while (it2.hasNext()) {
                    ROIData roi = it2.next();
                    if (!roiIds.contains(roi.getId()))
                        it2.remove();
                }

                if (r.getROIs().isEmpty())
                    it.remove();
            }

            return roiresults;

        } catch (Throwable e) {
            handleException(this, e, "Cannot load ROIs for folder " + folderId);
        }

        return Collections.EMPTY_LIST;
    }
}
