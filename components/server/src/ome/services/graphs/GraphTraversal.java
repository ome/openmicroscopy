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

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ome.model.IObject;
import ome.services.graphs.GraphPathBean.PropertyKind;
import ome.services.graphs.GraphPolicy.Action;
import ome.services.graphs.GraphPolicy.Details;
import ome.services.graphs.GraphPolicy.Orphan;

/**
 * An experimental re-implementation of graph traversal functionality.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public class GraphTraversal {

    private static final Logger log = LoggerFactory.getLogger(GraphTraversal.class);

    /* all bulk operations are batched; this size should be suitable for IN (:ids) for HQL */
    private static final int BATCH_SIZE = 256;

    /**
     * A tuple noting the state of a mapped object instance in the current graph traversal.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static final class DetailsWithCI extends Details {
        /* more useful than IObject for equals and hashCode */
        private final CI subjectAsCI;

        /**
         * Construct a note of an object and its details.
         * {@link #equals(Object)} and {@link #hashCode()} consider only the subject, not the action or orphan.
         * @param subject the object whose details these are
         * @param action the current plan for the object
         * @param orphan the current <q>orphan</q> state of the object
         */
        DetailsWithCI(IObject subject, Action action, Orphan orphan) {
            super(subject, action, orphan);
            this.subjectAsCI = new CI(subject);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof DetailsWithCI) {
                final DetailsWithCI other = (DetailsWithCI) object;
                return this.subjectAsCI.equals(other.subjectAsCI);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), subjectAsCI);
        }

        @Override
        public String toString() {
            return subjectAsCI + "/" + (action == Action.EXCLUDE ? orphan : action);
        }
    }

    /* The tuples immediately below could be elaborately related by a variety of interfaces and builders
     * but their usage does not justify such effort. */

    /**
     * An immutable tuple of class name, instance ID.
     * Within this class, equality and hash code is determined wholly by these values.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static final class CI {
        final String className;
        final long id;

        /**
         * Construct an instance with the given field values.
         * @param className a class name
         * @param id an instance ID
         */
        CI(String className, long id) {
            this.className = className;
            this.id = id;
        }

        /**
         * Construct an instance corresponding to the given object.
         * @param object a Hibernate proxy for a mapped object instance
         */
        CI(IObject object) {
            this.className = Hibernate.getClass(object).getName();
            this.id = object.getId();
        }

        /**
         * Construct a new {@link IObject}
         * @return an unloaded {@link IObject} corresponding to this {@link CI}
         * @throws GraphException if the {@link IObject} could not be constructed
         */
        IObject toIObject() throws GraphException {
            try {
                final Class<? extends IObject> actualClass = (Class<? extends IObject>) Class.forName(className);
                return actualClass.getConstructor(Long.class, boolean.class).newInstance(id, false);
            } catch (/* TODO Java SE 7 ReflectiveOperation*/Exception e) {
                throw new GraphException(
                        "no invokable constructor for: new " + className + "(Long.valueOf(" + id + "L), false)");
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof CI) {
                final CI other = (CI) object;
                return this.id == other.id &&
                        this.className.equals(other.className);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), className, id);
        }

        @Override
        public String toString() {
            return className + "[" + id + "]";
        }
    }

    /**
     * An immutable tuple of class name, property name.
     * Within this class, equality and hash code is determined wholly by these values.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static final class CP {
        final String className;
        final String propertyName;

        /**
         * Construct an instance with the given field values.
         * @param className a class name
         * @param propertyName a property name
         */
        CP(String className, String propertyName) {
            this.className = className;
            this.propertyName = propertyName;
        }

        /**
         * Construct a {@link CPI} from this {@link CP} and the given instance ID.
         * @param id an instance ID
         * @return a {@link CPI} with the corresponding values
         */
        CPI toCPI(long id) {
            return new CPI(className, propertyName, id);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof CP) {
                final CP other = (CP) object;
                return this.className.equals(other.className) &&
                        this.propertyName.equals(other.propertyName);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), className, propertyName);
        }

        @Override
        public String toString() {
            return(className + "." + propertyName).intern();
        }
    }

    /**
     * An immutable tuple of class name, property name, instance ID.
     * Within this class, equality and hash code is determined wholly by these values.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static final class CPI {
        final String className;
        final String propertyName;
        final long id;

        private CP asCP;

        /**
         * Construct an instance with the given field values.
         * @param className a class name
         * @param propertyName a property name
         * @param id an instance ID
         */
        CPI(String className, String propertyName, long id) {
            this.className = className;
            this.propertyName = propertyName;
            this.id = id;
        }

        /**
         * Construct a {@link CP} from this {@link CPI}.
         * Repeated calls to this method may return the same {@link CP} instance.
         * @param id an instance ID
         * @return a {@link CPI} with the corresponding values
         */
        CP toCP() {
            if (asCP == null) {
                asCP = new CP(className, propertyName);
            }
            return asCP;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof CPI) {
                final CPI other = (CPI) object;
                return this.id == other.id &&
                        this.className.equals(other.className) &&
                        this.propertyName.equals(other.propertyName);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), className, propertyName, id);
        }

        @Override
        public String toString() {
            return className + "[" + id + "]." + propertyName;
        }
    }

    /**
     * The state of the graph traversal. Various rules apply:
     * <ol>
     *   <li>An instance may be in no more than one of {@link #included}, {@link #deleted}, {@link #outside},
     *       {@link #findIfLast} and {@link #foundIfLast}.</li>
     *   <li>An instance may be inserted into {@link #included} or {@link #deleted}
     *       whereupon it is also inserted into {@link #toProcess}.</li>
     *   <li>An instance may be inserted into {@link #outside}
     *       whereupon it is also removed from {@link #toProcess}.</li>
     *   <li>An instance may not be removed from {@link #included} or {@link #deleted}
     *       except to be inserted into {@link #included} or {@link #deleted} or {@link #outside}.</li>
     *   <li>An instance may be in {@link #findIfLast} or {@link #foundIfLast} but not both.</li>
     *   <li>An instance may not be removed from {@link #findIfLast} or {@link #foundIfLast}
     *       except to move between them
     *       whereupon it is also inserted into {@link #toProcess}.</li>
     *   <li>An instance may be inserted into {@link #cached}
     *       whereupon it is also inserted into {@link #toProcess}.</li>
     *   <li>{@link #forwardLinksCached}, {@link #backwardLinksCached}, {@link #befores} and {@link #afters}
     *       contain entries for exactly the instances in {@link #cached}.</li>
     *   <li>An instance may be in {@link #included} or {@link #deleted} only if it is in {@link #cached}.</li>
     *   <li>An instance is inserted into {@link #queue} only once.</li>
     *   <li>{@link #queue} contains exactly the instances that are in {@link #included} or {@link #deleted}.</li>
     * </ol>
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static class Planning {
        /* process state */
        final Set<CI> toProcess = new HashSet<CI>();
        final Set<CI> included = new HashSet<CI>();
        final Set<CI> deleted = new HashSet<CI>();
        final Set<CI> outside = new HashSet<CI>();
        /* orphan checks */
        final Set<CI> findIfLast = new HashSet<CI>();
        final Map<CI, Boolean> foundIfLast = new HashMap<CI, Boolean>();
        /* links */
        final Set<CI> cached = new HashSet<CI>();
        final SetMultimap<CPI, CI> forwardLinksCached = HashMultimap.create();
        final SetMultimap<CPI, CI> backwardLinksCached = HashMultimap.create();
        final SetMultimap<CI, CI> befores = HashMultimap.create();
        final SetMultimap<CI, CI> afters = HashMultimap.create();
        final Map<CI, Set<CI>> blockedBy = new HashMap<CI, Set<CI>>();
    }

    /**
     * Executes the planned operation.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    public interface Processor {

        /**
         * Null the given property of the indicated instances.
         * @param className full name of mapped Hibernate class
         * @param propertyName HQL-style property name of class
         * @param ids applicable instances of class, no more than {@link #BATCH_SIZE}
         */
        void nullProperties(String className, String propertyName, Collection<Long> ids);

        /**
         * Remove elements from the given property's {@link Collection}
         * @param className full name of mapped Hibernate class
         * @param propertyName HQL-style property name of class
         * @param ids applicable instances of class, no more keys than {@link #BATCH_SIZE},
         *        values indicating those to remove from the {@link Collection}
         */
        void filterProperties(String className, String propertyName, Collection<Entry<Long, Collection<Entry<String, Long>>>> ids);

        /**
         * Delete the given instances.
         * @param className full name of mapped Hibernate class
         * @param ids applicable instances of class, no more than {@link #BATCH_SIZE}
         */
        void deleteInstances(String className, Collection<Long> ids);

        /**
         * Process the given instances. They will have been sufficiently unlinked by the other methods.
         * @param className full name of mapped Hibernate class
         * @param ids applicable instances of class, no more than {@link #BATCH_SIZE}
         */
        void processInstances(String className, Collection<Long> ids);
    }

    private final GraphPathBean bean;
    private final Planning planning;
    private final GraphPolicy policy;
    private final Processor processor;

    /**
     * Construct a new instance of a graph traversal manager.
     * @param graphPathBean the graph path bean
     * @param policy how to determine which related objects to include in the operation
     * @param processor how to operate on the resulting target object graph
     */
    public GraphTraversal(GraphPathBean graphPathBean, GraphPolicy policy, Processor processor) {
        this.bean = graphPathBean;
        this.planning = new Planning();
        this.policy = policy;
        this.processor = log.isDebugEnabled() ? debugWrap(processor) : processor;
    }

    /**
     * Traverse model object graph to determine steps for the proposed operation.
     * @param session the Hibernate session to use for HQL queries
     * @param objectInstances the model objects to process, may be unloaded with ID only
     * @return the model objects included in the operation, and the deleted objects, may be unloaded with ID only
     * @throws GraphException if the model objects were not as expected
     */
    public Entry<Collection<IObject>, Collection<IObject>> planOperation(Session session,
            Collection<? extends IObject> objectInstances) throws GraphException {
        /* note the object instances for processing as included objects */
        final SetMultimap<String, Long> objectsToQuery = HashMultimap.create();
        for (final IObject instance : objectInstances) {
            if (instance instanceof HibernateProxy) {
                final CI object = new CI(instance);
                policy.noteDetails(instance, object.className, object.id);
                planning.included.add(object);
            } else {
                objectsToQuery.put(instance.getClass().getName(), instance.getId());
            }
        }
        planning.included.addAll(objectsToCIs(session, objectsToQuery));
        planning.toProcess.addAll(planning.included);
        /* track state to guarantee progress in reprocessing objects whose orphan status is relevant */
        Set<CI> optimisticReprocess = null;
        /* process any pending objects */
        while (!(planning.toProcess.isEmpty() && planning.findIfLast.isEmpty())) {
            /* first process any cached objects that do not await orphan status determination */
            final Set<CI> toProcess = new HashSet<CI>(planning.toProcess);
            toProcess.retainAll(planning.cached);
            toProcess.removeAll(planning.findIfLast);
            if (!toProcess.isEmpty()) {
                if (optimisticReprocess != null && !Sets.difference(planning.toProcess, optimisticReprocess).isEmpty()) {
                    /* processing something beyond optimistic suggestion, so circumstances have changed */
                    optimisticReprocess = null;
                }
                for (final CI nextObject : toProcess) {
                    reviewObject(nextObject);
                }
                continue;
            }
            /* if none of the above exist, then fill the cache */
            final Set<CI> toCache = new HashSet<CI>(planning.toProcess);
            toCache.removeAll(planning.cached);
            if (!toCache.isEmpty()) {
                optimisticReprocess = null;
                cache(session, toCache);
                continue;
            }
            /* try processing the findIfLast in case of any changes */
            if (!planning.toProcess.isEmpty()) {
                final Set<CI> previousToProcess = new HashSet<CI>(planning.toProcess);
                final Set<CI> previousFindIfLast = new HashSet<CI>(planning.findIfLast);
                for (final CI nextObject : previousToProcess) {
                    reviewObject(nextObject);
                }
                /* This condition is tricky. We do want to reprocess objects that are suggested for such, while
                 * avoiding an infinite loop that comes of such processing not resolving any orphan status. */
                if (!Sets.symmetricDifference(previousFindIfLast, planning.findIfLast).isEmpty() ||
                    (optimisticReprocess == null || !Sets.symmetricDifference(planning.toProcess, optimisticReprocess).isEmpty()) &&
                     !Sets.symmetricDifference(previousToProcess, planning.toProcess).isEmpty()) {
                    optimisticReprocess = new HashSet<CI>(planning.toProcess);
                    continue;
                }
            }
            /* if no other processing or caching is needed, then deem outstanding objects orphans */
            optimisticReprocess = null;
            for (final CI orphan : planning.findIfLast) {
                planning.foundIfLast.put(orphan, true);
                if (log.isDebugEnabled()) {
                    log.debug("marked " + orphan + " as " + Orphan.IS_LAST);
                }
            }
            planning.toProcess.addAll(planning.findIfLast);
            planning.findIfLast.clear();
        };
        /* report which objects are to be included in the operation or deleted so that it can proceed */
        final Collection<IObject> included = new ArrayList<IObject>(planning.included.size());
        for (final CI includedObject : planning.included) {
            included.add(includedObject.toIObject());
        }
        final Collection<IObject> deleted = new ArrayList<IObject>(planning.deleted.size());
        for (final CI deletedObject : planning.deleted) {
            deleted.add(deletedObject.toIObject());
        }
        return Maps.immutableEntry(included, deleted);
    }

    /**
     * Convert the indicated objects to {@link CI}s with their actual class identified.
     * @param session a Hibernate session
     * @param objects the objects to query
     * @return {@link CI}s corresponding to the objects
     */
    private Collection<CI> objectsToCIs(Session session, Multimap<String, Long> objects) {
        final List<CI> returnValue = new ArrayList<CI>(objects.size());
        for (final Entry<String, Collection<Long>> oneQueryClass : objects.asMap().entrySet()) {
            final String queryClassName = oneQueryClass.getKey();
            final String query = "FROM " + queryClassName + " WHERE id IN (:ids)";
            for (final List<Long> ids : Iterables.partition(oneQueryClass.getValue(), BATCH_SIZE)) {
                for (final Object proxy : session.createQuery(query).setParameterList("ids", ids).list()) {
                    final IObject instance = (IObject) proxy;
                    final CI object = new CI(instance);
                    policy.noteDetails(instance, object.className, object.id);
                    returnValue.add(object);
                }
            }
        }
        return returnValue;
    }

    /**
     * Load object instances and their links into the various cache fields of {@link Planning}.
     * @param session a Hibernate session
     * @param toCache the objects to cache
     */
    private void cache(Session session, Collection<CI> toCache) {
        /* note which links to query, organized for batch querying */
        final SetMultimap<CP, Long> forwardLinksWanted = HashMultimap.create();
        final SetMultimap<CP, Long> backwardLinksWanted = HashMultimap.create();
        for (final CI inclusionCandidate : toCache) {
            for (final String inclusionCandidateSuperclassName : bean.getSuperclassesOfReflexive(inclusionCandidate.className)) {
                for (final Entry<String, String> forwardLink : bean.getLinkedTo(inclusionCandidateSuperclassName)) {
                    final CP linkProperty = new CP(inclusionCandidateSuperclassName, forwardLink.getValue());
                    forwardLinksWanted.put(linkProperty, inclusionCandidate.id);
                }
                for (final Entry<String, String> backwardLink : bean.getLinkedBy(inclusionCandidateSuperclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    backwardLinksWanted.put(linkProperty, inclusionCandidate.id);
                }
            }
        }
        /* query and cache forward links */
        for (final Entry<CP, Collection<Long>> forwardLink : forwardLinksWanted.asMap().entrySet()) {
            final CP linkProperty = forwardLink.getKey();
            final boolean propertyIsAccessible = bean.isPropertyAccessible(linkProperty.className, linkProperty.propertyName);
            final String query = "SELECT linker, linked FROM " + linkProperty.className + " AS linker " +
                    "JOIN linker." + linkProperty.propertyName + " AS linked WHERE linker.id IN (:ids)";
            for (final List<Long> ids : Iterables.partition(forwardLink.getValue(), BATCH_SIZE)) {
                for (final Object[] resultRow : (List<Object[]>) session.createQuery(query).setParameterList("ids", ids).list()) {
                    final IObject linkerInstance = (IObject) resultRow[0];
                    final IObject linkedInstance = (IObject) resultRow[1];
                    final CI linker = new CI(linkerInstance);
                    final CI linked = new CI(linkedInstance);
                    policy.noteDetails(linkedInstance, linked.className, linked.id);
                    planning.forwardLinksCached.put(linkProperty.toCPI(linker.id), linked);
                    if (propertyIsAccessible) {
                        planning.befores.put(linked, linker);
                        planning.afters.put(linker, linked);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(linkProperty.toCPI(linker.id) + " links to " + linked);
                    }
                }
            }
        }
        /* query and cache backward links */
        for (final Entry<CP, Collection<Long>> backwardLink : backwardLinksWanted.asMap().entrySet()) {
            final CP linkProperty = backwardLink.getKey();
            final boolean propertyIsAccessible = bean.isPropertyAccessible(linkProperty.className, linkProperty.propertyName);
            final String query = "SELECT linker, linked FROM " + linkProperty.className + " AS linker " +
                    "JOIN linker." + linkProperty.propertyName + " AS linked WHERE linked.id IN (:ids)";
            for (final List<Long> ids : Iterables.partition(backwardLink.getValue(), BATCH_SIZE)) {
                for (final Object[] resultRow : (List<Object[]>) session.createQuery(query).setParameterList("ids", ids).list()) {
                    final IObject linkerInstance = (IObject) resultRow[0];
                    final IObject linkedInstance = (IObject) resultRow[1];
                    final CI linker = new CI(linkerInstance);
                    final CI linked = new CI(linkedInstance);
                    policy.noteDetails(linkerInstance, linker.className, linker.id);
                    planning.backwardLinksCached.put(linkProperty.toCPI(linked.id), linker);
                    if (propertyIsAccessible) {
                        planning.befores.put(linked, linker);
                        planning.afters.put(linker, linked);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(linkProperty.toCPI(linker.id) + " links to " + linked);
                    }
                }
            }
        }
        /* note cached objects for further processing */
        planning.cached.addAll(toCache);
        planning.toProcess.addAll(toCache);
    }

    /**
     * Invalidate {@link Orphan#IS_NOT_LAST} for objects linked to one no longer {@link Action#EXCLUDE}d.
     * @param object the object that is no longer {@link Action#EXCLUDE}d
     */
    private void orphanCheckNoLongerExcluded(CI object) {
        for (final String superclassName : bean.getSuperclassesOfReflexive(object.className)) {
            for (final Entry<String, String> forwardLink : bean.getLinkedTo(superclassName)) {
                /* next forward link */
                final CPI linkSource = new CPI (superclassName, forwardLink.getValue(), object.id);
                for (final CI linked : planning.forwardLinksCached.get(linkSource)) {
                    /* next object linked by this one */
                    if (Boolean.FALSE.equals(planning.foundIfLast.get(linked))) {
                        planning.findIfLast.add(linked);
                        planning.foundIfLast.remove(linked);
                        planning.toProcess.add(linked);
                    }
                }
            }
            for (final Entry<String, String> backwardLink : bean.getLinkedBy(superclassName)) {
                /* next backward link */
                final CPI linkTarget = new CPI (backwardLink.getKey(), backwardLink.getValue(), object.id);
                for (final CI linker : planning.backwardLinksCached.get(linkTarget)) {
                    /* next object this one links */
                    if (Boolean.FALSE.equals(planning.foundIfLast.get(linker))) {
                        planning.findIfLast.add(linker);
                        planning.foundIfLast.remove(linker);
                        planning.toProcess.add(linker);
                    }
                }
            }
        }
    }

    /**
     * Determine the appropriate value of {@link Action} for the given object.
     * @param object an object
     * @return the object's {@link Action} value
     */
    private Action getAction(CI object) {
        if (planning.included.contains(object)) {
            return Action.INCLUDE;
        } else if (planning.deleted.contains(object)) {
            return Action.DELETE;
        } else if (planning.outside.contains(object)) {
            return Action.OUTSIDE;
        } else {
            return Action.EXCLUDE;
        }
    }

    /**
     * Determine the appropriate value of {@link Orphan} for the given object.
     * @param object an object
     * @return the object's {@link Orphan} value
     */
    private Orphan getOrphan(CI object) {
        if (planning.findIfLast.contains(object)) {
            return Orphan.RELEVANT;
        }
        final Boolean isLast = planning.foundIfLast.get(object);
        if (isLast == null) {
            return Orphan.IRRELEVANT;
        } else {
            return isLast ? Orphan.IS_LAST : Orphan.IS_NOT_LAST;
        }
    }

    /**
     * Return details of the given model object.
     * Repeated queries for the same model object return exactly the same details object as previously.
     * @param cache the cache of details by object
     * @param object an object
     * @return the details of the object
     * @throws GraphException if the object could not be constructed as an {@link IObject}
     */
    private Details getDetails(Map<CI, Details> cache, CI object) throws GraphException {
        Details details = cache.get(object);
        if (details == null) {
            final Action linkerAction = getAction(object);
            final Orphan linkerIsOrphan = linkerAction == Action.EXCLUDE ?
                    getOrphan(object) : Orphan.IRRELEVANT;
            details =  new DetailsWithCI(object.toIObject(), linkerAction, linkerIsOrphan);
            cache.put(object, details);
        }
        return details;
    }

    /**
     * Process the object, adjusting the planning state accordingly.
     * @param object an object
     * @throws GraphException on detecting the policy attempting an illegal change of {@link Action}
     */
    private void reviewObject(CI object) throws GraphException {
        /* note the object's details */
        final Map<CI, Details> detailsCache = new HashMap<CI, Details>();
        final Details objectDetails = getDetails(detailsCache, object);
        if (log.isDebugEnabled()) {
            log.debug("reviewing " + objectDetails);
        }
        /* review the object's links */
        final Map<String, Set<Details>> linkedFromDetails = new HashMap<String, Set<Details>>();
        final Map<String, Set<Details>> linkedToDetails   = new HashMap<String, Set<Details>>();
        final Set<String> notNullable = new HashSet<String>();
        for (final String superclassName : bean.getSuperclassesOfReflexive(object.className)) {
            for (final Entry<String, String> forwardLink : bean.getLinkedTo(superclassName)) {
                /* next forward link */
                final CP linkProperty = new CP(superclassName, forwardLink.getValue());
                if (bean.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.REQUIRED) {
                    notNullable.add(linkProperty.toString());
                }
                final Set<Details> linkedsDetails = new HashSet<Details>();
                linkedToDetails.put(linkProperty.toString(), linkedsDetails);
                final CPI linkSource = linkProperty.toCPI(object.id);
                for (final CI linked : planning.forwardLinksCached.get(linkSource)) {
                    /* next object linked by this one */
                    linkedsDetails.add(getDetails(detailsCache, linked));
                }
            }
            for (final Entry<String, String> backwardLink : bean.getLinkedBy(superclassName)) {
                /* next backward link */
                final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                if (bean.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.REQUIRED) {
                    notNullable.add(linkProperty.toString());
                }
                final Set<Details> linkersDetails = new HashSet<Details>();
                linkedFromDetails.put(linkProperty.toString(), linkersDetails);
                final CPI linkTarget = linkProperty.toCPI(object.id);
                for (final CI linker : planning.backwardLinksCached.get(linkTarget)) {
                    /* next object this one links */
                    linkersDetails.add(getDetails(detailsCache, linker));
                }
            }
        }
        final Set<Details> changes = policy.review(linkedFromDetails, objectDetails, linkedToDetails, notNullable);
        /* object is now processed */
        planning.toProcess.remove(object);
        if (changes == null) {
            return;
        }
        /* act on collated policies */
        for (final Details change : changes) {
            final CI instance = new CI(change.subject);
            final Action previousAction = getAction(instance);
            if (previousAction != change.action) {
                /* undo previous action */
                switch (previousAction) {
                case EXCLUDE:
                    /* query orphan status only for EXCLUDEd objects */
                    planning.findIfLast.remove(instance);
                    planning.foundIfLast.remove(instance);
                    /* re-check objects whose IS_NOT_LAST may have depended on this object being excluded */
                    orphanCheckNoLongerExcluded(instance);
                    break;
                case DELETE:
                    planning.deleted.remove(instance);
                    break;
                default:
                    throw new GraphException("policy cannot change action from " + previousAction);
                }
                /* accept new action */
                switch (change.action) {
                case DELETE:
                    planning.deleted.add(instance);
                    planning.toProcess.add(instance);
                    break;
                case INCLUDE:
                    planning.included.add(instance);
                    planning.toProcess.add(instance);
                    break;
                case OUTSIDE:
                    planning.outside.add(instance);
                    planning.toProcess.remove(instance);
                    break;
                default:
                    throw new GraphException("policy cannot change action to " + change.action);
                }
            } else if ((change.orphan == Orphan.IS_LAST || change.orphan == Orphan.IS_NOT_LAST) &&
                    planning.findIfLast.remove(instance)) {
                /* relevant orphan status now determined so object must be processed */
                planning.foundIfLast.put(instance, change.orphan == Orphan.IS_LAST);
                planning.toProcess.add(object);
            } else if (change.action == Action.EXCLUDE && change.orphan == Orphan.RELEVANT &&
                    planning.findIfLast.add(instance) && !planning.cached.contains(instance)) {
                /* orphan status is relevant; if just now noted as such then ensure the object is or will be cached */
                planning.toProcess.add(instance);
            } else if (!(change.action == Action.OUTSIDE || instance.equals(object))) {
                /* probably just needs review */
                planning.toProcess.add(instance);
            }
            if (log.isDebugEnabled()) {
                log.debug("adjusted " + change);
            }
        }
        /* if object is now DELETE or INCLUDE then it must be in the queue */
        final Action chosenAction = getAction(object);
        if ((chosenAction == Action.DELETE || chosenAction == Action.INCLUDE) && !planning.blockedBy.containsKey(object)) {
            final Set<CI> queuedItems = planning.blockedBy.keySet();
            planning.blockedBy.put(object, new HashSet<CI>(Sets.intersection(planning.befores.get(object), queuedItems)));
            for (final CI afterItem : Sets.intersection(planning.afters.get(object), queuedItems)) {
                planning.blockedBy.get(afterItem).add(object);
            }
        }
    }

    /**
     * Note a linked object to remove from a linker property's {@link Collection} value.
     * @param linkerToIdToLinked the map from linker property to linker ID to objects in {@link Collection}s
     * @param linker the linker object
     * @param linked the linked object
     */
    private void addRemoval(Map<CP, SetMultimap<Long, Entry<String, Long>>> linkerToIdToLinked, CPI linker, CI linked) {
        if (bean.isPropertyAccessible(linker.className, linker.propertyName)) {
            SetMultimap<Long, Entry<String, Long>> idMap = linkerToIdToLinked.get(linker.toCP());
            if (idMap == null) {
                idMap = HashMultimap.create();
                linkerToIdToLinked.put(linker.toCP(), idMap);
            }
            idMap.put(linker.id, Maps.immutableEntry(linked.className, linked.id));
        }
    }

    /**
     * Create a processor proxy that logs method calls and arguments at debug level.
     * Object IDs may be rearranged to be in ascending order to aid readability.
     * @param processor the processor to wrap
     * @return the wrapped processor
     */
    private static Processor debugWrap(final Processor processor) {
        return new Processor() {
            @Override
            public void nullProperties(String className, String propertyName, Collection<Long> ids) {
                if (!(ids instanceof SortedSet)) {
                    ids = new TreeSet<Long>(ids);
                }
                if (log.isDebugEnabled()) {
                    log.debug("processor: null " + className + "[" + Joiner.on(',').join(ids) + "]." + propertyName);
                }
                processor.nullProperties(className, propertyName, ids);
            }

            @Override
            public void filterProperties(String className, String propertyName,
                    Collection<Entry<Long, Collection<Entry<String, Long>>>> ids) {
                final SortedMap<Long, Collection<Entry<String, Long>>> idMap = new TreeMap<Long, Collection<Entry<String, Long>>>();
                for (final Entry<Long, Collection<Entry<String, Long>>> entry : ids) {
                    idMap.put(entry.getKey(), entry.getValue());
                }
                ids = idMap.entrySet();
                if (log.isDebugEnabled()) {
                    final StringBuffer sb = new StringBuffer();
                    sb.append("processor: collection removal from " + className + "." + propertyName + ":");
                    for (final Entry<Long, Collection<Entry<String, Long>>> removal : ids) {
                        sb.append(" from [" + removal.getKey() + "] remove");
                        for (final Entry<String, Long> target : removal.getValue()) {
                            sb.append(" " + target.getKey() + "[" + target.getValue() + "]");
                        }
                    }
                    log.debug(sb.toString());
                }
                processor.filterProperties(className, propertyName, ids);
            }

            @Override
            public void deleteInstances(String className, Collection<Long> ids) {
                if (!(ids instanceof SortedSet)) {
                    ids = new TreeSet<Long>(ids);
                }
                if (log.isDebugEnabled()) {
                    log.debug("processor: delete " + className + "[" + Joiner.on(',').join(ids) + "]");
                }
                processor.deleteInstances(className, ids);
            }

            @Override
            public void processInstances(String className, Collection<Long> ids) {
                if (!(ids instanceof SortedSet)) {
                    ids = new TreeSet<Long>(ids);
                }
                if (log.isDebugEnabled()) {
                    log.debug("processor: process " + className + "[" + Joiner.on(',').join(ids) + "]");
                }
                processor.processInstances(className, ids);
            }
        };
    }

    /**
     * Remove links between the targeted model objects and the remainder of the model object graph.
     */
    public void unlinkTargets() {
        /* accumulate plan for unlinking included/deleted from others */
        final SetMultimap<CP, Long> toNullByCP = HashMultimap.create();
        final Map<CP, SetMultimap<Long, Entry<String, Long>>> linkerToIdToLinked =
                new HashMap<CP, SetMultimap<Long, Entry<String, Long>>>();
        for (final CI object : planning.included) {
            for (final String superclassName : bean.getSuperclassesOfReflexive(object.className)) {
                for (final Entry<String, String> forwardLink : bean.getLinkedTo(superclassName)) {
                    final CP linkProperty = new CP(superclassName, forwardLink.getValue());
                    final boolean isCollection =
                            bean.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
                    final CPI linkSource = linkProperty.toCPI(object.id);
                    for (final CI linked : planning.forwardLinksCached.get(linkSource)) {
                        final Action linkedAction = getAction(linked);
                        if (!(linkedAction == Action.INCLUDE || linkedAction == Action.OUTSIDE)) {
                            /* INCLUDE is linked to EXCLUDE or DELETE, so unlink */
                            if (isCollection) {
                                addRemoval(linkerToIdToLinked, linkProperty.toCPI(object.id), linked);
                            } else {
                                toNullByCP.put(linkProperty, object.id);
                            }
                        }
                    }
                }
                for (final Entry<String, String> backwardLink : bean.getLinkedBy(superclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    final boolean isCollection =
                            bean.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
                    final CPI linkTarget = linkProperty.toCPI(object.id);
                    for (final CI linker : planning.backwardLinksCached.get(linkTarget)) {
                        final Action linkerAction = getAction(linker);
                        if (linkerAction == Action.EXCLUDE) {
                            /* EXCLUDE is linked to INCLUDE, so unlink */
                            if (isCollection) {
                                addRemoval(linkerToIdToLinked, linkProperty.toCPI(linker.id), object);
                            } else {
                                toNullByCP.put(linkProperty, linker.id);
                            }
                        }
                    }
                }
            }
        }
        for (final CI object : planning.deleted) {
            for (final String superclassName : bean.getSuperclassesOfReflexive(object.className)) {
                for (final Entry<String, String> backwardLink : bean.getLinkedBy(superclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    final boolean isCollection =
                            bean.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
                    final CPI linkTarget = linkProperty.toCPI(object.id);
                    for (final CI linker : planning.backwardLinksCached.get(linkTarget)) {
                        final Action linkerAction = getAction(linker);
                        if (linkerAction != Action.DELETE) {
                            /* EXCLUDE, INCLUDE or OUTSIDE is linked to DELETE, so unlink */
                            if (isCollection) {
                                addRemoval(linkerToIdToLinked, linkProperty.toCPI(linker.id), object);
                            } else {
                                toNullByCP.put(linkProperty, linker.id);
                            }
                        }
                    }
                }
            }
        }
        /* unlink included/deleted by nulling properties */
        for (final Entry<CP, Collection<Long>> nullCurr : toNullByCP.asMap().entrySet()) {
            final CP fromCP = nullCurr.getKey();
            for (final List<Long> ids : Iterables.partition(nullCurr.getValue(), BATCH_SIZE)) {
                processor.nullProperties(fromCP.className, fromCP.propertyName, ids);
            }
        }
        /* unlink included/deleted by removing from collections */
        for (final Entry<CP, SetMultimap<Long, Entry<String, Long>>> removeCurr : linkerToIdToLinked.entrySet()) {
            final CP linker = removeCurr.getKey();
            for (final List<Entry<Long, Collection<Entry<String, Long>>>> idMap :
                Iterables.partition(removeCurr.getValue().asMap().entrySet(), BATCH_SIZE)) {
                processor.filterProperties(linker.className, linker.propertyName, idMap);
            }
        }
    }

    /**
     * Process the targeted model objects.
     * @param planning the state of operation planning
     * @param processor how to operate on the resulting target object graph
     * @throws GraphException if a cycle is detected in the model object graph
     */
    public void processTargets() throws GraphException {
        /* process the targets forward across links */
        while (!planning.blockedBy.isEmpty()) {
            /* determine which objects can be processed in this step */
            final Collection<CI> nowUnblocked = new ArrayList<CI>();
            final Iterator<Entry<CI, Set<CI>>> blocks = planning.blockedBy.entrySet().iterator();
            while (blocks.hasNext()) {
                final Entry<CI, Set<CI>> block = blocks.next();
                final CI object = block.getKey();
                if (block.getValue().isEmpty()) {
                    blocks.remove();
                    nowUnblocked.add(object);
                }
            }
            if (nowUnblocked.isEmpty()) {
                throw new GraphException("cycle detected among " + planning.blockedBy.keySet());
            }
            for (final Set<CI> blockers : planning.blockedBy.values()) {
                blockers.removeAll(nowUnblocked);
            }
            final SetMultimap<String, Long> toJoin = HashMultimap.create();
            final SetMultimap<String, Long> toDelete = HashMultimap.create();
            for (final CI object : nowUnblocked) {
                if (planning.included.contains(object)) {
                    toJoin.put(object.className, object.id);
                } else {
                    toDelete.put(object.className, object.id);
                }
            }
            /* perform this group's deletes */
            if (!toDelete.isEmpty()) {
                for (final Entry<String, Collection<Long>> oneClassToDelete : toDelete.asMap().entrySet()) {
                    final String className = oneClassToDelete.getKey();
                    for (final List<Long> ids : Iterables.partition(oneClassToDelete.getValue(), BATCH_SIZE)) {
                        processor.deleteInstances(className, ids);
                    }
                }
            }
            /* perform this group's includes */
            if (!toJoin.isEmpty()) {
                for (final Entry<String, Collection<Long>> oneClassToJoin : toJoin.asMap().entrySet()) {
                    final String className = oneClassToJoin.getKey();
                    for (final List<Long> ids : Iterables.partition(oneClassToJoin.getValue(), BATCH_SIZE)) {
                        processor.processInstances(className, ids);
                    }
                }
            }
        }
    }
}
