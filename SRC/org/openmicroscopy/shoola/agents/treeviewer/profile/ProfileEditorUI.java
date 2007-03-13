/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorUI 
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
package org.openmicroscopy.shoola.agents.treeviewer.profile;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The {@link ProfileEditor}'s View.
 * Displays the UI components used to manage user's details.
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
class ProfileEditorUI 
	extends JPanel
{

	/** The title of the dialog displayed if a problem occurs. */
	static final String				DIALOG_TITLE = "Change Password";
	
	/** Title of the UI. */
	private static final String		TITLE = "Profile Editor";
	
	/** Text of the UI. */
	private static final String		NOTE = "Edit My Profile";
	
	/** Title of the paned hosting the user details. */
	private static final String		USER_TITLE = "My Profile";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** Reference to the Model. */
	private ProfileEditorModel		model;
	
	/** Reference to the Control. */
	private ProfileEditorControl	controller;
	
    /** Button to finish the operation e.g. create, edit, etc. */
    private JButton         		finishButton;
    
    /** Button to cancel the object creation. */
    private JButton         		cancelButton;
    
    /** 
     * The tabbed pane hosting the various components if we are in the 
     * <code>Editor</code> mode.
     */
    private JTabbedPane     		tabs;
    
    /** Component hosting the user details. */
    private UserProfile				userProfile;

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
               controller.close();
            }
        });
        finishButton = new JButton("Save");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
            	//finish(); 
            }
        });
        userProfile = new UserProfile(model, controller);
	}
	
	/**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        //bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        return bar;
    }
    
    /** Builds and lays out the tabbed pane. */
    private void buildTabbedPane()
    {
    	IconManager icons = IconManager.getInstance();
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addTab(USER_TITLE, icons.getIcon(IconManager.OWNER), userProfile);
    }
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		buildTabbedPane();
		setLayout(new BorderLayout(0, 0));
		setOpaque(true);
		IconManager im = IconManager.getInstance();
		TitlePanel titlePanel = new TitlePanel(TITLE, NOTE, 
				im.getIcon(IconManager.OWNER_48));
		add(titlePanel, BorderLayout.NORTH);

		add(tabs, BorderLayout.CENTER);
		JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
		p.setOpaque(true);
		add(p, BorderLayout.SOUTH);
	}
	
	/** Creates a new instance. */
	ProfileEditorUI()
	{
		
	}
	
    /**
     * Links MVC.
     * 
     * @param controller    Reference to the control.
     *                      Mustn't be <code>null</code>.   
     * @param model         Reference to the control. 
     *                      Mustn't be <code>null</code>.   
     */
	void initialize(ProfileEditorModel model, ProfileEditorControl controller)
	{
		if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.controller = controller;
        this.model = model;
        initComponents();
        buildGUI();
	}

	/** 
	 * Indicates to update the View when the password was 
	 * successfully updated. 
	 */
	void passwordChanged() { userProfile.passwordChanged(); }

}
