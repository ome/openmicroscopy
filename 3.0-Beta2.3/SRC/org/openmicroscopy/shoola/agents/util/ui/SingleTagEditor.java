/*
 * org.openmicroscopy.shoola.agents.util.ui.SingleTagEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;

/** 
 * Edits tags related to a given data obejct.
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
public class SingleTagEditor 
	extends JPanel
	implements PropertyChangeListener
{

	/** Bound property indicating to load the available tags. */
	public static final String	LOAD_AVAILABLE_TAGS_PROPERTY = 
														"loadAvailableTags";
	
	/** Bound property indicating to browse the tag. */
	public static final String	BROWSE_TAG_PROPERTY = "browseTag";
	
	/** Bound property indicating to remove the tag form the edited object. */
	public static final String	REMOVE_TAG_PROPERTY = "removeTag";
	
	/** Bound property indicating to add the tag to the edited object. */
	public static final String	ADD_TAG_PROPERTY = "addTag";
	
	/** Bound property indicating to add the tag to the edited object. */
	public static final String	EDIT_TAG_PROPERTY = "editTag";
	
	/** Text indicating the describing the component. */
	private static final String TEXT = "The list of tags linked to the " +
			"image. Double Click on the name to browse the tag " +
			"or the tag set.";
	
	/** Text indicating the describing the component. */
	private static final String TEXT_NO_TAG = "The image has no tags.";
	
	/** Collection of nodes hosting the tag sets. */
	private List<TagNode>	tagSetNodes;
	
	/** Collection of nodes hosting the tags. */
	private List<TagNode>	tagNodes;
	
	/** Collection of nodes hosting the available tags. */
	private List<TagNode>	availableTagNodes;
	
	/** Button to retrieve the available tags. */
	private JButton			availableTag;
	
	/** 
	 * Lays out the passed collection.
	 * 
	 * @param nodes The collection of nodes to lay out.
	 * @return See above.
	 */
	private JPanel layoutCollection(List<TagNode> nodes)
	{
		JPanel p = new JPanel();
		if (nodes == null) return p;
		double[] tl = {TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.PREFERRED}; //columns
		TableLayout layout = new TableLayout();
		layout.setColumn(tl);
		int j = 0;
		int m = nodes.size();
		double[] rows;
		int size = m/tl.length+1;
		rows = new double[size];
		for (int i = 0; i < rows.length; i++) 
			rows[i] = TableLayout.PREFERRED;
		
		layout.setRow(rows);
		p.setLayout(layout);
		int row = -1;
		TagNode node;
		for (int i = 0; i < m; i++) {
			node = nodes.get(i);
			if (i%tl.length == 0) {
				row++;
				j = 0;
			} else j++;
			
			p.add(node, j+", "+row+", l, c");
		}
		return p;
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param tags 			Collection of tags linked to an edited object. 
	 * @param tagSets 		Collection of tag sets related to the tags. 
	 * @param availableTags	Collection of unused tags.
	 */
	private void initComponents(List<CategoryData> tags, 
								List<CategoryGroupData> tagSets, 
								List<CategoryData> availableTags)
	{
		availableTag = new JButton("My Available Tags");
		availableTag.setToolTipText("Retrieve the available tags.");
		availableTag.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				availableTag.setCursor(
						Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//controller.loadAvailableTags();
				firePropertyChange(LOAD_AVAILABLE_TAGS_PROPERTY, 
									Boolean.FALSE, Boolean.TRUE);
			}
		
		});
		Iterator i;
		TagNode n;
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.EDIT_REMOVE);
		if (tagSets != null && tagSets.size()> 0) {
			tagSetNodes = new ArrayList<TagNode>(tagSets.size());
			i = tagSets.iterator();
			CategoryGroupData data;
			while (i.hasNext()) {
				data = (CategoryGroupData) i.next();
				n = new TagNode(data, icon, TagNode.REMOVE_TYPE);
				n.addPropertyChangeListener(this);
				tagSetNodes.add(n);
			}
		}
		if (tags != null && tags.size() > 0) {
			tagNodes = new ArrayList<TagNode>(tags.size());
			i = tags.iterator();
			CategoryData data;
			while (i.hasNext()) {
				data = (CategoryData) i.next();
				n = new TagNode(data, icon, TagNode.REMOVE_TYPE);
				n.addPropertyChangeListener(this);
				tagNodes.add(n);
			}
		}
		
		if (availableTags != null && availableTags.size() > 0) {
			availableTagNodes = new ArrayList<TagNode>(availableTags.size());
			i = availableTags.iterator();
			CategoryData data;
			icon = icons.getIcon(IconManager.ADD_12);
			while (i.hasNext()) {
				data = (CategoryData) i.next();
				n = new TagNode(data, icon, TagNode.ADD_TYPE);
				n.addPropertyChangeListener(this);
				availableTagNodes.add(n);
			}
		}
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		String text = TEXT;
		if (tagNodes == null && tagSetNodes == null)
			text = TEXT_NO_TAG;
		
	    double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
	    				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
	    				TableLayout.PREFERRED, 5, TableLayout.PREFERRED} }; //rows
	    TableLayout layout = new TableLayout(tl);
	    content.setLayout(layout);
	    content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
	    if (tagNodes != null) {
	    	content.add(UIUtilities.setTextFont("Tags"), "0, 0, l, c");
	        content.add(layoutCollection(tagNodes), "1, 0, l, c");
	    }
	    
	    content.add(new JLabel(), "0, 1, 1, 1");
	    if (tagSetNodes != null) {
	    	 content.add(UIUtilities.setTextFont("Tag sets"), "0, 2, l, c");
	         content.add(layoutCollection(tagSetNodes), "1, 2, l, c");
	    }
	    content.add(UIUtilities.buildComponentPanel(availableTag), 
	    		"0, 4, l, c");
	    
	    if (availableTagNodes != null) {
	    	content.add(UIUtilities.setTextFont("Available Tags"), "0, 6, l, c");
	    	content.add(layoutCollection(availableTagNodes), "1, 6, l, c");
	    } 
	    
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    add(UIUtilities.buildComponentPanel(new JLabel(text)), 
	    	BorderLayout.NORTH);
	    add(content, BorderLayout.CENTER);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param tags 			Collection of tags linked to an edited object. 
	 * @param tagSets 		Collection of tag sets related to the tags. 
	 * @param availableTags	Collection of unused tags.
	 * @param addIcon		The icon indicating to add the tag.
	 * @param removeIcon	The icon indicating to remove the tag.
	 */
	public SingleTagEditor(List<CategoryData> tags, 
					List<CategoryGroupData> tagSets, 
					List<CategoryData> availableTags)
	{
	    initComponents(tags, tagSets, availableTags);
	    buildGUI();
	}
	
	/**
	 * Highlights the nodes.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		Iterator i;
		TagNode node;
		if (TagNode.TAG_SELECTED_PROPERTY.equals(name)) {
			TagNode refNode = (TagNode) evt.getNewValue();
			if (tagSetNodes == null) return;
			i = tagSetNodes.iterator();
			long id = -1;
			if (refNode != null) id = refNode.getObjectID();
			while (i.hasNext()) {
				node = (TagNode) i.next();
				if (refNode != null && node.containsNode(id))
					node.highLightNode();
				else node.resetNodeDisplay();
			}
		} else if (TagNode.TAG_SET_SELECTED_PROPERTY.equals(name)) {
			TagNode refNode = (TagNode) evt.getNewValue();
			if (tagNodes == null) return;
			i = tagNodes.iterator();
			while (i.hasNext()) {
				node = (TagNode) i.next();
				if (refNode != null && refNode.containsNode(node.getObjectID()))
					node.highLightNode();
				else node.resetNodeDisplay();
			}
		} else if (TagNode.BROWSE_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			firePropertyChange(BROWSE_TAG_PROPERTY, null, ho);
	
		} else if (TagNode.DELETE_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			firePropertyChange(REMOVE_TAG_PROPERTY, null, ho);
		} else if (TagNode.ADD_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			firePropertyChange(ADD_TAG_PROPERTY, null, ho);
		} else if (TagNode.EDIT_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			firePropertyChange(EDIT_TAG_PROPERTY, null, ho);
		}
	}

}