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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.TagAnnotationData;

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
	private static final String TITLE = "Tags";
	
	/** Action id indicating to add new url area. */
	private static final String	ADD_ACTION = "add";
	
	/** Action id indicating to add new url area. */
	private static final String	NEW_ACTION = "new";
	
	/** Button to add existing tags. */
	private JButton		addButton;
	
	/** Button to create new tags. */
	private JButton		newButton;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		addButton = new JButton("Add...");
		addButton.setActionCommand(ADD_ACTION);
		addButton.addActionListener(this);
		newButton = new JButton("New...");
		newButton.setActionCommand(NEW_ACTION);
		newButton.addActionListener(this);
	}
	
	/** 
	 * Layouts the tags associated to the object.
	 * 
	 * @return See above.
	 */
	private JPanel layoutTags()
	{
		JPanel p = new JPanel();
		return p;
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
		
		add(addButton);
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			List<Object> l = new ArrayList<Object>();
			l.add(new TagAnnotationData("tag one"));
			l.add(new TagAnnotationData("tag two"));
			SelectionWizard wizard = new SelectionWizard(new JFrame(), l);
			IconManager icons = IconManager.getInstance();
			wizard.setTitle("Tags Selection" , "text goes there", 
					icons.getIcon(IconManager.TAGS_48));
			UIUtilities.centerAndShow(wizard);
		} else if (NEW_ACTION.equals(s)) {
			
		}
	}
	
}
