/*
 * ome.parameters.Page
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;

import java.util.ArrayList;
import java.util.Collection;

import ome.conditions.ApiUsageException;

/**
 * parameter which defines the ordering as well as the start and offset for a
 * List-valued result set.
 * 
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0-M2
 */
public class Period {
    final private int m_offset;

    final private int m_limit;

    final private Collection m_order = new ArrayList();

    public Period(int offset, int limit) {
        m_offset = offset;
        m_limit = limit;
    }

    public Period add(String field) {
        if (null == field) {
            throw new ApiUsageException(
                    "Field name argument to addOrder cannot be null.");
        }
        m_order.add(field);
        return this;
    }

}
