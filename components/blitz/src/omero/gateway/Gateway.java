/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway;

import static omero.gateway.util.CmdUtil.REF_GROUP;
import static omero.gateway.util.CmdUtil.createDeleteCommand;
import static omero.gateway.util.GatewayUtils.SUPPORTED_SPECIAL_CHAR;
import static omero.gateway.util.GatewayUtils.WILD_CARDS;
import static omero.gateway.util.GatewayUtils.convertAnnotation;
import static omero.gateway.util.GatewayUtils.convertPojos;
import static omero.gateway.util.GatewayUtils.convertTypeForSearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import java.util.Set;

import ome.formats.OMEROMetadataStoreClient;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import omero.ConcurrencyException;
import omero.InternalException;
import omero.LockTimeout;
import omero.MissingPyramidException;
import omero.RType;
import omero.ServerError;
import omero.ValidationException;
import omero.api.ExporterPrx;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRoiPrx;
import omero.api.IScriptPrx;
import omero.api.IUpdate;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.api.Save;
import omero.api.SearchPrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.Chgrp;
import omero.cmd.Chmod;
import omero.cmd.CmdCallback;
import omero.cmd.CmdCallbackI;
import omero.cmd.Request;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.FSAccessException;
import omero.gateway.exception.ImportException;
import omero.gateway.exception.ProcessException;
import omero.gateway.exception.RenderingServiceException;
import omero.gateway.model.ExportFormat;
import omero.gateway.model.SearchDataContext;
import omero.gateway.model.SecurityContext;
import omero.grid.ProcessCallbackI;
import omero.grid.ScriptProcessPrx;
import omero.grid.SharedResourcesPrx;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Details;
import omero.model.DetailsI;
import omero.model.ExperimenterGroup;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Line;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Polyline;
import omero.model.RenderingDef;
import omero.model.Roi;
import omero.model.Shape;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FilesetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ROICoordinate;
import pojos.ROIData;
import pojos.ShapeData;
import pojos.util.PojoMapper;

@SuppressWarnings("unchecked")
public class Gateway extends ConnectionManager {
    
    /** The default MIME type. */
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";
    
    /* checksum provider factory for verifying file integrity in upload */
    private static final ChecksumProviderFactory checksumProviderFactory = new ChecksumProviderFactoryImpl();
    
    /** Maximum size of pixels read at once. */
    private static final int                                INC = 262144;//256000;
    
    //
    // Regular service lookups
    //

