/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXTreeTable;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeTableModel;
import omero.gateway.model.DataObject;

/** 
 * Dialog displaying the objects that could not be deleted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class NotDeletedObjectDialog 
	extends JDialog
{

	/** Indicate that the dialog is closing. */
	public static final int		CLOSE = 0;
	
	/** Identifies the column displaying the type of object. */
	static final int 			TYPE_COL = 0;
	
	/** Identifies the column displaying the id of object. */
	static final int 			ID_COL = 1;
	
	/** Identifies the column displaying the name of object. */
	static final int 			NAME_COL = 2;
	
	
	/** The title of the dialog. */
	private static final String TITLE = "Objects not deleted";
	
	/** The text displayed in the header. */
	private static final String TEXT = "Follow the list of objects that " +
			"could not be deleted.";
	
	/** The text of the {@link #NAME_COL}. */
	private static final String	NAME =  "Name";
	
	/** The text of the {@link #TYPE_COL}. */
	private static final String	TYPE =  "Type";
	
	/** The text of the {@link #ID_COL}. */
	private static final String	ID =  "ID";
	
	
	/** The columns of the table. */
	private static Vector<String>		COLUMNS;
	
	/** Map indicating how to render each column. */
	private static Map<Integer, Class> RENDERERS;
	
	static {
		COLUMNS = new Vector<String>(3);
		COLUMNS.add(TYPE);
		COLUMNS.add(ID);
		COLUMNS.add(NAME);
		RENDERERS = new HashMap<Integer, Class>();
		RENDERERS.put(NAME_COL, DeletableTableNode.class);
		RENDERERS.put(TYPE_COL, String.class);
		RENDERERS.put(ID_COL, Long.class);
	}
	
	/** The table displaying the objects not deleted. */
	private OMETreeTable 		table;
	
	/** The root node of the tree. */
	private DeletableTableNode 	root;
	
	/** Button to close the dialog. */
	private JButton				closeButton;
	
	/** One of the constants defined by this class. */
	private int					index;
	
	/** Closes the dialog. */
	private void close()
	{
		index = CLOSE;
		setVisible(false);
	}
	
	/**
	 * Initializes the components.
	 * 
	 * @param notDeleted 	The collection of objects that could not be deleted.
	 * 						Mustn't be <code>null</code> or of size 
	 * 						<code>0</code>.
	 */
	private void initComponents(Collection<DataObject> notDeleted)
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				close();
		
			}
		});
		getRootPane().setDefaultButton(closeButton);
		root = new DeletableTableNode("");
		
		//table.setColumnControlVisible(true);
		Iterator<DataObject> i = notDeleted.iterator();
		DataObject node;
		while (i.hasNext()) {
			node = i.next();
			if (node != null)
				root.insert(new DeletableTableNode(node), root.getChildCount());
		}
		table = new OMETreeTable();
		table.setTableModel(new OMETreeTableModel(root, COLUMNS, RENDERERS));
		table.setTreeCellRenderer(new DeletableTableRenderer());
		table.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		//table.setDefaultRenderer(String.class, new NumberCellRenderer());
		table.setRootVisible(false);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		table.setHorizontalScrollEnabled(true);
	}

	 /**
     * Builds and lays out the buttons.
     * 
     * @return See above.
     */
    public JPanel buildToolBar()
    {
    	JPanel bar = new JPanel();
    	//bar.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	bar.add(closeButton);
    	bar.add(Box.createHorizontalStrut(10));
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
    	//p.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
    	return p;
    }
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.REMOVE_48);
		Container c = getContentPane();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, icon);
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
		c.add(new JScrollPane(table), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner			The owner of the dialog.
	 * @param notDeleted	The collection of objects that could not be deleted.
	 * 						Mustn't be <code>null</code> or of size 
	 * 						<code>0</code>.
	 */
	public NotDeletedObjectDialog(JFrame owner, 
			Collection<DataObject> notDeleted)
	{
		super(owner);
		if (notDeleted == null || notDeleted.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		setModal(true);
		setTitle(TITLE);
		initComponents(notDeleted);
		buildGUI();
		pack();
	}
	
	/**
	 * Centers and shows the dialog. Returns one of the constants defined
	 * by this class.
	 * 
	 * @return See above.
	 */
	public int centerAndShow()
	{
		UIUtilities.centerAndShow(this);
		return index;
	}
}
