package ome.utils;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import ome.model.IEnum;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * It's the utils class.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class Utils {

	/**
	 * Wraps original {@link java.util.List} as GUI List
	 * {@link javax.faces.model.SelectItem}
	 * 
	 * @param originalList
	 *            {@link java.util.List}
	 * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
	 */
	public static synchronized List wrapExperimenterClassAsGUIList(
			List<Class<IEnum>> originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (Class klass : originalList) {
			SelectItem item = new SelectItem(klass.getName(), klass.getName());
			items.add(item);
		}
		return items;
	}

	/**
	 * Wraps original {@link java.util.List} as GUI List
	 * {@link javax.faces.model.SelectItem}
	 * 
	 * @param originalList
	 *            {@link java.util.List}
	 * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
	 */
	public static synchronized List wrapExperimenterGroupAsGUIList(
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

	/**
	 * Wraps original {@link java.util.List} as GUI List
	 * {@link javax.faces.model.SelectItem}
	 * 
	 * @param originalList
	 *            {@link java.util.List} never-null.
	 * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
	 */
	public static synchronized List<SelectItem> wrapSelectItemAsGUIList(
			List<Experimenter> originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (int i = 0, n = originalList.size(); i < n; i++) {
			Experimenter bean = originalList.get(i);
			SelectItem item = new SelectItem(bean.getId().toString(), bean
					.getOmeName());
			items.add(item);
		}
		return items;
	}

}
