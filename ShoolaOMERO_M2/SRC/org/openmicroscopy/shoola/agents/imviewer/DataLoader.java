/*
 * org.openmicroscopy.shoola.agents.imviewer.DataLoader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.imviewer;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.ImViewerView;

/** 
 * 
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
public abstract class DataLoader
    extends DSCallAdapter
{

    /** The viewer this data loader is for. */
    protected final ImViewer            viewer;
    
    /** Convenience reference for subclasses. */
    protected final Registry            registry;
    
    /** Convenience reference for subclasses. */
    protected final ImViewerView        ivView;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     */
    protected DataLoader(ImViewer viewer)
    {
        if (viewer == null) throw new NullPointerException("No viewer.");
        this.viewer = viewer;
        registry = ImViewerAgent.getRegistry();
        ivView = (ImViewerView) 
                    registry.getDataServicesView(ImViewerView.class);
    }
    
    /**
     * Notifies the {@link #viewer} one last time.
     * This way the {@link #viewer} has a chance to hide the progress bar.
     */
    public void onEnd() { viewer.setStatus(null, -1); }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /**
     * Notifies the user that the data retrieval has been cancelled.
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        registry.getUserNotifier().notifyInfo("Data Retrieval Cancellation", 
                                              info);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Data Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        viewer.discard();
        //TODO: Change this.  What to do in the case of failure is up to
        //the viewer.  So we need to refactor this b/c the decision is
        //made in the wrong place!
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
