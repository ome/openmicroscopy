/*
 * org.openmicroscopy.shoola.agents.annotator.view.AnnotatorFactory 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.annotator.view;

//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link Annotator} component.
 * This class keeps track of the {@link Annotator} instance that has been 
 * created and is not yet in the {@link Annotator#DISCARDED} state.
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
     * @param objects Collections of <code>DataObject</code>s to annotate.
     * @return See above.
     */
    public static Annotator getAnnotator(Set objects)
    {
    	if (objects == null || objects.size() == 0) return null;
    	 return singleton.createAnnotator(objects);
    }
    
    /** Creates a new instance. */
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
