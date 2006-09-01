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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component displaying the minimum information on the currently edited 
 * <code>DataObject</code>.
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
    
    /** Title of the annotation pane. */
    static final String     ANNOTATION = "Annotation";
    
    /** Title of the classification pane. */
    static final String     CLASSIFICATION = "Classification";   
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    JTextField                  nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    JTextArea                   descriptionArea;
    
    /** The tabbed pane hosting the annotation pane or classified pane. */
    private JTabbedPane         tabbedPane;
    
    /** 
     * The component hosting the annotation, <code>null</code> if the data 
     * object is not annotable.
     */
    private DOAnnotation        annotator;
    
    /** 
     * The component hosting the images' annotation when the edited object
     * is either a <code>Dataset</code> or <code>Category</code>.
     */
    private DOImagesAnnotation  leavesAnnotator;
    
    /** 
     * The component hosting the classification. <code>null</code> if the data 
     * object hasn't been clasified.
     */
    private DOClassification    classifier;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener    nameAreaListener;
    
    /** Reference to the View. */
    private EditorUI            view;
    
    /** Reference to the Model. */
    private EditorModel         model;
    
    /** Reference to the Control. */
    private EditorControl       controller;
    
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
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        tabbedPane = new JTabbedPane();
        nameArea = new JTextField();
        setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        setTextAreaDefault(descriptionArea);
        if (model.getEditorType() == Editor.PROPERTIES_EDITOR) {
            nameArea.setText(model.getDataObjectName());
            descriptionArea.setText(model.getDataObjectDescription());
            boolean b = model.isWritable();
            nameArea.setEnabled(b);
            descriptionArea.setEnabled(b);
            descriptionArea.getDocument().addDocumentListener(
                    new DocumentListener() {

                /** Handles text insertion. */
                public void insertUpdate(DocumentEvent de)
                {
                    view.handleDescriptionAreaInsert();
                }
                
                /** Handles text insertion. */
                public void removeUpdate(DocumentEvent de)
                {
                    view.handleDescriptionAreaInsert();
                }

                /** 
                 * Required by I/F but no-op implementation in our case. 
                 * @see DocumentListener#removeUpdate(DocumentEvent)
                 */
                public void changedUpdate(DocumentEvent de) {}
                
            });
            if (model.isAnnotatable()) {
                annotator = new DOAnnotation(view, model);
                IconManager im = IconManager.getInstance();
                //tabbedPane.ins
                tabbedPane.addTab(ANNOTATION, 
                            im.getIcon(IconManager.ANNOTATION), annotator);
            }
            if (model.isClassified()) {
                classifier = new DOClassification(model, controller);
                tabbedPane.addTab(CLASSIFICATION, 
                      IconManager.getInstance().getIcon(IconManager.CATEGORY),
                      classifier);
            }
        }
        nameAreaListener = new DocumentListener() {
            
            /** 
             * Updates the editor's controls when some text is inserted. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
                view.handleNameAreaInsert();
            }
            
            /** 
             * Displays an error message when the data object has no name. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                view.handleNameAreaRemove(de.getDocument().getLength());
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        nameArea.getDocument().addDocumentListener(nameAreaListener);
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
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
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
        return content;
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
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view          Reference to the {@link EditorUI}.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the {@link EditorModel}.
     *                      Mustn't be <code>null</code>.  
     * @param controller    Reference to the {@link EditorControl}.
     *                      Mustn't be <code>null</code>.                           
     */
    DOBasic(EditorUI view, EditorModel model, EditorControl controller)
    {
        if (view == null) 
            throw new IllegalArgumentException("No View.");
        if (model == null) 
            throw new IllegalArgumentException("No Model.");
        if (controller == null) 
            throw new IllegalArgumentException("No Control.");
        this.view = view;
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }   

    /**
     * Returns <code>true</code> if the data object can be annotated.
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotable()
    { 
        if (annotator == null) return false;
        return annotator.isAnnotable();
    }
    
    /**
     * Returns <code>true</code> if the data object has to be deleted,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotationDeleted()
    {  
        if (annotator == null) return false;
        return annotator.isAnnotationDeleted();
    }
    
    /** 
     * Returns the text of the annotation. 
     * 
     * @return See above. 
     */
    String getAnnotationText()
    { 
        if (annotator == null) return null;
        return annotator.getAnnotationText().trim();
    }
    
    /**
     * Returns the value of the {@link #nameArea}.
     * 
     * @return See above.
     */
    String getNameText() { return nameArea.getText().trim(); }
    
    /**
     * Returns the value of the {@link #descriptionArea}.
     * 
     * @return See above.
     */
    String getDescriptionText() { return descriptionArea.getText().trim(); }
    
    /** 
     * Sets the text to <code>null</code> but we first need to remove the 
     * {@link DocumentListener} attached to the {@link #nameArea}.
     * This method is invoked when the user tries to save a object which 
     * name is only made of spaces.
     * 
     *
     */
    void resetNameArea()
    {
        nameArea.getDocument().removeDocumentListener(nameAreaListener);
        nameArea.setText(null);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
    }
    
    /** Displays the images annotations. */
    void showLeavesAnnotations()
    {
        if (leavesAnnotator == null) return;
        leavesAnnotator.showAnnotations();
    }
    
    /** Displays the annotations. */
    void showAnnotations()
    { 
        if (annotator == null) return;
        annotator.showAnnotations();
    }

    /** Shows the classifications. */
    void showClassifications()
    {
        if (classifier == null) return;
        classifier.showClassifications();
    }
    
    //void resetNameAre
    /** 
     * Adds a listener to the {@link #tabbedPane} as soon as the thumbnail
     * is ready.
     */
    void addListeners()
    {
        //Add listener to the tabbed pane,
        tabbedPane.addChangeListener(new ChangeListener() {
            
            /**
             * Retrieves the classification when the classification tabbed pane
             * is selected for the first time.
             * @see ChangeListener#stateChanged(ChangeEvent)
             */
            public void stateChanged(ChangeEvent ce)
            {
                JTabbedPane pane = (JTabbedPane) ce.getSource();
                Component c = pane.getSelectedComponent();
                if (c instanceof DOClassification) {
                    if (!model.isClassificationLoaded())
                        model.fireClassificationLoading();
                }
            };
        });
    }
    
}
