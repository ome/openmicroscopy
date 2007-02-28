/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierModel 
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
package org.openmicroscopy.shoola.agents.util.classifier.view;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.classifier.ClassificationsLoader;
import org.openmicroscopy.shoola.agents.util.classifier.ClassificationsSaver;
import org.openmicroscopy.shoola.agents.util.classifier.ClassifierLoader;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The Model component in the <code>Classifier</code> MVC triad.
 * This class tracks the <code>Classifier</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link ClassifierComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
class ClassifierModel
{
	
    /** Holds one of the state flags defined by {@link Classifier}. */
    private int					state;
    
	/** Collection of <code>Image</code>s to classify. */
	private Set 				toClassified;
	
	/** The Id of the root node. */
	private long				rootID;
	
	/** The available paths. */
	private Set					classificationPaths;
	
	/** 
	 * The classification mode, either {@link Classifier#CLASSIFY_MODE}
	 * or {@link Classifier#DECLASSIFY_MODE}.
	 */
	private int					mode;
	
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
	private ClassifierLoader	currentLoader;
	
    /** Reference to the component that embeds this model. */
    protected Classifier		component;
	
    /** 
	 * Creates a new instance.
	 * 
	 * @param objects 	Collection of <code>Image</code>s to classify.
     * @param rootID	The Id of the root node.
     * @param m         The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE}, 
     *                  {@link Classifier#DECLASSIFY_MODE}.
	 */
    ClassifierModel(Set objects, long rootID, int m)
	{
		this.toClassified = objects;
		this.rootID = rootID;
		mode = m;
		state = DataHandler.NEW;
	}
	
	/**
     * Called by the <code>Classifier</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
	void initialize(Classifier component) { this.component = component; }
	
	/**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Classifier} interface.  
     */
	int getState() { return state; }    
   
	/**
     * Sets the object in the {@link DataHandler#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
	void discard()
	{
		cancel();
        state = DataHandler.DISCARDED;
    }
   
	/**
	 * Sets the object in the {@link DataHandler#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
       if (currentLoader != null) {
           currentLoader.cancel();
           currentLoader = null;
       }
       state = DataHandler.READY;
    }
	
	/** Loads asynchronously the classifications paths. */
	void fireClassificationPathsLoading()
	{
		Iterator i = toClassified.iterator();
		Set<Long> ids = new HashSet<Long>(toClassified.size());
		Object object;
		while (i.hasNext()) {
			object = i.next();
			if (object instanceof ImageData)
				ids.add(new Long(((ImageData) object).getId()));
		}
		currentLoader = new ClassificationsLoader(component, ids, rootID, mode);
		currentLoader.load();
		state = DataHandler.LOADING;
	}
	
	/** 
	 * Saves asynchronously the classifications. 
	 * 
	 * @param categories The categories to add the images to.
	 */
	void fireClassificationsSaving(Set categories)
	{
		currentLoader = new ClassificationsSaver(component, toClassified, 
												categories, mode);
		currentLoader.load();
		state = DataHandler.SAVING;
	}

	/**
	 * Sets the available classification paths.
	 * 
	 * @param paths The value to set.
	 */
	void setClassificationPaths(Set paths)
	{ 
		classificationPaths = paths; 
		state = DataHandler.READY;
	}
	
	/**
	 * Returns the available classification paths.
	 * 
	 * @return See above.
	 */
	Set getClassificationPaths() { return classificationPaths; }
	
	/**
	 * Returns the classification mode, either {@link Classifier#CLASSIFY_MODE}
	 * or {@link Classifier#DECLASSIFY_MODE}.
	 * 
	 * @return See above.
	 */
	int getMode() { return mode; }
	
    /**
     * Returns the id of the group, the current user is using as the logging
     * group. By default, the method returns the default group. If the
     * user belongs to more than one group, the method returns the 
     * currently selected group.
     * 
     * @return See above.
     */
    long getRootGroupID() { return rootID; }

    /**
     * Returns the ID of the current user.
     * 
     * @return See above.
     */
	long getUserID()
	{
		return ((ExperimenterData) ClassifierFactory.getRegistry().lookup(
			        LookupNames.CURRENT_USER_DETAILS)).getId();
	}
	
}
