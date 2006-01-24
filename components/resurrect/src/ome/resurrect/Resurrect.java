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

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
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

        List<ome.model.Experimenter> l = c2.getExperimenters();

        displayGui(l);

        c3 = Omero3Connector.getInstance();
        
        event = new Event();
        event.setName("Transmutted by Resurrect");
        c3.save(event);        
        
        transmuteAllExperimenters(l);
        transmutePixels(73);
    }

    private void displayGui(List<ome.model.Experimenter> l)
    {
        Display display = new Display();
        //Shell shell = new Shell(display);
        Shell shell = new Shell(display, SWT.CLOSE | SWT.MIN);
        Table table = new Table 
            (shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        for (ome.model.Experimenter e : l) {
            TableItem item = new TableItem (table, SWT.NONE);
            item.setText (e.getFirstname() + " " + e.getLastname());
        }
        table.setSize (200, 244);
        
        Button button = new Button(shell, SWT.PUSH);
        button.setBounds(50, 246, 100, 26);
        button.setText("Import");
        
        table.addListener (SWT.Selection, new org.eclipse.swt.widgets.Listener () {
            public void handleEvent (org.eclipse.swt.widgets.Event ev) {
                if (ev.detail == SWT.CHECK)
                    System.out.println (ev.item + " checked");
            }
        });
        shell.setSize (200, 300);
        shell.open();
        while(!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
        display.dispose();
    }

    private void transmuteAllExperimenters(List<ome.model.Experimenter> l)
    {

        List toSave = null;
        for (ome.model.Experimenter e : l)
        {
            ExperimenterTrans transform =
                new ExperimenterTrans(e, null, null, event, null);
            toSave = transform.transmute();
            c3.save(toSave.toArray());
        }
    }
    
    private void transmutePixels(int id)
    {
        Experimenter e = c3.getExperimenter(1);
        List l = c2.transmutePixels(e, event, id);
        
        c3.save(l.toArray());
    }
}

