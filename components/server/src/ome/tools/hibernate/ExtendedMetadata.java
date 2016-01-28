/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IAnnotated;
import ome.model.IGlobal;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.internal.Permissions;
import ome.tools.spring.OnContextRefreshedEventListener;

/**
 * extension of the model metadata provided by {@link SessionFactory}. During
 * construction, the metadata is created and cached for later use.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SessionFactory
 * @since 3.0-M3
 */
public interface ExtendedMetadata {

    Set<String> getClasses();

    /**
     * Returns all the classes which implement {@link IAnnotated}
     * @return the annotatable types
     */
    Set<Class<IAnnotated>> getAnnotatableTypes();

    /**
     * Returns all the classes which subclass {@link Annotation}
     * @return the types of annotation
     */
    Set<Class<Annotation>> getAnnotationTypes();

    /**
     * Returns the query for obtaining the number of collection items to a
     * particular instance. All such queries will return a ResultSet with rows
     * of the form: 0 (Long) id of the locked class 1 (Long) count of the
     * instances locking that class
     *
     * @param field
     *            Field name as specified in the class.
     * @return String query. Never null.
     * @throws ApiUsageException
     *             if return value would be null.
     */
    String getCountQuery(String field) throws ApiUsageException;

    /**
     * Given the name of a database table or alternatively the simple class name
     * (non-fully qualified) of an IObject, this method returns the class which
     * Hibernate will map that table to.
     * @param table a database table name, or simple class name of a model object
     * @return the corresponding mapped class
     */
    Class<IObject> getHibernateClass(String table);

    /**
     * walks the {@link IObject} argument <em>non-</em>recursively and gathers
     * all {@link IObject} instances which will be linked to by the
     * creation or updating of the argument. (Previously this was called "locking"
     * since a flag was set on the object to mark it as linked, but this was
     * removed in 4.2)
     *
     * @param iObject
     *            A newly created or updated {@link IObject} instance which
     *            might possibly lock other {@link IObject IObjects}. A null
     *            argument will return an empty array to be checked.
     * @return A non-null array of {@link IObject IObjects} which will be linked to.
     */
    IObject[] getLockCandidates(IObject iObject);

    /**
     * Rather than iterating over an {@link IObject} like
     * {@link #getLockCandidates(IObject)} this method returns type/field name
     * pairs (like {@link #getLockChecks(Class)}) to allow performing the
     * queries manually.
     *
     * If onlyWithGroups is true, then only checks which point to non-IGlobal
     * objects will be returned.
     *
     * @param klass Not null.
     * @param onlyWithGroups if should omit checks that point to {@link IGlobal}s
     * @return the lock candidates for checking
     */
    String[][] getLockCandidateChecks(Class<? extends IObject> klass, boolean onlyWithGroups);

    /**
     * returns all class/field name pairs which may possibly link to an object
     * of type <code>klass</code>.
     *
     * @param klass
     *            Non-null {@link Class subclass} of {@link IObject}
     * @return A non-null array of {@link String} queries which can be used to
     *         determine if an {@link IObject} instance can be unlocked.
     */
    String[][] getLockChecks(Class<? extends IObject> klass);

    /**
     * Takes the lock checks returned by {@link #getLockChecks(Class)} and
     * performs the actual check returning a map from class to total number
     * of locks. The key "*" contains the total value.
     *
     * If the id argument is null, then checks will be against all rows rather
     * than individual objects, e.g.
     * <pre>
     * select count(x) from Linker x, Linked y where x.$FIELD.id = y.id $CLAUSE;
     * </pre>
     * otherwise
     * <pre>
     * select count(x) from Linker x where x.$FIELD.id = :id $CLAUSE'
     * </pre>
     *
     * If the clause argument is null or empty it will be omitted.
     */
    Map<String, Long> countLocks(Session session, Long id, String[][] lockChecks, String clause);

    /**
     * Walks the data on what locks what
     * for "from" argument to see if there is any direct relationship to the
     * "to" argument. If there is, the name will be returned. Otherwise, null.
     */
    String getRelationship(String from, String to);

