/*
 * Copyright (C) 2014-2016 University of Dundee & Open Microscopy Environment.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.api.IQuery;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.model.IObject;
import ome.parameters.Parameters;
import ome.security.ACLVoter;
import ome.security.basic.LightAdminPrivileges;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.services.util.ReadOnlyStatus;
import ome.system.Login;
import ome.system.Roles;
import omero.api.LongPair;
import omero.cmd.DiskUsage2;
import omero.cmd.DiskUsage2Response;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * Calculate the disk usage entailed by the given objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
@SuppressWarnings("serial")
public class DiskUsage2I extends DiskUsage2 implements IRequest, ReadOnlyStatus.IsAware {

    /* FIELDS AND CONSTRUCTORS */

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUsage2I.class);

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private static final Set<GraphPolicy.Ability> REQUIRED_ABILITIES = ImmutableSet.of();

    private final ACLVoter aclVoter;
    private final GraphPathBean graphPathBean;
    private final Set<Class<? extends IObject>> legalClasses;
    private final GraphPolicy graphPolicy;
    private PixelsService pixelsService;
    private ThumbnailService thumbnailService;

    private Helper helper;
    private GraphHelper graphHelper;
    private GraphTraversal graphTraversal;

    private SetMultimap<String, Long> targetMultimap = null;
    private GraphTraversal.PlanExecutor processor;

    /* keep track of disk usage totals */
    private final Usage usage = new Usage();
    /* original file ID to types that refer to them */
    final SetMultimap<Long, String> typesWithFiles = HashMultimap.create();
    /* original file ID to file ownership and size */
    final Map<Long, OwnershipAndSize> fileSizes = new HashMap<Long, OwnershipAndSize>();

    /**
     * Construct a new disk usage request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param graphPathBean the graph path bean to use
     * @param adminPrivileges the light administrator privileges helper
     * @param targetClasses legal target object classes for the search
     * @param graphPolicy the graph policy to apply for the search
     */
    public DiskUsage2I(ACLVoter aclVoter, Roles securityRoles, GraphPathBean graphPathBean, LightAdminPrivileges adminPrivileges,
            Set<Class<? extends IObject>> targetClasses, GraphPolicy graphPolicy) {
        this.aclVoter = aclVoter;
        this.graphPathBean = graphPathBean;
        this.legalClasses = targetClasses;
        this.graphPolicy = graphPolicy;
    }

    /**
     * Provided by {@link omero.cmd.RequestObjectFactoryRegistry}.
     * @param pixelsService the pixels service
     */
    public void setPixelsService(PixelsService pixelsService) {
        this.pixelsService = pixelsService;
    }

    /**
     * Provided by {@link omero.cmd.RequestObjectFactoryRegistry}.
     * @param thumbnailService the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    /* CMD REQUEST FRAMEWORK */

    @Override
    public Map<String, String> getCallContext() {
       return new HashMap<String, String>(ALL_GROUPS_CONTEXT);
    }

    @Override
    public void init(Helper helper) {
        if (LOGGER.isDebugEnabled()) {
            final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
            arguments.addParameter("targetClasses", targetClasses);
            arguments.addParameter("targetObjects", targetObjects);
            LOGGER.debug("request: " + arguments);
        }

        this.helper = helper;
        helper.setSteps(5);
        this.graphHelper = new GraphHelper(helper, graphPathBean);

        graphTraversal = new GraphTraversal(helper.getSession(), helper.getEventContext(), aclVoter, graphPathBean, null,
                graphPolicy, new InternalProcessor());
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            switch (step) {
            case 0:
                if (targetObjects != null) {
                    targetMultimap = graphHelper.getTargetMultimap(legalClasses, targetObjects);
                } else {
                    targetMultimap = HashMultimap.create();
                }
                if (targetClasses != null) {
                    final IQuery queryService = helper.getServiceFactory().getQueryService();
                    for (final String className : graphHelper.getTargetSet(legalClasses, targetClasses)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("fetching IDs for class " + className);
                        }
                        final String hql = "SELECT id FROM " + className;
                        for (final Object[] resultRow : queryService.projection(hql, null)) {
                            if (resultRow != null) {
                                final Long objectId = (Long) resultRow[0];
                                targetMultimap.put(className, objectId);
                            }
                        }
                    }
                }
                return null;
            case 1:
                final Map.Entry<SetMultimap<String, Long>, SetMultimap<String, Long>> plan =
                        graphTraversal.planOperation(targetMultimap, true, true);
                targetMultimap.clear();
                if (plan.getValue().isEmpty()) {
                    graphTraversal.assertNoUnlinking();
                } else {
                    final Exception e = new IllegalStateException("querying the model graph does not delete any objects");
                    helper.cancel(new ERR(), e, "graph-fail");
                }
                return null;
            case 2:
                processor = graphTraversal.processTargets();
                return null;
            case 3:
                processor.execute();
                return null;
            case 4:
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
                return null;
            default:
                final Exception e = new IllegalArgumentException("model object graph operation has no step " + step);
                throw helper.cancel(new ERR(), e, "bad-step");
            }
        } catch (Cancel c) {
            throw c;
        } catch (GraphException ge) {
            final omero.cmd.GraphException graphERR = new omero.cmd.GraphException();
            graphERR.message = ge.message;
            throw helper.cancel(graphERR, ge, "graph-fail");
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "graph-fail");
        }
    }

    @Override
    public void finish() {
        helper.setResponseIfNull(usage.getDiskUsageResponse());
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
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
        public DiskUsage2Response getDiskUsageResponse() {
            return new DiskUsage2Response(countByTypeByWho, sizeByTypeByWho, totalCountByWho, totalSizeByWho);
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
     * A processor that notes disk usage and how to attribute it.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.3.0
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        /* which classes and properties "contain" original files */
        private final Map<String, String> fileProperties = ImmutableMap.<String, String>of(
                "FileAnnotation", "file", "FilesetEntry", "originalFile", "JobOriginalFileLink", "child",
                "PixelsOriginalFileMap", "parent", "Roi", "source");

        /* for the containers of original files, to which class the usage should be attributed */
        private final Map<String, String> attributions = ImmutableMap.<String, String>of(
                "JobOriginalFileLink", "Job", "PixelsOriginalFileMap", "Pixels");

        /* the query service */
        private final IQuery queryService = helper.getServiceFactory().getQueryService();

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) throws GraphException {
            className = className.substring(className.lastIndexOf('.') + 1);
            final Parameters parameters = new Parameters().addIds(ids);

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
            } else {
                /* may contain an original file, if so then note which */
                final String property = fileProperties.get(className);
                if (property != null) {
                    String attribution = attributions.get(className);
                    if (attribution == null) {
                        attribution = className;
                    }
                    final String hql = "SELECT " + property + ".id FROM " + className + " WHERE id IN (:ids)";
                    for (final Object[] resultRow : queryService.projection(hql, parameters)) {
                        if (resultRow != null && resultRow[0] instanceof Long) {
                            final Long fileId = (Long) resultRow[0];
                            typesWithFiles.put(fileId, attribution);
                        }
                    }
                }
            }
        }

        @Override
        public Set<GraphPolicy.Ability> getRequiredPermissions() {
            return REQUIRED_ABILITIES;
        }
    }

    @Override
    public boolean isReadOnly(ReadOnlyStatus readOnly) {
        return true;
    }
}
