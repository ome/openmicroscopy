/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOBasic
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.util.UtilConstants;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;

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
class DOBasic
    extends JPanel
{
    
    /** Used as text of the {@link #annotationButton}. */
    private static final String     SHOW_ANNOTATE = "Annotation >>";
    
    /** Used as text of the {@link #annotationButton}. */
    private static final String     HIDE_ANNOTATE = "<< Annotation";
    
    /** 
     * The text displayed when the currently edited <code>DataObject</code>
     * hasn't been annotated.
     */
    private static final String		NO_ANNOTATION_TEXT = "Not annotated";
    
    /** 
     * The preferred size of the scroll pane containing the explanation of
     * the notification message.
     */
    private static final Dimension  SCROLL_PANE_SIZE = new Dimension(300, 150);
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension  SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /** 
     * The size of the invisible components used to separate widgets
     * vertically.
     */
    protected static final Dimension    V_SPACER_SIZE = new Dimension(1, 20);
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    JTextArea           nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    JTextArea           descriptionArea;
    
    /** Area where to annotate the <code>DataObject</code>. */
    JTextArea           annotationArea;
    
    /** Button to show or hide the {@link #annotationPanel}. */
    private JButton     annotationButton;
    
    /** Panel hosting the {@link #annotationArea}. */
    private JPanel      annotationPanel;
    
    private boolean     isAnnotationShowing;
    
    /** Reference to the parent. */
    private DOEditor    editor;
    
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
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        nameArea = new MultilineLabel();
        setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        setTextAreaDefault(descriptionArea);
        nameArea.getDocument().addDocumentListener(
                new DocumentListener() {

            public void insertUpdate(DocumentEvent de)
            {
                editor.handleNameAreaInsert();
            }

            public void removeUpdate(DocumentEvent de)
            {
                if (de.getDocument().getLength() == 0)
                    editor.handleEmptyNameArea();
            }

            /** Required by I/F but no-op in our case. */
            public void changedUpdate(DocumentEvent de) {}
        });
        
        if (editor.getEditorType() == DOEditor.EDIT) {
            nameArea.setText(editor.getDataObjectName());
            descriptionArea.setText(editor.getDataObjectDescription());
            descriptionArea.getDocument().addDocumentListener(
                    new DocumentListener() {

                public void insertUpdate(DocumentEvent de)
                {
                    editor.setButtonsEnabled(true);
                }
                
                /** Required by I/F but no-op in our case. */
                public void removeUpdate(DocumentEvent de) {}

                /** Required by I/F but no-op in our case. */
                public void changedUpdate(DocumentEvent de) {}
            });
            if (!(editor.isEditable())) {
                nameArea.setEditable(false);
                descriptionArea.setEditable(false);
             }
            if (editor.isAnnotable()) {
                annotationArea = new MultilineLabel();
                annotationArea.setEditable(editor.isEditable());
                annotationArea.getDocument().addDocumentListener(
                        new DocumentListener() {

                    public void insertUpdate(DocumentEvent de)
                    {
                        editor.setButtonsEnabled(true);
                        editor.setAnnotated(true);
                    }
                    
                    /** Required by I/F but no-op in our case. */
                    public void removeUpdate(DocumentEvent de) {}

                    /** Required by I/F but no-op in our case. */
                    public void changedUpdate(DocumentEvent de) {}
                });
                setTextAreaDefault(annotationArea);
                annotationButton = new JButton(SHOW_ANNOTATE);
                annotationButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    { 
                        handleClick();
                    }
                });
                buildAnnotationPanel();
            }
        }
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link DOEditor#EDIT} mode,
     * the {@link #annotationButton} is shown. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
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
        content.add(pane, c);
        label = UIUtilities.setTextFont("Description");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        c.gridx = 1;
        c.ipady = 60;      //make this component tall
        c.gridheight = 2; //label north location
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pane  = new JScrollPane(descriptionArea);
        label.setLabelFor(pane);
        content.add(pane, c);
        if (editor.getEditorType() == DOEditor.EDIT && editor.isAnnotable()) {
            AnnotationData data = editor.getAnnotationData();
            if (data != null) annotationArea.setText(data.getText());
            label = new JLabel(formatAnnotation(data));
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            c.ipadx = 0;
            c.ipady = 0; 
            content.add(label, c);
            c.gridx = 1;
            label.setLabelFor(annotationButton);
            content.add(annotationButton, c);
        }
        return content;
    }
    
    /** Builds the panel hosting the {@link #annotationArea}. */
    private void buildAnnotationPanel()
    {
        annotationPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(annotationArea);
        scrollPane.setPreferredSize(SCROLL_PANE_SIZE);
        annotationPanel.setLayout(
                            new BoxLayout(annotationPanel, BoxLayout.Y_AXIS));
        annotationPanel.setBorder(
                                BorderFactory.createEmptyBorder(0, 10, 0, 10));
        annotationPanel.add(Box.createRigidArea(V_SPACER_SIZE));
        annotationPanel.add(new JSeparator());
        annotationPanel.add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
        annotationPanel.add(scrollPane);
        annotationPanel.add(Box.createVerticalGlue());
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        JPanel contentPanel = buildContentPanel();
        setLayout(new BorderLayout());
        setMaximumSize(contentPanel.getPreferredSize());
        setBorder(new EtchedBorder());
        add(contentPanel, BorderLayout.NORTH);
    }
    
    /**
     * Formats the specified annotation.
     * 
     * @param data The annotation to format.
     * @return See above.
     */
    private String formatAnnotation(AnnotationData data)
    {
        if (data == null) return NO_ANNOTATION_TEXT;
        StringBuffer buf = new StringBuffer();
        buf.append("<html><body>");
        DateFormat df = DateFormat.getDateInstance();
        buf.append("<p>Last annotated: <br>");
        buf.append(df.format(data.getLastModified()));
        buf.append("<br>");
        buf.append("by "+data.getOwner().getFirstName()+" "+
                	data.getOwner().getLastName());
        buf.append("</p></body></html>");
        return buf.toString();
    }
    
    /**
     * Handles mouse clicks on the {@link #annotationButton}.
     * The {@link #annotationPanel} is shown/hidden depending on the current 
     * value of {@link #isAnnotationShowing}, which is then modified to
     * reflect the new state. Also the {@link #annotationButton} text is changed
     * accordingly.
     */
    private void handleClick()
    {
        if (isAnnotationShowing) {
            annotationButton.setText(SHOW_ANNOTATE);
            remove(annotationPanel);
        } else {
            annotationButton.setText(HIDE_ANNOTATE);
            add(annotationPanel, BorderLayout.CENTER);
        }
        isAnnotationShowing = !isAnnotationShowing;
        validate();
        repaint();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param editor    Reference to the {@link DOEditor}.
     *                  Mustn't be <code>null</code>.
     */
    DOBasic(DOEditor editor)
    {
        if (editor == null) 
            throw new IllegalArgumentException("No editor.");
        this.editor = editor;
        initComponents();
        buildGUI();
    }   

}
