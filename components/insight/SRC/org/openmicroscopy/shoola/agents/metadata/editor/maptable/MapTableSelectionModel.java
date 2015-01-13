package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

@SuppressWarnings("serial")
public class MapTableSelectionModel extends DefaultListSelectionModel {

	private MapTable table;

	public MapTableSelectionModel(MapTable table) {
		this.table = table;
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public MapTable getTable() {
		return table;
	}

}
