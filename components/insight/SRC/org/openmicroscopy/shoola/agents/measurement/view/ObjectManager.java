/*
 * org.openmicroscopy.shoola.agents.measurement.view.ObjectManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries
import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.calls.ROIFolderSaver.ROIFolderAction;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.FancyTextField;
import org.openmicroscopy.shoola.util.ui.ScrollablePopupMenu;
import org.openmicroscopy.shoola.util.ui.SelectableMenu;
import org.openmicroscopy.shoola.util.ui.SelectableMenuItem;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.util.Pojos;
import omero.log.LogMessage;

/**
 * UI Component managing a Region of Interest.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since OME3.0
 */
class ObjectManager extends JPanel implements TabPaneInterface {

    /**
     * List of default column names.
     */
    private static Vector<String> COLUMN_NAMES;

    static {
        COLUMN_NAMES = new Vector<String>(6);
        COLUMN_NAMES.add("ROI");
        COLUMN_NAMES.add(AnnotationDescription.ROIID_STRING);
        COLUMN_NAMES.add(AnnotationDescription.ZSECTION_STRING);
        COLUMN_NAMES.add(AnnotationDescription.TIME_STRING);
        COLUMN_NAMES.add(AnnotationDescription.SHAPE_STRING);
        COLUMN_NAMES.add(AnnotationDescription.annotationDescription
                .get(AnnotationKeys.TEXT));
        COLUMN_NAMES.add("Visible");
    }

    /**
     * List of default column sizes.
     */
    private static Map<String, Integer> COLUMN_WIDTHS;

