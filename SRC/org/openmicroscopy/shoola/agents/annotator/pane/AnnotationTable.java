/*
 * org.openmicroscopy.shoola.agents.annotator.pane.AnnotationTable
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

package org.openmicroscopy.shoola.agents.annotator.pane;

//Java imports
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl;
import org.openmicroscopy.shoola.agents.annotator.AnnotatorUIF;
import org.openmicroscopy.shoola.agents.annotator.IconManager;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;

/** 
 * 
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
class AnnotationTable
    extends TableComponent
{
    
    private static final int        ANNOTATION = 0;
    private static final int        TIME = 1;
    private static final int        OWNER = 2;
    private static final int        VIEWER = 3;
    private static final int        MAX_ID = 3;
    
    static final String[]           header, createHeader;
    
    boolean                         creation;
   
    MultilineLabel                  newAnnotation;
    
    AnnotationData                  currentData;
       
    int                             height;
    
    private AnnotationTableMng      manager;
    
    private AnnotationData[]        annotations;
    private JTextArea[]             areas;
    
    static {
        header = new String[MAX_ID+1];
        header[ANNOTATION] = "Annotation";
        header[TIME] = "Time";
        header[OWNER] = "Owner";
        header[VIEWER] = "";
        createHeader = new String[MAX_ID];
        createHeader[ANNOTATION] = "Annotation";
        createHeader[TIME] = "Time";
        createHeader[TIME+1] = "";
    }
    
    /** Create a new instance. */
    AnnotationTable(int numberRows, List rows, String[] h, 
                    AnnotatorCtrl control, boolean b)
    {
        super(numberRows, h.length);
        manager = new AnnotationTableMng(this, control);
        creation = false;
        height = AnnotatorUIF.SCROLLPANE_HEADER+
                AnnotatorUIF.ROW_TABLE_HEIGHT*numberRows;
        IconManager im = IconManager.getInstance(control.getRegistry());
        if (rows.size() > 0) initTable(rows, im, b);
        else initCreationTable(im);
        initSorter();
        setTableLayout(im, h);
    }
    
    /** Return the current annotation. */
    AnnotationData getAnnotationData() { return currentData; }
    
    AnnotationData getAnnotationData(int index) { return annotations[index]; }
    
    JTextArea getArea(int index) { return areas[index]; }
    
    /**  
     * Keep the table presentation, only use when the current user create a 
     * new annotation.
     */ 
    private void initCreationTable(IconManager im)
    {
        creation = true;
        newAnnotation = new MultilineLabel("(no annotation)");
        newAnnotation.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(newAnnotation);
        scrollPane.setPreferredSize(AnnotatorUIF.DIM_SCROLL_TABLE);
        setValueAt(scrollPane, 0, ANNOTATION);
        //To use the same format
        setValueAt(getDate(), 0, TIME);
        JButton viewer = createViewButton(im);
        setValueAt(viewer, 0, TIME+1);
        manager.attachButtonListener(viewer, 0);
    }
    
    /** Initializes the table. */
    private void initTable(List rows, IconManager im, boolean b)
    {
        creation = false;
        Iterator i = rows.iterator();
        AnnotationData data;
        int index = 0;
        annotations = new AnnotationData[rows.size()];
        areas = new JTextArea[rows.size()];
        while (i.hasNext()) {
            data = (AnnotationData) i.next();
            addRow(data, index, im, b);
            annotations[index] = data;
            index++;
        }
    }
    
    /** Create a table sorter. */
    private void initSorter()
    {
        TableSorter sorter = new TableSorter(getModel()); 
        setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(this);
        sorter.sortByColumn(TIME);     // default 
    }
    
    /** Add a row to the table. */
    private void addRow(AnnotationData data, int index, IconManager im, 
                        boolean b)
    {
        currentData = data;
        JButton viewer = createViewButton(im);
        manager.attachButtonListener(viewer, index);
        MultilineLabel annotation = new MultilineLabel(data.getAnnotation());
        annotation.setEditable(b);
        manager.attachAreaListener(annotation, index);
        areas[index] = annotation;
        JScrollPane scrollPane  = new JScrollPane(annotation);
        scrollPane.setPreferredSize(AnnotatorUIF.DIM_SCROLL_TABLE);
        setValueAt(scrollPane, index, ANNOTATION);
        setValueAt(new Date(data.getDate().getTime()), index, TIME);
        setValueAt(data.getOwnerLastName(), index, OWNER);
        setValueAt(viewer, index, VIEWER);
    }
    
    /** Set the layout of the table. */
    private void setTableLayout(IconManager im, String[] h)
    {
        getTableHeader().setReorderingAllowed(false);
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        TableColumnModel tcm = getTableHeader().getColumnModel();
        TableColumn tc;
        TableHeaderTextAndIcon txt;
        for (int i = 0; i < h.length; i++) {
            tc = tcm.getColumn(i);
            tc.setHeaderRenderer(iconHeaderRenderer);
            if (i != h.length-1) 
                txt = new TableHeaderTextAndIcon(h[i], 
                        im.getIcon(IconManager.UP), 
                        im.getIcon(IconManager.DOWN), "Order by "+h[i]+".");
            else {
                tc.setMaxWidth(AnnotatorUIF.WIDTH_MINOR);
                tc.setMinWidth(AnnotatorUIF.WIDTH_MINOR);
                txt = new TableHeaderTextAndIcon(h[i], null, null, "");
            }
                
            tc.setHeaderValue(txt);
        }
        setRowHeight(AnnotatorUIF.ROW_TABLE_HEIGHT);
        setDefaultRenderer(JComponent.class, 
                                new TableComponentCellRenderer());
        setDefaultEditor(JComponent.class, 
                                new TableComponentCellEditor());
    }
    
    private Date getDate()
    {
        java.util.Date today = new java.util.Date();
        return new Date(today.getTime());
    }
    
    private JButton createViewButton(IconManager im)
    {
        JButton view = new JButton(im.getIcon(IconManager.VIEWER));
        view.setToolTipText(
                UIUtilities.formatToolTipText("View the annotated Image."));
        return view;
    }
    
}
