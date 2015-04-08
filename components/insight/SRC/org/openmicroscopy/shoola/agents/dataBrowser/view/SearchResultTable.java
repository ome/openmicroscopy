/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.ImageData;

/**
 * A table for displaying a {@link AdvancedSearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
public class SearchResultTable extends JXTable {
    
    /** Reference to the DataBrowserModel */
    private AdvancedResultSearchModel model;

    /** Holds the DataObjects shown in the table */
    private List<DataObject> data;

    /** A reference to the component holding this table */
    private SearchResultView parent;

    /**
     * Creates a new instance
     * 
     * @param data
     * @param browserModel
     */
    public SearchResultTable(SearchResultView parent, List<DataObject> data,
            AdvancedResultSearchModel browserModel) {
        this.parent = parent;
        this.data = data;
        this.model = browserModel;
        this.model.registerTable(this);
        initTable();
    }

    /**
     * Initializes the table; i. e. creates/sets the cell renderers, etc.
     */
    public void initTable() {
        TableCellRenderer defaultRenderer = new MyRenderer();
        setDefaultRenderer(DataObject.class, defaultRenderer);
        setDefaultRenderer(String.class, defaultRenderer);
        setDefaultRenderer(Icon.class, defaultRenderer);

        setDefaultEditor(DataObject.class, new DataObjectEditor());

        setBackground(UIUtilities.BACKGROUND_COLOR);

        setRowHeight(42);

        // disable column dragging:
        getTableHeader().setReorderingAllowed(false);
        
        getSelectionModel().setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        List<DataObject> selectedObjs = getSelectedObjects();

                        if (!selectedObjs.isEmpty()) {

                            if (!isSelectionValid(selectedObjs)) {
                                UserNotifier un = DataBrowserAgent
                                        .getRegistry().getUserNotifier();
                                un.notifyInfo("Invalid Selection",
                                        "A selection of items of different groups is not supported.");
                                getSelectionModel().clearSelection();
                                return;
                            }

                            parent.fireSelectionEvent(selectedObjs);
                        }
                    }

                    /**
                     * Returns <code>true</code> if all {@link DataObject}s in
                     * selectedObjs belong to the same group; <code>false</code>
                     * otherwise.
                     * 
                     * @param selectedObjs
                     * @return
                     */
                    boolean isSelectionValid(List<DataObject> selectedObjs) {
                        long groupId = -1;
                        for (DataObject obj : selectedObjs) {
                            if (groupId == -1) {
                                groupId = obj.getGroupId();
                            } else if (groupId != obj.getGroupId())
                                return false;
                        }
                        return true;
                    }
                });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                e = SwingUtilities.convertMouseEvent((Component) e.getSource(),
                        e, SearchResultTable.this);

                Point p = e.getPoint();

                if (e.getButton() == 3) {
                    parent.firePopupEvent(p);
                }
            }
        });
    }

    /**
     * Get the current selected DataObjects
     */
    public List<DataObject> getSelectedObjects() {
        List<DataObject> selectedObjs = new ArrayList<DataObject>();
        for (int row : getSelectedRows()) {
            row = convertRowIndexToModel(row);
            DataObject obj = (DataObject) getModel().getValueAt(row,
                    SearchResultTableModel.VIEWBUTTON_COLUMN_INDEX);
            if (obj != null) {
                selectedObjs.add(obj);
            }
        }
        return selectedObjs;
    }

    /**
     * Constructs a new TableModel on basis of the underlying search results
     */
    public void refreshTable() {
        setModel(new SearchResultTableModel(this, data, model));
        getColumnExt(SearchResultTableModel.VIEWBUTTON_COLUMN_INDEX)
                .setSortable(false);

        int wI = 80;
        int wB = 140;
        getColumn(0).setMinWidth(wI);
        getColumn(0).setMaxWidth(wI);
        getColumn(0).setPreferredWidth(wI);
        getColumn(0).setWidth(wI);
        getColumn(4).setMinWidth(wB);
        getColumn(4).setMaxWidth(wB);
        getColumn(4).setPreferredWidth(wB);
        getColumn(4).setWidth(wB);
    }

    private JButton createActionButton(final DataObject obj) {
        JButton button = null;

        if (obj instanceof ImageData) {
            button = new JButton("View");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageData img = (ImageData) obj;
                    RequestEvent ev = new ViewImage(new SecurityContext(obj
                            .getGroupId()), new ViewImageObject(img), null);
                    ImViewerAgent.getRegistry().getEventBus().post(ev);
                }
            });
        } else {
            // TODO: Have to remove the Browse Button for now; first it must
            // be ensured that DataObjectSelectionEvent are handled properly,
            // i. e. they also can navigate to nodes which are not yet already
            // visible/expanded.

            // button = new JButton("Browse");
            // button.addActionListener(new ActionListener() {
            //
            // @Override
            // public void actionPerformed(ActionEvent arg0) {
            // DataObjectSelectionEvent event =
            // new DataObjectSelectionEvent(obj.getClass(), obj.getId());
            // event.setSelectTab(true);
            // EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
            // bus.post(event);
            // }
            // });
        }

        return button;
    }

    /**
     * A custom renderer which shows a JLabel, Icon or Button depending on the
     * Object it gets
     */
    class MyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                final Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            JPanel p = new JPanel();

            if (isSelected)
                p.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
            else
                p.setBackground(row % 2 == 0 ? UIUtilities.BACKGROUND_COLOUR_EVEN
                        : UIUtilities.BACKGROUND_COLOUR_ODD);

            if (value==null) {
                JLabel l = new JLabel("--");
                p.add(l);
            }
            else if (value instanceof DataObject) {
                final DataObject dataObj = (DataObject) value;
                JButton b = createActionButton(dataObj);
                if (b != null)
                    p.add(b);
            } else if (value instanceof Icon) {
                JLabel l = new JLabel((Icon) value);
                p.add(l);
            } else if (value instanceof Date) {
                JLabel l = new JLabel(UIUtilities.formatDefaultDate(((Date)value)));
                p.add(l);
            }
            else {
                String s = value.toString();
                if (s.matches(".*\\<.*\\>.*")) {
                    s = "<html>" + s + "</html>";
                }
                JLabel l = new JLabel(s);
                p.add(l);

                if (column == 1)
                    p.setLayout(new FlowLayout(FlowLayout.LEFT));
            }

            return p;
        }

    }

    /**
     * The editor which shows the View button for the last column
     */
    class DataObjectEditor implements TableCellEditor {

        public DataObjectEditor() {
        }

        @Override
        public void addCellEditorListener(CellEditorListener arg0) {

        }

        @Override
        public void cancelCellEditing() {

        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(EventObject arg0) {
            return true;
        }

        @Override
        public void removeCellEditorListener(CellEditorListener arg0) {

        }

        @Override
        public boolean shouldSelectCell(EventObject arg0) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable arg0,
                final Object arg1, boolean arg2, int arg3, int arg4) {
            JPanel p = new JPanel();

            if (arg2)
                p.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
            else
                p.setBackground(arg3 % 2 == 0 ? UIUtilities.BACKGROUND_COLOUR_EVEN
                        : UIUtilities.BACKGROUND_COLOUR_ODD);

            final DataObject dataObj = (DataObject) arg1;
            JButton b = createActionButton(dataObj);
            if (b != null)
                p.add(b);

            return p;
        }

    }

}
