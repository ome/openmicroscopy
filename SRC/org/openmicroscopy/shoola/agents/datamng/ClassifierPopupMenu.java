/*
 * org.openmicroscopy.shoola.agents.datamng.ClassifierPopupMenu
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DataObject;

/** 
 * The UI of the context pop-up menu used within this agent's UI. 
 * Provides buttons for accessing the properties of an object (a category group, 
 * category or image), viewing an object, and annotating an image.
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
class ClassifierPopupMenu
    extends JPopupMenu
{
    
    /** This UI component's controller and model. */
    private ClassifierPopupMenuMng  manager;
    
    /** Holds the configuration entries. */
    private Registry                config;
    
    /** 
     * Button to bring up the property sheet of an object (project, dataset 
     * or image).
     */
    JMenuItem                       properties;
    
    /** Button to view an object. */
    JMenuItem                       view;

    /** Button to annotate a dataset or an image. */
    JMenuItem                       annotate;
        
    /** Button to reload data from the DB. */
    JMenuItem                       refresh;
    
    /** 
     * Creates a new instance.
     *
     * @param agentCtrl   The agent's control component.
     */
    ClassifierPopupMenu(DataManagerCtrl agentCtrl, Registry r) 
    {
        this.config = r;
        initProperties();
        initView();
        initAnnotate();
        initRefresh();
        manager = new ClassifierPopupMenuMng(this, agentCtrl);
        buildGUI() ;
    }

    /** 
     * Sets the object the menu is going to 
     * operate on. 
     * The annotate button will be enabled only if the passed object is
     * an image summary.
     *
     * @param   t  The object for which the menu has to be brought up.
     */
    void setTarget(DataObject t) { manager.setTarget(t); }
    
    /** Creates and initializes the properties button. */
    private void initProperties() 
    {
        IconManager icons = IconManager.getInstance(config);
        properties = new JMenuItem("Properties", 
                                    icons.getIcon(IconManager.PROPERTIES));
        properties.setBorder(null);
        properties.setFont((Font) config.lookup("/resources/fonts/Labels"));
        properties.setForeground(DataManagerUIF.STEELBLUE);
        properties.setEnabled(false);    
    }

    /** Creates and initializes the view button. */
    private void initView() 
    {
        IconManager icons = IconManager.getInstance(config);
        view = new JMenuItem("View", icons.getIcon(IconManager.VIEWER));
        view.setBorder(null);
        view.setFont((Font) config.lookup("/resources/fonts/Labels"));
        view.setForeground(DataManagerUIF.STEELBLUE); 
        view.setEnabled(false);  
    }
    
    /** Creates and initializes the annotate button. */
    private void initAnnotate()
    {
        IconManager icons = IconManager.getInstance(config);
        annotate = new JMenuItem("Annotate", 
                                icons.getIcon(IconManager.ANNOTATE));
        annotate.setBorder(null);
        annotate.setFont((Font) config.lookup("/resources/fonts/Labels"));
        annotate.setForeground(DataManagerUIF.STEELBLUE);
        annotate.setEnabled(false);  
    }
    
    /** Creates and initializes the refresh button. */
    private void initRefresh() 
    {
        IconManager icons = IconManager.getInstance(config);
        refresh = new JMenuItem("Refresh", icons.getIcon(IconManager.REFRESH));
        refresh.setBorder(null);
        refresh.setFont((Font) config.lookup("/resources/fonts/Labels"));
        refresh.setForeground(DataManagerUIF.STEELBLUE); 
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(view);
        add(annotate);
        add(refresh);
    }
    
}
