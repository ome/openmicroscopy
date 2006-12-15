/*
 * ome.resurrect.transform.PixelsTrans
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
public class ChannelTrans extends Transformer {
    /** The index of the channel * */
    Integer index;

    public ChannelTrans(Object model, Session session, Experimenter owner,
            Event creationEvent, List toSave) {
        super(model, session, owner, creationEvent, toSave);
    }

    public ChannelTrans(Transformer transformer, Object model) {
        super(model, transformer.getSession(), transformer.getOwner(),
                transformer.getCreationEvent(), transformer.getToSave());
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @SuppressWarnings("unchecked")
    public List transmute() {
        if (index == null)
            throw new NullPointerException("Index must be set.");
        ome.model.ChannelComponent oldChannel = (ome.model.ChannelComponent) getModel();

        List toSave = getToSave();
        Event creationEvent = getCreationEvent();

        LogicalChannelTrans transform = new LogicalChannelTrans(this,
                oldChannel.getLogicalChannel());
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
