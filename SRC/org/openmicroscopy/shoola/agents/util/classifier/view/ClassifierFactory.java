/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierFactory 
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
import java.util.Set;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 *  Factory to create {@link Classifier} component.
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
public class ClassifierFactory
{
	
	/** The sole instance. */
    private static final ClassifierFactory  singleton = new ClassifierFactory();

    /**
     * Returns the {@link Classifier}.
     * 
     * @param parent	The owner of the dialog.
     * @param objects 	Collections of <code>Image</code>s to classify.
     * @param rootID	The Id of the root node.
     * @param m         The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE}, 
     *                  {@link Classifier#DECLASSIFY_MODE}.
     * @param ctx 		A reference to the {@link Registry}.
     * @return See above.
     */
    public static Classifier getClassifier(JFrame parent, 
    			Set objects, long rootID, int m, Registry ctx)
    {
    	if (registry == null) registry = ctx;
    	if (owner == null) owner = parent;
    	if (objects == null || objects.size() == 0) return null;
    	 return singleton.createClassifier(objects, rootID, m);
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
    private ClassifierFactory() {}
    
    /**
     * Creates a classifier component for the specified <code>model</code>.
     * 
     * @param objects 	The <code>Image</code>s to classify.
     * @param rootID	The Id of the root node.
     * @param m         The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE}, 
     *                  {@link Classifier#DECLASSIFY_MODE}.
     * @return A {@link Classifier}.
     */
    private Classifier createClassifier(Set objects, long rootID, int m)
    {
    	ClassifierModel model = new ClassifierModel(objects, rootID, m);
    	ClassifierComponent component = new ClassifierComponent(model);
    	model.initialize(component);
    	component.initialize();
        return component;
    }
    
}
