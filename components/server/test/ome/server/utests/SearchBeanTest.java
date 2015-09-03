/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.model.IObject;
import ome.model.annotations.TagAnnotation;
import ome.model.core.Image;
import ome.services.SearchBean;
import ome.services.fulltext.FullTextAnalyzer;
import ome.services.search.SearchAction;
import ome.services.search.SearchValues;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.Session;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

public class SearchBeanTest extends MockObjectTestCase {

    protected Class<? extends Analyzer> analyzer = FullTextAnalyzer.class;
    protected Executor executor = new Executor.Impl(null, null, null, null) {
        @Override
        public Object execute(Principal p, Work work) {
            return work.doWork(null, null);
        }
    };

    protected SearchBean bean;

    @Test
    public void testDefaults() {
        bean = new SearchBean(executor, analyzer);
        assertDefaults();
    }

    void assertDefaults() {
        assertTrue(bean.activeQueries() == 0);
        assertTrue(bean.getBatchSize() == bean.DEFAULT_BATCH_SIZE);
        assertTrue(bean.isCaseSensitive() == bean.DEFAULT_CASE_SENSITIVTY);
        assertTrue(bean.isMergedBatches() == bean.DEFAULT_MERGED_BATCHES);
        assertTrue(bean.isReturnUnloaded() == bean.DEFAULT_RETURN_UNLOADED);
        assertTrue(bean.isUseProjections() == bean.DEFAULT_USE_PROJECTIONS);
    }

    @Test
    public void testBasicUsage() {
        bean = new SearchBean(executor, analyzer);
        bean.onlyType(Image.class);
        bean.onlyAnnotatedWith(TagAnnotation.class);
        bean.byFullText("Here's my query");
    }

    @Test
    public void testHasNextWithResults() {
        bean = new SearchBean(executor, analyzer);
        assertFalse(bean.hasNext());
        List<IObject> list = new ArrayList<IObject>();
        list.add(new Image());
        bean.addResult(list);
        assertTrue(bean.hasNext());
    }

    @Test
    public void testHasNextWithAction() {
        bean = new SearchBean(executor, analyzer);
        assertFalse(bean.hasNext());
        addActionWithResultOfSize_n(1);
        assertTrue(bean.hasNext());
    }

    @Test
    public void testActiveQueries() {
        bean = new SearchBean(executor, analyzer);
        addActionWithResultOfSize_n(1);
        assertTrue(bean.activeQueries() == 1);
        assertNotNull(bean.next());
        assertFalse(bean.hasNext());
        assertTrue(bean.activeQueries() == 0);
    }

    @Test
    public void testBatchSizeRollOver() {
        bean = new SearchBean(executor, analyzer);
        addActionWithResultOfSize_n(4);
        bean.setBatchSize(3);
        assertEquals(3, bean.getBatchSize());
        assertEquals(3, bean.results().size());
        assertEquals(1, bean.results().size());
    }

    @Test
    public void testMergedBatches() {
        bean = new SearchBean(executor, analyzer);
        addActionWithResultOfSize_n(3);
        addActionWithResultOfSize_n(1);
        bean.setBatchSize(5);
        assertEquals(4, bean.results().size());
    }

    @Test
    public void testUnloaded() {
        bean = new SearchBean(executor, analyzer);
        addActionWithResultOfSize_n(1);
        bean.setReturnUnloaded(true);
        IObject i = bean.next();
        assertFalse(i.isLoaded());
    }

    @Test
    public void testResetDefaults() {
        bean = new SearchBean(executor, analyzer);
        bean.setBatchSize(4);
        bean.setCaseSentivice(false);
        bean.setMergedBatches(true);
        bean.setReturnUnloaded(true);
        // Disallowed bean.setUseProjections(true);
        bean.resetDefaults();
        assertDefaults();
    }

    @Test
    public void testClearingQueries() {
        bean = new SearchBean(executor, analyzer);
        bean.onlyType(Image.class);
        bean.byFullText("a");
        assertTrue(bean.activeQueries() == 1);
        bean.clearQueries();
        assertTrue(bean.activeQueries() == 0);

        // Now add results directly and see if they
        // get cleared
        assertFalse(bean.hasNext());
        bean.addResult(Arrays.<IObject> asList(new Image()));
        assertTrue(bean.hasNext());
        bean.byHqlQuery("a", null);
        assertTrue(bean.hasNext());
        assertTrue(bean.activeQueries() == 1);
        bean.clearQueries();
        assertTrue(bean.activeQueries() == 0);
        assertTrue(bean.hasNext()); // Still here.
    }

    @Test
    public void testCanSearchForObjectsWhichArentAnnotated() {
        bean = new SearchBean(executor, analyzer);
        bean.onlyAnnotatedWith((java.lang.Class[]) null);
    }

    // ==============================================

    private void addActionWithResultOfSize_n(final int n) {
        bean.addAction(new SearchAction(new SearchValues()) {

            public Object doWork(Session session, ServiceFactory sf) {
                List<IObject> rv = new ArrayList<IObject>();
                for (int i = 0; i < n; i++) {
                    rv.add(new Image());
                }
                return rv;
            }
        });
    }

}
