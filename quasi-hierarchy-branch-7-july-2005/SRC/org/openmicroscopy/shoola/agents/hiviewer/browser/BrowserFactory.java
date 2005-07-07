/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserFactory
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link Browser} objects.
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
public class BrowserFactory
{

    /**
     * Creates a new {@link Browser}.
     * 
     * @param topNodes Each node is the top node of a visualization tree.
     *                  Don't pass <code>null</code>.
     * @return A new {@link Browser} object.
     */
    public static Browser createBrowser(Set topNodes)
    {
        if (topNodes == null) throw new NullPointerException("No top nodes.");
        
        //Create the View.  Add each visualization tree to the root display.
        RootDisplay view = new RootDisplay();
        Iterator i = topNodes.iterator();
        while (i.hasNext()) 
            view.addChildDisplay((ImageDisplay) i.next());
        
        
        //Now the Model.  In an ideal world the Model wouldn't depend on the
        //View; however right now the dependence is basically insignificant
        //(see BrowserModel code) and simplifies matters quite a bit.
        BrowserModel model = new BrowserModel(view);
        
        //Finally the Controller.  Call intialize to allow subscription.
        BrowserControl controller = new BrowserControl(model, view);
        controller.initialize();
        
        //Fit to go!
        return model;
    }
    
}
