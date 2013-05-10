/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.DeleteBox
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
package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.apache.commons.io.FilenameUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
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

	/** Indicates that the dialog is used to delete objects.*/
	public static final int DELETE = 0;
	
	/** Indicates that the dialog is used to move objects.*/
	public static final int MOVE = 1;
	
	/**
	 * The preferred size of the viewport displaying images than cannot
	 * be deleted.
	 */
	private static final Dimension VIEWPORT_SIZE = new Dimension(300, 120);
	
	/** The title of the dialog. */
	private static final String TITLE_DELETE = "Confirm delete";
	
	/** The title of the dialog. */
	private static final String TITLE_MOVE = "Confirm Group change";
	
	/** The default delete text. */
	private static final String DEFAULT_TEXT_DELETE =
			"Are you sure you want to delete the selected ";
	
	/** The default delete text. */
	private static final String DEFAULT_TEXT_MOVE =
			"Are you sure you want to move the selected ";

	/** The text displayed in the tool tip for annotations. */
	private static final String TOOL_TIP = "The annotations are " +
			"deleted only if you own them and if they are not used by other " +
			"users.";
		
	/** Text display if the user is a group owner. */
	private static final String WARNING_GROUP_OWNER = "Some data " +
			"might be used by other users,\nthey will no longer be able to " +
			"use or see them.";
	
	/** Text display if the user is a group owner. */
	private static final String WARNING_FILESET_DELETE =
			"The following images " +
			"cannot be deleted. All images composing a multi-images file " +
			"must be selected.";
	
	/** Text display if the user is a group owner. */
	private static final String WARNING_FILESET_MOVE = "The following images " +
			"cannot be moved. All images composing a multi-images file " +
			"must be selected.";

	/** The button to display the tool tip. */
	private JButton infoButton;
	
	/** Delete the related annotations if selected. */
	private JCheckBox withAnnotation;
	
	/** The type of object to remove. */
	private Class<?> type;
	
	/** Flag indicating if the objects have been annotated. */
	private boolean annotation;
    
	/** The components corresponding to the annotation. */
	private Map<JCheckBox, Class<?>> annotationTypes;
	
	/** The UI component hosting the various annotations types. */
	private JPanel typesPane;
	
	/** The image to exclude. */
	private JPanel toExcludePane;
	
	/** The dialog index. One of the constants defined this class.*/
	private int index;
	
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
	 */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		infoButton = new JButton(icons.getIcon(IconManager.INFO));
		infoButton.setToolTipText(TOOL_TIP);
		withAnnotation = new JCheckBox("Also delete the annotations " +
				"linked to the objects.");
		withAnnotation.setToolTipText(TOOL_TIP);
		annotationTypes = new LinkedHashMap<JCheckBox, Class<?>>();
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
	 * @param toExclude The nodes to exclude from the delete list.
	 * @param number The number of objects to delete. If <code>0</code> no
	 * delete information will be displayed.
	 */
	private void layoutComponents(boolean groupLeader,
			Collection<ImageData> toExclude, int number)
	{
		int h = 0;
		if (toExclude != null && toExclude.size() > 0) {
			toExcludePane = new JPanel();
			toExcludePane.setLayout(new BoxLayout(toExcludePane,
					BoxLayout.Y_AXIS));
			Iterator<ImageData> j = toExclude.iterator();
			JLabel label;
			while (j.hasNext()) {
				label = new JLabel(FilenameUtils.getName(j.next().getName()));
				toExcludePane.add(label);
				h += label.getPreferredSize().height;
			}
		}
		
		Iterator<JCheckBox> i = annotationTypes.keySet().iterator();
		typesPane.setLayout(new BoxLayout(typesPane, BoxLayout.Y_AXIS));
		typesPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 0));
		while (i.hasNext()) {
			typesPane.add(i.next());
		}
		JPanel body = new JPanel();
		
		TableLayout layout = new TableLayout();
		double[] columns = {TableLayout.FILL};
		layout.setColumn(columns);
		body.setLayout(layout);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel mLabel;
		int count = 0;
		if (toExcludePane != null) {
			String s;
			switch (index) {
				case MOVE:
					s = WARNING_FILESET_MOVE;
					break;
				case DELETE:
				default:
				s = WARNING_FILESET_DELETE;
			}
			mLabel = UIUtilities.setTextFont(s, Font.BOLD);
			layout.insertRow(count, TableLayout.PREFERRED);
			body.add(mLabel, "0, "+count);
			JScrollPane pane = new JScrollPane(toExcludePane);
			pane.setHorizontalScrollBarPolicy(
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pane.setVerticalScrollBarPolicy(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			if (h > VIEWPORT_SIZE.height)
				pane.getViewport().setPreferredSize(VIEWPORT_SIZE);
			count++;
			layout.insertRow(count, TableLayout.PREFERRED);
			body.add(pane, "0, "+count);
			count++;
			layout.insertRow(count, TableLayout.PREFERRED);
			body.add(Box.createVerticalStrut(10), "0, "+count);
		}
		boolean add = false;
		if (DatasetData.class.equals(type) ||
				ProjectData.class.equals(type) ||
				PlateData.class.equals(type) ||
				ScreenData.class.equals(type) ||
				PlateAcquisitionData.class.equals(type) ||
				ImageData.class.equals(type)) {
			add = true;
			
			if (annotation && number != 0) {
				p.add(buildAnnotationWarning());
				p.add(Box.createVerticalStrut(10));
				p.add(withAnnotation);
				p.add(typesPane);
			}
		}
		
		if (groupLeader && number != 0) {
			mLabel = UIUtilities.setTextFont(WARNING_GROUP_OWNER, Font.BOLD);
			mLabel.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
			if (count > 0) count++;
			layout.insertRow(count, TableLayout.PREFERRED);
			body.add(mLabel, "0, "+count);
			if (annotation) {
				count++;
				layout.insertRow(count, TableLayout.FILL);
				body.add(new JSeparator(), "0, "+count);
			}
			
		}
		if (count > 0) count++;
		layout.insertRow(count, TableLayout.PREFERRED);
		body.add(UIUtilities.buildComponentPanel(p), "0, "+count);
		if (add) {
			addBodyComponent(UIUtilities.buildComponentPanel(body));
		}
	}
	
	/**
	 * Returns the text corresponding to the specified type.
	 * 
	 * @param type		The type of object to handle.
	 * @param number	The number of object to remove.
	 * @param ns		Name space related to the data object if any.
	 * @return See above.
	 */
	private String getTypeAsString(Class<?> type, int number, String ns)
	{
		if (number == 0) return "";
		String end = "";
		StringBuffer buffer = new StringBuffer();
		if (number > 1) end = "s";
		if (ImageData.class.equals(type))
			buffer.append("Image");
		else if (DatasetData.class.equals(type))
			buffer.append("Dataset");
		else if (ProjectData.class.equals(type))
			buffer.append("Project");
		else if (FileAnnotationData.class.equals(type))
			buffer.append("File");
		else if (ScreenData.class.equals(type))
			buffer.append("Screen");
		else if (PlateData.class.equals(type))
			buffer.append("Plate");
		else if (PlateAcquisitionData.class.equals(type))
			buffer.append("Plate Run");
		else if (ExperimenterData.class.equals(type))
			buffer.append("Experimenter");
		else if (GroupData.class.equals(type))
			buffer.append("Group");
		else if (TagAnnotationData.class.equals(type)) {
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
				buffer.append("Tag Set");
			else buffer.append("Tag");
		}
		if (buffer.length() != 0) buffer.append(end);
		return buffer.toString();
	}
	
	/**
	 * Returns the message corresponding to the specified class and
	 * the number of selected items.
	 * 
	 * @param type The type of object to handle.
	 * @param number The number of object to remove.
	 * @param nameSpace Name space related to the data object if any.
	 * @return See above. 
	 */
	private String getMessage(Class<?> type, int number, String nameSpace)
	{
		if (number == 0) return "";
		StringBuffer buffer = new StringBuffer();
		String value = getTypeAsString(type, number, nameSpace);
		if (value != null && value.length() > 0) {
			switch (index) {
				case MOVE:
					buffer.append(DEFAULT_TEXT_MOVE);
					break;
				case DELETE:
				default:
					buffer.append(DEFAULT_TEXT_DELETE);
			}
			buffer.append(value);
			buffer.append("?");
		}
		return buffer.toString();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type of objects to delete.
	 * @param annotation Pass <code>true</code> if the object has
	 * been annotated, <code>false</code> otherwise.
	 * @param number The number of objects to delete. If <code>0</code> no
	 * delete information will be displayed.
	 * @param nameSpace Name space related to the data object if any.
	 * @param parent The parent of the frame.
	 * @param groupLeader Pass <code>true</code> to indicate that the user
	 * currently logged in is the owner of one of the groups the objects 
	 * belong to, <code>false</code> otherwise.
	 * @param index One of the constants defined by this class.
	 */
	public DeleteBox(JFrame parent, Class<?> type, boolean annotation,
			int number, String nameSpace, boolean groupLeader,
			Collection<ImageData> toExclude, int index)
	{
		super(parent, TITLE_DELETE, "");
		this.index = index;
		if (index == MOVE) setTitle(TITLE_MOVE);
		this.type = type;
		this.annotation = annotation;
		header.setDescription(getMessage(type, number, nameSpace));
		initComponents();
		layoutComponents(groupLeader, toExclude, number);
		if (number == 0) {
			setYesText("OK");
			hideNoButton();
		}
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