    /**
     * provides the link between two tables similar to
     * {link {@link #getRelationship(String, String)}. However, whereas
     * {@link #getRelationship(String, String)} needs to be called twice, once
     * for each of the Hibernate directions,
     * {@link #getSQLJoin(String, String, String, String)} need only ever be
     * called once since there will be only one correct SQL join.
     *
     * For example, getRelationship("Image", "DatasetImageLink") returns
     * "datasetLinks" while getRelationship("DatasetImageLink", "Image")
     * returns "child". getSQLJoin("Image", "I", "DatasetImageLink", "L"),
     * however, will always return "I.id = L.child" (though the order may be
     * reversed).
     */
    String getSQLJoin(String fromType, String fromAlias, String toType, String toAlias);

    /**
     * Check if an object of this class may have map properties.
     * @param iObjectClass a class
     * @return if this object or any of its mapped subclasses have any map properties
     */
    boolean mayHaveMapProperties(Class<? extends IObject> iObjectClass);

    /**
     * Get the names of any String&rarr;RString map properties this class has, otherwise an empty set if none.
     * @param className the name of a class, as from {@link Class#getName()}
     * @return the class' map property names
     */
    Set<String> getMapProperties(String className);

/**
 * Sole implementation of ExtendedMetadata. The separation is intended to make
 * unit testing without a full {@link ExtendedMetadata} possible.
 */
public static class Impl extends OnContextRefreshedEventListener implements ExtendedMetadata {

    private final static Logger log = LoggerFactory.getLogger(ExtendedMetadata.class);

    private final Map<String, Locks> locksHolder = new HashMap<String, Locks>();

    private final Map<String, String[][]> lockedByHolder = new HashMap<String, String[][]>();

    private final Map<String, Immutables> immutablesHolder = new HashMap<String, Immutables>();

    private final Map<String, String> collectionCountHolder = new HashMap<String, String>();

    private final Map<String, Class<IObject>> targetHolder = new HashMap<String, Class<IObject>>();

    private final Set<Class<IAnnotated>> annotatableTypes = new HashSet<Class<IAnnotated>>();

    private final Set<Class<Annotation>> annotationTypes = new HashSet<Class<Annotation>>();

    private final Map<String, Map<String, Relationship>> relationships = new HashMap<String, Map<String, Relationship>>();

    private final Map<String, Class<IObject>> hibernateClasses = new HashMap<String, Class<IObject>>();

    private final Set<Class<?>> mapPropertyClasses = new HashSet<Class<?>>();

    private final SetMultimap<String, String> mapProperties = HashMultimap.create();

    private boolean initialized = false;

    // NOTES:
    // TODO we could just delegate to sf and implement the same interface.
    // TOTEST
    // will need to get collection items out. // no filtering.
    // will also need to do the same for checkIfNeedLock once we have
    // a collection valued (non-association) type. (does that exist??)
    // will also need to handle components if we have any other than details.
    // Doesn't handle ComponentTypes

    /**
     * Listener method which waits for a {@link ContextRefreshedEvent} and then
     * extracts the {@link SessionFactory} from the {@link ApplicationContext}
     * and pases it to {@link #setSessionFactory(SessionFactory)}.
     */
    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent cre) {
        ApplicationContext ctx = cre.getApplicationContext();
        if (ctx.containsBean("sessionFactory")) {
            SessionFactory sessionFactory = (SessionFactory) ctx
                    .getBean("sessionFactory");
            setSessionFactory(sessionFactory);
        } else {
            log.warn("No session factory found. Cannot initialize");
        }
    }

