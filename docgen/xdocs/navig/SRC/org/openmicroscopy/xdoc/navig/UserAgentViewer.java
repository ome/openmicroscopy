/*
 * org.openmicroscopy.xdoc.navig.UserAgentViewer
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

package org.openmicroscopy.xdoc.navig;


//Java imports
import java.applet.AppletContext;
import java.net.MalformedURLException;
import java.net.URL;

//Third-party libraries

//Application-internal dependencies

/** 
 * Triggers the display of document pages in the browser hosting the
 * applet.
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
class UserAgentViewer
    implements DocumentViewer
{

    /** Gateway to the browser. */
    private AppletContext userAgent;
    
    /** The base <i>URL</i> of the document. */
    private URL           docBase;
    
    /** The name of the browser's window in which to display the pages. */
    private String        displayWindow;
    
    
    /**
     * Creates a new instance.
     * 
     * @param ctx Gateway to the browser.
     * @param docBase The base <i>URL</i> of the document.
     * @param displayWindow The name of the browser's window in which to display
     *                      the pages.
     */
    UserAgentViewer(AppletContext ctx, URL docBase,String displayWindow)
    {
        if (ctx == null) throw new NullPointerException("No applet context.");
        if (docBase == null) throw new NullPointerException("No doc base.");
        userAgent = ctx;
        this.docBase = docBase;
        if (displayWindow == null || displayWindow.length() == 0)
            displayWindow = "_blank";
        this.displayWindow = displayWindow;
    }
    
    /**
     * Implemented as specified by the {@link DocumentViewer} interface.
     * @see DocumentViewer#showPage(java.lang.String)
     */
    public void showPage(String url)
    {
        try {
            if (url == null || url.length() == 0)
                throw new MalformedURLException("No URL was provided.");
            
            if (url.indexOf('.') == -1) url += ".html";
            //NOTE: An enclosing section may have no html file associated to
            //it.  In this case, the stylesheet will automatically generate
            //an empty HTML file.  The the href attribute must still contain 
            //a string -- with no '.' in it.  This string is used by the
            //stylesheet to generate the file name by appending '.html'. 
            
            URL pageURL = new URL(docBase, url);
            userAgent.showDocument(pageURL, displayWindow);
        } catch (MalformedURLException mue) {
            userAgent.showStatus("Invalid document page: "+url);
        }
    }

}
