/*
 * ome.parameters.Page
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;

// Java imports
import java.io.Serializable;

// Third-party libraries

// Application-internal dependencies
import ome.conditions.ValidationException;

/**
 * parameter which specifies the offset and limit of the result.
 * 
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since 3.0-M2
 */
public class Page implements Serializable {

    final private int m_offset;

    final private int m_limit;

    public Page(int offset, int limit) {
        validate(offset, limit);
        m_offset = offset;
        m_limit = limit;
    }

    public int offset() {
        return m_offset;
    }

    /**
     * the defined limit for this Page. No more than this number of items will
     * be returned from any query.
     * 
     * @return
     */
    public int limit() {
        return m_limit;
    }

    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 1135303821179948L;

    public boolean equals(Object obj) {
        if (!(obj instanceof Page))
            return false;

        Page p = (Page) obj;

        if (this == p)
            return true;

        if (p.m_offset != this.m_offset)
            return false;

        if (p.m_limit != this.m_limit)
            return false;

        return true;
    }

    public int hashCode() {
        int result = 11;
        result = 17 * result + m_offset;
        result = 19 * result + m_limit;
        return result;
    }

    // ~ Helpers
    // =========================================================================
    private void validate(int offset, int limit) {
        if (offset < 0)
            throw new ValidationException("Offset cannot be less than zero.");

        if (limit < 0)
            throw new ValidationException("Limit cannot be less than zero.");

    }

}
