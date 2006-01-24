/*
 * ome.resurrect.transform.ExperimenterTrans
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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;


/**
 * @author callan
 *
 */
public class ExperimenterTrans extends Transformer
{
    public ExperimenterTrans(Object model, Session session, Experimenter owner,
                             Event creationEvent, List toSave)
    {
        if (model == null)
            throw new NullPointerException("Expecting not null model.");
        if (creationEvent == null)
            throw new NullPointerException("Expecting not null creationEvent");
        if (toSave == null)
            setToSave(new ArrayList());
        
        setModel(model);
        setCreationEvent(creationEvent);
    }
    
    @SuppressWarnings("unchecked")
    public List transmute()
    {
        ome.model.Experimenter oldExperimenter =
            (ome.model.Experimenter) getModel();
        
        List toSave = getToSave();
       
        Experimenter e = new Experimenter();
        e.setEmail(oldExperimenter.getEmail());
        e.setFirstName(oldExperimenter.getFirstname());
        e.setLastName(oldExperimenter.getLastname());
        e.setOmeName(oldExperimenter.getOmeName());
        e.setInstitution(oldExperimenter.getInstitution());
        
        toSave.add(e);
        return toSave;
    }
}
