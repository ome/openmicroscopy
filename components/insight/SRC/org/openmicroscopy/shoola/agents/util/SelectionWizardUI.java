/*
 * org.openmicroscopy.shoola.agents.util.SelectionWizardUI 
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
package org.openmicroscopy.shoola.agents.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import info.clearthought.layout.TableLayout;
import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.TagAnnotationData;


/**
 * Provided UI to select between two lists of objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SelectionWizardUI
    extends JPanel
    implements ActionListener, DocumentListener
{

    /** Bound property indicating that the selection has changed. */
    public static final String SELECTION_CHANGE = "selectionChange";

    /**
     * Bound property indicating that a new item is selected in the
     * available items tree.
     */
    static final String AVAILABLE_SELECTION_CHANGE = "availableSelectionChange";

    /** All tags regardless of who is the owner will be displayed.*/
    static Integer ALL = 0;

    /** Tags owned by logged in user will be displayed.*/
    static int CURRENT = 1;

    /** Tags not owned by logged in user will be displayed.*/
    static int OTHERS = 2;

    /** The default text for the filter dialog.*/
    private static final String DEFAULT_FILTER_TEXT = "Filter";

    /** Action command ID to add a field to the result table. */
    private static final int ADD = 0;

    /** Action command ID to remove a field from the result table. */
    private static final int REMOVE = 1;

    /** Action command ID to add all fields to the result table. */
    private static final int ADD_ALL = 2;

    /** Action command ID to remove all fields from the result table. */
    private static final int REMOVE_ALL = 3;

    /** The original items before the user selects items. */
    private List<TreeImageDisplay> originalItems;

    /** The original selected items before the user selects items. */
    private List<TreeImageDisplay> originalSelectedItems;

    /** Collection of available items. */
    private List<TreeImageDisplay> availableItems;

    /** Collection of all the selected items. */
    private List<TreeImageDisplay> selectedItems;

    /** The list box showing the available items. */
    private JTree availableItemsListbox;

    /** The list box showing the selected items. */
    private JTree selectedItemsListbox;

    /** The button to move an item from the remaining items to current items. */
    private JButton addButton;

    /** The button to move an item from the current items to remaining items. */
    private JButton removeButton;

    /** The button to move all items to the current items. */
    private JButton addAllButton;

    /** The button to move all items to the remaining items. */
    private JButton removeAllButton;

    /** Sorts the object. */
    private ViewerSorter sorter;

    /** The type to handle. */
    private Class<?> type;

    /** The collection of immutable  nodes. */
    private Collection immutable;

    /** The group available.*/
    private Collection<GroupData> groups;

    /** Filter the data.*/
    private JTextField filterArea;

    /** Flag indicating to filter with letter anywhere in the text.*/
    private boolean filterAnywhere;

    /** The original color of a text field.*/
    private Color originalColor;

    /** The parent dialog.*/
    private JDialog view;

    /** The user currently logged in.*/
    private ExperimenterData user;

    private int ownerFilterIndex;

    /**
     * Returns <code>true</code> if the item is already selected or is
     * an item to create, <code>false</code> otherwise.
     *
     * @param elt The element to handle.
     * @return See above.
     */
    private boolean isSelected(Object elt)
    {
        if (elt instanceof TreeImageDisplay) {
            DataObject n = (DataObject) ((TreeImageDisplay) elt).getUserObject();
            for (TreeImageDisplay item : selectedItems) {
                DataObject data = (DataObject) item.getUserObject();
                if (n.getId() == data.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Filters the data according to ownership.
     *
     * @param value The value to check
     * @param txt The text of reference.
     * @param data The data object of reference.
     * @return
     */
    private boolean filterItem(String value, String txt, DataObject data)
    {
        if (!(data instanceof ExperimenterData || data instanceof GroupData)) {
            ExperimenterData exp = data.getOwner();
            if (ownerFilterIndex == CURRENT) {
                if (exp.getId() != user.getId()) return false;
            } else if (ownerFilterIndex == OTHERS) {
                if (exp.getId() == user.getId()) return false;
            }
        }
        if (filterAnywhere) {
            return value.contains(txt);
        }
        return value.startsWith(txt);
    }

    /**
     * Filters the list of displayed items.
     *
     * @param insert Pass <code>true</code> when inserting new character
     *               <code>false</code> when removing.
     */
    private void filter(boolean insert)
    {
        String txt = filterArea.getText();
        if (DEFAULT_FILTER_TEXT.equals(txt)) {
            return;
        }
        filterArea.setForeground(originalColor);
        List<TreeImageDisplay> ref;
        Iterator<TreeImageDisplay> i;
        TreeImageDisplay node, child;
        Object ho;
        String value;
        if (insert) {
            ref = availableItems;
        } else {
            ref = new ArrayList<TreeImageDisplay>();
            for (TreeImageDisplay item : originalItems) {
                if (!isSelected(item)) {
                    ref.add(item);
                }
            }
            for (TreeImageDisplay item : originalSelectedItems) {
                if (!isSelected(item) && !isChild(item)) {
                    ref.add(item);
                }
            }
        }
        i = ref.iterator();

        txt = txt.toLowerCase();
        List<TreeImageDisplay> toKeep = new ArrayList<TreeImageDisplay>();
        while (i.hasNext()) {
            node = i.next();
            ho = node.getUserObject();
            value = null;
            if (ho instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData) ho;
                if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace())) {
                    value = tag.getTagValue();
                } else {
                    List l = node.getChildrenDisplay();
                    Iterator j = l.iterator();
                    while (j.hasNext()) {
                        child = (TreeImageDisplay) j.next();
                        if (!isSelected(child)) {
                            ho = child.getUserObject();
                            if (ho instanceof TagAnnotationData) {
                                tag = (TagAnnotationData) ho;
                                value = tag.getTagValue();
                                value = value.toLowerCase();
                                if (filterItem(value, txt, (DataObject) ho)) {
                                    toKeep.add(node);
                                    break;
                                }
                            }
                        }
                    }
                    value = null;
                }
            } else if (ho instanceof FileAnnotationData) {
                value = ((FileAnnotationData) ho).getFileName();
            } else if (ho instanceof DataObject) {
                value = node.getNodeName();
            } 
            if (value != null) {
                value = value.toLowerCase();
                if (filterItem(value, txt, (DataObject) ho)) {
                    toKeep.add(node);
                }
            }
        }
        availableItems.clear();
        availableItems.addAll(toKeep);
        availableItems = sorter.sort(availableItems);
        populateTreeItems(availableItemsListbox, availableItems);
        //Select the first not
        //Get the first node.
        if (CollectionUtils.isNotEmpty(availableItems) &&
                CommonsLangUtils.isNotBlank(txt)) {
            node = availableItems.get(0);
            TreePath path = null;
            if (node.hasChildrenDisplay() && node.getChildCount() > 0) {
                child = (TreeImageDisplay) node.getFirstChild();
                path = new TreePath(child.getPath());
            } else {
                path = new TreePath(node.getPath());
            }
            if (path != null) {
                availableItemsListbox.setSelectionPath(path);
                availableItemsListbox.scrollPathToVisible(path);
                availableItemsListbox.repaint();
            }
        }
    }

    /**
     * Returns the node found if any.
     *
     * @param object The object to handle.
     * @param selected From the currently selected tag.
     * @return See above.
     */
    private TreeImageDisplay doesObjectExist(DataObject object, boolean selected)
    {
        if (object == null) return null;
        if (object instanceof TagAnnotationData) {
            Iterator<TreeImageDisplay> i;
            TagAnnotationData ob;
            String value = ((TagAnnotationData) object).getTagValue();
            if (value == null) return null;
            value = value.toLowerCase();
            String v;
            TreeImageDisplay node;
            List children;
            Iterator j;
            if (!selected) {
                i = originalItems.iterator();
                while (i.hasNext()) {
                    node = i.next();
                    ob = (TagAnnotationData) node.getUserObject();
                    if (ob != null) {
                        if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                                ob.getNameSpace())) {
                            children = node.getChildrenDisplay();
                            j = children.iterator();
                            while (j.hasNext()) {
                                node = (TreeImageDisplay) j.next();
                                if (!isSelected(node)) {
                                    ob = (TagAnnotationData) node.getUserObject();
                                    v = ob.getTagValue().toLowerCase();
                                    if (value.equals(v)) {
                                        return node;
                                    }
                                }
                            }
                        } else {
                           if (!isSelected(node)) {
                               v = ob.getTagValue().toLowerCase();
                               if (value.equals(v)) {
                                   return node;
                               } 
                            }
                        }
                    }
                }
                //not in the original selection. Might have been moved.
                i = availableItems.iterator();
                while (i.hasNext()) {
                    node = i.next();
                    ob = (TagAnnotationData) node.getUserObject();
                    if (ob != null) {
                        if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                                ob.getNameSpace())) {
                            children = node.getChildrenDisplay();
                            j = children.iterator();
                            while (j.hasNext()) {
                                node = (TreeImageDisplay) j.next();
                                if (!isSelected(node)) {
                                    ob = (TagAnnotationData) node.getUserObject();
                                    v = ob.getTagValue().toLowerCase();
                                    if (value.equals(v)) {
                                        return node;
                                    }
                                }
                            }
                        } else {
                            if (!isSelected(node)) {
                                v = ob.getTagValue().toLowerCase();
                                if (value.equals(v)) {
                                    return node;
                                } 
                            }
                        }
                    }
                }
            } else {
                i = selectedItems.iterator();
                while (i.hasNext()) {
                    node = i.next();
                    ob = (TagAnnotationData) node.getUserObject();
                    if (ob != null) {
                        v = ob.getTagValue().toLowerCase();
                        if (value.equals(v))
                            return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the default text for the specified field.
     *
     * @param text The text to display
     */
    private void setTextFieldDefault(String text)
    {
        filterArea.getDocument().removeDocumentListener(this);
        if (text == null) {
            filterArea.setText("");
            filterArea.setForeground(originalColor);
        } else {
            filterArea.setText(text);
            filterArea.setForeground(Color.LIGHT_GRAY);
        }
        filterArea.getDocument().addDocumentListener(this);
        filterArea.setSelectionStart(0);
        filterArea.setSelectionEnd(filterArea.getText().length());
    }

    /**
     * Initializes the specified tree
     * 
     * @param tree The tree to handle.
     * @param user The user currently logged in.
     */
    private void initializeTree(JTree tree, ExperimenterData user)
    {
        tree.setVisible(true);
        tree.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new TreeCellRenderer(false));
        tree.setShowsRootHandles(true);
        TreeImageSet root = new TreeImageSet("");
        tree.setModel(new DefaultTreeModel(root));
    }

    /** Initializes the components composing the display.*/
    private void initComponents()
    {
        filterAnywhere = true;
        filterArea = new JTextField();
        originalColor = filterArea.getForeground();
        setTextFieldDefault(DEFAULT_FILTER_TEXT);
        StringBuilder builder = new StringBuilder();
        builder.append("Filter");
        if (TagAnnotationData.class.equals(type)) {
            builder.append(" Tags.");
        } else if (FileAnnotationData.class.equals(type)) {
            builder.append(" Attachments.");
        } else if (DatasetData.class.equals(type)) {
            builder.append(" Datasets.");
        } else builder.append(".");
        filterArea.setToolTipText(builder.toString());
        filterArea.getDocument().addDocumentListener(this);
        filterArea.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent evt) {
                String value = filterArea.getText();
                if (CommonsLangUtils.isBlank(value)) {
                    setTextFieldDefault(DEFAULT_FILTER_TEXT);
                }
            }

            @Override
            public void focusGained(FocusEvent evt) {}
        });
        filterArea.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                String value = filterArea.getText();
                if (DEFAULT_FILTER_TEXT.equals(value)) {
                    setTextFieldDefault(null);
                }
            }
        });
        filterArea.addKeyListener(new KeyAdapter() {

            /**
             * Adds the items to the selected list.
             * @see KeyListener#keyPressed(KeyEvent)
             */
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                        filterArea.isFocusOwner()) {
                        addItem();
                        //reset filter
                        setTextFieldDefault(DEFAULT_FILTER_TEXT);
                        availableItemsListbox.requestFocus();
                }
            }
        });
        sorter = new ViewerSorter();
        availableItemsListbox = new JTree();
        initializeTree(availableItemsListbox, user);
        availableItemsListbox.addKeyListener(new KeyAdapter() {

            /**
             * Adds the items to the selected list.
             * @see KeyListener#keyPressed(KeyEvent)
             */
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                        availableItemsListbox.isFocusOwner()) {
                    addItem();
                }
            }
        });
        availableItemsListbox.addMouseListener(new MouseAdapter() {

            /**
             * Adds the items to the selected list.
             * @see MouseListener#mouseReleased(MouseEvent)
             */
            public void mouseReleased(MouseEvent e)
            {
                if (e.getClickCount() == 2) {
                    if (availableItemsListbox.isFocusOwner())
                        addItem();
                }
            }
        });
        availableItemsListbox.addTreeSelectionListener(new TreeSelectionListener() {
            
            @Override
            public void valueChanged(TreeSelectionEvent evt) {
                firePropertyChange(AVAILABLE_SELECTION_CHANGE, Boolean.TRUE,
                        Boolean.FALSE);
            }
        });
        selectedItemsListbox = new JTree();
        initializeTree(selectedItemsListbox, user);
        selectedItemsListbox.addKeyListener(new KeyAdapter() {

            /**
             * Removes the selected elements from the selected list.
             * @see KeyListener#keyPressed(KeyEvent)
             */
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                        selectedItemsListbox.isFocusOwner()) {
                    removeItem();
                }
            }
        });
        selectedItemsListbox.addMouseListener(new MouseAdapter() {

            /**
             * Removes the selected elements from the selected list.
             * @see MouseListener#mouseReleased(MouseEvent)
             */
            public void mouseReleased(MouseEvent e)
            {
                if (e.getClickCount() == 2) {
                    if (selectedItemsListbox.isFocusOwner())
                        removeItem();
                }
            }
        });
        IconManager icons = IconManager.getInstance();
        addButton = new JButton(icons.getIcon(IconManager.RIGHT_ARROW));
        removeButton = new JButton(icons.getIcon(IconManager.LEFT_ARROW));
        addAllButton = new JButton(
                icons.getIcon(IconManager.DOUBLE_RIGHT_ARROW));
        removeAllButton = new JButton(
                icons.getIcon(IconManager.DOUBLE_LEFT_ARROW));

        addButton.setActionCommand(""+ADD);
        addButton.addActionListener(this);
        addAllButton.setActionCommand(""+ADD_ALL);
        addAllButton.addActionListener(this);
        removeButton.setActionCommand(""+REMOVE);
        removeButton.addActionListener(this);
        removeAllButton.setActionCommand(""+REMOVE_ALL);
        removeAllButton.addActionListener(this);
        setImmutableElements(null);
    }

    /**
     * Formats the tooltip of a tag.
     *
     * @param data The annotation to handle.
     * @param parent Its parent.
     * @param exp The experimenter associated to the tag.
     * @return See
     */
    public String formatTooltip(AnnotationData data,
            List<TagAnnotationData> parents, ExperimenterData exp)
    {
        if (data == null) return "";
        StringBuilder buf = new StringBuilder();
        buf.append("<html><body>");
        String txt = "";
        if (exp != null) {
            txt = EditorUtil.formatExperimenter(exp);
        } else {
            txt = EditorUtil.formatExperimenter(data.getOwner());
        }
        if (CommonsLangUtils.isNotBlank(txt)) {
            buf.append("<b>");
            buf.append("Owner: ");
            buf.append("</b>");
            buf.append(txt);
            buf.append("<br>");
        }
        if (data instanceof TagAnnotationData) {
            txt = ((TagAnnotationData) data).getTagDescription();
            if (CommonsLangUtils.isNotBlank(txt)) {
                buf.append("<b>");
                buf.append("Description: ");
                buf.append("</b>");
                buf.append(txt);
                buf.append("<br>");
            }
        }
        if (CollectionUtils.isNotEmpty(parents)) {
            buf.append("<b>");
            buf.append("Tag Set: ");
            buf.append("</b>");
            for (TagAnnotationData item : parents) {
                buf.append(item.getTagValue());
                buf.append(" ");
            }
            buf.append("<br>");
        }
        buf.append("</body></html>");
        return buf.toString();
    }

    /**
     * Returns the list of tag sets the tag is linked to.
     *
     * @param tag The tag to handle.
     * @return See above
     */
    private List<TagAnnotationData> getParents(TagAnnotationData tag)
    {
        List<TagAnnotationData> parents = new ArrayList<TagAnnotationData>();
        for (TreeImageDisplay item : originalItems) {
            TagAnnotationData t = (TagAnnotationData) item.getUserObject();
            if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(t.getNameSpace())) {
                Set<TagAnnotationData> tags = t.getTags();
                if (CollectionUtils.isEmpty(tags))
                    continue;
                for (TagAnnotationData n : tags) {
                    if (n.getId() == tag.getId()) {
                        parents.add(t);
                    }
                }
            }
        }
        return parents;
    }

    /** Creates a copy of the original selections. */
    private void createOriginalSelections()
    {
        originalItems = new ArrayList<TreeImageDisplay>();
        TagAnnotationData tag;
        Object ho;
        if (availableItems != null) {
            for (TreeImageDisplay item : availableItems) {
                originalItems.add(item);
                ho = item.getUserObject();
                if (ho instanceof TagAnnotationData) {
                    tag = (TagAnnotationData) ho;
                    item.setToolTip(formatTooltip(tag, null, null));
                    List<TreeImageDisplay> l = item.getChildrenDisplay();
                    List<TagAnnotationData> p = Arrays.asList(tag);
                    for (TreeImageDisplay j : l) {
                        j.setToolTip(formatTooltip(tag, p, null));
                    }
                } else if (ho instanceof AnnotationData) {
                    item.setToolTip(formatTooltip((AnnotationData) ho, null,
                            null));
                }
            }
        }

        originalSelectedItems  = new ArrayList<TreeImageDisplay>();
        if (selectedItems != null) {
            for (TreeImageDisplay item : selectedItems) {
                //format tooltip of annotation.
                originalSelectedItems.add(item);
                ho = item.getUserObject();
                if (ho instanceof TagAnnotationData) {
                    tag = (TagAnnotationData) item.getUserObject();
                    item.setToolTip(formatTooltip(tag, getParents(tag), null));
                } else if (ho instanceof AnnotationData) {
                    item.setToolTip(formatTooltip((AnnotationData) ho, null,
                            null));
                }
            }
        }
    }

    /** Adds all the items to the selection. */
    private void addAllItems()
    {
        TreeImageDisplay child;
        List<TreeImageDisplay> toKeep = new ArrayList<TreeImageDisplay>();
        for (TreeImageDisplay node: availableItems) {
            if (node.hasChildrenDisplay()) { //tagset with tag.
                toKeep.add(node);
                List l = node.getChildrenDisplay();
                Iterator j = l.iterator();
                while (j.hasNext()) {
                    child = (TreeImageDisplay) j.next();
                    if (!isSelected(child)) {
                        selectedItems.add(child);
                    }
                }
            } else {
                Object ho = node.getUserObject();
                if (ho instanceof TagAnnotationData) {
                    TagAnnotationData tag = (TagAnnotationData) ho;
                    if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace())) {
                        if (!isSelected(node)) {
                            selectedItems.add(node);
                        }
                    } else toKeep.add(node);
                } else {
                    if (!isSelected(node)) {
                        selectedItems.add(node);
                    }
                }
            }
        }
        availableItems.retainAll(toKeep);
        sortLists();
        populateTreeItems(availableItemsListbox, availableItems);
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
    }

    /**
     * Returns <code>true</code> if the node cannot remove,
     * <code>false</code> otherwise.
     *
     * @param data The element to handle.
     * @return See above.
     */
    private boolean isImmutable(DataObject data)
    {
        Iterator i = immutable.iterator();
        DataObject o;
        while (i.hasNext()) {
            o = (DataObject) i.next();
            if (data.getId() == o.getId()) return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the node is the child of an available node,
     * <code>false</code> otherwise.
     * 
     * @param node The node to handle.
     * @return See above.
     */
    private boolean isChild(TreeImageDisplay node)
    {
        if (node == null) return false;
        Object uo = node.getUserObject();
        if (!(uo instanceof DataObject)) return false;
        DataObject ref = (DataObject) uo;
        Iterator<TreeImageDisplay> i = originalItems.iterator();
        TreeImageDisplay n;
        Object data;

        while (i.hasNext()) {
            n = i.next();
            data = n.getUserObject();
            if (data instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData) data;
                if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace())) {
                    Set<TagAnnotationData> children = tag.getTags();
                    if (CollectionUtils.isEmpty(children))
                        continue;
                    Iterator<TagAnnotationData> j = children.iterator();
                    DataObject o;
                    while (j.hasNext()) {
                        o = j.next();
                        if (o.getId() == ref.getId())
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /** Removes an item from the selection. */
    private void removeItem()
    {
        TreePath[] paths = selectedItemsListbox.getSelectionPaths();
        if (paths == null || paths.length == 0) return;
        Object c;
        TreeImageDisplay node;
        Object ho;
        DataObject data;
        List<TreeImageDisplay> toRemove = new ArrayList<TreeImageDisplay>();
        for (int i = 0; i < paths.length; i++) {
            c = paths[i].getLastPathComponent();
            if (c instanceof TreeImageDisplay) {
                node = (TreeImageDisplay) c;
                ho = node.getUserObject();
                if (ho instanceof DataObject) {
                    data = (DataObject) ho;
                    if (!isImmutable(data)) {
                        if (data.getId() >= 0 && !isChild(node)) {
                            availableItems.add(node);
                        }
                        toRemove.add(node);
                    }
                } else {
                    toRemove.add(node);
                }
            }
        }
        selectedItems.removeAll(toRemove);
        sortLists();
        populateTreeItems(availableItemsListbox, availableItems);
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
    }

    /** Removes all items from the selection. */
    private void removeAllItems()
    {
        List<TreeImageDisplay> toKeep = new ArrayList<TreeImageDisplay>();
        Object ho;
        DataObject data;
        for (TreeImageDisplay node: selectedItems) {
            ho = node.getUserObject();
            if (ho instanceof DataObject) {
                data = (DataObject) ho;
                if (isImmutable(data)) {
                    toKeep.add(node);
                } else {
                    if (data.getId() >= 0 && !isChild(node)) {
                        availableItems.add(node);
                    }
                }
            }
        }
        selectedItems.retainAll(toKeep);
        sortLists();
        populateTreeItems(availableItemsListbox, availableItems);
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
    }

    /** Adds an item to the list and then sorts the list to maintain order.*/
    private void addItem()
    {
        TreePath[] paths = availableItemsListbox.getSelectionPaths();
        if (paths == null || paths.length == 0) return;
        Object c;
        TreeImageDisplay node, child;
        //List
        List<TreeImageDisplay> toRemove = new ArrayList<TreeImageDisplay>();
        for (int i = 0; i < paths.length; i++) {
            c = paths[i].getLastPathComponent();
            if (c instanceof TreeImageDisplay) {
                node = (TreeImageDisplay) c;
                if (node.hasChildrenDisplay()) { //tagset
                    List l = node.getChildrenDisplay();
                    Iterator j = l.iterator();
                    while (j.hasNext()) {
                        child = (TreeImageDisplay) j.next();
                        if (!isSelected(child)) {
                            selectedItems.add(child);
                        }
                    }
                } else {
                    if (!isSelected(node)) {
                        toRemove.add(node);
                        selectedItems.add(node);
                    }
                }
            }
        }
        availableItems.removeAll(toRemove);
        sortLists();
        populateTreeItems(availableItemsListbox, availableItems);
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
    }

    /** Notifies that the selection has changed. */
    private void onSelectionChange()
    {
        boolean b = false;
        if (originalSelectedItems.size() != selectedItems.size()) {
            b = true;
        } else {
            int n = 0;
            Iterator<TreeImageDisplay> i = selectedItems.iterator();
            DataObject ref, original;
            Iterator<TreeImageDisplay> j;
            while (i.hasNext()) {
                ref = (DataObject) i.next().getUserObject();
                j = originalSelectedItems.iterator();
                while (j.hasNext()) {
                    original = (DataObject) j.next().getUserObject();
                    if (original.getId() == ref.getId()) {
                        n++;
                        break;
                    }
                }
            }
            b = (n != originalSelectedItems.size());
        }
        firePropertyChange(SELECTION_CHANGE, Boolean.valueOf(!b),
                Boolean.valueOf(b));
    }

    /**
     * Returns <true> if the node should be filtered, <code>false</code>
     * otherwise.
     *
     * @param child The node to handle.
     * @return See above.
     */
    private boolean isFiltered(TreeImageDisplay child)
    {
        if (child == null) return false;
        String txt = filterArea.getText();
        if (CommonsLangUtils.isBlank(txt) || DEFAULT_FILTER_TEXT.equals(txt))
            return false;
        txt = txt.toLowerCase();
        Object ho = child.getUserObject();
        String value;
        if (ho instanceof TagAnnotationData) {
            TagAnnotationData tag = (TagAnnotationData) ho;
            value = tag.getTagValue();
            value = value.toLowerCase();
            if (filterAnywhere) {
                return !value.contains(txt);
            }
            return !value.startsWith(txt);
        }
        return true;
    }

    /**
     * Updates the specified tree.
     *
     * @param tree The tree to update.
     * @param nodes The collection of nodes to handle.
     */
    private void populateTreeItems(JTree tree, List<TreeImageDisplay> nodes)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        TreeImageDisplay parent = (TreeImageDisplay) dtm.getRoot();
        parent.removeAllChildrenDisplay();
        parent.removeAllChildren();
        Iterator<TreeImageDisplay> i = nodes.iterator();
        TreeImageDisplay node, child;
        Iterator<TreeImageDisplay> j;
        Set<TreeImageDisplay> toExpand = new HashSet<TreeImageDisplay>();
        while (i.hasNext()) {
            node = i.next();
            node.setDisplayItems(false);
            Object ho = node.getUserObject();
            if (ho instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData) ho;
                if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace()) ||
                        node.hasChildrenDisplay()) {
                    dtm.insertNodeInto(node, parent, parent.getChildCount());
                }
            } else {
                dtm.insertNodeInto(node, parent, parent.getChildCount());
            }
            if (node.hasChildrenDisplay()) {
                node.removeAllChildren();
                tree.expandPath(new TreePath(node.getPath()));
                Collection<TreeImageDisplay> l = node.getChildrenDisplay();
                l = sorter.sort(l);
                j = l.iterator();
                while (j.hasNext()) {
                    child = j.next();
                    child.setDisplayItems(false);
                    if (!isSelected(child) && !isFiltered(child)) {
                        dtm.insertNodeInto(child, node, node.getChildCount());
                        toExpand.add(node);
                        tree.expandPath(new TreePath(node.getPath()));
                    }
                }
            }
        }
        dtm.reload();
        i = toExpand.iterator();
        while (i.hasNext()) {
            tree.expandPath(new TreePath(i.next().getPath()));
        }
    }

    /** Sorts the lists. */
    private void sortLists()
    {
        if (availableItems != null) 
            availableItems = sorter.sort(availableItems);
        if (selectedItems != null) selectedItems = sorter.sort(selectedItems);
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        double[][] size = {{TableLayout.FILL, 40, TableLayout.FILL},
                {TableLayout.FILL}};
        setLayout(new TableLayout(size));
        add(createAvailableItemsPane(), "0, 0");
        add(createSelectionPane(), "1, 0, CENTER, CENTER");
        add(createSelectedItemsPane(), "2, 0");
    }

    /**
     * Builds and lays out the available tags.
     *
     * @return See above.
     */
    private JPanel createAvailableItemsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UIUtilities.setTextFont(createText("Available")));
        panel.add(filterArea);
        panel.add(Box.createVerticalStrut(2));
        p.add(panel, BorderLayout.NORTH);
        p.add(new JScrollPane(availableItemsListbox), BorderLayout.CENTER);
        populateTreeItems(availableItemsListbox, availableItems);
        return p;
    }

    /**
     * Builds and lays out the buttons used to select tags.
     *
     * @return See above.
     */
    private JPanel createSelectionPane()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(addAllButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(removeAllButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        return buttonPanel;
    }

    /**
     * Builds and lays out the list of selected tags.
     *
     * @return See above.
     */
    private JPanel createSelectedItemsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(UIUtilities.setTextFont(createText("Selected")),
                BorderLayout.NORTH);
        p.add(new JScrollPane(selectedItemsListbox), BorderLayout.CENTER);
        populateTreeItems(selectedItemsListbox, selectedItems);
        return p;
    }

    /**
     * Creates the text displayed above the selections.
     *
     * @param txt The text to display.
     * @return See above.
     */
    private String createText(String txt)
    {
        StringBuilder b = new StringBuilder();
        b.append(txt);
        if (TagAnnotationData.class.equals(type)) {
            b.append(" tags");
        } else if (FileAnnotationData.class.equals(type)) {
            b.append(" attachments");
        }
        b.append(":");
        return b.toString();
    }
    /**
     * Creates a new instance.
     *
     * @param view The parent.
     * @param available The collection of available items.
     * @param type The type of object to handle.
     * @param user The current user.
     */
    public SelectionWizardUI(JDialog view,
            Collection<Object> available, Class<?> type, ExperimenterData user)
    {
        this(view, available, null, type, user);
    }

    /**
     * Creates a new instance.
     *
     * @param available The collection of available items.
     * @param selected The collection of selected items.
     * @param type The type of object to handle.
     * @param user The current user.
     */
    public SelectionWizardUI(JDialog view, Collection<Object> available,
            Collection<Object> selected, Class<?> type, ExperimenterData user)
    {
        if (selected == null) selected = new ArrayList<Object>();
        if (available == null) available = new ArrayList<Object>();
        this.view = view;
        this.user = user;
        this.availableItems = new ArrayList<TreeImageDisplay>(
                TreeViewerTranslator.transformHierarchy(available));
        this.selectedItems = new ArrayList<TreeImageDisplay>(
                TreeViewerTranslator.transformHierarchy(selected));
        this.type = type;
        createOriginalSelections();
        initComponents();
        sortLists();
        buildGUI();
    }

    /** Resets the selection. */
    void reset()
    {
        availableItems.clear();
        selectedItems.clear();
        for (TreeImageDisplay item : originalItems)
            availableItems.add(item);
        for (TreeImageDisplay item : originalSelectedItems)
            selectedItems.add(item);
        sortLists();
        populateTreeItems(availableItemsListbox, availableItems);
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
    }

    /**
     * Adds the passed objects. Returns <code>true</code> to clear the text
     * and resets the default, <code>false</code> otherwise.
     * 
     * @param toAdd The objects to add.
     * @return See above.
     */
    boolean addObjects(List<DataObject> toAdd)
    {
        if (CollectionUtils.isEmpty(toAdd)) return true;
        Iterator<DataObject> i = toAdd.iterator();
        DataObject data;
        TreeImageDisplay node = null;
        String desc = "";
        String s = "", value;
        while (i.hasNext()) {
            data = i.next();
            node = doesObjectExist(data, true);
            if (node != null) {
                if (data instanceof TagAnnotationData) {
                    desc = ((TagAnnotationData) data).getTagDescription();
                }
                break;
            }
        }
        //now check if the node is already selected.
        if (node != null) {
            //check the description for the txt
            data = (DataObject) node.getUserObject();
            if (data instanceof TagAnnotationData) {
                value = ((TagAnnotationData) data).getTagDescription();
                if (!desc.equals(value)) {
                    s = " a different";
                }
            }
            NotificationDialog msg = new NotificationDialog(view,
                    "Add new tag", String.format(
                            "A tag with the same name and%s description " +
                            "already exists\nand is selected.", s), null);
                   UIUtilities.centerAndShow(msg);
            return false;
        }
        if (node == null) { //warning
            i = toAdd.iterator();
            while (i.hasNext()) {
                data = i.next();
                node = doesObjectExist(data, false);
                if (node != null) {
                    if (data instanceof TagAnnotationData) {
                        desc = ((TagAnnotationData) data).getTagDescription();
                    }
                    break;
                }
            }
            if (node != null) {
              //check the description for the txt
                data = (DataObject) node.getUserObject();
                if (data instanceof TagAnnotationData) {
                    value = ((TagAnnotationData) data).getTagDescription();
                    if (!desc.equals(value)) {
                        s = " a different";
                    }
                }
                MessageBox msg = new MessageBox(view, "Add new tag", 
                        String.format("A tag with the same name and" +
                                "%s description already exists.\n" +
                                "Would you like to select the existing tag?", s));
                int option = msg.centerMsgBox();
                if (option == MessageBox.YES_OPTION) {
                    availableItemsListbox.setSelectionPath(
                            new TreePath(node.getPath()));
                    addItem();
                    availableItemsListbox.requestFocus();
                    return true;
                }
                return false;
            }
        }
        //We create a new tag.
        i = toAdd.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = i.next();
            node = TreeViewerTranslator.transformDataObject(ho);
            if (ho instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData) ho;
                Set<DataObject> set = tag.getDataObjects();
                List<TagAnnotationData> p = new ArrayList<TagAnnotationData>();
                if (CollectionUtils.isNotEmpty(set)) {
                    Iterator<DataObject> j = set.iterator();
                    DataObject d;
                    while (j.hasNext()) {
                        d = j.next();
                        if (d instanceof TagAnnotationData) {
                            p.add((TagAnnotationData) d);
                        }
                    }
                }
                String txt = formatTooltip(tag, p, user);
                node.setToolTip(txt);
            }
            selectedItems.add(node);
        }
        sortLists();
        populateTreeItems(selectedItemsListbox, selectedItems);
        onSelectionChange();
        return true;
    }

    /**
     * Sets the collection of nodes that cannot be removed.
     *
     * @param immutable The collection to set.
     */
    void setImmutableElements(Collection immutable)
    {
        if (immutable == null) immutable = new ArrayList();
        this.immutable = immutable;
    }

    /**
     * Returns the collection of immutable objects.
     * 
     * @return See above.
     */
    Collection getImmutableElements() { return immutable; }

    /**
     * Returns <code>true</code> if the node has been added,
     * <code>false</code> otherwise.
     *
     * @param value The value to handle.
     * @return See above.
     */
    boolean isAddedNode(Object value)
    {
        return !originalSelectedItems.contains(value);
    }

    /**
     * Returns the name of the group corresponding to identifier.
     *
     * @param ctx The context to handle.
     * @return See above
     */
    String getGroupName(long groupId)
    {
        if (groups == null) return null;
        Iterator<GroupData> i = groups.iterator();
        GroupData g;
        while (i.hasNext()) {
            g = i.next();
            if (g.getId() == groupId)
                return g.getName();
        }
        return null;
    }

    /**
     * Sets the groups.
     *
     * @param groups The groups to set.
     */
    void setGroups(Collection<GroupData> groups)
    {
        this.groups = groups;
    }

    /**
     * Returns <code>true</code> to filter by term anywhere in the word,
     * <code>false</code> otherwise.
     * @return
     */
    boolean isFilterAnywhere() { return filterAnywhere; }

    /**
     * Sets to <code>true</code> to filter by term anywhere in the word,
     * <code>false</code> otherwise.
     *
     * @param filterAnywhere The value to set.
     */
    void setFilterAnywhere(boolean filterAnywhere)
    { 
        this.filterAnywhere = filterAnywhere;
        String text = filterArea.getText();
        if (!DEFAULT_FILTER_TEXT.equals(text)) {
            filter(false);
        }
    }

    /**
     * Sets the owner's filtering index.
     *
     * @param index The value to set.
     */
    void setOwnerIndex(int index)
    {
        ownerFilterIndex = index;
        String text = filterArea.getText();
        boolean reset = false;
        if (DEFAULT_FILTER_TEXT.equals(text)) {
            setTextFieldDefault(null);
            reset = true;
        }
        filter(false);
        if (reset) {
            setTextFieldDefault(DEFAULT_FILTER_TEXT);
        }
    }

    /**
     * Returns the collection of nodes selected in the "Available" pane.
     *
     * @return See above.
     */
    Set<DataObject> getAvailableSelectedNodes()
    {
        TreePath[] paths = availableItemsListbox.getSelectionPaths();
        if (paths == null || paths.length == 0) return null;
        Object c;
        Set<DataObject> nodes = new HashSet<DataObject>();
        TreeImageDisplay node;
        for (int i = 0; i < paths.length; i++) {
            c = paths[i].getLastPathComponent();
            if (c instanceof TreeImageDisplay) {
                node = (TreeImageDisplay) c;
                if (node.hasChildrenDisplay()) { //tagset
                    nodes.add((DataObject) node.getUserObject());
                } else {
                    if (isChild(node)) {
                        nodes.add((DataObject)
                                node.getParentDisplay().getUserObject());
                    }
                }
            }
        }
        return nodes;
    }

    /**
     * Returns the selected items, excluding the immutable node.
     *
     * @return See above.
     */
    public Collection<Object> getSelection()
    { 
        Iterator<TreeImageDisplay> i = selectedItems.iterator();
        List<Object> results = new ArrayList<Object>();
        TreeImageDisplay object;
        Object uo;
        while (i.hasNext()) {
            object = i.next();
            uo = object.getUserObject();
            if (isAddedNode(object))
                results.add(uo);
            else {
                //was there but is immutable
                if (uo instanceof DataObject) {
                    if (!isImmutable((DataObject) uo))
                        results.add(uo);
                } else results.add(uo);
            }
        }
        return results;
    }

    /**
     * Reacts to event fired by the various controls.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {
        int id = Integer.parseInt(evt.getActionCommand());
        switch (id) {
        case ADD:
            addItem();
            break;
        case ADD_ALL:
            addAllItems();
            break;
        case REMOVE:
            removeItem();
            break;
        case REMOVE_ALL:
            removeAllItems();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) { filter(false); }

    @Override
    public void insertUpdate(DocumentEvent e) { filter(true); }

    @Override
    public void changedUpdate(DocumentEvent e) {}

}
