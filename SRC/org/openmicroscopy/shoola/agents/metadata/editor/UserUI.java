/*
 * org.openmicroscopy.shoola.agents.metadata.editor.UserUI 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;

import layout.TableLayout;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ExperimenterData;

/** 
 * Component displaying the user's details.
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
class UserUI 
	extends AnnotationUI
	implements PropertyChangeListener
{

	/** The title of the chart. */
	static final String TITLE = "Disk Space";

	/** The title of the chart. */
	private static final String TITLE_DETAILS = "User's details";
	
	/** The component displaying the user profile. */
	private UserProfile 	profile;
	
	/** Component displaying the disk space. */
	private UserDiskSpace 	diskSpace;
	
	/** The tree hosting the {@link #UserDiskSpace}. */
	private JXTaskPane		diskTask;
	
	/** The disk space. */
	private List			space;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param control	Reference to the control.
	 */
	private void initComponents(EditorControl control)
	{
		profile = new UserProfile(model);
		profile.addPropertyChangeListener(control);
		
		JXTaskPane pane = EditorUtil.createTaskPane(TITLE_DETAILS);
		//pane.setCollapsed(false);
		pane.add(profile, null, 0);
		
		diskSpace = new UserDiskSpace(this);
		diskTask = EditorUtil.createTaskPane(TITLE);
		diskTask.add(diskSpace, null, 0);
		diskTask.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
		
		double[][] size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(pane, "0, 0");
		add(diskTask, "0, 1");
		
		/*
		JXTaskPaneContainer container = new JXTaskPaneContainer();
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new VerticalLayout(2));
		container.add(pane);
		container.add(diskTask);
		setBorder(BorderFactory.createEmptyBorder());
		add(container);
		*/
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	Reference to the model. Mustn't be <code>null</code>.
	 * @param control	Reference to the control. Mustn't be <code>null</code>.
	 */
	UserUI(EditorModel model, EditorControl control)
	{
		super(model);
		if (control == null)
			throw new IllegalArgumentException("No control.");
		initComponents(control);
	}
	
	/** Clears the password fields. */
	void passwordChanged() { profile.passwordChanged(); }

	/**
	 * Returns the experimenter to save.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenterToSave()
	{
		return profile.getExperimenterToSave();
	}
	
	/**
	 * Sets the disk space information.
	 * 
	 * @param space The value to set.
	 */
	void setDiskSpace(List space) { this.space = space; }
	
	/** 
	 * Returns the list with disk space information.
	 * 
	 * @return See above.
	 */
	List isDiskSpaceLoaded() { return space; }

	/**
	 * Overridden to lay out the UI.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		profile.buildGUI();
		diskSpace.buildGUI();
		revalidate();
		repaint();
	}

	/**
	 * Removes all components.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		space = null;
		clearDisplay();
	}

	/**
	 * Removes all components.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay()
	{ 
		diskSpace.removeAll();
		profile.removeAll();
		diskTask.setCollapsed(true);
		removeAll();
		diskSpace.revalidate();
		profile.revalidate();
		revalidate();
		repaint();
	}

	/**
	 * No-operation implementation in our case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{ 
		return new ArrayList<AnnotationData>();  
	}

	/**
	 * No-operation implementation in our case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{ 
		return new ArrayList<AnnotationData>(); 
	}

	/**
	 * Returns the title associated to this component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return ""; }

	/**
	 * Returns <code>true</code> if user's info has been modified, 
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave() { return profile.hasDataToSave(); }

	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle() {}
	
	/**
	 * Loads the data if expanding, cancels any on-going loading otherwise.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (!(model.getRefObject() instanceof ExperimenterData)) return;
		if (diskTask.isCollapsed()) model.cancelDiskSpaceLoading();
		else model.loadDiskSpace();
	}

}
