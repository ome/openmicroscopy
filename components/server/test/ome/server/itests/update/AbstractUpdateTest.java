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
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;
import ome.server.itests.AbstractInternalContextTest;
import ome.server.itests.ConfigHelper;


public class AbstractUpdateTest
        extends AbstractInternalContextTest
{
    
     /** this method serves as our client and as our test data store.
     * An object that has no id is "new"; an object with an id is detached
     * and can represent something serialized from IQuery.
     * TODO put this into ome.testing.ObjectFactory
     */
    protected Pixels createPixelGraph(Pixels example)
    {

        Pixels p = new Pixels();
        AcquisitionContext ac = new AcquisitionContext();
        PhotometricInterpretation pi = new PhotometricInterpretation();
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
