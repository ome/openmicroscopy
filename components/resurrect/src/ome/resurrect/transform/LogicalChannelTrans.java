/*
 * ome.resurrect.transform.PixelsTrans
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.resurrect.transform;

import java.util.List;

import org.hibernate.Session;

import ome.model.core.LogicalChannel;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

/**
 * @author callan
 * 
 */
public class LogicalChannelTrans extends Transformer {
    public LogicalChannelTrans(Object model, Session session,
            Experimenter owner, Event creationEvent, List toSave) {
        super(model, session, owner, creationEvent, toSave);
    }

    public LogicalChannelTrans(Transformer transformer, Object model) {
        super(model, transformer.getSession(), transformer.getOwner(),
                transformer.getCreationEvent(), transformer.getToSave());
    }

    @SuppressWarnings("unchecked")
    public List transmute() {
        ome.model.LogicalChannel oldLogicalChannel = (ome.model.LogicalChannel) getModel();

        List toSave = getToSave();
        Event creationEvent = getCreationEvent();

        LogicalChannel lchannel = new LogicalChannel();
        lchannel.setCreationEvent(creationEvent);
        lchannel.setOwner(getOwner());
        lchannel.setEmissionWave(oldLogicalChannel.getEmWave());
        lchannel.setExcitationWave(oldLogicalChannel.getExWave());
        lchannel.setFluor(oldLogicalChannel.getFluor());
        lchannel.setName(oldLogicalChannel.getName());
        lchannel.setNdFilter(oldLogicalChannel.getNdFilter());
        lchannel.setPinHoleSize(oldLogicalChannel.getPinholeSize());

        toSave.add(lchannel);
        return toSave;
    }
}
