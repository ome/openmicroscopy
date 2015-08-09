/*
 * ome.model.ILink
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

/**
 * interface for all mutable domain objects. Provides access to the version
 * property which the backend uses for optimistic locking. An object with an id
 * but without a version passed to the backend is considered an error, since
 * some backends will silently create a new object in the database.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @author josh
 * 
 */
public interface IMutable extends IObject {

    /** optimistic-lock version. Usually managed by the backend. */
    public Integer getVersion();

    /**
     * use with caution. In general, the version should only be altered by the
     * backend. In the best case, an exception will be thrown for a version not
     * equal to the current DB value. In the worst (though rare) case, the new
     * version could match the database, and override optimistic lock checks
     * that are supposed to prevent data loss.
     * 
     * @param version
     *            Value for this objects version.
     */
    public void setVersion(Integer version);
    // TODO public Event getUpdateEvent();
    // TODO public void setUpdateEvent(Event e);

}
