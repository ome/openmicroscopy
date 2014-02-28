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


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.util.ui.ThumbnailLabel;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A dialog to warn the user if he wants to delete images
 * which are linked to multiple datasets
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class MultiLinkNotificationDialog extends JDialog {

	/** Bound property indicating to delete the objects. */
	public static final String DELETE_PROPERTY = "delete";

	/** The title of the dialog. */
	private static final String TITLE = "Delete";

	/** The message shown in the dialog */
	private static final String MESSAGE = "These images are linked to multiple datasets.\nDeleting them will remove them from all datasets!";

	/** The button to close the dialog. */
	private JButton closeButton;

	/** The button to delete anyway */
	private JButton deleteButton;

	/** The thumbnails for the (first part of the) affected images */
	private List<ThumbnailData> imgs;
	
	/** Total number of images linked to multiple datasets */
	private int totalImageCount;

	/** Closes and disposes. */
	private void close() {
		setVisible(false);
		dispose();
	}

	/** Fires delete property and disposes */
	private void delete() {
		firePropertyChange(DELETE_PROPERTY, null, null);
		dispose();
	}

	/** Initializes the buttons. */
	private void initialize() {
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				close();
			}
		});
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				delete();
			}
		});
	}

	/**
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar() {
		JPanel bar = new JPanel();
		bar.add(deleteButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		bar.add(closeButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}

	/** Builds and lays out the UI. */
	private void buildGUI() {
		setTitle(TITLE);

		Container c = getContentPane();

		TitlePanel tp = new TitlePanel(TITLE, MESSAGE, null);
		c.add(tp, BorderLayout.NORTH);

		c.add(buildThumbnailPanel(imgs), BorderLayout.CENTER);

		c.add(buildToolBar(), BorderLayout.SOUTH);
	}

	/** Builds the panel holding the thumbnails */
	public JPanel buildThumbnailPanel(List<ThumbnailData> imgs) {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		FlowLayout l = new FlowLayout(FlowLayout.LEFT);
		l.setHgap(3);
		l.setVgap(3);
		p.setLayout(l);
		
		p.setPreferredSize(new Dimension(100, 150));
		
		ThumbnailLabel label;
		for (ThumbnailData img : imgs) {
			label = new ThumbnailLabel();
			label.setData(img);
			label.setToolTipText("");
			p.add(label);
		}
		
		if(imgs.size()<totalImageCount) {
			p.add(new JLabel("... ("+(totalImageCount-imgs.size())+" others)"));
		}
		
		return p;
	}

	/**
	 * Creates a new instance
	 * 
	 * @param owner The owner of the dialog.
	 * @param imgs Thumbnails of the (first part of the) images linked to multiple datasets
	 * @param totalImageCount Total number of images linked to multiple datasets
	 */
	public MultiLinkNotificationDialog(JFrame owner, List<ThumbnailData> imgs, int totalImageCount) {
		super(owner);
		if (CollectionUtils.isEmpty(imgs))
			throw new IllegalArgumentException("No result to display");
		this.imgs = imgs;
		this.totalImageCount = totalImageCount;
		initialize();
		buildGUI();
		pack();
	}

}
