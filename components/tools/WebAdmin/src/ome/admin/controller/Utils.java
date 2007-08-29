package ome.admin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import ome.model.meta.ExperimenterGroup;

public class Utils {

	/**
	 * Wraps original {@link java.util.List} as GUI List
	 * {@link javax.faces.model.SelectItem}
	 * 
	 * @param originalList
	 *            {@link java.util.List}
	 * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
	 */
	protected static synchronized List wrapAsGUIList(
			List<ExperimenterGroup> originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (ExperimenterGroup exgp : originalList) {
			SelectItem item = new SelectItem(exgp.getId().toString(), exgp
					.getName());
			items.add(item);
		}
		return items;
	}

}
