/*
 * org.openmicroscopy.shoola.agents.roi.pane.ToolBar
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

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
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
public class ToolBar
    extends JPanel
{
    
    JButton                 createROI, eraseCurrentROI, assistant, viewer;
    
    JComboBox               listROI;
    
    ToolBarMng              manager;
    
    public ToolBar(ROIAgtCtrl control, int maxT, int maxZ, String[] data)
    {
        initComponents(IconManager.getInstance(control.getRegistry()), data);
        manager = new ToolBarMng(this, control, maxT, maxZ);
        buildGUI();
    }
    
    public ToolBarMng getManager() { return manager; }
    
    public int getSelectedIndex() { return listROI.getSelectedIndex(); }
    
    public int getListModelSize() 
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel) listROI.getModel();
        return model.getSize();
    }
    
    /** Add a new element to the list. */
    public void addROI5D(int index)
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel) listROI.getModel();
        String s = new String("#"+index);
        model.addElement(s);
        setControlButtonsEnabled(true);
        //remove listener otherwise an event is fired.
        setSelectedROIIndex(index);
    }
    
    /** Remove element from the list. */
    public void removeROI5D(int index)
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel) listROI.getModel();
        int j;
        String data[] = new String[model.getSize()-1];
        for (int i = 0; i < model.getSize(); i++) {
            if (i > index) {
                j = i-1;
                data[j] = new String("#"+j);
            } else if (i < index) data[i] = new String("#"+i);
        }
        boolean b = false;
        DefaultComboBoxModel newModel = new DefaultComboBoxModel(data);
        listROI.removeActionListener(manager);
        listROI.setModel(newModel);
        if (newModel.getSize() != 0) {
            b = true;
            listROI.setSelectedIndex(newModel.getSize()-1);
        }
        listROI.addActionListener(manager);
        setControlButtonsEnabled(b);
        createROI.setEnabled(true);
    }
    
    /** Remove the listener otherwise an event is fired. */  
    public void setSelectedROIIndex(int index)
    {
        listROI.removeActionListener(manager);
        listROI.setSelectedIndex(index);
        listROI.addActionListener(manager);
        manager.refreshDialogs(index);
    }
    
    /** SetEnabled the controls buttons. */
    private void setControlButtonsEnabled(boolean b)
    {
        listROI.setEnabled(b);
        assistant.setEnabled(b);
        eraseCurrentROI.setEnabled(b);
        viewer.setEnabled(b);
    }
    
    public void setButtonsEnabled(boolean b)
    {
        viewer.setEnabled(b);
        createROI.setEnabled(b);
        listROI.setEnabled(b);
        assistant.setEnabled(b);
        eraseCurrentROI.setEnabled(b);
    }
    
    private void initComponents(IconManager im, String[] data)
    {
        viewer = new JButton(im.getIcon(IconManager.VIEWER));
        viewer.setToolTipText(
        UIUtilities.formatToolTipText("Bring up the ROI viewer."));
        assistant = new JButton(im.getIcon(IconManager.ASSISTANT));
        assistant.setToolTipText(
        UIUtilities.formatToolTipText("Bring up the assistant dialog."));
        createROI = new JButton(im.getIcon(IconManager.CREATE_ROI));
        createROI.setToolTipText(
                UIUtilities.formatToolTipText("Create a 4D-selection."));
        eraseCurrentROI = new JButton(im.getIcon(IconManager.ERASE_ALL));
        eraseCurrentROI.setToolTipText(
             UIUtilities.formatToolTipText("Erase the current 4D-selection."));
        listROI = new JComboBox(data);
        listROI.setToolTipText(
           UIUtilities.formatToolTipText("Select the current 4D-selection."));
        if (data.length == 0) {
            listROI.setEnabled(false);
            assistant.setEnabled(false);
            eraseCurrentROI.setEnabled(false);
            viewer.setEnabled(false);
        }
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(buildMain());
    }
    
    private JPanel buildMain()
    {
        JPanel all = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        all.setLayout(gridbag);
        JToolBar bar = buildButtonsBar();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(bar, c);
        all.add(bar);
        JPanel p = UIUtilities.buildComponentPanel(new JLabel(" Current ROI "));
        c.gridx = 1;
        gridbag.setConstraints(p, c);
        all.add(p);
        p = UIUtilities.buildComponentPanel(listROI);
        c.gridx = 2;
        gridbag.setConstraints(p, c);
        all.add(p);
        return all;
    }
    
    /** Build a toolBar with buttons. */
    private JToolBar buildButtonsBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(createROI);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(eraseCurrentROI);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(assistant);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(viewer);
        return bar;
    } 
    
}