    /**
     * Initializes the metadata needed by this instance.
     * 
     * @param sessionFactory the Hibernate session factory
     * @see org.hibernate.SessionFactory#getAllClassMetadata()
     */
    public void setSessionFactory(SessionFactory sessionFactory) {

        if (initialized) {
            return; // EARLY EXIT !!
        }

        log.info("Calculating ExtendedMetadata...");

        SessionFactoryImplementor sfi = (SessionFactoryImplementor) sessionFactory;
        Map<String, ClassMetadata> m = sessionFactory.getAllClassMetadata();

        // do Locks() first because they are used during the
        // calculation of LockedBy()
        for (String key : m.keySet()) {
            ClassMetadata cm = m.get(key);
            locksHolder.put(key, new Locks(cm));
        }

        // now that all Locks() are available, determine LockedBy()
        for (String key : m.keySet()) {
            lockedByHolder.put(key, lockedByFields(key, m));
        }

        for (String key : m.keySet()) {
            ClassMetadata cm = m.get(key);
            immutablesHolder.put(key, new Immutables(cm));
        }

        for (Map.Entry<String, ClassMetadata> entry : m.entrySet()) {
            String key = entry.getKey();
            ClassMetadata cm = entry.getValue();
            key = key.substring(key.lastIndexOf(".") + 1).toLowerCase();
            if (hibernateClasses.containsKey(key)) {
                throw new RuntimeException("Duplicate keys!: " + key);
            }
            hibernateClasses.put(key, cm.getMappedClass(EntityMode.POJO));
        }

        for (String key : m.keySet()) {
            Map<String, Relationship> value = new HashMap<String, Relationship>();
            ClassMetadata cm = m.get(key);
            for (Class<?> c : hierarchy(m, key)) {
                Locks locks = locksHolder.get(c.getName());
                locks.fillRelationships(sfi, value);
            }

            // FIXME: using simple name rather than FQN
            Map<String, Relationship> value2 = new HashMap<String, Relationship>();
            for (Map.Entry<String, Relationship> i : value.entrySet()) {
                String k = i.getKey();
                k = k.substring(k.lastIndexOf(".")+1);
                value2.put(k, i.getValue());
            }
            relationships.put(key.substring(key.lastIndexOf(".")+1), value2);

            /* note map properties */
            boolean hasMapProperty = false;
            final String[] propertyNames = cm.getPropertyNames();
            final Type[] propertyTypes = cm.getPropertyTypes();
            for (int i = 0; i < propertyNames.length; i++) {
                if (propertyTypes[i] instanceof CollectionType && Map.class == propertyTypes[i].getReturnedClass()) {
                    final CollectionType propertyType = (CollectionType) propertyTypes[i];
                    final Type elementType = propertyType.getElementType((SessionFactoryImplementor) sessionFactory);
                    if (String.class == elementType.getReturnedClass()) {
                        mapProperties.put(key, propertyNames[i]);
                        hasMapProperty = true;
                    }
                }
            }
            if (hasMapProperty) {
                for (Class<?> mc = cm.getMappedClass(EntityMode.POJO); mc != null; mc = mc.getSuperclass()) {
                    mapPropertyClasses.add(mc);
                }
            }
        }

        Set<Class<IAnnotated>> anns = new HashSet<Class<IAnnotated>>();
        Set<Class<Annotation>> anns2 = new HashSet<Class<Annotation>>();
        for (String key : m.keySet()) {
            ClassMetadata cm = m.get(key);
            Map<String, String> queries = countQueriesAndEditTargets(key,
                    lockedByHolder.get(key));
            collectionCountHolder.putAll(queries);

            // Checking classes, specifically for ITypes
            Class c = cm.getMappedClass(EntityMode.POJO);
            if (IAnnotated.class.isAssignableFrom(c)) {
                anns.add(c);
            }
            if (Annotation.class.isAssignableFrom(c)) {
                anns2.add(c);
            }
        }
        annotatableTypes.addAll(anns);
        annotationTypes.addAll(anns2);
        initialized = true;
    }

    public Set<String> getClasses() {
        return locksHolder.keySet();
    }

    public Class<IObject> getHibernateClass(String table) {
        int idx = table.lastIndexOf(".");
        if (idx > 0) {
            table = table.substring(idx+1);
        }
        table = table.toLowerCase();
        return hibernateClasses.get(table);
    }

    private Relationship _getRelationship(String from, String to) {
        Map<String, Relationship> m = relationships.get(from);
        if (m != null) {
            return m.get(to);
        }
        return null;
    }

