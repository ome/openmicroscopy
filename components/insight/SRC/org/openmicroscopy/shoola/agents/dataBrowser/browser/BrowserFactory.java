/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *      This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;



//Java imports
import java.util.Collection;
import java.util.Iterator;


//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link Browser} objects.
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
public class BrowserFactory
{

        /**
     * Creates a new {@link Browser}.
     * 
     * @param topNodes  Each node is the top node of a visualization tree.
     *                  Don't pass <code>null</code>.           
     * @return A new {@link Browser} object.
     */
    public static Browser createBrowser(Collection topNodes)
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
         //Finally the Controller.  Call initialize to allow subscription.
         BrowserControl controller = new BrowserControl(model, view);
         controller.initialize();

         //Fit to go!
         return model;
    }
    
}
