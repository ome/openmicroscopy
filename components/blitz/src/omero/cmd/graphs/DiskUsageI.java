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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
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
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.parameters.Parameters;
import ome.system.Login;
import omero.cmd.DiskUsage;
import omero.cmd.DiskUsageResponse;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * Calculate the disk usage entailed by the given objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1
 */
@SuppressWarnings("serial")
public class DiskUsageI extends DiskUsage implements IRequest {
    /* TODO: This class can be substantially refactored and simplified once the graph traversal reimplementation is merged. */

    /* FIELDS AND CONSTRUCTORS */

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUsageI.class);
    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    /* <FromClass, Map.Entry<ToClass, HQL>> */
    private static final ImmutableMultimap<String, Map.Entry<String, String>> TRAVERSAL_QUERIES;

    private static final ImmutableSet<String> OWNED_OBJECTS;
    private static final ImmutableSet<String> ANNOTATABLE_OBJECTS;

    private final PixelsService pixelsService;
    private final ThumbnailService thumbnailService;

    private Helper helper;

    /**
     * Construct a disk usage request.
     * @param pixelsService the pixels service
     * @param thumbnailService the thumbnail service
     */
    public DiskUsageI(PixelsService pixelsService, ThumbnailService thumbnailService) {
        this.pixelsService = pixelsService;
        this.thumbnailService = thumbnailService;
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
        builder.put("Roi", Maps.immutableEntry("OriginalFile",
                "SELECT source.id FROM Roi WHERE id IN (:ids)"));

        TRAVERSAL_QUERIES = builder.build();
    }

    static {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builder.add("Annotation");
        builder.add("Channel");
        builder.add("Dataset");
        builder.add("Fileset");
        builder.add("Image");
        builder.add("OriginalFile");
        builder.add("Pixels");
        builder.add("PlaneInfo");
        builder.add("PlateAcquisition");
        builder.add("Plate");
        builder.add("Project");
        builder.add("Reagent");
        builder.add("Roi");
        builder.add("Screen");
        builder.add("Well");
        builder.add("WellSample");

        OWNED_OBJECTS = builder.build();
    }

    static {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builder.add("Annotation");
        builder.add("Channel");
        builder.add("Dataset");
        builder.add("Experimenter");
        builder.add("ExperimenterGroup");
        builder.add("Fileset");
        builder.add("Image");
        builder.add("OriginalFile");
        builder.add("Pixels");
        builder.add("PlaneInfo");
        builder.add("PlateAcquisition");
        builder.add("Plate");
        builder.add("Project");
        builder.add("Reagent");
        builder.add("Roi");
        builder.add("Screen");
        builder.add("Well");
        builder.add("WellSample");

        ANNOTATABLE_OBJECTS = builder.build();
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
    public Long step(int step) throws Cancel {
        helper.assertStep(step);
        if (step != 0) {
            throw helper.cancel(new ERR(), new IllegalArgumentException(), "disk usage operation has no step " + step);
        }
        try {
            return getDiskUsage();
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
            final DiskUsageResponse response = new DiskUsageResponse((Long) object);
            helper.setResponseIfNull(response);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /* DISK USAGE CALCULATION */

    /**
     * Get the size of the file at the given path, or {@code 0} if it does not exist.
     * @param path a file path
     * @return the file's size, or {@code 0} if the file does not exist
     */
    private long getFileSize(String path) {
        final File file = new File(path);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Calculate the disk usage of the model objects specified in the request.
     * @return the total usage, in bytes
     */
    private long getDiskUsage() {
        final IQuery queryService = helper.getServiceFactory().getQueryService();

        final int batchSize = 256;

        final SetMultimap<String, Long> objectsToProcess = HashMultimap.create();
        final SetMultimap<String, Long> objectsProcessed = HashMultimap.create();
        long size = 0;

        /* note the objects to process */

        for (final Map.Entry<String, long[]> objectList : objects.entrySet()) {
            objectsToProcess.putAll(objectList.getKey(), Arrays.asList(ArrayUtils.toObject(objectList.getValue())));

            if (LOGGER.isDebugEnabled()) {
                final List<Long> ids = Lists.newArrayList(objectsToProcess.get(objectList.getKey()));
                Collections.sort(ids);
                LOGGER.debug("size calculator to process " + objectList.getKey() + " " + Joiner.on(", ").join(ids));
            }
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
                for (final Long id : idsToQuery) {
                    final String pixelsPath = pixelsService.getPixelsPath(id);
                    size += getFileSize(pixelsPath);
                    size += getFileSize(pixelsPath + PixelsService.PYRAMID_SUFFIX);
                }
            } else if ("Thumbnail".equals(className)) {
                /* Thumbnails may have /OMERO/Thumbnails/<id> files */
                for (final Long id : idsToQuery) {
                    final String thumbnailPath = thumbnailService.getThumbnailPath(id);
                    size += getFileSize(thumbnailPath);
                }
            } else if ("OriginalFile".equals(className)) {
                /* OriginalFiles have their size noted */
                final String hql = "SELECT size FROM OriginalFile WHERE id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    if (resultRow != null && resultRow[0] instanceof Long) {
                        size += (Long) resultRow[0];
                    }
                }
            } else if ("Experimenter".equals(className)) {
                /* for an experimenter, use the list of owned objects */
                for (final String resultClassName : OWNED_OBJECTS) {
                    final String hql = "SELECT id FROM " + resultClassName + " WHERE details.owner.id IN (:ids)";
                    for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                        objectsToProcess.put(resultClassName, (Long) resultRow[0]);
                    }
                }
            } else if ("ExperimenterGroup".equals(className)) {
                /* for an experimenter group, use the list of owned objects */
                for (final String resultClassName : OWNED_OBJECTS) {
                    final String hql = "SELECT id FROM " + resultClassName + " WHERE details.group.id IN (:ids)";
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
                        objectsToProcess.put(resultClassName, (Long) resultRow[0]);
                    }
                }
            }
            /* also watch for annotations on the current objects */
            if (includeAnnotations && ANNOTATABLE_OBJECTS.contains(className)) {
                final String hql = "SELECT child.id FROM " + className + "AnnotationLink WHERE parent.id IN (:ids)";
                for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                    objectsToProcess.put("Annotation", (Long) resultRow[0]);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                Collections.sort(idsToQuery);
                LOGGER.debug("size is " + size + " bytes after processing " + className + " " + Joiner.on(", ").join(idsToQuery));
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("final size is " + size + " bytes");
        }

        return size;
    }
}
