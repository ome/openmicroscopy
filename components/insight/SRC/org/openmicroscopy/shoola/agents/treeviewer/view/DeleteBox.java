/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.DeleteBox
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * A modal dialog asking what the user wants to delete.
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
public class DeleteBox 
	extends MessageBox
{

	/** The title of the dialog. */
	private static final String		TITLE = "Confirm delete";
	
	/** The default delete text. */
	private static final String		DEFAULT_TEXT = "<html>Are you sure you want to " +
			"delete the selected ";

	/** The text displayed in the tool tip for annotations. */
	private static final String    TOOL_TIP = "The annotations are " +
			"deleted only if you own them and if they are not used by others.";
		
	/** Text display if the user is a group owner. */
	private static final String		WARNING_GROUP_OWNER = "<html>Some data " +
			"might be used by other users<br/>"+
			"they will no longer be able to use or see them.</html>";
	
    /**
     * Text display if other user's data is going to be deleted 
     */
    private static final String NOT_OWNER_WARNING = "Warning: Some objects you selected are owned by other users!";

	/** The button to display the tool tip. */
	private JButton					infoButton;
	
	/** Delete the related annotations if selected. */
	private JCheckBox				withAnnotation;
	
	/** The type of object to remove. */
	private Class					type;
	
	/** The Name space of the object to remove. */
	private String					nameSpace;
	
	/** Flag indicating if the objects have been annotated. */
	private boolean					annotation;
    
	/** The components corresponding to the annotation. */
	private Map<JCheckBox, Class>	annotationTypes;
	
	/** The UI component hosting the various annotations types. */
	private JPanel					typesPane;
	
	/**
	 * Creates and formats a check box.
	 * 
	 * @param name The name to display.
	 * @return See above.
	 */
	private JCheckBox createBox(String name)
	{
		JCheckBox box = new JCheckBox(name);
		Font f = box.getFont();
		int size = f.getSize()-2;
		Font newFont = f.deriveFont(Font.ITALIC, size);
		box.setSelected(true);
		box.setFont(newFont);
		box.setEnabled(false);
		return box;
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param annotationText The text to display after the annotations text.
	 */
	private void initComponents(String annotationText)
	{
		IconManager icons = IconManager.getInstance();
		infoButton = new JButton(icons.getIcon(IconManager.INFO));
		infoButton.setToolTipText(TOOL_TIP);
		withAnnotation = new JCheckBox("Also delete the annotations " +
				"linked to the objects.");
		withAnnotation.setToolTipText(TOOL_TIP);
		annotationTypes = new LinkedHashMap<JCheckBox, Class>();
		annotationTypes.put(createBox("Tag"), TagAnnotationData.class);
		annotationTypes.put(createBox("Attachment"), FileAnnotationData.class);
		//annotationTypes.put(createBox("Ontology Terms"), 
		//		TermAnnotationData.class);
		withAnnotation.addChangeListener(new ChangeListener() {
		
			public void stateChanged(ChangeEvent e) {
				layoutAnnotationTypes();
			}
		
		});
		typesPane = new JPanel();
		typesPane.setLayout(new GridBagLayout());
	}
	
	/** Lays out the annotation types. */
	private void layoutAnnotationTypes()
	{
		boolean b = withAnnotation.isSelected();
		typesPane.setEnabled(b);
		Iterator<JCheckBox> i = annotationTypes.keySet().iterator();
		while (i.hasNext()) {
			(i.next()).setEnabled(b);
		}
	}
	
	/**
	 * Builds and lays out the message displayed next to the option to delete
	 * annotations.
	 * 
	 * @return See above.
	 */
	private JLabel buildAnnotationWarning()
	{
		JLabel label = UIUtilities.setTextFont(TOOL_TIP, Font.ITALIC);
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		return label;
	}
	
	/** 
	 * Builds and lays out the component.
	 * 
	 * @param groupLeader Pass <code>true</code> to indicate that the user
	 * currently logged in is the owner of one of the groups the objects 
	 * belong to, <code>false</code> otherwise.
	 */
	private void layoutComponents(boolean groupLeader)
	{
		Iterator<JCheckBox> i = annotationTypes.keySet().iterator();
		typesPane.setLayout(new BoxLayout(typesPane, BoxLayout.Y_AXIS));
		typesPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		while (i.hasNext()) {
			typesPane.add(i.next());
		}
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		boolean add = false;
		if (DatasetData.class.equals(type) || 
				ProjectData.class.equals(type) ||
				PlateData.class.equals(type) || 
				ScreenData.class.equals(type) || 
				PlateAcquisitionData.class.equals(type) ||
				ImageData.class.equals(type)) {
			add = true;
			if (annotation) {
				p.add(buildAnnotationWarning());
				p.add(Box.createVerticalStrut(10));
				p.add(withAnnotation);
				p.add(typesPane);
			}
		}
		JPanel body;
		if (groupLeader) {
			body = new JPanel();
			body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
			body.add(UIUtilities.buildComponentPanel(p));
			JLabel label = UIUtilities.setTextFont(WARNING_GROUP_OWNER, 
					Font.BOLD);
			label.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
			body.add(UIUtilities.buildComponentPanel(label));
			body.add(Box.createVerticalStrut(5));
		} else body = p;
		if (add)
			addBodyComponent(UIUtilities.buildComponentPanel(body));
	}
	
	/**
	 * Returns the text corresponding to the specified type.
	 * 
	 * @param type		The type of object to handle.
	 * @param number	The number of object to remove.
	 * @param ns		Name space related to the data object if any.
	 * @return See above.
	 */
	private static String getTypeAsString(Class type, int number, String ns)
	{
		String end = "";
		if (number > 1) end = "s";
		if (ImageData.class.equals(type))
			return "Image"+end;
		else if (DatasetData.class.equals(type))
			return "Dataset"+end;
		else if (ProjectData.class.equals(type))
			return "Project"+end;
		else if (FileAnnotationData.class.equals(type))
			return "File"+end;
		else if (ScreenData.class.equals(type))
			return "Screen"+end;
		else if (PlateData.class.equals(type))
			return "Plate"+end;
		else if (PlateAcquisitionData.class.equals(type))
			return "Plate Run"+end;
		else if (ExperimenterData.class.equals(type))
			return "Experimenter"+end;
		else if (GroupData.class.equals(type))
			return "Group"+end;
		else if (TagAnnotationData.class.equals(type)) {
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
				return "Tag Set"+end;
			return "Tag"+end;
		} 
		return "";
	}
	
	/**
	 * Returns the message corresponding to the specified class and
	 * the number of selected items.
	 * 
	 * @param type			The type of object to handle.
	 * @param number		The number of object to remove.
	 * @param nameSpace		Name space related to the data object if any.
	 * @param annotation	Pass <code>true</code> if the objects have been 
	 * 						annotated, <code>false</code> otherwise.
	 * @param notOwner     Flag indicating that objects belonging to other users
     *  are going to be deleted
	 * @return See above. 
	 */
	private static String getMessage(Class type, int number, String nameSpace,
						boolean annotation, boolean notOwner)
	{
		StringBuffer buffer = new StringBuffer(); 
		String value = getTypeAsString(type, number, nameSpace);
		String text = null;
		if (value != null && value.length() > 0)
			text = DEFAULT_TEXT+value+"?";
		if (text != null) {
			buffer.append(text);
			buffer.append("\n");
			if (ImageData.class.equals(type) || 
					DatasetData.class.equals(type) || 
					ProjectData.class.equals(type) ||
					ScreenData.class.equals(type) || 
					PlateData.class.equals(type) ||
					PlateAcquisitionData.class.equals(type)) {
				if (annotation) buffer.append("If yes, ");
			}
		}
		if(notOwner) {
		    buffer.append("<br/><br/>");
		    buffer.append("<p style=\"font-weight: bold; color: red;\">"+NOT_OWNER_WARNING+"</p>");
		}
		buffer.append("</html>");
		return buffer.toString();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type			The type of objects to delete.
	 * @param annotation	Pass <code>true</code> if the object has 
	 * 						been annotated, <code>false</code> otherwise.
	 * @param number		The number of objects to delete.
	 * @param nameSpace		Name space related to the data object if any.
	 * @param parent 		The parent of the frame.
	 * @param groupLeader Pass <code>true</code> to indicate that the user
	 * currently logged in is the owner of one of the groups the objects 
	 * belong to, <code>false</code> otherwise.
	 * @param notOwner Flag indicating that objects belonging to other users
	 *  are going to be deleted
	 */
	public DeleteBox(JFrame parent, Class type, boolean annotation, int number,
			String nameSpace, boolean groupLeader, boolean notOwner)
	{
		super(parent, TITLE, 
				DeleteBox.getMessage(type, number, nameSpace, annotation, notOwner));
		this.nameSpace = nameSpace;
		this.type = type;
		this.annotation = annotation;
		initComponents(DeleteBox.getTypeAsString(type, number, nameSpace));
		layoutComponents(groupLeader);
		pack();
		setResizable(false);
	}
    
    /**
     * Returns <code>true</code> if the objects contained in the objects
     * to delete have to be deleted.
     * 
     * @return See above.
     */
    public boolean deleteContents()
    {
    	return !ImageData.class.equals(type);
    }
    
    /**
     * Returns the types of annotations to keep.
     * 
     * @return See above.
     */
    public List<Class> getAnnotationTypes()
    {
    	List<Class> types = new ArrayList<Class>();
    	Iterator<JCheckBox> i = annotationTypes.keySet().iterator();
    	JCheckBox box;
    	if (!withAnnotation.isSelected()) {
    		while (i.hasNext()) {
    			box = i.next();
    			types.add(annotationTypes.get(box));
			}
    	} else {
    		while (i.hasNext()) {
        		box = i.next();
    			if (!box.isSelected()) 
    				types.add(annotationTypes.get(box));
    		}
    	}
    	return types;
    }
    
}
