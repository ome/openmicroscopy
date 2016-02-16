/*
 * Copyright (C) 2015-2016 University of Dundee & Open Microscopy Environment.
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

package omero.gateway.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.cmd.Chgrp2;
import omero.cmd.Chmod2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.cmd.DiskUsage;
import omero.cmd.Duplicate;
import omero.cmd.GraphModify2;
import omero.cmd.SkipHead;
import omero.cmd.graphs.ChildOption;
import omero.model.IObject;

/**
 * A utility class of factory methods with various signatures for
 * clients to use in generating requests for graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class Requests {

    /**
     * Find the mapped type name for the given model object class.
     * @param objectType a model object class
     * @return the class' mapped type name
     */
    private static String getClassName(Class<? extends IObject> objectType) {
        if (objectType == IObject.class) {
            return IObject.class.getSimpleName();
        }
        while (true) {
            final Class<? extends IObject> superclass =
                objectType.getClass().asSubclass(IObject.class);
            if (superclass == IObject.class) {
                return objectType.getSimpleName();
            }
            objectType = superclass;
        }
    }

    /**
     * Convert a collection of model objects to a map of type names and IDs.
     * @param objects the model object collection
     * @return the objects' types and IDs
     */
    private static Map<String, List<Long>> mapOfObjects(
        Collection<? extends IObject> objects) {
        final Map<String, List<Long>> objectMap =
            new HashMap<String, List<Long>>();
        for (final IObject object : objects) {
            final String objectType = getClassName(object.getClass());
            List<Long> objectIds = objectMap.get(objectType);
            if (objectIds == null) {
                objectIds = new ArrayList<Long>();
                objectMap.put(objectType, objectIds);
            }
            objectIds.add(object.getId().getValue());
        }
        return objectMap;
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType lists each child type to include
     * @param excludeType lists each child type to exclude
     * @param includeNs lists each annotation namespace to which this option applies
     * @param excludeNs lists each annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(
        List<String> includeType, List<String> excludeType, List<String> includeNs, List<String> excludeNs) {
        return new ChildOption(
            includeType,
            excludeType,
            includeNs,
            excludeNs);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType lists each child type to include
     * @param excludeType lists each child type to exclude
     * @return the new instance
     */
    public static ChildOption option(
        List<String> includeType, List<String> excludeType) {
        return new ChildOption(
            includeType,
            excludeType,
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @return the new instance
     */
    public static ChildOption option() {
        return new ChildOption(
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, Long groupId) {
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, boolean dryRun, Long groupId) {
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, Long groupId) {
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects) {
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<ChildOption> childOptions, boolean dryRun) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<ChildOption> childOptions, Long groupId) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<ChildOption> childOptions) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        boolean dryRun, Long groupId) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        boolean dryRun) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Long groupId) {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @return the new instance
     */
    public static Chgrp2 chgrp() {
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String permissions) {
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, boolean dryRun, String permissions) {
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, String permissions) {
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects) {
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<ChildOption> childOptions, boolean dryRun, String permissions) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<ChildOption> childOptions, boolean dryRun) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<ChildOption> childOptions, String permissions) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<ChildOption> childOptions) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        boolean dryRun, String permissions) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        boolean dryRun) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String permissions) {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @return the new instance
     */
    public static Chmod2 chmod() {
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, Long userId) {
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, boolean dryRun, Long userId) {
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, Long userId) {
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects) {
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<ChildOption> childOptions, boolean dryRun, Long userId) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        List<ChildOption> childOptions, boolean dryRun) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<ChildOption> childOptions, Long userId) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        List<ChildOption> childOptions) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        boolean dryRun, Long userId) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        boolean dryRun) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Long userId) {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @return the new instance
     */
    public static Chown2 chown() {
        return new Chown2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects) {
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        List<ChildOption> childOptions, boolean dryRun) {
        return new Delete2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        List<ChildOption> childOptions) {
        return new Delete2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        boolean dryRun) {
        return new Delete2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @return the new instance
     */
    public static Delete2 delete() {
        return new Delete2(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects) {
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @return the new instance
     */
    public static Duplicate duplicate() {
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> startFrom) {
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> startFrom) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, GraphModify2 request) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects) {
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, List<String> startFrom) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun, List<String> startFrom, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun, List<String> startFrom) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun, GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        GraphModify2 request) {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @return the new instance
     */
    public static SkipHead skipHead() {
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objects map of each object by type and ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, Map<String, List<Long>> objects) {
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes) {
        return new DiskUsage(
            classes,
            new HashMap<String, List<Long>>());
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objects map of each object by type and ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        Map<String, List<Long>> objects) {
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @return the new instance
     */
    public static DiskUsage diskUsage() {
        return new DiskUsage(
            new ArrayList<String>(),
            new HashMap<String, List<Long>>());
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeOneType the child type to include
     * @param excludeOneType the child type to exclude
     * @param includeNs lists each annotation namespace to which this option applies
     * @param excludeNs lists each annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(
        String includeOneType, String excludeOneType, List<String> includeNs, List<String> excludeNs) {
        final List<String> includeType = new ArrayList<String>();
        if (includeOneType != null) {
            includeType.add(includeOneType);
        }
        final List<String> excludeType = new ArrayList<String>();
        if (excludeOneType != null) {
            excludeType.add(excludeOneType);
        }
        return new ChildOption(
            includeType,
            excludeType,
            includeNs,
            excludeNs);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeOneType the child type to include
     * @param excludeOneType the child type to exclude
     * @return the new instance
     */
    public static ChildOption option(
        String includeOneType, String excludeOneType) {
        final List<String> includeType = new ArrayList<String>();
        if (includeOneType != null) {
            includeType.add(includeOneType);
        }
        final List<String> excludeType = new ArrayList<String>();
        if (excludeOneType != null) {
            excludeType.add(excludeOneType);
        }
        return new ChildOption(
            includeType,
            excludeType,
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType lists each child type to include
     * @param excludeType lists each child type to exclude
     * @param includeOneNs the annotation namespace to which this option applies
     * @param excludeOneNs the annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(
        List<String> includeType, List<String> excludeType, String includeOneNs, String excludeOneNs) {
        final List<String> includeNs = new ArrayList<String>();
        if (includeOneNs != null) {
            includeNs.add(includeOneNs);
        }
        final List<String> excludeNs = new ArrayList<String>();
        if (excludeOneNs != null) {
            excludeNs.add(excludeOneNs);
        }
        return new ChildOption(
            includeType,
            excludeType,
            includeNs,
            excludeNs);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, List<ChildOption> childOptions, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, Long groupId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, Long groupId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chgrp2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, List<ChildOption> childOptions, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String permissions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String permissions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chmod2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, List<ChildOption> childOptions, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, Long userId) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, Long userId) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Chown2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Delete2(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Delete2(
            new HashMap<String, List<Long>>(),
            childOptions,
            false);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> startFrom) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, GraphModify2 request) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds) {
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objects map of each object by type and ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, Map<String, List<Long>> objects) {
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass) {
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            new HashMap<String, List<Long>>());
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectInstance the object
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, IObject objectInstance) {
        final Map<String, List<Long>> objects = mapOfObjects(Collections.singleton(objectInstance));
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objectInstance the object
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        IObject objectInstance) {
        final Map<String, List<Long>> objects = mapOfObjects(Collections.singleton(objectInstance));
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectInstances lists each object
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, List<? extends IObject> objectInstances) {
        final Map<String, List<Long>> objects = mapOfObjects(objectInstances);
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, String objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, Class<? extends IObject> objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        Class<? extends IObject> objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeOneType the child type to include
     * @param excludeOneType the child type to exclude
     * @param includeOneNs the annotation namespace to which this option applies
     * @param excludeOneNs the annotation namespace to which this option does not apply
     * @return the new instance
     */
    public static ChildOption option(
        String includeOneType, String excludeOneType, String includeOneNs, String excludeOneNs) {
        final List<String> includeNs = new ArrayList<String>();
        if (includeOneNs != null) {
            includeNs.add(includeOneNs);
        }
        final List<String> excludeNs = new ArrayList<String>();
        if (excludeOneNs != null) {
            excludeNs.add(excludeOneNs);
        }
        final List<String> includeType = new ArrayList<String>();
        if (includeOneType != null) {
            includeType.add(includeOneType);
        }
        final List<String> excludeType = new ArrayList<String>();
        if (excludeOneType != null) {
            excludeType.add(excludeOneType);
        }
        return new ChildOption(
            includeType,
            excludeType,
            includeNs,
            excludeNs);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, boolean dryRun, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, Long groupId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, boolean dryRun, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String permissions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, boolean dryRun, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, Long userId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            new ArrayList<ChildOption>(),
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> startFrom) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, GraphModify2 request) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId) {
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new SkipHead(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objectInstance the object
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, IObject objectInstance) {
        final Map<String, List<Long>> objects = mapOfObjects(Collections.singleton(objectInstance));
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, String objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objectType the object type
     * @param objectIds the IDs of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, Class<? extends IObject> objectType, List<Long> objectIds) {
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, String objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param classes lists each type
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        List<String> classes, Class<? extends IObject> objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        Class<? extends IObject> objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        return new DiskUsage(
            new ArrayList<String>(),
            objects);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param groupId the ID of the destination group
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, Long groupId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            groupId);
    }

    /**
     * Create a new {@link Chgrp2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chgrp2 chgrp(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chgrp2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            dryRun,
            null);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param permissions the new group permissions
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String permissions) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            permissions);
    }

    /**
     * Create a new {@link Chmod2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chmod2 chmod(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chmod2(
            targetObjects,
            childOptions,
            false,
            null);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            dryRun,
            -1);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param userId the ID of the destination user
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, Long userId) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            userId);
    }

    /**
     * Create a new {@link Chown2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Chown2 chown(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Chown2(
            targetObjects,
            childOptions,
            false,
            -1);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            dryRun);
    }

    /**
     * Create a new {@link Delete2} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Delete2 delete(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Delete2(
            targetObjects,
            childOptions,
            false);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            new ArrayList<String>(),
            new ArrayList<String>());
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        IObject targetObjectInstance, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> startFrom, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param startFrom lists each type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> startFrom) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, GraphModify2 request) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption) {
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            new ArrayList<String>(),
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, String objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(objectType, objectIds);
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link DiskUsage} instance.
     * @param oneClass the type
     * @param objectType the object type
     * @param objectId the ID of the object ID
     * @return the new instance
     */
    public static DiskUsage diskUsage(
        String oneClass, Class<? extends IObject> objectType, Long objectId) {
        final List<Long> objectIds = new ArrayList<Long>();
        if (objectId != null) {
            objectIds.add(objectId);
        }
        final Map<String, List<Long>> objects = new HashMap<String, List<Long>>();
        objects.put(getClassName(objectType), objectIds);
        final List<String> classes = new ArrayList<String>();
        if (oneClass != null) {
            classes.add(oneClass);
        }
        return new DiskUsage(
            classes,
            objects);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, List<String> typesToIgnore) {
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjects map of each target object by type and ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Map<String, List<Long>> targetObjects, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        return new Duplicate(
            new HashMap<String, List<Long>>(),
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            dryRun,
            startFrom,
            null);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @param request the processor
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String oneStartFrom, GraphModify2 request) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            request);
    }

    /**
     * Create a new {@link SkipHead} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param oneStartFrom the type from which to process
     * @return the new instance
     */
    public static SkipHead skipHead(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String oneStartFrom) {
        final List<String> startFrom = new ArrayList<String>();
        if (oneStartFrom != null) {
            startFrom.add(oneStartFrom);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new SkipHead(
            targetObjects,
            childOptions,
            false,
            startFrom,
            null);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstance the target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        IObject targetObjectInstance, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(Collections.singleton(targetObjectInstance));
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectInstances lists each target object
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        List<? extends IObject> targetObjectInstances, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = mapOfObjects(targetObjectInstances);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typesToIgnore lists each type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, String typeToReference, List<String> typesToIgnore) {
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typesToReference lists each type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, List<String> typesToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typesToDuplicate lists each type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, List<String> typesToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOptions child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, List<ChildOption> childOptions, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            new ArrayList<ChildOption>(),
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectIds the IDs of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, List<Long> targetObjectIds, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        String targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(targetObjectType, targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param dryRun if this is a dry run
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, boolean dryRun, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            dryRun,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }

    /**
     * Create a new {@link Duplicate} instance.
     * @param targetObjectType the target object type
     * @param targetObjectId the ID of the target object ID
     * @param childOption child option
     * @param typeToDuplicate the type to duplicate
     * @param typeToReference the type to reference
     * @param typeToIgnore the type to ignore
     * @return the new instance
     */
    public static Duplicate duplicate(
        Class<? extends IObject> targetObjectType, Long targetObjectId, ChildOption childOption, String typeToDuplicate, String typeToReference, String typeToIgnore) {
        final List<String> typesToIgnore = new ArrayList<String>();
        if (typeToIgnore != null) {
            typesToIgnore.add(typeToIgnore);
        }
        final List<String> typesToReference = new ArrayList<String>();
        if (typeToReference != null) {
            typesToReference.add(typeToReference);
        }
        final List<String> typesToDuplicate = new ArrayList<String>();
        if (typeToDuplicate != null) {
            typesToDuplicate.add(typeToDuplicate);
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>();
        if (childOption != null) {
            childOptions.add(childOption);
        }
        final List<Long> targetObjectIds = new ArrayList<Long>();
        if (targetObjectId != null) {
            targetObjectIds.add(targetObjectId);
        }
        final Map<String, List<Long>> targetObjects = new HashMap<String, List<Long>>();
        targetObjects.put(getClassName(targetObjectType), targetObjectIds);
        return new Duplicate(
            targetObjects,
            childOptions,
            false,
            typesToDuplicate,
            typesToReference,
            typesToIgnore);
    }
}
