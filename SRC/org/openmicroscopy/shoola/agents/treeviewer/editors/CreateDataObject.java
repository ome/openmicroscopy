/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.CreateDataObject
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;




//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.util.UtilConstants;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

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
public class CreateDataObject
    extends JPanel
{

    public static final String  CANCEL_CREATION_PROPERTY = "cancelCreation";
    
    public static final String  FINISH_PROPERTY = "finish";
    
    /** The default height of the {@link TitlePanel}. */
    private static final int    TITLE_HEIGHT = 80;
    
    /** The text corresponding to the creation of a <code>Project</code>. */
    private static final String PROJECT_MSG = "Project";
    
    /** The text corresponding to the creation of a <code>Dataset</code>. */
    private static final String DATASET_MSG = "Dataset";
    
    /** 
     * The text corresponding to the creation of a
     * <code>CategoryGroup</code>.
     */
    private static final String CATEGORY_GROUP_MSG = "Category group";
    
    /** The text corresponding to the creation of a <code>Category</code>. */
    private static final String CATEGORY_MSG = "Category";
    
    /** The message displayed when the {@link #nameArea} is empty. */
    private static final String EMPTY_MSG = "The name is empty.";
    
    /** Button to create the object. */
    private JButton         createButton;
    
    /** Button to cancel the object creation. */
    private JButton         cancelButton;
    
    /** 
     * Area where to enter the name of the <code>DataObject</code> to create.
     */
    private JTextArea       nameArea;
     
    /** 
     * Area where to enter the description of the <code>DataObject</code>
     * to create.
     */
    private JTextArea       descriptionArea;
    
    /** The panel displaying the message when no name is entered. */
    private JPanel          emptyMessagePanel;
    
    /** The component hosting the title and the warning messages if required. */
    private JLayeredPane    titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
    /** The message identifying the <code>Dataobject</code> to create. */
    private String          message;
    
    /** Flag to indicate that a warning message is displayed. */
    private boolean         warning;
    
    /** The class identifying the <code>Dataobject</code> to create. */
    private Class           nodeType;
    
    /** 
     * Sets the {@link #message} corresponding to 
     * the <code>Dataobject</code> to create. 
     * 
     * @param nodeType  The specified class identifying the
     *                  <code>Dataobject</code> to create.
     */
    private void getMessage(Class nodeType)
    {
        if (nodeType.equals(ProjectData.class)) message = PROJECT_MSG;
        else if (nodeType.equals(DatasetData.class)) message = DATASET_MSG;
        else if (nodeType.equals(CategoryData.class)) message = CATEGORY_MSG;
        else if (nodeType.equals(CategoryGroupData.class)) 
            message = CATEGORY_GROUP_MSG;
    }
    
    /**
     * Sets the defaults for the specified area.
     * 
     * @param area The text area.
     */
    private void setTextAreaDefault(JTextArea area)
    {
        area.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        area.setForeground(UtilConstants.STEELBLUE);
        area.setBackground(Color.WHITE);
        area.setOpaque(true);
        area.setEditable(true);
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        titleLayer = new JLayeredPane();
        //buttons
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                firePropertyChange(CANCEL_CREATION_PROPERTY, Boolean.FALSE,
                                    Boolean.TRUE);
            }
        });
        createButton = new JButton("Finish");
        createButton.setEnabled(false);
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { create(); }
        });
        //Area.
        nameArea = new MultilineLabel();
        setTextAreaDefault(nameArea);
        nameArea.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de)
            {
                createButton.setEnabled(true);
                if (warning) {
                    titleLayer.remove(emptyMessagePanel);
                    titleLayer.repaint();
                }
                warning = false;
            }

            public void removeUpdate(DocumentEvent de)
            {
                if (de.getDocument().getLength() == 0) handleEmptyNameArea();
            }

            /** Required by I/F but no-op in our case. */
            public void changedUpdate(DocumentEvent de) {}
        });
        descriptionArea = new MultilineLabel();
        setTextAreaDefault(descriptionArea);
    }
    
    /** Resets the UI components when the {@link #nameArea} is empty. */
    private void handleEmptyNameArea()
    {
        warning = true;
        createButton.setEnabled(false);
        buildEmptyPanel();
        titleLayer.add(emptyMessagePanel, 1);
    }
    
    /** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setBorder(
                            BorderFactory.createLineBorder(Color.BLACK));
        Rectangle r = titlePanel.getBounds();
        emptyMessagePanel.setLayout(new BoxLayout(emptyMessagePanel,
                                                BoxLayout.X_AXIS));
        IconManager im = IconManager.getInstance();
        JLabel label = new JLabel(im.getIcon(IconManager.ERROR));
        emptyMessagePanel.add(label);
        int w = label.getWidth();
        label = new JLabel(EMPTY_MSG);
        int h = label.getFontMetrics(label.getFont()).getHeight();
        w += EMPTY_MSG.length()*getFontMetrics(getFont()).charWidth('m');
        emptyMessagePanel.add(label);
        Insets i = emptyMessagePanel.getInsets();
        h += i.top+i.bottom+2;
        emptyMessagePanel.setBounds(0, r.height-h, w, h);
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #createButton}.
     * 
     * @return See above;
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.add(createButton);
        bar.add(cancelButton);
        return bar;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     * 
     * @return See above.
     */
    private JPanel buildContent()
    {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        JLabel label = UIUtilities.setTextFont("Name");
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        JScrollPane pane  = new JScrollPane(nameArea);
        label.setLabelFor(pane);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(nameArea, c);
        label = UIUtilities.setTextFont("Description");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        c.gridx = 1;
        c.ipady = 60;      //make this component tall
        c.gridheight = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        label.setLabelFor(pane);
        content.add(descriptionArea, c);
        JPanel p = new JPanel(new BorderLayout());
        p.add(content, BorderLayout.NORTH);
        p.setBorder(BorderFactory.createEtchedBorder());
        return p;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        IconManager im = IconManager.getInstance();
        titlePanel = new TitlePanel(message, 
                            "Create a new "+ message.toLowerCase()+".", 
                            im.getIcon(IconManager.CREATE_BIG));
        titleLayer.add(titlePanel, 0);
        setLayout(new BorderLayout(0, 0));
        add(titleLayer, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        add(p, BorderLayout.SOUTH);
    }
    
    /** Creates a new data object. */
    private void create()
    {
        String name = nameArea.getSelectedText();
        String description = descriptionArea.getSelectedText();
        if (nodeType.equals(ProjectData.class)) {
            ProjectData object = new ProjectData();
            object.setName(name);
            object.setDescription(description);
            firePropertyChange(FINISH_PROPERTY, null, object);
        } else if (nodeType.equals(DatasetData.class)) {
            DatasetData object = new DatasetData();
            object.setName(name);
            object.setDescription(description);
            firePropertyChange(FINISH_PROPERTY, null, object);
        } else if (nodeType.equals(CategoryData.class)) {
            CategoryData object = new CategoryData();
            object.setName(name);
            object.setDescription(description);
            firePropertyChange(FINISH_PROPERTY, null, object);
        } else if (nodeType.equals(CategoryGroupData.class)) {
            CategoryGroupData object = new CategoryGroupData();
            object.setName(name);
            object.setDescription(description);
            firePropertyChange(FINISH_PROPERTY, null, object);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param nodeType The type of node to create.
     */
    public CreateDataObject(Class nodeType)
    {
        if (nodeType == null)
            throw new IllegalArgumentException("Node type not supported.");
        this.nodeType = nodeType;
        initComponents();
        getMessage(nodeType);
        buildGUI();
    }

    /**
     * Sets the size of the {@link #titlePanel} and the {@link #titleLayer}.
     * 
     * @param width The width of the components.
     */
    public void setComponentsSize(int width)
    {
        Dimension d  = new Dimension(width, TITLE_HEIGHT);
        titlePanel.setSize(d);
        titlePanel.setPreferredSize(d);
        titleLayer.setSize(d);
        titleLayer.setPreferredSize(d);
    }

}
