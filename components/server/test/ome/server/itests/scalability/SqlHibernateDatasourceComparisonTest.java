package ome.server.itests.scalability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Random;

import javax.sql.DataSource;

import ome.api.IQuery;
import ome.server.itests.ManagedContextFixture;
import ome.testing.Report;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.testng.annotations.Test;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Test(groups = { "integration" })
public class SqlHibernateDatasourceComparisonTest {

    static final String select = "select p from Pixels p ";
    static final String idsin = "p.id in (220, 221, 222, 223, 224, 225, 226, 227, 228, 229)";
    static final String alone_q = select + "where " + idsin;
    static final String annotationLinks_q = select
            + "left outer join fetch p.annotationLinks where " + idsin;
    static final String channels_q = select + "join fetch p.channels where "
            + idsin;
    static final String[] names = new String[] { "c3p0DataSource",
            "dbcpDataSource", "springSingleDataSource" };

    ManagedContextFixture fixture = new ManagedContextFixture();
    Random random = new Random();
    DataSource[] sources = new DataSource[names.length];
    {
        for (int i = 0; i < sources.length; i++) {
            sources[i] = source(names[i]);
        }
    }

    DataSource source(String name) {

        class NameInterceptor implements MethodInterceptor {

            final String name;

            public NameInterceptor(String name) {
                this.name = name;
            }

            public Object invoke(MethodInvocation arg0) throws Throwable {
                if (arg0.getMethod().getName().equals("toString")) {
                    return name.substring(0, 1);
                } else {
                    return arg0.proceed();
                }
            }
        }

        DataSource ds = (DataSource) fixture.ctx.getBean(name);
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[] { DataSource.class });
        factory.setTarget(ds);
        factory.addAdvice(new NameInterceptor(name));
        return (DataSource) factory.getProxy();
    }

    void callJdbcDirect(DataSource ds, String which, String query)
            throws Exception {
        Monitor m = MonitorFactory.getTimeMonitor(which + "" + ds);
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

    void callHibernate(DataSource ds, String which, String query)
            throws Exception {
        Properties p = (Properties) fixture.ctx.getBean("hibernateProperties");
        FilterDefinitionFactoryBean fdfb = new FilterDefinitionFactoryBean();
        fdfb.setBeanName("securityFilter");
        fdfb.setDefaultFilterCondition("true = true");
        fdfb.afterPropertiesSet();
        FilterDefinition fd = (FilterDefinition) fdfb.getObject();
        AnnotationSessionFactoryBean asfb = new AnnotationSessionFactoryBean();
        asfb.setHibernateProperties(p);
        asfb.setConfigLocation(new ClassPathResource("hibernate.cfg.xml"));
        asfb.setDataSource(ds);
        asfb.setFilterDefinitions(new FilterDefinition[] { fd });
        asfb.afterPropertiesSet();
        SessionFactory sf = (SessionFactory) asfb.getObject();
        try {
            Monitor m = MonitorFactory.getTimeMonitor(which).start();
            Session s = sf.openSession();
            s.createQuery(query).list();
            s.close();
            m.stop();
        } finally {
            sf.close();
        }
    }

    void callOMERO(String which, String query) {
        IQuery q = fixture.sf.getQueryService();
        Monitor m = MonitorFactory.getTimeMonitor(which).start();
        q.findAllByQuery(query, null);
        m.stop();
    }

    public void testComparseDataSources() throws Exception {
        try {
            for (int i = 0; i < names.length; i++) {
                // Prime the data sources
                callHibernate(sources[i], "HP", alone_q);
                callJdbcDirect(sources[i], "JP", alone_q);
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < sources.length; j++) {
                    DataSource ds = sources[j];

                    callHibernate(ds, "HA" + ds, alone_q);
                    callOMERO("OA", alone_q);
                    callJdbcDirect(ds, "JA", "select p.id from pixels p where "
                            + idsin);

                    callHibernate(ds, "HC" + ds, channels_q);
                    callOMERO("OC", channels_q);
                    callJdbcDirect(ds, "JC",
                            "select p.id from pixels p, channel c "
                                    + "where c.pixels = p.id and " + idsin);

                    callHibernate(ds, "HL" + ds, annotationLinks_q);
                    callOMERO("OL", annotationLinks_q);
                    callJdbcDirect(ds, "JL",
                            "select p.id from pixels p, pixelsannotationlink l "
                                    + "where l.parent = p.id and " + idsin);
                }
            }
        } finally {
            System.out.println(new Report());
            fixture.ctx.closeAll();
        }
    }

    @Test(enabled = false)
    public static void main(String[] args) throws Exception {
        new SqlHibernateDatasourceComparisonTest().testComparseDataSources();
    }

}
