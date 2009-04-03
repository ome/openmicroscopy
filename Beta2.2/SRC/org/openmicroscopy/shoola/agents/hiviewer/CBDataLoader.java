/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipBoard.CBDataLoader
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * Parent of all classes that load data asynchronously for a {@link ClipBoard}.
 * All these classes invoke methods of the {@link HierarchyBrowsingView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the callbacks to notify the 
 * {@link ClipBoard} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link ClipBoard} back with the results.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class CBDataLoader
    extends DSCallAdapter
{

    /** The {@link ClipBoard} this data loader is for. */
    protected ClipBoard             clipBoard;
    
    /** Convenience reference to the agent's registry. */
    protected final Registry        registry;
    
    /** Convenience reference. */
    protected HierarchyBrowsingView hiBrwView;
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard The viewer this data loader is for.
     *                  Mustn't be <code>null</code>. 
     */
    public CBDataLoader(ClipBoard clipBoard)
    {
        if (clipBoard == null) throw new NullPointerException("No clipBoard.");
        this.clipBoard = clipBoard;
        registry = HiViewerAgent.getRegistry();
        hiBrwView = (HierarchyBrowsingView) registry.
                    getDataServicesView(HierarchyBrowsingView.class);
    }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /**
     * Notifies the user that the data retrieval has been cancelled.
     * @see DSCallAdapter#handleCancellation()
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        registry.getUserNotifier().notifyInfo("Data Retrieval Cancellation", 
                                              info);
    }
    
    /**
     * Notifies the user that an error has occurred.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Data Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        clipBoard.discard();
        //TODO: Change this.  What to do in the case of failure is up to
        //the classifier.  So we need to refactor this b/c the decision is
        //made in the wrong place!
    }
    
    /** Fires an asynchrnonous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
