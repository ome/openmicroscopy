/*
 *   Copyright (C) 2008-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.scalability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.sql.DataSource;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.server.itests.ManagedContextFixture;
import ome.testing.Report;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.stat.Statistics;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Test(enabled = false, groups = { "integration" })
public class SqlHibernateDatasourceComparisonTest extends TestCase {

    static final String select = "select p from Pixels p ";
    static final String idsin = "p.id in (220, 221, 222, 223, 224, 225, 226, 227, 228, 229)";
    static final String alone_q = select + "where " + idsin;
    static final String links_q = select
            + "left outer join fetch p.annotationLinks links " + "where "
            + idsin;
    static final String channels_q = select
            + "left outer join fetch p.channels where " + idsin;

    String oldProperty;
    ManagedContextFixture fixture;
    DataSource ds;
    SessionFactory omesf;
    SessionFactory rawsf;
    
    @BeforeClass
    public void setup() {
        oldProperty = System.getProperty("dataSource");
        System.setProperty("dataSource", "dbcpDataSource");
        fixture = new ManagedContextFixture();
        ds = (DataSource) fixture.ctx.getBean("dataSource");
        omesf = (SessionFactory) fixture.ctx
                .getBean("sessionFactory");
        rawsf = loadSessionFactory(ds);
    }
    
    @AfterClass
    public void tearDown() {
        System.setProperty("dataSource", oldProperty);
    }
    
    private SessionFactory loadSessionFactory(DataSource source) {
        Properties p = (Properties) fixture.ctx.getBean("hibernateProperties");
        FilterDefinitionFactoryBean fdfb = new FilterDefinitionFactoryBean();
        fdfb.setBeanName("securityFilter");
        fdfb.setDefaultFilterCondition("true = true");
        fdfb.afterPropertiesSet();
        FilterDefinition fd = (FilterDefinition) fdfb.getObject();
        LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
        lsfb.setHibernateProperties(p);
        lsfb.setConfigLocation(new ClassPathResource("hibernate.cfg.xml"));
        lsfb.setDataSource(ds);
        lsfb.setFilterDefinitions(new FilterDefinition[] { fd });
        try {
            lsfb.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (SessionFactory) lsfb.getObject();
    }

    List<Callable<Object>> calls = new ArrayList<Callable<Object>>();
    {
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callHibernateAlone();
                return null;
            }
        });

        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callOmeroAlone();
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callJdbcDirect("JA", "select p.id from pixels p where " + idsin);
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callHibernateChannels();
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callOmeroChannels();
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callJdbcDirect("JC", "select p.id from pixels p, channel c "
                        + "where c.pixels = p.id and " + idsin);
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callHibernateLinks();
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callOmeroLinks();
                return null;
            }
        });
        calls.add(new Callable<Object>() {
            public Object call() throws Exception {
                callJdbcDirect("JL",
                        "select p.id from pixels p, pixelsannotationlink l "
                                + "where l.parent = p.id and " + idsin);
                return null;
            }
        });
    }

    void callJdbcDirect(String which, String query) throws Exception {
        Monitor m = MonitorFactory.getTimeMonitor(which);
        m.start();
        try {
            Connection c = ds.getConnection();
            try {
                PreparedStatement ps = c.prepareCall(query);
                ResultSet rs = ps.executeQuery();
            } finally {
                c.close();
            }
        } finally {
            m.stop();
        }
    }

    void callHibernateAlone() throws Exception {
        _callHibernate("HA", alone_q);
    }

    void callHibernateChannels() throws Exception {
        _callHibernate("HC", channels_q);
    }

    void callHibernateLinks() throws Exception {
        _callHibernate("HL", links_q);
    }

    void _callHibernate(String which, String query) throws Exception {
        try {
            Monitor m = MonitorFactory.getTimeMonitor(which).start();
            Session s = rawsf.openSession();
            s.createQuery(query).list();
            s.close();
            m.stop();
        } finally {
            rawsf.close();
        }
    }

    void callOmeroAlone() throws Exception {
        _callOmero("OA", alone_q);
    }

    void callOmeroChannels() throws Exception {
        _callOmero("OC", channels_q);
    }

    void callOmeroLinks() throws Exception {
        _callOmero("OL", links_q);
    }

    void _callOmero(String which, String query) {
        IQuery q = fixture.managedSf.getQueryService();
        Monitor m = MonitorFactory.getTimeMonitor(which).start();
        q.findAllByQuery(query, null);
        m.stop();
    }

    @Test(enabled = false)
    public void testComparseDataSources() throws Exception {
        try {
            // check pre-conditions
            assertEquals(9, calls.size());
            prime();

            List<Callable<Object>> copy = new ArrayList(calls);
            ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1);
            for (int i = 0; i < 5; i++) {
                Collections.shuffle(copy);
                List<Future<Object>> futures = ex.invokeAll(copy);
                for (Future<Object> future : futures) {
                    future.get();
                }
            }
        } finally {
            System.out.println(new Report());
            fixture.ctx.closeAll();
        }
    }

    void prime() throws Exception {
        // Prime the data sources
        _callHibernate("HP", alone_q);
        _callHibernate("HP", channels_q);
        _callHibernate("HP", links_q);
        // callJdbcDirect("JP", alone_q);
        // callJdbcDirect("JP", channels_q);
        // callJdbcDirect("JP", links_q);
        _callOmero("OP", alone_q);
        _callOmero("OP", channels_q);
        _callOmero("OP", links_q);
    }

    @Test(enabled = false)
    public void testCompareDirect() throws Exception {
        prime();

        Statistics rawstats = rawsf.getStatistics();
        Statistics omestats = omesf.getStatistics();

        System.out.println("Clearing stats");
        rawstats.clear();
        omestats.clear();

        Category.getInstance("org.hibernate.SQL").setLevel(Level.DEBUG);

        // callHibernateAlone();
        callHibernateChannels();
        // callHibernateLinks();
        System.out.println("**** RawStats");
        rawstats.logSummary();
        // callOmeroAlone();
        callOmeroChannels();
        // callOmeroLinks();
        System.out.println("**** OmeStats");
        omestats.logSummary();
        System.out.println(new Report());

    }

    @Test(enabled = false)
    public static void main(String[] args) throws Exception {
        SqlHibernateDatasourceComparisonTest t = new SqlHibernateDatasourceComparisonTest();
        try {
            t.testComparseDataSources();
        } finally {
            t.fixture.close();
        }
    }
}
