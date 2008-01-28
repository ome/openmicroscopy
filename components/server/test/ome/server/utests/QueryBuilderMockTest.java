/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Date;
import java.util.HashSet;

import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class QueryBuilderMockTest extends MockObjectTestCase {

    protected Mock mockSession, mockQuery;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        createMocks();
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        super.verify();
        super.tearDown();
    }

    protected void createMocks() {
        mockSession = mock(Session.class);
        mockQuery = mock(Query.class);
        mockSession.expects(once()).method("createQuery").will(
                returnValue(mockQuery.proxy()));

    }

    protected Session session() {
        return (Session) mockSession.proxy();
    }

    @Test
    public void testBasicUsage() {

        QueryBuilder qb = new QueryBuilder();

        qb.select("img");
        qb.from("Image", "img");
        qb.join("img.pixels", "pix", true, false);
        qb.join("img.datasetImageLinks", "dil", false, false);
        qb.join("pix.pixelsType", "pt", true, true);
        // qb.cond(Dataset.class.equals(cls)).fetch("img.datasetLink")

        // Can't join anymore after this
        qb.where(); // First
        qb.append("(");
        qb.and("pt.details.creationTime > :time");
        qb.param("time", new Date());
        qb.append(")");
        qb.and("img.id in (:ids)");
        qb.paramList("ids", new HashSet());
        qb.order("img.id", true);
        qb.order("this.details.creationEvent.time", false);
        mockQuery.expects(once()).method("setParameter").with(this.ANYTHING,
                this.ANYTHING);
        mockQuery.expects(once()).method("setParameterList").with(
                this.ANYTHING, this.ANYTHING);
        Query q = qb.query(session());
    }
}
