/**
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import omero.model.NamedValue;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.editor.EditorModel.MapAnnotationType;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTable;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTableModel;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTableSelectionModel;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.MapAnnotationData;

/**
 * A Component handling the {@link MapAnnotationData}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapAnnotationsComponent extends JPanel implements
		ListSelectionListener {

	private static final long serialVersionUID = -6379927802043007970L;

	/** Reference to the {@link EditorModel} */
	private EditorModel model;

	/** Reference to the {@link EditorUI} */
	private EditorUI view;

	/** The toolbar */
	private JPanel toolbar;

	/** The toolbar buttons */
	private JButton copyButton, pasteButton, deleteButton;

	/** Reference to the {@link MapTable}s */
	private List<MapTable> mapTables = new ArrayList<MapTable>();

	/** Flag to en/disable the ListSelectionListener */
	private boolean listenerActive = true;

	/** The layout constraints */
	private GridBagConstraints c;

	/** Reference to the copy/paste storage */
	private static List<NamedValue> copiedValues = MetadataViewerFactory
			.getCopiedMapAnnotationsEntries();

	/**
	 * Creates a new MapAnnotationsComponent
	 * 
	 * @param model
	 * @param view
	 */
	public MapAnnotationsComponent(EditorModel model, EditorUI view) {
		this.model = model;
		this.view = view;
		initComponents();
	}

	/**
	 * Initializes the component
	 */
	private void initComponents() {
		setLayout(new GridBagLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		toolbar = createToolBar();
		toolbar.setBackground(UIUtilities.BACKGROUND_COLOR);
	}

	/**
	 * Creates the toolbar
	 * 
	 * @return The panel used as a toolbar
	 */
	private JPanel createToolBar() {
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(Box.createHorizontalGlue());

		IconManager icons = IconManager.getInstance();

		MouseListener ml = new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getSource() == copyButton) {
					copySelection();
				}
				if (e.getSource() == pasteButton) {
					pasteSelection();
				}
				if (e.getSource() == deleteButton) {
					deleteSelection();
				}
			}

		};
		copyButton = new JButton(icons.getIcon(IconManager.COPY));
		UIUtilities.unifiedButtonLookAndFeel(copyButton);
		copyButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		copyButton.setToolTipText("Copy");
		copyButton.addMouseListener(ml);
		bar.add(copyButton);

		pasteButton = new JButton(icons.getIcon(IconManager.PASTE));
		UIUtilities.unifiedButtonLookAndFeel(pasteButton);
		pasteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		pasteButton.setToolTipText("Paste");
		pasteButton.addMouseListener(ml);
		bar.add(pasteButton);

		deleteButton = new JButton(icons.getIcon(IconManager.DELETE_12));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.setToolTipText("Delete");
		deleteButton.addMouseListener(ml);
		bar.add(deleteButton);
		return bar;
	}

	/**
	 * Builds the basic UI
	 */
	protected void buildUI() {
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(3, 2, 3, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		add(toolbar, c);
		c.gridy++;
		
		copyButton.setEnabled(canCopy());
		pasteButton.setEnabled(canPaste());
		deleteButton.setEnabled(canDelete());
	}

	/**
	 * Completely resets the component
	 */
	public void clear() {
		mapTables.clear();
		removeAll();
		buildUI();
	}

	/**
	 * Refreshes the UI with the model's current content
	 */
	public void reload() {
		List<MapAnnotationData> list = model
				.getMapAnnotations(MapAnnotationType.USER);
		if (list.isEmpty()) {
			MapAnnotationData newMA = new MapAnnotationData();
			newMA.setNameSpace(MapAnnotationData.NS_CLIENT_CREATED);
			list.add(newMA);
		}
		list.addAll(model.getMapAnnotations(MapAnnotationType.OTHER_USERS));
		list.addAll(model.getMapAnnotations(MapAnnotationType.OTHER));

		for (MapAnnotationData ma : list) {
			MapTable t = findTable(ma);
			if (t != null) {
				t.setData(ma);
			} else {
				// if there isn't a table yet, create it
				JPanel p = new JPanel();
				p.setLayout(new BorderLayout());
				t = createMapTable(ma);
				if (t != null) {
					if (mapTables.size() == 1)
						p.add(t.getTableHeader(), BorderLayout.NORTH);
					p.add(t, BorderLayout.CENTER);
					add(p, c);
					c.gridy++;
				}
			}
		}
	}

	/**
	 * Finds the table corresponding to the given {@link MapAnnotationData}
	 * object
	 * 
	 * @param data
	 *            The {@link MapAnnotationData} to look for
	 * @return The {@link MapTable} or <code>null</code> if it doesn't exist
	 */
	private MapTable findTable(MapAnnotationData data) {
		for (MapTable table : mapTables) {
			if (table.getData().getId() == data.getId())
				return table;
		}

		// the user's MapAnnotation might not have an ID yet.
		if (isUsers(data)) {
			for (MapTable table : mapTables) {
				if (table.getData().getId() < 0) {
					return table;
				}
			}
		}

		return null;
	}

	/**
	 * Check if the given {@link MapAnnotationData} is the user's own annotation
	 * 
	 * @param data
	 *            The {@link MapAnnotationData}
	 * @return See above
	 */
	private boolean isUsers(MapAnnotationData data) {
		return MapAnnotationData.NS_CLIENT_CREATED.equals(data.getNameSpace())
				&& (data.getOwner() == null || MetadataViewerAgent
						.getUserDetails().getId() == data.getOwner().getId());
	}

	/**
	 * Creates a {@link MapTable} and adds it to the list of mapTables; Returns
	 * <code>null</code> if the {@link MapAnnotationData} is empty and not
	 * editable!
	 * 
	 * @param m
	 *            The data to show
	 * @return See above
	 */
	@SuppressWarnings("unchecked")
	private MapTable createMapTable(MapAnnotationData m) {
		boolean editable = MapAnnotationData.NS_CLIENT_CREATED.equals(m
				.getNameSpace())
				&& model.canAnnotate()
				&& (m.getId() <= 0 || m.getOwner().getId() == MetadataViewerAgent
						.getUserDetails().getId());

		boolean deletable = MetadataViewerAgent.isAdministrator();

		if (!editable
				&& (m.getContent() == null || ((List<NamedValue>) m
						.getContent()).isEmpty()))
			return null;

		int permissions;
		if (editable)
			permissions = MapTable.PERMISSION_DELETE | MapTable.PERMISSION_MOVE
					| MapTable.PERMISSION_EDIT;
		else if (deletable)
			permissions = MapTable.PERMISSION_DELETE;
		else
			permissions = MapTable.PERMISSION_NONE;

		final MapTable t = new MapTable(permissions);
		t.setDoubleClickEdit(true);
		t.getSelectionModel().addListSelectionListener(this);
		t.setData(m);
		t.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				MapTableModel m = (MapTableModel) t.getModel();
				if (m.isDirty()) {
					view.saveData(true);
				}
			}
		});
		mapTables.add(t);
		return t;
	}

	/**
	 * Get all {@link MapAnnotationData}s handled by this component
	 * 
	 * @param onlyDirty
	 *            Pass <code>true</code> if you only want the dirty ones
	 * @return
	 */
	public List<MapAnnotationData> getMapAnnotations(boolean onlyDirty) {
		List<MapAnnotationData> result = new ArrayList<MapAnnotationData>();
		for (MapTable t : mapTables) {
			if (!onlyDirty || ((MapTableModel) t.getModel()).isDirty())
				result.add(t.getData());
		}
		return result;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && listenerActive) {
			listenerActive = false;
			MapTable src = ((MapTableSelectionModel) e.getSource()).getTable();
			for (MapTable t : mapTables) {
				if (t != src) {
					t.getSelectionModel().clearSelection();
				}
			}
			listenerActive = true;

			deleteButton.setEnabled(canDelete());
			copyButton.setEnabled(canCopy());
			pasteButton.setEnabled(canPaste());
		}
	}

	/**
	 * Get the table which holds the current selection
	 * 
	 * @return See above
	 */
	public MapTable getSelectedTable() {
		for (MapTable t : mapTables) {
			if (t.getSelectedRow() != -1)
				return t;
		}
		return null;
	}

	/**
	 * Get the table which holds the the user's own {@link MapAnnotationData}
	 * 
	 * @return See above
	 */
	public MapTable getUserTable() {
		for (MapTable t : mapTables) {
			if (isUsers(t.getData()))
				return t;
		}
		return null;
	}

	/**
	 * Get the selected {@link NamedValue}s
	 * 
	 * @return
	 */
	public List<NamedValue> getSelection() {
		MapTable t = getSelectedTable();
		if (t != null)
			return t.getSelection();
		return Collections.emptyList();
	}

	/**
	 * Copy the current selection
	 */
	private void copySelection() {
		copiedValues.clear();
		copiedValues.addAll(getSelection());
		pasteButton.setEnabled(!copiedValues.isEmpty());
	}

	/**
	 * Paste previously copied selection
	 */
	private void pasteSelection() {
		MapTable t = getSelectedTable();
		if (t == null)
			t = getUserTable();
		MapTableModel m = (MapTableModel) t.getModel();
		int index = t.getSelectedRow();
		m.addEntries(copiedValues, index);
	}

	/**
	 * Delete the currently selected items
	 */
	private void deleteSelection() {
		MapTable t = getSelectedTable();
		t.deleteSelected();
	}

	/**
	 * Checks if delete action is possible
	 */
	private boolean canDelete() {
		return !getSelection().isEmpty() && getSelectedTable().canDelete();
	}

	/**
	 * Checks if copy action is possible
	 */
	private boolean canCopy() {
		return !getSelection().isEmpty();
	}

	/**
	 * Checks if paste action is possible
	 */
	private boolean canPaste() {
		return !copiedValues.isEmpty()
				&& (getSelectedTable() == null || getSelectedTable().canEdit());
	}
}
