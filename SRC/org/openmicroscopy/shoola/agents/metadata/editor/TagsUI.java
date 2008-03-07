/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TagsUI 
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;

/** 
 * UI component displaying the tags.
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
class TagsUI 
	extends AnnotationUI 
	implements ActionListener
{

	/** The title associated to this component. */
	private static final String TITLE = "Tags ";
	
	/** Action id indicating to add new url area. */
	private static final String	ADD_ACTION = "add";
	
	/** Action id indicating to add new url area. */
	private static final String	NEW_ACTION = "new";
	
	/** Button to add existing tags. */
	private JButton					addButton;
	
	/** Button to create new tags. */
	private JButton					newButton;
	
    /** Area where to enter the name of the tags. */
    private JTextField				nameArea;
     
    /** Area where to enter the description of the tags. */
    private JTextArea				descriptionArea; 
    
    /** Collection of tags to add. */
    private List<AnnotationData>	addedTags;
    
    /** Collection of tags to remove. */
    private List<AnnotationData>	removedTags;
    
    /** Collection of selected tags. */
    private List<AnnotationData>	selectedTags;
    
    /** Menu used to manage the tags. */
    private TagPopupMenu			menu;
    
	/** Initializes the UI components. */
	private void initComponents()
	{
		addButton = new JButton("Add...");
		addButton.setActionCommand(ADD_ACTION);
		addButton.addActionListener(this);
		newButton = new JButton("New...");
		newButton.setActionCommand(NEW_ACTION);
		newButton.addActionListener(this);
		nameArea = new JTextField();
		descriptionArea = new JTextArea();
		descriptionArea.setBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		addedTags = new ArrayList<AnnotationData>();
		removedTags = new ArrayList<AnnotationData>();
		selectedTags = new ArrayList<AnnotationData>();
	}
	
	/** 
	 * Lays out the newly created tags.
	 * 
	 * @return See above.
	 */
	private JPanel layoutNewTags()
	{
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 60} }; //rows
		p.setLayout(new TableLayout(tl));
		p.add (new JLabel("New Tags"), "0, 0");
		p.add(nameArea, "2, 0");
		p.add (new JLabel("Description"), "0, 2");
		p.add(descriptionArea, "2, 2, 2, 3");
		return p;
	}
	
	/** 
	 * Lays out the tags associated to the object.
	 * 
	 * @return See above.
	 */
	private JPanel layoutExistingTags()
	{
		JPanel p = new JPanel();
		JLabel label = new JLabel("Tags: ");
		Collection l = model.getTags();
		Iterator i;
		TagComponent comp;
		p.add(label);
		AnnotationData data;
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!removedTags.contains(data)) {
					comp = new TagComponent(this, data);
					p.add(comp);
				}
			}
		}
		
		i = addedTags.iterator();
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (!removedTags.contains(data)) {
				comp = new TagComponent(this, data);
				comp.setForeground(Color.RED);
				p.add(comp);
			}
		}
		return UIUtilities.buildComponentPanel(p);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	TagsUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
	}
	
	/**
	 * Sets the selected tag.
	 * 
	 * @param isShiftDown	Pass <code>true</code> if the <code>Shift</code>
	 * 						is down, to allow multi selections. 
	 * 						Pass <code>false</code> otherwise.
	 * @param data			The selected annotation.
	 */
	void setSelectedTag(boolean isShiftDown, AnnotationData data)
	{
		if (!isShiftDown) selectedTags.clear();
		selectedTags.add(data);
	}
	
	/**
	 * Edits the passed annotation only if the tag.
	 * 
	 * @param data The tag to edit.
	 */
	void editAnnotation(AnnotationData data)
	{
		//if (!model.isCurrentUserOwner(data)) return;
	}
	
	/**
	 * Shows the tag menu.
	 * 
	 * @param location	The location of the mouse click.
	 * @param invoker	The last selected component.
	 */
	void showMenu(Point location, Component invoker)
	{
		if (menu == null)
			menu = new TagPopupMenu(this);
		menu.show(invoker, location.x, location.y);
	}
	
	/** Removes the selected tags. */
	void removeSelectedTags()
	{
		removedTags.clear();
		removedTags.addAll(selectedTags);
		selectedTags.clear();
		buildUI();
	}
	
	/** Edits the selected tags. */
	void editSelectedTags()
	{
		
	}
	
	/** Browses the selected tags. */
	void browseSelectedTags()
	{
		//TODO implements that code.
		selectedTags.clear();
	}
	
	/** Shows the collection of existing tags. */
	void showSelectionWizard()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Collection l = model.getExistingTags();
		Registry reg = MetadataViewerAgent.getRegistry();
		if (l.size() == 0) {
			UserNotifier un = reg.getUserNotifier();
			un.notifyInfo("Existing Tags", "No tags found.");
			return;
		}
		SelectionWizard wizard = new SelectionWizard(
										reg.getTaskBar().getFrame(), l);
		IconManager icons = IconManager.getInstance();
		wizard.setTitle("Tags Selection" , "Select existing tags", 
				icons.getIcon(IconManager.TAGS_48));
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		int n = model.getTagsCount();
		title = TITLE+LEFT+n+RIGHT;
		Border border = new TitledLineBorder(title, getBackground());
		setBorder(border);
		getCollapseComponent().setBorder(border);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(layoutExistingTags());
		add(new JSeparator());
		add(Box.createVerticalStrut(5));
		add(layoutNewTags());
		add(UIUtilities.buildComponentPanel(addButton));
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Returns the collection of tags to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{ 
		return removedTags; 
	}

	/**
	 * Returns the collection of tags to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{ 
		//some work to do here.
		return addedTags; 
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (removedTags.size() > 0) return true;
		if (getAnnotationToSave().size() > 0) return true;
		return false;
	}
	
	/**
	 * Brings up the selection wizard to select existing tags.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			Collection l = model.getExistingTags();
			if (l == null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				model.loadExistingTags();
				return;
			}
			showSelectionWizard();
		} 
	}
	
}
