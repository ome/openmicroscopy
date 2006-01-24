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
