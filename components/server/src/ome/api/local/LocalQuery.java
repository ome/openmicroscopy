/*
 * ome.api.local.LocalQuery
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

import org.springframework.orm.hibernate3.HibernateCallback;

import ome.services.query.Query;

/**
 * Provides local (internal) extensions for querying
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @since OMERO3.0
 */
public interface LocalQuery extends ome.api.IQuery {

    /**
     * creates and returns a convenience Dao which provides generics despite the
     * &lt; Java5 requirement on {@link ome.api.IQuery}.
     * 
     * @param <T>
     * @return
     */
    // TODO <T extends IObject> Dao<T> getDao();
    /**
     * Executes a {@link HibernateCallback}
     * 
     * @param callback
     *            An implementation of the HibernateCallback interface.
     * 
     * @see org.springframework.orm.hibernate3.HibernateTemplate
     * @see org.springframework.orm.hibernate3.HibernateCallback
     */
    <T> T execute(HibernateCallback callback);

    /**
     * Executes a locally defined Query.
     * 
     * @param query
     *            A subclass of the {@link Query} interface.
     * @return result of the query See document for the query for the return
     *         type.
     */
    <T> T execute(Query<T> query);

    /**
     * Tests if an object is currently contained in the session.
     * 
     * @param object
     */
    boolean contains(Object object);

    /**
     * Removes an object graph from the session. This allows for non-permanent,
     * mutable calls on the graph.
     * 
     * @param object
     */
    void evict(Object object);

	void clear();

    /**
     * Uses the Hibernate static method <code>initialize</code> to prepare an
     * object for shipping over the wire.
     * 
     * It is better to do this in your queries.
     * 
     * @param object
     * @see org.hibernate.Hibernate
     */
    void initialize(Object object);

    /**
     * Checks if a type has been mapped in Hibernate.
     * 
     * @param type
     *            String representation of a full-qualified Hibernate-mapped
     *            type.
     * @return yes or no.
     */
    boolean checkType(String type);

    /**
     * Checks if a property is defined on a mapped Hibernate type.
     * 
     * @param type
     *            String representation of a full-qualified Hibernate-mapped
     *            type.
     * @param property
     *            Property as defined in Hibernate NOT the public final static
     *            Strings on our IObject classes.
     * @return yes or no.
     */
    boolean checkProperty(String type, String property);

}
