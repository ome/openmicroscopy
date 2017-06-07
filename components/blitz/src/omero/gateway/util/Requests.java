/*
 * Copyright (C) 2015-2017 University of Dundee & Open Microscopy Environment.
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
import omero.cmd.DiskUsage2;
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
         * @param types types of children to include in the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(Iterable<String> types) {
            for (final String type : types) {
                assembly.includeType.add(type);
            }
            return this;
        }

        /**
         * @param types types of children to include in the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.includeType.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeType(Iterable<String> types) {
            for (final String type : types) {
                assembly.excludeType.add(type);
            }
            return this;
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final ChildOptionBuilder excludeType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.excludeType.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param namespaces annotation namespaces to which to this option applies, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeNs(Iterable<String> namespaces) {
            for (final String namespace : namespaces) {
                assembly.includeNs.add(namespace);
            }
            return this;
        }

        /**
         * @param namespaces annotation namespaces to which to this option does not apply, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types types of children to include in the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeType(String... types) {
            return includeType(Arrays.asList(types));
        }

        /**
         * @param types types of children to exclude from the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder excludeType(String... types) {
            return excludeType(Arrays.asList(types));
        }

        /**
         * @param namespaces annotation namespaces to which to this option applies, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public ChildOptionBuilder includeNs(String... namespaces) {
            return includeNs(Arrays.asList(namespaces));
        }

        /**
         * @param namespaces annotation namespaces to which to this option does not apply, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param targets target objects for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param targetClass a target object type for this operation, required to then use an {@code id} method;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B target(String targetClass) {
            targetObjectClass = targetClass;
            return (B) this;
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param ids target object IDs for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param targets target objects for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param targets target objects for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public B target(Multimap<String, Long> targets) {
            return target(targets.asMap());
        }

        /**
         * @param targetClass a target object type for this operation, required to then use an {@code id} method;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public B target(Class<? extends IObject> targetClass) {
            return target(getModelClassName(targetClass));
        }

        /**
         * @param ids target object IDs for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param options child options for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param dryRun if this operation is a dry run, does overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        @SuppressWarnings("unchecked")
        public B dryRun(boolean dryRun) {
            assembly.dryRun = dryRun;
            return (B) this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param options child options for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param id the group to which to move the target objects, does overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chgrp2Builder toGroup(RLong id) {
            return toGroup(id.getValue());
        }

        /**
         * @param group the group to which to move the target objects, does overwrite previous calls;
         * {@code null} values not permitted
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
         * Instantiate a new {@link Chown2} and initialize its collection containers.
         */
        public Chown2Builder() {
            super(new Chown2());
            assembly.targetUsers = new ArrayList<Long>();
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

        /**
         * @param ids the IDs of users whose data is to be targeted for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder targetUsers(Iterable<Long> ids) {
            for (final Long id : ids) {
                assembly.targetUsers.add(id);
            }
            return this;
        }

        /**
         * @param ids the IDs of users whose data is to be targeted for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder targetUsers(RLong... ids) {
            for (final RLong id : ids) {
                assembly.targetUsers.add(id.getValue());
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param id the user to which to give the target objects, does overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder toUser(RLong id) {
            return toUser(id.getValue());
        }

        /**
         * @param user the user to which to give the target objects, does overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder toUser(Experimenter user) {
            return toUser(user.getId());
        }

        /**
         * @param ids the IDs of users whose data is to be targeted for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder targetUsers(Long... ids) {
            return targetUsers(Arrays.asList(ids));
        }

        /**
         * @param user the user whose data is to be targeted for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public Chown2Builder targetUsers(Experimenter user) {
            return targetUsers(user.getId());
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
         * @param permissions the permissions to which to set the target objects, does overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types types to duplicate, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DuplicateBuilder duplicateType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToDuplicate.add(type);
            }
            return this;
        }

        /**
         * @param types types to duplicate, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final DuplicateBuilder duplicateType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesToDuplicate.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DuplicateBuilder referenceType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToReference.add(type);
            }
            return this;
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final DuplicateBuilder referenceType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesToReference.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DuplicateBuilder ignoreType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesToIgnore.add(type);
            }
            return this;
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types types to duplicate, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DuplicateBuilder duplicateType(String... types) {
            return duplicateType(Arrays.asList(types));
        }

        /**
         * @param types types to reference from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DuplicateBuilder referenceType(String... types) {
            return referenceType(Arrays.asList(types));
        }

        /**
         * @param types types to keep separate from duplicates, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types the types of parents to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindParentsBuilder parentType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesOfParents.add(type);
            }
            return this;
        }

        /**
         * @param types the types of parents to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final FindParentsBuilder parentType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesOfParents.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindParentsBuilder stopBefore(Iterable<String> types) {
            for (final String type : types) {
                assembly.stopBefore.add(type);
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types the types of parents to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindParentsBuilder parentType(String... types) {
            return parentType(Arrays.asList(types));
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types the types of children to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder childType(Iterable<String> types) {
            for (final String type : types) {
                assembly.typesOfChildren.add(type);
            }
            return this;
        }

        /**
         * @param types the types of children to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final FindChildrenBuilder childType(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.typesOfChildren.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder stopBefore(Iterable<String> types) {
            for (final String type : types) {
                assembly.stopBefore.add(type);
            }
            return this;
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types the types of children to find, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public FindChildrenBuilder childType(String... types) {
            return childType(Arrays.asList(types));
        }

        /**
         * @param types the types to exclude from the search, does not overwrite previous calls;
         * {@code null} values not permitted
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
         * @param types types from which to start the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public SkipHeadBuilder startFrom(Iterable<String> types) {
            for (final String type : types) {
                assembly.startFrom.add(type);
            }
            return this;
        }

        /**
         * @param types types from which to start the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final SkipHeadBuilder startFrom(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.startFrom.add(getModelClassName(type));
            }
            return this;
        }

        /**
         * @param request the operation to perform once target objects are identified, does overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final SkipHeadBuilder request(GraphModify2 request) {
            assembly.request = request;
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types types from which to start the operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public SkipHeadBuilder startFrom(String... types) {
            return startFrom(Arrays.asList(types));
        }

        /**
         * @param request the operation to perform once target objects are identified, does overwrite previous calls;
         * {@code null} values not permitted
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
     * A builder for {@link DiskUsage2} instances.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.3
     */
    public static class DiskUsageBuilder extends GraphQueryBuilder<DiskUsageBuilder, DiskUsage2> {

        /**
         * Instantiate a new {@link DiskUsage2} and initialize its collection containers.
         */
        DiskUsageBuilder() {
            super(new DiskUsage2());
            assembly.targetClasses = new ArrayList<String>();
        }

        /* PROPERTY SETTERS THAT ACT DIRECTLY ON THE INSTANCE BEING ASSEMBLED */

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public DiskUsageBuilder type(Iterable<String> types) {
            for (final String type : types) {
                assembly.targetClasses.add(type);
            }
            return this;
        }

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
         * @return this builder, for method chaining
         */
        public final DiskUsageBuilder type(@SuppressWarnings("unchecked") Class<? extends IObject>... types) {
            for (final Class<? extends IObject> type : types) {
                assembly.targetClasses.add(getModelClassName(type));
            }
            return this;
        }

        /* PROPERTY SETTERS THAT SIMPLY WRAP USAGE OF THE ABOVE SETTERS */

        /**
         * @param types whole types to target for this operation, does not overwrite previous calls;
         * {@code null} values not permitted
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
     * @return a new {@link DiskUsage2} builder
     */
    public static DiskUsageBuilder diskUsage() {
        return new DiskUsageBuilder();
    }
}
