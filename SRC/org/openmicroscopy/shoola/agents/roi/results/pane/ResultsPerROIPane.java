/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.ResultsPerROIPane
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

package org.openmicroscopy.shoola.agents.roi.results.pane;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
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
public class ResultsPerROIPane
    extends JPanel
{
    
    private static final Dimension  ANNOTATION = new Dimension(200, 40);
    
    private static final Color      STEELBLUE = new Color(0x4682B4);
    
    JComboBox                       channels;
    
    private ResultsPerROIPaneMng    manager;
    
    public ResultsPerROIPane(int viewIndex, ROIResultsMng mng, 
                            String[] listChannels, String title, 
                            String annotation)
    {
        initComponents(listChannels);
        manager = new ResultsPerROIPaneMng(this, mng, viewIndex);
        buildGUI(title, annotation);
    }
    
    ResultsPerROIPaneMng getManager() { return manager; }
    
    public void addToContainer(JComponent component)
    {
        add(component);
        repaint();
    }
    
    private void initComponents(String[] listChannels)
    {
        channels = new JComboBox(listChannels);
        channels.setToolTipText(UIUtilities.formatToolTipText("Select " +
                "a channel and display the corresponding result."));
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(String title, String description)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(UIUtilities.buildComponentPanel(buildMain(title, description)));
    }
    
    private JPanel buildMain(String title, String description)
    {
        JPanel p = new JPanel(), component;
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        component = buildDescriptionPanel(title, description);
        gridbag.setConstraints(component, c);
        p.add(component);
        c.gridy = 1;
        component = buildBoxPanel();
        gridbag.setConstraints(component, c);
        p.add(component);
        return p;
    }
    
    /** Build a panel with title and ROI annotation. */
    private JPanel buildDescriptionPanel(String title, String description)
    {
        JPanel p = new JPanel();
        JLabel label = new JLabel(title);
        MultilineLabel annotation = new MultilineLabel(description);
        annotation.setEditable(false);
        annotation.setForeground(STEELBLUE);
        JScrollPane scroll = new JScrollPane(annotation);
        scroll.setPreferredSize(ANNOTATION);
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        JLabel l = new JLabel("Title ");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(label, c);
        p.add(label);
        l = new JLabel("Annotation ");
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(scroll, c);
        p.add(scroll);
        return p;
    }
    
    private JPanel buildBoxPanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel("Display results for channel ");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        JPanel component = UIUtilities.buildComponentPanel(channels);
        gridbag.setConstraints(component, c);
        p.add(component);
        return p;
    }
    
}
