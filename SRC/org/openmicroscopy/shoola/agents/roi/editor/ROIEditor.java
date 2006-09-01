/*
 * org.openmicroscopy.shoola.agents.roi.editor.ROIEditor
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

package org.openmicroscopy.shoola.agents.roi.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableColumn;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;

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
public class ROIEditor
    extends JDialog
{
    
    public static final int             NAME = 0, ANNOTATION = 1, COLOR = 2, 
                                        MAX = 2;
    
    /** Selection of line color for the ROI. */
    static final int                    RED = 0;
    static final int                    GREEN = 1;
    static final int                    BLUE = 2;
    static final int                    CYAN = 3;
    static final int                    MAGENTA = 4;
    static final int                    ORANGE = 5;
    static final int                    PINK = 6;
    static final int                    YELLOW = 7;
    
    static final int                    MAX_COLOR = 7;
    
    private static final String         NEW_MSG = "Describe the new " +
                                                "ROI.";
    
    private static final String         MSG = "Annotate ROI #";
    /** Selection of colors. */
    private static final String[]       selection;
    
    private static final Color[]        colorSelection;
    
    static {
        selection = new String[MAX_COLOR+1];
        selection[RED] = "Red";
        selection[GREEN] = "Green";
        selection[BLUE] = "Blue";
        selection[CYAN] = "Cyan";
        selection[MAGENTA] = "Magenta";
        selection[ORANGE] = "Orange";
        selection[PINK] = "Pink";
        selection[YELLOW] = "Yellow";
        
        colorSelection = new Color[MAX_COLOR+1];
        colorSelection[RED] = Color.RED;
        colorSelection[GREEN] = Color.GREEN;
        colorSelection[BLUE] = Color.BLUE;
        colorSelection[CYAN] = Color.CYAN;
        colorSelection[MAGENTA] = Color.MAGENTA;
        colorSelection[ORANGE] = Color.ORANGE;
        colorSelection[PINK] = Color.PINK;
        colorSelection[YELLOW] = Color.YELLOW;
    }

    MultilineLabel                  annotationArea;
    
    JTextField                      nameArea;  
    
    JButton                         save, cancel;
    
    JComboBox                       colors;
    
    public ROIEditor(ROIAgtCtrl control, String name, String annotation, 
                    int index)
    {
        //super(control.getReferenceFrame(), "ROI Editor", true);
        IconManager im = IconManager.getInstance(control.getRegistry());
        initComponents(annotation, name, im);
        new ROIEditorMng(this, control, index);
        buildGUI(im, index);
        setSize(ROIAgtUIF.EDITOR_WIDTH, ROIAgtUIF.EDITOR_HEIGHT);
    }
    
    Color getColorSelected(int i) { return colorSelection[i]; }  
    
    /** Initializes the components. */
    private void initComponents(String annotation, String name, IconManager im)
    {
        nameArea = new JTextField(name);
        nameArea.setForeground(ROIAgtUIF.STEELBLUE);
        nameArea.setEditable(true);
        annotationArea = new MultilineLabel(annotation);
        annotationArea.setForeground(ROIAgtUIF.STEELBLUE);
        annotationArea.setEditable(true);
        cancel = new JButton(im.getIcon(IconManager.CLOSE));
        save = new JButton(im.getIcon(IconManager.SAVE));
        cancel.setToolTipText(
                UIUtilities.formatToolTipText("Close the window."));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the annotation.")); 
        colors = new JComboBox(selection);
        colors.setToolTipText(
                UIUtilities.formatToolTipText("Pick a color for the shape."));
    }

    /** Build and lay out the GUI. */
    private void buildGUI(IconManager im, int index)
    {
        String s;
        if (index == -1) s = NEW_MSG;
        else s = MSG+index;
        TitlePanel tp = new TitlePanel("ROI Editor", s, 
                        im.getIcon(IconManager.ANNOTATE_BIG));
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildFieldsPane(), BorderLayout.CENTER);
        getContentPane().add(buildBar(), BorderLayout.SOUTH);
    }
    
    private JPanel buildFieldsPane()
    {
        JPanel p = new JPanel(), all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.add(buildTable());
        all.add(buildBoxPanel());
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(all);
        p.setOpaque(false);
        return p;
    }

    private JPanel buildBoxPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JLabel l = new JLabel("Shape color ");
        p.add(l);
        p.add(UIUtilities.buildComponentPanel(colors));
        return p;
    }
    
    /** Build toolBar with JButtons. */
    private JPanel buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(save);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(cancel);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(bar);
        return p;
    }
    
    /** 
     * A <code>2x2</code> table model to view dataset summary.
     * The first column contains the property names (name, description)
     * and the second column holds the corresponding values. 
     * <code>name</code> and <code>description</code> values
     * are marked as editable.
     */
    private TableComponent buildTable()
    {
        TableComponent table = new TableComponent(2, 2);
        setTableLayout(table);
        // Labels
        JLabel label = new JLabel(" Name");
        table.setValueAt(label, 0, 0);
        label = new JLabel(" Annotation");
        table.setValueAt(label, 1, 0);
        table.setValueAt(nameArea, 0, 1);  
        JScrollPane scrollPane  = new JScrollPane(annotationArea);
        scrollPane.setPreferredSize(ROIAgtUIF.DIM_SCROLL);
        table.setValueAt(scrollPane, 1, 1);
        return table;
    }
    
    /** Set the layout of the table. */
    private void setTableLayout(TableComponent table)
    {
        table.setTableHeader(null);
        table.setRowHeight(1, ROIAgtUIF.ROW_TABLE_HEIGHT);
        table.setRowHeight(0, ROIAgtUIF.ROW_NAME_FIELD);
        TableColumn col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(ROIAgtUIF.COLUMN_WIDTH);
        table.setDefaultRenderer(JComponent.class, 
                                new TableComponentCellRenderer());
        table.setDefaultEditor(JComponent.class, 
                                new TableComponentCellEditor());
    }

}
