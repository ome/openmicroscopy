/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.DataLoader;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerModel
{

    /** Holds one of the state flags defined by {@link TreeViewer}. */
    private int                 state;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private DataLoader          currentLoader;
    
    /** The browsers controlled by the model. */
    private Map                 browsers;
    
    /** The currently selected {@link Browser}. */
    private Browser             selectedBrowser;
    
    /**
     * The type of the <code>DataObject</code> to create.
     * Might be <code>null</code>.
     */
    private Class               dataObjectType;
    
    /** Reference to the component that embeds this model. */
    protected TreeViewer        component;
    
    /** Creates the browsers controlled by this model. */
    private void createBrowsers()
    {
        Browser browser = 
                BrowserFactory.createBrowser(Browser.HIERARCHY_EXPLORER);
        selectedBrowser = browser;
        browsers.put(new Integer(Browser.HIERARCHY_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.CATEGORY_EXPLORER);
        browsers.put(new Integer(Browser.CATEGORY_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.IMAGES_EXPLORER);
        browsers.put(new Integer(Browser.IMAGES_EXPLORER), browser);
    }
    
    /**
     * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
     */
    protected TreeViewerModel()
    {
        state = TreeViewer.NEW;
        browsers = new HashMap();
        createBrowsers();
    }
    
    /**
     * Called by the <code>TreeViewer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(TreeViewer component) { this.component = component; }

    /**
     * Sets the currently selected {@link Browser}.
     * 
     * @param browser The currently selected {@link Browser}.
     */
    void setSelectedBrowser(Browser browser) { selectedBrowser =  browser; }
    
    /**
     * Returns the selected {@link Browser}.
     * 
     * @return See above.
     */
    Browser getSelectedBrowser() { return selectedBrowser; }
    
    /**
     * Returns the browsers.
     * 
     * @return See above.
     */
    Map getBrowsers() { return browsers; }

    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link TreeViewer} interface.  
     */
   int getState() { return state; }    
   
   void fireDataObjectCreation(DataObject object)
   {
       
   }
   
   /*
   void setDataObjectType(Class doType)
   { 
       if (doType == null || (doType.equals(ProjectData.class)) || 
           (doType.equals(DatasetData.class)) ||
           (doType.equals(CategoryData.class)) ||
           (doType.equals(CategoryGroupData.class)))
           dataObjectType = doType;
       else throw new IllegalArgumentException("DataObject type not supported");
   }
     
   Class getDataObjectType() { return dataObjectType; }
   */
   
}
