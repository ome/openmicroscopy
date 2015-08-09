/*
 * ome.util.RdfPrinter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * walks an object graph producing RDF-like output.
 */
public class RdfPrinter extends ContextFilter {

    StringBuilder sb = new StringBuilder();

    public String getRdf() {
        return sb.toString();
    }

    void space() {
        sb.append(" ");
    }

    void newline() {
        sb.append("\n");
    }

    protected void entry(String field, Object o) {
        if (!Collection.class.isAssignableFrom(currentContext().getClass())) {
            sb.append(currentContext());
            space();
            sb.append(field);
            space();
            sb.append(o);
            newline();
        }
    }

    @Override
    public Object filter(String fieldId, Object o) {
        entry(fieldId, o);
        return super.filter(fieldId, o);
    }

    @Override
    public Filterable filter(String fieldId, Filterable f) {
        entry(fieldId, f);
        return super.filter(fieldId, f);
    }

    @Override
    public Collection filter(String fieldId, Collection c) {
        sb.append(currentContext() == null ? "" : currentContext());
        space();
        sb.append(fieldId);
        space();
        sb.append(" [ ");
        Collection result;
        if (c != null && c.size() > 0) {
            for (Iterator it = c.iterator(); it.hasNext();) {
                sb.append(it.next());

            }
        } else {
        }
        sb.append(" ] \n");
        return super.filter(fieldId, c);
    }
}
