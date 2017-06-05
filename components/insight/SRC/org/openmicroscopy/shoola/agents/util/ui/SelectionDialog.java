/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITable 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import omero.gateway.model.DataObject;
import omero.gateway.model.FolderData;

import org.apache.commons.lang.StringUtils;

/**
 * Simple dialog for selecting a {@link DataObject}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class SelectionDialog extends JDialog implements ActionListener {

    /** Property indicating an object has been selected */
    public static String OBJECT_SELECTION_PROPERTY = "OBJECT_SELECTION_PROPERTY";

    /** Property indicating the --None-- property has been selected */
    public static String NONE_SELECTION_PROPERTY = "NONE_SELECTION_PROPERTY";

    /** The available objects */
    private Collection<DataObject> selection;

    /** Optional description text */
    private String text;

    /** Ok button */
    private JButton okButton;

    /** Cancel button */
    private JButton cancelButton;

    /** The list */
    private JList list;

    /**
     * Flag to indicate if a --None-- item should be added to the available
     * items
     */
    private boolean addNoneItem = false;

    /**
     * Creates a new instance
     * 
     * @param selection
     *            The available items
     * @param title
     *            The title of the dialog
     * @param text
     *            An optional description text
     */
    public SelectionDialog(Collection<DataObject> selection, String title,
            String text) {
        this(null, false, selection, title, text, false);
    }

    /**
     * Creates a new instance
     * 
     * @param selection
     *            The available items
     * @param title
     *            The title of the dialog
     * @param text
     *            An optional description text
     * @param addNoneItem
     *            Flag to indicate if a --None-- item should be added to the
     *            available items
     */
    public SelectionDialog(Collection<DataObject> selection, String title,
            String text, boolean addNoneItem) {
        this(null, false, selection, title, text, addNoneItem);
    }

    /**
     * 
     * @param owner
     *            The owner
     * @param selection
     *            The available items
     * @param title
     *            The title of the dialog
     * @param text
     *            An optional description text
     * @param addNoneItem
     *            Flag to indicate if a --None-- item should be added to the
     *            available items
     */
    public SelectionDialog(Frame owner, Collection<DataObject> selection,
            String title, String text, boolean addNoneItem) {
        this(owner, false, selection, title, text, addNoneItem);
    }

    /**
     * 
     * @param owner
     *            The owner
     * @param modal
     *            Flag if the dialog should be modal
     * @param selection
     *            The available items
     * @param title
     *            The title of the dialog
     * @param text
     *            An optional description text
     * @param addNoneItem
     *            Flag to indicate if a --None-- item should be added to the
     *            available items
     */
    public SelectionDialog(Frame owner, boolean modal,
            Collection<DataObject> selection, String title, String text,
            boolean addNoneItem) {
        super(owner, modal);
        setTitle(title);
        this.selection = selection;
        this.text = text;
        this.addNoneItem = addNoneItem;
        initUI();
    }

    void initUI() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(3, 3));

        if (StringUtils.isNotEmpty(text)) {
            p.add(new JLabel(text), BorderLayout.NORTH);
        }

        DataObjectWrapper[] items = null;
        int i = -1;
        if (addNoneItem) {
            items = new DataObjectWrapper[selection.size() + 1];
            items[0] = new DataObjectWrapper();
            i = 1;
        } else {
            items = new DataObjectWrapper[selection.size()];
            i = 0;
        }
        
        Iterator<DataObject> it = selection.iterator();
        while (it.hasNext()) {
            items[i++] = new DataObjectWrapper(it.next());
        }
        Arrays.sort(items);

        list = new JList(items);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        p.add(buttonPanel, BorderLayout.SOUTH);

        p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        getContentPane().add(p);

        pack();
        setSize(300, 400);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            DataObjectWrapper obj = (DataObjectWrapper) list.getSelectedValue();
            if (obj != null) {
                if (obj.object == null) {
                    firePropertyChange(NONE_SELECTION_PROPERTY, true, false);
                } else
                    firePropertyChange(OBJECT_SELECTION_PROPERTY, null,
                            obj.object);
            }
        }
        setVisible(false);
    }

    class DataObjectWrapper implements Comparable<DataObjectWrapper> {
        DataObject object;

        DataObjectWrapper() {
        }

        DataObjectWrapper(DataObject object) {
            this.object = object;
        }

        @Override
        public String toString() {
            if (object == null) {
                return "--NONE--";
            }

            if (object instanceof FolderData) {
                return ((FolderData) object).getFolderPathString();
            }

            return object.toString();
        }

        @Override
        public int compareTo(DataObjectWrapper o) {
            if (object == null)
                return -1;

            if (o.object == null)
                return 1;

            if (object instanceof FolderData) {
                return ((FolderData) object)
                        .getFolderPathString()
                        .toLowerCase()
                        .compareTo(
                                ((FolderData) ((DataObjectWrapper) o).object)
                                        .getFolderPathString().toLowerCase());
            }
            return 0;
        }
    }
}
