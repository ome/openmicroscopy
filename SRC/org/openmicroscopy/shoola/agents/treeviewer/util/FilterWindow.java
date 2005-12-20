/*
 * rg.openmicroscopy.shoola.agents.treeviewer.util.FilterWindow
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

package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FilterWindow
    extends JDialog
{

    /** The <code>Dataset</code> container type. */
    public static final int         DATASET = 0;
    
    /** The <code>Category</code> container type. */
    public static final int         CATEGORY = 1;
    
    /** Text corresponding to the {@link #DATASET} type. */
    private static final String     DATASET_MSG = "datasets";
    
    /** Text corresponding to the {@link #CATEGORY} type. */
    private static final String     CATEGORY_MSG = "categories";
    
    /** 
     * The size of the invisible components used to separate widgets
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(20, 1);
    
    /** 
     * The size of the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension  V_SPACER_SIZE = new Dimension(1, 10);
    
    /** The default size of the window. */
    private static final Dimension  WINDOW_SIZE = new Dimension(300, 300);
    
    /** The width of the text label. */
    private static final int        LABEL_WIDTH = 200;
    
    /** The maximum height of the scroll pane. */
    private static final int        SCROLL_HEIGHT = 150;
    
    /** The border color. */
    private static final Color      BORDER_COLOR = Color.GRAY;
    

    /** Default background color. */
    public static final Color       BACKGROUND = Color.WHITE;
    
    /** The specified type of container. */
    private int             containerType;
    
    /** To close the window. */
    private JButton         cancelButton;
    
    /** To set the containers' selection. */ 
    private JButton         setButton;
    
    /** The panel hosting the available containers. */
    private JPanel          containersPanel;
    
    /** 
     * The list of {@link FilterCheckBox} objets hosting the 
     * {@link DataObject}s.
     */
    private List            containersList;
    
    /** The list of the {@link DataObject}'s ID. */
    private Set             selectedNodes;
    
    /** 
     * Checks if the supported type is supported.
     * 
     * @param type The type to check.
     */
    private void checkType(int type)
    {
        switch (type) {
            case DATASET:
            case CATEGORY:    
                break;
                default:
                    throw new 
                    IllegalArgumentException("Container not supported");
        }
    }
    
    /**
     * Returns the string corresponding to the {@link #containerType}.
     * 
     * @return See above.
     */
    private String getContainerString()
    {
        switch (containerType) {
            case DATASET:
                return DATASET_MSG;
            case CATEGORY:
                return CATEGORY_MSG;
        }
        return "";
    }
    
    /**
     * Initializes the UI components.
     * 
     * @param nodes The collection of {@link DataObject}s.
     */
    private void initComponents(Set nodes)
    {
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { close(); }
        });
        setButton = new JButton("Set");
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setValues(); }
        });
        containersList = new ArrayList(nodes.size());
        ViewerSorter sorter = new ViewerSorter(nodes);
        List sorterNodes = sorter.sort();
        Iterator i = sorterNodes.iterator();
        JCheckBox box;
        while (i.hasNext()) {
            box = new FilterCheckBox((DataObject) i.next());
            box.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e)
                {
                    FilterCheckBox source = 
                        (FilterCheckBox) e.getItemSelectable();
                    Integer id = source.getDataObjectID();
                    if (id == null) return;
                    if (e.getStateChange() == ItemEvent.SELECTED)
                        selectedNodes.add(id);
                    else selectedNodes.remove(id);
                }
            });
            containersList.add(box);
        }
    }
    
    /** 
     * Builds and lays out the panel hosting the list of available containers.
     * 
     * @return See above.
     */
    private JScrollPane createContainersPanel()
    {
        containersPanel = new JPanel();
        containersPanel.setLayout(new BoxLayout(containersPanel,
                                        BoxLayout.Y_AXIS));
        Iterator i = containersList.iterator();
        while (i.hasNext())
            containersPanel.add((JCheckBox) i.next());
        JPanel p = UIUtilities.buildComponentPanel(containersPanel);
        JScrollPane pane = new JScrollPane(p);
        pane.setSize(p.getPreferredSize().width, SCROLL_HEIGHT);
        pane.setBorder(new LineBorder(BORDER_COLOR));
        return pane;
    }
    
    /**
     * Builds and lays out the panel hosting the buttons.
     * 
     * @return See above.
     */
    private JPanel createButtonsPanel()
    {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        buttonsPanel.add(setButton);
        return buttonsPanel;
    }
    
    /**
     * Creates the message.
     * 
     * @return See above.
     */
    private MultilineLabel createMessageLabel()
    {
        String msg = "To limit the images retrieval to " +
                        "the selected "+getContainerString()+", " +
                        "Please select items in the following list:";
        MultilineLabel label = new MultilineLabel(msg);
        label.setSize(LABEL_WIDTH, label.getPreferredSize().height);
        return label;
    }
    
    /**
     * Builds and lays out the GUI. */
    private void buildGUI()
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 15, 10)));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JPanel p = UIUtilities.buildComponentPanel(createMessageLabel());
        p.setBackground(BACKGROUND);
        p.setBorder(new LineBorder(BORDER_COLOR));
        contentPanel.add(p);
        contentPanel.add(Box.createRigidArea(V_SPACER_SIZE));
        contentPanel.add(createContainersPanel(), BorderLayout.CENTER);
        contentPanel.add(Box.createRigidArea(V_SPACER_SIZE));
        contentPanel.add(createButtonsPanel());
        getContentPane().add(contentPanel);
    }
    
    /**
     * Binds the {@link #close() close} action to the exit event generated
     */
    private void attachListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { close(); }
        });
    }
    
    /** Closes and disposes of the window. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /** Sets the selected values and closes the window. */
    private void setValues()
    {
        setVisible(false);
        firePropertyChange(Browser.FILTER_NODES_PROPERTY, null, selectedNodes);
        dispose();
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param owner The owner of the frame.
     * @param containerType 
     */
    public FilterWindow(JFrame owner, int containerType, Set nodes)
    {
        super(owner, "Filter Images Retrieval", true);
        
        if (nodes == null) 
            throw new IllegalArgumentException("No nodes");
        checkType(containerType);
        this.containerType = containerType;
        selectedNodes = new HashSet();
        initComponents(nodes);
        buildGUI();
        attachListeners();
        setSize(WINDOW_SIZE);
    }
    
}
