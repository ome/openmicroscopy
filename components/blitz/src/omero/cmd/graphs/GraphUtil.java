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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.hibernate.Session;

import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy.Ability;
import ome.services.graphs.GraphTraversal.Processor;
import ome.services.graphs.ModelObjectSequencer;
import omero.cmd.GraphModify2;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Static utility methods for model graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphUtil {
    /**
     * Copy the {@link GraphModify2} fields of one request to another.
     * @param requestFrom the source of the field copy
     * @param requestTo the target of the field copy
     */
    static void copyFields(GraphModify2 requestFrom, GraphModify2 requestTo) {
        if (requestFrom.targetObjects == null) {
            requestTo.targetObjects = null;
        } else {
            requestTo.targetObjects = new HashMap<String, List<Long>>();
            for (final Map.Entry<String, List<Long>> targetObjectsOneClass : requestFrom.targetObjects.entrySet()) {
                final String targetClass = targetObjectsOneClass.getKey();
                final List<Long> targetIds = targetObjectsOneClass.getValue();
                requestTo.targetObjects.put(targetClass, new ArrayList<Long>(targetIds));
            }
        }
        if (requestFrom.childOptions == null) {
            requestTo.childOptions = null;
        } else {
            requestTo.childOptions = new ArrayList<ChildOption>(requestFrom.childOptions);
        }
        requestTo.dryRun = requestFrom.dryRun;
    }

    /**
     * Make a copy of a multimap with the full class names in the keys replaced by the simple class names
     * and the ordering of the values preserved.
     * @param entriesByFullName a multimap
     * @return a new multimap with the same contents, except for the package name having been trimmed off each key
     */
    static <X> SetMultimap<String, X> trimPackageNames(SetMultimap<String, X> entriesByFullName) {
        final SetMultimap<String, X> entriesBySimpleName = LinkedHashMultimap.create();
        for (final Map.Entry<String, Collection<X>> entriesForOneClass : entriesByFullName.asMap().entrySet()) {
            final String fullClassName = entriesForOneClass.getKey();
            final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            final Collection<X> values = entriesForOneClass.getValue();
            entriesBySimpleName.putAll(simpleClassName, values);
        }
        return entriesBySimpleName;
    }

    /**
     * Copy a multimap to a new map. Useful for constructing Ice-compatible responses from graph requests.
     * @param result a result from a graph operation
     * @return the result transformed to fit a {@code StringLongListMap} API binding
     */
    public static Map<String, List<Long>> copyMultimapForResponse(SetMultimap<String, Long> result) {
        /* if the results object were in terms of IObjectList then this would need IceMapper.map */
        final Map<String, List<Long>> forResponse = new HashMap<String, List<Long>>();
        for (final Map.Entry<String, Collection<Long>> oneClass : result.asMap().entrySet()) {
            final String className = oneClass.getKey();
            final Collection<Long> ids = oneClass.getValue();
            forResponse.put(className, new ArrayList<Long>(ids));
        }
        return forResponse;
    }

    /**
     * Rearrange the deletion targets such that original files are listed before their containing directories.
     * @param session the Hibernate session
     * @param targetObjects the objects that are to be deleted
     * @return the given target objects with any original files suitably ordered for deletion
     */
    static SetMultimap<String, Long> arrangeDeletionTargets(Session session, SetMultimap<String, Long> targetObjects) {
        if (targetObjects.get(OriginalFile.class.getName()).size() < 2) {
            /* no need to rearrange anything, as there are not multiple original files */
            return targetObjects;
        }
        final SetMultimap<String, Long> orderedIds = LinkedHashMultimap.create();
        for (final Map.Entry<String, Collection<Long>> targetObjectsByClass : targetObjects.asMap().entrySet()) {
            final String className = targetObjectsByClass.getKey();
            Collection<Long> ids = targetObjectsByClass.getValue();
            if (OriginalFile.class.getName().equals(className)) {
                final Collection<Collection<Long>> sortedIds = ModelObjectSequencer.sortOriginalFileIds(session, ids);
                ids = new ArrayList<Long>(ids.size());
                for (final Collection<Long> idBatch : sortedIds) {
                    ids.addAll(idBatch);
                }
            }
            orderedIds.putAll(className, ids);
        }
        return orderedIds;
    }

    /**
     * Wrap a graph traversal processor so that it has no write effects.
     * @param processor a graph traversal processor to wrap
     * @return the graph traversal processor wrapped so that it has no write effects
     */
    static Processor disableProcessor(final Processor processor) {
        return new Processor() {

            @Override
            public void nullProperties(String className, String propertyName, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public void deleteInstances(String className, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public void processInstances(String className, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public Set<Ability> getRequiredPermissions() {
                return processor.getRequiredPermissions();
            }

            @Override
            public void assertMayProcess(String className, long id, Details details) throws GraphException {
                processor.assertMayProcess(className, id, details);
            }
        };
    }

    /**
     * Filter the given value to return only objects from the mapping. If a collection type, recurse into filtering the elements.
     * @param mapping a mapping of non-collection elements to replace with others
     * @param value the value to filter, may be {@code null}
     * @return the mapped objects for the objects found in the given value, never {@code null}
     */
    static <X> Set<X> filterComplexValue(Function<Object, X> mapping, Object value) {
        return value == null ? Collections.<X>emptySet() : filterComplexValue(new HashSet<X>(), mapping, value);
    }

    private static <X> Set<X> filterComplexValue(Set<X> sought, Function<Object, X> mapping, Object value) {
        if (value instanceof Iterable) {
            @SuppressWarnings("unchecked")
            final Iterable<Object> iterable = (Iterable<Object>) value;
            for (final Object element : iterable) {
                filterComplexValue(sought, mapping, element);
            }
            return sought;
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<Object, Object> map = (Map<Object, Object>) value;
            for (final Map.Entry<Object, Object> element : map.entrySet()) {
                filterComplexValue(sought, mapping, element.getKey());
                filterComplexValue(sought, mapping, element.getValue());
            }
        } else if (value != null) {
            final X mapped = mapping.apply(value);
            if (mapped != null) {
                sought.add(mapped);
            }
        }
        return sought;
    }

    /**
     * Copy the given value. If a collection type, recurse into copying the elements.
     * @param mapping a mapping of non-collection elements to replace with others in the copy
     * @param value the value to copy, may be {@code null}
     * @return a copy of the value with newly instantiated lists, sets and maps
     */
    static Object copyComplexValue(Function<Object, ? extends Object> mapping, Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Object> original = (List<Object>) value;
            final List<Object> copy = new ArrayList<Object>(original.size());
            for (final Object element : original) {
                copy.add(copyComplexValue(mapping, element));
            }
            return copy;
        } else if (value instanceof Set) {
            @SuppressWarnings("unchecked")
            final Set<Object> original = (Set<Object>) value;
            final Set<Object> copy = new HashSet<Object>();
            for (final Object element : original) {
                copy.add(copyComplexValue(mapping, element));
            }
            return copy;
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<Object, Object> original = (Map<Object, Object>) value;
            final Map<Object, Object> copy = new HashMap<Object, Object>();
            for (final Map.Entry<Object, Object> element : original.entrySet()) {
                copy.put(copyComplexValue(mapping, element.getKey()), copyComplexValue(mapping, element.getValue()));
            }
            return copy;
        } else {
            final Object mapped = mapping.apply(value);
            return mapped == null ? value : mapped;
        }
    }

    /**
     * Create a human-readable view of graph request parameters and results to be logged for debugging.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.1
     */
    static class ParameterReporter {

        private final List<String> parameters = new ArrayList<String>();

        /**
         * Sort a collection only if it is a {@link List} of {@link Comparable} elements.
         * @param collection the collection to sort
         */
        private static void possiblySort(Collection<Object> collection) {
            if (!(collection instanceof List)) {
                return;
            }
            final List<Comparable<Object>> comparableElements = new ArrayList<Comparable<Object>>(collection.size());
            for (Object element : collection) {
                if (element instanceof Comparable) {
                    comparableElements.add((Comparable<Object>) element);
                } else {
                    return;
                }
            }
            Collections.sort(comparableElements);
            collection.clear();
            for (final Comparable<Object> comparableElement : comparableElements) {
                collection.add(comparableElement);
            }
        }

        /**
         * Add a parameter to those to be reported.
         * @param name the parameter name
         * @param values the parameter value
         */
        void addParameter(String name, Collection<Object> values) {
            if (CollectionUtils.isEmpty(values)) {
                return;
            }
            possiblySort(values);
            final List<String> elements = new ArrayList<String>();
            for (Object element : values) {
                if (element instanceof ChildOption) {
                    final ChildOption childOption = (ChildOption) element;
                    final StringBuilder sb =  new StringBuilder();
                    sb.append('<');
                    final ParameterReporter arguments = new ParameterReporter();
                    arguments.addParameter("includeType", childOption.includeType);
                    arguments.addParameter("excludeType", childOption.excludeType);
                    arguments.addParameter("includeNs", childOption.includeNs);
                    arguments.addParameter("excludeNs", childOption.excludeNs);
                    sb.append(arguments);
                    sb.append('>');
                    element = sb;
                }
                elements.add(element.toString());
            }
            parameters.add(name + " = [" + Joiner.on(',').join(elements) + "]");
        }

        /**
         * Add a parameter to those to be reported.
         * @param name the parameter name
         * @param values the parameter value
         */
        void addParameter(String name, Map<String, List<Long>> values) {
            if (MapUtils.isEmpty(values)) {
                return;
            }
            final StringBuilder sb =  new StringBuilder();
            sb.append(name);
            sb.append(" = ");
            sb.append('{');
            boolean needComma = false;
            if (!(values instanceof SortedMap)) {
                values = new TreeMap<String, List<Long>>(values);
            }
            for (final Map.Entry<String, List<Long>> oneClass : values.entrySet()) {
                if (needComma) {
                    sb.append(',');
                } else {
                    needComma = true;
                }
                sb.append(oneClass.getKey());
                sb.append(':');
                final List<Long> ids = oneClass.getValue();
                Collections.sort(ids);
                sb.append('[');
                sb.append(Joiner.on(',').join(ids));
                sb.append(']');
            }
            sb.append('}');
            parameters.add(sb.toString());
        }

        /**
         * Add a parameter to those to be reported.
         * @param name the parameter name
         * @param values the parameter value
         */
        void addParameter(String name, Object value) {
            if (value instanceof Collection) {
                addParameter(name, (Collection<Object>) value);
            } else if (value instanceof Map) {
                this.addParameter(name, (Map<String, List<Long>>) value);
            } else if (value != null) {
                parameters.add(name + " = " + value);
            }
        }

        @Override
        public String toString() {
            return Joiner.on(", ").join(parameters);
        }
    }

    /**
     * Construct a predicate to test if an object's class is of any of the given classes.
     * @param matchTypes model object classes
     * @return a predicate for testing membership of those classes
     */
    public static Predicate<Class<? extends IObject>> getPredicateFromClasses(final Iterable<Class<? extends IObject>> matchTypes) {
        return new Predicate<Class<? extends IObject>>() {
            @Override
            public boolean apply(Class<? extends IObject> subjectType) {
                for (final Class<? extends IObject> matchType : matchTypes) {
                    if (matchType.isAssignableFrom(subjectType)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
