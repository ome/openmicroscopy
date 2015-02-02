/*
 *   Copyright (C) 2010-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.db;

import java.util.List;

import junit.framework.TestCase;
import ome.model.IObject;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.testng.annotations.Test;

import static org.apache.lucene.util.Version.LUCENE_30;

@Test( groups = "integration" )
public class SearchTest extends TestCase {

    private static Logger log = LoggerFactory.getLogger(SearchTest.class);

    @Test
    public void testEvent() throws Exception {
        HibernateTest ht = new HibernateTest();
        ht.setupSession();
        ht.createEvent();
        ht.closeSession();

        ht.setupSession();
        String queryStr = "manual";
        List<Event> list = query(ht, queryStr, Event.class, "status");
        assertTrue(list.size() > 0);
    }

    @Test
    public void testReindexingExperimenter() throws Exception {
        HibernateTest ht = new HibernateTest();
        ht.setupSession();

        FullTextSession fullTextSession = Search.getFullTextSession(ht.s);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        Transaction transaction = fullTextSession.beginTransaction();
        // Scrollable results will avoid loading too many objects in memory
        ScrollableResults results = fullTextSession.createCriteria(
                Experimenter.class).scroll(ScrollMode.FORWARD_ONLY);
        int index = 0;
        int batchSize = 10;
        while (results.next()) {
            index++;
            fullTextSession.index(results.get(0)); // index each element
            if (index % batchSize == 0) {
                ht.s.clear(); // clear every batchSize since the queue is
                // processed
            }
        }
        ht.closeSession();

        ht.setupSession();
        List<Experimenter> list = query(ht, "root", Experimenter.class,
                "omeName");
        assertTrue(list.toString(), list.size() == 1);
        ht.closeSession();
    }

    @Test
    public void testMultipleClassSearch() throws Exception {
        HibernateTest ht = new HibernateTest();
        ht.setupSession();

        String queryStr = "root OR manual";
        QueryParser qp = new MultiFieldQueryParser(LUCENE_30,
                new String[] { "omeName", "status" },
                new StandardAnalyzer(LUCENE_30));
        org.apache.lucene.search.Query lq = qp.parse(queryStr);
        FullTextSession fts = Search.getFullTextSession(ht.s);
        Query q = fts.createFullTextQuery(lq, Event.class, Experimenter.class);
        List list = q.list();
        assertTrue(list.toString(), list.size() > 1);

        ht.closeSession();
    }

    // ===========================================================

    private <T extends IObject> List<T> query(HibernateTest ht,
            String queryStr, Class<T> k, String... fields)
            throws ParseException {
        MultiFieldQueryParser parser = new MultiFieldQueryParser(LUCENE_30, fields,
                new StandardAnalyzer(LUCENE_30));
        org.apache.lucene.search.Query luceneQuery = parser.parse(queryStr);
        FullTextSession fts = Search.getFullTextSession(ht.s);
        Query q = fts.createFullTextQuery(luceneQuery, k);
        List<T> list = q.list();
        return list;
    }

}
