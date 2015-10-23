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
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DataObject;
import omero.gateway.model.DetectorData;
import omero.gateway.model.DichroicData;
import omero.gateway.model.FilterData;
import omero.gateway.model.FilterSetData;
import omero.gateway.model.InstrumentData;
import omero.gateway.model.LightSourceData;
import omero.gateway.model.ObjectiveData;

/** 
 * Describes the instrument used to capture the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class InstrumentComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** Reference to the Model. */
	private EditorModel							model;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** Flag indicating if the components have been initialized. */
	private boolean								init;

	/** Button to show or hides the unset fields. */
	private JLabelButton						unsetMicroscope;
	
	/** Flag indicating the unset fields for the stage are displayed. */
	private boolean								unsetMicroscopeShown;
	
	/** The UI component hosting the stage metadata. */
	private JPanel								microscopePane;
	
	/** The fields displaying the microscope. */
	private Map<String, DataComponent> 			fieldsMicroscope;
	
	/** The component displaying the microscope types. */
	private OMEComboBox							microscopeBox;
	
	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l = 
			model.getChannelEnumerations(Editor.MICROSCOPE_TYPE);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationUI.NO_SET_TEXT);
		microscopeBox = EditorUtil.createComboBox(array);
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetMicroscopeFields()
	{
		unsetMicroscopeShown = !unsetMicroscopeShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetMicroscopeShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetMicroscope.setText(s);
		parent.layoutFields(microscopePane, unsetMicroscope, fieldsMicroscope, 
				unsetMicroscopeShown);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		resetBoxes();
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fieldsMicroscope = new LinkedHashMap<String, DataComponent>();
		microscopePane = new JPanel();
		parent.format(microscopePane, "Microscope");
	}

	/**
	 * Transforms the microscope data into the corresponding UI objects.
	 * 
	 * @param details The data to transform.
	 */
	private void transformMicroscope(Map<String, Object> details)
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
		if (notSet.size() > 0 && unsetMicroscope == null) {
			unsetMicroscope = parent.formatUnsetFieldsControl();
			unsetMicroscope.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		boolean set;
		Object selected;
		Iterator i = entrySet.iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
            key = (String) entry.getKey();
			set = !notSet.contains(key);
            value = entry.getValue();
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);	
            if (EditorUtil.TYPE.equals(key)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.MICROSCOPE_TYPE, (String) value);
            	if (selected != null) {
            		microscopeBox.setSelectedItem(selected);
            	} else {
            		set = false;
            		notSet.add(key);
            		microscopeBox.setSelectedIndex(
            				microscopeBox.getItemCount()-1);
            	}
            	microscopeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = microscopeBox;//parent.replaceCombobox(microscopeBox);
            } else {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
                if (value == null || value.equals("")) {
                	value = AnnotationUI.DEFAULT_TEXT;
                	set = false;
                }
                ((OMETextArea) area).setText((String) value);
           	 	((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            }
            area.setEnabled(!set);
            comp = new DataComponent(label, area);
            comp.setEnabled(false);
			comp.setSetField(!notSet.contains(key));
			fieldsMicroscope.put(key, comp);
        }
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param components Collection of components to add.
	 */
	private void buildGUI(List<JComponent> components)
	{
		removeAll();
		if (microscopePane.isVisible()) {
			parent.layoutFields(microscopePane, unsetMicroscope, 
					fieldsMicroscope, unsetMicroscopeShown);
			add(microscopePane);
			parent.attachListener(fieldsMicroscope);
		}
		Iterator<JComponent> i = components.iterator();
		while (i.hasNext()) {
			add(i.next());
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model	 Reference to the Model. Mustn't be <code>null</code>.
	 */
	InstrumentComponent(AcquisitionDataUI parent, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		this.parent = parent;
		this.model = model;
		initComponents();
	}
	
	/** Sets the instrument and its components. */
	void setInstrumentData()
	{
		resetBoxes();
		fieldsMicroscope.clear();
		InstrumentData data = model.getInstrumentData();
		//Microscope.
		Map<String, Object> details = EditorUtil.transformMicroscope(data);
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		microscopePane.setVisible(false);
		if (notSet.size() != EditorUtil.MAX_FIELDS_MICROSCOPE) {
			microscopePane.setVisible(true);
			transformMicroscope(details);
		}
		List<JComponent> components = new ArrayList<JComponent>();
		Iterator i;
		//Objectives
		List<ObjectiveData> objectives = data.getObjectives();
		if (objectives != null) {
			model.sortDataObjectByID(objectives);
			i = objectives.iterator();
			ObjectiveComponent op;
			while (i.hasNext()) {
				details = EditorUtil.transformObjective(
						(ObjectiveData) i.next());
				notSet = (List) details.get(EditorUtil.NOT_SET);
				if (notSet.size() != EditorUtil.MAX_FIELDS_OBJECTIVE) {
					op = new ObjectiveComponent(parent, model);
					op.displayObjective(details);
					components.add(op);
				}
			}
		}
		//Filters
		List<FilterData> filters = data.getFilters();
		if (filters != null) {
			model.sortDataObjectByID(filters);
			i = filters.iterator();
			FilterComponent fp;
			while (i.hasNext()) {
				details = EditorUtil.transformFilter((FilterData) i.next());
				notSet = (List) details.get(EditorUtil.NOT_SET);
				if (notSet.size() != EditorUtil.MAX_FIELDS_FILTER) {
					fp = new FilterComponent(parent, model, null);
					fp.displayFilter(details);
					components.add(fp);
				}
			}
		}
		
		//Filters
		List<DichroicData> dichroics = data.getDichroics();
		if (dichroics != null) {
			model.sortDataObjectByID(dichroics);
			i = dichroics.iterator();
			DichroicComponent dp;
			while (i.hasNext()) {
				details = EditorUtil.transformDichroic((DichroicData) i.next());
				notSet = (List) details.get(EditorUtil.NOT_SET);
				if (notSet.size() != EditorUtil.MAX_FIELDS_DICHROIC) {
					dp = new DichroicComponent(parent, model);
					dp.displayDichroic(details);
					components.add(dp);
				}
			}
		}
		
		//Filter Set
		List<FilterSetData> sets = data.getFilterSets();
		if (sets != null) {
			model.sortDataObjectByID(sets);
			i = sets.iterator();
			FilterGroupComponent g;
			while (i.hasNext()) {
				g = new FilterGroupComponent(parent, model, 
						(DataObject) i.next());
				components.add(g);
			}
		}
		//Detectors
		List<DetectorData> detectors = data.getDetectors();
		if (detectors != null) {
			model.sortDataObjectByID(detectors);
			i = detectors.iterator();
			DetectorComponent dp;
			while (i.hasNext()) {
				details = EditorUtil.transformDetector((DetectorData) i.next());
				notSet = (List) details.get(EditorUtil.NOT_SET);
				if (notSet.size() != EditorUtil.MAX_FIELDS_DETECTOR) {
					dp = new DetectorComponent(parent, model);
					dp.displayDetector(details);
					components.add(dp);
				}
			}
		}
		//Lights
		List<LightSourceData> lights = data.getLightSources();
		if (lights != null) {
			model.sortDataObjectByID(lights);
			i = lights.iterator();
			LightSourceComponent lp;
			String kind;
			int n;
			while (i.hasNext()) {
				details = EditorUtil.transformLightSource(
						(LightSourceData) i.next());
				notSet = (List) details.get(EditorUtil.NOT_SET);
				kind = (String) details.get(EditorUtil.LIGHT_TYPE);
				details.remove(EditorUtil.LIGHT_TYPE);
				n = EditorUtil.MAX_FIELDS_LIGHT;
				if (LightSourceData.LASER.equals(kind)) 
					n = EditorUtil.MAX_FIELDS_LASER;
				if (notSet.size() != n) {
					lp = new LightSourceComponent(parent, model);
					lp.displayLightSource(kind, details);
					components.add(lp);
				}
			}
		}
		buildGUI(components);
	}
	
	/** Clears the data. */
	void setRootObject()
	{
		init = false;
		removeAll();
	}

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() { return false; }
	
	/**
	 * Reacts to property fired by the <code>JLabelButton</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (JLabelButton.SELECTED_PROPERTY.equals(name)) {
			displayUnsetMicroscopeFields();
		}
	}
}