    /**
     * Walks the data on what locks what
     * for "from" argument to see if there is any direct relationship to the
     * "to" argument. If there is, the name will be returned. Otherwise, null.
     */
    public String getRelationship(String from, String to) {
        Relationship r = _getRelationship(from, to);
        if (r != null) {
            return r.relationshipName;
        }
        return null;
    }

    /**
     * Note: this implementation does not yet take into account the mapping
     * of joined subclasses like Job->UpdateJob.
     */
    public String getSQLJoin(String fromType, String fromAlias, String toType, String toAlias) {
        String fromPath = "UNKNOWN";
        String toPath = "UNKNOWN";


        final Relationship fromRel = _getRelationship(fromType, toType);
        final Relationship toRel = _getRelationship(toType, fromType);

        if (fromRel != null && !fromRel.collection) {
            fromPath = fromRel.relationshipName;
            toPath = "id";
        } else if (toRel != null && !toRel.collection) {
            toPath = toRel.relationshipName;
            fromPath = "id";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("fromType=");
            sb.append(fromType);
            sb.append(";toType=");
            sb.append(toType);
            throw new InternalException("Unhandled SQL Join! -- " + sb);
        }

        return String.format("%s.%s = %s.%s",
                fromAlias, fromPath, toAlias, toPath);
    }

    public Set<Class<IAnnotated>> getAnnotatableTypes() {
        return Collections.unmodifiableSet(annotatableTypes);
    }

    public Set<Class<Annotation>> getAnnotationTypes() {
        return Collections.unmodifiableSet(annotationTypes);
    }

    /**
     * walks the {@link IObject} argument <em>non-</em>recursively and gathers
     * all {@link IObject} instances which will be linked to by the
     * creation or updating of the argument. (Previously this was called "locking"
     * since a flag was set on the object to mark it as linked, but this was 
     * removed in 4.2)
     * 
     * @param iObject
     *            A newly created or updated {@link IObject} instance which
     *            might possibly lock other {@link IObject IObjects}. A null
     *            argument will return an empty array to be checked.
     * @return A non-null array of {@link IObject IObjects} which will be linked to.
     */
    public IObject[] getLockCandidates(IObject iObject) {
        if (iObject == null) {
            return new IObject[] {};
        }

        Locks l = locksHolder.get(iObject.getClass().getName());
        return l.getLockCandidates(iObject);
    }

    public String[][] getLockCandidateChecks(Class<? extends IObject> k, boolean onlyWithGroups) {
        Locks l = locksHolder.get(k.getName());
        return l.getLockCandidateChecks(onlyWithGroups);
    }

    /**
     * returns all class/field name pairs which may possibly link to an object
     * of type <code>klass</code>.
     * 
     * @param klass
     *            Non-null {@link Class subclass} of {@link IObject}
     * @return A non-null array of {@link String} queries which can be used to
     *         determine if an {@link IObject} instance can be unlocked.
     */
    public String[][] getLockChecks(Class<? extends IObject> klass) {
        if (klass == null) {
            throw new ApiUsageException("Cannot proceed with null klass.");
        }

        String[][] checks = lockedByHolder.get(klass.getName());

        if (checks == null) {
            throw new ApiUsageException("Metadata not found for: "
                    + klass.getName());
        }

        return checks;
    }

    public Map<String, Long> countLocks(final Session session, final Long id,
            String[][] checks, String clause) {

        final QueryBuilder qb = new QueryBuilder();
        qb.select("count(x.id)");
        qb.from("%s", "x");

        // Only one of the these two will happen, so the second replacement
        // argument to String.format will have to be check[1].
        if (id == null) {
            qb.join("x.%s", "y", false, false);
        }


        if (id != null) {
            qb.where();
            qb.and("%s.id = :id");
        }

        if (clause != null && clause.length() > 0) {
            qb.where();
            qb.and(clause);
            qb.appendSpace();
        }


        final String queryString = qb.queryString();
        final Map<String, Long> counts = new HashMap<String, Long>();
        long total = 0L;

        // run the individual queries
        for (final String[] check : checks) {

            final String hql = String.format(queryString, check[0], check[1]);

            org.hibernate.Query q = session.createQuery(hql);
            if (id != null) {
                q.setLong("id", id);
            }

            Long count = (Long) q.uniqueResult();

            if (count != null && count.longValue() > 0) {
                total += count;
                counts.put(check[0], count);
            }
        }
        counts.put("*", total);
        return counts;

    }

