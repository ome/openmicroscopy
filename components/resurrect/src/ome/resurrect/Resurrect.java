/*
 * ome.resurrect.Main
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
package ome.resurrect;

import java.util.List;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.resurrect.transform.ExperimenterTrans;


/**
 * @author callan
 *
 */
public class Resurrect
{
    /** Connector to the OME 2.5 (OMERO2) database **/
    Omero2Connector c2;
    
    /** Connector to the OMERO3 database **/
    Omero3Connector c3;
    
    /** Event that will be used for all transmutations **/
    Event event;
   
    public static void main(String[] args)
    {
        new Resurrect();
    }

    public Resurrect()
    {
        c2 = Omero2Connector.getInstance();
        c3 = Omero3Connector.getInstance();
        
        event = new Event();
        event.setName("Transmutted by Resurrect");
        
        transmuteAllExperimenters();
        transmutePixels(73);
    }

    private void transmuteAllExperimenters()
    {
        List<ome.model.Experimenter> l = c2.getExperimenters();

        List toSave = null;
        for (ome.model.Experimenter e : l)
        {
            ExperimenterTrans transform =
                new ExperimenterTrans(e, null, null, event, null);
            toSave = transform.transmute();
            c3.save(event, toSave.toArray());
        }
    }
    
    private void transmutePixels(int id)
    {
        Experimenter e = c3.getExperimenter(1);
        List l = c2.transmutePixels(e, event, id);
        
        c3.save(event, l.toArray());
    }
}
