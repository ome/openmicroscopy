/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.MIFNotificationDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.ui.ThumbnailLabel;
import org.openmicroscopy.shoola.env.data.model.MultiDatasetImageLinkResult;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;

//Third-party libraries

/**
 * Notifies the user that some images he wants to delete are 
 * linked to multiple DataSets
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LinkNotificationDialog
	extends JDialog
{

        /** The maximum number of datasets to show per image */
        private static final int MAX_DATASETS_PER_IMAGE = 3;
        
	/** Bound property indicating to move all the objects.*/
	public static final String DELETE_PROPERTY = "delete";
	
	/** The title of the dialog if it is a <code>Delete</code> action.*/
	private static final String TITLE = "Delete";
	
	private static final String MESSAGE = "These images are linked to multiple datasets.\nDeleting them will remove them from all datasets!";
	
	/** The button to close the dialog.*/
	private JButton closeButton;
	
	/** The button to close the dialog.*/
        private JButton deleteButton;
        
	/** The result to display.*/
	private MultiDatasetImageLinkResult result;
	
	/** Closes and disposes.*/
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Initializes the component.*/
	private void initialize()
	{
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			
			/** 
			 * Closes the dialog.
			 */
			public void actionPerformed(ActionEvent evt) { close(); }
		});
		
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
                        
                        /** 
                         * Closes the dialog.
                         */
                        public void actionPerformed(ActionEvent evt) { firePropertyChange(DELETE_PROPERTY, null, null); }
                });
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(deleteButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		bar.add(closeButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI.*/
	private void buildGUI()
	{
	        Icon dsIcon = IconManager.getInstance().getIcon(IconManager.DATASET);
	        Icon linkIcon = IconManager.getInstance().getIcon(IconManager.FORWARD_NAV);
	        
		String title = TITLE;
		setTitle(title);
		
		TitlePanel tp = new TitlePanel(title, MESSAGE, null);
		
		Container c = getContentPane();
		c.add(tp, BorderLayout.NORTH);
		
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setAlignmentY(0);
		p.setAlignmentX(0);
		for(long imgId : result.getImageIds()) {
		    JPanel row = new JPanel();
		    row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		    
		    ThumbnailLabel label = new ThumbnailLabel();
                    label.setData(result.getThumbnail(imgId));
                    label.setToolTipText("");
                    row.add(label);
                    
                    row.add(Box.createRigidArea(new Dimension(10,0)));
                    
                    row.add(new JLabel(linkIcon));
                    
                    row.add(Box.createRigidArea(new Dimension(10,0)));
                    
                    JPanel dsPanel = new JPanel();
                    dsPanel.setLayout(new BoxLayout(dsPanel, BoxLayout.Y_AXIS));
                    int i = 0;
                    for(DatasetData ds : result.getDatasets(imgId)) {
                        if(i<MAX_DATASETS_PER_IMAGE) {
                            JLabel dsLabel = new JLabel(dsIcon);
                            dsLabel.setText(ds.getName());
                            dsPanel.add(dsLabel);
                        }
                        else {
                            JLabel dsLabel = new JLabel("... ("+(result.getDatasets(imgId).size()-i)+" more)");
                            dsPanel.add(dsLabel);
                            break;
                        }
                        i++;
                    }
                    row.add(dsPanel);
                    row.add(Box.createHorizontalGlue());
                    p.add(row);
                    p.add(Box.createRigidArea(new Dimension(0,10)));
		}
		p.add(Box.createVerticalGlue());
		
		JScrollPane sp = new JScrollPane(p);
		c.add(sp, BorderLayout.CENTER);
		
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param result The result of the Image-Dataset-Linkcheck.
	 */
	public LinkNotificationDialog(JFrame owner, MultiDatasetImageLinkResult result)
	{
		super(owner);
		if (result.getImageIds().isEmpty())
			throw new IllegalArgumentException("No result to display");
		this.result = result;
		initialize();
		buildGUI();
		pack();
		
	}
	
}