    static {
        COLUMN_WIDTHS = new HashMap<String, Integer>();
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(0), 180);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(1), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(2), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(3), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(4), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(5), 96);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(6), 36);
    }

    /** Index to identify tab */
    private final static int INDEX = MeasurementViewerUI.MANAGER_INDEX;

    /** The name of the panel. */
    private static final String NAME = "Manager";

    private static final String DEFAULT_FILTER_TEXT = "Filter ROI Folders...";

    /** The table hosting the ROI objects. */
    private ROITable objectsTable;

    /** Reference to the Model. */
    private MeasurementViewerModel model;

    /** Reference to the View. */
    private MeasurementViewerUI view;

    /** Reference to the Model. */
    private MeasurementViewerControl control;

    /** The filter toolbar */
    private JToolBar bar;

    /** The button for the popup menu */
    private JButton filterButton;

    /** The filter by name text field */
    private JTextField filterField;

    /** Show all check box */
    private JCheckBox showAllBox;

    /** The filter popup menu */
    private JPopupMenu popupMenu;

    /** The 'Select All' popup menu item */
    private SelectableMenuItem<String> selectAll;
    
    /** References to all popup menu items */
    Map<Long, Object> popupMenuItems = new HashMap<Long, Object>();

    /**
     * The table selection listener attached to the table displaying the
     * objects.
     */
    private TreeSelectionListener treeSelectionListener;

    /** Initializes the components composing the display. */
    private void initComponents() {
        ROINode root = new ROINode("root");
        objectsTable = new ROITable(new ROITableModel(root, COLUMN_NAMES),
                COLUMN_NAMES, this);
        objectsTable.setRootVisible(false);
        treeSelectionListener = new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                TreeSelectionModel tsm = objectsTable.getTreeSelectionModel();
                if (tsm.isSelectionEmpty())
                    return;
                int[] index = tsm.getSelectionRows();
                if (index.length == 0)
                    return;
                if (index.length == 1) {
                    ROINode node = (ROINode) objectsTable
                            .getNodeAtRow(objectsTable.getSelectedRow());
                    if (node == null)
                        return;
                    Object nodeValue = node.getUserObject();
                    view.clearInspector();
                    if (nodeValue instanceof ROIShape) {
                        view.selectFigure(((ROIShape) nodeValue).getFigure());
                    }
                    if (nodeValue instanceof FolderData) {
                        // if folder is selected clear figure selection
                        view.selectFigure(null);
                    }
                    int col = objectsTable.getSelectedColumn();
                    int row = objectsTable.getSelectedRow();

                    if (row < 0 || col < 0)
                        return;
                } else {
                    ROIShape shape;
                    for (int i = 0; i < index.length; i++) {
                        shape = objectsTable.getROIShapeAtRow(index[i]);
                        if (shape != null) {
                            view.selectFigure(shape.getFigure());
                        } else {
                            // if folder is selected clear figure selection
                            ROINode node = (ROINode) objectsTable
                                    .getNodeAtRow(index[i]);
                            if (node != null) {
                                Object nodeValue = node.getUserObject();
                                if (nodeValue instanceof FolderData) {
                                    view.selectFigure(null);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };

        objectsTable.addTreeSelectionListener(treeSelectionListener);

        ColumnFactory columnFactory = new ColumnFactory() {

            public void configureColumnWidths(JXTable table,
                    TableColumnExt columnExt) {
                columnExt.setPreferredWidth(COLUMN_WIDTHS.get(columnExt
                        .getHeaderValue()));
            }
        };
        objectsTable.setHorizontalScrollEnabled(true);
        objectsTable.setColumnControlVisible(true);
        objectsTable.setColumnFactory(columnFactory);

        IconManager icons = IconManager.getInstance();
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);

        filterButton = new JButton(icons.getIcon(IconManager.FILTER_MENU));
        MouseAdapter adapter = new MouseAdapter() {

            /**
             * Shows the popup menu
             */
            public void mousePressed(MouseEvent me) {
                createFilterMenu((Component) me.getSource(), me.getPoint());
            }
        };
        filterButton.setHorizontalTextPosition(SwingConstants.LEFT);
        filterButton.setText("Display ROI Folders");
        filterButton.addMouseListener(adapter);

        filterField = new FancyTextField(DEFAULT_FILTER_TEXT, 50);
        filterField.addPropertyChangeListener(FancyTextField.EDIT_PROPERTY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        filterFolders((String) evt.getNewValue());
                    }
                });

        showAllBox = new JCheckBox("Show all ROI Folders");
        showAllBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterButton.setEnabled(!showAllBox.isSelected());
                filterField.setEnabled(!showAllBox.isSelected());
                showAll();
            }
        });

        popupMenu = new ScrollablePopupMenu();
    }

    /**
     * Creates the filter popup menu
     * 
     * @param src
     *            The component which triggers the popup menu
     * @param loc
     *            The location where to show the popup menu
     */
    private void createFilterMenu(Component src, Point loc) {
        if (!src.isEnabled())
            return;

        popupMenu.removeAll();

        popupMenuItems.clear();
        
        selectAll = new SelectableMenuItem(
                (objectsTable.getIDFilter().size() == model
                        .getFolders().size()), "Select All", true);
        selectAll.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (SelectableMenuItem.SELECTION_PROPERTY.equals(name)) {
                    handleSelectAll(selectAll.isChecked());
                }
            }
        });
        popupMenu.add(selectAll);
        popupMenu.add(new JSeparator());
        
        List<Object> topLevelMenuItems = new ArrayList<Object>();

        // As FolderData doesn't have references to children, start from the
        // leaf nodes and work the way up to the top level of the hierarchy
        for (FolderData folder : getLeafFolders(model.getFolders())) {
            final SelectableMenuItem<FolderData> item = new SelectableMenuItem<FolderData>(
                    objectsTable.getIDFilter().contains(folder.getId()),
                    folder.getName(), true);
            item.setObject(folder);
            item.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (SelectableMenuItem.SELECTION_PROPERTY.equals(name)) {
                        handleFolderSelection(item, item.isChecked());
                    }
                }
            });

            if (folder.getParentFolder() != null)
                buildMenuBranch(folder, popupMenuItems, topLevelMenuItems, item);
            else {
                topLevelMenuItems.add(item);
            }

            popupMenuItems.put(folder.getId(), item);
        }

        Collections.sort(topLevelMenuItems, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String name1 = "", name2 = "";
                if (o1 instanceof SelectableMenu)
                    name1 = ((SelectableMenu) o1).getText();
                if (o1 instanceof SelectableMenuItem)
                    name1 = ((SelectableMenuItem) o1).getText();
                if (o2 instanceof SelectableMenu)
                    name2 = ((SelectableMenu) o2).getText();
                if (o2 instanceof SelectableMenuItem)
                    name2 = ((SelectableMenuItem) o2).getText();

                return name1.compareToIgnoreCase(name2);
            }
        });

        for (Object obj : topLevelMenuItems) {
            if (obj instanceof SelectableMenu)
                popupMenu.add((SelectableMenu) obj);
            if (obj instanceof SelectableMenuItem)
                popupMenu.add((SelectableMenuItem) obj);
        }

        popupMenu.show(src, loc.x, loc.y);
    }
    
    /**
     * Recursively builds up the popup menu structure from bottom (leaves) to
     * top.
     * 
     * @param folder
     *            The current folder
     * @param menuItems
     *            A map of all menu items created so far
     * @param topLevelMenuItems
     *            A collection to gather the top level menu items (only these
     *            have to added to the popup menu)
     * @param item
     *            The current menu item
     */
    private void buildMenuBranch(FolderData folder,
            Map<Long, Object> menuItems, Collection<Object> topLevelMenuItems,
            Object item) {

        FolderData parent = folder.getParentFolder();
        SelectableMenu<FolderData> parentItem = (SelectableMenu<FolderData>) menuItems
                .get(parent.getId());

        if (parentItem != null) {
            // menu item for parent folder already exists, simply add the
            // current menu item to it
            if (item != null) {
                if (item instanceof SelectableMenu)
                    parentItem.add(
                            (SelectableMenu) item,
                            getInsertionsIndex(parentItem,
                                    (SelectableMenu) item));
                else if (item instanceof SelectableMenuItem)
                    parentItem.add(
                            (SelectableMenuItem) item,
                            getInsertionsIndex(parentItem,
                                    (SelectableMenuItem) item));
            }
        } else {
            // menu item for the parent folder doesn't exist yet, create one and
            // add the current menu item to it
            final SelectableMenu tmp = new SelectableMenu<FolderData>(
                    objectsTable.getIDFilter().contains(parent.getId()),
                    parent.getName(), true);
            tmp.setObject(parent);
            tmp.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (SelectableMenu.GROUP_SELECTION_PROPERTY.equals(name)) {
                        handleFolderSelection(tmp, tmp.isMenuSelected());
                    }
                }
            });
            parentItem = tmp;
            if (item != null) {
                if (item instanceof SelectableMenu)
                    parentItem.add(
                            (SelectableMenu) item,
                            getInsertionsIndex(parentItem,
                                    (SelectableMenu) item));
                else if (item instanceof SelectableMenuItem)
                    parentItem.add(
                            (SelectableMenuItem) item,
                            getInsertionsIndex(parentItem,
                                    (SelectableMenuItem) item));
            }
            menuItems.put(parent.getId(), parentItem);
        }

        if (parent.getParentFolder() != null)
            // the parent has a parent folder itself, continue with recursion on
            // next level
            buildMenuBranch(parent, menuItems, topLevelMenuItems, parentItem);
        else
            // if not, the parent item is a top level menu item
            topLevelMenuItems.add(parentItem);
    }

    /**
     * Determines on which position of the menu the item has to be inserted in
     * order to maintain an alphabetical order
     * 
     * @param menu
     *            The target menu
     * @param item
     *            The new menu item
     * @return See above.
     */
    private int getInsertionsIndex(SelectableMenu<FolderData> menu, Object item) {
        String name = "";
        if (item instanceof SelectableMenu)
            name = ((SelectableMenu) item).getText();
        else if (item instanceof SelectableMenuItem)
            name = ((SelectableMenuItem) item).getText();

        int i = 0;
        for (; i < menu.getMenuComponentCount(); i++) {
            Component c = menu.getMenuComponent(i);
            String childName = "";
            if (c instanceof SelectableMenu) {
                SelectableMenu<FolderData> child = (SelectableMenu<FolderData>) c;
                childName = child.getText();
            }
            if (c instanceof SelectableMenuItem) {
                SelectableMenuItem<FolderData> child = (SelectableMenuItem<FolderData>) c;
                childName = child.getText();
            }
            if (childName.compareToIgnoreCase(name) >= 0)
                break;
        }
        return i;
    }
    
    /**
     * Handles the 'Select All' action
     * 
     * @param selectAll
     *            Pass <code>true</code> to select all,
     *            <code>false<code> to deselect all
     */
    private void handleSelectAll(boolean selectAll) {
        for (Object obj : popupMenuItems.values()) {
            if (obj instanceof SelectableMenu) {
                SelectableMenu<FolderData> menu = (SelectableMenu<FolderData>) obj;
                menu.setMenuSelected(selectAll, false);
            }
            if (obj instanceof SelectableMenuItem) {
                SelectableMenuItem<FolderData> menuitem = (SelectableMenuItem<FolderData>) obj;
                menuitem.setChecked(selectAll, false);
            }
        }

        if (selectAll) {
            for (FolderData f : model.getFolders())
                objectsTable.getIDFilter().add(f.getId());
        } else {
            objectsTable.getIDFilter().clear();
        }

        rebuildTable();
    }

    /**
     * Handles check/uncheck events of the filter popup menu
     * 
     * @param obj
     *            The menu item which has been checked/unchecked
     * @param isChecked
     *            The flag if the item has been checked or unchecked
     */
    private void handleFolderSelection(Object obj, boolean isChecked) {
        FolderData folder = null;
        if (obj instanceof SelectableMenu)
            folder = ((SelectableMenu<FolderData>) obj).getObject();
        else if (obj instanceof SelectableMenuItem)
            folder = ((SelectableMenuItem<FolderData>) obj).getObject();

        // Add or remove the selected folder to the visible folder ids,
        // depending on if it has been checked or unchecked; propagate
        // check/uncheck state to sub folders
        if (isChecked) {
            objectsTable.getIDFilter().add(folder.getId());
            for (FolderData f : model.getFolders())
                if (hasAncestor(f, folder.getId()))
                    objectsTable.getIDFilter().add(f.getId());
        } else {
            objectsTable.getIDFilter().remove(folder.getId());
            for (FolderData f : model.getFolders())
                if (hasAncestor(f, folder.getId()))
                    objectsTable.getIDFilter().remove(f.getId());
        }

        // Refresh the checkbox state of all menu items
        for (Object item : popupMenuItems.values()) {
            if (item instanceof SelectableMenu) {
                SelectableMenu<FolderData> folderItem = (SelectableMenu<FolderData>) item;
                folderItem.setMenuSelected(
                        objectsTable.getIDFilter().contains(
                                folderItem.getObject().getId()), false);
            } else if (item instanceof SelectableMenuItem) {
                SelectableMenuItem<FolderData> folderItem = (SelectableMenuItem<FolderData>) item;
                folderItem.setChecked(
                        objectsTable.getIDFilter().contains(
                                folderItem.getObject().getId()), false);
            }
        }
        selectAll.setChecked(objectsTable.getIDFilter().size() == model
                .getFolders().size(), false);
        
        rebuildTable();
    }
    
    /**
     * Checks if the specified folder has an ancestor with the given id
     * 
     * @param f
     *            The Folder to check
     * @param parentId
     *            The potential parent id
     * @return See above
     */
    private boolean hasAncestor(FolderData f, long parentId) {
        if (f.getParentFolder() != null) {
            if (f.getParentFolder().getId() == parentId)
                return true;
            else
                return hasAncestor(f.getParentFolder(), parentId);
        }
        return false;
    }

    /**
     * Determines which of the given Folders are leaf folders, i.e. don't have
     * sub folders
     * 
     * @param folders
     *            The collection of Folders to check
     * @return The leaf folders
     */
    private Collection<FolderData> getLeafFolders(Collection<FolderData> folders) {
        Map<Long, FolderData> result = new HashMap<Long, FolderData>();
        // First consider all folders as leaf folders...
        for (FolderData f : folders) {
            result.put(f.getId(), f);
        }

        // ...then remove the parent folders
        for (FolderData f : folders) {
            if (f.getParentFolder() != null)
                result.remove(f.getParentFolder().getId());
        }

        return result.values();
    }

    /**
     * Propagate the filter by name text to the table and perform refresh.
     * 
     * @param text
     *            The filter by name text
     */
    private void filterFolders(String text) {
        objectsTable.setNameFilter(text);
        rebuildTable();
    }

    /**
     * Disable any filtering and show all folders
     */
    private void showAll() {
        boolean showAll = showAllBox.isSelected();
        objectsTable.setIgnoreFilters(showAll);
        rebuildTable();
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {
        setLayout(new BorderLayout());

        bar.add(filterButton);
        bar.add(filterField);
        bar.add(showAllBox);

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(objectsTable), BorderLayout.CENTER);
    }

    /**
     * Creates a new instance.
     * 
     * @param view
     *            Reference to the view. Mustn't be <code>null</code>.
     * @param controller
     *            Reference to the control. Mustn't be <code>null</code>.
     * @param model
     *            Reference to the Model. Mustn't be <code>null</code>.
     */
    ObjectManager(MeasurementViewerUI view, MeasurementViewerControl control,
            MeasurementViewerModel model) {
        if (view == null)
            throw new IllegalArgumentException("No view.");
        if (model == null)
            throw new IllegalArgumentException("No model.");
        if (control == null)
            throw new IllegalArgumentException("No control.");
        this.view = view;
        this.model = model;
        this.control = control; 
        initComponents();
        buildGUI();
    }

    /**
     * Displays the menu at the specified location if not already visible.
     * 
     * @param x
     *            The x-coordinate of the mouse click.
     * @param y
     *            The y-coordinate of the mouse click.
     */
    void showROIManagementMenu(int x, int y) {
        objectsTable.showROIManagementMenu(view.getDrawingView(), x, y);
    }

    /** Rebuilds Tree */
    void rebuildTable(Map<FolderData, Collection<ROIData>> result, ROIFolderAction action) {
        if (action == ROIFolderAction.CREATE_FOLDER || action == ROIFolderAction.ADD_TO_FOLDER && !result.isEmpty()) {
            FolderData f = (FolderData) result.keySet().iterator().next();
            objectsTable.getIDFilter().add(f.getId());
        }
        rebuildTable();
    }

    /** Rebuilds Tree */
    void rebuildTable() {    
        if (objectsTable.getIDFilter().isEmpty()
                && (selectAll == null || !selectAll.isChecked())) {
            // setup initial folder selection (only show folders
            // which contain ROIs for the current image)
            for (FolderData f : model.getUsedFolders()) {
                objectsTable.getIDFilter().add(f.getId());
            }
        }
        
        Set<Long> expandedFolderIds = objectsTable.getExpandedFolders();
        expandedFolderIds.addAll(Pojos.extractIds(objectsTable
                .getRecentlyModifiedFolders()));
        objectsTable.clear();
        objectsTable.initFolders(getFolders());
        objectsTable.addROIShapeList(extractShapes(model.getROI().values()));
        objectsTable.collapseAll();
        objectsTable.expandFolders(expandedFolderIds);
        objectsTable.setAutoCreateColumnsFromModel(false);
    }

    List<ROIShape> extractShapes(Collection<ROI> rois) {
        List<ROIShape> result = new ArrayList<ROIShape>();
        Iterator<ROI> iterator = rois.iterator();
        while (iterator.hasNext()) {
            ROI roi = iterator.next();
            TreeMap<Coord3D, ROIShape> shapeList = roi.getShapes();
            result.addAll(shapeList.values());
        }
        return result;
    }
    
    /**
     * Returns the name of the component.
     * 
     * @return See above.
     */
    String getComponentName() {
        return NAME;
    }

    /**
     * Returns the icon of the component.
     * 
     * @return See above.
     */
    Icon getComponentIcon() {
        IconManager icons = IconManager.getInstance();
        return icons.getIcon(IconManager.MANAGER);
    }

    /**
     * Adds the collection of figures to the display.
     * 
     * @param l
     *            The collection of objects to add.
     */
    void addFigures(Collection l) {
        Iterator i = l.iterator();
        ROI roi;
        Iterator<ROIShape> j;
        while (i.hasNext()) {
            roi = (ROI) i.next();
            j = roi.getShapes().values().iterator();
            while (j.hasNext())
                objectsTable.addROIShape(j.next());
        }
    }

    /**
     * Adds the collection of ROIShapes to the display.
     * 
     * @param shapeList
     *            The collection of ROIShapes to add.
     */
    void addROIShapes(List<ROIShape> shapeList) {
        objectsTable.initFolders(getFolders());
        objectsTable.addROIShapeList(shapeList);
    }

    /**
     * Selects the collection of figures.
     * 
     * @param l
     *            The collection of objects to select.
     * @param clear
     *            Pass <code>true</code> to clear the selection
     *            <code>false</code> otherwise.
     */
    void setSelectedFigures(List<ROIShape> l, boolean clear) {
        Iterator<ROIShape> i = l.iterator();
        TreeSelectionModel tsm = objectsTable.getTreeSelectionModel();
        ROIFigure figure = null;
        ROIShape shape;
        if (clear)
            tsm.clearSelection();
        objectsTable.removeTreeSelectionListener(treeSelectionListener);

        try {
            while (i.hasNext()) {
                shape = i.next();
                figure = shape.getFigure();
                objectsTable.selectROIShape(figure.getROIShape());
            }
            objectsTable.repaint();
            if (figure != null)
                objectsTable.scrollToROIShape(figure.getROIShape());
        } catch (Exception e) {
            MeasurementAgent.getRegistry().getLogger()
                    .info(this, "Figure selection " + e);
        }

        objectsTable.addTreeSelectionListener(treeSelectionListener);
    }

    /**
     * Removes the passed figure from the table.
     * 
     * @param figure
     *            The figure to remove.
     */
    void removeFigure(ROIFigure figure) {
        if (figure == null)
            return;
        objectsTable.removeROIShape(figure.getROIShape());
        objectsTable.repaint();
    }

    /**
     * Removes the passed figures from the table.
     * 
     * @param figures
     *            The figures to handle.
     */
    void removeFigures(List<ROIFigure> figures) {
        if (figures == null || figures.size() == 0)
            return;
        Iterator<ROIFigure> i = figures.iterator();
        while (i.hasNext()) {
            objectsTable.removeROIShape(i.next().getROIShape());
        }
        objectsTable.repaint();
    }

    /**
     * Deletes the ROI shapes in the list.
     * 
     * @param shapeList
     *            see above.
     */
    void deleteROIShapes(List<ROIShape> shapeList) {
        view.deleteROIShapes(shapeList);
        this.rebuildTable();
    }

    /** Resets the component. */
    void reset() {
        model.getROIComponent().reset();
    }

    /**
     * Duplicates the ROI shapes in the list and belonging to the ROI with id.
     * 
     * @param id
     *            see above.
     * @param shapeList
     *            see above.
     */
    void duplicateROI(long id, List<ROIShape> shapeList) {
        view.duplicateROI(id, shapeList);
        this.rebuildTable();
    }

    /**
     * Calculates the statistics for the roi in the list.
     * 
     * @param shapeList
     *            The collection of shapes.
     */
    void calculateStats(List<ROIShape> shapeList) {
        view.calculateStats(shapeList);
    }

    /**
     * Merges the ROI shapes in the list and belonging to the ROI with id in
     * idList into a single new ROI. The ROI in the shape list should all be on
     * separate planes.
     * 
     * @param idList
     *            see above. see above.
     * @param shapeList
     *            see above.
     */
    void mergeROI(List<Long> idList, List<ROIShape> shapeList) {
        view.mergeROI(idList, shapeList);
        this.rebuildTable();
    }

    /**
     * Split the ROI shapes in the list and belonging to the ROI with id into a
     * single new ROI. The ROI in the shape list should all be on separate
     * planes.
     * 
     * @param id
     *            see above. see above.
     * @param shapeList
     *            see above.
     */
    void splitROI(long id, List<ROIShape> shapeList) {
        view.splitROI(id, shapeList);
        this.rebuildTable();
    }

    /** Repaints the table. */
    void update() {
        objectsTable.refresh();
        objectsTable.invalidate();
        objectsTable.repaint();
    }

    /**
     * Shows the roi assistant for the roi.
     * 
     * @param roi
     *            see above.
     */
    void propagateROI(ROI roi) {
        view.showROIAssistant(roi);
    }

    /**
     * Display message in status bar.
     * 
     * @param messageString
     *            see above.
     */
    void showMessage(String messageString) {
        view.setStatus(messageString);
    }

    /**
     * Display Ready message in status bar.
     */
    void showReadyMessage() {
        view.setReadyStatus();
    }

    /** Invokes when new figures are selected. */
    void onSelectedFigures() {
        Collection<Object> tmp = new ArrayList<Object>();
        tmp.addAll(model.getSelectedFigures());
        objectsTable.onSelection(tmp);
    }

    /**
     * Returns the selected figures.
     * 
     * @return See above.
     */
    Collection<Figure> getSelectedFigures() {
        return model.getSelectedFigures();
    }

    /**
     * Implemented as specified by the I/F {@link TabPaneInterface}
     * 
     * @see TabPaneInterface#getIndex()
     */
    public int getIndex() {
        return INDEX;
    }

    /** Loads the tags. */
    void loadTags() {
        control.loadTags();
    }

    /**
     * Returns a collection of all figures selected If a ROI is selected, all
     * the figures hosted will that ROI will be returned.
     * 
     * @return See above.
     */
    Collection<Figure> getSelectedFiguresFromTables() {
        Collection<Object> l = objectsTable.getSelectedObjects();
        if (CollectionUtils.isEmpty(l))
            return null;
        Iterator i = l.iterator();
        Object o;
        ROI roi;
        ROIShape shape;
        List<Figure> list = new ArrayList<Figure>();
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof ROI) {
                roi = (ROI) o;
                list.addAll(roi.getAllFigures());
            } else if (o instanceof ROIShape) {
                shape = (ROIShape) o;
                list.add(shape.getFigure());
            }
        }
        return list;
    }

    /**
     * Get the state of the {@link MeasurementViewer}
     * 
     * @return See above.
     */
    public int getState() {
        return model.getState();
    }
    
    /**
     * Add ROIs to Folders
     * 
     * @param selectedObjects
     *            The ROIs
     * @param folders
     *            The Folders
     */
    public void addRoisToFolder(Collection<ROIShape> selectedObjects,
            Collection<FolderData> folders) {
        List<ROIData> allRois = model.getROIData();
        Map<Long, ROIData> selectedRois = new HashMap<Long, ROIData>();
        for (ROIShape shape : selectedObjects) {
            if (!selectedRois.containsKey(shape.getID())) {
                ROIData rd = findROI(allRois, shape.getROI());
                if (rd != null)
                    selectedRois.put(shape.getID(), rd);
            }
        }
        model.addROIsToFolder(allRois, selectedRois.values(), folders);
    }

    /**
     * Move ROIs to Folders
     * 
     * @param selectedObjects
     *            The ROIs
     * @param folders
     *            The Folders
     */
    public void moveROIsToFolder(Collection<ROIShape> selectedObjects,
            Collection<FolderData> folders) {
        List<ROIData> allRois = model.getROIData();
        Map<Long, ROIData> selectedRois = new HashMap<Long, ROIData>();
        for (ROIShape shape : selectedObjects) {
            if (!selectedRois.containsKey(shape.getID())) {
                ROIData rd = findROI(allRois, shape.getROI());
                if (rd != null)
                    selectedRois.put(shape.getID(), rd);
            }
        }
        model.moveROIsToFolder(allRois, selectedRois.values(), folders);
    }

    /**
     * Find an {@link ROIData} within a collection by it's {@link ROI} uuid
     * 
     * @param coll
     *            The collection of {@link ROIData}
     * @param roi
     *            The {@link ROI} to search for
     * @return See above.
     */
    private ROIData findROI(Collection<ROIData> coll, ROI roi) {
        Iterator<ROIData> it = coll.iterator();
        while (it.hasNext()) {
            ROIData next = it.next();
            if (next.getUuid().equals(roi.getUUID()))
                return next;
        }
        return null;
    }

    /**
     * Delete Folders
     * 
     * @param folders
     *            The Folders
     */
    public void deleteFolders(Collection<FolderData> folders) {
        saveROIs();
        model.deleteFolders(folders);
    }

    /**
     * Removes ROIs from Folders
     * 
     * @param selectedObjects
     *            The ROIs
     * @param folders
     *            The Folders
     */
    public void removeRoisFromFolder(Collection<ROIShape> selectedObjects,
            Collection<FolderData> folders) {
        saveROIs();
        Map<Long, ROIData> rois = new HashMap<Long, ROIData>();
        for (ROIShape shape : selectedObjects) {
            ROI roi = shape.getROI();
            if (roi.isClientSide())
                continue;
            if (!rois.containsKey(roi.getID())) {
                ROIData data = new ROIData();
                data.setId(roi.getID());
                data.setImage(model.getImage().asImage());
                rois.put(roi.getID(), data);
            }
        }
        model.removeROIsFromFolder(rois.values(), folders);
    }

    public void saveROIFolders(Collection<FolderData> folders) {
        saveROIs();
        model.saveROIFolders(folders);
    }

    /**
     * Checks if the current image is editable by the user
     * 
     * @return See above.
     */
    public boolean canEdit() {
        if (model.getImage() == null)
            return false;
        else
            return model.getImage().canEdit();
    }

    /**
     * Get all available folders
     * 
     * @return See above
     */
    Collection<FolderData> getFolders() {
        return model.getFolders();
    }

    /**
     * Save ROIs if there are unsaved ROIs
     * 
     * @return The saved ROIs
     */
    private Collection<ROIData> saveROIs() {
        if (model.hasROIToSave()) {
            Registry reg = MeasurementAgent.getRegistry();
            List<ROIData> roiList = model.getROIData();
            ExperimenterData exp = (ExperimenterData) MeasurementAgent
                    .getUserDetails();
            OmeroImageService svc = reg.getImageService();
            try {
                return svc.saveROI(model.getSecurityContext(),
                        model.getImageID(), exp.getId(), roiList);

            } catch (Exception e) {
                reg.getUserNotifier().notifyWarning("Could not save ROIs",
                        "Failed to save some unsaved ROIs");
                reg.getLogger().warn(this,
                        new LogMessage("Could not save ROIs", e));
            }
        }
        return Collections.EMPTY_LIST;
    }
}
