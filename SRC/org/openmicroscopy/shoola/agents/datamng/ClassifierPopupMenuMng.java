/*
 * org.openmicroscopy.shoola.agents.datamng.ClassifierPopupMenuMng
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * The UI manager of the {@link ClassifierPopupMenu}.
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
class ClassifierPopupMenuMng
    implements ActionListener 
{

    /** This UI component's view. */
    private ClassifierPopupMenu      view; 
    
    /** The agent's control component. */
    private DataManagerCtrl         control;
    
    /** 
     * The object (categoryGroup, category or image) the menu is currently
     * operating on.
     */
    private DataObject              target;
    
    public ClassifierPopupMenuMng(ClassifierPopupMenu view, 
                                DataManagerCtrl control)
    {
        this.view = view;
        this.control = control;
        initListeners();
    }

    /** 
     * Sets the object (group, category or image) the menu is going to
     * operate on. 
     * The view button will be enabled only if the passed object is
     * an image summary.
     *
     * @param t  The object for which the menu has to be brought up.
     */
    void setTarget(DataObject t) 
    {
        target = t;
        if (target != null) {
            view.properties.setEnabled(true);
            view.view.setEnabled(true);
            view.annotate.setEnabled((target instanceof ImageSummary));
        }
    }
    
    /** 
     * Reacts to activation of the menu buttons.
     *
     * @param e   Represents an activation of a menu button, as a click.
     */
    public void actionPerformed(ActionEvent e) 
    {
        Object src = e.getSource();
        if (target != null) {   
            if (src == view.properties) 
                control.showProperties(target);
            else if (src == view.view && target instanceof ImageSummary)       
                control.viewImage(((ImageSummary) target));
            else if (src == view.view && target instanceof CategoryGroupData)       
                control.viewCategoryGroup(((CategoryGroupData) target));
            else if (src == view.view && target instanceof CategorySummary)       
                control.viewCategory(((CategorySummary) target));
            else if (src == view.annotate && target instanceof ImageSummary)
                control.annotateImage(((ImageSummary) target));
        }
        view.setVisible(false);
    }
    
    /** Registers listeners with the view's widgets. */
    private void initListeners()
    {
        view.properties.addActionListener(this);
        view.view.addActionListener(this);
        view.annotate.addActionListener(this);
    }

}
