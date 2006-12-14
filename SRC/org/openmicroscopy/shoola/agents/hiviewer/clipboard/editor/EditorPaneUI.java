/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPaneUI
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

/** 
 * The UI delegate for the {@link EditorPane}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class EditorPaneUI
    extends JPanel
{
    
    /**
     * The message displayed when the name of the <code>DataObject</code> is 
     * null or of length 0.
     */
    private static final String     EMPTY_MSG = "The name cannot be empty.";
    
    /** The title of the <code>Property</code> tabbed pane. */
    private static final String		PROPERTY = "Properties";
    
    /** The title of the <code>Permissions</code> tabbed pane. */
    private static final String		PERMISSIONS = "Permissions";
    
    /** Reference to the Model. */
    private EditorPane          model;

    /** Button to finish the operation e.g. create, edit, etc. */
    private JButton             finishButton;
    
    /** The label displaying the edit message. */
    private JLabel              titleLabel;
    
    /** The panel hosting the text area. */
    //private JPanel              bodyPanel;
    
    /** The panel hosting the text area. */
    private JPanel              permissionPanel;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener    nameAreaListener;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener    descriptionAreaListener;
    
    /** Indicates that a warning message is displayed if <code>true</code>. */
    private boolean         	warning;
    
    /** Flag indicating if the name/description fields are modified.*/
    private boolean         	edit;
      
    /** The UI component hosting the properties. */
    private JTabbedPane 		tabs;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField      	nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea       	descriptionArea;
    
    /** The default font of the title. */
    private Font            	titleLabelDefaultfont;
    
    /** The title message. */
    private String          	message;
    
    /** The name to be displayed. */
    private String				originalName;
    
    /**
     * Enables the {@link #finishButton} and removes the warning message
     * when the name of the <code>DataObject</code> is valid.
     * Sets the {@link #edit} flag to <code>true</code>.
     */
    private void handleNameAreaInsert()
    {
        finishButton.setEnabled(true);
        edit = true;
        if (warning) {
            titleLabel.setFont(titleLabelDefaultfont);
            titleLabel.setText(message);
            titleLabel.repaint();
        }
        warning = false;
    }
    
    /**
     * Displays an error message when the length of the inserted name is
     * <code>0</code>.
     * 
     * @param length The length of the 
     */
    private void handleNameAreaRemove(int length)
    {
        if (length == 0) {
            warning = true;
            message = titleLabel.getText();
            finishButton.setEnabled(false);
            titleLabel.setFont(titleLabelDefaultfont.deriveFont(Font.BOLD));
            titleLabel.setText(EMPTY_MSG);
            titleLabel.repaint();
        } else finishButton.setEnabled(true);
    }
    
    /**
     * Enables the {@link #finishButton} and sets the {@link #edit} flag
     * to <code>true</code>.
     */
    private void handleDescriptionAreaInsert()
    {
        finishButton.setEnabled(true);
        edit = true;
    }

    /**
     * Sets the defaults for the specified area.
     * 
     * @param area The text area.
     */
    private void setTextAreaDefault(JTextComponent area)
    {
        area.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        area.setForeground(UIUtilities.STEELBLUE);
        area.setBackground(Color.WHITE);
        area.setOpaque(true);
        area.setEditable(true);
        area.setEnabled(false);
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        titleLabel = new JLabel(getMessage(null));
        titleLabelDefaultfont = titleLabel.getFont();
        //TitleBar
        finishButton = new JButton("Save");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  model.finish(); }
        });
        
        nameArea = new JTextField();
        
        setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        setTextAreaDefault(descriptionArea);
        nameAreaListener = new DocumentListener() {
            
            /** 
             * Updates the editor's controls when some text is inserted. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
                handleNameAreaInsert();
            }
            
            /** 
             * Displays an error message when the data object has no name. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                handleNameAreaRemove(de.getDocument().getLength());
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        nameArea.getDocument().addDocumentListener(nameAreaListener);
        descriptionAreaListener = new DocumentListener() {

            /** Handles text insertion. */
            public void insertUpdate(DocumentEvent de)
            {
                handleDescriptionAreaInsert();
            }
            
            /** Handles text insertion. */
            public void removeUpdate(DocumentEvent de)
            {
                handleDescriptionAreaInsert();
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        descriptionArea.getDocument().addDocumentListener(
                descriptionAreaListener);
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * twe display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
    	JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, 5, 0, 100} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        content.add(UIUtilities.setTextFont("Name"), "0, 0, l, c");
        content.add(nameArea, "1, 0, f, c");
        content.add(new JLabel(), "0, 1, 1, 1");
        JLabel l = UIUtilities.setTextFont("Description");
        layout.setRow(2, l.getFontMetrics(l.getFont()).getHeight()+5);
        content.add(l, "0, 2, l, c");
        JScrollPane pane  = new JScrollPane(descriptionArea);
        content.add(pane, "1, 2, 1, 3");
        return content;
    }
    
    /**
     * Builds and lays out a panel hosting the permission.
     * 
     * @return See above.
     */
    private JPanel builPermissionPanel()
    {
        permissionPanel = new JPanel(); 
        permissionPanel.setLayout(new BorderLayout());
        return permissionPanel;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        setOpaque(true);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(titleLabel);
        p.add(finishButton);
        add(UIUtilities.buildComponentPanel(p),  BorderLayout.NORTH);
        tabs = new JTabbedPane();
        tabs.addTab(PROPERTY, null, buildContentPanel());
        tabs.addTab(PERMISSIONS, null, builPermissionPanel());
        add(tabs, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * 
     */
    EditorPaneUI(EditorPane model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }

    /** 
     * Returns the message corresponding to currently edited 
     * <code>Dataobject</code>. 
     * 
     * @param ho  The data object to edit.
     * @return See above.
     */
    String getMessage(DataObject ho)
    {
        if (ho instanceof ProjectData)
            return "Edit project: "+((ProjectData) ho).getName();
        else if (ho instanceof DatasetData)
                return "Edit dataset: "+((DatasetData) ho).getName();
        else if (ho instanceof CategoryGroupData)
            return "Edit category group: "+((CategoryGroupData) ho).getName();
        else if (ho instanceof CategoryData)
            return "Edit category: "+((CategoryData) ho).getName();
        else if (ho instanceof ImageData)
            return "Edit image: "+((ImageData) ho).getName();
        else return "No object to edit";
    }
    
    /**
     * Sets the name and description of the edited object.
     * Sets the title corresponding to the edited object.
     * 
     * @param name          The name of the data object.
     * @param description   The description of the data object.
     * @param title         The title displayed.
     * @param isWritable    Pass <code>true</code> if the current user is 
     *                      allowed to modify the name and description, 
     *                      <code>false</code> otherwise.             
     */
    void setAreas(String name, String description, String title, 
                boolean isWritable)
    {
    	originalName = name;
        nameArea.setEnabled(isWritable);
        nameArea.getDocument().removeDocumentListener(nameAreaListener);
        nameArea.setText(name);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
        descriptionArea.setEnabled(isWritable);
        descriptionArea.getDocument().removeDocumentListener(
                            descriptionAreaListener);
        descriptionArea.setText(description);
        descriptionArea.getDocument().addDocumentListener(
                        descriptionAreaListener);
        tabs.setVisible(!(name.equals("")));
        finishButton.setEnabled(!(name.equals("")));
        titleLabel.setText(title);
        //repaint();
    }

    /**
     * Displays the permission and related information.
     * 
     * @param details       The information to display.
     * @param permission    The object permission.
     */
    void displayDetails(Map details, PermissionData permission)
    {
       permissionPanel.removeAll();
       
       if (details != null) {
           permissionPanel.add(
                   new PermissionPane(this, model, details, permission), 
                   BorderLayout.NORTH);
       }
       permissionPanel.validate();
       permissionPanel.repaint();
    }

    /**
     * Returns the name of the edited data object.
     * 
     * @return See above.
     */
    String getObjectName() { return nameArea.getText(); }

    /**
     * Returns the description of the edited data object.
     * 
     * @return See above.
     */
    String getObjectDescription() { return descriptionArea.getText(); }

    /**
     * Returns the value of the edit flag.
     * 
     * @return See above.
     */
    boolean isEdit() { return edit; }
    
    /**
     * Sets the value of the {@link #edit} flag.
     * 
     * @param edit The value to set.
     */
    void setEdit(boolean edit) { this.edit = edit; }
    
    /** Resets the original. */
    void resetName()
    {
    	nameArea.getDocument().removeDocumentListener(nameAreaListener);
        nameArea.setText(originalName);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
    }
    
}
