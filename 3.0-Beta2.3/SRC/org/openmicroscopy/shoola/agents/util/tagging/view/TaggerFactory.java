/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerFactory 
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
package org.openmicroscopy.shoola.agents.util.tagging.view;


//Java imports
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import pojos.ImageData;

/** 
 * Factory to create {@link Tagger} component.
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
public class TaggerFactory
{
	
	/** The sole instance. */
	private static final TaggerFactory singleton = new TaggerFactory();
	
	/**
	 * Returns a tagger for the specified image.
	 * 
	 * @param registry	Helper reference to the registry.
	 * @param imageID	The id of the image the tagger is for.
	 * @return See above.
	 */
	public static Tagger getImageTagger(Registry registry, long imageID)
	{
		if (singleton.registry == null)
			singleton.registry = registry;
		Set<Long> ids = new HashSet<Long>(1);
		ids.add(imageID);
		return singleton.createImageTagger(ids);
	}
	
	/**
	 * Returns a tagger for the specified image.
	 * 
	 * @param registry	Helper reference to the registry.
	 * @param imageIDs	Collection of images' id the tagger is for.
	 * @return See above.
	 */
	public static Tagger getImageTagger(Registry registry, Set<Long> imageIDs)
	{
		if (singleton.registry == null)
			singleton.registry = registry;
		if (imageIDs == null) return null;
		return singleton.createImageTagger(imageIDs);
	}
	
	/**
	 * Returns a tagger for the specified image.
	 * 
	 * @param registry	Helper reference to the registry.
	 * @param ref		The time ref object hosting the period of time.
	 * @return See above.
	 */
	public static Tagger getImageTagger(Registry registry, TimeRefObject ref)
	{
		if (singleton.registry == null)
			singleton.registry = registry;
		if (ref == null) return null;
		return singleton.createImageTagger(ref);
	}
	
	/**
	 * Returns a tagger for the specified image.
	 * 
	 * @param registry	Helper reference to the registry.
	 * @param ids		Collection of objects' id the tagger is for.
	 * @param rootType	The type of object to tag.
	 * @param depth		The level of the tagging.
	 * @return See above.
	 */
	public static Tagger getContainerTagger(Registry registry, Set<Long> ids, 
											Class rootType, int depth)
	{
		if (singleton.registry == null)
			singleton.registry = registry;
		if (ids == null) return null;
		return singleton.createContainerTagger(ids, rootType, depth);
	}
	
	/**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
	public static Registry getRegistry() { return singleton.registry; }
	
	/**
	 * Returns the task bar frame.
	 * 
	 * @return See above.
	 */
	public static JFrame getRefFrame()
	{
		return singleton.registry.getTaskBar().getFrame();
	}
	
	/** Reference to the registry. */
	private Registry 	registry;
	

	/** Creates a new instance. */
	private TaggerFactory()
	{
		//TODO
	}
	
	/**
	 * Creates or recycles a tagger component for the specified 
	 * <code>image</code>.
	 * 
	 * @param ref The time reference the tagger is for.
	 * @return See above.
	 */
	private Tagger createImageTagger(TimeRefObject ref)
	{
		TaggerModel model = new TaggerModel(ref, ImageData.class);
		TaggerComponent comp = new TaggerComponent(model);
		model.initialize(comp);
		comp.initialize();
		return comp;
	}
	
	/**
	 * Creates or recycles a tagger component for the specified 
	 * <code>image</code>.
	 * 
	 * @param imageIDs Collection of images' id the tagger is for.
	 * @return See above.
	 */
	private Tagger createImageTagger(Set<Long> imageIDs)
	{
		TaggerModel model = new TaggerModel(imageIDs, ImageData.class);
		TaggerComponent comp = new TaggerComponent(model);
		model.initialize(comp);
		comp.initialize();
		return comp;
	}
	
	/**
	 * Creates or recycles a tagger component for the specified 
	 * <code>containers</code>.
	 * 
	 * @param ids 		Collection of container' id the tagger is for.
	 * @param rootType 	Collection of container' id the tagger is for.
	 * @param level		The tagging level.
	 * @return See above.
	 */
	private Tagger createContainerTagger(Set<Long> ids, Class rootType, 
										int level)
	{
		TaggerModel model = new TaggerModel(ids, rootType, level);
		TaggerComponent comp = new TaggerComponent(model);
		model.initialize(comp);
		comp.initialize();
		return comp;
	}
	
	
}
