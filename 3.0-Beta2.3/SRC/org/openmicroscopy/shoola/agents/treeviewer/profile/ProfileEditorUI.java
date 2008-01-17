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
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.login.ServerEditor;

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
	
	/** Title of the component hosting the disk space. */
	private static final String				DISK_TITLE = "Disk Space";
	
	/** Title of the UI. */
	private static final String		TITLE = "Profile Editor";
	
	/** Text of the UI. */
	private static final String		NOTE = "Edit My Profile";
	
	/** Title of the component hosting the user details. */
	private static final String		USER_TITLE = "My Profile";
	
	/** Title of the component hosting the servers' details used by user. */
	private static final String		SERVER_TITLE = "My Servers";
	
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
    
    /** The component hosting the title and the messages. */
    private JLayeredPane    		titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      		titlePanel;
    
    /** Component hosting the user details. */
    private UserProfile				userProfile;

    /** Component hosting the server details. */
    private ServerEditor			serverProfile;
    
    /** Component displaying the used and free space on file system. */
    private DiskSpace				diskSpace;
    
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
        serverProfile = new ServerEditor(model.getHostName());
        serverProfile.initFocus();
        diskSpace = new DiskSpace();
        IconManager im = IconManager.getInstance();
		titlePanel = new TitlePanel(TITLE, NOTE, 
									im.getIcon(IconManager.OWNER_48));
		titleLayer = new JLayeredPane();
		titleLayer.add(titlePanel, new Integer(0));
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addTab(USER_TITLE, im.getIcon(IconManager.OWNER), userProfile);
        tabs.addTab(SERVER_TITLE, im.getIcon(IconManager.SERVER), 
        			serverProfile);
        tabs.addTab(DISK_TITLE, im.getIcon(IconManager.DISK_SPACE), diskSpace);
        tabs.addChangeListener(new ChangeListener() {
		
			public void stateChanged(ChangeEvent e) {
				
				switch (tabs.getSelectedIndex()) {
					case 1:
						serverProfile.setVisible(true);
						serverProfile.initFocus();
						break;
					case 2:
						controller.getDiskSpace();
						diskSpace.buildGUI();
				}
			}
		
		});
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
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		setOpaque(true);
		add(titleLayer, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
		p.setOpaque(true);
		add(p, BorderLayout.SOUTH);
	}
	
	/** Creates a new instance. */
	ProfileEditorUI() {}
	
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
        serverProfile.addPropertyChangeListener(controller);
        buildGUI();
	}

	/** 
	 * Indicates to update the View when the password was 
	 * successfully updated. 
	 */
	void passwordChanged() { userProfile.passwordChanged(); }
	
	/**
	 * Adds the passed component to the title layer if the passed flag
	 * is <code>true</code>, removes it otherwise.
	 * 
	 * @param b			Pass <code>true</code> to add the component, 
	 * 					<code>false</code> to remove it.
	 * @param component	The component to add.
	 */
	void showMessage(boolean b, JComponent component) 
	{
		if (b) {
            titleLayer.add(component, new Integer(1));
            titleLayer.validate();
            titleLayer.repaint();
        } else {
        	if (component == null) return;
        	titleLayer.remove(component);
            titleLayer.repaint();
        }
	}
	
	/**
	 * Sets the free and used disk space on the file system.
	 * 
	 * @param free 	The free space on the file system.
	 * @param used	The used space on the file system.
	 */
	void setDiskSpace(long free, long used)
	{
		diskSpace.buildGraph(used, free);
	}
	
    /**
     * Overridden to set the size of the title panel.
     * @see JPanel#setSize(int, int)
     */
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        Dimension d  = new Dimension(width, ServerEditor.TITLE_HEIGHT);
        titlePanel.setSize(d);
        titlePanel.setPreferredSize(d);
        titleLayer.setSize(d);
        titleLayer.setPreferredSize(d);
    }

    /**
     * Overridden to set the size of the title panel.
     * @see JPanel#setSize(Dimension)
     */
    public void setSize(Dimension d) { setSize(d.width, d.height); }

}
