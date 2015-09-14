/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.util.Collection;
import java.util.Iterator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.util.ui.IconManager;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;

/**
 * Renderer used to display various kind of <code>DataObject</code>s in 
 * a table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0

 * @since OME3.0
 */
public class DataObjectListCellRenderer
    extends DefaultListCellRenderer
{

    /** The foreground color when the object is a new object. */
    public static final Color NEW_FOREGROUND_COLOR = Color.red;

    /** Reference to the <code>Dataset</code> icon. */
    private static final Icon DATASET_ICON;

    /** Reference to the <code>File</code> icon. */
    private static final Icon FILE_ICON;

    /** Reference to the <code>Tag</code> icon. */
    private static final Icon TAG_ICON;

    /** Reference to the <code>Tag Set</code> icon. */
    private static final Icon TAG_SET_ICON;

    /** Reference to the <code>Owner</code> icon. */
    private static final Icon OWNER_ICON;

    /** Reference to the <code>Text File</code> icon. */
    private static final Icon FILE_TEXT_ICON;

    /** Reference to the <code>PDF File</code> icon. */
    private static final Icon FILE_PDF_ICON;

    /** Reference to the <code>HTML File</code> icon. */
    private static final Icon FILE_HTML_ICON;

    /** Reference to the <code>Power Point File</code> icon. */
    private static final Icon FILE_PPT_ICON;

    /** Reference to the <code>Word File</code> icon. */
    private static final Icon FILE_WORD_ICON;

    /** Reference to the <code>Excel File</code> icon. */
    private static final Icon FILE_EXCEL_ICON;

    /** Reference to the <code>XML File</code> icon. */
    private static final Icon FILE_XML_ICON;

    /** Reference to the <code>Editor File</code> icon. */
    private static final Icon FILE_EDITOR_ICON;

    /** Reference to the <code>Experiment</code> icon. */
    private static final Icon EDITOR_EXPERIMENT_ICON;

    /** Reference to the <code>Date</code> icon. */
    private static final Icon GROUP_ICON;

    /** Reference to the <code>Date</code> icon. */
    private static final Icon ONTOLOGY_ICON;

    /** Reference to the <code>Tag</code> icon. */
    private static final Icon TAG_OTHER_OWNER_ICON;

    /** Reference to the <code>Tag set</code> icon. */
    private static final Icon TAG_SET_OTHER_OWNER_ICON;

    /** Reference to the <code>Group Private</code> icon. */
    private static final Icon GROUP_PRIVATE_ICON;

    /** Reference to the <code>Group RWR---</code> icon. */
    private static final Icon GROUP_READ_ONLY_ICON;

    /** Reference to the <code>Group RWRA--</code> icon. */
    private static final Icon GROUP_READ_LINK_ICON;

    /** Reference to the <code>Group RWRW--</code> icon. */
    private static final Icon GROUP_READ_WRITE_ICON;

    /** Reference to the <code>Group</code> icon. */
    private static final Icon GROUP_PUBLIC_READ_ICON;

    /** Reference to the <code>Group</code> icon. */
    private static final Icon GROUP_PUBLIC_READ_WRITE_ICON;

    static { 
        IconManager icons = IconManager.getInstance();
        DATASET_ICON = icons.getIcon(IconManager.DATASET);
        FILE_ICON = icons.getIcon(IconManager.FILE);
        TAG_ICON = icons.getIcon(IconManager.TAG);
        TAG_SET_ICON = icons.getIcon(IconManager.TAG_SET);
        OWNER_ICON = icons.getIcon(IconManager.OWNER);
        FILE_TEXT_ICON = icons.getIcon(IconManager.FILE_TEXT);
        FILE_PDF_ICON = icons.getIcon(IconManager.FILE_PDF);
        FILE_HTML_ICON = icons.getIcon(IconManager.FILE_HTML);
        FILE_PPT_ICON = icons.getIcon(IconManager.FILE_PPT);
        FILE_WORD_ICON = icons.getIcon(IconManager.FILE_WORD);
        FILE_EXCEL_ICON = icons.getIcon(IconManager.FILE_EXCEL);
        FILE_XML_ICON = icons.getIcon(IconManager.FILE_XML);
        FILE_EDITOR_ICON = icons.getIcon(IconManager.FILE_EDITOR);
        EDITOR_EXPERIMENT_ICON = icons.getIcon(IconManager.EDITOR_EXPERIMENT);
        GROUP_ICON = icons.getIcon(IconManager.GROUP);
        GROUP_PRIVATE_ICON = icons.getIcon(IconManager.PRIVATE_GROUP);
        GROUP_READ_ONLY_ICON = icons.getIcon(IconManager.READ_GROUP);
        GROUP_READ_LINK_ICON = icons.getIcon(IconManager.READ_LINK_GROUP);
        GROUP_READ_WRITE_ICON = icons.getIcon(IconManager.READ_WRITE_GROUP);
        GROUP_PUBLIC_READ_ICON = icons.getIcon(IconManager.PUBLIC_GROUP);
        GROUP_PUBLIC_READ_WRITE_ICON = icons.getIcon(
                IconManager.PUBLIC_GROUP);

        ONTOLOGY_ICON = icons.getIcon(IconManager.ONTOLOGY);
        TAG_OTHER_OWNER_ICON = icons.getIcon(IconManager.TAG_OTHER_OWNER);
        TAG_SET_OTHER_OWNER_ICON =
                icons.getIcon(IconManager.TAG_SET_OTHER_OWNER);
    }

    /** The user currently logged in. */
    private ExperimenterData currentUser;

    /** The collection of immutable  nodes. */
    private Collection immutable;

    /** Reference to the model. */
    private SelectionWizardUI 	model;

    /**
     * Sets the text displayed in the tool tip.
     * 
     * @param exp The experimenter to handle.
     */
    private void createTooltip(ExperimenterData exp)
    {
        setToolTipText("Owner: "+EditorUtil.formatExperimenter(exp));
    }

    /**
     * Returns <code>true</code> if the passed element is immutable.
     * <code>false</code> otherwise.
     *
     * @param value The element to handle.
     * @return See above.
     */
    private boolean isImmutable(Object value)
    {
        if (CollectionUtils.isEmpty(immutable)) return false;
        if (!(value instanceof DataObject)) return false;
        Iterator<Object> i = immutable.iterator();
        long id = ((DataObject) value).getId();
        if (id < 0) return false;
        Object object;
        while (i.hasNext()) {
            object = i.next();
            if (object.getClass().equals(value.getClass())) {
                if (((DataObject) object).getId() == id) {
                    return !model.isAddedNode(value);
                }
            }
        }
        return false;
    }

    /**
     * Returns the text displayed when the object is a tag.
     *
     * @param tag The tag to handle.
     * @return See above.
     */
    private String getTagName(TagAnnotationData tag)
    {
        String v = model.getGroupName(tag.getGroupId());
        if (v == null) return tag.getTagValue();
        StringBuffer buffer = new StringBuffer();
        buffer.append(tag.getTagValue());
        buffer.append(" [");
        buffer.append(v);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Creates a new instance.
     *
     * @param currentUser The user currently logged in.
     * @param model Reference to the UI wizard.
     */
    DataObjectListCellRenderer(ExperimenterData currentUser,
            SelectionWizardUI model)
    {
        this.model = model;
        this.currentUser = currentUser;
     }

    /**
     * Sets the collection of nodes that cannot be removed.
     *
     * @param immutable The collection to set.
     */
    void setImmutableElements(Collection immutable)
    {
        this.immutable = immutable;
    }

    /**
     * Overridden to set the text and icon corresponding to the selected object.
     * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object,
     *                          int, boolean, boolean)
     */
    public Component getListCellRendererComponent (JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
    {
        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
        if (value instanceof TagAnnotationData) {
            TagAnnotationData tag = (TagAnnotationData) value;
            setText(getTagName(tag));
            String ns = tag.getNameSpace();
            ExperimenterData exp;
            if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                if (currentUser != null) {
                    try {
                        exp = tag.getOwner();
                        createTooltip(exp);
                        if (exp.getId() == currentUser.getId())
                            setIcon(TAG_SET_ICON);
                        else
                            setIcon(TAG_SET_OTHER_OWNER_ICON);
                    } catch (Exception e) {
                        // tag.getOwner() throws when creating a new tag which
                        // doesn't have owner information
                        if (tag.getId() < 0) createTooltip(currentUser);
                        else createTooltip(null);
                        setIcon(TAG_SET_ICON);
                    }
                } else {
                    createTooltip(null);
                    setIcon(TAG_SET_ICON);
                }
            } else {
                if (currentUser != null) {
                    try {
                        exp = tag.getOwner();
                        createTooltip(exp);
                        if (exp.getId() == currentUser.getId())
                            setIcon(TAG_ICON);
                        else
                            setIcon(TAG_OTHER_OWNER_ICON);
                    } catch (Exception e) {
                        // As above
                        if (tag.getId() < 0) createTooltip(currentUser);
                        else createTooltip(null);
                        setIcon(TAG_ICON);
                    }
                } else {
                    createTooltip(null);
                    setIcon(TAG_ICON);
                }
            }
        } else if (value instanceof FileAnnotationData) {
            FileAnnotationData fad = (FileAnnotationData) value;
            setText(fad.getFileName());
            String format = fad.getFileFormat();
            Icon icon = FILE_ICON;
            if (FileAnnotationData.PDF.equals(format))
                icon = FILE_PDF_ICON;
            else if (FileAnnotationData.TEXT.equals(format) ||
                    FileAnnotationData.CSV.equals(format))
                icon = FILE_TEXT_ICON;
            else if (FileAnnotationData.HTML.equals(format) ||
                    FileAnnotationData.HTM.equals(format))
                icon = FILE_HTML_ICON;
            else if (FileAnnotationData.MS_POWER_POINT.equals(format) ||
                    FileAnnotationData.MS_POWER_POINT_SHOW.equals(format) ||
                    FileAnnotationData.MS_POWER_POINT_X.equals(format)) 
                icon = FILE_PPT_ICON;
            else if (FileAnnotationData.MS_WORD.equals(format) ||
                    FileAnnotationData.MS_WORD_X.equals(format))
                icon = FILE_WORD_ICON;
            else if (FileAnnotationData.MS_EXCEL.equals(format))
                icon = FILE_EXCEL_ICON;
            else if (FileAnnotationData.XML.equals(format) ||
                    FileAnnotationData.RTF.equals(format)) {
                icon = FILE_XML_ICON;
            } else icon = FILE_ICON;
            setIcon(icon);
        } else if (value instanceof TermAnnotationData) {
            TermAnnotationData term = (TermAnnotationData) value;
            setText(term.getTerm());
            setIcon(ONTOLOGY_ICON);
        } else if (value instanceof DatasetData) {
            DatasetData d = (DatasetData) value;
            setText(d.getName());
            setIcon(DATASET_ICON);
        } else if (value instanceof GroupData) {
            GroupData g = (GroupData) value;
            setText(g.getName());
            Icon icon = GROUP_ICON;
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
            setIcon(icon);
        } else if (value instanceof ExperimenterData) {
            ExperimenterData exp = (ExperimenterData) value;
            setText(EditorUtil.formatExperimenter(exp));
            setIcon(OWNER_ICON);
        }
        setEnabled(!isImmutable(value));
        return this;
    }

}
