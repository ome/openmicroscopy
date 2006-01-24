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

import java.util.List;

import org.hibernate.Session;

import ome.model.core.Channel;
import ome.model.core.LogicalChannel;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;


/**
 * @author callan
 *
 */
public class ChannelTrans extends Transformer
{
    /** The index of the channel **/
    Integer index;

    public ChannelTrans(Object model, Session session, Experimenter owner,
                        Event creationEvent, List toSave)
    {
        super(model, session, owner, creationEvent, toSave);
    }
    
    public ChannelTrans(Transformer transformer, Object model)
    {
        super(model, transformer.getSession(), transformer.getOwner(),
              transformer.getCreationEvent(), transformer.getToSave());
    }
    
    public void setIndex(int index)
    {
        this.index = index;
    }
 
    @SuppressWarnings("unchecked")
    public List transmute()
    {
        if (index == null)
            throw new NullPointerException("Index must be set.");
        ome.model.ChannelComponent oldChannel = 
            (ome.model.ChannelComponent) getModel();
        
        List toSave = getToSave();
        Event creationEvent = getCreationEvent();
        
        LogicalChannelTrans transform =
            new LogicalChannelTrans(this, oldChannel.getLogicalChannel());
        toSave = transform.transmute();
       
        Channel c = new Channel();
        c.setCreationEvent(creationEvent);
        c.setIndex(index);
        c.setLogicalChannel((LogicalChannel) toSave.get(toSave.size() - 1));
        c.setOwner(getOwner());
        
        toSave.add(c);
        return toSave;
    }
}
