/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.List;

import ome.model.IObject;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Intersection {@link SearchAction} which combines two other search actions
 * into one logical unit, e.g.
 * 
 * <pre>
 * A &amp;&amp; B
 * </pre>
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.Search#and()
 */
public class Intersection extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final SearchAction a;

    private final SearchAction b;

    public Intersection(SearchValues values, SearchAction a, SearchAction b) {
        super(values);
        Assert.notNull(a);
        Assert.notNull(b);
        this.a = a;
        this.b = b;
    }

    @Transactional(readOnly = true)
    public Object doWork(Session session, ServiceFactory sf) {

        List<IObject> rvA;
        List<IObject> rvB;

        rvA = (List<IObject>) a.doWork(session, sf);
        b.chainedSearch(rvA);
        rvB = (List<IObject>) b.doWork(session, sf);
        rvA.retainAll(rvB);

        return rvA;
    }
}
