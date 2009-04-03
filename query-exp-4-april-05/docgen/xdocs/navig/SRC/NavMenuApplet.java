/*
 * NavMenuApplet
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


//Java imports
import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.xdoc.navig.NavMenuManager;
import org.openmicroscopy.xdoc.navig.NavMenuUI;
import org.openmicroscopy.xdoc.navig.TOCParser;
import org.openmicroscopy.xdoc.navig.UserAgentViewer;

/** 
 * Navigation menu for browsing a multi-page <i>HTML</i> document.
 * <p>This applet is meant to be used in a multi-frame <i>HTML</i> page, in 
 * which one frame contains the applet and another frame displays <i>HTML</i>
 * pages.  The <i>HTML</i> pages are the output of one of our <i>xdoc</i> 
 * documents.  The 'doc.xml' file containing the table of contents of the 
 * document has to be copied in the same directory where the applet is deployed.  
 * This way, the applet can build a navigation tree and load the document pages
 * by parsing the 'doc.xml' file.</p>
 * <p>Here's the list of parameters that this applet accepts:</p>
 * <ul>
 *  <li><b>target</b>: The name of the browser window where to load document
 *      pages.  This should be set to the name of the frame where pages are
 *      displayed.</li>
 * </ul>
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
public class NavMenuApplet
    extends JApplet
{
    
    /** The name of the applet's target parameter. */
    private static final String     DISPLAY_WINDOW = "target";
    
    /**
     * Parses the 'doc.xml' file and builds the navigation menu.
     */
    public void init()
    {
        try {
            URL xdocURL = new URL(getCodeBase(), "doc.xml");
            TOCParser p = new TOCParser(xdocURL);
            DefaultMutableTreeNode toc = p.parse();
            NavMenuUI navTree = new NavMenuUI(toc);
            new NavMenuManager(navTree, 
                               new UserAgentViewer(getAppletContext(),
                                                   getDocumentBase(),
                                                   getParameter(DISPLAY_WINDOW))
            );  
            getContentPane().add(navTree, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel error = new JLabel("Couldn't load the table of contents.");      
            getContentPane().add(error, BorderLayout.NORTH);
        } 
    }
    
}
