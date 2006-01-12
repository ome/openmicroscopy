/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.ViewModeButton
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

package org.openmicroscopy.shoola.agents.hiviewer.tpane;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * The switch view mode button in the {@link TitleBar}.
 * This is a small MVC component that is aggregated into the bigger MVC set
 * of the {@link TinyPane}.  The MVC parts of this button are all collapsed
 * in this class.
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
class ViewModeButton
    extends JButton
    implements TinyObserver, PropertyChangeListener, ActionListener
{

    /** Tooltip text when the button allows to switch to single-view mode. */
    static final String SINGLE_VIEW_TOOLTIP = "Switch to single-view mode: "+
                                              "view one item at a time.";
    
    /** 
     * Text of the drop-down menu item that allows to switch back to 
     * multi-view mode.
     */
    static final String MULTI_VIEW = "Multi-view Mode";
    
    /** Tooltip text when the button allows to display the drop-down menu. */
    static final String DROP_DOWN_TOOLTIP = "List all contained items.";
    
    
    /** The Model this button is working with. */
    private TinyPane   model;
    
    /**
     * The drop-down menu that contains the children that can be displayed 
     * along with a button to switch back to the multi-view mode.
     * The items corresponding to the children in the Model are built every
     * time the menu is requested &#151; this way we avoid having to sync with
     * the Model if children are added/removed.  However, the item that allows
     * to switch back to the multi-view mode is never replaced.
     */
    private JPopupMenu  dropDownMenu;
    
    
    /**
     * Creates the item to switch back to multi-view mode and adds it to 
     * the {@link #dropDownMenu}.
     * This item should always be the first item in the menu.
     */
    private void initDropDownMenu()
    {
        IconManager icons = IconManager.getInstance();
        JMenuItem switchItem = new JMenuItem(MULTI_VIEW, 
                                    icons.getIcon(IconManager.MULTI_VIEW_MODE));
        dropDownMenu.add(switchItem);
        switchItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                model.setSingleViewMode(false);
            }
        });
    }
    
    /**
     * Removes all components in the {@link #dropDownMenu} except the item
     * that allows to switch back to the multi-view mode.
     */
    private void clearDropDownMenu()
    {
        int items = dropDownMenu.getComponentCount();
        while (1 < items) dropDownMenu.remove(--items);
    }
    
    /**
     * Pops up the drop-down menu that contains the children that can be
     * displayed along with a button to switch back to the multi-view mode.
     */
    private void displayMenu()
    {
        String title;
        
        //First off, get rid of all previous entries (if any) for desktop 
        //children.
        clearDropDownMenu();
        
        //Now make an item for the current child view and have it display
        //a checkmark.
        Component curView = model.getChildView();
        if (curView != null) {
            title = curView.getName();
            if (curView instanceof TinyPane) 
                title = ((TinyPane) curView).getTitle();
            JCheckBoxMenuItem cvi = new JCheckBoxMenuItem(title);
            cvi.setSelected(true);
            dropDownMenu.add(cvi);
        }
        
        //Then make an entry for every child in the inner desktop.
        //Note that when in single-view mode, the inner desktop contains
        //all its original children except the current child view which
        //is showing in the tmp single-view desktop.
        Component[] comp = model.getInternalDesktop().getComponents();
        if (0 < comp.length) {
            String[] items = new String[comp.length];
            dropDownMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
            for (int i = 0; i < comp.length; ++i) {
                final Component child = comp[i];
                title = child.getName();
                if (child instanceof TinyPane) 
                    title = ((TinyPane) child).getTitle();
                items[i] = title;   
            }
            
            JList list = new JList(items);
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            list.getSelectionModel().addListSelectionListener(
                    new ListListener(list));
            
            list.setLayoutOrientation(JList.VERTICAL);
            JScrollPane sp = new JScrollPane(list);
            JMenu selectMenu = new JMenu("Select");
            list.setBackground(selectMenu.getBackground());
            selectMenu.add(sp);
            //Add menu to the dropDownMenu
            dropDownMenu.add(selectMenu);
        }
        
        //Finally bring the menu on screen.
        Dimension d = getPreferredSize();
        dropDownMenu.show(this, 0, d.height);
    }
    
    /** Utility class.  */
    private class ListListener implements ListSelectionListener
    {

        /**
         * Reference to the JList the listener is attached to via the 
         * selectionModel.
         */
        private JList list;
        
        /** 
         * Creates a new intance. 
         * 
         * @param list The list to listen to. Mustn't be <code>null</code>.
         */
        ListListener(JList list) {
            if (list == null)
                throw new IllegalArgumentException("no list specified");
            this.list = list;
        }

        /**
         * Listens to the selection 
         * @see ListSelectionListener#valueChanged(ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e)
        {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) return;
            int index = list.getSelectedIndex();
            Component child = model.getInternalDesktop().getComponent(index);
            model.setChildView(child); 
            dropDownMenu.setVisible(false);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this button will be working with.
     *              Mustn't be <code>null</code>.
     */
    ViewModeButton(TinyPane model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        dropDownMenu = new JPopupMenu();
        initDropDownMenu();
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        //Just to make sure button sz=icon sz.
        setMargin(new Insets(0, 0, 0, 0));  
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
    }
    
    /**
     * Registers this button with the Model.
     * @see TinyObserver#attach()
     */
    public void attach() 
    { 
        addActionListener(this);
        model.addPropertyChangeListener(TinyPane.SINGLE_VIEW_PROPERTY, this);
        propertyChange(null);  //Synch button w/ current state.
    }

    /**
     * Detaches this button from the Model's change notification registry.
     * @see TinyObserver#detach()
     */
    public void detach() 
    { 
        model.removePropertyChangeListener(TinyPane.SINGLE_VIEW_PROPERTY, this); 
    }
    
    /** 
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#isFocusable()
     */
    public boolean isFocusable() { return false; }
    
    /** 
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#requestFocus()
     */
    public void requestFocus() {}
    
    /**
     * Sets the button appearence according to the single-view state of the 
     * Model.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        //NOTE: We can only receive SINGLE_VIEW_PROPERTY changes, see attach().
        IconManager icons = IconManager.getInstance();
        if (model.isSingleViewMode()) {
            setIcon(icons.getIcon(IconManager.VIEWS_LIST));
            setRolloverIcon(icons.getIcon(IconManager.VIEWS_LIST_OVER));
            setToolTipText(DROP_DOWN_TOOLTIP);
        } else {
            setIcon(icons.getIcon(IconManager.SINGLE_VIEW_MODE));
            setRolloverIcon(icons.getIcon(IconManager.SINGLE_VIEW_MODE_OVER));
            setToolTipText(SINGLE_VIEW_TOOLTIP);
        }
    }

    /**
     * Switches to single-view mode or displays the drop down menu if we're
     * already in single-view mode.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        if (model.isSingleViewMode()) displayMenu();
        else model.setSingleViewMode(true);
    }
    
}
