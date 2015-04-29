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

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.graphs.GraphPathBean.PropertyKind;
import ome.services.graphs.GraphPolicy.Ability;
import ome.services.graphs.GraphPolicy.Action;
import ome.services.graphs.GraphPolicy.Details;
import ome.services.graphs.GraphPolicy.Orphan;
import ome.system.EventContext;

/**
 * An alternative implementation of model object graph traversal, relying on SELECTing in advance for making decisions,
 * instead of rolling back to savepoints to recover from failed attempts to act.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphTraversal {

    private static final Logger log = LoggerFactory.getLogger(GraphTraversal.class);

    /* all bulk operations are batched; this size should be suitable for IN (:ids) for HQL */
    private static final int BATCH_SIZE = 256;

    /**
     * A tuple noting the state of a mapped object instance in the current graph traversal.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static final class DetailsWithCI extends Details {
        /* more useful than IObject for equals and hashCode */
        private final CI subjectAsCI;

        /**
         * Construct a note of an object and its details.
         * {@link #equals(Object)} and {@link #hashCode()} consider only the subject, not the action or orphan.
         * @param subject the object whose details these are
         * @param ownerId the ID of the object's owner
         * @param groupId the ID of the object's group
         * @param action the current plan for the object
         * @param orphan the current <q>orphan</q> state of the object
         * @param mayUpdate if the object may be updated
         * @param mayDelete if the object may be deleted
         * @param mayChmod if the object may have its permissions changed
         * @param isOwner if the user owns the object
         * @param isCheckPermissions if the user is expected to have the permissions required to process the object
         */
        DetailsWithCI(IObject subject, Long ownerId, Long groupId, Action action, Orphan orphan,
                boolean mayUpdate, boolean mayDelete, boolean mayChmod, boolean isOwner, boolean isCheckPermissions) {
            super(subject, ownerId, groupId, action, orphan, mayUpdate, mayDelete, mayChmod, isOwner, isCheckPermissions);
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
            final StringBuffer sb = new StringBuffer();
            sb.append(subjectAsCI);
            sb.append('/');
            sb.append(action == Action.EXCLUDE ? orphan : action);
            if (!isCheckPermissions) {
                sb.append('*');
            }
            return sb.toString();
        }
    }

    /* The tuples immediately below could be elaborately related by a variety of interfaces and builders
     * but their usage does not justify such effort. */

    /**
     * An immutable tuple of class name, instance ID.
     * Within this class, equality and hash code is determined wholly by these values.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
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
         * @param object a persisted object instance
         */
        CI(IObject object) {
            if (object instanceof HibernateProxy) {
                this.className = Hibernate.getClass(object).getName();
            } else {
                this.className = object.getClass().getName();
            }
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
                        "no invocable constructor for: new " + className + "(Long.valueOf(" + id + "L), false)");
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
     * @since 5.1.0
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
     * @since 5.1.0
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
     * @since 5.1.0
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
        final Map<CI, CI> aliases = new HashMap<CI, CI>();
        final Set<CI> cached = new HashSet<CI>();
        final SetMultimap<CPI, CI> forwardLinksCached = HashMultimap.create();
        final SetMultimap<CPI, CI> backwardLinksCached = HashMultimap.create();
        final SetMultimap<CI, CI> befores = HashMultimap.create();
        final SetMultimap<CI, CI> afters = HashMultimap.create();
        final Map<CI, Set<CI>> blockedBy = new HashMap<CI, Set<CI>>();
        /* permissions, unused for system users */
        final Map<CI, ome.model.internal.Details> detailsNoted = new HashMap<CI, ome.model.internal.Details>();
        final Set<CI> mayUpdate = new HashSet<CI>();
        final Set<CI> mayDelete = new HashSet<CI>();
        final Set<CI> mayChmod = new HashSet<CI>();
        final Set<CI> owns = new HashSet<CI>();
        final Set<CI> overrides = new HashSet<CI>();
    }

    /**
     * Executes the planned operation.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
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
         * Delete the given instances.
         * @param className full name of mapped Hibernate class
         * @param ids applicable instances of class, no more than {@link #BATCH_SIZE}
         * @throws GraphException if not all the instances could be deleted
         */
        void deleteInstances(String className, Collection<Long> ids) throws GraphException;

        /**
         * Process the given instances. They will have been sufficiently unlinked by the other methods.
         * @param className full name of mapped Hibernate class
         * @param ids applicable instances of class, no more than {@link #BATCH_SIZE}
         * @throws GraphException if not all the instances could be processed
         */
        void processInstances(String className, Collection<Long> ids) throws GraphException;

        /**
         * @return the permissions required for processing instances with {@link #processInstances(String, Collection)}
         */
        Set<Ability> getRequiredPermissions();

        /**
         * Assert that an object with the given details may be processed. Called only if the user is not an administrator.
         * @param className the name of the object's class
         * @param id the ID of the object
         * @param details the object's details
         * @throws GraphException if the object may not be processed
         */
        void assertMayProcess(String className, long id, ome.model.internal.Details details) throws GraphException;
    }

    private final Session session;
    private final EventContext eventContext;
    private final ACLVoter aclVoter;
    private final SystemTypes systemTypes;
    private final GraphPathBean model;
    private final SetMultimap<String, String> unnullable;
    private final Planning planning;
    private final GraphPolicy policy;
    private final Processor processor;

    /**
     * Construct a new instance of a graph traversal manager.
     * @param session the Hibernate session
     * @param eventContext the current event context
     * @param aclVoter ACL voter for permissions checking
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     * @param policy how to determine which related objects to include in the operation
     * @param processor how to operate on the resulting target object graph
     */
    public GraphTraversal(Session session, EventContext eventContext, ACLVoter aclVoter, SystemTypes systemTypes,
            GraphPathBean graphPathBean, SetMultimap<String, String> unnullable, GraphPolicy policy, Processor processor) {
        this.session = session;
        this.eventContext = eventContext;
        this.aclVoter = aclVoter;
        this.systemTypes = systemTypes;
        this.model = graphPathBean;
        this.unnullable = unnullable;
        this.planning = new Planning();
        this.policy = policy;
        this.processor = log.isDebugEnabled() ? debugWrap(processor) : processor;
    }

    /**
     * Traverse model object graph to determine steps for the proposed operation.
     * @param session the Hibernate session to use for HQL queries
     * @param objects the model objects to process
     * @param if the given model objects are to be included (instead of just deleted)
     * @return the model objects included in the operation, and the deleted objects
     * @throws GraphException if the model objects were not as expected
     */
    public Entry<SetMultimap<String, Long>, SetMultimap<String, Long>> planOperation(Session session,
            SetMultimap<String, Long> objects, boolean include) throws GraphException {
        final Set<CI> targetSet = include ? planning.included : planning.deleted;
        /* note the object instances for processing */
        targetSet.addAll(objectsToCIs(session, objects));
        /* actually do the planning of the operation */
        planning.toProcess.addAll(targetSet);
        planOperation(session);
        /* report which objects are to be included in the operation or deleted so that it can proceed */
        final SetMultimap<String, Long> included = HashMultimap.create();
        for (final CI includedObject : planning.included) {
            included.put(includedObject.className, includedObject.id);
        }
        final SetMultimap<String, Long> deleted = HashMultimap.create();
        for (final CI deletedObject : planning.deleted) {
            deleted.put(deletedObject.className, deletedObject.id);
        }
        return Maps.immutableEntry(included, deleted);
    }

    /**
     * Traverse model object graph to determine steps for the proposed operation.
     * @param session the Hibernate session to use for HQL queries
     * @param objectInstances the model objects to process, may be unloaded with ID only
     * @param if the given model objects are to be included (instead of just deleted)
     * @return the model objects included in the operation, and the deleted objects, may be unloaded with ID only
     * @throws GraphException if the model objects were not as expected
     */
    public Entry<Collection<IObject>, Collection<IObject>> planOperation(Session session,
            Collection<? extends IObject> objectInstances, boolean include) throws GraphException {
        final Set<CI> targetSet = include ? planning.included : planning.deleted;
        /* note the object instances for processing */
        final SetMultimap<String, Long> objectsToQuery = HashMultimap.create();
        for (final IObject instance : objectInstances) {
            if (instance.isLoaded() && instance.getDetails() != null) {
                final CI object = new CI(instance);
                noteDetails(object, instance.getDetails());
                targetSet.add(object);
            } else {
                objectsToQuery.put(instance.getClass().getName(), instance.getId());
            }
        }
        targetSet.addAll(objectsToCIs(session, objectsToQuery));
        /* actually do the planning of the operation */
        planning.toProcess.addAll(targetSet);
        planOperation(session);
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
     * Traverse model object graph to determine steps for the proposed operation.
     * Assumes that the internal {@code planning} field is set up and mutates it accordingly.
     * @param session the Hibernate session to use for HQL queries
     * @throws GraphException if the model objects were not as expected
     */
    private void planOperation(Session session) throws GraphException {
        /* track state to guarantee progress in reprocessing objects whose orphan status is relevant */
        Set<CI> optimisticReprocess = null;
        /* set of not-last objects after latest review */
        Set<CI> isNotLast = null;
        while (true) {
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
                        reviewObject(nextObject, false);
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
                        reviewObject(nextObject, false);
                    }
                    /* This condition is tricky. We do want to reprocess objects that are suggested for such, while
                     * avoiding an infinite loop that comes of such processing not resolving any orphan status. */
                    if (!Sets.symmetricDifference(previousFindIfLast, planning.findIfLast).isEmpty() ||
                            (optimisticReprocess == null ||
                             !Sets.symmetricDifference(planning.toProcess, optimisticReprocess).isEmpty()) &&
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
            }
            /* determine which objects are now not last */
            final Set<CI> latestIsNotLast = new HashSet<CI>();
            for (final Entry<CI, Boolean> objectAndIsLast : planning.foundIfLast.entrySet()) {
                if (!objectAndIsLast.getValue()) {
                    latestIsNotLast.add(objectAndIsLast.getKey());
                }
            }
            if (latestIsNotLast.isEmpty() || (isNotLast != null && Sets.difference(isNotLast, latestIsNotLast).isEmpty())) {
                /* no fewer not-last objects than before */
                break;
            }
            /* before completing processing, verify not-last status of objects */
            isNotLast = latestIsNotLast;
            planning.toProcess.addAll(isNotLast);
            planning.findIfLast.addAll(isNotLast);
            for (final CI object : isNotLast) {
                planning.foundIfLast.remove(object);
                if (log.isDebugEnabled()) {
                    log.debug("marked " + object + " as " + Orphan.RELEVANT + " to verify " + Orphan.IS_NOT_LAST + " status");
                }
            }
        }
        /* review objects for error conditions */
        for (final CI object : planning.cached) {
            reviewObject(object, true);
        }
    }

    /**
     * Note the details of the given object.
     * @param object the class and ID of the object instance
     * @param objectDetails the details of the object instance
     * @throws GraphException if the object could not be converted to an unloaded instance
     */
    private void noteDetails(CI object, ome.model.internal.Details objectDetails) throws GraphException {
        final IObject objectInstance = object.toIObject();

        if (planning.detailsNoted.put(object, objectDetails) != null) {
            return;
        }

        if (!eventContext.isCurrentUserAdmin()) {
            /* allowLoad ensures that BasicEventContext.groupPermissionsMap is populated */
            aclVoter.allowLoad(session, objectInstance.getClass(), objectDetails, object.id);

            if (aclVoter.allowUpdate(objectInstance, objectDetails)) {
                planning.mayUpdate.add(object);
            }
            if (aclVoter.allowDelete(objectInstance, objectDetails)) {
                planning.mayDelete.add(object);
            }
            if (objectInstance instanceof ExperimenterGroup) {
                final ExperimenterGroup loadedGroup = (ExperimenterGroup) session.load(ExperimenterGroup.class, object.id);
                if (aclVoter.allowChmod(loadedGroup)) {
                    planning.mayChmod.add(object);
                }
            }
            final Experimenter objectOwner = objectDetails.getOwner();
            if (objectOwner != null && eventContext.getCurrentUserId().equals(objectOwner.getId())) {
                planning.owns.add(object);
            }
        }

        policy.noteDetails(session, objectInstance, object.className, object.id);
    }

    /**
     * For the given class name and IDs, construct the corresponding {@link CI} instances without loading the persisted objects,
     * and ensure that their {@link ome.model.internal.Details} are noted.
     * @param className a model object class name
     * @param ids IDs of instances of the class
     * @return the {@link CI} instances indexed by object ID
     * @throws GraphException if an object could not be converted to an unloaded instance
     */
    private Map<Long, CI> findObjectDetails(String className, Collection<Long> ids) throws GraphException {
        final Map<Long, CI> objectsById = new HashMap<Long, CI>();
        final Set<Long> idsToQuery = new HashSet<Long>();

        for (final Long id : ids) {
            final CI object = new CI(className, id);
            final CI alias = planning.aliases.get(object);
            if (alias == null) {
                idsToQuery.add(id);
            } else {
                objectsById.put(object.id, alias);
            }
        }

        if (!idsToQuery.isEmpty()) {
            /* query persisted object instances without loading them */
            final String rootQuery = "FROM " + className + " WHERE id IN (:ids)";
            for (final List<Long> idsBatch : Iterables.partition(idsToQuery, BATCH_SIZE)) {
                final Iterator<Object> objectInstances = session.createQuery(rootQuery).setParameterList("ids", idsBatch).iterate();
                while (objectInstances.hasNext()) {
                    /*final*/ Object objectInstance = objectInstances.next();
                    if (objectInstance instanceof HibernateProxy) {
                        /* TODO: this is an awkward hack pending Hibernate 4's type() function */
                       final LazyInitializer initializer = ((HibernateProxy) objectInstance).getHibernateLazyInitializer();
                       final Long id = (Long) initializer.getIdentifier();
                       String realClassName = initializer.getEntityName();
                       boolean lookForSubclass = true;
                       while (lookForSubclass) {
                           lookForSubclass = false;
                           for (final String subclassName : model.getSubclassesOf(realClassName)) {
                               final String classQuery = "FROM " + subclassName + " WHERE id = :id";
                               final Iterator<Object> instance = session.createQuery(classQuery).setParameter("id", id).iterate();
                               if (instance.hasNext()) {
                                   realClassName = subclassName;
                                   lookForSubclass = true;
                                   break;
                               }
                           }
                       }
                       objectInstance = new CI(realClassName, id).toIObject();
                    }
                    final CI object = new CI((IObject) objectInstance);
                    objectsById.put(object.id, object);
                    planning.aliases.put(new CI(className, object.id), object);
                }
            }

            /* construct query according to which details may be queried */
            final Set<String> linkProperties = new HashSet<String>();
            for (final String superclassName : model.getSuperclassesOfReflexive(className)) {
                final Set<Entry<String, String>> forwardLinks = model.getLinkedTo(superclassName);
                for (final Entry<String, String> forwardLink : forwardLinks) {
                    linkProperties.add(forwardLink.getValue());
                }
            }
            final List<String> soughtProperties = ImmutableList.of("details.owner", "details.group");
            final List<String> selectTerms = new ArrayList<String>(soughtProperties.size() + 1);
            selectTerms.add("root.id");
            for (final String soughtProperty : soughtProperties) {
                if (linkProperties.contains(soughtProperty)) {
                    selectTerms.add("root." + soughtProperty);
                } else {
                    selectTerms.add("NULLIF(0,0)");  /* a simple NULL doesn't work in Hibernate 3.5 */
                }
            }
            selectTerms.add("root.details.permissions");  /* to include among soughtProperties once GraphPathBean knows of it */
            final String detailsQuery =
                    "SELECT " + Joiner.on(',').join(selectTerms) + " FROM " + className +" AS root WHERE root.id IN (:ids)";

            /* query and note details of objects */
            for (final List<Long> idsBatch : Iterables.partition(idsToQuery, BATCH_SIZE)) {
                final Query hibernateQuery = session.createQuery(detailsQuery).setParameterList("ids", idsBatch);
                for (final Object[] result : (List<Object[]>) hibernateQuery.list()) {
                    final ome.model.internal.Details details = ome.model.internal.Details.create();
                    final Long id = (Long) result[0];
                    details.setOwner((Experimenter) result[1]);
                    details.setGroup((ExperimenterGroup) result[2]);
                    details.setPermissions((Permissions) result[3]);
                    noteDetails(objectsById.get(id), details);
                }
            }
        }

        return objectsById;
    }

    /**
     * Convert the indicated objects to {@link CI}s with their actual class identified.
     * @param session a Hibernate session
     * @param objects the objects to query
     * @return {@link CI}s corresponding to the objects
     * @throws GraphException if any of the specified objects could not be queried
     */
    private Collection<CI> objectsToCIs(Session session, SetMultimap<String, Long> objects) throws GraphException {
        final List<CI> returnValue = new ArrayList<CI>(objects.size());
        for (final Entry<String, Collection<Long>> oneQueryClass : objects.asMap().entrySet()) {
            final String className = oneQueryClass.getKey();
            final Collection<Long> ids = oneQueryClass.getValue();
            final Collection<CI> retrieved = findObjectDetails(className, ids).values();
            if (ids.size() != retrieved.size()) {
                throw new GraphException("cannot read all the specified objects of class " + className);
            }
            returnValue.addAll(retrieved);
        }
        return returnValue;
    }

    /**
     * Given a class and a property of that class, determine to which class it links.
     * @param linkProperty a class and property name
     * @return the class linked to
     */
    private String getLinkedClass(CP linkProperty) {
        for (final Entry<String, String> forwardLink : model.getLinkedTo(linkProperty.className)) {
            if (linkProperty.propertyName.equals(forwardLink.getValue())) {
                return forwardLink.getKey();
            }
        }
        return null;
    }

    /**
     * Given a class and a property linking to that class, determine from which class it is linked.
     * @param linkProperty a class and property name
     * @return the linking class
     */
    @SuppressWarnings("unused")
    private String getLinkerClass(CP linkProperty) {
        for (final Entry<String, String> backwardLink : model.getLinkedBy(linkProperty.className)) {
            if (linkProperty.propertyName.equals(backwardLink.getValue())) {
                return backwardLink.getKey();
            }
        }
        return null;
    }

    /**
     * Load a specific link property's object relationships into the various cache fields of {@link Planning}.
     * @param linkProperty the link property being processed
     * @param query the HQL to query the property's object relationships
     * @param ids the IDs of the related objects
     * @return which linker objects are related to which linked objects by the given property
     * @throws GraphException if the objects could not be converted to unloaded instances
     */
    private List<Entry<CI,CI>> getLinksToCache(CP linkProperty, String query, Collection<Long> ids) throws GraphException {
        final String linkedClassName = getLinkedClass(linkProperty);
        final boolean propertyIsAccessible = model.isPropertyAccessible(linkProperty.className, linkProperty.propertyName);
        final SetMultimap<Long, Long> linkerToLinked = HashMultimap.create();
        for (final List<Long> idsBatch : Iterables.partition(ids, BATCH_SIZE)) {
            for (final Object[] result : (List<Object[]>) session.createQuery(query).setParameterList("ids", idsBatch).list()) {
                linkerToLinked.put((Long) result[0], (Long) result[1]);
            }
        }
        final List<Entry<CI,CI>> linkerLinked = new ArrayList<Entry<CI,CI>>();
        final Map<Long, CI> linkersById = findObjectDetails(linkProperty.className, linkerToLinked.keySet());
        final Map<Long, CI> linkedsById = findObjectDetails(linkedClassName, new HashSet<Long>(linkerToLinked.values()));
        for (final Entry<Long, Long> linkerIdLinkedId : linkerToLinked.entries()) {
            final CI linker = linkersById.get(linkerIdLinkedId.getKey());
            final CI linked = linkedsById.get(linkerIdLinkedId.getValue());
            if (!planning.detailsNoted.containsKey(linker)) {
                log.warn("failed to query for " + linker);
            } else if (!planning.detailsNoted.containsKey(linked)) {
                log.warn("failed to query for " + linked);
            } else {
                linkerLinked.add(Maps.immutableEntry(linker, linked));
                if (propertyIsAccessible) {
                    planning.befores.put(linked, linker);
                    planning.afters.put(linker, linked);
                }
                if (log.isDebugEnabled()) {
                    log.debug(linkProperty.toCPI(linker.id) + " links to " + linked);
                }
            }
        }
        return linkerLinked;
    }

    /**
     * Load object instances and their links into the various cache fields of {@link Planning}.
     * @param session a Hibernate session
     * @param toCache the objects to cache
     * @throws GraphException if the objects could not be converted to unloaded instances
     */
    private void cache(Session session, Collection<CI> toCache) throws GraphException {
        /* note which links to query, organized for batch querying */
        final SetMultimap<CP, Long> forwardLinksWanted = HashMultimap.create();
        final SetMultimap<CP, Long> backwardLinksWanted = HashMultimap.create();
        for (final CI inclusionCandidate : toCache) {
            for (final String inclusionCandidateSuperclassName : model.getSuperclassesOfReflexive(inclusionCandidate.className)) {
                for (final Entry<String, String> forwardLink : model.getLinkedTo(inclusionCandidateSuperclassName)) {
                    final CP linkProperty = new CP(inclusionCandidateSuperclassName, forwardLink.getValue());
                    forwardLinksWanted.put(linkProperty, inclusionCandidate.id);
                }
                for (final Entry<String, String> backwardLink : model.getLinkedBy(inclusionCandidateSuperclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    backwardLinksWanted.put(linkProperty, inclusionCandidate.id);
                }
            }
        }
        /* query and cache forward links */
        for (final Entry<CP, Collection<Long>> forwardLink : forwardLinksWanted.asMap().entrySet()) {
            final CP linkProperty = forwardLink.getKey();
            final String query = "SELECT linker.id, linked.id FROM " + linkProperty.className + " AS linker " +
                    "JOIN linker." + linkProperty.propertyName + " AS linked WHERE linker.id IN (:ids)";
            for (final Entry<CI, CI> linkerLinked : getLinksToCache(linkProperty, query, forwardLink.getValue())) {
                planning.forwardLinksCached.put(linkProperty.toCPI(linkerLinked.getKey().id), linkerLinked.getValue());
            }
        }
        /* query and cache backward links */
        for (final Entry<CP, Collection<Long>> backwardLink : backwardLinksWanted.asMap().entrySet()) {
            final CP linkProperty = backwardLink.getKey();
            final String query = "SELECT linker.id, linked.id FROM " + linkProperty.className + " AS linker " +
                    "JOIN linker." + linkProperty.propertyName + " AS linked WHERE linked.id IN (:ids)";
            for (final Entry<CI, CI> linkerLinked : getLinksToCache(linkProperty, query, backwardLink.getValue())) {
                planning.backwardLinksCached.put(linkProperty.toCPI(linkerLinked.getValue().id), linkerLinked.getKey());
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
        for (final String superclassName : model.getSuperclassesOfReflexive(object.className)) {
            for (final Entry<String, String> forwardLink : model.getLinkedTo(superclassName)) {
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
            for (final Entry<String, String> backwardLink : model.getLinkedBy(superclassName)) {
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
            final ome.model.internal.Details objectDetails = planning.detailsNoted.get(object);
            final Experimenter owner = objectDetails.getOwner();
            final ExperimenterGroup group = objectDetails.getGroup();
            final Long ownerId = owner == null ? null : owner.getId();
            final Long groupId = group == null ? null : group.getId();

            final Action action = getAction(object);
            final Orphan orphan = action == Action.EXCLUDE ? getOrphan(object) : Orphan.IRRELEVANT;

            if (eventContext.isCurrentUserAdmin()) {
                details = new DetailsWithCI(object.toIObject(), ownerId, groupId, action, orphan, true, true, true, true, true);
            } else {
                details = new DetailsWithCI(object.toIObject(), ownerId, groupId, action, orphan,
                        planning.mayUpdate.contains(object), planning.mayDelete.contains(object),
                        planning.mayChmod.contains(object), planning.owns.contains(object),
                        !planning.overrides.contains(object));
            }

            cache.put(object, details);
        }

        return details;
    }

    /**
     * Process the object, adjusting the planning state accordingly.
     * @param object an object
     * @param isErrorRules if {@link GraphPolicy#review(Map, Details, Map, Set)} should apply final checks instead of normal rules
     * @throws GraphException on detecting the policy attempting an illegal change of {@link Action}
     */
    private void reviewObject(CI object, boolean isErrorRules) throws GraphException {
        /* note the object's details */
        final Map<CI, Details> detailsCache = new HashMap<CI, Details>();
        final Details objectDetails = getDetails(detailsCache, object);
        if (log.isDebugEnabled()) {
            final StringBuffer sb = new StringBuffer();
            sb.append("reviewing ");
            sb.append(objectDetails);
            if (isErrorRules) {
                sb.append(" for error conditions");
            }
            log.debug(sb.toString());
        }
        /* review the object's links */
        final Map<String, Set<Details>> linkedFromDetails = new HashMap<String, Set<Details>>();
        final Map<String, Set<Details>> linkedToDetails   = new HashMap<String, Set<Details>>();
        final Set<String> notNullable = new HashSet<String>();
        for (final String superclassName : model.getSuperclassesOfReflexive(object.className)) {
            for (final Entry<String, String> forwardLink : model.getLinkedTo(superclassName)) {
                /* next forward link */
                final CP linkProperty = new CP(superclassName, forwardLink.getValue());
                if (model.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.REQUIRED) {
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
            for (final Entry<String, String> backwardLink : model.getLinkedBy(superclassName)) {
                /* next backward link */
                final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                if (model.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.REQUIRED) {
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
        final Set<Details> changes = policy.review(linkedFromDetails, objectDetails, linkedToDetails, notNullable, isErrorRules);
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
                    !planning.foundIfLast.containsKey(instance)) {
                /* relevant orphan status now determined so object must be processed */
                planning.findIfLast.remove(instance);
                planning.foundIfLast.put(instance, change.orphan == Orphan.IS_LAST);
                planning.toProcess.add(instance);
            } else if (change.action == Action.EXCLUDE && change.orphan == Orphan.RELEVANT &&
                    planning.findIfLast.add(instance) && !planning.cached.contains(instance)) {
                /* orphan status is relevant; if just now noted as such then ensure the object is or will be cached */
                planning.toProcess.add(instance);
            } else if (!(change.action == Action.OUTSIDE || instance.equals(object))) {
                /* probably just needs review */
                planning.toProcess.add(instance);
            }
            if (!(change.isCheckPermissions || eventContext.isCurrentUserAdmin())) {
                /* do not check the user's permissions on this object */
                planning.overrides.add(instance);
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
        if (model.isPropertyAccessible(linker.className, linker.propertyName)) {
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
            public void deleteInstances(String className, Collection<Long> ids) throws GraphException {
                if (!(ids instanceof SortedSet)) {
                    ids = new TreeSet<Long>(ids);
                }
                if (log.isDebugEnabled()) {
                    log.debug("processor: delete " + className + "[" + Joiner.on(',').join(ids) + "]");
                }
                processor.deleteInstances(className, ids);
            }

            @Override
            public void processInstances(String className, Collection<Long> ids) throws GraphException {
                if (!(ids instanceof SortedSet)) {
                    ids = new TreeSet<Long>(ids);
                }
                if (log.isDebugEnabled()) {
                    log.debug("processor: process " + className + "[" + Joiner.on(',').join(ids) + "]");
                }
                processor.processInstances(className, ids);
            }

            @Override
            public Set<Ability> getRequiredPermissions() {
                return processor.getRequiredPermissions();
            }

            @Override
            public void assertMayProcess(String className, long id, ome.model.internal.Details details) throws GraphException {
                processor.assertMayProcess(className, id, details);
            }
        };
    }

    /**
     * Determine if the given {@link IObject} class is a system type as judged by {@link SystemTypes#isSystemType(Class)}.
     * @param className a class name
     * @return if the class is a system type
     * @throws GraphException if {@code className} does not name an accessible class
     */
    private boolean isSystemType(String className) throws GraphException {
        try {
            final Class<? extends IObject> actualClass = (Class<? extends IObject>) Class.forName(className);
            return systemTypes.isSystemType(actualClass);
        } catch (ClassNotFoundException e) {
            throw new GraphException("no model object class named " + className);
        }
    }

    /**
     * Assert that the processor may operate upon the given objects with {@link Processor#processInstances(String, Collection)}.
     * Never fails for system types.
     * @param className a class name
     * @param ids instance IDs
     * @throws GraphException if the user does not have the necessary permissions for all of the objects
     */
    private void assertMayBeProcessed(String className, Collection<Long> ids) throws GraphException {
        final Set<CI> objects = idsToCIs(className, ids);
        if (!isSystemType(className)) {
            assertPermissions(objects, processor.getRequiredPermissions());
        }
        if (!eventContext.isCurrentUserAdmin()) {
            for (final CI object : Sets.difference(objects, planning.overrides)) {
                try {
                    processor.assertMayProcess(object.className, object.id, planning.detailsNoted.get(object));
                } catch (GraphException e) {
                    throw new GraphException("cannot process " + object + ": " + e.message);
                }
            }
        }
    }

    /**
     * Assert that the user may delete the given objects. Never fails for system types.
     * @param className a class name
     * @param ids instance IDs
     * @throws GraphException if the user may not delete all of the objects
     */
    private void assertMayBeDeleted(String className, Collection<Long> ids) throws GraphException {
        if (!isSystemType(className)) {
            assertPermissions(idsToCIs(className, ids), Collections.singleton(Ability.DELETE));
        }
    }

    /**
     * Assert that the user may update the given objects. Never fails for system types.
     * @param className a class name
     * @param ids instance IDs
     * @throws GraphException if the user may not update all of the objects
     */
    private void assertMayBeUpdated(String className, Collection<Long> ids) throws GraphException {
        if (!isSystemType(className)) {
            assertPermissions(idsToCIs(className, ids), Collections.singleton(Ability.UPDATE));
        }
    }

    /**
     * Assert that the user has the given abilities to operate upon the given objects.
     * @param objects some objects
     * @param abilities some abilities, may be {@code null}
     * @throws GraphException if the user does not have all the abilities to operate upon all of the objects
     */
    private void assertPermissions(Set<CI> objects, Collection<GraphPolicy.Ability> abilities) throws GraphException {
        if (abilities == null || eventContext.isCurrentUserAdmin()) {
            return;
        }
        objects = Sets.difference(objects, planning.overrides);
        if (abilities.contains(Ability.DELETE)) {
            final Set<CI> violations = Sets.difference(objects, planning.mayDelete);
            if (!violations.isEmpty()) {
                throw new GraphException("not permitted to delete " + Joiner.on(", ").join(violations));
            }
        }
        if (abilities.contains(Ability.UPDATE)) {
            final Set<CI> violations = Sets.difference(objects, planning.mayUpdate);
            if (!violations.isEmpty()) {
                throw new GraphException("not permitted to update " + Joiner.on(", ").join(violations));
            }
        }
        if (abilities.contains(Ability.CHMOD)) {
            final Set<CI> violations = Sets.difference(objects, planning.mayChmod);
            if (!violations.isEmpty()) {
                throw new GraphException("not permitted to change permissions on " + Joiner.on(", ").join(violations));
            }
        }
        if (abilities.contains(Ability.OWN)) {
            final Set<CI> violations = Sets.difference(objects, planning.owns);
            if (!violations.isEmpty()) {
                throw new GraphException("does not own " + Joiner.on(", ").join(violations));
            }
        }
    }

    /**
     * Convert the given IDs to objects of the given class.
     * @param className a class name
     * @param ids instance IDs
     * @return objects of the given class and IDs
     */
    private static Set<CI> idsToCIs(String className, Collection<Long> ids) {
        final Set<CI> objects = new HashSet<CI>();
        for (final Long id : ids) {
            objects.add(new CI(className, id));
        }
        return objects;
    }

    /**
     * Remove links between the targeted model objects and the remainder of the model object graph.
     * @throws GraphException if the user does not have permission to unlink the targets
     */
    public void unlinkTargets() throws GraphException {
        /* accumulate plan for unlinking included/deleted from others */
        final SetMultimap<CP, Long> toNullByCP = HashMultimap.create();
        final Map<CP, SetMultimap<Long, Entry<String, Long>>> linkerToIdToLinked =
                new HashMap<CP, SetMultimap<Long, Entry<String, Long>>>();
        for (final CI object : planning.included) {
            for (final String superclassName : model.getSuperclassesOfReflexive(object.className)) {
                for (final Entry<String, String> forwardLink : model.getLinkedTo(superclassName)) {
                    final CP linkProperty = new CP(superclassName, forwardLink.getValue());
                    final boolean isCollection =
                            model.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
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
                for (final Entry<String, String> backwardLink : model.getLinkedBy(superclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    final boolean isCollection =
                            model.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
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
            for (final String superclassName : model.getSuperclassesOfReflexive(object.className)) {
                for (final Entry<String, String> backwardLink : model.getLinkedBy(superclassName)) {
                    final CP linkProperty = new CP(backwardLink.getKey(), backwardLink.getValue());
                    final boolean isCollection =
                            model.getPropertyKind(linkProperty.className, linkProperty.propertyName) == PropertyKind.COLLECTION;
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
            final CP linker = nullCurr.getKey();
            if (unnullable.get(linker.className).contains(linker.propertyName) ||
                    model.getPropertyKind(linker.className, linker.propertyName) == PropertyKind.REQUIRED) {
                throw new GraphException("cannot null " + linker);
            }
            final Collection<Long> allIds = nullCurr.getValue();
            assertMayBeUpdated(linker.className, allIds);
            for (final List<Long> ids : Iterables.partition(allIds, BATCH_SIZE)) {
                processor.nullProperties(linker.className, linker.propertyName, ids);
            }
        }
        /* unlink included/deleted by removing from collections */
        for (final Entry<CP, SetMultimap<Long, Entry<String, Long>>> removeCurr : linkerToIdToLinked.entrySet()) {
            final CP linker = removeCurr.getKey();
            final Collection<Long> allIds = removeCurr.getValue().keySet();
            assertMayBeUpdated(linker.className, allIds);
            throw new GraphException("cannot remove elements from collection " + linker);
        }
    }

    /**
     * Process the targeted model objects.
     * @throws GraphException if the user does not have permission to process the targets or
     * if a cycle is detected in the model object graph
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
                throw new GraphException("cycle detected among " + Joiner.on(", ").join(planning.blockedBy.keySet()));
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
                    final Collection<Long> allIds = oneClassToDelete.getValue();
                    assertMayBeDeleted(className, allIds);
                    final Collection<List<Long>> idGroups;
                    if (OriginalFile.class.getName().equals(className)) {
                        idGroups = ModelObjectSequencer.sortOriginalFileIds(session, allIds);
                        for (final List<Long> idGroup : idGroups) {
                            for (final List<Long> ids : Iterables.partition(idGroup, BATCH_SIZE)) {
                                processor.deleteInstances(className, ids);
                            }
                        }
                    } else {
                        for (final List<Long> ids : Iterables.partition(allIds, BATCH_SIZE)) {
                            processor.deleteInstances(className, ids);
                        }
                    }
                }
            }
            /* perform this group's includes */
            if (!toJoin.isEmpty()) {
                for (final Entry<String, Collection<Long>> oneClassToJoin : toJoin.asMap().entrySet()) {
                    final String className = oneClassToJoin.getKey();
                    final Collection<Long> allIds = oneClassToJoin.getValue();
                    assertMayBeProcessed(className, allIds);
                    for (final List<Long> ids : Iterables.partition(allIds, BATCH_SIZE)) {
                        processor.processInstances(className, ids);
                    }
                }
            }
        }
    }
}
