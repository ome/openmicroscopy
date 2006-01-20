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

import ome.model.core.PixelsDimensions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;


/**
 * @author callan
 *
 */
public class PixelsDimensionsTrans extends Transformer
{
    public PixelsDimensionsTrans(Object model, Session session, Experimenter owner,
            Event creationEvent, List toSave)
    {
        super(model, session, owner, creationEvent, toSave);
    }
    
    public PixelsDimensionsTrans(Transformer transformer, Object model)
    {
        super(model, transformer.getSession(), transformer.getOwner(),
              transformer.getCreationEvent(), transformer.getToSave());
    }
    
    @SuppressWarnings("unchecked")
    public List transmute()
    {

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
