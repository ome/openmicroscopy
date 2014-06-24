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
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.IconCellRenderer;

import edu.emory.mathcs.backport.java.util.Collections;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

public class SearchResultTable extends JTable {

    SearchResultView view;

    DataBrowserModel model;
    
    List<DataObject> data;

    public SearchResultTable(List<DataObject> data) {
        this.data = data;
        initTable();
    }

    public void initTable() {
        TableCellRenderer defaultRenderer = new MyRenderer();
        setDefaultRenderer(DataObject.class, defaultRenderer);
        setDefaultRenderer(String.class, defaultRenderer);
        setDefaultRenderer(Icon.class, new IconCellRenderer());
        
        setDefaultEditor(DataObject.class, new DataObjectEditor());

        setBackground(UIUtilities.BACKGROUND_COLOR);

        setRowHeight(50);
    }

    public void refreshTable() {
        setModel(new SearchResultTableModel(data));
    }

    public void setHighlightedNodes(List<ImageDisplay> nodes) {

    }

    public void setSelectedNodes(List<DataObject> objects) {

    }

    class MyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                final Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            
            Color bg = (row%2==0) ? UIUtilities.BACKGROUND_COLOUR_EVEN : UIUtilities.BACKGROUND_COLOUR_ODD;
            
            JPanel p = new JPanel();
            p.setBackground(bg);
            
            if(value instanceof DataObject) {
                JButton b = new JButton("View");
                b.addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        BrowseContainer e = new BrowseContainer(value, null);
                        TreeViewerAgent.getRegistry().getEventBus().post(e);
                    }
                });
                
                p.add(b);
            }
            else if (value instanceof Icon) {
                JLabel l = new JLabel((Icon)value);
                l.setBackground(bg);
                p.add(l);
            }
            else {
                JLabel l = new JLabel(value.toString());
                l.setBackground(bg);
                p.add(l);
            }
            return p;
        }

    }

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
        public Component getTableCellEditorComponent(JTable arg0, final Object arg1,
                boolean arg2, int arg3, int arg4) {
            JPanel p = new JPanel();
            p.setBackground(((arg3%2==0) ? UIUtilities.BACKGROUND_COLOUR_EVEN : UIUtilities.BACKGROUND_COLOUR_ODD));
            
            JButton b = new JButton("View");
            b.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    BrowseContainer e = new BrowseContainer(arg1, null);
                    TreeViewerAgent.getRegistry().getEventBus().post(e);
                }
            });
            
            p.add(b);
            
            return p;
        }

    }
    
    protected static ExperimenterData getUserDetails()
    {
        return (ExperimenterData) TreeViewerAgent.getRegistry().lookup(
                                        LookupNames.CURRENT_USER_DETAILS);
    }
}
