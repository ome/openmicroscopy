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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.services.graphs.PermissionsPredicate;
import ome.system.Roles;
import omero.cmd.Duplicate;
import omero.cmd.DuplicateResponse;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * Request to duplicate model objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.2.1
 */
public class DuplicateI extends Duplicate implements IRequest, WrappableRequest<Duplicate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateI.class);

    private static final Set<GraphPolicy.Ability> REQUIRED_ABILITIES = ImmutableSet.of();

    /* all bulk operations are batched; this size should be suitable for IN (:ids) for HQL */
    private static final int BATCH_SIZE = 256;

    private static enum Inclusion {
        /* the object is to be duplicated */
        DUPLICATE,
        /* the object is not to be duplicated, but it may be referenced from duplicates */
        REFERENCE,
        /* the object is not to be duplicated, nor is it to be referenced from duplicates */
        IGNORE
    };

    private final ACLVoter aclVoter;
    private final SystemTypes systemTypes;
    private final GraphPathBean graphPathBean;
    private final Set<Class<? extends IObject>> targetClasses;
    private GraphPolicy graphPolicy;  /* not final because of adjustGraphPolicy */
    private final SetMultimap<String, String> unnullable;

    /* a taxonomy based on the class hierarchy of model objects in Java */
    private SpecificityClassifier<Class<? extends IObject>, Inclusion> classifier;

    private List<Function<GraphPolicy, GraphPolicy>> graphPolicyAdjusters = new ArrayList<Function<GraphPolicy, GraphPolicy>>();
    private Helper helper;
    private GraphHelper graphHelper;
    private GraphTraversal graphTraversal;

    private FlushMode flushMode;
    private GraphTraversal.PlanExecutor processor;

    private int targetObjectCount = 0;
    private int duplicatedObjectCount = 0;

    private final Map<IObject, IObject> originalsToDuplicates = new HashMap<IObject, IObject>();

    /**
     * Construct a new <q>duplicate</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean to use
     * @param deletionInstance a deletion instance for deleting files
     * @param targetClasses legal target object classes for duplicate
     * @param graphPolicy the graph policy to apply for duplicate
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     */
    public DuplicateI(ACLVoter aclVoter, Roles securityRoles, SystemTypes systemTypes, GraphPathBean graphPathBean,
            Deletion deletionInstance, Set<Class<? extends IObject>> targetClasses, GraphPolicy graphPolicy,
            SetMultimap<String, String> unnullable) {
        this.aclVoter = aclVoter;
        this.systemTypes = systemTypes;
        this.graphPathBean = graphPathBean;
        this.targetClasses = targetClasses;
        this.graphPolicy = graphPolicy;
        this.unnullable = unnullable;
    }

    @Override
    public Map<String, String> getCallContext() {
        return null;
    }

    @Override
    public void init(Helper helper) {
        if (LOGGER.isDebugEnabled()) {
            final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
            arguments.addParameter("typesToDuplicate", typesToDuplicate);
            arguments.addParameter("typesToReference", typesToReference);
            arguments.addParameter("typesToIgnore", typesToIgnore);
            arguments.addParameter("targetObjects", targetObjects);
            arguments.addParameter("childOptions", childOptions);
            arguments.addParameter("dryRun", dryRun);
            LOGGER.debug("request: " + arguments);
        }

        this.helper = helper;
        helper.setSteps(dryRun ? 3 : 6);
        this.graphHelper = new GraphHelper(helper, graphPathBean);

        classifier = new SpecificityClassifier<Class<? extends IObject>, Inclusion>(
                new SpecificityClassifier.ContainmentTester<Class<? extends IObject>>() {
                    @Override
                    public boolean isProperSupersetOf(Class<? extends IObject> parent, Class<? extends IObject> child) {
                        return parent != child && parent.isAssignableFrom(child);
                    }});

        try {
            classifier.addClass(Inclusion.DUPLICATE, graphHelper.getClassesFromNames(typesToDuplicate));
            classifier.addClass(Inclusion.REFERENCE, graphHelper.getClassesFromNames(typesToReference));
            classifier.addClass(Inclusion.IGNORE,    graphHelper.getClassesFromNames(typesToIgnore));
        } catch (IllegalArgumentException e) {
            throw helper.cancel(new ERR(), e, "bad-class");
        }

        graphPolicyAdjusters.add(0, new Function<GraphPolicy, GraphPolicy>() {
            @Override
            public GraphPolicy apply(GraphPolicy graphPolicy) {
                return SkipTailPolicy.getSkipTailPolicy(graphPolicy,
                        new Predicate<Class<? extends IObject>>() {
                    @Override
                    public boolean apply(Class<? extends IObject> modelObject) {
                        final Inclusion classification = classifier.getClass(modelObject);
                        return classification == Inclusion.REFERENCE || classification == Inclusion.IGNORE;
                    }});
            }});

        graphPolicy.registerPredicate(new PermissionsPredicate());

        graphTraversal = graphHelper.prepareGraphTraversal(childOptions, REQUIRED_ABILITIES, graphPolicy, graphPolicyAdjusters,
                aclVoter, systemTypes, graphPathBean, unnullable, new InternalProcessor(), dryRun);

        graphPolicyAdjusters = null;
    }

    /**
     * Duplicate model object properties, linking them as appropriate with each other and with other model objects.
     * @throws GraphException if duplication failed
     */
    private void setDuplicatePropertyValues() throws GraphException {
        /* organize duplicate index by class name and ID */
        final Map<Entry<String, Long>, IObject> duplicatesByOriginalClassAndId = new HashMap<Entry<String, Long>, IObject>();
        for (final Entry<IObject, IObject> originalAndDuplicate : originalsToDuplicates.entrySet()) {
            final IObject original = originalAndDuplicate.getKey();
            final String originalClass = Hibernate.getClass(original).getName();
            final Long originalId = original.getId();
            final IObject duplicate = originalAndDuplicate.getValue();
            duplicatesByOriginalClassAndId.put(Maps.immutableEntry(originalClass, originalId), duplicate);
        }
        /* allow lookup regardless of if original is actually a Hibernate proxy object */
        final Function<Object, Object> duplicateLookup = new Function<Object, Object>() {
            @Override
            public Object apply(Object original) {
                if (original instanceof IObject) {
                    final String originalClass;
                    if (original instanceof HibernateProxy) {
                        originalClass = Hibernate.getClass(original).getName();
                    } else {
                        originalClass = original.getClass().getName();
                    }
                    final Long originalId = ((IObject) original).getId();
                    return duplicatesByOriginalClassAndId.get(Maps.immutableEntry(originalClass, originalId));
                } else {
                    return null;
                }
            }
        };
        /* copy property values into duplicates and link with other model objects */
        final Session session = helper.getSession();
        for (final Entry<IObject, IObject> originalAndDuplicate : originalsToDuplicates.entrySet()) {
            final IObject original = originalAndDuplicate.getKey();
            final IObject duplicate = originalAndDuplicate.getValue();
            final String originalClass = Hibernate.getClass(original).getName();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("copying properties from " + originalClass + ":" + original.getId());
            }
            try {
                /* process property values for a given object that is duplicated */
                for (final String superclassName : graphPathBean.getSuperclassesOfReflexive(originalClass)) {
                    /* process property values that link from the duplicate to other model objects */
                    for (final Entry<String, String> forwardLink : graphPathBean.getLinkedTo(superclassName)) {
                        /* next forward link */
                        final String linkedClassName = forwardLink.getKey();
                        final String property = forwardLink.getValue();
                        /* ignore details for now, duplicates never preserve original ownership */
                        if (property.startsWith("details.")) {
                            continue;
                        }
                        /* note which of the objects to which the original links should be ignored */
                        final Set<Long> linkedToIdsToIgnore = new HashSet<Long>();
                        for (final Entry<String, Collection<Long>> linkedToClassIds :
                            graphTraversal.getLinkeds(superclassName, property, original.getId()).asMap().entrySet()) {
                            final String linkedToClass = linkedToClassIds.getKey();
                            final Collection<Long> linkedToIds = linkedToClassIds.getValue();
                            if (classifier.getClass(Class.forName(linkedToClass).asSubclass(IObject.class)) == Inclusion.IGNORE) {
                                linkedToIdsToIgnore.addAll(linkedToIds);
                            }
                        }
                        /* check for another accessor for inaccessible properties */
                        if (graphPathBean.isPropertyAccessible(superclassName, property)) {
                            /* copy the linking from the original's property over to the duplicate's */
                            Object value;
                            try {
                                value = PropertyUtils.getNestedProperty(original, property);
                            } catch (NestedNullException e) {
                                continue;
                            }
                            if (value instanceof Collection) {
                                /* if a collection property, include only the objects that aren't to be ignored */
                                final Collection<IObject> valueCollection = (Collection<IObject>) value;
                                final Collection<IObject> valueToCopy;
                                if (value instanceof List) {
                                    valueToCopy = new ArrayList<IObject>();
                                } else if (value instanceof Set) {
                                    valueToCopy = new HashSet<IObject>();
                                } else {
                                    throw new GraphException("unexpected collection type: " + value.getClass());
                                }
                                for (final IObject linkedTo : valueCollection) {
                                    if (!linkedToIdsToIgnore.contains(linkedTo.getId())) {
                                        valueToCopy.add(linkedTo);
                                    }
                                }
                                value = valueToCopy;
                            } else if (value instanceof IObject) {
                                /* if the property value is to be ignored then null it */
                                if (linkedToIdsToIgnore.contains(((IObject) value).getId())) {
                                    value = null;
                                }
                            }
                            /* copy the property value, replacing originals with corresponding duplicates */
                            final Object duplicateValue = GraphUtil.copyComplexValue(duplicateLookup, value);
                            try {
                                PropertyUtils.setNestedProperty(duplicate, property, duplicateValue);
                            } catch (NestedNullException e) {
                                throw new GraphException(
                                        "cannot set property " + superclassName + '.' + property + " on duplicate");
                            }
                        } else {
                            /* this could be a one-to-many property with direct accessors protected */
                            final Class<? extends IObject> linkerClass = Class.forName(superclassName).asSubclass(IObject.class);
                            final Class<? extends IObject> linkedClass = Class.forName(linkedClassName).asSubclass(IObject.class);
                            final Method reader, writer;
                            try {
                                reader = linkerClass.getMethod("iterate" + StringUtils.capitalize(property));
                                writer = linkerClass.getMethod("add" + linkedClass.getSimpleName(), linkedClass);
                            } catch (NoSuchMethodException | SecurityException e) {
                                /* no luck, so ignore this property */
                                continue;
                            }
                            /* copy the linking from the original's property over to the duplicate's */
                            final Iterator<IObject> linkedTos = (Iterator<IObject>) reader.invoke(original);
                            while (linkedTos.hasNext()) {
                                final IObject linkedTo = linkedTos.next();
                                /* copy only links to other duplicates, as otherwise we may steal objects from the original */
                                final IObject duplicateOfLinkedTo = (IObject) duplicateLookup.apply(linkedTo);
                                if (duplicateOfLinkedTo != null) {
                                    writer.invoke(duplicate, duplicateOfLinkedTo);
                                }
                            }
                        }
                    }
                    /* process property values that link to the duplicate from other model objects */
                    for (final Entry<String, String> backwardLink : graphPathBean.getLinkedBy(superclassName)) {
                        /* next backward link */
                        final String linkingClass = backwardLink.getKey();
                        final String property = backwardLink.getValue();
                        /* ignore inaccessible properties */
                        if (!graphPathBean.isPropertyAccessible(linkingClass, property)) {
                            continue;
                        }
                        for (final Entry<String, Collection<Long>> linkedFromClassIds :
                            graphTraversal.getLinkers(linkingClass, property, original.getId()).asMap().entrySet()) {
                            final String linkedFromClass = linkedFromClassIds.getKey();
                            final Collection<Long> linkedFromIds = linkedFromClassIds.getValue();
                            if (classifier.getClass(Class.forName(linkedFromClass).asSubclass(IObject.class)) == Inclusion.IGNORE) {
                                /* these linkers are to be ignored */
                                continue;
                            }
                            /* load the instances that link to the original */
                            final String rootQuery = "FROM " + linkedFromClass + " WHERE id IN (:ids)";
                            for (final List<Long> idsBatch : Iterables.partition(linkedFromIds, BATCH_SIZE)) {
                                final List<IObject> linkers =
                                        session.createQuery(rootQuery).setParameterList("ids", idsBatch).list();
                                for (final IObject linker : linkers) {
                                    if (originalsToDuplicates.containsKey(linker)) {
                                        /* ignore linkers that are to be duplicated, those are handled as forward links */
                                        continue;
                                    }
                                    /* copy the linking from the original's property over to the duplicate's */
                                    Object value;
                                    try {
                                        value = PropertyUtils.getNestedProperty(linker, property);
                                    } catch (NestedNullException e) {
                                        continue;
                                    }
                                    /* for linkers only adjust collection properties */
                                    if (value instanceof Collection) {
                                        final Collection<IObject> valueCollection = (Collection<IObject>) value;
                                        final Collection<IObject> newDuplicates = new ArrayList<IObject>();
                                        for (final IObject originalLinker : valueCollection) {
                                            final IObject duplicateOfValue = originalsToDuplicates.get(originalLinker);
                                            if (duplicateOfValue != null) {
                                                /* previous had just original, now include duplicate too */
                                                newDuplicates.add(duplicateOfValue);
                                            }
                                        }
                                        valueCollection.addAll(newDuplicates);
                                    }
                                }
                            }
                        }
                    }
                    /* process property values that do not relate to edges in the model object graph */
                    for (final String property : graphPathBean.getSimpleProperties(superclassName, false)) {
                        /* ignore inaccessible properties */
                        if (!graphPathBean.isPropertyAccessible(superclassName, property)) {
                            continue;
                        }
                        /* copy original property value to duplicate */
                        final Object value = PropertyUtils.getProperty(original, property);
                        PropertyUtils.setProperty(duplicate, property, GraphUtil.copyComplexValue(duplicateLookup, value));
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new GraphException("failed to duplicate " + originalClass + ':' + original.getId());
            }
        }
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        final Session session = helper.getSession();
        try {
            switch (step) {
            case 0:
                final SetMultimap<String, Long> targetMultimap = graphHelper.getTargetMultimap(targetClasses, targetObjects);
                targetObjectCount += targetMultimap.size();
                final Entry<SetMultimap<String, Long>, SetMultimap<String, Long>> plan =
                        graphTraversal.planOperation(session, targetMultimap, true, true);
                if (plan.getValue().isEmpty()) {
                    graphTraversal.assertNoUnlinking();
                } else {
                    final Exception e = new IllegalArgumentException("duplication plan unexpectedly includes deletion");
                    throw helper.cancel(new ERR(), e, "bad-plan");
                }
                return plan.getKey();
            case 1:
                graphTraversal.assertNoPolicyViolations();
                return null;
            case 2:
                processor = graphTraversal.processTargets();
                return null;
            case 3:
                processor.execute();
                return null;
            case 4:
                /* prevent premature flush triggered by duplication queries */
                flushMode = session.getFlushMode();
                session.setFlushMode(FlushMode.COMMIT);
                setDuplicatePropertyValues();
                return null;
            case 5:
                for (final IObject duplicate : originalsToDuplicates.values()) {
                    session.persist(duplicate);
                }
                session.flush();
                session.setFlushMode(flushMode);
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
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        /* if the results object were in terms of IObjectList then this would need IceMapper.map */
        if (dryRun && step == 0) {
            final SetMultimap<String, Long> result = (SetMultimap<String, Long>) object;
            final Map<String, List<Long>> duplicatedObjects = GraphUtil.copyMultimapForResponse(result);
            duplicatedObjectCount += result.size();
            final DuplicateResponse response = new DuplicateResponse(duplicatedObjects);
            helper.setResponseIfNull(response);
            helper.info("in mock duplication of " + targetObjectCount + ", duplicated " + duplicatedObjectCount + " in total");
        } else if (!dryRun && step == 5) {
            final Map<String, List<Long>> duplicatedObjects = new HashMap<String, List<Long>>();
            for (final IObject duplicate : originalsToDuplicates.values()) {
                final String className = duplicate.getClass().getName();
                List<Long> ids = duplicatedObjects.get(className);
                if (ids == null) {
                    ids = new ArrayList<Long>();
                    duplicatedObjects.put(className, ids);
                }
                ids.add(duplicate.getId());
                duplicatedObjectCount++;
            }
            final DuplicateResponse response = new DuplicateResponse(duplicatedObjects);
            helper.setResponseIfNull(response);
            helper.info("in duplication of " + targetObjectCount + ", duplicated " + duplicatedObjectCount + " in total");

            if (LOGGER.isDebugEnabled()) {
                final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
                arguments.addParameter("duplicates", response.duplicates);
                LOGGER.debug("response: " + arguments);
            }
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    @Override
    public void copyFieldsTo(Duplicate request) {
        GraphUtil.copyFields(this, request);
        request.typesToDuplicate = new ArrayList<String>(typesToDuplicate);
        request.typesToReference = new ArrayList<String>(typesToReference);
        request.typesToIgnore = new ArrayList<String>(typesToIgnore);
    }

    @Override
    public void adjustGraphPolicy(Function<GraphPolicy, GraphPolicy> adjuster) {
        if (graphPolicyAdjusters == null) {
            throw new IllegalStateException("request is already initialized");
        } else {
            graphPolicyAdjusters.add(adjuster);
        }
    }

    @Override
    public int getStepProvidingCompleteResponse() {
        return dryRun ? 0 : 5;
    }

    @Override
    public GraphPolicy.Action getActionForStarting() {
        return GraphPolicy.Action.INCLUDE;
    }

    @Override
    public Map<String, List<Long>> getStartFrom(Response response) {
        return ((DuplicateResponse) response).duplicates;
    }

    /**
     * A <q>duplicate</q> processor that assists with duplicating model objects.
     * This processor merely reads from the database and initializes the duplicate objects.
     * The updates to the objects' property values occur in a later step.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.1
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) throws GraphException {
            final String rootQuery = "FROM " + className + " WHERE id IN (:ids)";
            for (final List<Long> idsBatch : Iterables.partition(ids, BATCH_SIZE)) {
                final List<IObject> originals = session.createQuery(rootQuery).setParameterList("ids", idsBatch).list();
                for (final IObject original : originals) {
                    final IObject duplicate;
                    try {
                        duplicate = (IObject) Hibernate.getClass(original).newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new GraphException("cannot create a duplicate of " + original);
                    }
                    originalsToDuplicates.put(original, duplicate);
                }
            }
        }

        @Override
        public Set<GraphPolicy.Ability> getRequiredPermissions() {
            return REQUIRED_ABILITIES;
        }
    }
}
