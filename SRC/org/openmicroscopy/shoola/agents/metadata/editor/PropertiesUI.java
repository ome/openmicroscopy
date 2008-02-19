/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.ObjectTranslator;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class PropertiesUI   
	extends JPanel
{
    
    /** Title of the annotation pane. */
    static final String     	ANNOTATION = "Annotation";
    
    /** Title of the classification pane. */
    static final String     	CLASSIFICATION = "Tag";   
    
    private static final String DETAILS = "Details";
    
    private static final String INFO = "Info";
    
    /** Text of the {@link #download} button. */
    private static final String	DOWNLOAD = "Download";
    
    /** Description of the {@link #download} button. */
    private static final String	DOWNLOAD_DESCRIPTION = "Download the " +
    												"archived files";
    
   
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField          nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea          	descriptionArea;
    
    /** Button to download the archived files. */
    private JButton				download;
    
    /** The tabbed pane hosting the annotation pane or classified pane. */
    private JTabbedPane         tabbedPane;
    
    /** 
     * The component hosting the annotation, <code>null</code> if the data 
     * object is not annotable.
     */
    //private AnnotatorEditor		annotator;
    
    /** 
     * The component hosting the classification. <code>null</code> if the data 
     * object hasn't been clasified.
     */
    //private DOClassification    classifier;
    
    /** Component hosting the additional information. */
    private JComponent			informationPanel;

    /** Panel hosting the main display. */
    private JComponent			contentPanel;

    /** Panel hosting the information about the image. */
    private JComponent			imageInfoPanel;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener    nameAreaListener;

    /** Reference to the Model. */
    private PropertiesView         model;
    
    /** Downloads the archived files. */
    void download()
    { 
    	
    }
    
    private void handleDescriptionAreaInsert()
    {
    	
    }
    private void handleNameAreaInsert()
    {
    	
    }
    
    private void handleNameAreaRemove(int length)
    {
    	
    }
    
    private JPanel buildInfoPane( Map details)
    {
    	double[][] tl = {{TableLayout.FILL}, {200}}; 
    	setLayout(new TableLayout(tl));
    	JScrollPane pane = new JScrollPane(contentPanel); 
    	JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(tl));
		panel.add(pane, "0, 0, f, t");
    	return panel;
    }
    
    private JPanel buildPermissionPane(Map details)
    {
    	contentPanel = layoutDetails(details);
    	double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		p.add(new JSeparator());
		p.add(Box.createRigidArea(PropertiesView.SMALL_V_SPACER_SIZE));
		if (model.isPermissionsShowable()) {
			 p.add(buildPermissions(model.getRefObjectPermissions()));
		     p.add(Box.createVerticalGlue());
		}
		JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(tl));
		panel.add(contentPanel, "0, 0, f, t");
		panel.add(p, "0, 1, f, t");
		return panel;
    }
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(PermissionData permissions)
    {
        JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //The owner is the only person allowed to modify the permissions.
        //boolean isOwner = model.isObjectOwner();
        //Owner
        JLabel label = UIUtilities.setTextFont(ObjectTranslator.OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isUserRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isUserWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 0, l, c");
        content.add(p, "1, 0, l, c");  
        //Group
        label = UIUtilities.setTextFont(ObjectTranslator.GROUP);
        p = new JPanel();
        box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isGroupRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isGroupWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 1, l, c");
        content.add(p, "1,1, l, c"); 
        //OTHER
        label = UIUtilities.setTextFont(ObjectTranslator.WORLD);
        p = new JPanel();
        box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isWorldRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isWorldWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 2, l, c");
        content.add(p, "1, 2, l, c"); 
        return content;
    }
    
    private JPanel layoutDetails(Map details)
    {
    	JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        return content;
    }
    
    private void finish()
    {
    	
    }
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	tabbedPane = new JTabbedPane();
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        if (model.getType() == PropertiesView.EDITION) {
            nameArea.setText(model.getRefObjectName());
            descriptionArea.setText(model.getRefObjectDescription());
            boolean b = model.isWritable();
            nameArea.setEnabled(b);
            descriptionArea.setEnabled(b);
            descriptionArea.getDocument().addDocumentListener(
                    new DocumentListener() {

                /** 
                 * Handles text insert. 
                 * @see DocumentListener#insertUpdate(DocumentEvent)
                 */
                public void insertUpdate(DocumentEvent de)
                {
                    handleDescriptionAreaInsert();
                }
                
                /** 
                 * Handles text removal. 
                 * @see DocumentListener#removeUpdate(DocumentEvent)
                 */
                public void removeUpdate(DocumentEvent de)
                {
                    handleDescriptionAreaInsert();
                }

                /** 
                 * Required by I/F but no-op implementation in our case. 
                 * @see DocumentListener#changedUpdate(DocumentEvent)
                 */
                public void changedUpdate(DocumentEvent de) {}
                
            });
            Map details;
            Object ho = model.getRefObject();
            if (ho instanceof ImageData) {
            	download = new JButton(DOWNLOAD);
            	download.setToolTipText(DOWNLOAD_DESCRIPTION);
            	download.setEnabled(model.isReadable());
            	download.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent e)
            		{ 
            			download(); 
            		}
				});
            	/*
            	details = EditorUtil.transformImageData((ImageData) ho);
            	imageInfoPanel = new DOInfo(view, model, details, false, 
            								DOInfo.INFO_TYPE);
            								*/
            }
            ExperimenterData exp = model.getRefObjectOwner();
            /*
            details = EditorUtil.transformExperimenterData(exp);
            informationPanel = new DOInfo(view, model, details, true, 
                                DOInfo.OWNER_TYPE);
                                */
            
        } //end if editor type
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
        nameArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    Object source = e.getSource();
                    if (source instanceof JTextField) {
                        JTextField field = (JTextField) source;
                        if (field.getText() != null && 
                                field.getText().length() > 0)
                            finish();
                    }
                }
            }
        });
        if (tabbedPane == null) return;
        tabbedPane.addChangeListener(new ChangeListener() {
            
            /**
             * Retrieves the classification when the classification tabbed pane
             * is selected for the first time.
             * @see ChangeListener#stateChanged(ChangeEvent)
             */
            public void stateChanged(ChangeEvent ce)
            {
            	/*
                JTabbedPane pane = (JTabbedPane) ce.getSource();
                int index = pane.getSelectedIndex();
                EditorFactory.setSubSelectedPane(index);
                switch (index) {
					case Editor.ANNOTATION_INDEX:
						if (model.isAnnotatable()) 
	                        controller.retrieveAnnotations();
						break;
					case Editor.CLASSIFICATION_INDEX:
						if (model.isClassified()) {
	                        if (!model.isClassificationLoaded())
	                            controller.loadClassifications();
	                    }
						break;
				}
				*/
            };
        });
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 100;
        //if (model.isAnnotatable()) height = 50;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{0, TableLayout.PREFERRED, 5, 0, height} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        if (model.getType() != PropertiesView.CREATION) { 
            layout.setRow(0, TableLayout.PREFERRED);
            content.add(UIUtilities.setTextFont("ID"), "0, 0, l, c");
            l = new JLabel(""+model.getRefObjectID());
            l.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
            content.add(l, "1, 0, f, c");
        }
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(nameArea, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        l = UIUtilities.setTextFont("Description");
        int h = l.getFontMetrics(l.getFont()).getHeight()+5;
        layout.setRow(3, h);
        content.add(l, "0, 3, l, c");
        JScrollPane pane = new JScrollPane(descriptionArea);
        content.add(pane, "1, 3, 1, 4");
        return content;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        setLayout(new BorderLayout());
        if (informationPanel != null) {
        	UIUtilities.setBoldTitledBorder(DETAILS, informationPanel);
        	TreeComponent tree = new TreeComponent();
        	tree.insertNode(informationPanel, 
        				UIUtilities.buildCollapsePanel(DETAILS), false);
        	/*
        	TreeComponentNode c = new TreeComponentNode(informationPanel, 
        			UIUtilities.buildCollapsePanel(DETAILS), false);
        	c.addPropertyChangeListener(this);
        	*/
        	contentPanel = new JPanel();
        	contentPanel.setLayout(new BoxLayout(contentPanel, 
        								BoxLayout.Y_AXIS));
        	contentPanel.add(buildContentPanel());
        	//contentPanel.add(c);
        	if (imageInfoPanel != null) {
        		UIUtilities.setBoldTitledBorder(INFO, imageInfoPanel);
        		tree.insertNode(imageInfoPanel, 
        				UIUtilities.buildCollapsePanel(INFO));
        		/*
        		c = new TreeComponentNode(imageInfoPanel, 
            			UIUtilities.buildCollapsePanel(INFO));
            	c.addPropertyChangeListener(this);
            	contentPanel.add(c);
            	*/
        	}
        	contentPanel.add(tree);
            contentPanel.add(new JPanel());
            add(contentPanel, BorderLayout.NORTH);
        } else add(buildContentPanel(), BorderLayout.NORTH);
        //add(view.buildBasicToolBar(download), BorderLayout.SOUTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link PropertiesView}.
     * 				Mustn't be <code>null</code>.                            
     */
    PropertiesUI(PropertiesView model)
    {
        if (model == null)  throw new IllegalArgumentException("No Model.");
        this.model = model;
        initComponents();
        buildGUI();
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
     */
    void resetNameArea()
    {
        nameArea.getDocument().removeDocumentListener(nameAreaListener);
        nameArea.setText(null);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
    }

    /** Shows the tags. */
    void showTags()
    {
    	/*
    	if (!model.isImage()) return;
    	contentPanel.remove(contentPanel.getComponentCount()-1);
    	SingleTagEditor editor = new SingleTagEditor(model.getTags(), 
    					model.getTagSets(), model.getAvailableTags());
    	editor.addPropertyChangeListener(controller);
    	contentPanel.add(UIUtilities.buildComponentPanel(editor));
    	validate();
    	repaint();
    	*/
    }

    /** 
     * Resets the value of the {@link #nameArea} when a wrong value 
     * has been entered.
     */
    void resetName()
    {
    	nameArea.getDocument().removeDocumentListener(nameAreaListener);
    	if (model.getType() == PropertiesView.EDITION) 
            nameArea.setText(model.getRefObjectName());
        else nameArea.setText("");
    	nameArea.getDocument().addDocumentListener(nameAreaListener);
    }
    
    /**
     * Checks if the name and/or description have been modified.
     * Returns <code>true</code> if modified, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasDataToSave()
    {
    	// heck if the name and the description have been modified first.
    	/*
    	String s = getNameText();
    	String name = model.getDataObjectName();
    	if (!(name.equals(s))) return true;
    	String d = getDescriptionText();
    	String description = model.getDataObjectDescription();
    	
    	if (d == null) return (description != null);
    	return (!(d.equals(description)));
    	*/
    	return false;
    }

    /** Sets the focus on the name area. */
	void setFocusOnName() { nameArea.requestFocus(); }
   
}
