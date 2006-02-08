package ome.server.itests.update;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.enums.EventType;
import ome.model.enums.PIType;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
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

    protected HibernateTransactionManager   _tm;
    protected SessionFactory                _sf;
    protected Session                       _s;
    protected IQuery                        _qu;
    protected IUpdate                       _up;
    protected JdbcTemplate                  _jt;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        _tm = (HibernateTransactionManager) applicationContext.getBean("transactionManager");
        _sf = (SessionFactory) applicationContext.getBean("sessionFactory");
        _qu = (IQuery) applicationContext.getBean("queryService");
        _up = (IUpdate) applicationContext.getBean("updateService");
        _jt = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");

    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {

        /* make sure the whole test runs within a single session,
         * unless closeSession() is called */
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
//	SessionFactoryUtils.processDeferredClose(_sf); // TODO see OpenInView
    }
    
    protected void clear()
    {
        _s.clear();
    }
    
    protected void flush()
    { 
        _s.flush();
    }

    /** this method serves as our client and as our test data store.
     * An object that has no id is "new"; an object with an id is detached
     * and can represent something serialized from IQuery.
     */
    protected Pixels createPixelGraph(Long pixelsId)
    {
        // TODO put this into ome.testing.ObjectFactory
        PIType pi = new PIType();
        pi.setValue("test");
    
        AcquisitionContext ac = new AcquisitionContext();
        ac.setPhotometricInterpretation(pi);
    
        Channel c = new Channel();
        c.setIndex(1);
    
        Pixels p = new Pixels(pixelsId);
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
        p.setAcquisitionContext(ac);
        p.setChannels(new HashSet());
        p.getChannels().add(c);
    
        c.setPixels(p);
        
        p.setDetails(new Details());
        return p;
    }

    protected boolean equalSets(Set<IObject> before, Set<IObject> after)
    {
        Set<Long> beforeIds = new HashSet<Long>();
        for (IObject object : before)
        {
            beforeIds.add(object.getId());
        }
        
        Set<Long> afterIds = new HashSet<Long>();
        for (IObject object : after)
        {
            afterIds.add(object.getId());
        }
        
        return beforeIds.containsAll(afterIds);
    }
    
}
