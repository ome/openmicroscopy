/*
 * org.openmicroscopy.shoola.agents.util.annotator.AnnotatorEditorLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Parent of all classes that load data asynchronously for a 
 * {@link AnnotatorEditor}.
 * All these classes invoke methods of the {@link DataHandlerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the callbacks to notify the 
 * {@link AnnotatorEditor} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link AnnotatorEditor} back with the results.
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
public abstract class AnnotatorEditorLoader 
	extends DSCallAdapter
{

	/** The TreeViewer this data loader is for. */
	protected final AnnotatorEditor		viewer;
	
	/** Convenience reference for subclasses. */
	protected final Registry            registry;
	
	/** Convenience reference for subclasses. */
	protected final DataHandlerView		dhView;
	
	/**
	 * Controls if the annotation type is supported. Returns <code>true</code>
	 * if the type is valid, <code>false</code> otherwise.
	 * 
	 * @param c	The class identifying the annotation type.
	 * @return See above.
	 */
	protected boolean checkAnnotationType(Class c)
	{
		if (DatasetData.class.equals(c)) return true;
		else if (ImageData.class.equals(c)) return true; 
		return false;
	} 
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Annotator this data loader is for.
	 *               Mustn't be <code>null</code>.
	 */
	protected AnnotatorEditorLoader(AnnotatorEditor viewer)
	{
	    if (viewer == null) throw new NullPointerException("No viewer.");
	    this.viewer = viewer;
	    registry = AnnotatorFactory.getRegistry();
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
	    //registry.getUserNotifier().notifyInfo("Data Retrieval Cancellation", 
	    //                                      info);
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
