package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

public class MapTableSelectionModel extends DefaultListSelectionModel {

	private MapTable table;

	private boolean active = true;

	public MapTableSelectionModel(MapTable table) {
		this.table = table;
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public MapTable getTable() {
		return table;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	protected void fireValueChanged(boolean isAdjusting) {
		if (active)
			super.fireValueChanged(isAdjusting);
	}

	@Override
	protected void fireValueChanged(int firstIndex, int lastIndex) {
		if (active)
			super.fireValueChanged(firstIndex, lastIndex);
	}

	@Override
	protected void fireValueChanged(int firstIndex, int lastIndex,
			boolean isAdjusting) {
		if (active)
			super.fireValueChanged(firstIndex, lastIndex, isAdjusting);
	}

}
