/*
 * org.openmicroscopy.shoola.agents.metadata.editor.FilterComponent
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Font;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Component hosting a filter.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FilterComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** The title displayed if the filter is an emission filter. */
	static final String EMISSION_FILTER = "Emission Filter";
	
	/** The title displayed if the filter is an excitation filter. */
	static final String EXCITATION_FILTER = "Excitation Filter";
	
	/** The component displaying the filter options. */
	private OMEComboBox							filterBox;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fieldsFilter;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton						unsetFilter;
	
	/** Flag indicating the unset fields displayed. */
	private boolean								unsetFilterShown;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI	parent;
	
	/** Reference to the Model. */
	private EditorModel			model;
	
	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 
		EnumerationObject[] array;
		Iterator<EnumerationObject> j;
		int i = 0;
		l = model.getChannelEnumerations(Editor.FILTER_TYPE);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationUI.NO_SET_TEXT);
		
		filterBox = EditorUtil.createComboBox(array);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		resetBoxes();
		fieldsFilter = new LinkedHashMap<String, DataComponent>();
		unsetFilter = null;
		unsetFilterShown = false;
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetFilterFields()
	{
		unsetFilterShown = !unsetFilterShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetFilterShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetFilter.setText(s);
		parent.layoutFields(this, unsetFilter, fieldsFilter, unsetFilterShown);
	}
	
	/**
	 * Transforms the filter .
	 * 
	 * @param details The value to transform.
	 */
	private void transformFilterSource(Map<String, Object> details)
	{
		DataComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetFilter == null) {
			unsetFilter = parent.formatUnsetFieldsControl();
			unsetFilter.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		Iterator i = entrySet.iterator();
		boolean set;
		Object selected;
		String v;
		while (i.hasNext()) {
			entry = (Entry) i.next();
            key = (String) entry.getKey();
            set = !notSet.contains(key);
            value = entry.getValue();
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (value instanceof Number) {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	if (value instanceof Double) {
            		v = ""+UIUtilities.roundTwoDecimals(
            				((Number) value).doubleValue());
            		((NumericalTextField) area).setNumberType(Double.class);
            	} else if (value instanceof Float) {
            		v = ""+UIUtilities.roundTwoDecimals(
            				((Number) value).doubleValue());
            		((NumericalTextField) area).setNumberType(Float.class);
            	} else v = ""+value;
            	((NumericalTextField) area).setText(v);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            } else if (EditorUtil.TYPE.equals(key)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.FILTER_TYPE, (String) value);
            	if (selected != null) {
            		filterBox.setSelectedItem(selected);
            	} else {
            		set = false;
            		notSet.add(key);
            		filterBox.setSelectedIndex(filterBox.getItemCount()-1);
            	}
            	filterBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = filterBox;
            } else {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	if (value == null || value.equals("")) 
                	value = AnnotationUI.DEFAULT_TEXT;
            	 ((OMETextArea) area).setEditable(false);
            	 ((OMETextArea) area).setText((String) value);
            	 ((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            }
            area.setEnabled(!set);
            comp = new DataComponent(label, area);
            comp.setEnabled(false);
			comp.setSetField(!notSet.contains(key));
			fieldsFilter.put(key, comp);
        }
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param title The filter's type e.g. secondary Emission filter
	 */
	private void buildGUI(String title)
	{
		setBorder(BorderFactory.createTitledBorder(title));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The UI reference.
	 * @param model  Reference to the model.
	 * @param title	 The filter's type e.g. secondary Emission filter.
	 */
	FilterComponent(AcquisitionDataUI parent, EditorModel model, String title)
	{
		this.parent = parent;
		this.model = model;
		if (title == null || title.trim().length() == 0)
			title = "Filter";
		initComponents();
		buildGUI(title);
	}
	
	/**
	 * Transforms the filter metadata.
	 * 
	 * @param details The value to transform.
	 */
	void displayFilter(Map<String, Object> details)
	{
		resetBoxes();
		fieldsFilter.clear();
		transformFilterSource(details);
		parent.layoutFields(this, unsetFilter, fieldsFilter, 
				unsetFilterShown);
    	parent.attachListener(fieldsFilter);
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() { return parent.hasDataToSave(fieldsFilter); }
	
	/** Prepares the data to save. */
	void prepareDataToSave()
	{
		
	}
	
	/**
	 * Reacts to property fired by the <code>JLabelButton</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (JLabelButton.SELECTED_PROPERTY.equals(name)) 
			displayUnsetFilterFields();
	}

}
