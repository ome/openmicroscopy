/*
 * ome.resurrect.transform.PixelsTrans
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.resurrect.transform;

import java.util.List;

import org.hibernate.Session;

import ome.model.core.PixelsDimensions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

/**
 * @author callan
 * 
 */
public class PixelsDimensionsTrans extends Transformer {
    public PixelsDimensionsTrans(Object model, Session session,
            Experimenter owner, Event creationEvent, List toSave) {
        super(model, session, owner, creationEvent, toSave);
    }

    public PixelsDimensionsTrans(Transformer transformer, Object model) {
        super(model, transformer.getSession(), transformer.getOwner(),
                transformer.getCreationEvent(), transformer.getToSave());
    }

    @SuppressWarnings("unchecked")
    public List transmute() {

        List toSave = getToSave();
        Event creationEvent = getCreationEvent();

        ome.model.ImageDimension oldDimensions = (ome.model.ImageDimension) getModel();
        PixelsDimensions p = new PixelsDimensions();
        p.setOwner(getOwner());
        p.setCreationEvent(creationEvent);
        p.setSizeX(oldDimensions.getPixelSizeX());
        p.setSizeY(oldDimensions.getPixelSizeY());
        p.setSizeZ(oldDimensions.getPixelSizeZ());

        toSave.add(p);
        return toSave;
    }
}
