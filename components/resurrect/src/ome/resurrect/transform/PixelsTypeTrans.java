/*
 * ome.resurrect.transform.PixelsTrans
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.resurrect.transform;

import java.util.List;

import org.hibernate.Session;

import ome.model.enums.PixelsType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.resurrect.Omero3Connector;


/**
 * @author callan
 *
 */
public class PixelsTypeTrans extends Transformer
{
    public PixelsTypeTrans(Object model, Session session, Experimenter owner,
            Event creationEvent, List toSave)
    {
        super(model, session, owner, creationEvent, toSave);
    }
    
    public PixelsTypeTrans(Transformer transformer, Object model)
    {
        super(model, transformer.getSession(), transformer.getOwner(),
              transformer.getCreationEvent(), transformer.getToSave());
    }
    
    @SuppressWarnings("unchecked")
    public List transmute()
    {
        Omero3Connector connector = Omero3Connector.getInstance();
        ome.model.ImagePixel pixels = (ome.model.ImagePixel) getModel();
        PixelsType p = connector.getPixelsType(pixels.getPixelType());
        List toSave = getToSave();
        
        toSave.add(p);
        return toSave;
    }
}
