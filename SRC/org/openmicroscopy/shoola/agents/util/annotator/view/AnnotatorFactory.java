/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
import java.util.Set;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.env.config.Registry;

//Third-party libraries

//Application-internal dependencies

/** 
* Factory to create {@link Annotator} component.
*
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
public class AnnotatorFactory
{

	/** The sole instance. */
	private static final AnnotatorFactory  singleton = new AnnotatorFactory();

	/**
	 * Returns the {@link Annotator}.
	 * 
	 * @param parent	The owner of the dialog.
	 * @param objects 	Collections of <code>DataObject</code>s to annotate.
	 * @param ctx 		A reference to the {@link Registry}.
	 * @return See above.
	 */
	public static Annotator getAnnotator(JFrame parent, Set objects, 
										Registry ctx)
	{
		if (registry == null) registry = ctx;
		if (parent == null) owner = parent;
		if (objects == null || objects.size() == 0) return null;
		return singleton.createAnnotator(objects);
	}
	  
	/**
	 * Helper method. 
	 * 
	 * @return A reference to the {@link Registry}.
	 */
	public static Registry getRegistry() { return registry; }
	  
	/**
	 * Helper method. 
	 * 
	 * @return A reference to the {@link JFrame owner}.
	 */
	public static JFrame getOwner() { return owner; }
	  
	/** Reference to the registry. */
	private static Registry         registry;
	  
    /** The owner of the dialog. */
    private static JFrame			owner;
    
	/** Creates a new instance.*/
	private AnnotatorFactory() {}
	  
	/**
	 * Creates or recycles an annotator component for the specified 
	 * <code>model</code>.
	 * 
	 * @param objects The <code>DataObject</code>s to annotate.
	 * @return A {@link Annotator}.
	 */
	private Annotator createAnnotator(Set objects)
	{
		AnnotatorModel model = new AnnotatorModel(objects);
	  	AnnotatorComponent component = new AnnotatorComponent(model);
	  	model.initialize(component);
	  	component.initialize();
	      return component;
	}
  
}
