package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
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
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.MapAnnotationData;

@SuppressWarnings("serial")
public class MapAnnotationsComponent extends JPanel implements
		ListSelectionListener {

	private EditorModel model;

	private EditorUI view;

	private EditorControl controller;

	private JPanel toolbar;

	private JButton copyButton, pasteButton, deleteButton;

	private List<MapTable> mapTables = new ArrayList<MapTable>();

	private boolean listenerActive = true;
	
	public MapAnnotationsComponent(EditorModel model, EditorUI view,
			EditorControl controller) {
		this.model = model;
		this.view = view;
		this.controller = controller;
		initComponents();
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		toolbar = createToolBar();
		toolbar.setBackground(UIUtilities.BACKGROUND_COLOR);
	}

	private JPanel createToolBar() {
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(Box.createHorizontalGlue());

		IconManager icons = IconManager.getInstance();

		copyButton = new JButton(icons.getIcon(IconManager.COPY));
		UIUtilities.unifiedButtonLookAndFeel(copyButton);
		copyButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		copyButton.setToolTipText("Copy");
		copyButton.addMouseListener(controller);
		copyButton.setActionCommand("" + EditorControl.COPY_NAMEDVALUES);
		bar.add(copyButton);

		pasteButton = new JButton(icons.getIcon(IconManager.PASTE));
		UIUtilities.unifiedButtonLookAndFeel(pasteButton);
		pasteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		pasteButton.setToolTipText("Paste");
		pasteButton.addMouseListener(controller);
		pasteButton.setActionCommand("" + EditorControl.PASTE_NAMEDVALUES);
		bar.add(pasteButton);

		deleteButton = new JButton(icons.getIcon(IconManager.DELETE_12));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.setToolTipText("Delete");
		deleteButton.addMouseListener(controller);
		deleteButton.setActionCommand("" + EditorControl.DELETE_NAMEDVALUES);
		bar.add(deleteButton);
		return bar;
	}

	protected void buildUI() {

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(3, 2, 3, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		add(toolbar, c);
		c.gridy++;

		List<MapAnnotationData> list = model
				.getMapAnnotations(MapAnnotationType.USER);
		if (list.isEmpty()) {
			MapAnnotationData newMA = new MapAnnotationData();
			newMA.setNameSpace(MapAnnotationData.NS_CLIENT_CREATED);
			list.add(newMA);
		}
		list.addAll(model.getMapAnnotations(MapAnnotationType.OTHER_USERS));
		list.addAll(model.getMapAnnotations(MapAnnotationType.OTHER));

		for (int i = 0; i < list.size(); i++) {
			MapAnnotationData m = list.get(i);
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			MapTable t = createMapTable(m);
			if (t != null) {
				if (i == 0)
					p.add(t.getTableHeader(), BorderLayout.NORTH);
				p.add(t, BorderLayout.CENTER);
				add(p, c);
				c.gridy++;
			}
		}
	}

	/**
	 * Creates a MapTable and adds it to the list of mapTables; Returns
	 * <code>null</code> if the MapAnnotationData is empty and not editable!
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
		}
	}
}
