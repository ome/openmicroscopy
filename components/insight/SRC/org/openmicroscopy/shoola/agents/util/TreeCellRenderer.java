/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.dnd.DnDTree;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

/**
 * Determines and sets the icon corresponding to a data object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since OME5.0
 */
public class TreeCellRenderer
    extends DefaultTreeCellRenderer
{

    /** Reference to the <code>Image</code> icon. */
    private static final Icon IMAGE_ICON;

    /** Reference to the <code>Dataset</code> icon. */
    private static final Icon DATASET_ICON;

    /** Reference to the <code>Project</code> icon. */
    private static final Icon PROJECT_ICON;

    /** Reference to the <code>Tag</code> icon. */
    private static final Icon TAG_ICON;

    /** Reference to the <code>Tag Set</code> icon. */
    private static final Icon TAG_SET_ICON;

    /** Reference to the <code>Owner</code> icon. */
    private static final Icon OWNER_ICON;

    /** Reference to the <code>Text File</code> icon. */
    private static final Icon FILE_TEXT_ICON;

    /** Reference to the <code>Group Private</code> icon. */
    private static final Icon GROUP_PRIVATE_ICON;

    /** Reference to the <code>Group RWR---</code> icon. */
    private static final Icon GROUP_READ_ONLY_ICON;

    /** Reference to the <code>Group RWRA--</code> icon. */
    private static final Icon GROUP_READ_LINK_ICON;

    /** Reference to the <code>Group RWRW--</code> icon. */
    private static final Icon GROUP_READ_WRITE_ICON;

    /** Reference to the <code>Group RWRWR-</code> icon. */
    private static final Icon GROUP_PUBLIC_READ_ICON;

    /** Reference to the <code>Group RWRWRW</code> icon. */
    private static final Icon GROUP_PUBLIC_READ_WRITE_ICON;

    static { 
        IconManager icons = IconManager.getInstance();
        GROUP_PRIVATE_ICON = icons.getIcon(IconManager.PRIVATE_GROUP);
        GROUP_READ_ONLY_ICON = icons.getIcon(IconManager.READ_GROUP);
        GROUP_READ_LINK_ICON = icons.getIcon(IconManager.READ_LINK_GROUP);
        GROUP_READ_WRITE_ICON = icons.getIcon(IconManager.READ_WRITE_GROUP);
        GROUP_PUBLIC_READ_ICON = icons.getIcon(IconManager.PUBLIC_GROUP);
        GROUP_PUBLIC_READ_WRITE_ICON = icons.getIcon(
                IconManager.PUBLIC_GROUP_READ_WRITE);
        IMAGE_ICON = icons.getIcon(IconManager.IMAGE);
        DATASET_ICON = icons.getIcon(IconManager.DATASET);
        PROJECT_ICON = icons.getIcon(IconManager.PROJECT);
        TAG_ICON = icons.getIcon(IconManager.TAG);
        TAG_SET_ICON = icons.getIcon(IconManager.TAG_SET);
        OWNER_ICON = icons.getIcon(IconManager.OWNER);
        FILE_TEXT_ICON = icons.getIcon(IconManager.FILE_TEXT);
    }

    /** The dimension of the busy label. */
    private static final Dimension SIZE = new Dimension(16, 16);

    /** Flag to indicate if the number of children is visible. */
    private boolean numberChildrenVisible;

    /** Flag indicating if the node to render is the target node.*/
    private boolean isTargetNode;

    /** Flag indicating if drag and drop is allowed.*/
    private boolean droppedAllowed;

    /** The color used when dragging.*/
    private Color draggedColor;

    /** Indicates if the node is selected or not.*/
    private boolean selected;

    /** The location of the text.*/
    private int xText;

    /**
     * Sets the icon and the text corresponding to the user's object.
     * 
     * @param node The node to handle.
     */
    private void setIcon(TreeImageDisplay node)
    {
        Object usrObject = node.getUserObject();
        Icon icon = FILE_TEXT_ICON;
        if (usrObject instanceof ProjectData) {
            icon = PROJECT_ICON;
        } else if (usrObject instanceof DatasetData) {
            icon = DATASET_ICON;
        } else if (usrObject instanceof ImageData) {
            icon = IMAGE_ICON;
        } else if (usrObject instanceof TagAnnotationData) {
            TagAnnotationData tag = (TagAnnotationData) usrObject;
            String ns = tag.getNameSpace();
            if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                icon = TAG_SET_ICON;
            } else {
                icon = TAG_ICON;
            }
        } else if (usrObject instanceof GroupData) {
            GroupData g = (GroupData) usrObject;
            switch (g.getPermissions().getPermissionsLevel()) {
            case GroupData.PERMISSIONS_PRIVATE:
                icon = GROUP_PRIVATE_ICON;
                break;
            case GroupData.PERMISSIONS_GROUP_READ:
                icon = GROUP_READ_ONLY_ICON;
                break;
            case GroupData.PERMISSIONS_GROUP_READ_LINK:
                icon = GROUP_READ_LINK_ICON;
                break;
            case GroupData.PERMISSIONS_GROUP_READ_WRITE:
                icon = GROUP_READ_WRITE_ICON;
                break;
            case GroupData.PERMISSIONS_PUBLIC_READ:
                icon = GROUP_PUBLIC_READ_ICON;
                break;
            case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
                icon = GROUP_PUBLIC_READ_WRITE_ICON;
            }
        } else if (usrObject instanceof ExperimenterData) {
            icon = OWNER_ICON;
        } 
        setIcon(icon);
    }

    /**
     * Sets the color of the selected cell depending on the darkness 
     * of the specified color.
     *
     * @param c The color of reference.
     */
    private void setTextColor(Color c)
    {
        if (c == null) return;
        // check if the passed color is dark if yes, modify the text color.
        if (UIUtilities.isDarkColor(c))
            setForeground(UIUtilities.DEFAULT_TEXT);
    }

    /**
     * Creates a new instance.
     *
     * @param b Passed <code>true</code> to show the number of children,
     *          <code>false</code> otherwise.
     */ 
    public TreeCellRenderer(boolean b)
    {
        numberChildrenVisible = b;
        selected = false;
        draggedColor = new Color(backgroundSelectionColor.getRed(),
                backgroundSelectionColor.getGreen(),
                backgroundSelectionColor.getBlue(), 100);
    }

    /**
     * Overridden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object,
     *                      boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);
        isTargetNode = false;
        droppedAllowed = true;
        selected = sel;
        if (tree instanceof DnDTree) {
            DnDTree dndTree = (DnDTree) tree;
            isTargetNode = (value == dndTree.getDropTargetNode());
            if (dndTree.getRowDropLocation() == row) {
                droppedAllowed = false;
            }
        }
        setIcon(FILE_TEXT_ICON);
        if (!(value instanceof TreeImageDisplay)) return this;
        TreeImageDisplay  node = (TreeImageDisplay) value;

        int w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        Object ho = node.getUserObject();
        if (node.getLevel() == 0) {
            if (ho instanceof ExperimenterData) setIcon(OWNER_ICON);
            if (getIcon() != null) w += getIcon().getIconWidth();
            w += getIconTextGap();
            w += fm.stringWidth(getText());
            setPreferredSize(new Dimension(w, fm.getHeight()));
            Color c = node.getHighLight();
            if (c == null) c = tree.getForeground();
            setForeground(c);
            if (!sel) setBorderSelectionColor(getBackground());
            else setTextColor(getBackgroundSelectionColor());
            return this;
        } 
        setIcon(node);

        if (numberChildrenVisible) setText(node.getNodeText());
        else setText(node.getNodeName());
        setToolTipText(node.getToolTip());
        Color c = node.getHighLight();
        if (c == null) c = tree.getForeground();
        setForeground(c);
        if (!sel) setBorderSelectionColor(getBackground());
        else setTextColor(getBackgroundSelectionColor());
        if (getIcon() != null) w += getIcon().getIconWidth();
        else w += SIZE.width;
        w += getIconTextGap();
        xText = w;
        if (ho instanceof ImageData)
            w += fm.stringWidth(node.getNodeName());
        else if (node instanceof TreeFileSet)
            w +=  fm.stringWidth(getText())+40;
        else w += fm.stringWidth(getText());

        setPreferredSize(new Dimension(w, fm.getHeight()+4));//4 b/c GTK L&F
        setEnabled(node.isSelectable());
        return this;
    }

    /**
     * Overridden to highlight the destination of the target.
     * @see #paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        if (isTargetNode) {
            if (!droppedAllowed) {
                if (selected) g.setColor(backgroundSelectionColor);
                else g.setColor(backgroundNonSelectionColor);

            } else g.setColor(draggedColor);
            g.fillRect(xText, 0, getSize().width, getSize().height);
        }
        selected = false;
        isTargetNode = false;
        droppedAllowed = false;
        super.paintComponent(g);
    }

}
