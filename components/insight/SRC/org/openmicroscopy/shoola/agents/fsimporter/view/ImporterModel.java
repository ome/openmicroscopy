/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.DataImporterLoader;
import org.openmicroscopy.shoola.agents.fsimporter.ImagesImporter;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.TagsLoader;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;

/** 
 * The Model component in the <code>Importer</code> MVC triad.
 * This class tracks the <code>Importer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link ImporterComponent} intercepts the 
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
 * @since 3.0-Beta4
 */
class ImporterModel
{

	/** Holds one of the state flags defined by {@link Importer}. */
	private int					state;

	/** Reference to the component that embeds this model. */
	protected Importer			component;
	
	/** 
	 * Will either be a data loader or
	 * <code>null</code> depending on the current state. 
	 */
	private DataImporterLoader 	currentLoader;
	
	/** The collection of existing tags. */
	private Collection			tags;
	
	/** Keeps track of the different loaders. */
	private Map<Integer, ImagesImporter> loaders;
	
	/** Creates a new instance. */
	ImporterModel()
	{
		state = Importer.NEW;
		loaders = new HashMap<Integer, ImagesImporter>();
	}
	
	/**
	 * Called by the <code>FSImporter</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Importer component)
	{
		this.component = component;
	}
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link Importer} interface.  
	 */
	int getState() { return state; }    

	/**
	 * Sets the state of the component.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
	/**
	 * Sets the object in the {@link Importer#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		if (loaders.size() > 0) {
			Iterator<ImagesImporter> i = loaders.values().iterator();
			while (i.hasNext()) {
				i.next().cancel();
			}
			loaders.clear();
		}
		state = Importer.DISCARDED;
	}

	/**
	 * Sets the object in the {@link Importer#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		if (currentLoader != null) {
			currentLoader.cancel();
			currentLoader = null;
		}
		state = Importer.READY;
	}

	/**
	 * Cancels the specified on-going import.
	 * 
	 * @param loaderID The identifier of the loader.
	 */
	void cancel(int loaderID)
	{
		ImagesImporter loader = loaders.get(loaderID);
		if (loader != null) {
			//loader.cancel();
			loaders.remove(loaderID);
		}
	}
	
	/**
	 * Returns the list of the supported file formats.
	 * 
	 * @return See above.
	 */
	FileFilter[] getSupportedFormats()
	{
		return 
		ImporterAgent.getRegistry().getImageService().getSupportedFileFormats();
	}
	
	/**
	 * Fires an asynchronous call to import the files in the object.
	 * 
	 * @param data The file to import.
	 * @param loaderID The identifier of the component this loader is for.
	 */
	void fireImportData(ImportableObject data, int loaderID)
	{
		if (data == null) return;
		ImagesImporter loader = new ImagesImporter(component, data, loaderID);
		loaders.put(loaderID, loader);
		loader.load();
		state = Importer.IMPORTING;
	}
	
	/**
	 * Notifies that the import has finished.
	 * 
	 * @param loaderID  The identifier of the loader associated to the finished
	 * 					import.
	 */
	void importCompleted(int loaderID)
	{
		state = Importer.READY;
		loaders.remove(loaderID);
	}
	
	/**
	 * Returns the collection of the existing tags.
	 * 
	 * @return See above.
	 */
	Collection getTags() { return tags; }
	
	/**
	 * Sets the collection of the existing tags.
	 * 
	 * @param The value to set.
	 */
	void setTags(Collection tags)
	{ 
		this.tags = tags; 
	}
	
	/** Starts an asynchronous call to load the tags. */
	void fireTagsLoading()
	{
		if (tags != null) return; //already loading tags
		TagsLoader loader = new TagsLoader(component);
		loader.load();
	}
	
}