    public String[] getImmutableFields(Class<? extends IObject> klass) {
        if (klass == null) {
            throw new ApiUsageException("Cannot proceed with null klass.");
        }

        Immutables i = immutablesHolder.get(klass.getName());
        return i.getImmutableFields();

    }

    private final static String field_msg = " is not a valid field for counting. Make sure you use "
            + "the single-valued (e.g. ImageAnnotation.IMAGE) and "
            + "not the collection-valued (e.g. Image.ANNOTATIONS) end.";

    /**
     * Returns the query for obtaining the number of collection items to a
     * particular instance. All such queries will return a ResultSet with rows
     * of the form: 0 (Long) id of the locked class 1 (Long) count of the
     * instances locking that class
     * 
     * @param field
     *            Field name as specified in the class.
     * @return String query. Never null.
     * @throws ApiUsageException
     *             if return value would be null.
     */
    public String getCountQuery(String field) throws ApiUsageException {
        String q = collectionCountHolder.get(field);
        if (q == null) {
            throw new ApiUsageException(field + field_msg);
        }
        return q;
    }

    /**
     * Returns the {@link IObject} type which a given field points to. E.g.
     * getTargetType(ImageAnnotation.IMAGE) returns Image.class.
     */
    public Class<IObject> getTargetType(String field) throws ApiUsageException {
        Class<IObject> k = targetHolder.get(field);
        if (k == null) {
            throw new ApiUsageException(field + field_msg);
        }
        return k;
    }

    @Override
    public boolean mayHaveMapProperties(Class<? extends IObject> iObjectClass) {
        return mapPropertyClasses.contains(iObjectClass);
    }

    @Override
    public Set<String> getMapProperties(String className) {
        return mapProperties.get(className);
    }

    // ~ Helpers
    // =========================================================================

    /**
     * examines all model objects to see which fields contain a {@link Type}
     * which points to this class. Uses {@link #locksFields(Type[])} since this
     * is the inverse process.
     */
    private String[][] lockedByFields(String klass, Map<String, ClassMetadata> m) {
        if (m == null) {
            throw new InternalException("ClassMetadata map cannot be null.");
        }

        List<String[]> fields = new ArrayList<String[]>();

        for (String k : m.keySet()) {
            ClassMetadata cm = m.get(k);
            Type[] type = cm.getPropertyTypes();
            String[] names = cm.getPropertyNames();
            Locks inverse = locksHolder.get(k);
            for (int i = 0; i < inverse.size(); i++) {

                if (!inverse.include(i)) {
                    continue;
                }

                // this is an embedded component and must be treated
                // specially. specifically, that we cannot compare against
                // the top-level returnedClass name but rather against
                // each of the individual subtype returnedClass names.
                if (inverse.hasSubtypes(i)) {
                    for (int j = 0; j < inverse.numberOfSubtypes(i); j++) {
                        if (inverse.subtypeEquals(i, j, klass)) {
                            fields.add(new String[] { k,
                                    inverse.subtypeName(i, j) });
                        }
                    }
                }

                // no subtypes so can compare directly
                else if (klass.equals(type[i].getReturnedClass().getName())) {
                    fields.add(new String[] { k, names[i] });
                }
            }
        }
        return fields.toArray(new String[fields.size()][2]);
    }

