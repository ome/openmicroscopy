/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOTag 
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
package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
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
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.ui.TagNode;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * Component displaying the tags.
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
class DOTag
	extends JPanel
	implements PropertyChangeListener
{
	
	/** The separator between the tags and tag sets. */
	private static final String	SEPARATOR = "|";

	/** Text indicating the describing the component. */
	private static final String TEXT = "The list of tags linked to the " +
			"image. Click on the name to browse the tag " +
			"or the tag set.";
	
	/** Text indicating the describing the component. */
	private static final String TEXT_NO_TAG = "The image has no tags.";
	
    /** Reference to the Model. */
    private EditorModel     model;
    
    /** Reference to the control. */
    private EditorControl	controller;
    
    /** Collection of nodes hosting the tag sets. */
    private List<TagNode>	tagSetNodes;
    
    /** Collection of nodes hosting the tags. */
    private List<TagNode>	tagNodes;
    
    /** Collection of nodes hosting the available tags. */
    private List<TagNode>	availableTagNodes;
    
    /** Button to retrieve the available tags. */
    private JButton			availableTag;
    
    /** 
     * Builds and lays out the panel hosting the tags. 
     * 
     * @return See above.
     */
    private JPanel buildTagsPanel()
    {
    	JPanel p = new JPanel();
    	if (tagNodes != null) {
    		Iterator i = tagNodes.iterator();
    		int index = 0;
    		int l = tagNodes.size()-1;
    		while (i.hasNext()) {
    			p.add((TagNode) i.next());
				if (index != l) p.add(new JLabel(SEPARATOR));
				index++;
			}
    	}
    	return p;
    }
    
    /** 
     * Builds and lays out the panel hosting the tags. 
     * 
     * @return See above.
     */
    private JPanel buildAvailableTagsPanel()
    {
    	JPanel p = new JPanel();
    	if (availableTagNodes != null) {
    		Iterator i = availableTagNodes.iterator();
    		int index = 0;
    		int l = availableTagNodes.size()-1;
    		while (i.hasNext()) {
    			p.add((TagNode) i.next());
				if (index != l) p.add(new JLabel(SEPARATOR));
				index++;
			}
    	}
    	return p;
    }
    
    /** 
     * Builds and lays out the panel hosting the tag sets. 
     * 
     * @return See above.
     */
    private JPanel buildTagSetsPanel()
    {
    	JPanel p = new JPanel();
    	if (tagSetNodes != null) {
    		Iterator i = tagSetNodes.iterator();
    		int index = 0;
    		int l = tagSetNodes.size()-1;
    		while (i.hasNext()) {
    			p.add((TagNode) i.next());
    			if (index != l) p.add(new JLabel(SEPARATOR));
				index++;
			}
    	}
    	return p;
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
    	availableTag = new JButton("Available Tags");
    	availableTag.setToolTipText("Retrieve the available tags.");
    	availableTag.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				controller.loadAvailableTags();
			}
		
		});
    	List<CategoryGroupData> tagSets = model.getTagSets();
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
    	List<CategoryData> tags = model.getTags();
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
    	tags = model.getAvailableTags();
    	
    	if (tags != null && tags.size() > 0) {
    		availableTagNodes = new ArrayList<TagNode>(tags.size());
    		i = tags.iterator();
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
    void buildGUI()
    {
    	String text = TEXT;
    	if (tagNodes == null && tagSetNodes == null)
    		text = TEXT_NO_TAG;
    	JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
        				TableLayout.PREFERRED, 5, TableLayout.PREFERRED} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        if (tagNodes != null) {
        	content.add(UIUtilities.setTextFont("Tags"), "0, 0, l, c");
            content.add(buildTagsPanel(), "1, 0, l, c");
        }
        
        content.add(new JLabel(), "0, 1, 1, 1");
        if (tagSetNodes != null) {
        	 content.add(UIUtilities.setTextFont("Tag sets"), "0, 2, l, c");
             content.add(buildTagSetsPanel(), "1, 2, l, c");
        }
        content.add(UIUtilities.buildComponentPanel(availableTag), 
        			"0, 4, l, c");
        if (availableTagNodes != null) {
        	content.add(UIUtilities.setTextFont("Available Tags"), "0, 6, l, " +
        				"c");
            content.add(buildAvailableTagsPanel(), "1, 6, l, c");
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(new JLabel(text)), 
        	BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller	Reference to the control. 
     * 						Mustn't be <code>null</code>.
     * @param model			Reference to the model. 
     * 						Mustn't be <code>null</code>.
     */
	DOTag(EditorControl controller, EditorModel model)
	{
		if (controller == null)
            throw new IllegalArgumentException("No controller.");
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.controller = controller;
        this.model = model;
        initComponents();
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
			Registry reg = TreeViewerAgent.getRegistry();
			
			ExperimenterData exp = (ExperimenterData) reg.lookup(
										LookupNames.CURRENT_USER_DETAILS);
			int index = Browse.CATEGORY;;
			if (ho instanceof CategoryGroupData)
				index = Browse.CATEGORY_GROUP;
			Browse event = new Browse(ho.getId(), index, exp, null);
			reg.getEventBus().post(event);
		} else if (TagNode.DELETE_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			controller.removeTag(ho);
		} else if (TagNode.ADD_PROPERTY.equals(name)) {
			DataObject ho = (DataObject) evt.getNewValue();
			if (ho == null) return;
			controller.addTagToImage(ho);
		}
	}
	
}
