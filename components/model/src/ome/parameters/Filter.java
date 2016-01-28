/*
 * ome.parameters.Filter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * parameter to generally reduce the size of a query result set.
 * 
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0-M2
 */
public class Filter implements Serializable {

    /**
     * flag determining if a {@code ome.services.query.Query} will attempt to
     * return a single value <em>if supported</em>.
     */
    private boolean unique = false;

    private long id_owner = -1, id_group = -1;

    public Integer limit;
    
    public Integer offset;

    public Timestamp startTime, endTime;

    public Filter page(Integer offset, Integer limit) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }
    
    // ~ Flags
    // =========================================================================
    /**
     * state that this Filter should only return a single value if possible. By
     * default, a Filter will make no assumptions regarding the uniquesness of a
     * query.
     */
    public Filter unique() {
        unique = true;
        return this;
    }

    /**
     * check uniqueness for this query. Participating queries will attempt to
     * call <code>uniqueResult</code> rather than <code>list</code>. This may
     * throw a {@link ome.conditions.ValidationException} on execution.
     */
    public boolean isUnique() {
        return unique;
    }

    // ~ Owner
    // =========================================================================
    public Filter owner(long ownerId) {
        id_owner = ownerId;
        return this;
    }

    public long owner() {
        return id_owner;
    }

    public Filter group(long groupId) {
        id_group = groupId;
        return this;
    }

    public long group() {
        return id_group;
    }

    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 60649802598825408L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("F(");
        if (offset != null) {
            sb.append("o");
            sb.append(offset);
        }
        if (limit != null) {
            sb.append("l");
            sb.append(limit);
        }
        if (id_owner >= 0) {
            sb.append("u");
            sb.append(id_owner);
        }
        if (id_group >= 0) {
            sb.append("g");
            sb.append(id_group);
        }
        if (unique) {
            sb.append("U");
        }
        sb.append(")");
        return sb.toString();
    }
}