    /**
     * Pre-builds all queries for checking the count of collections based on the
     * field names as defined in the ome.model.* classes.
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> countQueriesAndEditTargets(String type,
            String[][] lockedBy) {
        Map<String, String> queries = new HashMap<String, String>();

        for (int t = 0; t < lockedBy.length; t++) {
            String ltype = lockedBy[t][0];
            String lfield = lockedBy[t][1];

            String field_description = String.format("%s_%s", ltype, lfield);
            queries.put(field_description, String.format(
                    "select target.%s.id, count(target) "
                            + "from %s target group by target.%s.id", lfield,
                    ltype, lfield));

            try {
                targetHolder.put(field_description, (Class<IObject>) Class
                        .forName(type));
            } catch (Exception e) {
                throw new RuntimeException("Error getting class: " + ltype, e);
            }
        }

        return queries;
    }

    /**
     * Takes a class name as a string and find that class and all of its
     * subclasses (transitively) returns them as a list.
     */
    private static List<Class<?>> hierarchy(Map<String, ClassMetadata> m, String key) {

        final List<Class<?>> h = new ArrayList<Class<?>>();

        ClassMetadata cm = m.get(key);
        Class<?> c = cm.getMappedClass(EntityMode.POJO);
        h.add(c);

        int index = 0;

        while (index < h.size()) {
            for (String key2 : m.keySet()) {
                if (key.equals(key2)) {
                    continue;
                } else {
                    cm = m.get(key2);
                    c = cm.getMappedClass(EntityMode.POJO);
                    if (c.getSuperclass().equals(h.get(index))) {
                        h.add(c);
                    }
                }
            }
            index++;
        }
        return h;
    }

}

/**
 * Simple value class to maintain all of the state for use by
 * {@link ExtendedMetadata#getRelationship(String, String)} and
 * {@link ExtendedMetadata#getSQLJoin(String, String, String, String)}.
 */
class Relationship {

    /**
     * Value to be returned by {@link ExtendedMetadata#getRelationship(String, String)}.
     */
    private final String relationshipName;

    /**
     * Whether or not the given relationship is the many valued side, i.e.
     * represents a collection. In this case, there will not be a column
     * of the given name to join on for
     * {@link ExtendedMetadata#getSQLJoin(String, String, String, String)}.
     */
    private final boolean collection;

    Relationship(String name, boolean collection) {
        this.relationshipName = name;
        this.collection = collection;
    }
}

/**
 * inner class which wraps the information (index number, path, etc) related to
 * what fields a particular object can lock. This is fairly complicated because
 * though the properties available are a simple array, some of those properties
 * can actually be embedded components, meaning that the value of the property
 * is the instance itself. In those cases (where {@link #hasSubtypes(int)} is
 * true, special logic must be implemented to retrieve the proper values.
 */
class Locks {
    private final ClassMetadata cm;

    private final int size;

    private int total = 0;

    private final boolean[] include;

    private final String[][] subnames;

    private final Type[][] subtypes;

    private final String[][] checks;

    private final String[][] groupChecks;

    /**
     * examines all {@link Type types} for this class and stores pointers to
     * those fields which represent {@link IObject} instances. These fields may
     * need to be locked when an object of this type is created or updated.
     */
    Locks(ClassMetadata classMetadata) {

        this.cm = classMetadata;
        String[] name = cm.getPropertyNames();
        Type[] type = cm.getPropertyTypes();
        List<String[]> checks = new ArrayList<String[]>();
        List<String[]> groupChecks = new ArrayList<String[]>();

        this.size = type.length;
        this.include = new boolean[size];
        this.subnames = new String[size][];
        this.subtypes = new Type[size][];

        for (int i = 0; i < type.length; i++) {
            if (type[i].isComponentType()
                    && ((ComponentType) type[i]).isEmbedded()) {
                EmbeddedComponentType embedded = (EmbeddedComponentType) type[i];
                String[] sub_name = embedded.getPropertyNames();
                Type[] sub_type = embedded.getSubtypes();
                List<String> name_list = new ArrayList<String>();
                List<Type> type_list = new ArrayList<Type>();
                for (int j = 0; j < sub_type.length; j++) {
                    if (IObject.class.isAssignableFrom(sub_type[j]
                            .getReturnedClass())) {
                        String path = name[i] + "." + sub_name[j];
                        name_list.add(path);
                        type_list.add(sub_type[j]);

                        addCheck(checks, groupChecks, sub_type[j].getReturnedClass(), path);

                    }
                }
                add(i, name_list.toArray(new String[name_list.size()]),
                        type_list.toArray(new Type[type_list.size()]));
            } else if (IObject.class.isAssignableFrom(type[i]
                    .getReturnedClass())) {
                add(i);
                addCheck(checks, groupChecks, type[i].getReturnedClass(), name[i]);
                // Create checks for

            }
        }
        this.checks = checks.toArray(new String[checks.size()][]);
        this.groupChecks = groupChecks.toArray(new String[groupChecks.size()][]);
    }

