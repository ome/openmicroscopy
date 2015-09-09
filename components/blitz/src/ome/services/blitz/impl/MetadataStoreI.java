/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ome.api.IQuery;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.formats.OMEROMetadataStore;
import ome.io.nio.OriginalFilesService;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.enums.Format;
import ome.model.screen.Plate;
import ome.parameters.Parameters;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.roi.PopulateRoiJob;
import ome.services.throttling.Adapter;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;
import ome.util.SqlAction;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RMap;
import omero.RString;
import omero.ServerError;
import omero.api.AMD_MetadataStore_createRoot;
import omero.api.AMD_MetadataStore_populateMinMax;
import omero.api.AMD_MetadataStore_saveToDB;
import omero.api.AMD_MetadataStore_setPixelsFile;
import omero.api.AMD_MetadataStore_updateObjects;
import omero.api.AMD_MetadataStore_updateReferences;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api._MetadataStoreOperations;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.SharedResourcesPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.FilesetJobLink;
import omero.model.ScriptJob;
import omero.util.IceMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

public class MetadataStoreI extends AbstractCloseableAmdServant implements
        _MetadataStoreOperations, ServiceFactoryAware, BlitzOnly {

    private final static Logger log = LoggerFactory.getLogger(MetadataStoreI.class);

    protected final Set<Long> savedPlates = new HashSet<Long>();

    protected final Set<Long> savedImagesNotInPlates = new HashSet<Long>();

    protected OMEROMetadataStore store;

    protected ServiceFactoryI sf;
    
    protected PopulateRoiJob popRoi;

    protected final SqlAction sql;

    protected final OriginalFilesService filesService;

    protected final String omeroDataDir;

    public MetadataStoreI(final BlitzExecutor be, PopulateRoiJob popRoi,
            SqlAction sql, OriginalFilesService filesService,
            String omeroDataDir) throws Exception {
        super(null, be);
        this.popRoi = popRoi;
        this.sql = sql;
        this.filesService = filesService;
        this.omeroDataDir =
            new File(omeroDataDir).getAbsolutePath() + File.separator;
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.sf = sf;
    }

    @Override
    public void onSetOmeroContext(final OmeroContext ctx) throws Exception {
        ServiceFactory sf = new InternalServiceFactory(ctx);
        this.store = new OMEROMetadataStore(sf, sql);
    }

    @SuppressWarnings("unchecked")
    private <T extends IObject> T safeReverse(Object o, IceMapper mapper) {
        try {
            return (T) mapper.reverse(o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to safely reverse: " + o);
        }
    }

    /**
     * Called during
     * {@link #saveToDB_async(AMD_MetadataStore_saveToDB, Current)} to prepare
     * the list of pixels for post-processing.
     * 
     * @see #processing()
     */
    private void parsePixels(List<Pixels> pixels, Map<String, List<? extends IObject>> rv, IQuery query) {
        synchronized (savedPlates) {
            for (Pixels p : pixels) {
                ome.model.core.Image i = p.getImage();
                if (i != null) {
                    if (i.sizeOfWellSamples() < 1) {
                        savedImagesNotInPlates.add(i.getId());
                    } else {
                        for (ome.model.screen.WellSample ws :
                            i.unmodifiableWellSamples()) {
                            ome.model.screen.Well w = ws.getWell();
                            if (w != null) {
                                Plate plate = w.getPlate();
                                if (plate != null) {
                                    savedPlates.add(plate.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        List<IObject> plates = loadObjects("Plate", query, savedPlates);
        List<IObject> images = loadObjects("Image", query, savedImagesNotInPlates);
        rv.put("Plate", plates);
        rv.put("Image", images);
        rv.put("Pixels", pixels);
    }

    private List<IObject> loadObjects(String type, IQuery query, Collection<Long> ids) {
        if (ids != null && ids.size() > 0) {
            return query.findAllByQuery(
                "select p from " + type + " p where p.id in (:ids)",
                new Parameters().addIds(ids));
        }
        return null;
    }

    // ~ Service methods
    // =========================================================================

    public void createRoot_async(final AMD_MetadataStore_createRoot __cb,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "createRoot") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        store.createRoot();
                        return null;
                    }
                }));
    }

    public void populateMinMax_async(
            final AMD_MetadataStore_populateMinMax __cb,
            final double[][][] imageChannelGlobalMinMax, final Current __current)
            throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "populateMinMax") {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {

                        store.populateMinMax(imageChannelGlobalMinMax);
                        return null;
                    }
                }));
    }

    public void saveToDB_async(final AMD_MetadataStore_saveToDB __cb,
            final FilesetJobLink link, final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.PRIMITIVE_FILTERABLE_COLLECTION_MAP);
        final ome.model.fs.FilesetJobLink link_ =
                (ome.model.fs.FilesetJobLink) mapper.reverse(link);

        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "saveToDb") {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        Map<String, List<? extends IObject>> rv = new HashMap<String, List<? extends IObject>>();
                        List<Pixels> pix = store.saveToDB(link_);
                        rv.put("Pixels", pix);
                        parsePixels(pix, rv, sf.getQueryService());
                        return rv;
                    }
                }));
    }

    public void updateObjects_async(AMD_MetadataStore_updateObjects __cb,
            final IObjectContainer[] objects, Current __current)
            throws ServerError {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "updateObjects") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        for (IObjectContainer o : objects) {
                            IObject sourceObject;
                            try {
                                sourceObject = (IObject) mapper
                                        .reverse(o.sourceObject);
                            } catch (Exception e) {
                                // TODO: This is **WRONG**; exception handling
                                // here is messed up.
                                throw new RuntimeException(e);
                            }
                            store.updateObject(o.LSID, sourceObject, o.indexes);
                        }
                        return null;
                    }
                }));
    }

    public void updateReferences_async(AMD_MetadataStore_updateReferences __cb,
            final Map<String, String[]> references, Current __current)
            throws ServerError {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "updateReferences") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        store.updateReferences(references);
                        return null;
                    }
                }));
    }

    /**
     * Called after some number of Passes the {@link #savedPlates} to a
     * background processor for further work. This happens on
     * {@link #close_async(AMD_StatefulServiceInterface_close, Current)} since
     * no further pixels can be created, but also on
     * {@link #createRoot_async(AMD_MetadataStore_createRoot, Current)} which is
     * used by the client to reset the status of this instance. To prevent any
     * possible
     */
    public void postProcess_async(omero.api.AMD_MetadataStore_postProcess __cb, Current __current)
    throws ServerError {
        
        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        final List<Long> copy = new ArrayList<Long>();
        
        final List<InteractiveProcessorPrx> procs = new ArrayList<InteractiveProcessorPrx>();
        
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "postProcess") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory _sf) {

                        synchronized (savedPlates) {

                            copy.addAll(savedPlates);
                            if (copy.size() == 0) {
                                return null;
                            }
                            for (Long id : copy) {
                                
                                RMap inputs = omero.rtypes.rmap("Plate_ID",
                                        omero.rtypes.rlong(id));

                                ScriptJob job = popRoi.createJob(_sf);
                                InteractiveProcessorPrx prx;
                                try {
                                    SharedResourcesPrx sr = sf.sharedResources(null);
                                    prx = sr.acquireProcessor(job, 15);
                                    prx.execute(inputs);
                                    prx.setDetach(true);
                                    procs.add(prx);
                                    log.info("Launched populateroi.py on plate " + id);
                                } catch (ServerError e) {
                                    String msg = "Error acquiring post processor";
                                    log.error(msg, e);
                                    throw new InternalException(msg);
                                }
                            }
                            savedPlates.clear();
                            return procs;
                        }
                    }
                }));
    }

    /* (non-Javadoc)
     * @see omero.api._MetadataStoreOperations#setPixelsFile_async(omero.api.AMD_MetadataStore_setPixelsFile, long, String, String, Ice.Current)
     */
    public void setPixelsFile_async(
            AMD_MetadataStore_setPixelsFile __cb,
            final long pixelsId, final String file, final String repo,
            Current __current)
        throws ServerError
    {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);

        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "setPixelsParams")
        {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf)
            {
                final File targetFile;
                if (file != null) {
                    targetFile = new File(file);
                } else {
                    Pixels pixels = sf.getQueryService().get(
                            Pixels.class, pixelsId);
                    Format format = pixels.getImage().getFormat();
                    List<OriginalFile> files = pixels.linkedOriginalFileList();
                    if (files == null || files.size() == 0)
                    {
                        throw new ResourceError(String.format(
                                "Pixels:%d has no linked original files!",
                                pixelsId));
                    }
                    OriginalFile source = null;
                    for (OriginalFile file : files)
                    {
                        if (file.getMimetype().equals(format.getValue()))
                        {
                            if (source != null)
                            {
                                throw new ResourceError(String.format(
                                        "Pixels:%d has at least two source " +
                                        "original files %d and %d", pixelsId,
                                        source.getId(), file.getId()));
                            }
                            source = file;
                        }
                    }
                    targetFile = new File(filesService.getFilesPath(source.getId()));
                }

                // We need to perform a case insensitive replacement due to the
                // posibilitity that we're running on a case insensitive
                // filesystem like NTFS. (See #5654)
                Pattern p = Pattern.compile(
                        Pattern.quote(omeroDataDir), Pattern.CASE_INSENSITIVE);
                String parent = targetFile.getParent();
                if (log.isDebugEnabled())
                {
                    log.debug(String.format(
                            "omero.data.dir: '%s' file.absolutePath: '%s' " +
                            "parent: '%s'", omeroDataDir,
                            targetFile.getAbsolutePath(), parent));
                }
                String path = p.matcher(parent).replaceFirst("");
                sql.setPixelsNamePathRepo(pixelsId, targetFile.getName(),
                                          path, repo);
                return null;
            }
        }));
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Integer toJavaType(RInt x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Long toJavaType(RLong x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Boolean toJavaType(RBool x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Float toJavaType(RFloat x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Double toJavaType(RDouble x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public String toJavaType(RString x) {
        return x == null ? null : x.getValue();
    }

    // Stateful interface methods
    // =========================================================================

    @Override
    protected void preClose(Ice.Current current) {
        // Nulling should be sufficient.
        store = null;
    }

    @Override
    protected void postClose(Current current) {
        // no-op
    }
}
