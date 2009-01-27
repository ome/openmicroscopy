/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ImageAcquisitionComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import omero.model.Correction;
import omero.model.Immersion;
import omero.model.Medium;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageAcquisitionData;

/** 
 * Displays the acquisition metadata related to the image itself.
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
class ImageAcquisitionComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** Action ID to show or hide the unset objective data. */
	private static final int	OBJECTIVE = 0;
	
	/** Action ID to show or hide the unset stage data. */
	private static final int	STAGE = 1;
	
	/** Action ID to show or hide the unset detector data. */
	private static final int	ENVIRONMENT = 2;
	
	/** Reference to the Model. */
	private EditorModel							model;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** The component hosting the various immersion values. */
	private OMEComboBox							immersionBox;
	
	/** The component hosting the various coating values. */
	private OMEComboBox 						coatingBox;
	
	/** The component hosting the various medium values. */
	private OMEComboBox 						mediumBox;
	
	/** The component displaying the iris option for an objective. */
	private OMEComboBox							irisBox;
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsObjective;
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsEnv;
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsStage;
	
	/** Flag indicating if the components have been initialized. */
	private boolean								init;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton						unsetObjective;
	
	/** Flag indicating the unset fields for the objective are displayed. */
	private boolean								unsetObjectiveShown;
	
	/** The UI component hosting the objective metadata. */
	private JPanel								objectivePane;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton						unsetEnv;
	
	/** Flag indicating the unset fields for the environment are displayed. */
	private boolean								unsetEnvShown;
	
	/** The UI component hosting the environment metadata. */
	private JPanel								envPane;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton						unsetStage;
	
	/** Flag indicating the unset fields for the stage are displayed. */
	private boolean								unsetStageShown;
	
	/** The UI component hosting the stage metadata. */
	private JPanel								stagePane;
	
	/** Shows or hides the unset fields. */
	private void displayUnsetObjectiveFields()
	{
		unsetObjectiveShown = !unsetObjectiveShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetObjectiveShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetObjective.setText(s);
		parent.layoutFields(objectivePane, unsetObjective, fieldsObjective, 
				unsetObjectiveShown);
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetEnvFields()
	{
		unsetEnvShown = !unsetEnvShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetEnvShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetEnv.setText(s);
		parent.layoutFields(envPane, unsetEnv, fieldsEnv, unsetEnvShown);
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetStageFields()
	{
		unsetStageShown = !unsetStageShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetStageShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetStage.setText(s);
		parent.layoutFields(stagePane, unsetStage, fieldsStage, 
				unsetStageShown);
	}
	
	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 	
		l = model.getImageEnumerations(Editor.IMMERSION);
		immersionBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.CORRECTION);
		coatingBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.MEDIUM);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		mediumBox = EditorUtil.createComboBox(array);
	}
	
	/** Initiliases the components. */
	private void initComponents()
	{
		resetBoxes();
		String[] values = new String[3];
		values[0] = AcquisitionDataUI.BOOLEAN_YES;
		values[1] = AcquisitionDataUI.BOOLEAN_NO;
		values[2] = AnnotationDataUI.NO_SET_TEXT;
		irisBox = EditorUtil.createComboBox(values);
		
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fieldsObjective = new LinkedHashMap<String, AcquisitionComponent>();
		fieldsEnv = new LinkedHashMap<String, AcquisitionComponent>();
		fieldsStage = new LinkedHashMap<String, AcquisitionComponent>();
		unsetObjective = null;
		unsetObjectiveShown = false;
		objectivePane = new JPanel();
		objectivePane.setBorder(BorderFactory.createTitledBorder("Objective"));
		objectivePane.setBackground(UIUtilities.BACKGROUND_COLOR);
		objectivePane.setLayout(new GridBagLayout());
		unsetEnv = null;
		unsetEnvShown = false;
		envPane = new JPanel();
		envPane.setBorder(BorderFactory.createTitledBorder("Environment"));
		envPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		envPane.setLayout(new GridBagLayout());
		unsetStage = null;
		unsetStageShown = false;
		stagePane = new JPanel();
		stagePane.setBorder(BorderFactory.createTitledBorder("Position"));
		stagePane.setBackground(UIUtilities.BACKGROUND_COLOR);
		stagePane.setLayout(new GridBagLayout());
	}
	
	/**
	 * Transforms the objective metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	private void transformObjective(Map<String, Object> details)
	{
		AcquisitionComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetObjective == null) {
			unsetObjective = parent.formatUnsetFieldsControl();
			unsetObjective.setActionID(OBJECTIVE);
			unsetObjective.addPropertyChangeListener(this);
		}

		boolean set;
		boolean b;
		Iterator i = details.keySet().iterator();
		while (i.hasNext()) {
			key = (String) i.next();
			set = !notSet.contains(key);
			value = details.get(key);
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			if (key.equals(EditorUtil.IMMERSION)) {
				selected = model.getImageEnumerationSelected(
						Editor.IMMERSION, (String) value);
				if (selected != null) {
					immersionBox.setSelectedItem(selected);
					if (AcquisitionDataUI.UNSET_ENUM.contains(
							selected.toString()))
						set = false;
				}
				immersionBox.setEditedColor(UIUtilities.EDITED_COLOR);
				area = immersionBox;
			} else if (key.equals(EditorUtil.CORRECTION)) {
				selected = model.getImageEnumerationSelected(
						Editor.CORRECTION, (String) value);
				if (selected != null) {
					coatingBox.setSelectedItem(selected);
					if (AcquisitionDataUI.UNSET_ENUM.contains(
							selected.toString()))
						set = false;
				}
				area = coatingBox;
				coatingBox.setEditedColor(UIUtilities.EDITED_COLOR);
			} else if (key.equals(EditorUtil.MEDIUM)) {
				selected = model.getImageEnumerationSelected(
						Editor.MEDIUM, (String) value);
				if (selected != null) {
					mediumBox.setSelectedItem(selected);
				} else {
					set = false;
					mediumBox.setSelectedIndex(mediumBox.getItemCount()-1);
				}
				area = mediumBox;
				mediumBox.setEditedColor(UIUtilities.EDITED_COLOR);
			} else if (key.equals(EditorUtil.IRIS)) {
        		if (value != null) {
        			b = (Boolean) value;
        			if (b) 
        				irisBox.setSelectedItem(AcquisitionDataUI.BOOLEAN_YES);
        			else irisBox.setSelectedItem(AcquisitionDataUI.BOOLEAN_NO);
        		} else {
        			set = false;
        			irisBox.setSelectedItem(AnnotationDataUI.NO_SET_TEXT);
        		}
        		irisBox.setEditedColor(UIUtilities.EDITED_COLOR);
        		area = irisBox;
			} else {
				if (value instanceof Number) {
					area = UIUtilities.createComponent(
							NumericalTextField.class, null);
					if (value instanceof Double) 
						((NumericalTextField) area).setNumberType(
								Double.class);
					else if (value instanceof Float) 
						((NumericalTextField) area).setNumberType(
								Float.class);
					((NumericalTextField) area).setText(""+value);
					((NumericalTextField) area).setEditedColor(
							UIUtilities.EDITED_COLOR);
				} else {
					area = UIUtilities.createComponent(OMETextArea.class, 
							null);
					if (value == null || value.equals(""))
						value = AnnotationUI.DEFAULT_TEXT;
					((OMETextArea) area).setText((String) value);
					((OMETextArea) area).setEditedColor(
							UIUtilities.EDITED_COLOR);
				}
			}
			area.setEnabled(!set);
			comp = new AcquisitionComponent(label, area);
			comp.setSetField(!notSet.contains(key));
			fieldsObjective.put(key, comp);
		}
	}
	
	/**
	 * Transforms the position metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	private void transformStage(Map<String, Object> details)
	{
		AcquisitionComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetStage == null) {
			unsetStage = parent.formatUnsetFieldsControl();
			unsetStage.setActionID(STAGE);
			unsetStage.addPropertyChangeListener(this);
		}

		boolean set;
		Iterator i = details.keySet().iterator();
		while (i.hasNext()) {
            key = (String) i.next();
			set = !notSet.contains(key);
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (value instanceof String) {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
                if (value == null || value.equals("")) {
                	value = AnnotationUI.DEFAULT_TEXT;
                	set = false;
                }
                ((OMETextArea) area).setText((String) value);
           	 	((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            } else {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	((NumericalTextField) area).setNegativeAccepted(true);
            	if (value instanceof Double) 
            		((NumericalTextField) area).setNumberType(Double.class);
            	else if (value instanceof Float) 
        			((NumericalTextField) area).setNumberType(Float.class);
            	((NumericalTextField) area).setText(""+value);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            	
            }
            area.setEnabled(!set);
            comp = new AcquisitionComponent(label, area);
			comp.setSetField(!notSet.contains(key));
			fieldsStage.put(key, comp);
        }
	}
	
	/**
	 * Transforms the environment metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	private void transformEnv(Map<String, Object> details)
	{
		AcquisitionComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetEnv == null) {
			unsetEnv = parent.formatUnsetFieldsControl();
			unsetEnv.setActionID(ENVIRONMENT);
			unsetEnv.addPropertyChangeListener(this);
		}

		boolean set;
		Iterator i = details.keySet().iterator();
		while (i.hasNext()) {
			key = (String) i.next();
			set = !notSet.contains(key);
			value = details.get(key);
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);

			if (value instanceof Number) {
				area = UIUtilities.createComponent(
						NumericalTextField.class, null);
				if (EditorUtil.TEMPERATURE.equals(key)) {
					((NumericalTextField) area).setMinimum(-Double.MAX_VALUE);
					((NumericalTextField) area).setNegativeAccepted(true);
				}
				if (value instanceof Double) 
					((NumericalTextField) area).setNumberType(Double.class);
				else if (value instanceof Float) 
					((NumericalTextField) area).setNumberType(Float.class);
				((NumericalTextField) area).setText(""+value);
				((NumericalTextField) area).setEditedColor(
						UIUtilities.EDITED_COLOR);
				
			} else {
				area = UIUtilities.createComponent(
						OMETextArea.class, null);
				if (value == null || value.equals("")) {
					value = AnnotationUI.DEFAULT_TEXT;
					set = false;
				}
				((OMETextArea) area).setText((String) value);
				((OMETextArea) area).setEditedColor(
						UIUtilities.EDITED_COLOR);
			}
			area.setEnabled(!set);
			comp = new AcquisitionComponent(label, area);
			comp.setSetField(!notSet.contains(key));
			fieldsEnv.put(key, comp);
		}
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		fieldsObjective.clear();
		fieldsEnv.clear();
		fieldsStage.clear();
		ImageAcquisitionData data = model.getImageAcquisitionData();
		transformObjective(EditorUtil.transformObjective(data));
		transformEnv(EditorUtil.transformImageEnvironment(data));
		transformStage(EditorUtil.transformStageLabel(data));
		parent.layoutFields(objectivePane, unsetObjective, fieldsObjective, 
				unsetObjectiveShown);
		parent.layoutFields(envPane, unsetEnv, fieldsEnv, unsetEnvShown);
		parent.layoutFields(stagePane, unsetStage, fieldsStage, 
				unsetStageShown);
		add(objectivePane);
		add(envPane);
		add(stagePane);
		parent.attachListener(fieldsObjective);
		parent.attachListener(fieldsStage);
		parent.attachListener(fieldsEnv);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	ImageAcquisitionComponent(AcquisitionDataUI parent, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		this.parent = parent;
		this.model = model;
		initComponents();
	}
	
	/** Sets the metadata. */
	void setImageAcquisitionData()
	{
		if (!init) {
			init = true;
			resetBoxes();
			removeAll();
			buildGUI();
		}
		
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
	boolean hasDataToSave()
	{
		boolean b = parent.hasDataToSave(fieldsObjective);
		if (b) return true;
		b = parent.hasDataToSave(fieldsEnv);
		if (b) return true;
		b = parent.hasDataToSave(fieldsStage);
		if (b) return true;
		return false;
	}
	
	/**
	 * Prepares the data to save.
	 * 
	 * @return See above.
	 */
	ImageAcquisitionData prepareDataToSave()
	{
		if (!hasDataToSave()) return null;
		ImageAcquisitionData data = model.getImageAcquisitionData();
		String key;
		AcquisitionComponent comp;
		Object value;
		EnumerationObject enumObject;
		
		Iterator<String> i = fieldsObjective.keySet().iterator();
		//objective
		Number number;
		Boolean bool;
		while (i.hasNext()) {
			key = i.next();
			comp = fieldsObjective.get(key);
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.MODEL.equals(key)) {
					data.setModel((String) value);
				} else if (EditorUtil.MANUFACTURER.equals(key)) {
					data.setManufacturer((String) value);
				} else if (EditorUtil.SERIAL_NUMBER.equals(key)) {
					data.setSerialNumber((String) value);
				} else if (EditorUtil.NOMINAL_MAGNIFICATION.equals(key)) {
					data.setNominalMagnification((Integer) value);
				} else if (EditorUtil.CALIBRATED_MAGNIFICATION.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null)
						data.setCalibratedMagnification((Float) number);
				} else if (EditorUtil.LENSNA.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setLensNA((Float) number);
				} else if (EditorUtil.IMMERSION.equals(key)) {
					enumObject = (EnumerationObject) value;
					data.setImmersion((Immersion) enumObject.getObject());
				} else if (EditorUtil.CORRECTION.equals(key)) {
					enumObject = (EnumerationObject) value;
					data.setCorrection((Correction) enumObject.getObject());
				} else if (EditorUtil.WORKING_DISTANCE.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setWorkingDistance((Float) number);
				} else if (EditorUtil.MEDIUM.equals(key)) {
					enumObject = (EnumerationObject) value;
					if (enumObject.getObject() instanceof Medium)
						data.setMedium((Medium) enumObject.getObject());
				} else if (EditorUtil.REFRACTIVE_INDEX.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setRefractiveIndex((Float) number);
				} else if (EditorUtil.IRIS.equals(key)) {
					bool = parent.convertToBoolean((String) value);
					if (bool != null) data.setIris(bool);
				} else if (EditorUtil.CORRECTION_COLLAR.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) 
						data.setCorrectionCollar((Float) number);
				} 
			}
		}
		
		//environment
		i = fieldsEnv.keySet().iterator();
		float v;
		while (i.hasNext()) {
			key = i.next();
			comp = fieldsEnv.get(key);
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.TEMPERATURE.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setTemperature((Float) number);
				} else if (EditorUtil.AIR_PRESSURE.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setAirPressure((Float) number);
				} else if (EditorUtil.HUMIDITY.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) {
						v = ((Float) number) /100;
						data.setHumidity(v);
					}
				} else if (EditorUtil.CO2_PERCENT.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) {
						v = ((Float) number) /100;
						data.setCo2Percent(v);
					}
				}
			}
		}
		
		//stage label

		i = fieldsStage.keySet().iterator();
		while (i.hasNext()) {
			key = i.next();
			comp = fieldsStage.get(key);
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.NAME.equals(key)) {
					data.setLabelName((String) value);
				} else if (EditorUtil.POSITION_X.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionX((Float) number);
				} else if (EditorUtil.POSITION_Y.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionY((Float) number);
				} else if (EditorUtil.POSITION_Z.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionZ((Float) number);
				}
			}
		}
		return data;
	}
	
	/**
	 * Reacts to property fired by the <code>JLabelButton</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (JLabelButton.SELECTED_PROPERTY.equals(name)) {
			int id = ((Integer) evt.getNewValue()).intValue();
			switch (id) {
				case OBJECTIVE:
					displayUnsetObjectiveFields();
					break;
				case STAGE:
					displayUnsetStageFields();
					break;
				case ENVIRONMENT:
					displayUnsetEnvFields();
			}
		}
	}
	
}
