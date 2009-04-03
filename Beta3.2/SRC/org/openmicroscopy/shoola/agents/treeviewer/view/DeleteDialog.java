/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.DeleteDialog 
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class DeleteDialog 
	extends JDialog
{

	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The title of the dialog. */
	private static final String		TITLE = "Confirm delete";
	
	/** The question asked. */
	private static final String		TEXT = "Are you sure you want to delete " +
			"the selected objects.";
	
	/** User have selected the yes option. */
	public final static int			YES_OPTION = 1;

	/** User have selected the no option. */
	public final static int			NO_OPTION = 0;
	
	/** Indicates to delete the elements in the container. */
	public final static int			WITH_CONTENT = 0;
	
	/** Indicates not to delete the elements in the container. */
	public final static int			WITHOUT_CONTENT = 1;
	
	/** Controls to ask a confirmation question */
	private JButton         noButton;
	
	/** Controls to ask a confirmation question */
	private JButton			yesButton;
	
	/** Delete the objects and the contents. */
	private JRadioButton 	withContent;
	
	/** Delete the objects but not the contents. */
	private JRadioButton 	withoutContent;
	
	/** Either {@link #YES_OPTION} or {@link #NO_OPTION}. */
	private int				option;
	
    /** Hides the dialog and disposes of it. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		withContent = new JRadioButton("Also delete contents");
		withoutContent = new JRadioButton("Do not delete contents");
		ButtonGroup group = new ButtonGroup();
		group.add(withContent);
		group.add(withoutContent);
		withoutContent.setSelected(true);
		noButton = new JButton("No");
        yesButton = new JButton("Yes");
        noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	option = NO_OPTION; 
            	close();
            }
        });
        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	option = YES_OPTION; 
            	close();
            }
        });
        getRootPane().setDefaultButton(yesButton);
	}
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/**
	 * Builds the panel hosting the components.
	 * 
	 * @return See above.
	 */
	private JPanel buildControlPanel()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		controlPanel.add(yesButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		controlPanel.add(noButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(controlPanel);
	}
	
	/** 
	 * Returns the component hosting the various choices.
	 * 
	 * @return See above.
	 */
	private JPanel buildMainPane()
	{
		JPanel p = new JPanel();
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.QUESTION);
		double size[][] =  {{icon.getIconWidth(), 10, TableLayout.FILL}, // columns
              {TableLayout.PREFERRED, 10, TableLayout.PREFERRED}}; // rows
		p.setLayout(new TableLayout(size));
		p.add(new JLabel(icon), "0, 0");
		p.add(new JLabel(TEXT), "2, 0");
		JPanel choice = new JPanel();
		choice.setLayout(new BoxLayout(choice, BoxLayout.Y_AXIS));
		choice.add(withContent);
		choice.add(withoutContent);
		
		p.add(choice, "0, 2, 2, 2");
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		//c.add(buildMainPane());
		//Tmp
		JLabel l = new JLabel();
		l.setText("<html><body>Limitation: Only the selected container will be <br>" +
				"deleted not the elements contained in it</body></html>");
		body.add(UIUtilities.buildComponentPanel(l));
		body.add(buildControlPanel());
		add(body);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the frame
	 */
	public DeleteDialog(JFrame parent)
	{
		super(parent);
		initComponents();
		setProperties();
		buildGUI();
		pack();
	}
	
	/**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int showMsgBox()
    {
    	setLocation(this.getParent().getLocation());
    	setVisible(true);
    	return option;	
    }
   
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int centerMsgBox()
    {
    	UIUtilities.centerAndShow(this);
    	return option;	
    }
    
    /**
     * Returns either {@link #WITH_CONTENT} or {@link #WITHOUT_CONTENT}.
     * 
     * @return See above.
     */
    public int getSaveType()
    {
    	if (withContent.isSelected()) return WITH_CONTENT;
    	return WITHOUT_CONTENT;
    }
    
}
