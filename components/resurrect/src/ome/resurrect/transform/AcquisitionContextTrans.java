/*
 * ome.resurrect.transform.PixelsTrans
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.resurrect.transform;

import java.util.List;

import org.hibernate.Session;

import ome.model.acquisition.AcquisitionContext;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.resurrect.Omero3Connector;


/**
 * @author callan
 *
 */
public class AcquisitionContextTrans extends Transformer
{
    public AcquisitionContextTrans(Object model, Session session,
                                   Experimenter owner, Event creationEvent,
                                   List toSave)
    {
        super(model, session, owner, creationEvent, toSave);
    }
    
    public AcquisitionContextTrans(Transformer transformer, Object model)
    {
        super(model, transformer.getSession(), transformer.getOwner(),
              transformer.getCreationEvent(), transformer.getToSave());
    }
    
    @SuppressWarnings("unchecked")
    public List transmute()
    {
        ome.model.LogicalChannel oldLogicalChannel =
            (ome.model.LogicalChannel) getModel();
        
        List toSave = getToSave();
        Event creationEvent = getCreationEvent();
        Omero3Connector connector = Omero3Connector.getInstance();

        AcquisitionContext context = new AcquisitionContext();
        context.setCreationEvent(creationEvent);
        context.setOwner(getOwner());
        context.setPhotometricInterpretation(
                connector.getPIType(
                        oldLogicalChannel.getPhotometricInterpretation()));
        
        toSave.add(context);
        return toSave;
    }
}
