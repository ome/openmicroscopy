/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.ArrayList;
import java.util.List;

import ome.api.IQuery;
import ome.model.IObject;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delegate to {@link IQuery#findAllByQuery(String, Parameters)}. Uses the
 * {@link SearchAction#chainedList} list to fill the named parameter ":IDLIST"
 * if present.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class HqlQuery extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String query;
    private final Parameters params;

    public HqlQuery(SearchValues values, String query, Parameters p) {
        super(values);
        if (query == null || query.length() < 1) {
            throw new IllegalArgumentException("Query string must be non-empty");
        }
        this.query = query;
        this.params = p;
    }

    @Transactional(readOnly = true)
    public Object doWork(Session session, ServiceFactory sf) {

        Parameters _p = this.params;

        // If contained then we need to handle it.
        if (this.query.contains("IDLIST")) {

            QueryParameter qp;

            // Initialize.
            if (_p == null) {
                _p = new Parameters();
                qp = null;
            } else {
                qp = _p.get("IDLIST");
            }

            if (qp != null) {
                // User set something specifically. Move along.
            } else {

                List<Long> ids = new ArrayList<Long>();

                if (this.chainedList == null || this.chainedList.size() == 0) {
                    // No results, but Hibernate cannot handle
                    // empty lists so we set this to a non-existant
                    // id.
                    ids.add(-1L);
                } else {
                    for (IObject obj : chainedList) {
                        if (obj != null) {
                            ids.add(obj.getId());
                        }
                    }
                }
                _p.addList("IDLIST", ids);

            }
        }
        return sf.getQueryService().findAllByQuery(query, _p);
    }
}
