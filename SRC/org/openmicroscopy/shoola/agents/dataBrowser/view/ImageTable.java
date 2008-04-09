/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTable 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXTreeTable;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeTableModel;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * 
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
class ImageTable
	extends OMETreeTable
	implements TreeSelectionListener
{

	/** Identified the column displaying the file's path. */
	static final int  					NAME_COL = 0;
	
	/** Identified the column displaying the added date. */
	static final int  					DATE_COL = 1;
	
	/** Identified the column displaying if the file has been annotated. */
	static final int  					ANNOTATED_COL = 2;
	
	/** The texf of the {@link #NAME_COL}. */
	static final String					NAME =  "Name";
	
	/** The texf of the {@link #DATE_COL}. */
	static final String					DATE =  "Date Added";
	
	/** The texf of the {@link #ANNOTATED_COL}. */
	static final String					ANNOTATED =  "Annotated";
	
	/** The columns of the table. */
	private static Vector<String>		COLUMNS;
	
	/** Map indicating how to render each column. */
	private static Map<Integer, Class> RENDERERS;
	
	static {
		COLUMNS = new Vector<String>(3);
		COLUMNS.add(NAME);
		COLUMNS.add(DATE);
		COLUMNS.add(ANNOTATED);
		RENDERERS = new HashMap<Integer, Class>();
		RENDERERS.put(NAME_COL, ImageTableNode.class);
		RENDERERS.put(DATE_COL, String.class);
		RENDERERS.put(ANNOTATED_COL, Icon.class);
	}
	
	/** The root node of the table. */
	private ImageTableNode tableRoot;
	
	/**
	 * Builds the node.
	 * 
	 * @param parent 	The parent node.
	 * @param comp		The node to add to the parent.
	 */
	private void buildTreeNode(ImageTableNode parent, Component[] comp)
	{
		if (parent == null) return;
		if (comp == null || comp.length == 0) return;
		ImageDisplay display;
		ImageTableNode n;
		Component c;
		for (int i = 0; i < comp.length; i++) {
			c = comp[i];
			if (c instanceof ImageDisplay) {
				display = (ImageDisplay) c;
				n = new ImageTableNode(display);
				parent.insert(n, parent.getChildCount());
				buildTreeNode(n, display.getInternalDesktop().getComponents());
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param root The root of the tree.
	 */
	ImageTable(ImageDisplay root)
	{
		super();
		tableRoot = new ImageTableNode(root);
		Component[] comp = root.getInternalDesktop().getComponents();
		buildTreeNode(tableRoot, comp);
		setTableModel(new OMETreeTableModel(tableRoot, COLUMNS, RENDERERS));
		setTreeCellRenderer(new ImageTableRenderer());
		setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		setDefaultRenderer(String.class, new NumberCellRenderer());
		
		setRootVisible(false);
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(true);
		setHorizontalScrollEnabled(true);
		setColumnControlVisible(true);
		
	}

	/** Refreshes the table. */
	void refreshTable()
	{
		ImageDisplay root = ((ImageDisplay) tableRoot.getUserObject());
		tableRoot = new ImageTableNode(root);
		Component[] comp = root.getInternalDesktop().getComponents();
		buildTreeNode(tableRoot, comp);
		setTableModel(new OMETreeTableModel(tableRoot, COLUMNS, RENDERERS));
		setDefaultRenderer(String.class, new NumberCellRenderer());
		invalidate();
		repaint();
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/** Helper class to render that table. */
	private class ImageTableRenderer
		extends DefaultTreeCellRenderer
	{
		
		/** Helper reference to the {@link IconManager}. */
		private IconManager icons;
		
		/** Creates a new instance. */
		ImageTableRenderer()
		{
			setOpaque(true);
			icons = IconManager.getInstance();
		}

		/**
		 * Sets the icon associated to the data object.
		 * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, 
		 * 								Object, boolean, boolean, boolean, 
		 * 								int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, 
				Object value, boolean selected, boolean expanded, 
				boolean leaf, int row, boolean hasFocus)
		{
			if (selected) setBackground(getBackgroundSelectionColor());
			else setBackground(getBackgroundNonSelectionColor());
			if (!(value instanceof ImageTableNode)) return this;
			ImageTableNode node = (ImageTableNode) value;
			Object v = node.getHierarchyObject();
			if (v instanceof ImageData) {
				setIcon(icons.getIcon(IconManager.IMAGE));
				setText(node.getUserObject().toString());
			} else if (v instanceof DatasetData) {
				setIcon(icons.getIcon(IconManager.DATASET));
				setText(node.getUserObject().toString());
			} else if (v instanceof ProjectData) {
				setIcon(icons.getIcon(IconManager.PROJECT));
				setText(node.getUserObject().toString());
			}
			return this;
		}
	}
	
}
