package util.ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.tree.DefaultMutableTreeNode;

import omero.model.Dataset;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.service.ServiceFactory;


/*
 * .DatasetWindow 
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

//Java imports
//Third-party libraries
//Application-internal dependencies
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DatasetBrowser
	extends JDialog implements ActionListener
{	
	DatasetTree datasetTree; 
	long value;
	
	public DatasetBrowser(ServiceFactory sf)
	{
		super();
		this.setName("Select Dataset");
		buildUI(sf);
		this.setModal(true);
		this.setAlwaysOnTop(true);
		this.setSize(300,400);
	}
	
	private void buildUI(ServiceFactory sf)
	{
		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(panel);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout());
		try
		{
			JPanel tree;
			datasetTree = new DatasetTree(sf);
			tree = datasetTree.createTree();
			tree.setBorder(BorderFactory.createEtchedBorder());
			tree.setBackground(tree.getBackground());
			panel.add(tree, BorderLayout.CENTER);
		}
		catch (DSOutOfServiceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (DSAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JPanel bottomPanel = new JPanel();
		JButton selectBtn = new JButton("Select");
		selectBtn.addActionListener(this);
		selectBtn.setActionCommand("select");
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(this);
		cancelBtn.setActionCommand("cancel");
		bottomPanel.setLayout(new FlowLayout());
		bottomPanel.add(new JPanel());
		bottomPanel.add(selectBtn);
		bottomPanel.add(cancelBtn);
		bottomPanel.add(new JPanel());
		
		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("select"))
		{
			DefaultMutableTreeNode node = datasetTree.getSelection();
			Object o = node.getUserObject();
			if(o instanceof Dataset)
			{
				Dataset d = (Dataset)o;
				value = d.id.val;
				this.setVisible(false);
			}
		}
		else
		{
			value = -1;
			this.setVisible(false);
		}
	}
		
	public long showDialog()
	{
		centerOnScreen(this);
		this.setVisible(true);
		return value;
	}
	
	public static void centerOnScreen(Component window)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle ed = window.getBounds();
		window.setLocation((screenSize.width-ed.width)/2, 
							(screenSize.height-ed.height)/2);
	}
	
}


