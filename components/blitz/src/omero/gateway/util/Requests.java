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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import omero.RLong;
import omero.cmd.Chgrp2;
import omero.cmd.Chmod2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.cmd.DiskUsage;
import omero.cmd.Duplicate;
import omero.cmd.FindChildren;
import omero.cmd.FindParents;
import omero.cmd.GraphModify2;
import omero.cmd.GraphQuery;
import omero.cmd.SkipHead;
import omero.cmd.graphs.ChildOption;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;

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
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(String includeType, String excludeType) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        return builder.build();
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @return the new instance
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(List<String> includeType, List<String> excludeType) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        return builder.build();
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child type to include in the request's operation
     * @param excludeType the child type to exclude from the request's operation
     * @param includeNs the annotation namespace to which this option applies
     * @param excludeNs the annotation namespace to which this option does not apply
     * @return the new instance
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(String includeType, String excludeType, String includeNs, String excludeNs) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        if (includeNs != null) {
            builder = builder.includeNs(includeNs);
        }
        if (excludeNs != null) {
            builder = builder.excludeNs(excludeNs);
        }
        return builder.build();
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child type to include in the request's operation
     * @param excludeType the child type to exclude from the request's operation
     * @param includeNs the annotation namespaces to which this option applies
     * @param excludeNs the annotation namespaces to which this option does not apply
     * @return the new instance
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(String includeType, String excludeType, List<String> includeNs,
            List<String> excludeNs) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        if (includeNs != null) {
            builder = builder.includeNs(includeNs);
        }
        if (excludeNs != null) {
            builder = builder.excludeNs(excludeNs);
        }
        return builder.build();
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @param includeNs the annotation namespace to which this option applies
     * @param excludeNs the annotation namespace to which this option does not apply
     * @return the new instance
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(List<String> includeType, List<String> excludeType, String includeNs, String excludeNs) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        if (includeNs != null) {
            builder = builder.includeNs(includeNs);
        }
        if (excludeNs != null) {
            builder = builder.excludeNs(excludeNs);
        }
        return builder.build();
    }

    /**
     * Create a new {@link ChildOption} instance.
     * @param includeType the child types to include in the request's operation
     * @param excludeType the child types to exclude from the request's operation
     * @param includeNs the annotation namespaces to which this option applies
     * @param excludeNs the annotation namespaces to which this option does not apply
     * @return the new instance
     * @deprecated use {@link Requests.ChildOptionBuilder} from {@link #option()}, see this method for an example
     */
    @Deprecated
    public static ChildOption option(List<String> includeType, List<String> excludeType, List<String> includeNs,
            List<String> excludeNs) {
        ChildOptionBuilder builder = option();
        if (includeType != null) {
            builder = builder.includeType(includeType);
        }
        if (excludeType != null) {
            builder = builder.excludeType(excludeType);
        }
        if (includeNs != null) {
            builder = builder.includeNs(includeNs);
        }
        if (excludeNs != null) {
            builder = builder.excludeNs(excludeNs);
        }
        return builder.build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, long groupId) {
        return chgrp().target(targetClass).id(targetId).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, ChildOption childOption, long groupId) {
        return chgrp().target(targetClass).id(targetId).option(childOption).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, List<ChildOption> childOptions, long groupId) {
        return chgrp().target(targetClass).id(targetId).option(childOptions).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, boolean dryRun, long groupId) {
        return chgrp().target(targetClass).id(targetId).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, long groupId) {
        return chgrp().target(targetClass).id(targetId).option(childOption).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun, long groupId) {
        return chgrp().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, long groupId) {
        return chgrp().target(targetClass).id(targetIds).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, ChildOption childOption, long groupId) {
        return chgrp().target(targetClass).id(targetIds).option(childOption).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, long groupId) {
        return chgrp().target(targetClass).id(targetIds).option(childOptions).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, boolean dryRun, long groupId) {
        return chgrp().target(targetClass).id(targetIds).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun, long groupId) {
        return chgrp().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            long groupId) {
        return chgrp().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, long groupId) {
        return chgrp().target(targetObjects).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, ChildOption childOption, long groupId) {
        return chgrp().target(targetObjects).option(childOption).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, long groupId) {
        return chgrp().target(targetObjects).option(childOptions).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, boolean dryRun, long groupId) {
        return chgrp().target(targetObjects).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, long groupId) {
        return chgrp().target(targetObjects).option(childOption).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chgrp2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param groupId the destination group ID
     * @return the new request
     * @deprecated use {@link Requests.Chgrp2Builder} from {@link #chgrp()}, see this method for an example
     */
    @Deprecated
    public static Chgrp2 chgrp(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            long groupId) {
        return chgrp().target(targetObjects).option(childOptions).dryRun(dryRun).toGroup(groupId).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, String permissions) {
        return chmod().target(targetClass).id(targetId).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, ChildOption childOption, String permissions) {
        return chmod().target(targetClass).id(targetId).option(childOption).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, List<ChildOption> childOptions, String permissions) {
        return chmod().target(targetClass).id(targetId).option(childOptions).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, boolean dryRun, String permissions) {
        return chmod().target(targetClass).id(targetId).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, String permissions) {
        return chmod().target(targetClass).id(targetId).option(childOption).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun,
            String permissions) {
        return chmod().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, String permissions) {
        return chmod().target(targetClass).id(targetIds).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, ChildOption childOption, String permissions) {
        return chmod().target(targetClass).id(targetIds).option(childOption).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, String permissions) {
        return chmod().target(targetClass).id(targetIds).option(childOptions).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, boolean dryRun, String permissions) {
        return chmod().target(targetClass).id(targetIds).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun,
            String permissions) {
        return chmod().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            String permissions) {
        return chmod().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, String permissions) {
        return chmod().target(targetObjects).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, ChildOption childOption, String permissions) {
        return chmod().target(targetObjects).option(childOption).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String permissions) {
        return chmod().target(targetObjects).option(childOptions).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, boolean dryRun, String permissions) {
        return chmod().target(targetObjects).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, String permissions) {
        return chmod().target(targetObjects).option(childOption).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chmod2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param permissions the new permissions
     * @return the new request
     * @deprecated use {@link Requests.Chmod2Builder} from {@link #chmod()}, see this method for an example
     */
    @Deprecated
    public static Chmod2 chmod(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            String permissions) {
        return chmod().target(targetObjects).option(childOptions).dryRun(dryRun).toPerms(permissions).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, long userId) {
        return chown().target(targetClass).id(targetId).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, ChildOption childOption, long userId) {
        return chown().target(targetClass).id(targetId).option(childOption).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, List<ChildOption> childOptions, long userId) {
        return chown().target(targetClass).id(targetId).option(childOptions).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, boolean dryRun, long userId) {
        return chown().target(targetClass).id(targetId).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, long userId) {
        return chown().target(targetClass).id(targetId).option(childOption).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun, long userId) {
        return chown().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, long userId) {
        return chown().target(targetClass).id(targetIds).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, ChildOption childOption, long userId) {
        return chown().target(targetClass).id(targetIds).option(childOption).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, long userId) {
        return chown().target(targetClass).id(targetIds).option(childOptions).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, boolean dryRun, long userId) {
        return chown().target(targetClass).id(targetIds).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun, long userId) {
        return chown().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            long userId) {
        return chown().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, long userId) {
        return chown().target(targetObjects).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, ChildOption childOption, long userId) {
        return chown().target(targetObjects).option(childOption).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, long userId) {
        return chown().target(targetObjects).option(childOptions).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, boolean dryRun, long userId) {
        return chown().target(targetObjects).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun, long userId) {
        return chown().target(targetObjects).option(childOption).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Chown2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param userId the destination user ID
     * @return the new request
     * @deprecated use {@link Requests.Chown2Builder} from {@link #chown()}, see this method for an example
     */
    @Deprecated
    public static Chown2 chown(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            long userId) {
        return chown().target(targetObjects).option(childOptions).dryRun(dryRun).toUser(userId).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId) {
        return delete().target(targetClass).id(targetId).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId, ChildOption childOption) {
        return delete().target(targetClass).id(targetId).option(childOption).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId, List<ChildOption> childOptions) {
        return delete().target(targetClass).id(targetId).option(childOptions).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId, boolean dryRun) {
        return delete().target(targetClass).id(targetId).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId, ChildOption childOption, boolean dryRun) {
        return delete().target(targetClass).id(targetId).option(childOption).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun) {
        return delete().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds) {
        return delete().target(targetClass).id(targetIds).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds, ChildOption childOption) {
        return delete().target(targetClass).id(targetIds).option(childOption).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds, List<ChildOption> childOptions) {
        return delete().target(targetClass).id(targetIds).option(childOptions).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds, boolean dryRun) {
        return delete().target(targetClass).id(targetIds).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun) {
        return delete().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun) {
        return delete().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects) {
        return delete().target(targetObjects).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects, ChildOption childOption) {
        return delete().target(targetObjects).option(childOption).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions) {
        return delete().target(targetObjects).option(childOptions).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects, boolean dryRun) {
        return delete().target(targetObjects).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun) {
        return delete().target(targetObjects).option(childOption).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link Delete2} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @return the new request
     * @deprecated use {@link Requests.Delete2Builder} from {@link #delete()}, see this method for an example
     */
    @Deprecated
    public static Delete2 delete(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun) {
        return delete().target(targetObjects).option(childOptions).dryRun(dryRun).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, boolean dryRun, String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).dryRun(dryRun).startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, boolean dryRun, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOption).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetId the target object ID
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).dryRun(dryRun).startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOption).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, Long targetId, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetId).option(childOptions).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, boolean dryRun, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).dryRun(dryRun).startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetClass the target object class
     * @param targetIds the target object IDs
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).dryRun(dryRun).startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOption).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
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
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(String targetClass, List<Long> targetIds, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetClass).id(targetIds).option(childOptions).dryRun(dryRun)
                .startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, String startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, String startFrom,
            GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, boolean dryRun, String startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOption).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the class from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            String startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOptions).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOption).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOptions).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, boolean dryRun, List<String> startFrom,
            GraphModify2 request) {
        return skipHead().target(targetObjects).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOption how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, ChildOption childOption, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOption).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * Create a new {@link SkipHead} request.
     * @param targetObjects the target objects
     * @param childOptions how to process child objects
     * @param dryRun if this request is a dry run
     * @param startFrom the classes from which to start the actual processing
     * @param request the processor to use
     * @return the new request
     * @deprecated use {@link Requests.SkipHeadBuilder} from {@link #skipHead()}, see this method for an example
     */
    @Deprecated
    public static SkipHead skipHead(Map<String, List<Long>> targetObjects, List<ChildOption> childOptions, boolean dryRun,
            List<String> startFrom, GraphModify2 request) {
        return skipHead().target(targetObjects).option(childOptions).dryRun(dryRun).startFrom(startFrom).request(request).build();
    }

    /**
     * From a model object class determine its simple name suitable for HQL queries or request arguments.
     * @param modelClass a model object class
     * @return a good name for that class
     */
    private static String getModelClassName(Class<? extends IObject> modelClass) {
        if (modelClass != IObject.class) {
            while (true) {
                /* find a direct subclass of IObject */
                final Class<? extends IObject> superclass = modelClass.getSuperclass().asSubclass(IObject.class);
                if (superclass == IObject.class) {
                    break;
                }
                modelClass = superclass;
            }
        }
        return modelClass.getSimpleName();
    }

    /**
     * A general superclass for the builders.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     * @param <X> the type of object to be built
     */
    private static abstract class Builder<X> {

        protected X assembly;

        /**
         * Construct a new builder for the given object.
         * @param newAssembly the object to be built
         */
        Builder(X newAssembly) {
            this.assembly = newAssembly;
        }

        /**
         * Assemble and return the finished object.
         * @return the built instance
         */
        public X build() {
            return assembly;
        }
    }

    /**
     * A builder for {@link ChildOption} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class ChildOptionBuilder extends Builder<ChildOption> {

        /**
         * Instantiate a new {@link ChildOption} and initialize its collection containers.
         */
        private ChildOptionBuilder() {
            super(new ChildOption());
            assembly.includeType = new ArrayList<String>();
            assembly.excludeType = new ArrayList<String>();
            assembly.includeNs = new ArrayList<String>();
            assembly.excludeNs = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types types of children to include in the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(Iterable<String> types) {
            for (final String type : types) {
                assembly.includeType.add(type);
            }
            return this;
        }

        /**
         * @param types types of children to include in the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.includeType.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeType(Iterable<String> types) {
            for (final String type : types) {
                assembly.excludeType.add(type);
            }
            return this;
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final ChildOptionBuilder excludeType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.excludeType.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param namespaces annotation namespaces to which to this option applies, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeNs(Iterable<String> namespaces) {
            for (final String namespace : namespaces) {
                assembly.includeNs.add(namespace);
            }
            return this;
        }

        /**
         * @param namespaces annotation namespaces to which to this option does not apply, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeNs(Iterable<String> namespaces) {
            for (final String namespace : namespaces) {
                assembly.excludeNs.add(namespace);
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types types of children to include in the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(String... types) {
            return includeType(Arrays.asList(types));
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeType(String... types) {
            return excludeType(Arrays.asList(types));
        }

        /**
         * @param namespaces annotation namespaces to which to this option applies, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeNs(String... namespaces) {
            return includeNs(Arrays.asList(namespaces));
        }

        /**
         * @param namespaces annotation namespaces to which to this option does not apply, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeNs(String... namespaces) {
            return excludeNs(Arrays.asList(namespaces));
        }
    }

    /**
     * A builder for {@link GraphQuery} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.3.0
     * @param <B> the type of the builder
     * @param <R> the type of the object to be built
     */
    private static abstract class GraphQueryBuilder<B extends GraphQueryBuilder<B, R>, R extends GraphQuery>
        extends Builder<R> {

        /*
         * The @SuppressWarnings and omitted methods in this class are from Java's painfully primitive type system,
         * including lack of proper generics, let alone anything modern like associated type families.
         */

        /* the class targeted by calls to the id method */
        private String targetObjectClass = null;

        /* keep a deduplicated copy of all identified targets to minimize a possibly large argument size */
        private SetMultimap<String, Long> allTargets = HashMultimap.create();

        /**
         * Initialize a new {@link GraphQuery}'s collection containers.
         * @param assembly the new instance to assemble
         */
        GraphQueryBuilder(R assembly) {
            super(assembly);
            assembly.targetObjects = new HashMap<String, List<Long>>();
        }

        /**
         * Assemble and return the finished object.
         * @return the built instance
         */
        @Override
        public R build() {
            assembly.targetObjects.clear();
            for (final Map.Entry<String, Collection<Long>> target : allTargets.asMap().entrySet()) {
                assembly.targetObjects.put(target.getKey(), new ArrayList<Long>(target.getValue()));
            }
            return super.build();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B target(Map<String, ? extends Iterable<Long>> targets) {
            for (final Map.Entry<String, ? extends Iterable<Long>> classAndIds : targets.entrySet()) {
                allTargets.putAll(classAndIds.getKey(), classAndIds.getValue());
            }
            return (B) this;
        }

        /**
         * @param targetClass a target object type for this operation, required to then use an {@code id} method
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B target(String targetClass) {
            targetObjectClass = targetClass;
            return (B) this;
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        @SuppressWarnings("unchecked")
        public B id(Iterable<Long> ids) {
            if (targetObjectClass == null) {
                throw new IllegalStateException("must first use target(String) to set class name");
            }
            allTargets.putAll(targetObjectClass, ids);
            return (B) this;
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        @SuppressWarnings("unchecked")
        public B id(RLong... ids) {
            if (targetObjectClass == null) {
                throw new IllegalStateException("must first use target(String) to set class name");
            }
            for (final RLong id : ids) {
                allTargets.put(targetObjectClass, id.getValue());
            }
            return (B) this;
        }

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B target(IObject... targets) {
            for (final IObject target : targets) {
                target(target.getClass()).id(target.getId());
            }
            return (B) this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public B target(Multimap<String, Long> targets) {
            return target(targets.asMap());
        }

        /**
         * @param targetClass a target object type for this operation, required to then use an {@code id} method
         * @return this builder, for method chaining
         */
        public B target(Class<? extends IObject> targetClass) {
            return target(getModelClassName(targetClass));
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        public B id(Long... ids) {
            return id(Arrays.asList(ids));
        }
    }

    /**
     * A builder for {@link GraphModify2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     * @param <B> the type of the builder
     * @param <R> the type of the object to be built
     */
    private static abstract class GraphModify2Builder<B extends GraphModify2Builder<B, R>, R extends GraphModify2>
        extends GraphQueryBuilder<B, R> {

        /**
         * Initialize a new {@link GraphModify2}'s collection containers.
         * @param assembly the new instance to assemble
         */
        GraphModify2Builder(R assembly) {
            super(assembly);
            assembly.childOptions = new ArrayList<ChildOption>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param options child options for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B option(Iterable<ChildOption> options) {
            for (final ChildOption option : options) {
                assembly.childOptions.add(option);
            }
            return (B) this;
        }

        /**
         * @param dryRun if this operation is a dry run, does overwrite previous calls
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B dryRun(boolean dryRun) {
            assembly.dryRun = dryRun;
            return (B) this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param options child options for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public B option(ChildOption... options) {
            return option(Arrays.asList(options));
        }

        /**
         * Set that this operation is a dry run.
         * @return this builder, for method chaining
         */
        public B dryRun() {
            return dryRun(true);
        }
    }

    /**
     * A builder for {@link Chgrp2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class Chgrp2Builder extends GraphModify2Builder<Chgrp2Builder, Chgrp2> {

        /**
         * Instantiate a new {@link Chgrp2}.
         */
        public Chgrp2Builder() {
            super(new Chgrp2());
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param id the group to which to move the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chgrp2Builder toGroup(long id) {
            assembly.groupId = id;
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param id the group to which to move the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chgrp2Builder toGroup(RLong id) {
            return toGroup(id.getValue());
        }

        /**
         * @param group the group to which to move the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chgrp2Builder toGroup(ExperimenterGroup group) {
            return toGroup(group.getId());
        }
    }

    /**
     * A builder for {@link Chown2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class Chown2Builder extends GraphModify2Builder<Chown2Builder, Chown2> {

        /**
         * Instantiate a new {@link Chown2}.
         */
        public Chown2Builder() {
            super(new Chown2());
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param id the user to which to give the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chown2Builder toUser(long id) {
            assembly.userId = id;
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param id the user to which to give the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chown2Builder toUser(RLong id) {
            return toUser(id.getValue());
        }

        /**
         * @param user the user to which to give the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chown2Builder toGroup(Experimenter user) {
            return toUser(user.getId());
        }
    }

    /**
     * A builder for {@link Chmod2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class Chmod2Builder extends GraphModify2Builder<Chmod2Builder, Chmod2> {

        /**
         * Instantiate a new {@link Chmod2}.
         */
        public Chmod2Builder() {
            super(new Chmod2());
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param permissions the permissions to which to set the target objects, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public Chmod2Builder toPerms(String permissions) {
            assembly.permissions = permissions;
            return this;
        }
    }

    /**
     * A builder for {@link Delete2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class Delete2Builder extends GraphModify2Builder<Delete2Builder, Delete2> {

        /**
         * Instantiate a new {@link Delete2}.
         */
        public Delete2Builder() {
            super(new Delete2());
        }
    }

    /**
     * A builder for {@link Duplicate} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class DuplicateBuilder extends GraphModify2Builder<DuplicateBuilder, Duplicate> {

        /**
         * Instantiate a new {@link Duplicate} and initialize its collection containers.
         */
        public DuplicateBuilder() {
            super(new Duplicate());
            assembly.typesToDuplicate = new ArrayList<String>();
            assembly.typesToReference = new ArrayList<String>();
            assembly.typesToIgnore    = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types types to duplicate, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder duplicateType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToDuplicate.add(type);
            }
            return this;
        }

        /**
         * @param types types to duplicate, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final DuplicateBuilder duplicateType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesToDuplicate.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder referenceType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToReference.add(type);
            }
            return this;
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final DuplicateBuilder referenceType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesToReference.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder ignoreType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToIgnore.add(type);
            }
            return this;
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final DuplicateBuilder ignoreType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesToIgnore.add(getModelClassName(type));
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types types to duplicate, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder duplicateType(String... types) {
            return duplicateType(Arrays.asList(types));
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder referenceType(String... types) {
            return referenceType(Arrays.asList(types));
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DuplicateBuilder ignoreType(String... types) {
            return ignoreType(Arrays.asList(types));
        }
    }

    /**
     * A builder for {@link FindParents} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.3.0
     */
    public static class FindParentsBuilder extends GraphQueryBuilder<FindParentsBuilder, FindParents> {

        /**
         * Instantiate a new {@link FindParents} and initialize its collection containers.
         */
        public FindParentsBuilder() {
            super(new FindParents());
            assembly.typesOfParents = new ArrayList<String>();
            assembly.stopBefore = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types the types of parents to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindParentsBuilder parentType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesOfParents.add(type);
            }
            return this;
        }

        /**
         * @param types the types of parents to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final FindParentsBuilder parentType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesOfParents.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindParentsBuilder stopBefore(Iterable<String> types) {
            for (final String type : types) {
                assembly.stopBefore.add(type);
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final FindParentsBuilder stopBefore(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.stopBefore.add(getModelClassName(type));
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types the types of parents to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindParentsBuilder parentType(String... types) {
            return parentType(Arrays.asList(types));
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindParentsBuilder stopBefore(String... types) {
            return stopBefore(Arrays.asList(types));
        }
    }

    /**
     * A builder for {@link FindChildren} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.3.0
     */
    public static class FindChildrenBuilder extends GraphQueryBuilder<FindChildrenBuilder, FindChildren> {

        /**
         * Instantiate a new {@link FindChildren} and initialize its collection containers.
         */
        public FindChildrenBuilder() {
            super(new FindChildren());
            assembly.typesOfChildren = new ArrayList<String>();
            assembly.stopBefore = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types the types of children to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder childType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesOfChildren.add(type);
            }
            return this;
        }

        /**
         * @param types the types of children to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final FindChildrenBuilder childType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesOfChildren.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder stopBefore(Iterable<String> types) {
            for (final String type : types) {
                assembly.stopBefore.add(type);
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final FindChildrenBuilder stopBefore(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.stopBefore.add(getModelClassName(type));
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types the types of children to find, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder childType(String... types) {
            return childType(Arrays.asList(types));
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder stopBefore(String... types) {
            return stopBefore(Arrays.asList(types));
        }
}

    /**
     * A builder for {@link SkipHead} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class SkipHeadBuilder extends GraphModify2Builder<SkipHeadBuilder, SkipHead> {

        /**
         * Instantiate a new {@link SkipHead} and initialize its collection containers.
         */
        public SkipHeadBuilder() {
            super(new SkipHead());
            assembly.startFrom = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types types from which to start the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public SkipHeadBuilder startFrom(Iterable<String> types) {
            for (final String type : types) {
                assembly.startFrom.add(type);
            }
            return this;
        }

        /**
         * @param types types from which to start the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final SkipHeadBuilder startFrom(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.startFrom.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param request the operation to perform once target objects are identified, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public final SkipHeadBuilder request(GraphModify2 request) {
            assembly.request = request;
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types types from which to start the operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public SkipHeadBuilder startFrom(String... types) {
            return startFrom(Arrays.asList(types));
        }

        /**
         * @param request the operation to perform once target objects are identified, does overwrite previous calls
         * @return this builder, for method chaining
         */
        public final SkipHeadBuilder request(Class<? extends GraphModify2> request) {
            try {
                return request(request.newInstance());
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("invalid request class", e);
            }
        }
    }

    /**
     * A builder for {@link DiskUsage} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class DiskUsageBuilder extends Builder<DiskUsage> {

        /* the class targeted by calls to the id method */
        private String targetObjectClass = null;

        /* keep a deduplicated copy of all identified targets to minimize a possibly large argument size */
        private SetMultimap<String, Long> allTargets = HashMultimap.create();

        /**
         * Instantiate a new {@link DiskUsage} and initialize its collection containers.
         */
        DiskUsageBuilder() {
            super(new DiskUsage());
            assembly.classes = new ArrayList<String>();
            assembly.objects = new HashMap<String, List<Long>>();
        }

        /**
         * Assemble and return the finished object.
         * @return the built instance
         */
        @Override
        public DiskUsage build() {
            assembly.objects.clear();
            for (final Map.Entry<String, Collection<Long>> target : allTargets.asMap().entrySet()) {
                assembly.objects.put(target.getKey(), new ArrayList<Long>(target.getValue()));
            }
            return super.build();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder target(Map<String, ? extends Iterable<Long>> targets) {
            for (final Map.Entry<String, ? extends Iterable<Long>> classAndIds : targets.entrySet()) {
                allTargets.putAll(classAndIds.getKey(), classAndIds.getValue());
            }
            return this;
        }

        /**
         * @param targetClass a target object type for this operation, required to then use an {@code id} method
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder target(String targetClass) {
            targetObjectClass = targetClass;
            return this;
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        public DiskUsageBuilder id(Iterable<Long> ids) {
            if (targetObjectClass == null) {
                throw new IllegalStateException("must first use target(String) to set class name");
            }
            allTargets.putAll(targetObjectClass, ids);
            return this;
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        public DiskUsageBuilder id(RLong... ids) {
            if (targetObjectClass == null) {
                throw new IllegalStateException("must first use target(String) to set class name");
            }
            for (final RLong id : ids) {
                allTargets.put(targetObjectClass, id.getValue());
            }
            return this;
        }

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder target(IObject... targets) {
            for (final IObject target : targets) {
                target(target.getClass()).id(target.getId());
            }
            return this;
        }

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder type(Iterable<String> types) {
            for (final String type : types) {
                assembly.classes.add(type);
            }
            return this;
        }

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public final DiskUsageBuilder type(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.classes.add(getModelClassName(type));
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param targets target objects for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder target(Multimap<String, Long> targets) {
            return target(targets.asMap());
        }

        /**
         * @param targetClass a target object type for this operation, required to then use an {@code id} method
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder target(Class<? extends IObject> targetClass) {
            return target(getModelClassName(targetClass));
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         * @see #target(String)
         * @see #target(Class)
         */
        public DiskUsageBuilder id(Long... ids) {
            return id(Arrays.asList(ids));
        }

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder type(String... types) {
            return type(Arrays.asList(types));
        }
    }

    /**
     * @return a new {@link ChildOption} builder
     */
    public static ChildOptionBuilder option() {
        return new ChildOptionBuilder();
    }

    /**
     * @return a new {@link Chgrp2} builder
     */
    public static Chgrp2Builder chgrp() {
        return new Chgrp2Builder();
    }

    /**
     * @return a new {@link Chown2} builder
     */
    public static Chown2Builder chown() {
        return new Chown2Builder();
    }

    /**
     * @return a new {@link Chmod2} builder
     */
    public static Chmod2Builder chmod() {
        return new Chmod2Builder();
    }

    /**
     * @return a new {@link Delete2} builder
     */
    public static Delete2Builder delete() {
        return new Delete2Builder();
    }

    /**
     * @return a new {@link Duplicate} builder
     */
    public static DuplicateBuilder duplicate() {
        return new DuplicateBuilder();
    }

    /**
     * @return a new {@link FindParents} builder
     */
    public static FindParentsBuilder findParents() {
        return new FindParentsBuilder();
    }

    /**
     * @return a new {@link FindChildren} builder
     */
    public static FindChildrenBuilder findChildren() {
        return new FindChildrenBuilder();
    }

    /**
     * @return a new {@link SkipHead} builder
     */
    public static SkipHeadBuilder skipHead() {
        return new SkipHeadBuilder();
    }

    /**
     * @return a new {@link DiskUsage} builder
     */
    public static DiskUsageBuilder diskUsage() {
        return new DiskUsageBuilder();
    }
}
