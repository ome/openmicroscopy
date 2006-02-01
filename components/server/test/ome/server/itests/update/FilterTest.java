package ome.server.itests.update;

import java.util.HashSet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.tools.hibernate.ProxyCleanupFilter;
import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.enums.PIType;
import ome.server.itests.ConfigHelper;

public class FilterTest
        extends AbstractDependencyInjectionSpringContextTests
{

    protected String[] getConfigLocations()
    {
        return ConfigHelper.getDaoConfigLocations();
    }

    void test(Object p)
    {
        Object merged = ht.merge(p);
        assertTrue(p != merged);
        ht.saveOrUpdate(merged);
    }
    
    public void testNewWithNewObject()
    {
        PIType pi = new PIType();
        pi.setValue("hi");
        
        AcquisitionContext ac = new AcquisitionContext();
        ac.setPhotometricInterpretation(pi);
        
        ome.model.core.Pixels p = new ome.model.core.Pixels();
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");  // "pixels"
        p.setAcquisitionContext(ac);
        
        test(p);
    }
    
    public void testDetachedWithNewCollection()
    {
        final Channel c1 = new Channel();
        c1.setIndex(new Integer(6));
                
        ome.model.core.Pixels p = (ome.model.core.Pixels) ht.find(" from Pixels ").iterator().next();
        p.setChannels(new HashSet());
        p.getChannels().add(c1);

        // FIXME We can do one of two things; make this use the Daos and 
        // remove this here or use this here to show full usage.
        ProxyCleanupFilter filter = new ProxyCleanupFilter();
        p = (Pixels) filter.filter(null,p);
        
        test(p);
        
    }

    public void testNewWithNewCollection()
    {
        
        final Channel c1 = new Channel();
        c1.setIndex(new Integer(6));
        
        Pixels p = new Pixels();
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");  // "pixels"
        
        p.setChannels(new HashSet());
        p.getChannels().add(c1);        
        
        test(p);
    }
    

    protected HibernateTemplate  ht;
    
    public void setHt(HibernateTemplate ht)
    {
        this.ht = ht;
    }

}
