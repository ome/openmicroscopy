package ome.server.itests.update;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.core.Pixels;
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.security.CurrentDetails;
import ome.server.itests.ConfigHelper;


public class AbstractUpdateTest
        extends AbstractTransactionalDataSourceSpringContextTests
{

    AbstractUpdateTest(){
        setDefaultRollback(false);
    }
    
    @Override
    protected String[] getConfigLocations()
    {
        return ConfigHelper.getConfigLocations();
    }

    protected Session       _s;
    protected IQuery        _qu;
    protected IUpdate       _up;
    protected JdbcTemplate  _jt;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        _qu = (IQuery) applicationContext.getBean("queryService");
        _up = (IUpdate) applicationContext.getBean("updateService");
        _jt = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");

    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {

        /* make sure the whole test runs within a single session */
        _s = SessionFactoryUtils.getSession(getSessionFactory(),true);
        
        /* TODO run experiment as root. Dangerous? */
        Experimenter o = (Experimenter) _qu.getById(Experimenter.class,0);
        CurrentDetails.setOwner(o);
        EventType test = 
            (EventType) _qu.getUniqueByFieldEq(EventType.class,"value","Test");
        CurrentDetails.newEvent(test);

    }

    protected void flush()
    {
        _s.flush();
    }
    
    protected SessionFactory getSessionFactory()
    {
        return (SessionFactory) applicationContext.getBean("sessionFactory");
        
    }
    
    protected Pixels getSomePixels()
    {
        return (Pixels) _qu.queryList("select p from Pixels p "
            + "left outer join fetch p.channels " +
                "left outer join p.acquisitionContext ", 
            new Object[] {}).iterator().next();
    }
    
}