    /**
     * Returns the {@link SharedResourcesPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public SharedResourcesPrx getSharedResources(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getSharedResources();
    }

    /**
     * Returns the {@link IRenderingSettingsPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRenderingSettingsPrx getRenderingSettingsService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getRenderingSettingsService();
    }

    /**
     * Returns the {@link IRepositoryInfoPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRepositoryInfoPrx getRepositoryService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getRepositoryService();
    }

    /**
     * Returns the {@link IScriptPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IScriptPrx getScriptService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getScriptService();
    }

    /**
     * Returns the {@link IContainerPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IContainerPrx getPojosService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getPojosService();
    }

    /**
     * Returns the {@link IQueryPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IQueryPrx getQueryService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getQueryService();
    }

    /**
     * Returns the {@link IUpdatePrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IUpdatePrx getUpdateService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getUpdateService();
    }

    /**
     * Returns the {@link IMetadataPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IMetadataPrx getMetadataService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getMetadataService();
    }

    /**
     * Returns the {@link IRoiPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRoiPrx getROIService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getROIService();
    }

    /**
     * Returns the {@link IConfigPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IConfigPrx getConfigService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getConfigService();
    }

    /**
     * Returns the {@link ThumbnailStorePrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public ThumbnailStorePrx getThumbnailService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getThumbnailService();
    }

    /**
     * Returns the {@link ExporterPrx} service.
     * 
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
    public ExporterPrx getExporterService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getExporterService();
    }

    /**
     * Returns the {@link RawFileStorePrx} service.
     * 
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
    public RawFileStorePrx getRawFileService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getRawFileService();
    }

    /**
     * Returns the {@link RawPixelsStorePrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public RawPixelsStorePrx getPixelsStore(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getPixelsStore();
    }

    /**
     * Returns the {@link IPixelsPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IPixelsPrx getPixelsService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getPixelsService();
    }

    /**
     * Returns the {@link SearchPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public SearchPrx getSearchService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getSearchService();
    }

    /**
     * Returns the {@link IProjectionPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IProjectionPrx getProjectionService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getProjectionService();
    }

    /**
     * Returns the {@link IAdminPrx} service.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IAdminPrx getAdminService(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getAdminService();
    }

    //
    // Irregular service lookups
    //

    /**
     * Creates or recycles the import store.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public OMEROMetadataStoreClient getImportStore(SecurityContext ctx) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        return c.getImportStore();
    }

    /**
     * Returns the {@link RenderingEnginePrx Rendering service}.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public RenderingEnginePrx getRenderingService(SecurityContext ctx, long pixelsID)
            throws DSOutOfServiceException, ServerError {
        Connector c = getConnector(ctx, true, false);
        return c.getRenderingService(pixelsID);
    }
    
    /**
     * Retrieves hierarchy trees rooted by a given node. i.e. the requested node
     * as root and all of its descendants. The annotation for the current user
     * is also linked to the object. Annotations are currently possible only for
     * Image and Dataset. Wraps the call to the
     * {@link IPojos#loadContainerHierarchy(Class, List, Map)} and maps the
     * result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param ctx
     *            The security context, necessary to determine the service.
     * @param rootType
     *            The top-most type which will be searched for Can be
     *            <code>Project</code>. Mustn't be <code>null</code>.
     * @param rootIDs
     *            A set of the IDs of top-most containers. Passed
     *            <code>null</code> to retrieve all container of the type
     *            specified by the rootNodetype parameter.
     * @param options
     *            The Options to retrieve the data.
     * @return A set of hierarchy trees.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#loadContainerHierarchy(Class, List, Map)
     */
    public Set loadContainerHierarchy(SecurityContext ctx, Class rootType,
            List rootIDs, Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IContainerPrx service = c.getPojosService();
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    convertPojos(rootType).getName(), rootIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot load hierarchy for " + rootType + ".");
        }
        return Collections.emptySet();
    }

    /**
     * Retrieves hierarchy trees in various hierarchies that contain the
     * specified Images. The annotation for the current user is also linked to
     * the object. Annotations are currently possible only for Image and
     * Dataset. Wraps the call to the
     * {@link IPojos#findContainerHierarchies(Class, List, Map)} and maps the
     * result calling {@link PojoMapper#asDataObjects(Set)}.
     * 
     * @param ctx
     *            The security context, necessary to determine the service.
     * @param rootNodeType
     *            top-most type which will be searched for Can be
     *            <code>Project</code> Mustn't be <code>null</code>.
     * @param leavesIDs
     *            Set of identifiers of the Images that sit at the bottom of the
     *            trees. Mustn't be <code>null</code>.
     * @param options
     *            Options to retrieve the data.
     * @return A <code>Set</code> with all root nodes that were found.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#findContainerHierarchies(Class, List, Map)
     */
    public Set findContainerHierarchy(SecurityContext ctx, Class rootNodeType,
            List leavesIDs, Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IContainerPrx service = c.getPojosService();
            return PojoMapper.asDataObjects(service.findContainerHierarchies(
                    convertPojos(rootNodeType).getName(), leavesIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot find hierarchy for " + rootNodeType
                    + ".");
        }
        return Collections.emptySet();
    }

    /**
     * Loads all the annotations that have been attached to the specified
     * <code>rootNodes</code>. This method looks for all the <i>valid</i>
     * annotations that have been attached to each of the specified objects. It
     * then maps each <code>rootNodeID</code> onto the set of all annotations
     * that were found for that node. If no annotations were found for that
     * node, then the entry will be <code>null</code>. Otherwise it will be a
     * <code>Set</code> containing <code>Annotation</code> objects. Wraps the
     * call to the
     * {@link IMetadataPrx#loadAnnotations(String, List, List, List)} and maps
     * the result calling {@link PojoMapper#asDataObjects(Parameters)}.
     * 
     * @param ctx
     *            The security context.
     * @param nodeType
     *            The type of the rootNodes. Mustn't be <code>null</code>.
     * @param nodeIDs
     *            TheIds of the objects of type <code>rootNodeType</code>.
     *            Mustn't be <code>null</code>.
     * @param annotationTypes
     *            The collection of annotations to retrieve or passed an empty
     *            list if we retrieve all the annotations.
     * @param annotatorIDs
     *            The identifiers of the users for whom annotations should be
     *            retrieved. If <code>null</code>, all annotations are returned.
     * @param options
     *            Options to retrieve the data.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @see IPojos#findAnnotations(Class, List, List, Map)
     */
    public Map loadAnnotations(SecurityContext ctx, Class nodeType,
            List nodeIDs, List<Class> annotationTypes, List annotatorIDs,
            Parameters options) throws DSOutOfServiceException,
            DSAccessException {
        List<String> types = new ArrayList<String>();
        if (annotationTypes != null && annotationTypes.size() > 0) {
            types = new ArrayList<String>(annotationTypes.size());
            Iterator<Class> i = annotationTypes.iterator();
            String k;
            while (i.hasNext()) {
                k = convertAnnotation(i.next());
                if (k != null)
                    types.add(k);
            }
        }
        Connector c = getConnector(ctx, true, false);
        try {
            IMetadataPrx service = c.getMetadataService();
            return PojoMapper.asDataObjects(service.loadAnnotations(
                    convertPojos(nodeType).getName(), nodeIDs, types,
                    annotatorIDs, options));
        } catch (Throwable t) {
            handleException(t, "Cannot find annotations for " + nodeType + ".");
        }
        return Collections.emptyMap();
    }

    /**
     * Loads the specified annotations.
     * 
     * @param ctx
     *            The security context.
     * @param annotationIds
     *            The annotation to load.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.s
     */
    public Set<DataObject> loadAnnotation(SecurityContext ctx,
            List<Long> annotationIds) throws DSOutOfServiceException,
            DSAccessException {
        if (annotationIds == null || annotationIds.size() == 0)
            return new HashSet<DataObject>();

        Connector c = getConnector(ctx, true, false);
        try {
            IMetadataPrx service = c.getMetadataService();
            return PojoMapper.asDataObjects(service
                    .loadAnnotation(annotationIds));
        } catch (Throwable t) {
            handleException(t, "Cannot find the annotations.");
        }
        return Collections.emptySet();
    }
    
    /**
     * Closes the services initialized by the importer.
     *
     * @param ctx The security context.
     * @param userName The user's name.
     * @throws Throwable 
     */
    public void closeImport(SecurityContext ctx, String userName) throws Throwable
    {
        Connector c = getConnector(ctx, false, true);
        c = c.getConnector(userName);
        if (c != null)
            c.closeImport();
    }
    
    /**
     * Closes the specified service.
     * 
     * @param ctx
     *            The security context
     * @param svc
     *            The service to handle.
     */
    public void closeService(SecurityContext ctx,
            StatefulServiceInterfacePrx svc) throws Exception {
        Connector c = getConnector(ctx, false, true);
        if (c != null) {
            c.close(svc);
        } else {
            svc.close(); // Last ditch effort to close.
        }
    }
    
    /**
     * Retrieves an updated version of the specified object.
     *
     * @param ctx The security context.
     * @param klassName The type of object to retrieve.
     * @param id The object's id.
     * @return The last version of the object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public IObject findIObject(SecurityContext ctx, String klassName, long id)
            throws DSOutOfServiceException, DSAccessException
    {
            try {
                IQueryPrx service = getQueryService(ctx);
                    return service.find(klassName, id);
            } catch (Throwable t) {
                    handleException(t, "Cannot retrieve the requested object with "+
                                    "object ID: "+id);
            }
            return null;
    }
    
    /**
     * Deletes the specified object.
     *
     * @param ctx The security context.
     * @param object    The object to delete.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IUpdate#deleteObject(IObject)
     */
    public void deleteObject(SecurityContext ctx, IObject object)
            throws DSOutOfServiceException, DSAccessException
    {
            try {
                IUpdatePrx service = getUpdateService(ctx);
                    service.deleteObject(object);
            } catch (Throwable t) {
                    handleException(t, "Cannot delete the object.");
            }
    }
    
    /**
     * Uploads the passed file to the server and returns the
     * original file i.e. the server object.
     *
     * @param ctx The security context.
     * @param file The file to upload.
     * @param mimeType The mimeType of the file.
     * @param originalFileID The id of the file or <code>-1</code>.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public OriginalFile upload(SecurityContext ctx, File file,
                    String mimeType, long originalFileID)
            throws DSAccessException, DSOutOfServiceException
    {
            if (file == null)
                    throw new IllegalArgumentException("No file to upload");
            if (StringUtils.isBlank(mimeType))
                    mimeType =  DEFAULT_MIMETYPE;

            boolean fileCreated = false;
    final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
    checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
    OriginalFile save = null;
    Long fileId = null;

            Connector c = getConnector(ctx, true, false);
            try {
                IUpdatePrx update = c.getUpdateService();
                    OriginalFile oFile;
                    if (originalFileID <= 0) {
                            oFile = new OriginalFileI();
                            String name = file.getName();
                            oFile.setName(omero.rtypes.rstring(name));
                            String absolutePath = file.getAbsolutePath();
                            String path = absolutePath.substring(0,
                                            absolutePath.length()-name.length());
                            oFile.setPath(omero.rtypes.rstring(path));
                            oFile.setSize(omero.rtypes.rlong(file.length()));
                            //Need to be modified
                            oFile.setMimetype(omero.rtypes.rstring(mimeType));
                            oFile.setHasher(checksumAlgorithm);
                            save =
                                    (OriginalFile) update.saveAndReturnObject(oFile);
                            fileId = save.getId().getValue();
                            fileCreated = true;
                    } else {
                            oFile = (OriginalFile) findIObject(ctx,
                                            OriginalFile.class.getName(), originalFileID);
                            if (oFile == null) {
                                    oFile = new OriginalFileI();
                                    String name = file.getName();
                                    oFile.setName(omero.rtypes.rstring(name));
                                    String absolutePath = file.getAbsolutePath();
                                    String path = absolutePath.substring(0,
                                                    absolutePath.length()-name.length());
                                    oFile.setPath(omero.rtypes.rstring(path));
                                    oFile.setSize(omero.rtypes.rlong(file.length()));
                                    //Need to be modified
                                    oFile.setMimetype(omero.rtypes.rstring(mimeType));
                                    oFile.setHasher(checksumAlgorithm);
                                    save = (OriginalFile) update.saveAndReturnObject(oFile);
                                    fileId = save.getId().getValue();
                                    fileCreated = true;
                            } else {
                                    OriginalFile newFile = new OriginalFileI();
                                    newFile.setId(omero.rtypes.rlong(originalFileID));
                                    newFile.setName(omero.rtypes.rstring(file.getName()));
                                    newFile.setPath(omero.rtypes.rstring(
                                                    file.getAbsolutePath()));
                                    newFile.setSize(omero.rtypes.rlong(file.length()));
                                    ChecksumAlgorithm oldHasher = oFile.getHasher();
                                    if (oldHasher == null) {
                                        oldHasher = checksumAlgorithm;
                                    }
                                    newFile.setHasher(oldHasher);
                                    newFile.setMimetype(oFile.getMimetype());
                                    save = (OriginalFile) update.saveAndReturnObject(newFile);
                                    fileId = save.getId().getValue();
                            }
                    }
            } catch (Exception e) {
                    handleException(e, "Cannot set the file's id.");
            }


            byte[] buf = new byte[INC];
            FileInputStream stream = null;
            final ChecksumProvider hasher = checksumProviderFactory.getProvider(
                            ChecksumType.SHA1);
            RawFileStorePrx store = null;
            try {
                store = c.getRawFileService();
                store.setFileId(fileId);
                    stream = new FileInputStream(file);
                    long pos = 0;
                    int rlen;
                    ByteBuffer bbuf;
                    while ((rlen = stream.read(buf)) > 0) {
                            store.write(buf, pos, rlen);
                            pos += rlen;
                            bbuf = ByteBuffer.wrap(buf);
                            bbuf.limit(rlen);
                            hasher.putBytes(bbuf);
                    }
                    stream.close();
                    OriginalFile f = store.save();
                    if (f != null) {
                            save = f;
                            final String clientHash = hasher.checksumAsString();
                            final String serverHash = save.getHash().getValue();
                            if (!clientHash.equals(serverHash)) {
                                throw new ImportException("file checksum mismatch on " +
                                            "upload: " + file +
                                        " (client has " + clientHash + ", " +
                                                    "server has " + serverHash + ")");
                            }
                    }

            } catch (Exception e) {
                    try {
                            if (fileCreated) deleteObject(ctx, save);
                            if (stream != null) stream.close();
                    } catch (Exception ex) {
                        //log("Exception on upload cleanup: " + e);  TODO:
                    }
                    handleConnectionException(e);
                    throw new DSAccessException("Cannot upload the file with path " +
                                    file.getAbsolutePath(), e);
            } finally {
                if (store != null) c.close(store);
            }
            return save;
    }
    
    /**
     * Returns the original file corresponding to the passed id.
     *
     * @param ctx The security context.
     * @param id The id identifying the file.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public OriginalFile getOriginalFile(SecurityContext ctx, long id)
            throws DSAccessException, DSOutOfServiceException
    {
            OriginalFile of = null;

            try {
                IQueryPrx svc = getQueryService(ctx);
                    ParametersI param = new ParametersI();
                    param.map.put("id", omero.rtypes.rlong(id));
                    of = (OriginalFile) svc.findByQuery(
                                    "select p from OriginalFile as p " +
                                    "where p.id = :id", param);
            } catch (Exception e) {
                    handleException(e, "Cannot retrieve original file");
            }
            return of;
    }
    
    /**
     * Downloads a file previously uploaded to the server.
     *
     * @param ctx The security context.
     * @param file The file to copy the data into.
     * @param fileID The id of the file to download.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public File download(SecurityContext ctx, File file,
                    long fileID)
            throws DSAccessException, DSOutOfServiceException
    {
        if (file == null) return null;
        OriginalFile of = getOriginalFile(ctx, fileID);
        if (of == null) return null;

        final String path = file.getAbsolutePath();

        Connector c = getConnector(ctx, true, false);
        RawFileStorePrx store = null;
        try {
            store = c.getRawFileService();
                store.setFileId(fileID);
        } catch (Throwable e) {
            c.close(store);
                handleException(e, "Cannot set the file's id.");
                return null; // Never reached.
        }

        try {
                long size = -1;
                long offset = 0;
                FileOutputStream stream = new FileOutputStream(file);
                try {
                        try {
                            size = store.size();
                                for (offset = 0; (offset+INC) < size;) {
                                        stream.write(store.read(offset, INC));
                                        offset += INC;
                                }
                        } finally {
                                stream.write(store.read(offset, (int) (size-offset)));
                                stream.close();
                        }
                } catch (Exception e) {
                        if (stream != null) stream.close();
                        if (file != null) file.delete();
                }
        } catch (IOException e) {
                if (file != null) file.delete();
                c.close(store);
                throw new DSAccessException("Cannot create file  " +path, e);
        } finally {
            c.close(store);
        }

        return file;
    }
    
    /**
     * Updates the specified object.
     *
     * @param ctx The security context.
     * @param object The object to update.
     * @param options Options to update the data.
     * @return The updated object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx, IObject object,
                    Map options)
            throws DSOutOfServiceException, DSAccessException
    {
        Connector c = getConnector(ctx, true, false);
            try {
                IUpdatePrx service = c.getUpdateService();
                    if (options == null) return service.saveAndReturnObject(object);
                    return service.saveAndReturnObject(object, options);
            } catch (Throwable t) {
                    handleException(t, "Cannot update the object.");
            }
            return null;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx The security context.
     * @param object The object to update.
     * @param options Options to update the data.
     * @param userName The name of the user to create the data for.
     * @return The updated object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public IObject saveAndReturnObject(SecurityContext ctx, IObject object,
                    Map options, String userName)
            throws DSOutOfServiceException, DSAccessException
    {
        IObject obj = null;
        
        Connector c = getConnector(ctx, true, false);
        try {
            // Must be inside try because of Throwable
            c = c.getConnector(userName); // Replace
            IUpdatePrx service = c.getUpdateService();

            if (options == null)
                obj = service.saveAndReturnObject(object);
            else
                obj = service.saveAndReturnObject(object, options);
        } catch (Throwable t) {
            handleException(t, "Cannot update the object.");
        }
        
        try {
            c.closeDerived(isAvailable());
        } catch (Throwable e) {
            new Exception("Cannot close the derived connectors", e);
        }
        
        return obj;
    }

    /**
     * Updates the specified object.
     *
     * @param ctx The security context.
     * @param objects The objects to update.
     * @param options Options to update the data.
     * @return The updated object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @see IPojos#updateDataObject(IObject, Map)
     */
    public List<IObject> saveAndReturnObject(SecurityContext ctx,
                    List<IObject> objects, Map options, String userName)
            throws DSOutOfServiceException, DSAccessException
    {
        Connector c = getConnector(ctx, true, false);
            try {
        // Must be inside try because of Throwable
        c = c.getConnector(userName); // Replace
            IUpdatePrx service = c.getUpdateService();
                    return service.saveAndReturnArray(objects);
            } catch (Throwable t) {
                    handleException(t, "Cannot update the object.");
            }
            return new ArrayList<IObject>();
    }
    
    /**
     * Save the ROI for the image to the server.
     *
     * @param ctx The security context.
     * @param imageID       The image's ID.
     * @param userID        The user's ID.
     * @param roiList       The list of ROI to save.
     * @return updated list of ROIData objects.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public List<ROIData> saveROI(SecurityContext ctx, long imageID, long userID,
                    List<ROIData> roiList)
            throws DSOutOfServiceException, DSAccessException
    {
            try {
                IUpdatePrx updateService = getUpdateService(ctx);
                IRoiPrx svc = getROIService(ctx);
                    RoiOptions options = new RoiOptions();
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
                    long id;
                    RoiResult tempResults;
                    int shapeIndex;

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
                                    updateService.saveAndReturnObject(rr);
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
                                            if (s != null) updateService.deleteObject(s);
                                    } else {
                                            s = (Shape) entry.getValue();
                                            if (s instanceof Line || s instanceof Polyline) {
                                                    shape = clientCoordMap.get(coord);
                                                    if ((s instanceof Line &&
                                                                    shape.asIObject() instanceof Polyline) ||
                                                            (s instanceof Polyline &&
                                                                    shape.asIObject() instanceof Line)) {
                                                            removed.add(coord);
                                                            updateService.deleteObject(s);
                                                            deleted.add(shape.getId());
                                                    }
                                            }
                                    }
                            }
                            /*
                             * Step 5. retrieve new roi as some are stale.
                             */
                            if (serverRoi != null) {
                                    id = serverRoi.getId().getValue();
                                    tempResults = svc.findByImage(imageID, new RoiOptions());
                                    for (Roi rrr : tempResults.rois) {
                                            if (rrr.getId().getValue() == id)
                                                    serverRoi = rrr;
                                    }
                            }

                            /*
                             * Step 6. Check to see if the roi in the client has been updated
                             * if so replace the server roiShape with the client one.
                             */
                            si = clientCoordMap.entrySet().iterator();
                            Shape serverShape;
                            long sid;
                            while (si.hasNext()) {
                                    entry = (Entry) si.next();
                                    coord = (ROICoordinate) entry.getKey();
                                    shape = (ShapeData) entry.getValue();

                                    if (shape != null) {
                                            if (!serverCoordMap.containsKey(coord))
                                                    serverRoi.addShape((Shape) shape.asIObject());
                                            else if (shape.isDirty()) {
                                                    shapeIndex = -1;
                                                    if (deleted.contains(shape.getId())) {
                                                            serverRoi.addShape((Shape) shape.asIObject());
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
                                                                            updateService.deleteObject(serverShape);
                                                                    serverRoi.addShape(
                                                                                    (Shape) shape.asIObject());
                                                            } else {
                                                                    throw new Exception("serverRoi.shapeList " +
                                                                            "is corrupted");
                                                            }
                                                    }
                                                    else
                                                            serverRoi.setShape(shapeIndex,
                                                                    (Shape) shape.asIObject());
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
                                    serverRoi.setNamespaces(ri.getNamespaces());
                                    serverRoi.setKeywords(ri.getKeywords());
                                    serverRoi.setImage(unloaded);
                                    updateService.saveAndReturnObject(serverRoi);
                            }

                    }
                    return roiList;
            } catch (Exception e) {
                    handleException(e, "Cannot Save the ROI for image: "+imageID);
            }
            return new ArrayList<ROIData>();
    }

    /**
     * Creates or recycles the import store.
     *
     * @param ctx The security context.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public OMEROMetadataStoreClient getImportStore(SecurityContext ctx,
                    String userName)
            throws DSAccessException, DSOutOfServiceException
    {
            Connector c = null;
            OMEROMetadataStoreClient prx = null;
            try {
                    c = getConnector(ctx, true, false);
                    c = c.getConnector(userName);
                    prx = c.getImportStore();
            } catch (Throwable e) {
                    //method throws an exception
                    handleException(e, "Cannot access Import service.");
            }
            if (prx == null)
                    throw new DSOutOfServiceException(
                                    "Cannot access the Import service.");
            return prx;
    }

    /**
     * Reads the file hosting the user photo.
     *
     * @param ctx The security context.
     * @param fileID The id of the file.
     * @param size   The size of the file.
     * @return See above
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public byte[] retrieveUserPhoto(SecurityContext ctx,
                    long fileID, long size)
            throws DSOutOfServiceException, DSAccessException
    {
        Connector c = getConnector(ctx, true, false);
            RawFileStorePrx store = null;
            try {
                store = c.getRawFileService();
                    store.setFileId(fileID);
                    return store.read(0, (int) size);
            } catch (Exception e) {
                    handleConnectionException(e);
                    throw new DSAccessException("Cannot read the file" +fileID, e);
            } finally {
                if (store != null) c.close(store);
            }
    }
    
    /**
     * Retrieves the archived files if any for the specified set of pixels.
     *
     * @param ctx The security context.
     * @param file The location where to save the files.
     * @param image The image to retrieve.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public Map<Boolean, Object> retrieveArchivedFiles(
                    SecurityContext ctx, File file, ImageData image)
            throws DSAccessException, DSOutOfServiceException
    {
            List<?> files = null;
            String query;
            try {
                IQueryPrx service = getQueryService(ctx);
                    ParametersI param = new ParametersI();
                    long id;
                    if (image.isFSImage()) {
                            id = image.getId();
                            List<RType> l = new ArrayList<RType>();
                            l.add(omero.rtypes.rlong(id));
                            param.add("imageIds", omero.rtypes.rlist(l));
                            query = createFileSetQuery();
                    } else {//Prior to FS
                            if (image.isArchived()) {
                                    StringBuffer buffer = new StringBuffer();
                                    id = image.getDefaultPixels().getId();
                                    buffer.append("select ofile from OriginalFile as ofile ");
                                    buffer.append("join fetch ofile.hasher ");
                                    buffer.append("left join ofile.pixelsFileMaps as pfm ");
                                    buffer.append("left join pfm.child as child ");
                                    buffer.append("where child.id = :id");
                                    param.map.put("id", omero.rtypes.rlong(id));
                                    query = buffer.toString();
                            } else return null;
                    }
                    files = service.findAllByQuery(query, param);
            } catch (Exception e) {
                    handleConnectionException(e);
                    throw new DSAccessException("Cannot retrieve original file", e);
            }

            Map<Boolean, Object> result = new HashMap<Boolean, Object>();
            if (CollectionUtils.isEmpty(files)) return result;
            Iterator<?> i;
            List<OriginalFile> values = new ArrayList<OriginalFile>();
            if (image.isFSImage()) {
                    i = files.iterator();
                    Fileset set;
                    List<FilesetEntry> entries;
                    Iterator<FilesetEntry> j;
                    while (i.hasNext()) {
                            set = (Fileset) i.next();
                            entries = set.copyUsedFiles();
                            j = entries.iterator();
                            while (j.hasNext()) {
                                    FilesetEntry fs = j.next();
                                    values.add(fs.getOriginalFile());
                            }
                    }
            } else values.addAll((List<OriginalFile>) files);

            RawFileStorePrx store = null;
            OriginalFile of;
            long size;
            FileOutputStream stream = null;
            long offset = 0;
            File f = null;
            List<File> results = new ArrayList<File>();
            List<String> notDownloaded = new ArrayList<String>();
            String folderPath = null;
            folderPath = file.getAbsolutePath();
            i = values.iterator();

            Connector c = getConnector(ctx, true, false);
            
            while (i.hasNext()) {
                    of = (OriginalFile) i.next();

                    try {
                        store = c.getRawFileService();
                            store.setFileId(of.getId().getValue());

                            if (folderPath != null) {
                                f = new File(folderPath, of.getName().getValue());
                            } else f = file;
                                results.add(f);

                            stream = new FileOutputStream(f);
                            size = of.getSize().getValue();
                            try {
                                    try {
                                            for (offset = 0; (offset+INC) < size;) {
                                                    stream.write(store.read(offset, INC));
                                                    offset += INC;
                                            }
                                    } finally {
                                            stream.write(store.read(offset, (int) (size-offset)));
                                            stream.close();
                                    }
                            } catch (Exception e) {
                                    if (stream != null) stream.close();
                                    if (f != null) {
                                            f.delete();
                                            results.remove(f);
                                    }
                                    notDownloaded.add(of.getName().getValue());
                                    handleConnectionException(e);
                            }
                    } 
                    catch (IOException e) {
                            if (f != null) {
                                    f.delete();
                                    results.remove(f);
                            }
                            notDownloaded.add(of.getName().getValue());
                            throw new DSAccessException("Cannot create file in folderPath",
                                            e);
                    } catch (ServerError sr) {
                        throw new DSAccessException("ServerError on retrieveArchived", sr);
                    } 
                    
                    finally {
                        c.close(store);
                    }
            }
            result.put(Boolean.valueOf(true), results);
            result.put(Boolean.valueOf(false), notDownloaded);
            return result;
    }
    
    /**
     * Loads the file set corresponding to the specified image.
     *
     * @param ctx The security context.
     * @param imageIds The collection of images id.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public Set<DataObject> getFileSet(SecurityContext ctx, Collection<Long> imageIds)
            throws DSOutOfServiceException, DSAccessException
    {

            try {
                IQueryPrx service = getQueryService(ctx);
                    List<RType> l = new ArrayList<RType>(imageIds.size());
                    Iterator<Long> j = imageIds.iterator();
                    while (j.hasNext())
                            l.add(omero.rtypes.rlong(j.next()));
                    ParametersI param = new ParametersI();
                    param.add("imageIds", omero.rtypes.rlist(l));
                    return PojoMapper.asDataObjects(service.findAllByQuery(
                                    createFileSetQuery(), param));
            } catch (Exception e) {
                    handleException(e, "Cannot retrieve the file set");
            }
            return new HashSet<DataObject>();
    }
    
    /**
     * Creates the query to load the file set corresponding to a given image.
     *
     * @return See above.
     */
    private String createFileSetQuery()
    {
            StringBuffer buffer = new StringBuffer();
            buffer.append("select fs from Fileset as fs ");
            buffer.append("join fetch fs.images as image ");
            buffer.append("left outer join fetch fs.usedFiles as usedFile ");
            buffer.append("join fetch usedFile.originalFile as f ");
            buffer.append("join fetch f.hasher ");
            buffer.append("where image.id in (:imageIds)");
            return buffer.toString();
    }
    
    /**
     * Creates a new rendering service for the specified pixels set.
     *
     * @param ctx The security context.
     * @param pixelsID  The pixels set ID.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     * @throws FSAccessException If an error occurred when trying to build a
     * pyramid or access file not available.
     */
    public RenderingEnginePrx generateRenderingEngine(
                    SecurityContext ctx, long pixelsID)
            throws DSOutOfServiceException, DSAccessException, FSAccessException
    {
        Connector c = getConnector(ctx, true, false);
        RenderingEnginePrx service = null;
            try {
                service = c.getRenderingService(pixelsID);
                    service.lookupPixels(pixelsID);
                    needDefault(pixelsID, service);
                    service.load();
                    return service;
            } catch (Throwable t) {
                c.close(service);
                    String s = "Cannot start the Rendering Engine.";
                    handleFSException(t, s);
                    handleException(t, s);
            }
            return null;
    }
    
    /**
     * Checks if some default rendering settings have to be created
     * for the specified set of pixels.
     *
     * @param pixelsID      The pixels ID.
     * @param prx The rendering engine to load or thumbnail store.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    private void needDefault(long pixelsID, Object prx)
            throws DSAccessException, DSOutOfServiceException
    {
            try {
                    if (prx instanceof ThumbnailStorePrx) {
                            ThumbnailStorePrx service = (ThumbnailStorePrx) prx;
                            if (!(service.setPixelsId(pixelsID))) {
                            }
                    } else if (prx instanceof RenderingEnginePrx) {
                            RenderingEnginePrx re = (RenderingEnginePrx) prx;
                            if (!re.lookupRenderingDef(pixelsID)) {
                                    re.resetDefaults();
                                    re.lookupRenderingDef(pixelsID);
                            }
                    }
            } catch (Throwable e) {
                    handleConnectionException(e);
                    handleException(e, "Cannot set the rendering defaults.");
            }
    }
    
    
    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message.
     * This method is not supposed to be used in this class' constructor or in
     * the login/logout methods.
     *
     * @param t The exception.
     * @param message The context message.
     * @throws FSAccessException A server-side error.
     */
    private void handleFSException(Throwable t, String message)
            throws FSAccessException
    {
            boolean b = handleConnectionException(t);
            if (!b) return;
            if (!isConnected()) return;
            Throwable cause = t.getCause();
            String s = "\nImage not ready. Please try again later.";
            if (cause instanceof ConcurrencyException) {
                    ConcurrencyException mpe = (ConcurrencyException) cause;
                    //s += ", ready in approximately ";
                    //s += UIUtilities.calculateHMSFromMilliseconds(mpe.backOff);
                    FSAccessException fsa = new FSAccessException(message+s, cause);
                    if (mpe instanceof MissingPyramidException ||
                                    mpe instanceof LockTimeout)
                            fsa.setIndex(FSAccessException.PYRAMID);
                    fsa.setBackOffTime(mpe.backOff);
                    throw fsa;
            } else if (t instanceof ConcurrencyException) {
                    ConcurrencyException mpe = (ConcurrencyException) t;
                    s += mpe.backOff;
                    FSAccessException fsa = new FSAccessException(message+s, t);
                    if (mpe instanceof MissingPyramidException ||
                                    mpe instanceof LockTimeout)
                            fsa.setIndex(FSAccessException.PYRAMID);
                    fsa.setBackOffTime(mpe.backOff);
                    throw fsa;
            }
    }
    
    
    /**
     * Returns the back-off time if it requires a pyramid to be built,
     * <code>null</code> otherwise.
     *
     * @param ctx The security context.
     * @param pixelsId The identifier of the pixels set to handle.
     * @return See above
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public Boolean isLargeImage(SecurityContext ctx, long pixelsId)
            throws DSOutOfServiceException, DSAccessException
    {
    Connector c = getConnector(ctx, true, false);
    RawPixelsStorePrx store = null;
        try {
            store = c.getPixelsStore();
                    store.setPixelsId(pixelsId, true);
                    return store.requiresPixelsPyramid();
            } catch (Exception e) {
                    handleException(e, "Cannot start the Raw pixels store.");
            } finally {
                if (store != null) c.close(store);
            }
            return null;
    }
    
    /**
     * Updates the specified group.
     *
     * @param ctx The security context.
     * @param group The group to update.
     * @param permissions The new permissions.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public CmdCallback updateGroup(SecurityContext ctx, GroupData group,
                    int permissions)
            throws DSOutOfServiceException, DSAccessException
    {
            try {
                IAdminPrx svc = getAdminService(ctx);
                    ExperimenterGroup g = group.asGroup();
                    svc.updateGroup(g);
                    if (group.getPermissions().getPermissionsLevel() != permissions
                                    && permissions >= 0) {
                            String r = "rw----";
                            switch (permissions) {
                                    case GroupData.PERMISSIONS_GROUP_READ:
                                            r = "rwr---";
                                            break;
                                    case GroupData.PERMISSIONS_GROUP_READ_LINK:
                                            r = "rwra--";
                                            break;
                                    case GroupData.PERMISSIONS_GROUP_READ_WRITE:
                                            r = "rwrw--";
                                            break;
                                    case GroupData.PERMISSIONS_PUBLIC_READ:
                                            r = "rwrwr-";
                            }
                            Chmod chmod = new Chmod(REF_GROUP, group.getId(), null, r);
                            List<Request> l = new ArrayList<Request>();
                            l.add(chmod);
                            return getConnector(ctx, true, false).submit(l, null);
                    }
            } catch (Throwable t) {
                    handleException(t, "Cannot update the group. ");
            }
            return null;
    }
    
    /**
     * Executes the commands.
     *
     * @param commands The commands to execute.
     * @param ctx The security context.
     * @return See above.
     * @throws ProcessException If an error occurred while running the script.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public CmdCallbackI submit(List<Request> commands, SecurityContext ctx)
            throws ProcessException, DSOutOfServiceException, DSAccessException
    {
            try {
                    Connector c = getConnector(ctx, true, true);
                    if (c == null) return null;
                    return c.submit(commands, null);
            } catch (Throwable e) {
                    handleException(e, "Cannot execute the command.");
                    // Never reached
                    throw new ProcessException("Cannot execute the command.", e);
            }
    }
    
    /**
     * Moves data between groups.
     *
     * @param ctx The security context of the source group.
     * @param target The security context of the destination group.
     * @param map The object to move and where to move them
     * @param options The options.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public CmdCallbackI transfer(SecurityContext ctx, SecurityContext target,
                    Map<DataObject, List<IObject>> map, Map<String, String> options)
            throws DSOutOfServiceException, DSAccessException
    {
            Connector c = getConnector(ctx, true, true);
            if (c == null) return null; // TODO:
            try {
                    Entry<DataObject, List<IObject>> entry;
                    Iterator<Entry<DataObject, List<IObject>>>
                    i = map.entrySet().iterator();
                    DataObject data;
                    List<IObject> l;
                    Iterator<IObject> j;
                    List<Request> commands = new ArrayList<Request>();
                    Chgrp cmd;
                    Save save;
                    Map<Long, List<IObject>> images = new HashMap<Long, List<IObject>>();
                    while (i.hasNext()) {
                            entry = i.next();
                            data = entry.getKey();
                            l = entry.getValue();
                            if (data instanceof ImageData) {
                                    images.put(data.getId(), l);
                            } else {
                                    cmd = new Chgrp(createDeleteCommand(
                                            data.getClass().getName()), data.getId(), options,
                                            target.getGroupID());
                                    commands.add(cmd);
                                    j = l.iterator();
                                    while (j.hasNext()) {
                                            save = new Save();
                                            save.obj = j.next();
                                            commands.add(save);
                                    }
                            }
                    }
                    if (images.size() > 0) {
                            Set<DataObject> fsList = getFileSet(ctx, images.keySet());
                            List<Long> all = new ArrayList<Long>();
                            Iterator<DataObject> kk = fsList.iterator();
                            FilesetData fs;
                            List<Long> imageIds;
                            Iterator<Long> ii;
                            while (kk.hasNext()) {
                                    fs = (FilesetData) kk.next();
                                    imageIds = fs.getImageIds();
                                    if (imageIds.size() > 0) {
                                            cmd = new Chgrp(createDeleteCommand(
                                                            FilesetData.class.getName()), fs.getId(),
                                                            options, target.getGroupID());
                                            commands.add(cmd);
                                            all.addAll(imageIds);
                                            ii = imageIds.iterator();
                                            while (ii.hasNext()) {
                                                    l = images.get(ii.next());
                                                    j = l.iterator();
                                                    while (j.hasNext()) {
                                                            save = new Save();
                                                            save.obj = j.next();
                                                            commands.add(save);
                                                    }
                                            }
                                    }
                            }

                            //Now check that all the ids are covered.
                            Entry<Long, List<IObject>> ee;
                            Iterator<Entry<Long, List<IObject>>> e =
                                            images.entrySet().iterator();
                            while (e.hasNext()) {
                                    ee = e.next();
                                    if (!all.contains(ee.getKey())) { //pre-fs data.
                                            cmd = new Chgrp(createDeleteCommand(
                                                            ImageData.class.getName()), ee.getKey(),
                                                            options, target.getGroupID());
                                            commands.add(cmd);
                                            l = images.get(ee.getKey());
                                            j = l.iterator();
                                            while (j.hasNext()) {
                                                    save = new Save();
                                                    save.obj = j.next();
                                                    commands.add(save);
                                            }
                                    }
                            }
                    }
                    return c.submit(commands, target);
            } catch (Throwable e) {
                    handleException(e, "Cannot transfer the data.");
            }
            return null;
    }
    
    
    /**
     * Returns the collection of annotations of a given type.
     *
     * @param ctx The security context.
     * @param annotationType        The type of annotation.
     * @param terms                         The terms to search for.
     * @param start                         The lower bound of the time interval
     *                                                      or <code>null</code>.
     * @param end                           The lower bound of the time interval
     *                                                      or <code>null</code>.
     * @param exp                           The experimenter who annotated the object.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public List filterBy(SecurityContext ctx, Class annotationType, List<String> terms,
                            Timestamp start, Timestamp end, ExperimenterData exp)
            throws DSOutOfServiceException, DSAccessException
    {
            Connector c = getConnector(ctx, true, false);
        SearchPrx service = null;
            try {
                service = c.getSearchService();
                    if (start != null && end != null)
                            service.onlyAnnotatedBetween(
                                            omero.rtypes.rtime(start.getTime()),
                                            omero.rtypes.rtime(end.getTime()));
                    if (exp != null) {
                            Details d = new DetailsI();
                            d.setOwner(exp.asExperimenter());
                    }

                    List<String> t = prepareTextSearch(terms, service);
                    service.onlyType(convertPojos(annotationType).getName());
                    List rType = new ArrayList();
                    //service.bySomeMustNone(fSome, fMust, fNone);
                    service.bySomeMustNone(t, null, null);
                    Object size = handleSearchResult(
                                    convertTypeForSearch(annotationType), rType, service);
                    if (size instanceof Integer) rType = new ArrayList();
                    return rType;
            } catch (Exception e) {
                    handleException(e, "Filtering by annotation not valid");
            } finally {
            if (service != null) c.close(service);
            }
            return new ArrayList();
    }
    
    /**
     * Formats the terms to search for.
     *
     * @param terms         The terms to search for.
     * @param service       The search service.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    private List<String> prepareTextSearch(Collection<String> terms,
                    SearchPrx service)
            throws DSAccessException, DSOutOfServiceException
    {
            if (CollectionUtils.isEmpty(terms)) return null;
            String[] values = new String[terms.size()];
            Iterator<String> i = terms.iterator();
            int index = 0;
            while (i.hasNext()) {
                    values[index] = i.next();
                    index++;
            }
            return prepareTextSearch(values, service);
    }
    
    /**
     * Formats the terms to search for.
     *
     * @param terms         The terms to search for.
     * @param service       The search service.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    private List<String> prepareTextSearch(String[] terms, SearchPrx service)
            throws DSAccessException, DSOutOfServiceException
    {
            if (terms == null || terms.length == 0) return null;
            String value;
            int n;
            char[] arr;
            String v;
            List<String> formattedTerms = new ArrayList<String>(terms.length);
            String formatted;
            try {
                    for (int j = 0; j < terms.length; j++) {
                            value = terms[j];
                            if (startWithWildCard(value))
                                    service.setAllowLeadingWildcard(true);
                            //format string
                            n = value.length();
                            arr = new char[n];
                            v = "";
                            value.getChars(0, n, arr, 0);
                            for (int i = 0; i < arr.length; i++) {
                                    if (SUPPORTED_SPECIAL_CHAR.contains(arr[i]))
                                            v += "\\"+arr[i];
                                    else v += arr[i];
                            }
                            if (value.contains(" "))
                                    formatted = "\""+v.toLowerCase()+"\"";
                            else formatted = v.toLowerCase();
                            formattedTerms.add(formatted);
                    }
            } catch (Throwable e) {
                    handleException(e, "Cannot format text for search.");
            }
            return formattedTerms;
    }
    
    /**
     * Returns <code>true</code> if the specified value starts with a wild card,
     * <code>false</code> otherwise.
     *
     * @param value The value to handle.
     * @return See above.
     */
    private boolean startWithWildCard(String value)
    {
            if (StringUtils.isBlank(value)) return false;
            Iterator<String> i = WILD_CARDS.iterator();
            String card = null;
            while (i.hasNext()) {
                    card = i.next();
                    if (value.startsWith(card)) {
                            return true;
                    }
            }
            return false;
    }
    
    /**
     * Handles the result of the search.
     *
     * @param type  The supported type.
     * @param r             The collection to fill.
     * @param svc   Helper reference to the service.
     * @return See above.
     * @throws ServerError If an error occurs while reading the results.
     */
    private Object handleSearchResult(String type, Collection r, SearchPrx svc)
            throws ServerError
    {
            //First get object of a given type.
            boolean hasNext = false;
            try {
                    hasNext = svc.hasNext();
            } catch (Exception e) {
                    int size = 0;
                    if (e instanceof InternalException) size = -1;
                    else svc.getBatchSize();
                    return Integer.valueOf(size);
            }
            if (!hasNext) return r;
            List l = svc.results();
            Iterator k = l.iterator();
            IObject object;
            long id;
            while (k.hasNext()) {
                    object = (IObject) k.next();
                    if (type.equals(object.getClass().getName())) {
                            id = object.getId().getValue();
                            if (!r.contains(id))
                                    r.add(id); //Retrieve the object of a given type.
                    }
            }
            return r;
    }
    
    /**
     * Returns the specified script.
     * 
     * @param ctx
     *            The security context.
     * @param scriptID
     *            The identifier of the script to run.
     * @param parameters
     *            The parameters to pass to the script.
     * @return See above.
     * @throws ProcessException
     *             If an error occurred while running the script.
     */
    public ProcessCallbackI runScript(SecurityContext ctx, long scriptID,
            Map<String, RType> parameters) throws ProcessException {
        ProcessCallbackI cb = null;
        try {
            IScriptPrx svc = getScriptService(ctx);
            Connector c = getConnector(ctx, true, false);
            // scriptID, parameters, timeout (5s if null)
            ScriptProcessPrx prx = svc.runScript(scriptID, parameters, null);
            cb = new ProcessCallbackI(c.getClient(), prx);
        } catch (Exception e) {
            handleConnectionException(e);
            throw new ProcessException("Cannot run script with ID:" + scriptID,
                    e);
        }
        return cb;
    }
    
    /**
     * Returns the XY-plane identified by the passed z-section, time-point
     * and wavelength.
     *
     * @param ctx The security context.
     * @param pixelsID The id of pixels containing the requested plane.
     * @param z The selected z-section.
     * @param t The selected time-point.
     * @param c The selected wavelength.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public byte[] retrievePlane(SecurityContext ctx,
                    long pixelsID, int z, int t, int c)
            throws DSOutOfServiceException, DSAccessException, FSAccessException
    {
        Connector conn = getConnector(ctx, true, false);
            RawPixelsStorePrx service = null;
            try {
                service = conn.getPixelsStore();
                    service.setPixelsId(pixelsID, false);
                    byte[] plane = service.getPlane(z, c, t);
                    return plane;
            } catch (Throwable e) {
                    if (e instanceof ValidationException) return null;
                    String s = "Cannot retrieve the plane " +
                    "(z="+z+", t="+t+", c="+c+") for pixelsID: "+pixelsID;
                    handleFSException(e, s);
                    handleException(e, s);
            } finally {
                if (service != null) conn.close(service);
            }
            return null;
    }
    
    /**
     * Retrieves the thumbnail for the passed collection of pixels set.
     *
     * @param ctx The security context.
     * @param pixelsID The collection of pixels set.
     * @param maxLength The maximum length of the thumbnail width or height
     *                                      depending on the pixel size.
     * @param reset Pass <code>true</code> to reset the thumbnail store,
     *              <code>false</code> otherwise.
     * @return See above.
     * @throws RenderingServiceException If an error occurred while trying to
     *              retrieve data from the service.
     * @throws DSOutOfServiceException If the connection is broken.
     */
    public Map retrieveThumbnailSet(SecurityContext ctx,
                    List<Long> pixelsID, int maxLength, boolean reset)
            throws RenderingServiceException, DSOutOfServiceException
    {
    Connector c = getConnector(ctx, true, false);
            ThumbnailStorePrx service = null;
            try {
                service = c.getThumbnailService();
                    return service.getThumbnailByLongestSideSet(
                                    omero.rtypes.rint(maxLength), pixelsID);
            } catch (Throwable t) {
                    handleConnectionException(t);
                    if (t instanceof ServerError) {
                            throw new DSOutOfServiceException(
                                            "Thumbnail service null for pixelsID: "+pixelsID, t);
                    }
                    throw new RenderingServiceException("Cannot get thumbnail", t);
            } finally {
                c.close(service);
            }
    }
    
    /**
     * Retrieves the thumbnail for the passed set of pixels.
     *
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set the thumbnail is for.
     * @param maxLength The maximum length of the thumbnail width or height
     *                                      depending on the pixel size.
     * @return See above.
     * @throws RenderingServiceException If an error occurred while trying to
     *              retrieve data from the service.
     * @throws DSOutOfServiceException If the connection is broken.
     */
    public byte[] retrieveThumbnailByLongestSide(
                    SecurityContext ctx, long pixelsID, int maxLength)
            throws RenderingServiceException, DSOutOfServiceException
    {
    Connector c = getConnector(ctx, true, false);
            ThumbnailStorePrx service = null;
            try {
                service = c.getThumbnailService();
                    // No need to call setPixelsID if using set method?
                    return service.getThumbnailByLongestSide(
                                    omero.rtypes.rint(maxLength));
            } catch (Throwable t) {
                    handleConnectionException(t);
                    if (t instanceof ServerError) {
                            throw new DSOutOfServiceException(
                                            "Thumbnail service null for pixelsID: "+pixelsID, t);
                    }
                    throw new RenderingServiceException("Cannot get thumbnail", t);
            } finally {
                if (service != null) c.close(service);
            }
    }
    
    /**
     * Retrieves the thumbnail for the passed set of pixels.
     *
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set the thumbnail is for.
     * @param sizeX The size of the thumbnail along the X-axis.
     * @param sizeY The size of the thumbnail along the Y-axis.
     * @param userID The id of the user the thumbnail is for.
     * @return See above.
     * @throws RenderingServiceException If an error occurred while trying to
     *              retrieve data from the service.
     * @throws DSOutOfServiceException If the connection is broken.
     */
    public byte[] retrieveThumbnail(SecurityContext ctx,
                    long pixelsID, int sizeX, int sizeY, long userID)
            throws RenderingServiceException, DSOutOfServiceException
    {
        Connector c = getConnector(ctx, true, false);
        ThumbnailStorePrx service = null;
            try {
                service = c.getThumbnailService();
                    needDefault(pixelsID, service);
                    //getRendering Def for a given pixels set.
                    if (userID >= 0) {
                            RenderingDef def = getRenderingDef(ctx, pixelsID, userID);
                            if (def != null) service.setRenderingDefId(
                                            def.getId().getValue());
                    }
                    return service.getThumbnail(omero.rtypes.rint(sizeX),
                                    omero.rtypes.rint(sizeY));
            } catch (Throwable t) {
                    handleConnectionException(t);
                    if (t instanceof ServerError) {
                            throw new DSOutOfServiceException(
                                            "Thumbnail service null for pixelsID: "+pixelsID, t);
                    }
                    throw new RenderingServiceException("Cannot get thumbnail", t);
            } finally {
                if (service != null) c.close(service);
            }
    }
    
    /**
     * Retrieves the rendering settings for the specified pixels set.
     *
     * @param ctx The security context.
     * @param pixelsID  The pixels ID.
     * @param userID        The id of the user who set the rendering settings.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    private RenderingDef getRenderingDef(SecurityContext ctx, long pixelsID,
                    long userID)
            throws DSOutOfServiceException, DSAccessException
    {

            try {
                IPixelsPrx service = getPixelsService(ctx);
                    return service.retrieveRndSettingsFor(pixelsID, userID);
            } catch (Exception e) {
                    handleException(e, "Cannot retrieve the rendering settings");
            }
            return null;
    }
    
    /**
     * Returns the file
     *
     * @param index Either OME-XML or OME-TIFF.
     * @param file          The file to write the bytes.
     * @param imageID       The id of the image.
     * @param ctx The security context.
     * @param file The file to write the bytes.
     * @param imageID The id of the image.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public File exportImageAsOMEObject(SecurityContext ctx, ExportFormat format, File f,
                    long imageID)
            throws DSAccessException, DSOutOfServiceException
    {
            FileOutputStream stream = null;
            DSAccessException exception = null;
            Connector c = getConnector(ctx, true, false);
            try {
                ExporterPrx store = c.getExporterService();
                    stream = new FileOutputStream(f);
                    try {
                            store.addImage(imageID);
                            try {
                                    long size = 0;
                                    if (format == ExportFormat.OME_XML)
                                            size = store.generateXml();
                                    else size = store.generateTiff();
                                    long offset = 0;
                                    try {
                                            for (offset = 0; (offset+INC) < size;) {
                                                    stream.write(store.read(offset, INC));
                                                    offset += INC;
                                            }
                                    } finally {
                                            stream.write(store.read(offset, (int) (size-offset)));
                                            stream.close();
                                    }
                            } catch (Exception e) {
                                    if (stream != null) stream.close();
                                    if (f != null) f.delete();
                                    exception = new DSAccessException(
                                                    "Cannot export the image as an OME-formats ", e);
                                    handleConnectionException(e);
                            }
                    } finally {
                        c.close(store);
                            if (exception != null) throw exception;
                    }
            } catch (Throwable t) {
                    if (f != null) f.delete();
                    throw new DSAccessException(
                                    "Cannot export the image as an OME-TIFF", t);
            }
            return f;
    }
    
    /**
     * Searches for data.
     *
     * @param ctx The security context.
     * @param context The context of search.
     * @return The found objects.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
    public Object performSearch(SecurityContext ctx, SearchDataContext context)
            throws DSOutOfServiceException, DSAccessException
    {
        Map<Integer, Object> results = new HashMap<Integer, Object>();
        List<Class> types = context.getTypes();
        List<Integer> scopes = context.getScope();
        if (CollectionUtils.isEmpty(types)) return new HashMap();

        Connector c = getConnector(ctx, true, false);
        SearchPrx service = null;
        try {
            service = c.getSearchService();
                service.clearQueries();
                service.setAllowLeadingWildcard(true);
                service.setCaseSentivice(context.isCaseSensitive());
                Timestamp start = context.getStart();
                Timestamp end = context.getEnd();
                //Sets the time
                if (start != null || end != null) {
                        switch (context.getTimeIndex()) {
                                case SearchDataContext.CREATION_TIME:
                                        if (start != null && end != null)
                                                service.onlyCreatedBetween(
                                                        omero.rtypes.rtime(start.getTime()),
                                                        omero.rtypes.rtime(end.getTime()));
                                        else if (start != null && end == null)
                                                service.onlyCreatedBetween(
                                                                omero.rtypes.rtime(start.getTime()),
                                                                null);
                                        else if (start == null && end != null)
                                                service.onlyCreatedBetween(null,
                                                                omero.rtypes.rtime(end.getTime()));
                                        break;
                                case SearchDataContext.MODIFICATION_TIME:
                                        if (start != null && end != null)
                                                service.onlyModifiedBetween(
                                                        omero.rtypes.rtime(start.getTime()),
                                                        omero.rtypes.rtime(end.getTime()));
                                        else if (start != null && end == null)
                                                service.onlyModifiedBetween(
                                                                omero.rtypes.rtime(start.getTime()),
                                                                null);
                                        else if (start == null && end != null)
                                                service.onlyModifiedBetween(null,
                                                                omero.rtypes.rtime(end.getTime()));
                                        break;
                                case SearchDataContext.ANNOTATION_TIME:
                                        if (start != null && end != null)
                                                service.onlyAnnotatedBetween(
                                                        omero.rtypes.rtime(start.getTime()),
                                                        omero.rtypes.rtime(end.getTime()));
                                        else if (start != null && end == null)
                                                service.onlyAnnotatedBetween(
                                                                omero.rtypes.rtime(start.getTime()),
                                                                null);
                                        else if (start == null && end != null)
                                                service.onlyAnnotatedBetween(null,
                                                                omero.rtypes.rtime(end.getTime()));
                        }
                }
                List<ExperimenterData> users = context.getOwners();
                Iterator i;
                ExperimenterData exp;
                Details d;
                //owner
                List<Details> owners = new ArrayList<Details>();
                if (users != null && users.size() > 0) {
                        i = users.iterator();
                        while (i.hasNext()) {
                                exp = (ExperimenterData) i.next();
                                d = new DetailsI();
                                d.setOwner(exp.asExperimenter());
                        owners.add(d);
                        }
                }


                List<String> some = prepareTextSearch(context.getSome(), service);
                List<String> must = prepareTextSearch(context.getMust(), service);
                List<String> none = prepareTextSearch(context.getNone(), service);

                List<String> supportedTypes = new ArrayList<String>();
                i = types.iterator();
                while (i.hasNext())
                        supportedTypes.add(convertPojos((Class) i.next()).getName());

                List rType;

                Object size;
                Integer key;
                i = scopes.iterator();
                while (i.hasNext())
                        results.put((Integer) i.next(), new ArrayList());

                i = scopes.iterator();
                List<String> fSome = null, fMust = null, fNone = null;
                List<String> fSomeSec = null, fMustSec = null, fNoneSec = null;
                service.onlyType(Image.class.getName());
                while (i.hasNext()) {
                        key = (Integer) i.next();
                        rType = (List) results.get(key);
                        size = null;
                        if (key == SearchDataContext.TAGS) {
                                fSome = formatText(some, "tag");
                                fMust = formatText(must, "tag");
                                fNone = formatText(none, "tag");
                        } else if (key == SearchDataContext.NAME) {
                                fSome = formatText(some, "name");
                                fMust = formatText(must, "name");
                                fNone = formatText(none, "name");
                        } else if (key == SearchDataContext.DESCRIPTION) {
                                fSome = formatText(some, "description");
                                fMust = formatText(must, "description");
                                fNone = formatText(none, "description");
                        } else if (key == SearchDataContext.FILE_ANNOTATION) {
                                fSome = formatText(some, "file.name");
                                fMust = formatText(must, "file.name");
                                fNone = formatText(none, "file.name");
                                fSomeSec = formatText(some, "file.contents");
                                fMustSec = formatText(must, "file.contents");
                                fNoneSec = formatText(none, "file.contents");
                        } else if (key == SearchDataContext.TEXT_ANNOTATION) {
                                fSome = formatText(some, "annotation", "NOT", "tag");
                                fMust = formatText(must, "annotation", "NOT", "tag");
                                fNone = formatText(none, "annotation", "NOT", "tag");
                        } else if (key == SearchDataContext.URL_ANNOTATION) {
                                fSome = formatText(some, "url");
                                fMust = formatText(must, "url");
                                fNone = formatText(none, "url");
                        } else if (key == SearchDataContext.ID) {
                                fSome = formatText(some, "id");
                                fMust = formatText(must, "id");
                                fNone = formatText(none, "id");
                        } else {
                                fSome = formatText(some, "");
                                fMust = formatText(must, "");
                                fNone = formatText(none, "");
                        }
                        
                        //if (fSome != null) {
                        //while (owner.hasNext()) {
                                //d = owner.next();
                                //service.onlyOwnedBy(d);
                                service.bySomeMustNone(fSome, fMust, fNone);
                                size = handleSearchResult(
                                                convertTypeForSearch(Image.class), rType,
                                                service);
                                if (size instanceof Integer)
                                        results.put(key, size);
                                service.clearQueries();
                                if (!(size instanceof Integer) && fSomeSec != null &&
                                                fSomeSec.size() > 0) {
                                        service.bySomeMustNone(fSomeSec, fMustSec,
                                                        fNoneSec);
                                        size = handleSearchResult(
                                                        convertTypeForSearch(Image.class),
                                                        rType, service);
                                        if (size instanceof Integer)
                                                results.put(key, size);
                                        service.clearQueries();
                                }
                        //}
                        //}
                }
                return results;
        } catch (Throwable e) {
                handleException(e, "Cannot perform the search.");
        } finally {
            if (service != null) c.close(service);
        }
        return null;
    }
    
    public boolean isAdministrator(SecurityContext ctx, ExperimenterData exp) throws DSOutOfServiceException, DSAccessException {
        Collection<GroupData> groups = getAvailableGroups(ctx, exp, false);
        Iterator<GroupData> i = groups.iterator();
        GroupData g;
        while (i.hasNext()) {
            g = i.next();
                if (isSecuritySystemGroup(ctx, g.getId(), GroupData.SYSTEM)) {
                    return true;
                }
        }
        return false;
    }
    
    public boolean isSecuritySystemGroup(SecurityContext ctx, long groupID, String key) throws DSOutOfServiceException, DSAccessException
    {
        Roles roles = getSystemRoles(ctx);
        
        if (roles == null) return false;
        if (GroupData.USER.equals(key)) {
            return roles.userGroupId == groupID;
        } else if (GroupData.SYSTEM.equals(key)) {
            return roles.systemGroupId == groupID;
        } else if (GroupData.GUEST.equals(key)) {
            return roles.guestGroupId == groupID;
        }
        throw new IllegalArgumentException("Key not valid.");
    }
    
    /**
     * Retrieves the groups visible by the current experimenter.
     * 
     * @param ctx
     *            The security context.
     * @param loggedInUser
     *            The user currently logged in.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Set<GroupData> getAvailableGroups(SecurityContext ctx,
            ExperimenterData user, boolean excludeSystemGroups) throws DSOutOfServiceException,
            DSAccessException {

        Connector c = getConnector(ctx);
        Set<GroupData> pojos = new HashSet<GroupData>();
        try {
            IQueryPrx service = c.getQueryService();
            // Need method server side.
            ParametersI p = new ParametersI();
            p.addId(user.getId());
            List<IObject> groups = service
                    .findAllByQuery(
                            "select distinct g from ExperimenterGroup as g "
                                    + "join fetch g.groupExperimenterMap as map "
                                    + "join fetch map.parent e "
                                    + "left outer join fetch map.child u "
                                    + "left outer join fetch u.groupExperimenterMap m2 "
                                    + "left outer join fetch m2.parent p "
                                    + "where g.id in "
                                    + "  (select m.parent from GroupExperimenterMap m "
                                    + "  where m.child.id = :id )", p);
            ExperimenterGroup group;
            GroupData defaultGroup = null;
            long gid = user.getDefaultGroup().getId();
            
            // GroupData pojoGroup;
            Iterator<IObject> i = groups.iterator();
            while (i.hasNext()) {
                group = (ExperimenterGroup) i.next();
                GroupData g = (GroupData) PojoMapper.asDataObject(group);
                pojos.add(g);
                if (gid == g.getId()) 
                    defaultGroup = g;
            }
            
            if(excludeSystemGroups) {
                Roles roles = getSystemRoles(ctx);
                Iterator<GroupData> it = pojos.iterator();
                while(it.hasNext()) {
                    GroupData next = it.next();
                    long id = next.getGroupId();
                    if(id==roles.userGroupId || id==roles.systemGroupId || id==roles.guestId) {
                        it.remove();
                    }
                }
            }
            
          //to be on the safe side.
            if (pojos.size() ==  0) {
                //group with loaded users.
                if (defaultGroup != null) 
                    pojos.add(defaultGroup);
                else 
                    pojos.add(user.getDefaultGroup());
            }
            
            return pojos;
        } catch (Throwable t) {
            handleException(t, "Cannot retrieve the available groups ");
        }
        return pojos;
    }
    
    /**
     * Returns the system groups and users
     * 
     * @param ctx
     *            The security context.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @throws ServerError
     */
    public Roles getSystemRoles(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
       
        try {
            IAdminPrx svc = getAdminService(ctx);
            return svc.getSecurityRoles();
        } catch (ServerError e) {
            throw new DSOutOfServiceException("", e);
        }
    }

    /**
     * Formats the elements of the passed array. Adds the
     * passed field in front of each term.
     *
     * @param terms The terms to format.
     * @param field The string to add in front of the terms.
     * @return See above.
     */
    private List<String> formatText(List<String> terms, String field)
    {
            if (CollectionUtils.isEmpty(terms)) return null;
            if (StringUtils.isBlank(field)) return terms;
            List<String> formatted = new ArrayList<String>(terms.size());
            Iterator<String> j = terms.iterator();
            while (j.hasNext())
                    formatted.add(field+":"+j.next());

            return formatted;
    }
    
    /**
     * Formats the elements of the passed array. Adds the
     * passed field in front of each term.
     * @param terms                 The terms to format.
     * @param firstField    The string to add in front of the terms.
     * @param sep                   Separator used to join, exclude etc.
     * @param secondField   The string to add in front of the terms.
     * @return See above.
     */
    private List<String> formatText(List<String> terms, String firstField,
                                                            String sep, String secondField)
    {
            if (CollectionUtils.isEmpty(terms)) return null;
            List<String> formatted = new ArrayList<String>(terms.size());
            String value;
            Iterator<String> j = terms.iterator();
            String v;
            while (j.hasNext()) {
                    v = j.next();
                    value = firstField+":"+v+" "+sep+" "+secondField+":"+v;
                    formatted.add(value);
            }
            return formatted;
    }
}
