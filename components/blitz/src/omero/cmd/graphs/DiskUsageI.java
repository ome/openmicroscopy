/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import ome.api.IQuery;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.model.IObject;
import ome.parameters.Parameters;
import ome.services.graphs.GraphPathBean;
import ome.system.Login;
import omero.api.LongPair;
import omero.cmd.DiskUsage;
import omero.cmd.DiskUsageResponse;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.model.OriginalFile;

/**
 * Calculate the disk usage entailed by the given objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
@SuppressWarnings("serial")
public class DiskUsageI extends DiskUsage implements IRequest {
    /* TODO: This class can be substantially refactored and simplified by using the graph traversal reimplementation. */

    /* FIELDS AND CONSTRUCTORS */

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUsageI.class);
    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    /* <FromClass, Map.Entry<ToClass, HQL>> */
    private static final ImmutableMultimap<String, Map.Entry<String, String>> TRAVERSAL_QUERIES;

    private static final ImmutableSet<String> OWNED_OBJECTS;
    private static final ImmutableSet<String> ANNOTATABLE_OBJECTS;

    private static final Map<String, String> classIdProperties = Collections.synchronizedMap(new HashMap<String, String>());

    private final PixelsService pixelsService;
    private final ThumbnailService thumbnailService;
    private final GraphPathBean graphPathBean;

    private Helper helper;

    /**
     * Construct a disk usage request.
     * @param pixelsService the pixels service
     * @param thumbnailService the thumbnail service
     * @param graphPathBean the graph path bean
     */
    public DiskUsageI(PixelsService pixelsService, ThumbnailService thumbnailService, GraphPathBean graphPathBean) {
        this.pixelsService = pixelsService;
        this.thumbnailService = thumbnailService;
        this.graphPathBean = graphPathBean;
    }

    /* NAVIGATION OF MODEL OBJECT GRAPH */

    static {
        final ImmutableMultimap.Builder<String, Map.Entry<String, String>> builder = ImmutableMultimap.builder();

        builder.put("Project", Maps.immutableEntry("Dataset",
                "SELECT child.id FROM ProjectDatasetLink WHERE parent.id IN (:ids)"));
        builder.put("Dataset", Maps.immutableEntry("Image",
                "SELECT child.id FROM DatasetImageLink WHERE parent.id IN (:ids)"));
        builder.put("Screen", Maps.immutableEntry("Plate",
                "SELECT child.id FROM ScreenPlateLink WHERE parent.id IN (:ids)"));
        builder.put("Plate", Maps.immutableEntry("Well",
                "SELECT id FROM Well WHERE plate.id IN (:ids)"));
        builder.put("Plate", Maps.immutableEntry("PlateAcquisition",
                "SELECT id FROM PlateAcquisition WHERE plate.id IN (:ids)"));
        builder.put("PlateAcquisition", Maps.immutableEntry("WellSample",
                "SELECT id FROM WellSample WHERE plateAcquisition.id IN (:ids)"));
        builder.put("Well", Maps.immutableEntry("WellSample",
                "SELECT id FROM WellSample WHERE well.id IN (:ids)"));
        builder.put("Well", Maps.immutableEntry("Reagent",
                "SELECT child.id FROM WellReagentLink WHERE parent.id IN (:ids)"));
        builder.put("WellSample", Maps.immutableEntry("Image",
                "SELECT image.id FROM WellSample WHERE id IN (:ids)"));
        builder.put("Image", Maps.immutableEntry("Pixels",
                "SELECT id FROM Pixels WHERE image.id IN (:ids)"));
        builder.put("Pixels", Maps.immutableEntry("Thumbnail",
                "SELECT id FROM Thumbnail WHERE pixels.id IN (:ids)"));
        builder.put("Pixels", Maps.immutableEntry("OriginalFile",
                "SELECT parent.id FROM PixelsOriginalFileMap WHERE child.id IN (:ids)"));
        builder.put("Pixels", Maps.immutableEntry("Channel",
                "SELECT id FROM Channel WHERE pixels.id IN (:ids)"));
        builder.put("Pixels", Maps.immutableEntry("PlaneInfo",
                "SELECT id FROM PlaneInfo WHERE pixels.id IN (:ids)"));
        builder.put("Channel", Maps.immutableEntry("LogicalChannel",
                "SELECT logicalChannel.id FROM Channel WHERE id IN (:ids)"));
        builder.put("Image", Maps.immutableEntry("Fileset",
                "SELECT fileset.id FROM Image WHERE id IN (:ids)"));
        builder.put("Fileset", Maps.immutableEntry("Job",
                "SELECT child.id FROM FilesetJobLink WHERE parent.id IN (:ids)"));
        builder.put("Job", Maps.immutableEntry("OriginalFile",
                "SELECT child.id FROM JobOriginalFileLink WHERE parent.id IN (:ids)"));
        builder.put("Fileset", Maps.immutableEntry("Image",
                "SELECT id FROM Image WHERE fileset.id IN (:ids)"));
        builder.put("Fileset", Maps.immutableEntry("FilesetEntry",
                "SELECT id FROM FilesetEntry WHERE fileset.id IN (:ids)"));
        builder.put("FilesetEntry", Maps.immutableEntry("OriginalFile",
                "SELECT originalFile.id FROM FilesetEntry WHERE id IN (:ids)"));
        builder.put("Annotation", Maps.immutableEntry("OriginalFile",
                "SELECT file.id FROM FileAnnotation WHERE id IN (:ids)"));
        builder.put("Image", Maps.immutableEntry("Roi",
                "SELECT id FROM Roi WHERE image.id IN (:ids)"));
        builder.put("Roi", Maps.immutableEntry("Shape",
                "SELECT id FROM Shape WHERE roi.id IN (:ids)"));
        builder.put("Roi", Maps.immutableEntry("OriginalFile",
                "SELECT source.id FROM Roi WHERE id IN (:ids)"));
        builder.put("Image", Maps.immutableEntry("Instrument",
                "SELECT instrument.id FROM Image WHERE id IN (:ids)"));
        builder.put("Instrument", Maps.immutableEntry("Detector",
                "SELECT id FROM Detector WHERE instrument.id IN (:ids)"));
        builder.put("Instrument", Maps.immutableEntry("Dichroic",
                "SELECT id FROM Dichroic WHERE instrument.id IN (:ids)"));
        builder.put("Instrument", Maps.immutableEntry("Filter",
                "SELECT id FROM Filter WHERE instrument.id IN (:ids)"));
        builder.put("Instrument", Maps.immutableEntry("LightSource",
                "SELECT id FROM LightSource WHERE instrument.id IN (:ids)"));
        builder.put("Instrument", Maps.immutableEntry("Objective",
                "SELECT id FROM Objective WHERE instrument.id IN (:ids)"));
        builder.put("Dichroic", Maps.immutableEntry("LightPath",
                "SELECT id FROM LightPath WHERE dichroic.id IN (:ids)"));
        builder.put("LogicalChannel", Maps.immutableEntry("LightPath",
                "SELECT lightPath.id FROM LogicalChannel WHERE id IN (:ids)"));

        TRAVERSAL_QUERIES = builder.build();
    }

    static {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builder.add("Annotation");
        builder.add("Channel");
        builder.add("Dataset");
        builder.add("Detector");
        builder.add("Dichroic");
        builder.add("Fileset");
        builder.add("Filter");
        builder.add("Image");
        builder.add("LogicalChannel");
        builder.add("Instrument");
        builder.add("LightPath");
        builder.add("LightSource");
        builder.add("Objective");
        builder.add("OriginalFile");
        builder.add("Pixels");
        builder.add("PlaneInfo");
        builder.add("PlateAcquisition");
        builder.add("Plate");
        builder.add("Project");
        builder.add("Reagent");
        builder.add("Roi");
        builder.add("Screen");
        builder.add("Shape");
        builder.add("Well");
        builder.add("WellSample");

        OWNED_OBJECTS = builder.build();
    }

    static {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builder.add("Annotation");
        builder.add("Channel");
        builder.add("Dataset");
        builder.add("Detector");
        builder.add("Dichroic");
        builder.add("Experimenter");
        builder.add("ExperimenterGroup");
        builder.add("Fileset");
        builder.add("Filter");
        builder.add("Image");
        builder.add("Instrument");
        builder.add("LightPath");
        builder.add("LightSource");
        builder.add("Objective");
        builder.add("OriginalFile");
        builder.add("PlaneInfo");
        builder.add("PlateAcquisition");
        builder.add("Plate");
        builder.add("Project");
        builder.add("Reagent");
        builder.add("Roi");
        builder.add("Screen");
        builder.add("Shape");
        builder.add("Well");

        ANNOTATABLE_OBJECTS = builder.build();
    }

    /* USAGE STATISTICS TRACKING */

    /**
     * Track the disk usage subtotals and totals. Not thread-safe.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class Usage {
        private final Map<LongPair, Map<String, Integer>> countByTypeByWho = new HashMap<LongPair, Map<String, Integer>>();
        private final Map<LongPair, Map<String, Long>> sizeByTypeByWho = new HashMap<LongPair, Map<String, Long>>();

        private final Map<LongPair, Integer> totalCountByWho = new HashMap<LongPair, Integer>();
        private final Map<LongPair, Long> totalSizeByWho = new HashMap<LongPair, Long>();

        private boolean bumpTotals = false;

        /**
         * The next call to {@link #add(String, Long)} may bump {@link #totalCount} and {@link #totalSize}.
         * @return this instance, for method chaining
         */
        Usage bumpTotals() {
            bumpTotals = true;
            return this;
        }

        /**
         * Adjust counts and sizes according to given ownership, type and size.
         * Does not adjust anything unless {@code size > 0}.
         * @see #bumpTotals()
         * @param owner the ID of an owner
         * @param group the ID of a group
         * @param type a type
         * @param size a size
         */
        void add(long owner, long group, String type, Long size) {
            if (size <= 0) {
                bumpTotals = false;
                return;
            }
            final LongPair ownership = new LongPair(owner, group);
            final Map<String, Integer> countByType;
            final Map<String, Long> sizeByType;
            if (countByTypeByWho.containsKey(ownership)) {
                countByType = countByTypeByWho.get(ownership);
                sizeByType = sizeByTypeByWho.get(ownership);
            } else {
                countByType = new HashMap<String, Integer>();
                sizeByType = new HashMap<String, Long>();
                countByTypeByWho.put(ownership, countByType);
                sizeByTypeByWho.put(ownership, sizeByType);
            }
            Long sizeThisType = sizeByType.get(type);
            if (sizeThisType == null) {
                countByType.put(type, Integer.valueOf(1));
                sizeByType.put(type, size);
            } else {
                countByType.put(type, countByType.get(type) + 1);
                sizeByType.put(type, sizeThisType + size);
            }
            if (bumpTotals) {
                Integer totalCount = totalCountByWho.get(ownership);
                Long totalSize = totalSizeByWho.get(ownership);
                if (totalCount == null) {
                    totalCount = 0;
                }
                if (totalSize == null) {
                    totalSize = 0L;
                }
                totalCount++;
                totalSize += size;
                totalCountByWho.put(ownership, totalCount);
                totalSizeByWho.put(ownership, totalSize);
                bumpTotals = false;
            }
        }

        /**
         * @return a disk usage response corresponding to the current usage
         */
        public DiskUsageResponse getDiskUsageResponse() {
            return new DiskUsageResponse(countByTypeByWho, sizeByTypeByWho, totalCountByWho, totalSizeByWho);
        }

        /**
         * Convert a map into a concise string representation.
         * @param byWho a map with owner, group keys
         * @return the string representation
         */
        private String toString(Map<LongPair, ?> byWho) {
            final List<String> asStrings = new ArrayList<String>(byWho.size());
            final StringBuffer sb = new StringBuffer();
            for (final Map.Entry<LongPair, ?> entry : byWho.entrySet()) {
                sb.setLength(0);
                sb.append(entry.getKey().first);
                sb.append('/');
                sb.append(entry.getKey().second);
                sb.append('=');
                sb.append(entry.getValue());
                asStrings.add(sb.toString());
            }
            return Joiner.on(", ").join(asStrings);
        }

        @Override
        public String toString() {
            return "files = [" + toString(totalCountByWho) + "], bytes = [" + toString(totalSizeByWho) + "]";
        }
    }

    /* CMD REQUEST FRAMEWORK */

    @Override
    public ImmutableMap<String, String> getCallContext() {
       return ALL_GROUPS_CONTEXT;
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(1);
    }

    @Override
    public DiskUsageResponse step(int step) throws Cancel {
        helper.assertStep(step);
        if (step != 0) {
            throw helper.cancel(new ERR(), new IllegalArgumentException(), "disk usage operation has no step " + step);
        }
        try {
            return getDiskUsage();
        } catch (Cancel c) {
            throw c;
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "disk usage operation failed");
        }
    }

    @Override
    public void finish() {
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            helper.setResponseIfNull((DiskUsageResponse) object);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /* DISK USAGE CALCULATION */

    /**
     * Notes the ownership and disk usage of an original file. Immutable.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class OwnershipAndSize {
        /** the ID of the owner of the file */
        public final long owner;

        /** the ID of the group of the file */
        public final long group;

        /** the size of the file */
        public final long size;

        /**
         * Construct a tuple of a file's ownership and disk usage.
         * @param owner the ID of the owner of the file
         * @param group the ID of the group of the file
         * @param size the size of the file
         */
        OwnershipAndSize(long owner, long group, long size) {
            this.owner = owner;
            this.group = group;
            this.size = size;
        }
    }

    /**
     * Get the size of the file at the given path, or {@code 0} if it does not exist.
     * @param path a file path
     * @return the file's size, or {@code 0} if the file does not exist
     */
    private static long getFileSize(String path) {
        final File file = new File(path);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Look up the identifier property for the given class.
     * @param className a class name
     * @return the identifier property, never {@code null}
     * @throws Cancel if an identifier property could not be found for the given class
     */
    private String getIdPropertyFor(String className) throws Cancel {
        String idProperty = classIdProperties.get(className);
        if (idProperty == null) {
            final Class<? extends IObject> actualClass = graphPathBean.getClassForSimpleName(className);
            if (actualClass == null) {
                final Exception e = new IllegalArgumentException("class " + className + " is unknown");
                throw helper.cancel(new ERR(), e, "bad-class");
            }
            idProperty = graphPathBean.getIdentifierProperty(actualClass.getName());
            if (idProperty == null) {
                final Exception e = new IllegalArgumentException("no identifier property is known for class " + className);
                throw helper.cancel(new ERR(), e, "bad-class");
            }
            classIdProperties.put(className, idProperty);
        }
        return idProperty;
    }

    /**
     * Calculate the disk usage of the model objects specified in the request.
     * @return the total usage, in bytes
     */
    private DiskUsageResponse getDiskUsage() {
        final IQuery queryService = helper.getServiceFactory().getQueryService();

        final int batchSize = 256;

        final SetMultimap<String, Long> objectsToProcess = HashMultimap.create();
        final SetMultimap<String, Long> objectsProcessed = HashMultimap.create();
        final Usage usage = new Usage();

        /* original file ID to types that refer to them */
        final SetMultimap<Long, String> typesWithFiles = HashMultimap.create();
        /* original file ID to file ownership and size */
        final Map<Long, OwnershipAndSize> fileSizes = new HashMap<Long, OwnershipAndSize>();

        /* note the objects to process */

        for (final String className : classes) {
            final String hql = "SELECT " + getIdPropertyFor(className) + " FROM " + className;
            for (final Object[] resultRow : queryService.projection(hql, null)) {
                if (resultRow != null) {
                    final Long objectId = (Long) resultRow[0];
                    objectsToProcess.put(className, objectId);
                }
            }
        }

        for (final Map.Entry<String, List<Long>> objectList : objects.entrySet()) {
            objectsToProcess.putAll(objectList.getKey(), objectList.getValue());

            if (LOGGER.isDebugEnabled()) {
                final List<Long> ids = Lists.newArrayList(objectsToProcess.get(objectList.getKey()));
                Collections.sort(ids);
                LOGGER.debug("size calculator to process " + objectList.getKey() + " " + Joiner.on(", ").join(ids));
            }
        }

        /* check that the objects' class names are valid */

        for (final String className : objectsToProcess.keySet()) {
            getIdPropertyFor(className);
        }

        /* iteratively process objects, descending the model graph */

        while (!objectsToProcess.isEmpty()) {
            /* obtain canonical class name and ID list */
            final Map.Entry<String, Collection<Long>> nextClass = objectsToProcess.asMap().entrySet().iterator().next();
            String className = nextClass.getKey();
            final int lastDot = className.lastIndexOf('.');
            if (lastDot >= 0) {
                className = className.substring(lastDot + 1);
            } else if (className.charAt(0) == '/') {
                className = className.substring(1);
            }
            /* get IDs still to process, and split off a batch of them for this query */
            final Collection<Long> ids = nextClass.getValue();
            ids.removeAll(objectsProcessed.get(className));
            if (ids.isEmpty()) {
                continue;
            }
            final List<Long> idsToQuery = Lists.newArrayList(Iterables.limit(ids, batchSize));
            ids.removeAll(idsToQuery);
            objectsProcessed.putAll(className, idsToQuery);
            final Parameters parameters = new Parameters().addIds(idsToQuery);

            if ("Pixels".equals(className)) {
                /* Pixels may have /OMERO/Pixels/<id> files */
                final String hql = "SELECT id, details.owner.id, details.group.id FROM Pixels WHERE id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    if (resultRow != null) {
                        final Long pixelsId = (Long) resultRow[0];
                        final Long ownerId = (Long) resultRow[1];
                        final Long groupId = (Long) resultRow[2];
                        final String pixelsPath = pixelsService.getPixelsPath(pixelsId);
                        usage.bumpTotals().add(ownerId, groupId, className, getFileSize(pixelsPath));
                        usage.bumpTotals().add(ownerId, groupId, className, getFileSize(pixelsPath + PixelsService.PYRAMID_SUFFIX));
                        usage.bumpTotals().add(ownerId, groupId, className, getFileSize(pixelsPath + PixelsService.PYRAMID_SUFFIX +
                                BfPyramidPixelBuffer.PYR_LOCK_EXT));
                    }
                }
            } else if ("Thumbnail".equals(className)) {
                /* Thumbnails may have /OMERO/Thumbnails/<id> files */
                final String hql = "SELECT id, details.owner.id, details.group.id FROM Thumbnail WHERE id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    if (resultRow != null) {
                        final Long thumbnailId = (Long) resultRow[0];
                        final Long ownerId = (Long) resultRow[1];
                        final Long groupId = (Long) resultRow[2];
                        final String thumbnailPath = thumbnailService.getThumbnailPath(thumbnailId);
                        usage.bumpTotals().add(ownerId, groupId, className, getFileSize(thumbnailPath));
                    }
                }
            } else if ("OriginalFile".equals(className)) {
                /* OriginalFiles have their size noted */
                final String hql = "SELECT id, details.owner.id, details.group.id, size FROM OriginalFile WHERE id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    if (resultRow != null && resultRow[3] instanceof Long) {
                        final Long fileId = (Long) resultRow[0];
                        final Long ownerId = (Long) resultRow[1];
                        final Long groupId = (Long) resultRow[2];
                        final Long fileSize = (Long) resultRow[3];
                        fileSizes.put(fileId, new OwnershipAndSize(ownerId, groupId, fileSize));
                    }
                }
            } else if ("Experimenter".equals(className)) {
                /* for an experimenter, use the list of owned objects */
                for (final String resultClassName : OWNED_OBJECTS) {
                    final String hql = "SELECT " + getIdPropertyFor(resultClassName) + " FROM " + resultClassName +
                            " WHERE details.owner.id IN (:ids)";
                    for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                        objectsToProcess.put(resultClassName, (Long) resultRow[0]);
                    }
                }
            } else if ("ExperimenterGroup".equals(className)) {
                /* for an experimenter group, use the list of owned objects */
                for (final String resultClassName : OWNED_OBJECTS) {
                    final String hql = "SELECT " + getIdPropertyFor(resultClassName) + " FROM " + resultClassName +
                            " WHERE details.group.id IN (:ids)";
                    for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                        objectsToProcess.put(resultClassName, (Long) resultRow[0]);
                    }
                }
            }

            /* follow the next step from here on the model object graph */
            for (final Map.Entry<String, String> query : TRAVERSAL_QUERIES.get(className)) {
                final String resultClassName = query.getKey();
                final String hql = query.getValue();
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    if (resultRow != null && resultRow[0] instanceof Long) {
                        final Long resultId = (Long) resultRow[0];
                        objectsToProcess.put(resultClassName, resultId);
                        if ("OriginalFile".equals(resultClassName)) {
                            typesWithFiles.put(resultId, className);
                        }
                    }
                }
            }
            if (ANNOTATABLE_OBJECTS.contains(className)) {
                /* also watch for annotations on the current objects */
                final String hql = "SELECT child.id FROM " + className + "AnnotationLink WHERE parent.id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    objectsToProcess.put("Annotation", (Long) resultRow[0]);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                Collections.sort(idsToQuery);
                LOGGER.debug("usage is " + usage + " after processing " + className + " " + Joiner.on(", ").join(idsToQuery));
            }
        }

        /* collate file counts and sizes by referer type */
        for (final Map.Entry<Long, OwnershipAndSize> fileIdSize : fileSizes.entrySet()) {
            final Long fileId = fileIdSize.getKey();
            final OwnershipAndSize fileSize = fileIdSize.getValue();
            Set<String> types = typesWithFiles.get(fileId);
            if (types.isEmpty()) {
                types = ImmutableSet.of("OriginalFile");
            }
            usage.bumpTotals();
            for (final String type : types) {
                usage.add(fileSize.owner, fileSize.group, type, fileSize.size);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("usage is " + usage + " after including " + OriginalFile.class.getSimpleName() + " sizes");
        }

        return usage.getDiskUsageResponse();
    }
}
