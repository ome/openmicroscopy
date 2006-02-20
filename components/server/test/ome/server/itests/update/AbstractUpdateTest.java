package ome.server.itests.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.enums.DimensionOrder;
import ome.model.enums.EventType;
import ome.model.enums.Mode;
import ome.model.enums.PIType;
import ome.model.enums.PixelsType;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;
import ome.server.itests.ConfigHelper;


public class AbstractUpdateTest
        extends AbstractTransactionalDataSourceSpringContextTests
{
    
    @Override
    protected String[] getConfigLocations()
    {
        return ConfigHelper.getConfigLocations();
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
        _qu = (IQuery) applicationContext.getBean("internal.query");
        _up = (IUpdate) applicationContext.getBean("internal.update");
        _jt = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");

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
     * TODO put this into ome.testing.ObjectFactory
     */
    protected Pixels createPixelGraph(Pixels example)
    {

        Pixels p = new Pixels();
        AcquisitionContext ac = new AcquisitionContext();
        PIType pi = new PIType();
        Mode mode = new Mode();
        PixelsType pt = new PixelsType();
        DimensionOrder dO = new DimensionOrder();
        PixelsDimensions pd = new PixelsDimensions();
        Image i = new Image();
        Channel c = new Channel();
        
        if (example != null)
        {
            p.setId(example.getId());
            
            // everything else unloaded.
            ac.setId(example.getAcquisitionContext().getId());
            ac.unload();
            pt.setId(example.getPixelsType().getId());
            pt.unload();
            dO.setId(example.getDimensionOrder().getId());
            dO.unload();
            pd.setId(example.getPixelsDimensions().getId());
            pd.unload();
            i.setId(example.getImage().getId());
            i.unload();
            c.setId(((Channel)example.getChannels().get(0)).getId());
            c.unload();
        }
        
        else
        {
        
            mode.setValue("test"+System.currentTimeMillis());
            pi.setValue("test"+System.currentTimeMillis());                    
            ac.setPhotometricInterpretation(pi);
            ac.setMode(mode);
        
            pt.setValue("test"+System.currentTimeMillis());
            
            dO.setValue("XXXX"+System.currentTimeMillis());
            
            pd.setSizeX(1.0f);
            pd.setSizeY(1.0f);
            pd.setSizeZ(1.0f);
        
            c.setPixels(p);
            
            i.setName("test");
        
        }
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
        p.setAcquisitionContext(ac);
        p.setPixelsType(pt);
        p.setDimensionOrder(dO);
        p.setPixelsDimensions(pd);
        p.setImage(i);
        
        List channels = new ArrayList();
        channels.add(c);
        p.setChannels(channels);

        // Reverse links
        // FIXME i.setActivePixels(p);
        p.setDetails(new Details());

        return p;
    }

    protected boolean equalCollections(Collection<IObject> before, Collection<IObject> after)
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
