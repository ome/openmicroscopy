/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.view;




//Java imports
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.TaskBar;

/** 
 * Factory to create {@link ImViewer} components.
 * This class keeps track of all {@link ImViewer} instances that have been
 * created and are not yet {@link ImViewer#DISCARDED discarded}. A new
 * component is only created if none of the <i>tracked</i> ones is already
 * displaying the given hierarchy. Otherwise, the existing component is
 * recycled.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImViewerFactory
    implements ChangeListener
{

    /** The sole instance. */
    private static final ImViewerFactory  singleton = new ImViewerFactory();

    /** 
     * Returns all the {@link ImViewer} components that this factory is
     * currently tracking.
     * 
     * @return The set of currently tracked viewers. 
     */
    static Set getViewers() { return singleton.viewers; }
    
    /** 
     * Returns the <code>window</code> menu. 
     * 
     * @return See above.
     */
    static JMenu getWindowMenu() { return singleton.windowMenu; }
    
    /**
     * Returns <code>true</code> is the {@link #windowMenu} is attached 
     * to the <code>TaskBar</code>, <code>false</code> otherwise.
     *
     * @return See above.
     */
    static boolean isWindowMenuAttachedToTaskBar()
    {
        return singleton.isAttached;
    }
    
    /** Attaches the {@link #windowMenu} to the <code>TaskBar</code>. */
    static void attachWindowMenuToTaskBar()
    {
        if (isWindowMenuAttachedToTaskBar()) return;
        TaskBar tb = HiViewerAgent.getRegistry().getTaskBar();
        tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
        singleton.isAttached = true;
    }
    
    /**
     * Returns a viewer to display the image corresponding to the specified id.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param imageID   The id of the image.
     * @param name      The name of the image.
     * @param bounds    The bounds of the component invoking the 
     *                  {@link ImViewer}.
     * @return See above.
     */
    public static ImViewer getImageViewer(long pixelsID, long imageID,
                                            String name, Rectangle bounds)
    {
        ImViewerModel model = new ImViewerModel(pixelsID, imageID, name, 
                                            bounds);
        return singleton.getViewer(model);
    }

    /** All the tracked components. */
    private Set<ImViewer>     	viewers;

    /** The windows menu. */
    private JMenu   			windowMenu;
    
    /** 
     * Indicates if the {@link #windowMenu} is attached to the 
     * <code>TaskBar</code>.
     */
    private boolean 			isAttached;
    
    /** Creates a new instance. */
    private ImViewerFactory()
    {
        viewers = new HashSet<ImViewer>();
        isAttached = false;
        windowMenu = new JMenu("Viewers");
    }
    
    /**
     * Creates or recycle a viewer component for the specified 
     * <code>model</code>.
     * 
     * @param model The component's Model.
     * @return A {@link ImViewer} for the specified <code>model</code>.  
     */
    private ImViewer getViewer(ImViewerModel model)
    {
        Iterator v = viewers.iterator();
        ImViewerComponent comp;
        while (v.hasNext()) {
            comp = (ImViewerComponent) v.next();
            if (model.isSameDisplay(comp.getModel())) return comp;
        }
        comp = new ImViewerComponent(model);
        comp.initialize();
        comp.addChangeListener(this);
        viewers.add(comp);
        return comp;
    }
    
    /**
     * Removes a viewer from the {@link #viewers} set when it is
     * {@link ImViewer#DISCARDED discarded}. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent ce)
    {
        ImViewerComponent comp = (ImViewerComponent) ce.getSource(); 
        if (comp.getState() == ImViewer.DISCARDED) viewers.remove(comp);
        if (viewers.size() == 0) {
        	TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
            tb.removeFromMenu(TaskBar.WINDOW_MENU, windowMenu);
            isAttached = false;
        }
    }

}
