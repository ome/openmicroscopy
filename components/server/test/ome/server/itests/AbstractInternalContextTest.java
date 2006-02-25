package ome.server.itests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;
import ome.system.OmeroContext;


public class AbstractInternalContextTest
        extends AbstractTransactionalDataSourceSpringContextTests
{
    @Override
    protected String[] getConfigLocations()
    {
        return new String[]{};
    }
    
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getInternalServerContext();
    }

    protected SessionFactory                _sf;
    protected Session                       _s;
    protected IQuery                        _qu;
    protected IUpdate                       _up;
    protected JdbcTemplate                  _jt;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        _sf = (SessionFactory) applicationContext.getBean("sessionFactory");
        _jt = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
        
        // This is an internal test we don't want the wrapped spring beans. 
        _qu = (IQuery) applicationContext.getBean("ome.api.IQuery");
        _up = (IUpdate) applicationContext.getBean("ome.api.IUpdate");

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
        Experimenter o = 
            (Experimenter) _qu.getById(Experimenter.class,0);
        ExperimenterGroup g = 
            (ExperimenterGroup) _qu.getById(ExperimenterGroup.class,0);
        CurrentDetails.setOwner(o);
        CurrentDetails.setGroup(g);
        EventType test = 
            (EventType) _qu.getUniqueByFieldEq(EventType.class,"value","Test");
        CurrentDetails.newEvent(test);
    }

    protected void openSession()
    {
        _s = SessionFactoryUtils.getSession(_sf,true);
    }

    // TODO remove?
    // cf: http://forum.hibernate.org/viewtopic.php?t=929167
    protected void closeSession() throws Exception
    {
        flush();
        clear();
//        _s.connection().commit();
//        TransactionSynchronizationManager.unbindResource(_sf);
//        SessionFactoryUtils.releaseSession(_s, _sf);
//  SessionFactoryUtils.processDeferredClose(_sf); // TODO see OpenInView
    }
    
    protected void clear()
    {
        _s.clear();
    }
    
    protected void flush()
    { 
        _s.flush();
    }

}
