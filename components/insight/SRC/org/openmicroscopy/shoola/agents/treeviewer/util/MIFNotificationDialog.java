/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.MIFNotificationDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


//Third-party libraries
import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.ImageChecker;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ThumbnailLabel;
import org.openmicroscopy.shoola.env.data.model.MIFResultObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.GroupData;
import pojos.ImageData;

/**
 * Notifies the user that MIF could not be deleted or moved.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class MIFNotificationDialog
	extends JDialog
{

	/** The title of the dialog if it is a <code>Delete</code> action.*/
	private static final String TITLE_DELETE = "Delete";
	
	/** The title of the dialog if it is a <code>Change group</code> action.*/
	private static final String TITLE_CHGRP = "Move to Group: ";
	
	/** The button to close the dialog.*/
	private JButton closeButton;
	
	/** The button to move all the objects the dialog.*/
	private JButton moveButton;
	
	/** The index indicating the type of dialog to bring up.*/
	private int index;
	
	/** The result to display.*/
	private List<MIFResultObject> result;
	
	/** The action to do if the action continues.*/
	private Object action;
	
	/** The available groups.*/
	private Collection groups;
	
	/** Closes and disposes.*/
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Closes and disposes.*/
	private void move()
	{
		//Fire property
		close();
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
		moveButton = new JButton("Move All");
		moveButton.addActionListener(new ActionListener() {
			
			/** 
			 * Moves the MIF
			 */
			public void actionPerformed(ActionEvent evt) { move(); }
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
		if (index == ImageChecker.CHGRP) {
			bar.add(moveButton);
			bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		}
		bar.add(closeButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI.*/
	private void buildGUI()
	{
		String title = TITLE_DELETE;
		StringBuffer buf = new StringBuffer();
		
		if (index == ImageChecker.CHGRP) {
			ChgrpObject object = (ChgrpObject) action;
			GroupData group = object.getGroupData();
			StringBuffer buffer = new StringBuffer();
			buffer.append(TITLE_CHGRP);
			buffer.append(group.getName());
			title = buffer.toString();
			buf.append("Multi-image filesets cannot be split between 2 groups.");
		} else {
			buf.append("Multi-image filesets cannot be partially deleted.");
		}
		
		
		TitlePanel tp = new TitlePanel(title, buf.toString(), null);
		Container c = getContentPane();
		c.add(tp, BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		Iterator<MIFResultObject> i = result.iterator();
		int n = result.size();
		while (i.hasNext())
			p.add(layoutMIFResult(i.next(), n));
		
		c.add(p, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Layout the result per group.
	 * 
	 * @param object The object to handle
	 * @param n The number of objects to handle.
	 * @return See above.
	 */
	private JComponent layoutMIFResult(MIFResultObject object, int n)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		Map<Long, Map<Boolean, List<ImageData>>> map = object.getResult();
		
		Entry<Long, Map<Boolean, List<ImageData>>> e;
		Iterator<Entry<Long, Map<Boolean, List<ImageData>>>> i =
				map.entrySet().iterator();
		List<ThumbnailData> thumbnails;
		JPanel row;
		List<ImageData> images;
		StringBuffer buf;
		String text = "You tried to move ";
		if (index == ImageChecker.DELETE)
			text = "You tried to delete ";
		int size;
		while (i.hasNext()) {
			e = i.next();
			buf = new StringBuffer();
			buf.append(text);
			
			row = createRow();
			images = e.getValue().get(Boolean.valueOf(true));
			size = images.size();
			buf.append(size);
			buf.append(" image");
			if (size > 1) buf.append("s");
			buf.append(" from a fileset, leaving ");
			
			thumbnails = object.getThumbnailsFromList(images);
			row.add(layoutThumbnails(images.size()-thumbnails.size(),
					thumbnails));
			row.add(Box.createHorizontalStrut(10));
			
			images = e.getValue().get(Boolean.valueOf(false));
			buf.append(images.size());
			thumbnails = object.getThumbnailsFromList(images);
			row.add(layoutThumbnails(images.size()-thumbnails.size(),
					thumbnails));
			p.add(UIUtilities.buildComponentPanel(new JLabel(buf.toString())));
			p.add(row);
			
		}
		if (n == 1) return new JScrollPane(p);
		long id = object.getContext().getGroupID();
		GroupData group = getGroup(id);
		if (group != null) text = group.getName();
		else {
			buf = new StringBuffer();
			buf.append("Group ");
			buf.append(id);
			text = buf.toString();
		}
		JXTaskPane pane = EditorUtil.createTaskPane(text);
		pane.add(new JScrollPane(p));
		return pane;
	}
	
	private GroupData getGroup(long groupID)
	{
		Iterator<Object> i = groups.iterator();
		GroupData g;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (g.getId() == groupID) return g;
		}
		return null;
	}
	/**
	 * Lays out the thumbnails.
	 * 
	 * @param n The number of images not listed.
	 * @param thumbnails The objects to lay out.
	 */
	private JPanel layoutThumbnails(int n, List<ThumbnailData> thumbnails)
	{
		JPanel row = createRow();
		row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		Iterator<ThumbnailData> i = thumbnails.iterator();
		ThumbnailLabel label;
		while (i.hasNext()) {
			label = new ThumbnailLabel();
			label.setData(i.next());
			row.add(label);
		}
		if (n > 0) row.add(new JLabel("..."));
		return row;
	}
	
	/** 
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel createRow()
	{
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		return row;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param result The images that cannot be deleted or moved.
	 * @param action The action to take post check.
	 * @param index Either <code>Delete</code> or <code>Change Group</code>.
	 * @param groups the available groups.
	 */
	public MIFNotificationDialog(JFrame owner, List<MIFResultObject> result,
			Object action, int index, Collection groups)
	{
		super(owner);
		if (CollectionUtils.isEmpty(result))
			throw new IllegalArgumentException("No result to display");
		this.index = index;
		this.result = result;
		this.action = action;
		this.groups = groups;
		initialize();
		buildGUI();
		pack();
	}
	
}
