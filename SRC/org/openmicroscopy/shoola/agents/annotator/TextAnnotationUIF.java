/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotatorUIF
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.annotator;

import javax.swing.JInternalFrame;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/**
 * The UIF for a text annotation, not the UIF for other semantic types and
 * attributes and classifications.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class TextAnnotationUIF extends JInternalFrame
{
    private Registry registry;
    
    private TopFrame topFrame;
    
    private AnnotationCtrl controller;
    
    /**
     * Construct a new TextAnnotationUIF.
     * @param control The controller for the UI.
     * @param registry The reference to the application registry.
     */
    public TextAnnotationUIF(AnnotationCtrl control, Registry registry)
    {
        if(control == null || registry == null)
        {
            throw new IllegalArgumentException("No null arguments permitted.");
        }
        
        this.controller = control;
        this.registry = registry;
        
        buildGUI();
    }
    
    private void buildGUI()
    {
        // yup, build the GUI if we have to (TBD 3/9)
    }
}
