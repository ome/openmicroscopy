/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import omero.model.NamedValue;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.editor.EditorModel.MapAnnotationType;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTable;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTableModel;
import org.openmicroscopy.shoola.agents.metadata.editor.maptable.MapTableSelectionModel;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ToolTipGenerator;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
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

	/** Maximum width of the tables component before scrollbars are used*/
	private static final int MAX_TABLES_COMPONENT_WIDTH = 200;
	
	/** Maximum height of the tables component before scrollbars are used*/
	private static final int MAX_TABLES_COMPONENT_HEIGHT = 300;
	
	/** Reference to the {@link EditorModel} */
	private EditorModel model;

	/** Reference to the {@link EditorUI} */
	private EditorUI view;

	/** The toolbar */
	private JPanel toolbar;

	/** The toolbar buttons */
	private JButton addButton, copyButton, pasteButton, deleteButton;

	/** Reference to the {@link MapTable}s */
	private List<MapTable> mapTables = new ArrayList<MapTable>();

	/** Flag to en/disable the ListSelectionListener */
	private boolean listenerActive = true;

	/** The layout constraints */
	private GridBagConstraints c;

	/** Reference to the copy/paste storage */
	private static List<NamedValue> copiedValues = MetadataViewerFactory
			.getCopiedMapAnnotationsEntries();

	/** Component displaying the table header */
	private JPanel headerPanel = null;

	/** Component hosting the tables */
	private JPanel tablePanel = null;

	/** Scrollpane hosting the tables component */
	private JScrollPane sp;

	/**
	 * Creates a new MapAnnotationsComponent
	 * 
	 * @param model
	 * @param view
	 */
	public MapAnnotationsComponent(EditorModel model, EditorUI view) {
		this.model = model;
		this.view = view;
		buildUI();
	}

	/**
	 * Builds the component
	 */
	private void buildUI() {
		setLayout(new BorderLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);

		toolbar = createToolBar();
		toolbar.setBackground(UIUtilities.BACKGROUND_COLOR);
		add(toolbar, BorderLayout.NORTH);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 2, 4, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		tablePanel = new JPanel();
		tablePanel.setLayout(new GridBagLayout());
		tablePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		sp = new JScrollPane(tablePanel);
		sp.setBorder(BorderFactory.createEmptyBorder());
		tablePanel.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				adjustScrollPane();
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		add(sp, BorderLayout.CENTER);
		sp.setPreferredSize(null);

		headerPanel = createHeaderPanel();
		tablePanel.add(headerPanel, c);
		c.gridy++;
	}

	/**
	 * Adjusts the size of the scrollpane hosting the tables
	 */
	private void adjustScrollPane() {
		tablePanel.setPreferredSize(null);
		Dimension d = tablePanel.getPreferredSize();
		if (d.width > MAX_TABLES_COMPONENT_WIDTH)
			d.width = MAX_TABLES_COMPONENT_WIDTH;
		if (d.height > MAX_TABLES_COMPONENT_HEIGHT)
			d.height = MAX_TABLES_COMPONENT_HEIGHT;
		d.width += 5;
		d.height += 5;
		sp.setPreferredSize(d);
		view.revalidate();
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
				if (!((JButton) e.getSource()).isEnabled())
					return;

				if (e.getSource() == addButton) {
					insertRow();
				}
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

		addButton = new JButton(icons.getIcon(IconManager.PLUS));
		UIUtilities.unifiedButtonLookAndFeel(addButton);
		addButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		addButton.setToolTipText("Insert Row");
		addButton.addMouseListener(ml);
		addButton.setEnabled(false);
		addButton.setFocusable(false);
		bar.add(addButton);

		copyButton = new JButton(icons.getIcon(IconManager.COPY));
		UIUtilities.unifiedButtonLookAndFeel(copyButton);
		copyButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		copyButton.setToolTipText("Copy Rows");
		copyButton.addMouseListener(ml);
		copyButton.setEnabled(false);
		copyButton.setFocusable(false);
		bar.add(copyButton);

		pasteButton = new JButton(icons.getIcon(IconManager.PASTE));
		UIUtilities.unifiedButtonLookAndFeel(pasteButton);
		pasteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		pasteButton.setToolTipText("Paste Rows");
		pasteButton.addMouseListener(ml);
		pasteButton.setEnabled(false);
		pasteButton.setFocusable(false);
		bar.add(pasteButton);

		deleteButton = new JButton(icons.getIcon(IconManager.DELETE_12));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.setToolTipText("Delete Rows");
		deleteButton.addMouseListener(ml);
		deleteButton.setEnabled(false);
		deleteButton.setFocusable(false);
		bar.add(deleteButton);
		return bar;
	}

	/**
	 * Creates a header panel for the MapAnnotations table
	 * @return See above
	 */
	private JPanel createHeaderPanel() {
		JPanel p = new JPanel(new GridLayout(1, 2));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);

		JLabel l = constructHeaderLabel("Key");
		l.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
				Color.LIGHT_GRAY));
		p.add(l);
		p.add(constructHeaderLabel("Value"));

		return p;
	}

	/**
	 * Constructs a label for the header panel
	 * @return See above
	 */
	private JLabel constructHeaderLabel(String text) {
		JLabel l = new JLabel(" " + text);
		Font f = l.getFont().deriveFont(Font.ITALIC);
		l.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		l.setFont(f);
		return l;
	}

	/**
	 * Completely resets the component
	 */
	public void clear() {
		mapTables.clear();
		tablePanel.removeAll();
		c.gridy = 0;
		tablePanel.add(headerPanel, c);
		c.gridy++;
	}

    /**
     * Show only specific types of MapAnnotations
     * 
     * @param filter
     *            One of: {@link AnnotationDataUI#SHOW_ALL}, see
     *            {@link AnnotationDataUI#ADDED_BY_ME}, see
     *            {@link AnnotationDataUI#ADDED_BY_OTHERS}
     */
    public void filter(int filter) {
        for (MapTable table : mapTables) {
            table.getParent().setVisible(false);

            if (isUsers(table.getData())
                    && (filter == AnnotationDataUI.ADDED_BY_ME || filter == AnnotationDataUI.SHOW_ALL)) {
                table.getParent().setVisible(true);
            }

            if (isOtherUsers(table.getData())
                    && (filter == AnnotationDataUI.ADDED_BY_OTHERS || filter == AnnotationDataUI.SHOW_ALL)) {
                table.getParent().setVisible(true);
            }

            if (isOther(table.getData()) && filter == AnnotationDataUI.SHOW_ALL) {
                table.getParent().setVisible(true);
            }
        }
    }
	
    /**
     * Refreshes the UI with the model's current content
     * 
     * @param filter
     *            Show only specific types of MapAnnotations, see
     *            {@link AnnotationDataUI#SHOW_ALL}, see
     *            {@link AnnotationDataUI#ADDED_BY_ME}, see
     *            {@link AnnotationDataUI#ADDED_BY_OTHERS}
     */
	public void reload(int filter) {
        List<MapAnnotationData> list = new ArrayList<MapAnnotationData>();

        list.addAll(model.getMapAnnotations(MapAnnotationType.USER));
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
				String title = ma.getOwner() != null && !isUsers(ma) ? "Added by: "
						+ EditorUtil.formatExperimenter(ma.getOwner())
						: "";

				JPanel p = new JPanel();
				p.setBackground(UIUtilities.BACKGROUND_COLOR);
				UIUtilities.setBoldTitledBorder(title, p);
				p.setToolTipText(generateToolTip(ma));
				p.setLayout(new BorderLayout());
				t = createMapTable(ma);

				if (t != null) {
					p.add(t, BorderLayout.CENTER);

					// if the MapAnnotation has a custom namespace, display it
					if (!CommonsLangUtils.isEmpty(ma.getNameSpace())
							&& !MapAnnotationData.NS_CLIENT_CREATED.matches(ma
									.getNameSpace())) {
						JLabel ns = new JLabel(UIUtilities.formatPartialName(ma
								.getNameSpace()));
						ns.setFont(ns.getFont().deriveFont(Font.BOLD));
						p.add(ns, BorderLayout.NORTH);
					}

					tablePanel.add(p, c);
					c.gridy++;
				}
			}
		}

        if (filter > -1)
            filter(filter);
		
		refreshButtonStates();
		setVisible(!mapTables.isEmpty());
		adjustScrollPane();
	}

	private String generateToolTip(MapAnnotationData ma) {
	    ToolTipGenerator tt = new ToolTipGenerator();
        ExperimenterData exp = null;
        
        if(ma.getId()>0) {
            exp = model.getOwner(ma);
            tt.addLine("ID", ""+ma.getId(), true);
        }
        
        String ns = ma.getNameSpace();
        if(!CommonsLangUtils.isEmpty(ns) && !EditorUtil.isInternalNS(ns)) {
            tt.addLine("Namespace", ns, true);
        }
        
        String desc = ma.getDescription();
        if(!CommonsLangUtils.isEmpty(desc)) {
            tt.addLine("Description", desc, true);
        }
        
        if(exp!=null) {
            tt.addLine("Owner", EditorUtil.formatExperimenter(exp), true);
        }
        
        Timestamp created = ma.getCreated();
        if(created !=null) {
            tt.addLine("Date", UIUtilities.formatShortDateTime(created), true);
        }
        
        return tt.toString();
	}
	
	/**
	 * En-/Disables the toolbar buttons with respect to the current model state
	 */
	private void refreshButtonStates() {
		addButton.setEnabled(canInsert());
		copyButton.setEnabled(canCopy());
		pasteButton.setEnabled(canPaste());
		deleteButton.setEnabled(canDelete());
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
     * Check if the given {@link MapAnnotationData} belongs to an other user
     * 
     * @param data
     *            The {@link MapAnnotationData}
     * @return See above
     */
    private boolean isOtherUsers(MapAnnotationData data) {
        return MapAnnotationData.NS_CLIENT_CREATED.equals(data.getNameSpace())
                && !isUsers(data);
    }

    /**
     * Check if the given {@link MapAnnotationData} is a non-user annotation
     * 
     * @param data
     *            The {@link MapAnnotationData}
     * @return See above
     */
    private boolean isOther(MapAnnotationData data) {
        return !MapAnnotationData.NS_CLIENT_CREATED.equals(data.getNameSpace());
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
		boolean editable = (isUsers(m) && (model.canAnnotate()))
				|| (model.canEdit(m) && MapAnnotationData.NS_CLIENT_CREATED
						.equals(m.getNameSpace()));

		if (!editable
				&& (m.getContent() == null || ((List<NamedValue>) m
						.getContent()).isEmpty()))
			return null;

		int permissions = editable ? MapTable.PERMISSION_DELETE | MapTable.PERMISSION_MOVE
				| MapTable.PERMISSION_EDIT : MapTable.PERMISSION_NONE;

		final MapTable t = new MapTable(permissions);
		t.getSelectionModel().addListSelectionListener(this);
		t.setData(m);
		t.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				refreshButtonStates();
				MapTableModel m = (MapTableModel) t.getModel();
				if (m.isDirty())
					view.setDataToSave(true);
				if(m.isEmpty() && m.getMap().getId()>=0) {
					view.deleteAnnotation(m.getMap());
					view.saveData(true);
				}
				adjustScrollPane();
			}
		});
		t.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				MapTableModel m = (MapTableModel) t.getModel();
				if (m.isDirty())
					view.saveData(true);
			}

			@Override
			public void focusGained(FocusEvent e) {
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
	 * @param excludeEmpty
	 *            Pass <code>true</code> to exclude {@link MapAnnotationData}s
	 *            without {@link NamedValue} entries
	 * @return
	 */
	public List<MapAnnotationData> getMapAnnotations(boolean onlyDirty,
			boolean excludeEmpty) {
		List<MapAnnotationData> result = new ArrayList<MapAnnotationData>();
		for (MapTable t : mapTables) {
            if (t.getCellEditor() != null)
                t.getCellEditor().stopCellEditing();
			if ((!onlyDirty || ((MapTableModel) t.getModel()).isDirty())
					&& !(excludeEmpty && t.isEmpty()))
				result.add(t.getData());
		}
		return result;
	}

	/**
	 * Get all empty MapAnnotations
	 * @return See above.
	 */
	public List<MapAnnotationData> getEmptyMapAnnotations() {
		List<MapAnnotationData> result = new ArrayList<MapAnnotationData>();
		for (MapTable t : mapTables) {
			if (t.isEmpty())
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

			refreshButtonStates();
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
	private void insertRow() {
		MapTable t = getSelectedTable();
		if (t == null)
			t = getUserTable();
		MapTableModel m = (MapTableModel) t.getModel();
		// if nothing's selected add to end of table, otherwise below selected
		// row
		int index = t.getSelectedRow() == -1 ? t.getRowCount() : t.getSelectedRow() + 1;
		m.addEntries(Arrays.asList(new NamedValue("", "")), index);

		t.requestFocus();
		t.getSelectionModel().setSelectionInterval(index, index);
		t.setColumnSelectionInterval(0, 0);
	}

	/**
	 * Copy the current selection
	 */
	private void copySelection() {
		copiedValues.clear();
		copiedValues.addAll(getSelection());
		refreshButtonStates();
	}

	/**
	 * Creates a new {@link NamedValue} object with identical name and value as the
	 * {@link NamedValue} in original
	 * @param original The list of {@link NamedValue} objects to copy
	 * @return A new list with the copied {@link NamedValue}s
	 */
	private List<NamedValue> deepCopy(List<NamedValue> original) {
		List<NamedValue> result = new ArrayList<NamedValue>();
		for(NamedValue orig : original) {
			NamedValue copy = new NamedValue(orig.name, orig.value);
			result.add(copy);
		}
		return result;
	}
	
	/**
	 * Paste previously copied selection
	 */
	private void pasteSelection() {
		MapTable t = getSelectedTable();
		if (t == null)
			t = getUserTable();
		if (t == null) // no user table and nothing selected, don't know where to paste 
		    return;
		MapTableModel m = (MapTableModel) t.getModel();
		int index = t.getSelectedRow() + 1;
		m.addEntries(deepCopy(copiedValues), index);
		
		index += copiedValues.size()-1;
		t.requestFocus();
		t.getSelectionModel().setSelectionInterval(index, index);
	}

	/**
	 * Delete the currently selected items
	 */
	private void deleteSelection() {
		MapTable t = getSelectedTable();
		
		int index = t.getSelectedRow();
		
		t.deleteSelected();
		
		if(index >= t.getRowCount())
			index = t.getRowCount() -1;
		
		t.requestFocus();
		t.getSelectionModel().setSelectionInterval(index, index);
	}

	/**
	 * Checks if insert action is possible
	 */
	private boolean canInsert() {
		MapTable t = getSelectedTable();
		return ((t == null || t == getUserTable()) && model.canAnnotate())
				|| (t != null && t.canEdit());
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
