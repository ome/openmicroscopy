/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExTitleVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;




//Java imports
import java.awt.Color;
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;

/** 
 *  
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
public class FindRegExTitleVisitor
    implements ImageDisplayVisitor
{

    /** The color in which the title bar will be highlighted. */
    private static final Color HIGH_LIGHT_COLOR = Color.PINK;
    
    /** The pattern object created from the specified regular expression. */
    private Pattern pattern;
    
    public FindRegExTitleVisitor(String regEx)
    {
        pattern = RegExFactory.createCaseInsensitivePattern(regEx);
    }
    
    /** 
     * Highlight the image titleBar if the title contains the specified regular
     * expression.
     */
    public void visit(ImageNode node)
    {
        boolean b = RegExFactory.find(pattern, node.getTitle());
        if (b) node.setHighlight(HIGH_LIGHT_COLOR);
    }

    /** For now, we don't check the title of the container node. */
    public void visit(ImageSet node) {}

}
