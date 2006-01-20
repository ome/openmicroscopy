/*
 * ome.resurrect.transform.PixelsTrans
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.resurrect.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import ome.model.AnalysisChain;
import ome.model.ImageDimension;
import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.core.PixelsDimensions;


/**
 * @author callan
 *
 */
public class PixelsTrans extends Transformer
{
    public PixelsTrans(Object model, Session session, Experimenter owner,
                       Event creationEvent, List toSave)
    {
        super(model, session, owner, creationEvent, toSave);
    }
    
    public PixelsTrans(Transformer transformer, Object model)
    {
        super(model, transformer.getSession(), transformer.getOwner(),
              transformer.getCreationEvent(), transformer.getToSave());
    }
    
    @SuppressWarnings("unchecked")
    public List transmute()
    {
        ome.model.ImagePixel oldPixels = (ome.model.ImagePixel) getModel();
        
        List toSave = getToSave();
        Event creationEvent = getCreationEvent();
       
        // Transform the pixels set metadata
        Pixels p = new Pixels();
        p.setCreationEvent(creationEvent);
        p.setSha1(oldPixels.getFileSha1());
        p.setSizeX(oldPixels.getSizeX());
        p.setSizeY(oldPixels.getSizeY());
        p.setSizeZ(oldPixels.getSizeZ());
        p.setSizeC(oldPixels.getSizeC());
        p.setSizeT(oldPixels.getSizeT());
        p.setBigEndian(false);  // FIXME: This really shouldn't be here
        p.setOwner(getOwner());
        
        // Transform the pixels set info
        PixelsTypeTrans ptransform = new PixelsTypeTrans(this, oldPixels);
        toSave = ptransform.transmute();
        PixelsType pixelsType = (PixelsType) toSave.get(toSave.size() - 1);
        p.setPixelsType(pixelsType);
        
        // The pixels type doesn't actually need to be saved, as it is an
        // internal OMERO3 enumeration, so remove it from the list.
        toSave.remove(pixelsType);
        
        // Now transform all channel components of the given pixels set
        Set channelComponents = oldPixels.getChannelComponents();
        Set channelList = new HashSet();
        
        for (Object o : channelComponents)
        {
            ome.model.ChannelComponent c = (ome.model.ChannelComponent) o;
            ChannelTrans ctransform = new ChannelTrans(this, c);
            ctransform.setIndex(c.getIndex());
            toSave = ctransform.transmute();
            channelList.add(toSave.get(toSave.size() - 1));
        }
        
        // Add the channels to the pixels set
        p.setChannels(channelList);
        
        // Build an acquisiton context from the logical channel
        ome.model.LogicalChannel lc = getFirstLogicalChannel();
        
        AcquisitionContext ac = null;
        if (lc != null)
        {
            AcquisitionContextTrans actransform =
                new AcquisitionContextTrans(this, lc);
            toSave = actransform.transmute();
            ac = (AcquisitionContext) toSave.get(toSave.size() - 1);
        }
        else
            ac = new AcquisitionContext();
            toSave.add(ac);
            
        // Add the acquistion context to the pixels set
        p.setAcquisitionContext(ac);
        
    
        // Transform the PixelsDimensions and add them to the save list
        PixelsDimensionsTrans pdtransform = new PixelsDimensionsTrans(this, 
        		oldPixels.getImage().getImageDimensions().iterator().next());
        toSave = pdtransform.transmute();
        PixelsDimensions pixelsDimensions = 
        	    (PixelsDimensions) toSave.get(toSave.size() - 1);
        p.setPixelsDimensions(pixelsDimensions);
        
        // Finish up by adding this object to the toSave list and returning
        toSave.add(p);
        return toSave;
    }
    
    private ome.model.LogicalChannel getFirstLogicalChannel()
    {
        ome.model.ImagePixel oldPixels = (ome.model.ImagePixel) getModel();
        Set channels = oldPixels.getChannelComponents();
        
        if (channels.size() < 1)
            return null;
        
        for (Object o : channels)
        {
            ome.model.ChannelComponent channel = (ome.model.ChannelComponent) o;
            ome.model.LogicalChannel lc = channel.getLogicalChannel();
            
            if (lc != null)
                return lc;
        }
        
        return null;
    }
}