    private void addCheck(List<String[]> checks, List<String[]> groupChecks,
            Class type, String field) {
        String[] s = new String[]{type.getName(), field};
        checks.add(s);
        if (!IGlobal.class.isAssignableFrom(type)) {
            groupChecks.add(s);
        }
    }

    private void add(int i) {
        if (i >= size) {
            throw new IllegalArgumentException("size");
        }
        if (this.include[i] == true) {
            throw new IllegalStateException("set");
        }

        this.include[i] = true;
        this.subnames[i] = new String[] {};
        this.subtypes[i] = new Type[] {};
        total++;
    }

    private void add(int i, String[] paths, Type[] types) {
        if (i >= size) {
            throw new IllegalArgumentException("size");
        }
        if (paths == null) {
            throw new IllegalArgumentException("paths");
        }
        if (types == null) {
            throw new IllegalArgumentException("types");
        }
        if (paths.length != types.length) {
            throw new IllegalStateException("size");
        }
        if (this.include[i] == true) {
            throw new IllegalStateException("set");
        }

        if (paths.length > 0) {
            this.include[i] = true;
            this.subnames[i] = paths;
            this.subtypes[i] = types;
            total += paths.length;
        }
    }

    // ~ Main method
    // =========================================================================

    /**
     * For each of the fields contained in this {@link Locks} object, parse
     * out the type and the field name and store those as the key and value
     * in the "value" argument.
     */
    public void fillRelationships(SessionFactoryImplementor sfi,
            Map<String, Relationship> value) {

        final Type[] types = cm.getPropertyTypes();
        for (int t = 0; t < types.length; t++) {

            final Type type = types[t];
            final String name = type.getName();

            String to = null;
            Relationship field = null;
            if (type instanceof EntityType) {
                final EntityType entType = (EntityType) type;
                to = entType.getAssociatedEntityName();
                field = new Relationship(cm.getPropertyNames()[t], false);


            } else if (types[t] instanceof CollectionType) {
                final CollectionType colType = (CollectionType)types[t];
                final Type elemType = colType.getElementType(sfi);
                if (!elemType.isEntityType()) {
                    continue; // The case for count maps and other primitives.
                }
                to = elemType.getName();

                int open = name.indexOf("(");
                int close = name.lastIndexOf(")");
                String role = name.substring(open + 1, close);
                int dot = role.lastIndexOf(".");
                field = new Relationship(role.substring(dot+1), true);

            }

            if (to != null && field != null) {
                Map<String, ClassMetadata> m = sfi.getAllClassMetadata();
                for (Class<?> c : Impl.hierarchy(m, to)) {
                    value.put(c.getName(), field);
                }
            }
        }
    }

    public IObject[] getLockCandidates(IObject o) {
        int idx = 0;
        IObject[] toCheck = new IObject[total()];
        Object[] values = cm.getPropertyValues(o, EntityMode.POJO);
        for (int i = 0; i < size(); i++) {
            if (!include(i)) {
                continue;
            }

            // this relation has subtypes and therefore is an embedded
            // component. This means that the value in values[] is the
            // instance itself. we will now have to acquire the actual
            // component values.
            if (hasSubtypes(i)) {
                for (int j = 0; j < numberOfSubtypes(i); j++) {
                    Object value = getSubtypeValue(i, j, o);
                    if (value != null) {
                        toCheck[idx++] = (IObject) value;
                    }
                }
            }

            // this is a regular relation. if the value is non null,
            // add it to the list of candidates.
            else if (values[i] != null) {
                toCheck[idx++] = (IObject) values[i];
            }
        }

        IObject[] retVal;
        retVal = new IObject[idx];
        System.arraycopy(toCheck, 0, retVal, 0, idx);
        return retVal;
    }

