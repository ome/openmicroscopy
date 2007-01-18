/*
 * org.openmicroscopy.shoola.agents.util.classifier.ClassifierLoader 
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
package org.openmicroscopy.shoola.agents.util.classifier;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.classifier.view.Classifier;
import org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;

/** 
 * Parent of all classes that load data asynchronously for a {@link Classifier}.
 * All these classes invoke methods of the {@link DataHandlerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the callbacks to notify the 
 * {@link Classifier} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link Classifier} back with the results.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class ClassifierLoader
	extends DSCallAdapter
{

	/** The TreeViewer this data loader is for. */
    protected final Classifier			viewer;
    
    /** Convenience reference for subclasses. */
    protected final Registry            registry;
    
    /** Convenience reference for subclasses. */
    protected final DataHandlerView		dhView;
    
    /**
     * Controls if the specified mode is supported by this class.
     * 
     * @param index The value to control.
     */
    protected void controlMode(int index)
    {
    	switch (index) {
			case Classifier.CLASSIFY_MODE:
			case Classifier.DECLASSIFY_MODE:
				return;
			default:
				throw new IllegalArgumentException("Mode not supported.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The Classifier this data loader is for.
     *               Mustn't be <code>null</code>.
     */
    protected ClassifierLoader(Classifier viewer)
    {
        if (viewer == null) throw new NullPointerException("No viewer.");
        this.viewer = viewer;
        registry = ClassifierFactory.getRegistry();
        dhView = (DataHandlerView) 
        		registry.getDataServicesView(DataHandlerView.class);
    }
    
    /** Notifies the {@link #viewer} that the data retrieval is finished. */
    public void onEnd() {}
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /** Notifies the user that the data retrieval has been cancelled. */
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
        viewer.cancel();
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel(); 
    
}
