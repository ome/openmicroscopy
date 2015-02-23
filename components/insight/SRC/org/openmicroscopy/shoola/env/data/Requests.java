/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package org.openmicroscopy.shoola.env.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.cmd.Chgrp2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.cmd.GraphModify2;
import omero.cmd.SkipHead;
import omero.cmd.graphs.ChildOption;

/**
 * A utility class of factory methods with various signatures for clients to use in generating requests for graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class Requests {

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child type to include in the request's operation
     * @param excludeType the child type to exclude from the request's operation
     * @return the new instance
     */
    public static ChildOption option(String includeType, String excludeType) {
        final List<String> includeTypeList = includeType == null ? null : Collections.singletonList(includeType);
        final List<String> excludeTypeList = excludeType == null ? null : Collections.singletonList(excludeType);
        return new ChildOption(includeTypeList, excludeTypeList, null, null);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @return the new instance
     */
    public static ChildOption option(List<String> includeType, List<String> excludeType) {
        return new ChildOption(includeType, excludeType, null, null);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child type to include in the request's operation
     * @param excludeType the child type to exclude from the request's operation
     * @param includeNs the annotation namespace to which this option applies
     * @param excludeNs the annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(String includeType, String excludeType, String includeNs, String excludeNs) {
        final List<String> includeTypeList = includeType == null ? null : Collections.singletonList(includeType);
        final List<String> excludeTypeList = excludeType == null ? null : Collections.singletonList(excludeType);
        final List<String> includeNsList = includeNs == null ? null : Collections.singletonList(includeNs);
        final List<String> excludeNsList = excludeNs == null ? null : Collections.singletonList(excludeNs);
        return new ChildOption(includeTypeList, excludeTypeList, includeNsList, excludeNsList);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child type to include in the request's operation
     * @param excludeType the child type to exclude from the request's operation
     * @param includeNs the annotation namespaces to which this option applies
     * @param excludeNs the annotation namespaces to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(String includeType, String excludeType, List<String> includeNs,
            List<String> excludeNs) {
        final List<String> includeTypeList = includeType == null ? null : Collections.singletonList(includeType);
        final List<String> excludeTypeList = excludeType == null ? null : Collections.singletonList(excludeType);
        return new ChildOption(includeTypeList, excludeTypeList, includeNs, excludeNs);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @param includeNs the annotation namespace to which this option applies
     * @param excludeNs the annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(List<String> includeType, List<String> excludeType, String includeNs, String excludeNs) {
        final List<String> includeNsList = includeNs == null ? null : Collections.singletonList(includeNs);
        final List<String> excludeNsList = excludeNs == null ? null : Collections.singletonList(excludeNs);
        return new ChildOption(includeType, excludeType, includeNsList, excludeNsList);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @param includeNs the annotation namespaces to which this option applies
     * @param excludeNs the annotation namespaces to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(List<String> includeType, List<String> excludeType, List<String> includeNs,
            List<String> excludeNs) {
        return new ChildOption(includeType, excludeType, includeNs, excludeNs);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, (List<ChildOption>) null, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, ChildOption childOption, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, List<ChildOption> childOptions, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, childOptions, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, boolean dryRun, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, (List<ChildOption>) null, dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chgrp2(targetObjects, childOptions, dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, (List<ChildOption>) null, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, ChildOption childOption, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, childOptions, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, boolean dryRun, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, (List<ChildOption>) null, dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun, long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chgrp2(targetObjects, childOptions, dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, long groupId) {
        return new Chgrp2(targetObjects, (List<ChildOption>) null, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, ChildOption childOption, long groupId) {
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, long groupId) {
        return new Chgrp2(targetObjects, childOptions, false, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, boolean dryRun, long groupId) {
        return new Chgrp2(targetObjects, (List<ChildOption>) null, dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, long groupId) {
        return new Chgrp2(targetObjects, Collections.singletonList(childOption), dryRun, groupId);
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     */
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            long groupId) {
          return new Chgrp2(targetObjects, childOptions, dryRun, groupId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, (List<ChildOption>) null, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, ChildOption childOption, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, Collections.singletonList(childOption), false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, List<ChildOption> childOptions, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, childOptions, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, boolean dryRun, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, (List<ChildOption>) null, dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, Collections.singletonList(childOption), dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Chown2(targetObjects, childOptions, dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, (List<ChildOption>) null, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, ChildOption childOption, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, Collections.singletonList(childOption), false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, childOptions, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, boolean dryRun, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, (List<ChildOption>) null, dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun, long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, Collections.singletonList(childOption), dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Chown2(targetObjects, childOptions, dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, long userId) {
        return new Chown2(targetObjects, (List<ChildOption>) null, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, ChildOption childOption, long userId) {
        return new Chown2(targetObjects, Collections.singletonList(childOption), false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, long userId) {
        return new Chown2(targetObjects, childOptions, false, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, boolean dryRun, long userId) {
        return new Chown2(targetObjects, (List<ChildOption>) null, dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, long userId) {
        return new Chown2(targetObjects, Collections.singletonList(childOption), dryRun, userId);
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     */
    public static Chown2 chown(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            long userId) {
          return new Chown2(targetObjects, childOptions, dryRun, userId);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, (List<ChildOption>) null, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId, ChildOption childOption) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, Collections.singletonList(childOption), false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, childOptions, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, (List<ChildOption>) null, dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId, ChildOption childOption, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, Collections.singletonList(childOption), dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new Delete2(targetObjects, childOptions, dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, (List<ChildOption>) null, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds, ChildOption childOption) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, Collections.singletonList(childOption), false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, childOptions, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, (List<ChildOption>) null, dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, Collections.singletonList(childOption), dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new Delete2(targetObjects, childOptions, dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects) {
        return new Delete2(targetObjects, (List<ChildOption>) null, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects, ChildOption childOption) {
        return new Delete2(targetObjects, Collections.singletonList(childOption), false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Delete2(targetObjects, childOptions, false);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Delete2(targetObjects, (List<ChildOption>) null, dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        return new Delete2(targetObjects, Collections.singletonList(childOption), dryRun);
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     */
    public static Delete2 delete(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
          return new Delete2(targetObjects, childOptions, dryRun);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, childOptions, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, boolean dryRun, String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, childOptions, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, List<String> startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, childOptions, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, Collections.singletonList(targetId));
        return new SkipHead(targetObjects, childOptions, dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, childOptions, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, boolean dryRun, String startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun,
            String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, childOptions, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions,
            List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, childOptions, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetClass, targetIds);
        return new SkipHead(targetObjects, childOptions, dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, String startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        return new SkipHead(targetObjects, childOptions, false, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, boolean dryRun, String startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, Collections.singletonList(startFrom),
                request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, childOptions, dryRun, Collections.singletonList(startFrom), request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, (List<ChildOption>) null, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        return new SkipHead(targetObjects, Collections.singletonList(childOption), false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> startFrom,
            GraphModify2 request) {
        return new SkipHead(targetObjects, childOptions, false, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        return new SkipHead(targetObjects, (List<ChildOption>) null, dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, Collections.singletonList(childOption), dryRun, startFrom, request);
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     */
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return new SkipHead(targetObjects, childOptions, dryRun, startFrom, request);
    }
}