    public String[][] getLockCandidateChecks(boolean onlyWithGroups) {
        if (onlyWithGroups) {
            return groupChecks;
        }
        return checks;
    }

    // ~ Public
    // =========================================================================
    // public methods. know nothing about the arrays above. have only a
    // linear view of the contained values.

    /**
     * the total number of fields for this entity. The actual number of
     * {@link IObject} instances may vary since (1) some fields (like embedded
     * components) can possibly point to multiple instances. See
     * {@link #total()} for the final size and (2) some fields do not need to be
     * examined (Integers, e.g.). See {@link #include}
     * @return how many fields this entity has
     */
    public int size() {
        return size;
    }

    /**
     * as opposed to {@link #size()}, the returns the actual number of fields
     * that will need to be checked.
     * @return how many fields must be checked for this entity
     */
    public int total() {
        return total;
    }

    /**
     * returns true if this offset points to a field which may contain an
     * {@link IObject} instance
     */
    public boolean include(int i) {
        return include[i];
    }

    // ~ Subtypes
    // =========================================================================

    /**
     * returns true if this offset points to a field which is an embedded
     * component.
     */
    public boolean hasSubtypes(int i) {
        return include(i) && subtypes[i].length > 0;
    }

    /**
     * returns the number of subtypes for iterating over this secondary array.
     * If there are no subtypes, this method will return zero. Use
     * {@link #hasSubtypes(int)} to differentiate the two situations.
     */
    public int numberOfSubtypes(int i) {
        return hasSubtypes(i) ? subtypes[i].length : 0;
    }

    /**
     * uses the {@link ClassMetadata} for this {@link Locks} tp retrieve the
     * component value.
     */
    public Object getSubtypeValue(int i, int j, Object o) {
        return cm.getPropertyValue(o, subnames[i][j], EntityMode.POJO);
    }

    /**
     * returns true is the indexed subtype returns the same class type as the
     * klass argument.
     */
    public boolean subtypeEquals(int i, int j, String klass) {
        return klass.equals(subtypes[i][j].getReturnedClass().getName());
    }

    /** retrieves the full Hibernate path for this component field. */
    public String subtypeName(int i, int j) {
        return subnames[i][j];
    }
}

class Immutables {
    String[] immutableFields;

    EntityPersister ep;

    Immutables(ClassMetadata metadata) {
        if (metadata instanceof EntityPersister) {
            this.ep = (EntityPersister) metadata;
        } else {
            throw new IllegalArgumentException("Metadata passed to Immutables"
                    + " must be an instanceof EntityPersister, not "
                    + (metadata == null ? null : metadata.getClass()));
        }

        List<String> retVal = new ArrayList<String>();
        Type[] type = this.ep.getPropertyTypes();
        String[] name = this.ep.getPropertyNames();
        boolean[] up = this.ep.getPropertyUpdateability();

        for (int i = 0; i < type.length; i++) {

            // not updateable, so our work (for this type) is done.
            if (!up[i]) {
                retVal.add(name[i]);
            }

            // updateable, but maybe a component subtype is NOT.
            else if (type[i].isComponentType()
                    && ((ComponentType) type[i]).isEmbedded()) {
                EmbeddedComponentType embedded = (EmbeddedComponentType) type[i];
                String[] sub_name = embedded.getPropertyNames();
                Type[] sub_type = embedded.getSubtypes();
                List<String> name_list = new ArrayList<String>();
                List<Type> type_list = new ArrayList<Type>();
                for (int j = 0; j < sub_type.length; j++) {
                    // INCOMPLETE !!!
                }
            }
        }
        immutableFields = retVal.toArray(new String[retVal.size()]);
    }

    public String[] getImmutableFields() {
        return immutableFields;
    }
}
}
