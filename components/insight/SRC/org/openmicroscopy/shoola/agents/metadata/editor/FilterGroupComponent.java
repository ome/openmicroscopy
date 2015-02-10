/*
 * org.openmicroscopy.shoola.agents.metadata.editor.FilterGroupComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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


//Java imports
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.FilterData;
import pojos.FilterSetData;
import pojos.LightPathData;

/** 
 * Displays either a light path or a filter set.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FilterGroupComponent 
	extends JPanel
	implements PropertyChangeListener
{
	/** Reference to the parent of this component. */
	private AcquisitionDataUI	parent;
	
	/** Reference to the Model. */
	private EditorModel			model;
	
	/** The object to handle. */
	private DataObject			object;
	
	/** The collection of emission filters if any. */
	private List<FilterComponent> emissionfilters;
	
	/** The collection of excitation filters if any. */
	private List<FilterComponent> excitationfilters;
	
	/** Component displaying the dichroic if any. */
	private DichroicComponent	 dichroic;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent>	fieldsFilterSet;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton				unsetFilterSet;
	
	/** Flag indicating the unset fields displayed. */
	private boolean						unsetFilterSetShown;
	
	/**
	 * Transforms the passed filter if not <code>null</code>.
	 * 
	 * @param filter The filter to transform.
	 * @param l The list the UI object will be added to.
	 * @param title Identifies if it an emission filter or excitation filter.
	 */
	private void populateFilter(FilterData filter, List<FilterComponent> l, 
			String title)
	{
		if (filter == null) return;
		Map<String, Object>  details = EditorUtil.transformFilter(filter);
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		if (notSet.size() != EditorUtil.MAX_FIELDS_FILTER) {
			FilterComponent comp = new FilterComponent(parent, model, title);
			comp.displayFilter(details);
			l.add(comp);
		}
	}
	
	/**
	 * Transforms the filter set.
	 * 
	 * @param details The value to transform.
	 */
	private void transformFilterSet(Map<String, Object> details)
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
		if (notSet.size() > 0 && unsetFilterSet == null) {
			unsetFilterSet = parent.formatUnsetFieldsControl();
			unsetFilterSet.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		Iterator i = entrySet.iterator();
		boolean set;
		Object selected;
		while (i.hasNext()) {
			entry = (Entry) i.next();
            key = (String) entry.getKey();
            set = !notSet.contains(key);
            value = entry.getValue();
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            area = UIUtilities.createComponent(OMETextArea.class, null);
        	if (value == null || value.equals("")) 
            	value = AnnotationUI.DEFAULT_TEXT;
        	 ((OMETextArea) area).setEditable(false);
        	 ((OMETextArea) area).setText((String) value);
        	 ((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            area.setEnabled(!set);
            comp = new DataComponent(label, area);
            comp.setEnabled(false);
			comp.setSetField(!notSet.contains(key));
			fieldsFilterSet.put(key, comp);
        }
	}
	
	/** Initializes and converts the data object. */
	private void initialize()
	{
		Map<String, Object> details;
		List notSet;
		dichroic = new DichroicComponent(parent, model);
		dichroic.setVisible(false);
		emissionfilters = new ArrayList<FilterComponent>();
		excitationfilters = new ArrayList<FilterComponent>();
		fieldsFilterSet = new HashMap<String, DataComponent>();
		List<FilterData> l;
		Iterator<FilterData> i;
		if (object instanceof FilterSetData) {
			FilterSetData set = (FilterSetData) object;
			//first dichroic
			details = EditorUtil.transformDichroic(set.getDichroic());
			notSet = (List) details.get(EditorUtil.NOT_SET);
			if (notSet.size() != EditorUtil.MAX_FIELDS_DICHROIC) {
				dichroic.displayDichroic(details);
				dichroic.setVisible(true);
			}
			l = set.getEmissionFilters();
			if (l != null) {
				i = l.iterator();
				while (i.hasNext()) {
					populateFilter(i.next(), emissionfilters, 
							FilterComponent.EMISSION_FILTER);
				}
			}
			l = set.getExcitationFilters();
			if (l != null) {
				i = l.iterator();
				while (i.hasNext()) {
					populateFilter(i.next(), excitationfilters, 
							FilterComponent.EXCITATION_FILTER);
				}
			} 
			//transform the manufacturer of a filter set.
			transformFilterSet(EditorUtil.transformFilterSetManufacturer(set));
			
		} else {
			LightPathData path = (LightPathData) object;
			//first dichroic
			details = EditorUtil.transformDichroic(path.getDichroic());
			notSet = (List) details.get(EditorUtil.NOT_SET);
			if (notSet.size() != EditorUtil.MAX_FIELDS_DICHROIC) {
				dichroic.displayDichroic(details);
				dichroic.setVisible(true);
			}
			l = path.getEmissionFilters();
			if (l != null) {
				i = l.iterator();
				while (i.hasNext()) {
					populateFilter(i.next(), emissionfilters, 
							FilterComponent.EMISSION_FILTER);
				}
			}
			l = path.getExcitationFilters();
			if (l != null) {
				i = l.iterator();
				while (i.hasNext()) {
					populateFilter(i.next(), excitationfilters, 
							FilterComponent.EXCITATION_FILTER);
				}
			} 
		}
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetFilterSetFields()
	{
		if (object instanceof LightPathData) return;
		unsetFilterSetShown = !unsetFilterSetShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetFilterSetShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetFilterSet.setText(s);
		parent.layoutFields(this, unsetFilterSet, fieldsFilterSet, 
				unsetFilterSetShown);
	}
	
	/** Builds and lays out the UI. */
    private void buildGUI()
    {
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
    	setBackground(UIUtilities.BACKGROUND_COLOR);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.weightx = 1.0;
		constraints.gridy = 0;
		if (object instanceof FilterSetData) {
			parent.layoutFields(this, unsetFilterSet, fieldsFilterSet, 
					unsetFilterSetShown);
		}
		Iterator<FilterComponent> i = excitationfilters.iterator();
		while (i.hasNext()) {
			add(i.next(), constraints);	
			++constraints.gridy;
		}
		if (dichroic.isVisible()) {
			add(dichroic, constraints);
			++constraints.gridy;
		}
		i = emissionfilters.iterator();
		while (i.hasNext()) {
			add(i.next(), constraints);	
			++constraints.gridy;
		}
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The UI reference.
	 * @param model  Reference to the model.
	 * @param object The object to handle, 
	 * 				 either <code>FilterSetData</code> or 
	 * <code>LightPathData</code>
	 */
	FilterGroupComponent(AcquisitionDataUI parent, EditorModel model, 
			DataObject object)
	{
		if (!(object instanceof LightPathData || 
			object instanceof FilterSetData))
			return;
		this.parent = parent;
		this.model = model;
		this.object = object;
		initialize();
		buildGUI();
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() 
	{ 
		boolean b = false;
		if (object instanceof FilterSetData) 
			b = parent.hasDataToSave(fieldsFilterSet); 
		if (b) return b;
		Iterator<FilterComponent> i = emissionfilters.iterator();
		while (i.hasNext()) {
			if (i.next().hasDataToSave()) return true;
		}
		i = excitationfilters.iterator();
		while (i.hasNext()) {
			if (i.next().hasDataToSave()) return true;
		}
		return dichroic.hasDataToSave();
	}
	
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
			displayUnsetFilterSetFields();
	}

}
