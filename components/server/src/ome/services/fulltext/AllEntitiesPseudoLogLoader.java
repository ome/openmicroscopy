/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;

/**
 * {@link EventLogLoader} which iterates through each object model type (in no
 * particular order) and returns each object from lowest to highest id.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */

public class AllEntitiesPseudoLogLoader<T extends IObject> extends
        EventLogLoader {

    List<String> classes;
    String current = null;
    long last = -1;

    public void setClasses(Set<String> classes) {
        this.classes = new ArrayList<String>(classes);
    }

    @Override
    protected EventLog query() {
        if (current == null) {
            if (!more()) {
                return null;
            }
            current = classes.remove(0);
        }

        final String query = String.format(
                "select obj from %s obj where obj.id > %d order by id asc",
                current, last);
        final IObject obj = queryService.findByQuery(query, new Parameters(
                new Filter().page(0, 1)));

        if (obj != null) {
            last = obj.getId();
            // Here we pass the string to prevent $$ CGLIB style issues
            return wrap(current, obj);
        } else {
            // If no object, then reset and recurse.
            current = null;
            last = -1;
            return query();
        }
    }

    @Override
    public boolean more() {
        return classes.size() > 0;
    }

    protected EventLog wrap(String cls, IObject obj) {
        EventLog el = new EventLog();
        el.setEntityType(cls);
        el.setEntityId(obj.getId());
        el.setAction("UPDATE");
        return el;
    }
}