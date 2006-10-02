/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Basic editor to display <code>Image</code> related information.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class EditorPane
    extends ClipBoardPane
{

    /** The component hosting the display. */
    private EditorPaneUI    uiDelegate;
    
    /** 
     * Flag to indicate if the current user is the owner of the edited 
     * object.
     */
    private boolean         objectOwner;
    
    /**
     * Fills the <code>name</code> and <code>description</code> of the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    private DataObject fillDataObject()
    {
        Object hierarchyObject = model.getHierarchyObject();
        if (hierarchyObject instanceof ProjectData) {
            ProjectData data = (ProjectData) hierarchyObject;
            data.setName(uiDelegate.getObjectName());
            data.setDescription(uiDelegate.getObjectDescription());
            return data;
        } else if (hierarchyObject instanceof DatasetData) {
            DatasetData data = (DatasetData) hierarchyObject;
            data.setName(uiDelegate.getObjectName());
            data.setDescription(uiDelegate.getObjectDescription());
            return data;
        } else if (hierarchyObject instanceof CategoryData) {
            CategoryData data = (CategoryData) hierarchyObject;
            data.setName(uiDelegate.getObjectName());
            data.setDescription(uiDelegate.getObjectDescription());
            return data;
        } else if (hierarchyObject instanceof CategoryGroupData) {
            CategoryGroupData data = (CategoryGroupData) hierarchyObject;
            data.setName(uiDelegate.getObjectName());
            data.setDescription(uiDelegate.getObjectDescription());
            return data;
        } else if (hierarchyObject instanceof ImageData) {
            ImageData data = (ImageData) hierarchyObject;
            data.setName(uiDelegate.getObjectName());
            data.setDescription(uiDelegate.getObjectDescription());
            return data;
        } 
        return null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public EditorPane(ClipBoard model)
    {
        super(model);
        uiDelegate = new EditorPaneUI(this);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        
        // add uiDelegate which is the FindPaneUI. 
        add(uiDelegate, c);
    }
    
    /**
     * Edits the specified object.
     * 
     * @param ho    The object to edit.
     */
    public void edit(Object ho)
    {
        if (ho == null || !(ho instanceof DataObject)) {
            uiDelegate.setAreas("", "", uiDelegate.getMessage(null), false);
            uiDelegate.displayDetails(null, null);
            return; //root
        } 
        ExperimenterData user = model.getUserDetails();
        if (ho instanceof ImageData) {
            ImageData data = (ImageData) ho;
            uiDelegate.setAreas(data.getName(), data.getDescription(), 
                    uiDelegate.getMessage(data), model.isObjectWritable(data));
            ExperimenterData owner =  data.getOwner();
            Map details = EditorPaneUtil.transformExperimenterData(owner);
            objectOwner = (owner.getId() == user.getId());
            uiDelegate.displayDetails(details, data.getPermissions());
        } else if (ho instanceof DatasetData) {
            DatasetData data = (DatasetData) ho;
            uiDelegate.setAreas(data.getName(), data.getDescription(), 
                    uiDelegate.getMessage(data), model.isObjectWritable(data));
            ExperimenterData owner =  data.getOwner();
            Map details = EditorPaneUtil.transformExperimenterData(owner);
            objectOwner = (owner.getId() == user.getId());
            uiDelegate.displayDetails(details, data.getPermissions());
        } else if (ho instanceof ProjectData) {
            ProjectData data = (ProjectData) ho;
            uiDelegate.setAreas(data.getName(), data.getDescription(), 
                    uiDelegate.getMessage(data), model.isObjectWritable(data));
            ExperimenterData owner =  data.getOwner();
            Map details = EditorPaneUtil.transformExperimenterData(owner);
            objectOwner = (owner.getId() == user.getId());
            uiDelegate.displayDetails(details, data.getPermissions());
        } else if (ho instanceof CategoryGroupData) {
            CategoryGroupData data = (CategoryGroupData) ho;
            uiDelegate.setAreas(data.getName(), data.getDescription(), 
                    uiDelegate.getMessage(data), model.isObjectWritable(data));
            ExperimenterData owner =  data.getOwner();
            Map details = EditorPaneUtil.transformExperimenterData(owner);
            objectOwner = (owner.getId() == user.getId());
            uiDelegate.displayDetails(details, data.getPermissions()); 
        } else if (ho instanceof CategoryData) {
            CategoryData data = (CategoryData) ho;
            uiDelegate.setAreas(data.getName(), data.getDescription(), 
                    uiDelegate.getMessage(data), model.isObjectWritable(data));
            ExperimenterData owner =  data.getOwner();
            Map details = EditorPaneUtil.transformExperimenterData(owner);
            objectOwner = (owner.getId() == user.getId());
            uiDelegate.displayDetails(details, data.getPermissions());
        }
        uiDelegate.repaint();
    }
    
    /**
     * Overridden to update the UI components when a new node is selected in the
     * <code>Browser</code>.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        objectOwner = false;
        if (model.getSelectedPaneIndex() != ClipBoard.EDITOR_PANE) return;
        if (selectedDisplay == null) edit(null);
        else edit(selectedDisplay.getHierarchyObject());
    }
    
    /**
     * Returns <code>true</code> if the current user if the owner of the
     * object, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isObjectOwner() { return objectOwner; }

    /** Save the currently edited data object. */
    void finish()
    {
        if (!(uiDelegate.isEdit())) return;
        String name = uiDelegate.getObjectName();
        if (name == null || name.length() == 0) return;
        DataObject object = fillDataObject();
        if (object == null) return;
        model.saveObject(object);
    }
    
    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Editor"; }

    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.PROPERTIES);
    }

    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.EDITOR_PANE; }
    
    /**
     * Overriden to return the description of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Data object editor."; }

}
