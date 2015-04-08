/*
 * org.openmicroscopy.shoola.agents.util.finder.FinderLoader 
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
package org.openmicroscopy.shoola.agents.util.finder;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Parent of all classes that load data asynchronously for a {@link Finder}.
 * All these classes invoke methods of the {@link DataHandlerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call.
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
public abstract class FinderLoader 
	extends DSCallAdapter
{

	/** Indicates to search for tags. */
	public static final int TAGS = 0;
	
	/** Indicates to search for images. */
	public static final int IMAGES = 1;
	
	/** Indicates to search for annotations. */
	public static final int ANNOTATIONS = 2;
	
	/** Indicates to search for projects. */
	public static final int PROJECTS = 3;
	
	/** Indicates to search for datasets. */
	public static final int DATASETS = 4;
	
	/** The viewer this data loader is for. */
	protected Finder viewer;
	
	/** Convenience reference for subclasses. */
    protected final Registry registry;
    
	/** Convenience reference for subclasses. */
    protected final DataHandlerView dhView;
	
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** The security context.*/
    protected final List<SecurityContext> ctx;
    
    /** 
     * Checks if the passed type is supported and returns the 
     * class corresponding to the passed value.
     * 
     * @param value The value to handle.
     * @return See above.
     */
    protected Class checkType(int value)
    {
    	switch (value) {
			case IMAGES:
				return ImageData.class;
			case ANNOTATIONS:
				return AnnotationData.class;
			case PROJECTS:
				return ProjectData.class;
			case DATASETS:
				return DatasetData.class;
			default:
				throw new IllegalArgumentException("Type not supported.");
		}
    }
    
    /**
     * Returns the string associated to the passed type.
     * 
     * @param type The type to handle.
     * @return See above.
     */
    protected String convertType(Class type)
    {
    	 if (ImageData.class.equals(type)) return "Images";
    	 return "";
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     */
    protected FinderLoader(Finder viewer, List<SecurityContext> ctx)
    {
        if (viewer == null) throw new NullPointerException("No viewer.");
        if (ctx == null || ctx.size() ==0)
        	throw new NullPointerException("No security context.");
        this.registry = FinderFactory.getRegistry();
        this.viewer = viewer;
        this.ctx = ctx;
        dhView = (DataHandlerView) 
					registry.getDataServicesView(DataHandlerView.class);
        mhView = (MetadataHandlerView) 
			registry.getDataServicesView(MetadataHandlerView.class);
    }
    
    /**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
    protected ExperimenterData getUserDetails()
    {
    	return (ExperimenterData) registry.lookup(
    					LookupNames.CURRENT_USER_DETAILS);
    }
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
        viewer.setStatus("", false);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        viewer.setStatus("", false);
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
