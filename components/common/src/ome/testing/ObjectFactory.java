package ome.testing;

import java.util.ArrayList;
import java.util.List;

import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.internal.Details;


/** these method serve as a both client and test data store.
 * An object that has no id is "new"; an object with an id is detached 
 * and can represent something serialized from IQuery.
 * 
 * NOTE: this is a bit dangerous, causing model builds to fail sometimes.
 * where else could it live?
 */
public class ObjectFactory
{
    public static Pixels createPixelGraph(Pixels example)
    {

        Pixels p = new Pixels();
        AcquisitionContext ac = new AcquisitionContext();
        PhotometricInterpretation pi = new PhotometricInterpretation();
        AcquisitionMode mode = new AcquisitionMode();
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
            
            pd.setSizeX(new Float(1.0));
            pd.setSizeY(new Float(1.0));
            pd.setSizeZ(new Float(1.0));
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
}
