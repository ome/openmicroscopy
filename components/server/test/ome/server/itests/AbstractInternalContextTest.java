package ome.server.itests;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.OMEData;
import ome.tools.spring.InternalServiceFactory;

@Test(
        groups = { "integration" }
)
public class AbstractInternalContextTest
        extends AbstractTransactionalDataSourceSpringContextTests
{
    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception
    {
        super.tearDown();
    }
    // =========================================================================
    
    @Override
    protected String[] getConfigLocations()
    {
        return new String[]{};
    }
    
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getInternalServerContext();
    }

    protected Session                       session;
    protected DataSource                    dataSource;
    protected SessionFactory                sessionFactory;
    protected ServiceFactory				serviceFactory;
    protected LocalQuery                    iQuery;
    protected LocalUpdate                   iUpdate;
    protected IAdmin						iAdmin;
    protected IAnalysis						iAnalysis;
    protected JdbcTemplate                  jdbcTemplate;
    
    protected OMEData                       data;

    protected SecuritySystem				securitySystem;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        dataSource = (DataSource) applicationContext.getBean("dataSource");
        sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
        jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
        
        // This is an internal test we don't want the wrapped spring beans. 
        serviceFactory = new InternalServiceFactory( (OmeroContext) applicationContext );
        iQuery = (LocalQuery) serviceFactory.getQueryService();
        iUpdate = (LocalUpdate) serviceFactory.getUpdateService();
        iAnalysis = serviceFactory.getAnalysisService();
        iAdmin = serviceFactory.getAdminService();
        
        data = new OMEData();
        data.setDataSource(dataSource);

        securitySystem = (SecuritySystem) applicationContext.getBean("securitySystem");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        /* make sure the whole test runs within a single session,
         * unless closeSession() is called */
        setComplete();
        openSession();
        prepareCurrentDetails();
    }

    protected void prepareCurrentDetails()
    {
        /* TODO run experiment as root. Dangerous? */
    	login( new Principal("root","system","Test") );
    }
    
    protected void login( Principal p )
    {
    	securitySystem.login(p);
    	securitySystem.setCurrentDetails();
    }

    protected void openSession()
    {
        session = SessionFactoryUtils.getSession(sessionFactory,true);
    }

    // TODO remove?
    // cf: http://forum.hibernate.org/viewtopic.php?t=929167
    protected void closeSession() throws Exception
    {
        flush();
        clear();
//        session.connection().commit();
//        TransactionSynchronizationManager.unbindResource(sessionFactory);
//        SessionFactoryUtils.releaseSession(session, sessionFactory);
//  SessionFactoryUtils.processDeferredClose(sessionFactory); // TODO see OpenInView
    }
    
    protected void clear()
    {
        session.clear();
    }
    
    protected void flush()
    { 
        session.flush();
    }

}
