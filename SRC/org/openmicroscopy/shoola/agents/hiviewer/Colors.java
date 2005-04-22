/*
 * org.openmicroscopy.shoola.agents.hiviewer.Colors
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

package org.openmicroscopy.shoola.agents.hiviewer;

import java.awt.Color;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Collection of static field.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Colors
{

    /** List of colors to highlight the titleBar. */
    public static final Color REGEX_ANNOTATION = Color.YELLOW;
    
    public static final Color REGEX_TITLE = Color.PINK;
    
    public static final Color ANNOTATED = Color.ORANGE;
    
    /** 
     * The color in which the title bar will be highlighted.
     * By default we display the title bar in highlighted mode using this
     * color. 
     */
    public static final Color   DEFAULT_TITLEBAR = new Color(189, 210, 230);
    
    public static final Color   DEFAULT_TITLEBAR_ANNOTATED = 
                                                    new Color(210, 240, 230);
    
    
}
