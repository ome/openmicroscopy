/*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserFactory
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


//Java imports

//Third-party libraries

//Application-internal dependencies
package org.openmicroscopy.shoola.agents.editor.browser;

/** 
 * Factory to create {@link Browser} objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4782 $ $Date: 2007-04-24 12:14:11 +0100 (Tue, 24 Apr 2007) $)
 * </small>
 * @since OME2.2
 */
public class BrowserFactory
{

    /**
     * Creates a new {@link Browser}, with a UI either for disply or editing
     * of the Tree data. 
     * 
     * @param editingMode Either {@link Browser#FILE_LOCKED},
     * 		{@link Browser#EDIT_EXPERIMENT}, or {@link Browser#EDIT_PROTOCOL}.
     * 
     * @return A browser component. 
     */
    public static Browser createBrowser(int editingMode, int type)
    {
        BrowserModel model = new BrowserModel(editingMode, type);
        BrowserComponent component = new BrowserComponent(model);
        model.initialize(component);
        component.initialize();
        return component;
    }
    
    /**
     * Creates a new {@link Browser}, with a UI either in the Display mode.
     * 
     * @param type Either {@link Browser#PROTOCOL} or {@link Browser#EXPERIMENT}.
     * @return A browser component. 
     * 
     */
    public static Browser createBrowser(int type)
    {
    	int editingMode;
    	if (type == Browser.EXPERIMENT) 
    		editingMode = Browser.EDIT_EXPERIMENT;
    	else 
    		editingMode = Browser.EDIT_PROTOCOL;
    	
    	return createBrowser(editingMode, type);
    }
    
}
