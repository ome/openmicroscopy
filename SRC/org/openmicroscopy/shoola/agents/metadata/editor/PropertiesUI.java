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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

/** 
 * Displays the properties of the selected object.
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
	extends AnnotationUI
	implements DocumentListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";

	/** The details string. */
    private static final String DETAILS = "Details";
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField          nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea          	descriptionArea;

    /** Panel hosting the main display. */
    private JComponent			contentPanel;
    
    
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
        JLabel label = UIUtilities.setTextFont(EditorUtil.OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(EditorUtil.READ);
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
        box =  new JCheckBox(EditorUtil.WRITE);
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
        label = UIUtilities.setTextFont(EditorUtil.GROUP);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
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
        box =  new JCheckBox(EditorUtil.WRITE);
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
        label = UIUtilities.setTextFont(EditorUtil.WORLD);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
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
        box =  new JCheckBox(EditorUtil.WRITE);
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
    
    /**
     * Lays out the key/value (String, String) pairs.
     * 
     * @param details The map to handle.
     * @return See above.
     */
    private JPanel layoutDetails(Map details)
    {
    	JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        //c.insets = new Insets(3, 3, 3, 3);
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
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        nameArea.getDocument().addDocumentListener(this);
        descriptionArea.getDocument().addDocumentListener(this);
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
        /*
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        JLabel l;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        c.gridx = 0;
        content.add(UIUtilities.setTextFont("ID"), c);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l = new JLabel(""+model.getRefObjectID());
        content.add(l, c);
        c.gridy++;
        
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        c.gridx = 0;
        content.add(UIUtilities.setTextFont("Name"), c);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(nameArea, c);
        
        c.gridy++;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.weightx = 0.0; 
        c.gridx = 0;
        l = UIUtilities.setTextFont("Description");
        content.add(l, c);
       
        
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        JScrollPane pane = new JScrollPane(descriptionArea);
        pane.setPreferredSize(new Dimension(80, 100));
        content.add(pane, c);
        */
        int height = 80;
        double[][] tl = {{TableLayout.PREFERRED, 250}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
        				TableLayout.PREFERRED, height} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        //content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        content.add(UIUtilities.setTextFont("ID"), "0, 0, l, c");
        l = new JLabel(""+model.getRefObjectID());
        content.add(l, "1, 0, f, c");
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(nameArea, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        l = UIUtilities.setTextFont("Description");
        content.add(l, "0, 3, l, c");
        content.add(new JScrollPane(descriptionArea), "1, 3, 1, 4");
        return content;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
    	contentPanel.setLayout(new BoxLayout(contentPanel, 
    								BoxLayout.Y_AXIS));
    	contentPanel.add(buildContentPanel());
        ExperimenterData exp = model.getRefObjectOwner();
        if (exp != null) {
        	JPanel p = new JPanel();
        	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        	JPanel details = layoutDetails(
        				EditorUtil.transformExperimenterData(exp));
        	PermissionData perm = model.getRefObjectPermissions();
        	p.add(details);
        	if (perm != null && !(model.getRefObject() instanceof ImageData)) {
        		p.add(buildPermissions(perm));
        	}
        	UIUtilities.setBoldTitledBorder(DETAILS, p);
        	TreeComponent tree = new TreeComponent();
        	JPanel collapse = new JPanel();
        	collapse.setBorder(new TitledLineBorder(DETAILS, 
        						collapse.getBackground()));
        	tree.insertNode(p, collapse, false);
        	contentPanel.add(tree);
            contentPanel.add(new JPanel());
        }
        add(contentPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link EditorModel}.
     * 				Mustn't be <code>null</code>.                            
     */
    PropertiesUI(EditorModel model)
    {
       super(model);
       title = TITLE;
       initComponents();
       UIUtilities.setBoldTitledBorder(getComponentTitle(), this);
    }   

    /**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		nameArea.getDocument().removeDocumentListener(this);
		descriptionArea.getDocument().removeDocumentListener(this);
		nameArea.setText(model.getRefObjectName());
        descriptionArea.setText(model.getRefObjectDescription());
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        nameArea.setEnabled(b);
        descriptionArea.setEnabled(b);
        if (b) {
        	nameArea.getDocument().addDocumentListener(this);
    		descriptionArea.getDocument().addDocumentListener(this);
        }
        buildGUI();
	}
	
    /** Sets the focus on the name area. */
	void setFocusOnName() { nameArea.requestFocus(); }
   
	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = nameArea.getText().trim();
		String desc = descriptionArea.getText().trim();
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		}
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	/**
	 * No-op implementation in this case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * No-op implementation in this case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }
	
	/**
	 * Returns <code>true</code> if the data object has been edited,
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		String name = model.getRefObjectName();
		String value = nameArea.getText();
		if (!name.equals(value.trim())) return true;
		value = descriptionArea.getText().trim();
		name = model.getRefObjectDescription();
		value = value.trim();
		if (!value.equals(name)) return true;
		return false;
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
