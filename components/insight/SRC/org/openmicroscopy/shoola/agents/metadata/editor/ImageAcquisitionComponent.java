/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ImageAcquisitionComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import ome.formats.model.UnitsFactory;
import omero.model.LengthI;
import omero.model.PressureI;
import omero.model.TemperatureI;
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
	
	/** Action ID to show or hide the unset stage data. */
	private static final int	STAGE = 0;
	
	/** Action ID to show or hide the unset environment data. */
	private static final int	ENVIRONMENT = 1;
	
	/** Reference to the Model. */
	private EditorModel							model;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fieldsEnv;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fieldsStage;
	
	/** Flag indicating if the components have been initialized. */
	private boolean								init;

	/** The UI component hosting the objective metadata. */
	private ObjectiveComponent					objectivePane;
	
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

	/** Initializes the components. */
	private void initComponents()
	{	
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fieldsEnv = new LinkedHashMap<String, DataComponent>();
		fieldsStage = new LinkedHashMap<String, DataComponent>();
		objectivePane = new ObjectiveComponent(parent, model);
		unsetEnv = null;
		unsetEnvShown = false;
		envPane = new JPanel();
		parent.format(envPane, "Environment");
		unsetStage = null;
		unsetStageShown = false;
		stagePane = new JPanel();
		parent.format(stagePane, "Position");
	}
	
	/**
	 * Transforms the position metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	private void transformStage(Map<String, Object> details)
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
		if (notSet.size() > 0 && unsetStage == null) {
			unsetStage = parent.formatUnsetFieldsControl();
			unsetStage.setActionID(STAGE);
			unsetStage.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		boolean set;
		Iterator i = entrySet.iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
            key = (String) entry.getKey();
			set = !notSet.contains(key);
            value = entry.getValue();
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
            comp = new DataComponent(label, area);
            comp.setEnabled(false);
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
		if (notSet.size() > 0 && unsetEnv == null) {
			unsetEnv = parent.formatUnsetFieldsControl();
			unsetEnv.setActionID(ENVIRONMENT);
			unsetEnv.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		boolean set;
		Iterator i = entrySet.iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			set = !notSet.contains(key);
			value = entry.getValue();
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);

			if (value instanceof Number) {
				area = UIUtilities.createComponent(
						NumericalTextField.class, null);
				if (value instanceof Double) 
					((NumericalTextField) area).setNumberType(Double.class);
				else if (value instanceof Float) 
					((NumericalTextField) area).setNumberType(Float.class);
				if (EditorUtil.TEMPERATURE.equals(key)) {
					((NumericalTextField) area).setMinimum(-Double.MAX_VALUE);
				}
				
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
			comp = new DataComponent(label, area);
            comp.setEnabled(false);
			comp.setSetField(set);
			fieldsEnv.put(key, comp);
		}
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		if (objectivePane.isVisible()) add(objectivePane);
		if (envPane.isVisible()) {
			parent.layoutFields(envPane, unsetEnv, fieldsEnv, unsetEnvShown);
			add(envPane);
			parent.attachListener(fieldsEnv);
		}
		if (stagePane.isVisible()) {
			parent.layoutFields(stagePane, unsetStage, fieldsStage, 
					unsetStageShown);
			add(stagePane);
			parent.attachListener(fieldsStage);
		}
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
		if (init) return;
		init = true;
		fieldsEnv.clear();
		fieldsStage.clear();
		ImageAcquisitionData data = model.getImageAcquisitionData();
		Map<String, Object> details = 
			EditorUtil.transformObjectiveAndSettings(data);
    	List notSet = (List) details.get(EditorUtil.NOT_SET);
    	objectivePane.setVisible(false);
    	if (notSet.size() != EditorUtil.MAX_FIELDS_OBJECTIVE_AND_SETTINGS) {
    		objectivePane.displayObjective(details);
    		objectivePane.setVisible(true);
    	}
    	details = EditorUtil.transformImageEnvironment(data);
    	notSet = (List) details.get(EditorUtil.NOT_SET);
    	envPane.setVisible(false);
    	if (notSet.size() != EditorUtil.MAX_FIELDS_ENVIRONMENT) {
    		transformEnv(details);
    		envPane.setVisible(true);
    	}
    	details = EditorUtil.transformStageLabel(data);
    	notSet = (List) details.get(EditorUtil.NOT_SET);
    	stagePane.setVisible(false);
    	if (notSet.size() != EditorUtil.MAX_FIELDS_STAGE_LABEL) {
    		transformStage(details);
    		stagePane.setVisible(true);
    	}
		
		buildGUI();
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
		boolean b = objectivePane.hasDataToSave();
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
		objectivePane.prepareDataToSave();
		String key;
		DataComponent comp;
		Object value;
		Number number;
		Entry entry;
		//environment
		Iterator i = fieldsEnv.entrySet().iterator();
		float v;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			comp = (DataComponent) entry.getValue();
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.TEMPERATURE.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setTemperature(new TemperatureI((Float) number, UnitsFactory.ImagingEnvironment_Temperature));
				} else if (EditorUtil.AIR_PRESSURE.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setAirPressure(new PressureI((Float) number, UnitsFactory.ImagingEnvironment_AirPressure));
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

		i = fieldsStage.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			comp = (DataComponent) entry.getValue();
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.NAME.equals(key)) {
					data.setLabelName((String) value);
				} else if (EditorUtil.POSITION_X.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionX(new LengthI((Float) number, UnitsFactory.StageLabel_X));
				} else if (EditorUtil.POSITION_Y.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionY(new LengthI((Float) number, UnitsFactory.StageLabel_Z));
				} else if (EditorUtil.POSITION_Z.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null) data.setPositionZ(new LengthI((Float) number, UnitsFactory.StageLabel_Y));
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
			int id = ((Long) evt.getNewValue()).intValue();
			switch (id) {
				case STAGE:
					displayUnsetStageFields();
					break;
				case ENVIRONMENT:
					displayUnsetEnvFields();
			}
		}
	}
	
}
