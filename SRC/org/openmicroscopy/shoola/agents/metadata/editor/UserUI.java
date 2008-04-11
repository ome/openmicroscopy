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
import java.util.List;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TreeComponent;
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

	/** The title associated to that component. */
	private static final String TITLE = "Profile";
	
	/** The component displaying the user profile. */
	private UserProfile 	profile;
	
	/** Component displaying the disk space. */
	private UserDiskSpace 	diskSpace;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param control	Reference to the control.
	 */
	private void initComponents(EditorControl control)
	{
		profile = new UserProfile(model);
		profile.addPropertyChangeListener(control);
		diskSpace = new UserDiskSpace(model);
		
		TreeComponent tree = new TreeComponent();
		tree.insertNode(diskSpace, diskSpace.getCollapseComponent(), false);
		tree.addPropertyChangeListener(TreeComponent.EXPANDED_PROPERTY, this);
		double[][] size = {{TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(profile, "0, 0");
		add(tree, "0, 1");
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
	 * No-op implementation in our case.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData() {}

	/**
	 * Removes all components.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() { removeAll(); }

	/**
	 * No-op implementation in our case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * No-op implementation in our case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }

	/**
	 * Returns the title associated to this component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	/**
	 * Returns <code>true</code> if user's info has been modified, 
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave() { return profile.hasDataToSave(); }

	/**
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
			boolean b = (Boolean) evt.getNewValue();
			List l = model.isDiskSpaceLoaded();
			if (model.getRefObject() instanceof ExperimenterData) {
				if (l == null) {
					if (b) model.loadDiskSpace();
					else model.cancelDiskSpaceLoading();
				}
			}
			diskSpace.buildGUI();
		}
	}

}
