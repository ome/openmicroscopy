/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.GroupComponent
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import pojos.GroupData;


/** 
 * A UI component hosting a <code>GroupData</code> object.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class GroupComponent
	extends JButton
	implements ActionListener, PropertyChangeListener
{

    /** 
     * The symbol added to the text of the button to show the group's details.
     */
    private static final String EXPAND = " >>";
    
    /** 
     * The symbol added to the text of the button to hide the group's details.
     */
    private static final String COLLAPSE = " <<";
    
    /** The {@link GroupData group}  hosted by this component. */
    private GroupData group;
    
    /** A reference to parent. */
    private DOInfo		info;
    
    /** 
     * Flag to determine if the group's details are already visible.
     * If <code>true</code>, they are removed from the parent's panel,
     * otherwise they are displayed.
     */
    private boolean		detailsVisible;
    
    /** 
     * The group's details
     * @see EditorUtil#transformGroup(GroupData)
     */
    private Map			details;
    
    /**
     * Creates a new instance.
     * 
     * @param group The {@link GroupData group}  hosted by this component.
     * 				Mustn't be <code>null</code>.
     * @param info	The parent. Mustn't be <code>null</code>.
     */
    GroupComponent(GroupData group, DOInfo info)
    {
        if (group == null) 
            throw new IllegalArgumentException("No group.");
        if (info == null)
            throw new IllegalArgumentException("No parent.");
        this.group = group;
        this.info = info;
        details = EditorUtil.transformGroup(group);
        detailsVisible = false;
        setText(group.getName()+EXPAND);
        addActionListener(this);
    }
    
    /** 
     * If {@link #detailsVisible} is set to <code>true</code>, the group's 
     * details are displayed. 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (!detailsVisible) {
            setText(group.getName()+COLLAPSE);
            info.showGroupDetails(details);
        } else {
            setText(group.getName()+EXPAND);
            info.hideGroupDetails();
        }
        detailsVisible = !detailsVisible;
    }

    /**
     * Reacts to changes fired by {@link DOInfo}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        // TODO Auto-generated method stub
        
    }
    
}
