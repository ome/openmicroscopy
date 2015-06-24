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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

import ome.model.IObject;
import ome.services.graphs.GraphPathBean;
import omero.cmd.graphs.ChildOption;

/**
 * Child options adjust how child objects are treated according to their type and, if annotations, namespace,
 * overriding the default graph traversal policy for orphans.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class ChildOptionI extends ChildOption {

    private final GraphPathBean graphPathBean;
    private final ImmutableSet<String> defaultExcludeNs;

    private Function<Class<? extends IObject>, Boolean> isIncludeType = null;
    private Predicate<String> isTargetNamespace = null;

    /**
     * Construct a new child option instance.
     * @param graphPathBean the graph path bean
     * @param defaultExcludeNs annotation namespaces to exclude by default
     */
    public ChildOptionI(GraphPathBean graphPathBean, ImmutableSet<String> defaultExcludeNs) {
        this.graphPathBean = graphPathBean;
        this.defaultExcludeNs = defaultExcludeNs;
    }

    /**
     * Construct a new child option instance identical to that given. If the original was initialized, this one is too.
     * @param original a child option instance
     */
    public ChildOptionI(ChildOptionI original) {
        graphPathBean = original.graphPathBean;
        defaultExcludeNs = original.defaultExcludeNs;

        includeType = original.includeType == null ? null : new ArrayList<String>(original.includeType);
        excludeType = original.excludeType == null ? null : new ArrayList<String>(original.excludeType);
        includeNs = original.includeNs == null ? null : new ArrayList<String>(original.includeNs);
        excludeNs = original.excludeNs == null ? null : new ArrayList<String>(original.excludeNs);
        isIncludeType = original.isIncludeType;
        isTargetNamespace = original.isTargetNamespace;
    }

    /**
     * Initialize this child option instance.
     * An option takes effect according to the {@link ChildOption} field values set when this method was last called.
     */
    public void init() {
        /* convert the class names to actual classes */

        final Function<String, Class<? extends IObject>> getClassFromName = new Function<String, Class<? extends IObject>>() {
            @Override
            public Class<? extends IObject> apply(String className) {
                final int lastDot = className.lastIndexOf('.');
                if (lastDot > 0) {
                    className = className.substring(lastDot + 1);
                }
                return graphPathBean.getClassForSimpleName(className);
            }
        };

        /* construct the function corresponding to the type-based inclusion requirements */

        final ImmutableSet<Class<? extends IObject>> typeInclusions;
        final ImmutableSet<Class<? extends IObject>> typeExclusions;

        if (CollectionUtils.isEmpty(includeType)) {
            typeInclusions = ImmutableSet.of();
        } else {
            typeInclusions = ImmutableSet.copyOf(Collections2.transform(includeType, getClassFromName));
        }
        if (CollectionUtils.isEmpty(excludeType)) {
            typeExclusions = ImmutableSet.of();
        } else {
            typeExclusions = ImmutableSet.copyOf(Collections2.transform(excludeType, getClassFromName));
        }

        if (typeInclusions.isEmpty() && typeExclusions.isEmpty()) {
            throw new IllegalArgumentException("child option must include or exclude some type");
        }

        isIncludeType = new Function<Class<? extends IObject>, Boolean>() {
            @Override
            public Boolean apply(Class<? extends IObject> objectClass) {
                for (final Class<? extends IObject> typeInclusion : typeInclusions) {
                    if (typeInclusion.isAssignableFrom(objectClass)) {
                        return Boolean.TRUE;
                    }
                }
                for (final Class<? extends IObject> typeExclusion : typeExclusions) {
                    if (typeExclusion.isAssignableFrom(objectClass)) {
                        return Boolean.FALSE;
                    }
                }
                return null;
            }
        };

        /* if no annotation namespaces are set, then apply the defaults */

        if (CollectionUtils.isEmpty(includeNs) && CollectionUtils.isEmpty(excludeNs)) {
            excludeNs = new ArrayList<String>(defaultExcludeNs);
        }

        /* construct the predicate corresponding to the namespace restriction */

        if (CollectionUtils.isEmpty(includeNs)) {
            if (CollectionUtils.isEmpty(excludeNs)) {
                /* there is no adjustment to make, not even for any default namespaces */
                isTargetNamespace = Predicates.alwaysTrue();
            } else {
                final ImmutableSet<String> nsExclusions = ImmutableSet.copyOf(excludeNs);
                isTargetNamespace = new Predicate<String>() {
                    @Override
                    public boolean apply(String namespace) {
                        return !nsExclusions.contains(namespace);
                    }
                };
            }
        } else {
            if (CollectionUtils.isEmpty(excludeNs)) {
                final ImmutableSet<String> nsInclusions = ImmutableSet.copyOf(includeNs);
                isTargetNamespace = new Predicate<String>() {
                    @Override
                    public boolean apply(String namespace) {
                        return nsInclusions.contains(namespace);
                    }
                };
            } else {
                throw new IllegalArgumentException("child option may not both include and exclude namespace");
            }
        }
    }

    

    /**
     * Test if this child option adjusts graph traversal policy for the given child object class.
     * Requires {@link #init()} to have been called previously.
     * @param objectClass a child object class
     * @return {@code true} if such children should be included in the operation,
     * {@code false} if such children should not be included in the operation, or
     * {@code null} if this child option does not affect the treatment of such children
     */
    public Boolean isIncludeType(Class<? extends IObject> objectClass) {
        return isIncludeType.apply(objectClass);
    }

    /**
     * Test if this child option adjusts graph traversal policy for child objects that are annotations in the given namespace.
     * Requires {@link #init()} to have been called previously.
     * @param namespace an annotation namespace
     * @return if child objects that are annotations in this namespace are affected by this child option
     */
    public boolean isTargetNamespace(String namespace) {
        return isTargetNamespace.apply(namespace);
    }

    /**
     * Cast {@code ChildOption[]} to {@code ChildOptionI[]}.
     * @param childOptions an array of {@code ChildOption} which may all be casted to {@code ChildOptionI}, may be {@code null}
     * @return an array of {@code ChildOptionI}, may be {@code null}
     */
    public static List<ChildOptionI> castChildOptions(Collection<ChildOption> childOptions) {
        if (childOptions == null) {
            return null;
        } else {
            final List<ChildOptionI> childOptionsI = new ArrayList<ChildOptionI>(childOptions.size());
            for (final ChildOption childOption : childOptions) {
                childOptionsI.add((ChildOptionI) childOption);
            }

            return childOptionsI;
        }
    }
}
