/*
 * org.openmicroscopy.shoola.agents.annotator.editors.AnnotationPane
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
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl;
import org.openmicroscopy.shoola.agents.annotator.AnnotatorUIF;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class AnnotationPane
    extends JPanel
{
    
    JComboBox                   owners;
    
    AnnotationTable             table;
    
    private AnnotatorCtrl       control;
    
    private JScrollPane         scrollPane;
    
    public AnnotationPane(AnnotatorCtrl control, String name, 
                        String[] listOwners, List annotations, 
                        int selectedIndex)
    {
        this.control = control;
        initComponents(listOwners, annotations, selectedIndex);
        new AnnotationPaneMng(this, control);
        buildGUI(name);
    }
    
    public boolean isCreation() { return table.creation; }
    
    public String getAnnotation()
    {
        return table.newAnnotation.getText();
    }
    
    public AnnotationData getAnnotationData() 
    {
        return table.getAnnotationData();
    }
    
    void setTableAnnotation(List rows, boolean b)
    {
        initTable(rows, b);
        JViewport viewPort = scrollPane.getViewport();
        viewPort.removeAll();
        viewPort.add(table);
    }
    
    /** Initializes the components. */
    private void initComponents(String[] l, List rows, int selectedIndex)
    {
        owners = new JComboBox(l);
        owners.setSelectedIndex(selectedIndex);
        initTable(rows, selectedIndex == control.getUserIndex());
    }
    
    private void initTable(List rows, boolean b)
    {
        if (rows.size() > 0) 
            table = new AnnotationTable(rows.size(), rows, 
                                        AnnotationTable.header, control, b);
        else 
            table = new AnnotationTable(1, rows, AnnotationTable.createHeader, 
                                        control, b);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(String name)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(buildHeaderPanel(name));
        scrollPane = new JScrollPane(table);
        setScrollPaneSize();
        add(scrollPane);
    }
    
    private JPanel buildHeaderPanel(String name)
    {
        //TODO: ADD border
        JPanel p = UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont("Annotate: "));
        p.add(new JLabel(name));
        JPanel boxPanel = 
            UIUtilities.buildComponentPanel(new JLabel("Annotated by "));
        boxPanel.add(UIUtilities.buildComponentPanel(owners));
        JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.add(p);
        all.add(boxPanel);
        return UIUtilities.buildComponentPanel(all);
    }

    /** Set the size of the scrollPane. */
    private void setScrollPaneSize()
    {
        Dimension d = table.getPreferredScrollableViewportSize();
        int h = table.height;
        if (h > AnnotatorUIF.MAX_SCROLLPANE_HEIGHT) 
            h = AnnotatorUIF.MAX_SCROLLPANE_HEIGHT;
        scrollPane.setPreferredSize(new Dimension(d.width, h));
    }
    
}
