/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.ToolBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.view;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;




//Third-party libraries
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RunScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.util.GroupItem;
import org.openmicroscopy.shoola.agents.treeviewer.util.DataMenuItem;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.util.ui.ScrollablePopupMenu;
import org.openmicroscopy.shoola.util.ui.SelectableMenuItem;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The tool bar of {@link TreeViewer}.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
class ToolBar
    extends JPanel
{

    /** The text indicating the number of successful import.*/
    private static final String IMPORTED_TEXT = " Imported";

    /** The text indicating the number of failed import.*/
    private static final String FAILED_TEXT = " Failed";

    /** The tool tip used to indicate on-going import.*/
    private static final String IMPORT_TOOLTIP = "Indicates on-going imports";

    /** Size of the horizontal box. */
    private static final Dimension HBOX = new Dimension(100, 16);

    /** Text indicating to display the groups.*/ 
    private static final String GROUP_DISPLAY_TEXT = "Display Groups";
    
    /** The width of search field in characters */
    private static final int SEARCHFIELD_WIDTH = 12;
    
    /** The default text shown in the empty search field */
    private static final String SEARCHFIELD_TEXT = "Search ...";

    /** Reference to the control. */
    private TreeViewerControl controller;

    /** Reference to the model. */
    private TreeViewerModel model;

    /** Reference to the view. */
    private TreeViewerWin view;

    /** The menu displaying the groups the user is a member of. */
    private JPopupMenu personalMenu;

    /** Button to open the full in a separate window. */
    private JToggleButton fullScreen;

    /** The menu displaying the available scripts.*/
    private JPopupMenu scriptsMenu;

    /** The button showing the available scripts.*/
    private JButton scriptButton;

    /** Indicates the loading progress. */
    private JXBusyLabel busyLabel;

    /** The index of the {@link #scriptButton}.*/
    private int index;

    /** The management bar.*/
    private JToolBar bar;

    /** The button to display the menu.*/
    private JButton menuButton;

    /** Used to sort the list of users.*/
    private ViewerSorter sorter;

    /** The menu displaying the users option.*/
    private JPopupMenu popupMenu;

    /** Label indicating the number of successful import if any.*/
    private JLabel importSuccessLabel;

    /** Label indicating the number of failed import if any.*/
    private JLabel importFailureLabel;

    /** Label indicating the import status.*/
    private JXBusyLabel importLabel;

    /** The terms to search for. */
    private JTextField                              searchField;
    
    /**
     * Sets either the user display or group display.
     *
     * @param userDisplay Pass <code>true</code> for user display,
     *                    <code>false</code> for group display.
     */
    private void handleSelectionDisplay(boolean userDisplay)
    {
        int n = popupMenu.getComponentCount();
        GroupItem item;
        Component c;
        for (int i = 0; i < n; i++) {
            c = popupMenu.getComponent(i);
            if (c instanceof GroupItem) {
                item = (GroupItem) c;
                item.setDisplay(userDisplay);
            }
        }
        int mode = LookupNames.EXPERIMENTER_DISPLAY;
        if (!userDisplay) mode = LookupNames.GROUP_DISPLAY;
        controller.setDisplayMode(mode);
    }

    /**
     * Selects or de-selects all the groups.
     *
     * @param select Pass <code>true</code> to select all the groups,
     *               <code>false</code> otherwise.
     */
    private void handleAllGroupsSelection(boolean select)
    {
        int n = popupMenu.getComponentCount();
        GroupItem item;
        Component c;
        for (int i = 0; i < n; i++) {
            c = popupMenu.getComponent(i);
            if (c instanceof GroupItem) {
                item = (GroupItem) c;
                if (item.getGroup() != null) {
                    item.setMenuSelected(select, false);
                    if (select) item.selectUsers(false, true);
                }
            }
        }
        handleSelection();
    }

    /**
     * Selects or de-selects all the users.
     *
     * @param select Pass <code>true</code> to select all the groups,
     *               <code>false</code> otherwise.
     */
    private void handleAllUsersSelection(boolean select)
    {
        int n = popupMenu.getComponentCount();
        GroupItem item;
        Component c;
        for (int i = 0; i < n; i++) {
            c = popupMenu.getComponent(i);
            if (c instanceof GroupItem) {
                item = (GroupItem) c;
                if (item.getGroup() != null) {
                    item.setMenuSelected(true, false);
                    item.selectUsers(true, select);
                }
            }
        }
        handleSelection();
    }

    /** Handles the selection of users.*/
    private void handleSelection()
    {
        int n = popupMenu.getComponentCount();
        GroupItem item;
        Component c;
        boolean b;
        List<ExperimenterData> users;
        int count = 0;
        int total = 0;
        GroupItem allGroups = null;
        for (int i = 0; i < n; i++) {
            c = popupMenu.getComponent(i);
            if (c instanceof GroupItem) {
                item = (GroupItem) c;
                if (item.getGroup() != null) {
                    total++;
                    b = !item.isMenuSelected();
                    users = item.getSeletectedUsers();
                    
                    if (n == 1) {
                        if (b) { //group not selected
                            users = new ArrayList<ExperimenterData>();
                        }
                        b = false;
                    }
                    if (!b) count++;
                    controller.setSelection(item.getGroup(), users, b);
                } else {
                    if (GroupItem.ALL_GROUPS.equals(item.getText())) {
                        allGroups = item;
                    }
                }
            }
        }
        if (allGroups != null) {
            allGroups.setMenuSelected(total == count, false);
        }
    }

    /** 
     * Fires a search event
     */
    private void search() {
        TreeViewerAgent.getRegistry().getEventBus().post(new SearchEvent(searchField.getText()));
    }
    
    /**
     * Formats the header.
     * 
     * @param text The text to display
     * @return See above.
     */
    private JPanel formatHeader(String text)
    {
        JPanel title = new JPanel();
        title.setLayout(new FlowLayout(FlowLayout.LEFT));
        title.add(UIUtilities.setTextFont(text));
        title.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
        return title;
    }

    /**
     * Creates the menu hosting the users belonging to the specified group.
     * Returns <code>true</code> if the group is selected, <code>false</code>
     * otherwise.
     * 
     * @param groupItem The item hosting the group.
     * @param size The number of groups.
     * @return See above.
     */
    private boolean createGroupMenu(GroupItem groupItem, int size)
    {
        long loggedUserID = model.getUserDetails().getId();
        GroupData group = groupItem.getGroup();
        //Determine the user already added to the display
        Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
        TreeImageDisplay refNode = null;
        List<TreeImageDisplay> nodes;
        ExperimenterVisitor visitor;
        List<Long> users = new ArrayList<Long>();
        //Find the group already displayed
        if (group != null && size > 0) {
            visitor = new ExperimenterVisitor(browser, group.getId());
            browser.accept(visitor);
            nodes = visitor.getNodes();
            if (nodes.size() == 1) {
                refNode = nodes.get(0);
            }
            visitor = new ExperimenterVisitor(browser, -1, -1);
            if (refNode != null) refNode.accept(visitor);
            else if (size == 1) browser.accept(visitor);
            nodes = visitor.getNodes();
            
            TreeImageDisplay n;
            if (CollectionUtils.isNotEmpty(nodes)) {
                Iterator<TreeImageDisplay> j = nodes.iterator();
                while (j.hasNext()) {
                    n = j.next();
                    if (n.getUserObject() instanceof ExperimenterData) {
                        users.add(((ExperimenterData) n.getUserObject()).getId());
                    }
                }
                if (size == 1) {
                    groupItem.setMenuSelected(true, false);
                }
            }
        }
        
        //now add the users
        List<DataMenuItem> items = new ArrayList<DataMenuItem>();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        List l = null;
        if (group != null) l = sorter.sort(group.getLeaders());
        Iterator i;
        ExperimenterData exp;

        DataMenuItem item, allUser;
        
        boolean view = true;
        if (group != null) {
            int level = group.getPermissions().getPermissionsLevel();
            if (level == GroupData.PERMISSIONS_PRIVATE) {
                view = model.isAdministrator() || model.isGroupOwner(group);
            }
        }
        
        List<DataMenuItem> list = new ArrayList<DataMenuItem>();

        allUser = new DataMenuItem(DataMenuItem.ALL_USERS_TEXT, true);
        items.add(allUser);
        if (view) p.add(allUser);
        int count = 0;
        int total = 0;
        if (CollectionUtils.isNotEmpty(l)) {
            total += l.size();
            i = l.iterator();
            while (i.hasNext()) {
                exp = (ExperimenterData) i.next();
                if (view || exp.getId() == loggedUserID) {
                    item = new DataMenuItem(exp, true);
                    item.setChecked(users.contains(exp.getId()));
                    if (item.isChecked()) count++;
                    item.addPropertyChangeListener(groupItem);
                    items.add(item);
                    list.add(item);
                }
            }
            if (list.size() > 0) {
                p.add(formatHeader("Group owners"));
                for(DataMenuItem dmi : list)
                    p.add(dmi);
                list.clear();
            }
        }

        if (group != null) l = sorter.sort(group.getMembersOnly());
        if (CollectionUtils.isNotEmpty(l)) {
            total += l.size();
            i = l.iterator();
            while (i.hasNext()) {
                exp = (ExperimenterData) i.next();
                if (view || exp.getId() == loggedUserID) {
                    item = new DataMenuItem(exp, true);
                    item.setChecked(users.contains(exp.getId()));
                    if (item.isChecked()) count++;
                    item.addPropertyChangeListener(groupItem);
                    items.add(item);
                    list.add(item);
                }
            }
            if (list.size() > 0) {
                p.add(formatHeader("Members"));
                for(DataMenuItem dmi : list)
                    p.add(dmi);
            }
        }
        allUser.setChecked(total != 0 && total == count);
        allUser.addPropertyChangeListener(groupItem);
        JScrollPane pane = new JScrollPane(p);
        Dimension d = p.getPreferredSize();
        int max = 500;
        if (d.height > max) {
            Insets insets = pane.getInsets();
            pane.setPreferredSize(
                    new Dimension(d.width+insets.left+insets.right+20, max));
        }
        
        groupItem.add(pane);
        groupItem.setUsersItem(items);
        groupItem.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (GroupItem.USER_SELECTION_PROPERTY.equals(name))
                    handleSelection();
                else if (GroupItem.ALL_GROUPS_SELECTION_PROPERTY.equals(name))
                    handleAllGroupsSelection(true);
                else if (GroupItem.ALL_GROUPS_DESELECTION_PROPERTY.equals(name))
                    handleAllGroupsSelection(false);
                else if (GroupItem.ALL_USERS_SELECTION_PROPERTY.equals(name))
                    handleAllUsersSelection((Boolean) evt.getNewValue());
            }
        });
        return groupItem.isMenuSelected();
    }

    /**
     * Creates the menu displaying the groups and users.
     * 
     * @param source The invoker.
     * @param p The location of the mouse clicked.
     */
    private void createGroupsAndUsersMenu(Component source, Point p)
    {
        if (!source.isEnabled()) return;
        Collection groups = model.getGroups();
        if (CollectionUtils.isEmpty(groups)) return;
        popupMenu.removeAll();
        GroupData group;
        List sortedGroups = sorter.sort(groups);

        //Determine the group already displayed.
        Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
        List<TreeImageDisplay> nodes;
        ExperimenterVisitor visitor;
        //Find the user already added to the selected group.
        visitor = new ExperimenterVisitor(browser, -1);
        browser.accept(visitor);
        nodes = visitor.getNodes();
        Iterator<TreeImageDisplay> k = nodes.iterator();
        List<Long> groupIds = new ArrayList<Long>();
        long id;
        while (k.hasNext()) {
            id = k.next().getUserObjectId();
            if (id >= 0) groupIds.add(id);
        }

        //Create the group menu.
        Iterator i = sortedGroups.iterator();
        int size = sortedGroups.size();
        long userID = model.getExperimenter().getId();

        //First add item to toggle between users and group display
        final SelectableMenuItem data = new SelectableMenuItem(
                model.getDisplayMode() == LookupNames.EXPERIMENTER_DISPLAY,
                DataMenuItem.USERS_TEXT, true);
        data.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (SelectableMenuItem.SELECTION_PROPERTY.equals(name)) {
                    handleSelectionDisplay(data.isChecked());
                }
            }
        });
        popupMenu.add(data);
        popupMenu.add(new JSeparator());
        GroupItem item;
        GroupItem allGroup = null;
        //First add option to add all the groups.
        if (size > 1) {
            item = new GroupItem(false);
            item.setUserID(userID);
            createGroupMenu(item, 0);
            popupMenu.add(item);
            popupMenu.add(new JSeparator());
            allGroup = item;
        }

        boolean selected;
        int count = 0;
        while (i.hasNext()) {
            group = (GroupData) i.next();
            boolean b = groupIds.contains(group.getId());
            item = new GroupItem(group, b, size > 1);
            item.setUserID(userID);
            selected = createGroupMenu(item, size);
            popupMenu.add(item);
            if (selected) count++;
        }
        if (allGroup != null) {
            allGroup.setMenuSelected(count == sortedGroups.size(), false);
        }
        popupMenu.show(source, p.x, p.y);
    }

    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
        item.setBorder(null);
        item.setFont((Font) 
                TreeViewerAgent.getRegistry().lookup(
                        "/resources/fonts/Labels"));
    }

    /**
     * Helper method to create the tool bar hosting the management items.
     * 
     * @return See above.
     */
    private JComponent createManagementBar()
    {
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        JToggleButton button = new JToggleButton(
                controller.getAction(TreeViewerControl.INSPECTOR));
        button.setSelected(true);
        bar.add(button);

        button = new JToggleButton(
                controller.getAction(TreeViewerControl.METADATA));
        button.setSelected(true);
        bar.add(button);

        JButton b = new JButton(controller.getAction(TreeViewerControl.BROWSE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        boolean ij = false;
        switch (TreeViewerAgent.runAsPlugin()) {
        case LookupNames.IMAGE_J:
        case LookupNames.IMAGE_J_IMPORT:
            ij = true;
            b = UIUtilities.formatButtonFromAction(
                    controller.getAction(TreeViewerControl.VIEW));
            UIUtilities.unifiedButtonLookAndFeel(b);
            b.addMouseListener(new MouseAdapter() {

                /**
                 * Displays the menu when the user releases the mouse.
                 * @see MouseListener#mouseReleased(MouseEvent)
                 */
                public void mouseReleased(MouseEvent e)
                {
                    controller.showMenu(TreeViewer.VIEW_MENU,
                            (JComponent) e.getSource(), e.getPoint());
                }
            });
            bar.add(b);
            break;
        default:
            b = new JButton(controller.getAction(TreeViewerControl.VIEW));
            UIUtilities.unifiedButtonLookAndFeel(b);
            bar.add(b);
        }
        bar.add(b);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        //Now register the agent if any
        TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
        List<JComponent> l = tb.getToolBarEntries(TaskBar.AGENTS);
        if (l != null) {
            Iterator<JComponent> i = l.iterator();
            JComponent comp;
            while (i.hasNext()) {
                comp = i.next();
                UIUtilities.unifiedButtonLookAndFeel(comp);
                bar.add(comp);
            }
            bar.add(new JSeparator(JSeparator.VERTICAL));
        }
        fullScreen = new JToggleButton(
                controller.getAction(TreeViewerControl.FULLSCREEN));
        fullScreen.setSelected(model.isFullScreen());
        //bar.add(fullScreen);
        if (TreeViewerAgent.isAdministrator()) {
            b = new JButton(controller.getAction(
                    TreeViewerControl.UPLOAD_SCRIPT));
            UIUtilities.unifiedButtonLookAndFeel(b);
            bar.add(b);
        }
        TreeViewerAction a = controller.getAction(
                TreeViewerControl.AVAILABLE_SCRIPTS);
        b = new JButton(a);
        Icon icon  = b.getIcon();
        Dimension d = new Dimension(UIUtilities.DEFAULT_ICON_WIDTH,
                UIUtilities.DEFAULT_ICON_HEIGHT);
        if (icon != null) 
            d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        busyLabel = new JXBusyLabel(d);
        busyLabel.setVisible(true);
        b.addMouseListener((RunScriptAction) a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        scriptButton = b;
        bar.add(b);
        index = bar.getComponentCount()-1;

        bar.add(new JSeparator(JSeparator.VERTICAL));

        MouseAdapter adapter = new MouseAdapter() {

            /**
             * Shows the menu corresponding to the display mode.
             */
            public void mousePressed(MouseEvent me)
            {
                createGroupsAndUsersMenu((Component) me.getSource(),
                        me.getPoint());
            }
        };

        a = controller.getAction(TreeViewerControl.SWITCH_USER);
        IconManager icons = IconManager.getInstance();
        menuButton = new JButton(icons.getIcon(IconManager.FILTER_MENU));
        menuButton.setVisible(true);
        menuButton.setText(GROUP_DISPLAY_TEXT);
        menuButton.setHorizontalTextPosition(SwingConstants.LEFT);
        menuButton.addMouseListener(adapter);
        bar.add(menuButton);
        setPermissions();
        if (ij) {
            b = new JButton(a = controller.getAction(
                    TreeViewerControl.SAVE_TO_OMERO));
            bar.add(Box.createHorizontalStrut(5));
            bar.add(b);
        }
        return bar;
    }

    /**
     * Helper method to create the tool bar hosting the edit items.
     * 
     * @return See above.
     */
    private JToolBar createSearchBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        bar.add(new JToggleButton(
                controller.getAction(TreeViewerControl.SEARCH)));
        return bar;
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel bars = new JPanel(), outerPanel = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createManagementBar());
        if (!TreeViewerWin.JXTASKPANE_TYPE.equals(view.getLayoutType())) {
            bars.add(createSearchBar());
        }

        outerPanel.setBorder(null);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(bars);
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createHorizontalGlue());

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(UIUtilities.buildComponentPanel(outerPanel));
        add(UIUtilities.buildComponentPanelRight(buildRightPane()));
    }

    /** 
     * Builds and lays out the component displayed on the right hand side of
     * the toolbar.
     * 
     * @return See above.
     */
    private JPanel buildRightPane()
    {
        JPanel p = new JPanel();
        p.setBorder(null);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(searchField);
        p.add(Box.createHorizontalStrut(50));
        p.add(importFailureLabel);
        p.add(Box.createHorizontalStrut(5));
        p.add(importSuccessLabel);
        p.add(Box.createHorizontalStrut(5));
        p.add(importLabel);
        return p;
    }

    /**
     * Formats the specified label.
     * 
     * @param label The label to format.
     */
    private void formatLabel(JLabel label)
    {
        label.setHorizontalTextPosition(SwingConstants.LEADING);
        label.setAlignmentX(SwingConstants.RIGHT);
        label.setVisible(false);
    }

    /** Initializes the components.*/
    private void initialize()
    {
        IconManager icons = IconManager.getInstance();
        importFailureLabel = new JLabel(icons.getIcon(IconManager.DELETE));
        formatLabel(importFailureLabel);
        importSuccessLabel = new JLabel(icons.getIcon(IconManager.APPLY));
        formatLabel(importSuccessLabel);
        Dimension d = new Dimension(UIUtilities.DEFAULT_ICON_WIDTH,
                UIUtilities.DEFAULT_ICON_HEIGHT);
        importLabel = new JXBusyLabel(d);
        importLabel.setToolTipText(IMPORT_TOOLTIP);
        importLabel.setVisible(false);
        importLabel.setBusy(false);
        sorter = new ViewerSorter();
        sorter.setCaseSensitive(true);
        popupMenu = new ScrollablePopupMenu();
        
        searchField = initSearchField();
    }

    /**
     * Initializes the search field 
     */
    private JTextField initSearchField() {
        final JTextField searchField = new JTextField(SEARCHFIELD_WIDTH);
        searchField.setText(SEARCHFIELD_TEXT);
        
        final Font defaultFont = searchField.getFont();
        final Font italicFont = searchField.getFont().deriveFont(Font.ITALIC);
        searchField.setFont(italicFont);
        searchField.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
        
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        search();
                }
            }
        });
        
        searchField.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().equals("")) {
                    searchField.setText(SEARCHFIELD_TEXT);
                    searchField.setFont(italicFont);
                    searchField.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
                }
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCHFIELD_TEXT)) {
                    searchField.setText("");
                }
                else {
                    searchField.selectAll();
                }
                searchField.setFont(defaultFont);
                searchField.setForeground(Color.BLACK);
            }
        });
        
        return searchField;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param view Reference to the view. Mustn't be <code>null</code>.
     */
    ToolBar(TreeViewerControl controller, TreeViewerModel model,
            TreeViewerWin view)
    {
        if (controller == null)
            throw new NullPointerException("No Controller.");
        if (model == null) 
            throw new NullPointerException("No Model.");
        if (view == null) 
            throw new NullPointerException("No View.");
        this.model = model;
        this.controller = controller;
        this.view = view;
        initialize();
        buildGUI();
    }

    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-up menu.
     * @param p The point at which to display the menu, relative to the
     *          <code>component</code>'s coordinates.
     */
    void showManagementMenu(Component c, Point p)
    {
        if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        ManagePopupMenu managePopupMenu = new ManagePopupMenu(controller);
        managePopupMenu.show(c, p.x, p.y);
    }

    /**
     * Brings up the <code>Personal Menu</code> on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-up menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showPersonalMenu(Component c, Point p)
    {
        if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        personalMenu = new JPopupMenu();
        personalMenu.setBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED));
        List<JMenuItem> l = createMenuItem(false);
        Iterator<JMenuItem> i = l.iterator();
        while (i.hasNext()) {
            personalMenu.add(i.next());
        }
        personalMenu.show(c, p.x, p.y);
    }

    /**
     * Creates the items for the menu.
     * 
     * @param add Pass <code>true</code> to build items for the <code>Add</code>
     * menu, <code>false</code> otherwise.
     * @return See above
     */
    private List<JMenuItem> createMenuItem(boolean add)
    {
        List<JMenuItem> items = new ArrayList<JMenuItem>();
        List<GroupSelectionAction> l = controller.getUserGroupAction(add);
        Iterator<GroupSelectionAction> i = l.iterator();
        GroupSelectionAction a;
        JMenuItem item;
        if (add) {
            //Check the groups that already in the view.
            Browser browser = model.getSelectedBrowser();
            List<Long> ids = new ArrayList<Long>();
            if (browser != null) {
                ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
                browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
                List<TreeImageDisplay> nodes = v.getNodes();
                Iterator<TreeImageDisplay> j = nodes.iterator();
                TreeImageDisplay node;
                while (j.hasNext()) {
                    node = j.next();
                    ids.add(((GroupData) node.getUserObject()).getId());
                }
            }

            while (i.hasNext()) {
                a = i.next();
                item = new JMenuItem(a);
                if (ids.size() > 0) {
                    item.setEnabled(!ids.contains(a.getGroupId()));
                } else item.setEnabled(true);
                initMenuItem(item);
                items.add(item);
            }
        } else {
            ButtonGroup buttonGroup = new ButtonGroup();
            long id = model.getSelectedGroupId();
            while (i.hasNext()) {
                a = i.next();
                item = new JCheckBoxMenuItem(a);
                item.setEnabled(true);
                item.setSelected(a.isSameGroup(id));
                initMenuItem(item);
                buttonGroup.add(item);
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Brings up the <code>Available Scripts</code> on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-pup menu.
     * @param p The point at which to display the menu, relative to the
     *          <code>component</code>'s coordinates.
     */
    void showAvailableScriptsMenu(Component c, Point p)
    {
        if (p == null) {
            p = new Point(0, 0);
        };
        if (c == null) {
            c = scriptButton;
            repaint();
        }
        IconManager icons = IconManager.getInstance();
        Collection<ScriptObject> scripts = model.getAvailableScripts();
        if (CollectionUtils.isEmpty(scripts)) return;
        if (scriptsMenu == null) {
            scriptsMenu = new JPopupMenu();
            JMenuItem refresh = new JMenuItem(icons.getIcon(
                    IconManager.REFRESH));
            refresh.setText("Reload Scripts");
            refresh.setToolTipText("Reloads the existing scripts.");
            refresh.addMouseListener(new MouseAdapter() {

                /**
                 * Launches the dialog when the user releases the mouse.
                 * MouseAdapter#mouseReleased(MouseEvent)
                 */
                public void mouseReleased(MouseEvent e)
                {
                    model.setAvailableScripts(null);
                    scriptsMenu = null;
                    controller.reloadAvailableScripts(e.getPoint());
                }
            });
            scriptsMenu.add(refresh);
            scriptsMenu.add(new JSeparator());

            ScriptObject so;
            Map<String, JMenu> menus = new HashMap<String, JMenu>();
            String path;

            Icon icon = icons.getIcon(IconManager.ANALYSIS);
            Icon largeIcon = icons.getIcon(IconManager.ANALYSIS_48);
            ActionListener listener = new ActionListener() {

                /** 
                 * Listens to the selection of a script.
                 * @see ActionListener#actionPerformed(ActionEvent)
                 */
                public void actionPerformed(ActionEvent e) {
                    ScriptMenuItem item = (ScriptMenuItem) e.getSource();
                    controller.handleScriptSelection(item.getScript());
                }
            };
            String name ="";
            //loop twice to check if we need to add the first element
            String refString = null;
            int count = 0;
            Iterator<ScriptObject> i = scripts.iterator();
            String sep;
            String[] values;
            String value;
            while (i.hasNext()) {
                so = i.next();
                value = "";
                path = so.getPath();
                if (path != null) {
                    sep = FilenameUtils.getPrefix(path);
                    if (path.startsWith(sep))
                        path = path.substring(1, path.length());
                    values = UIUtilities.splitString(path);
                    if (values != null && values.length > 0) value = values[0];
                }

                if (refString == null) {
                    refString = value;
                    count++;
                } else if (refString.equals(value)) count++;
            }
            int index = 0;
            if (scripts.size() == count) index++;
            i = scripts.iterator();
            List<JMenuItem> topMenus = new ArrayList<JMenuItem>();
            JMenu ref = null;
            while (i.hasNext()) {
                so = i.next();
                path = so.getPath();
                if (path != null) {
                    sep = FilenameUtils.getPrefix(path);
                    if (path.startsWith(sep))
                        path = path.substring(1, path.length());
                    values = UIUtilities.splitString(path);
                    if (values != null) {
                        for (int j = index; j < values.length; j++) {
                            value = values[j];
                            JMenu v;
                            String text = name+value;
                            if (menus.containsKey(text)) {
                                v = menus.get(text);
                            } else {
                                value = value.replace(
                                        ScriptObject.PARAMETER_SEPARATOR,
                                        ScriptObject.PARAMETER_UI_SEPARATOR);
                                v = new JMenu(value);
                            }
                            if (ref == null) topMenus.add(v);
                            else ref.add(v);
                            ref = v;
                            name+=values[j];
                            menus.put(name, v);
                        }
                    }
                }
                ScriptMenuItem item = new ScriptMenuItem(so);
                item.addActionListener(listener);
                if (ref != null) ref.add(item);
                else topMenus.add(item);
                name = "";
                ref = null;
                if (so.getIcon() == null) {
                    so.setIcon(icon);
                    so.setIconLarge(largeIcon);
                }
            }
            Iterator<JMenuItem> j = topMenus.iterator();
            while (j.hasNext()) {
                scriptsMenu.add(j.next());
            }
        }
        scriptsMenu.show(c, p.x, p.y);
    }

    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-up menu.
     * @param p The point at which to display the menu, relative to the
     *          <code>component</code>'s coordinates.
     * @param index The index of the menu.
     */
    void showCreateMenu(Component c, Point p, int index)
    {
        if (c == null) return;
        if (p == null) return;
        PopupMenu menu = new PopupMenu(controller, model, index);
        menu.show(c, p.x, p.y);
    }

    /**
     * Sets the selected flag of the {@link #fullScreen} component.
     * 
     * @param selected The value to set.
     */
    void setFullScreenSelected(boolean selected)
    { 
        fullScreen.setSelected(selected);
    }

    /** 
     * Invokes when loadings scripts.
     * 
     * @param loading Passes <code>true</code> if there is an on-going loading.
     *                <code>false</code> otherwise.
     */
    void setScriptsLoadingStatus(boolean loading)
    {
        bar.remove(index);
        busyLabel.setBusy(loading);
        if (loading) bar.add(busyLabel, index);
        else bar.add(scriptButton, index);
        validate();
        repaint();
    }

    /** Sets the permissions level.*/
    void setPermissions()
    {
        Browser browser = model.getSelectedBrowser();
        if (browser != null &&
                browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
            menuButton.setEnabled(false);
            return;
        }
        menuButton.setEnabled(true);
    }

    /** Invokes when import is going on or finished.*/
    void onImport()
    {
        //Clear first
        importFailureLabel.setText("");
        importFailureLabel.setVisible(false);
        importSuccessLabel.setText("");
        importSuccessLabel.setVisible(false);
        importLabel.setBusy(model.isImporting());
        importLabel.setVisible(model.isImporting());
        int n = model.getImportFailureCount();
        StringBuffer buffer;
        if (n > 0) {
            buffer = new StringBuffer();
            buffer.append(n);
            buffer.append(FAILED_TEXT);
            importFailureLabel.setText(buffer.toString());
            importFailureLabel.setVisible(true);
        }
        n = model.getImportSuccessCount();
        if (n > 0) {
            buffer = new StringBuffer();
            buffer.append(n);
            buffer.append(IMPORTED_TEXT);
            importSuccessLabel.setText(buffer.toString());
            importSuccessLabel.setVisible(true);
        }
        repaint();
    }

    /** Clears the menus. */
    void clearMenus()
    {
        scriptsMenu = null; //reset the menu.
    }
}
