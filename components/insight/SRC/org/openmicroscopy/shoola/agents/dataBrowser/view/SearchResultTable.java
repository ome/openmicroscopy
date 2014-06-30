/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.SearchSelectionEvent;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.ui.ViewObjectEvent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.SelectionHighLighter;

import edu.emory.mathcs.backport.java.util.Collections;
import pojos.DataObject;

/**
 * A table for displaying a {@link AdvancedSearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class SearchResultTable extends JXTable {

    /** Reference to the DataBrowserModel */
    private AdvancedResultSearchModel model;

    /** Holds the DataObjects shown in the table */
    private List<DataObject> data;

    /**
     * Creates a new instance
     * @param data
     * @param browserModel
     */
    public SearchResultTable(List<DataObject> data,
            AdvancedResultSearchModel browserModel) {
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

        Highlighter h = HighlighterFactory.createAlternateStriping(
                UIUtilities.BACKGROUND_COLOUR_EVEN,
                UIUtilities.BACKGROUND_COLOUR_ODD);
        addHighlighter(h);
        addHighlighter(new SelectionHighLighter((JXTable)this));

        setRowHeight(75);
        
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = getSelectedRow();
                if(row==-1) {
                    return;
                }
                row = convertRowIndexToModel(row);
                
                DataObject obj = (DataObject) getModel().getValueAt(row, SearchResultTableModel.VIEWBUTTON_COLUMN_INDEX);
                if(obj!=null) {
                    TreeViewerAgent.getRegistry().getEventBus().post(new SearchSelectionEvent(obj));
                }
            }
        });
    }

    /**
     * Reloads the table
     */
    public void refreshTable() {
        setModel(new SearchResultTableModel(data, model));
        getColumnExt(SearchResultTableModel.VIEWBUTTON_COLUMN_INDEX).setSortable(false);
    }

    /**
     * A custom renderer which shows a JLabel, Icon or Button
     * depending on the Object it gets
     */
    class MyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                final Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            JPanel p = new JPanel();
            p.setOpaque(true);

            if (value instanceof DataObject) {
                final DataObject dataObj = (DataObject) value;
                
                JButton b = new JButton("View");
                
                b.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        RequestEvent ev;
                        ev = new BrowseContainer(dataObj, null);
                        TreeViewerAgent.getRegistry().getEventBus().post(ev);
                    }
                });

                p.add(b);
            } else if (value instanceof Icon) {
                JLabel l = new JLabel((Icon) value);
                p.add(l);
            } else {
                JLabel l = new JLabel(value.toString());
                p.add(l);
            }
            return p;
        }

    }

    /**
     * The editor which shows the View button for the last column
     */
    class DataObjectEditor implements TableCellEditor {

        public DataObjectEditor() {
            setOpaque(true);
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
            p.setOpaque(true);

            final DataObject dataObj = (DataObject) arg1;
            
            JButton b = new JButton("View");
            
            b.setOpaque(true);
            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    BrowseContainer e = new BrowseContainer(dataObj, null);
                    TreeViewerAgent.getRegistry().getEventBus().post(e);
                }
            });

            p.add(b);

            return p;
        }

    }
    
}
