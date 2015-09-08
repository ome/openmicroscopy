/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTaskPane;

import omero.model.AcquisitionMode;
import omero.model.ContrastMethod;
import omero.model.Illumination;
import omero.model.LengthI;
import omero.model.PlaneInfo;
import ome.formats.model.UnitsFactory;
import ome.model.units.BigResult;

import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.ColourIcon;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ChannelAcquisitionData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.LightSourceData;

/** 
 * Displays the metadata related to the channel.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ChannelAcquisitionComponent
	extends JPanel
	implements PropertyChangeListener
{

	/** Action ID to show or hide the unset general data. */
	private static final int	GENERAL = 0;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** The channel data. */
	private ChannelData 						channel;
	
	/** The component displaying the illumination's options. */
	private OMEComboBox							illuminationBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox							contrastMethodBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox							modeBox;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fieldsGeneral;
	
	/** The UI component hosting the lightSource metadata. */
	private LightSourceComponent				lightPane;
	
	/** The UI component hosting the detector metadata. */
	private DetectorComponent					detectorPane;
	
	/** The UI component hosting the filter set or <code>null</code>. */
	private FilterGroupComponent				filterSetPane;
	
	/** The UI component hosting the light path or <code>null</code>. */
	private FilterGroupComponent				lightPathPane;
	
	/** Button to show or hides the unset fields of the detector. */
	private JLabelButton						unsetGeneral;
	
	/** Flag indicating the unset fields for the general are displayed. */
	private boolean								unsetGeneralShown;
	
	/** The UI component hosting the general metadata. */
	private JPanel								generalPane;
	
	/** Flag indicating if the components have been initialized. */
	private boolean								init;
	
	/** Reference to the Model. */
	private EditorModel							model;

	/** The component hosting the exposure time. */
	private JXTaskPane							exposureTask;
	
	/** The icon displaying the color associated to the channel. */
	private ColourIcon							icon;

	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 
		l = model.getChannelEnumerations(Editor.ILLUMINATION_TYPE);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		illuminationBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.CONTRAST_METHOD);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		contrastMethodBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.MODE);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		modeBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.BINNING);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
	}
	
	/** Initializes the components */
	private void initComponents()
	{
		resetBoxes();
		fieldsGeneral = new LinkedHashMap<String, DataComponent>();
		detectorPane = new DetectorComponent(parent, model);
		lightPane = new LightSourceComponent(parent, model);
		unsetGeneral = null;
		unsetGeneralShown = false;
		generalPane = new JPanel();
		generalPane.setBorder(BorderFactory.createTitledBorder("Info"));
		generalPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		generalPane.setLayout(new GridBagLayout());
		exposureTask = EditorUtil.createTaskPane("Exposure Time");
		exposureTask.addPropertyChangeListener(this);
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetGeneralFields()
	{
		unsetGeneralShown = !unsetGeneralShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetGeneralShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetGeneral.setText(s);
		parent.layoutFields(generalPane, unsetGeneral, fieldsGeneral, 
				unsetGeneralShown);
	}
	
	/**
	 * Transforms the detector metadata.
	 * 
	 * @param details The value to transform.
	 */
	private void transformGeneralSource(Map<String, Object> details)
	{
		DataComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetGeneral == null) {
			unsetGeneral = parent.formatUnsetFieldsControl();
			unsetGeneral.setActionID(GENERAL);
			unsetGeneral.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		Iterator i = entrySet.iterator();
		boolean set;
		while (i.hasNext()) {
			entry = (Entry) i.next();
            key = (String) entry.getKey();
            set = !notSet.contains(key);
            value = entry.getValue();
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (EditorUtil.ILLUMINATION.equals(key)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) illuminationBox.setSelectedItem(selected);
            	else {
            		set = false;
            		illuminationBox.setSelectedIndex(
            				illuminationBox.getItemCount()-1);
            	}
            			
            	illuminationBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = illuminationBox;//parent.replaceCombobox(illuminationBox);
            } else if (EditorUtil.CONTRAST_METHOD.equals(key)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) 
            		contrastMethodBox.setSelectedItem(selected);
            	else {
            		set = false;
            		contrastMethodBox.setSelectedIndex(
            				contrastMethodBox.getItemCount()-1);
            	}
            			
            	contrastMethodBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = contrastMethodBox;//parent.replaceCombobox(contrastMethodBox);
            } else if (EditorUtil.MODE.equals(key)) {
            	selected = model.getChannelEnumerationSelected(Editor.MODE, 
            			(String) value);
            	if (selected != null) modeBox.setSelectedItem(selected);
            	else {
            		set = false;
            		modeBox.setSelectedIndex(modeBox.getItemCount()-1);
            	}
            	modeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = modeBox;//parent.replaceCombobox(modeBox);
            } else {
            	if (value instanceof Number) {
            		area = UIUtilities.createComponent(NumericalTextField.class, 
                			null);
            		String v = "";
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
            	} else {
            		area = UIUtilities.createComponent(OMETextArea.class, null);
                	if (value == null || value.equals("")) {
                		set = false;
                		value = AnnotationUI.DEFAULT_TEXT;
                	}
                	((OMETextArea) area).setEditable(false);
                	((OMETextArea) area).setText((String) value);
                	((OMETextArea) area).setEditedColor(
                			UIUtilities.EDITED_COLOR);
            	}
            }
            area.setEnabled(!set);
            comp = new DataComponent(label, area);
            comp.setEnabled(false);
			comp.setSetField(!notSet.contains(key));
			fieldsGeneral.put(key, comp);
        }
	}
	
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
		parent.layoutFields(generalPane, unsetGeneral, fieldsGeneral, 
				unsetGeneralShown);
		
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.weightx = 1.0;
		constraints.gridy = 0;
		if (generalPane.isVisible()) {
			add(generalPane, constraints);
			++constraints.gridy;
		}
		if (detectorPane.isVisible()) {
			add(detectorPane, constraints);
	    	++constraints.gridy;
		}
		if (lightPane.isVisible()) {
			add(lightPane, constraints);
	    	++constraints.gridy;
		}
		if (lightPathPane != null) {
			add(lightPathPane, constraints);
	    	++constraints.gridy;
		}
		if (filterSetPane != null) {
			add(filterSetPane, constraints);
	    	++constraints.gridy;
		}
    	constraints.fill = GridBagConstraints.HORIZONTAL;
    	add(exposureTask, constraints);
    	parent.attachListener(fieldsGeneral);
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 * @param channel 	The channel to display. Mustn't be <code>null</code>.
	 */
	ChannelAcquisitionComponent(AcquisitionDataUI parent, 
			EditorModel model, ChannelData channel)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		if (channel == null)
			throw new IllegalArgumentException("No channel.");
		this.model = model;
		this.channel = channel;
		this.parent = parent;
		initComponents();
	}
	
	/**
	 * Returns the index of the channel hosted by this component.
	 * 
	 * @return See above.
	 */
	int getChannelIndex() { return channel.getIndex(); }
	
	/**
	 * Sets the color associated to that channel.
	 * 
	 * @param color The associated color.
	 */
	void setChannelColor(Color color)
	{
		if (color == null) return;
		if (icon == null) {
			icon = new ColourIcon(color);
			icon.paintLineBorder(true);
		} else icon.setColour(color);
	}
	
	/** 
	 * Returns the color icon associated to the channel or <code>null</code>.
	 * 
	 * @return
	 */
	Icon getIcon() { return icon; }
	
	/**
	 * Displays the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		if (channel.getIndex() != index) return;
		if (!init) {
			init = true;
			resetBoxes();
			removeAll();
	    	fieldsGeneral.clear();
	    	ChannelAcquisitionData data = model.getChannelAcquisitionData(
	    			channel.getIndex());
	    	Map<String, Object> details =
	    		EditorUtil.transformChannelData(channel);
	    	List notSet = (List) details.get(EditorUtil.NOT_SET);
	    	generalPane.setVisible(false);
	    	if (notSet.size() != EditorUtil.MAX_FIELDS_CHANNEL) {
	    		transformGeneralSource(details);
				generalPane.setVisible(true);
			}
			//if no detector info: don't display.
			details = EditorUtil.transformDetectorAndSettings(data);
			notSet = (List) details.get(EditorUtil.NOT_SET);
			detectorPane.setVisible(false);
			if (notSet.size() != EditorUtil.MAX_FIELDS_DETECTOR_AND_SETTINGS) {
				detectorPane.displayDetector(details);
				detectorPane.setVisible(true);
			}
			details = EditorUtil.transformLightSourceAndSetting(data);
			String kind = (String) details.get(EditorUtil.LIGHT_TYPE);
			details.remove(EditorUtil.LIGHT_TYPE);
			notSet = (List) details.get(EditorUtil.NOT_SET);
			lightPane.setVisible(false);
			int n = EditorUtil.MAX_FIELDS_LIGHT_AND_SETTINGS;
			if (LightSourceData.LASER.equals(kind)) 
				n = EditorUtil.MAX_FIELDS_LASER_AND_SETTINGS;
			
			if (notSet.size() != n) {
				lightPane.displayLightSource(kind, details);
				lightPane.setVisible(true);
			}
			DataObject set = data.getFilterSet();
			if (set != null)
				filterSetPane = new FilterGroupComponent(parent, model, set);
			set = data.getLightPath();
			if (set != null)
				lightPathPane = new FilterGroupComponent(parent, model, set);
			buildGUI();
			//load the exposure time
			exposureTask.setCollapsed(false);
		}
	}
	
    /**
     * Formats time into a more human readable format
     * 
     * @param tInS
     *            The time in seconds
     * @return See above.
     */
    private String getReadableTime(double tInS) {
        if (tInS == 0.0)
            return "0s";

        Calendar date = Calendar.getInstance();
        date = DateUtils.truncate(date, Calendar.YEAR);
        date.add(Calendar.MILLISECOND, (int) (tInS * 1000));

        int d, h, m, s, ms;

        if (tInS > (23 * 60 * 60)) {
            date = DateUtils.round(date, Calendar.MINUTE);
            d = date.get(Calendar.DAY_OF_YEAR) - 1;
            h = date.get(Calendar.HOUR_OF_DAY);
            m = date.get(Calendar.MINUTE);
            return d + "d " + (h > 0 ? h + "h " : "")
                    + (m > 0 ? m + "min " : "");
        } else if (tInS > (59 * 60)) {
            date = DateUtils.round(date, Calendar.MINUTE);
            h = date.get(Calendar.HOUR_OF_DAY);
            m = date.get(Calendar.MINUTE);
            return h + "h " + (m > 0 ? m + "min" : "");
        } else if (tInS > 59) {
            date = DateUtils.round(date, Calendar.SECOND);
            m = date.get(Calendar.MINUTE);
            s = date.get(Calendar.SECOND);
            return m + "m " + (s > 0 ? s + "s" : "");
        } else if (tInS > 0.9) {
            date = DateUtils.round(date, Calendar.MILLISECOND);
            s = date.get(Calendar.SECOND);
            ms = date.get(Calendar.MILLISECOND);
            return s + "s " + (ms > 0 ? ms + "ms" : "");
        } else {
            double milis = UIUtilities.round((tInS * 1000), 2);
            return String.format("%.2g", milis) + "ms";
        }
    }
	
	/**
	 * Sets the plane info for the specified channel.
	 * 
	 * @param index  The index of the channel.
	 */
	void setPlaneInfo(int index)
	{
		if (channel.getIndex() != index) return;
		Collection result = model.getChannelPlaneInfo(index);
		String[][] values = new String[2][result.size()+1];
		String[] names = new String[result.size()+1];
		int i = 0;
		Iterator j = result.iterator();
		PlaneInfo info;
		Map<String, Object> details;
		List<String> notSet;
		names[0] = "t index";
		values[0][i] = "Delta T";
		values[1][i] = "Exposure";
		i++;
		while (j.hasNext()) {
			info = (PlaneInfo) j.next();
			details = EditorUtil.transformPlaneInfo(info);
			notSet = (List<String>) details.get(EditorUtil.NOT_SET);	
			if (!notSet.contains(EditorUtil.DELTA_T)) {
			    if(details.get(EditorUtil.DELTA_T) instanceof BigResult) {
			        MetadataViewerAgent.logBigResultExeption(this, details.get(EditorUtil.DELTA_T) , EditorUtil.DELTA_T);
			        values[0][i] = "N/A";
			    } else {
			        double tInS = ((Double)details.get(EditorUtil.DELTA_T));
			        values[0][i] = getReadableTime(tInS);
			    }
			}
			else
				values[0][i] = "--";

			if (!notSet.contains(EditorUtil.EXPOSURE_TIME)) {
			    if(details.get(EditorUtil.EXPOSURE_TIME) instanceof BigResult) {
                    MetadataViewerAgent.logBigResultExeption(this, details.get(EditorUtil.EXPOSURE_TIME) , EditorUtil.EXPOSURE_TIME);
                    values[1][i] = "N/A";
                } else {
                    double tInS = ((Double)details.get(EditorUtil.EXPOSURE_TIME));
                    values[1][i] = getReadableTime(tInS);
                }
			}
			else
				values[1][i] = "--";
			names[i] = "t="+(i-1);
			i++;
		}
		if (i > 1) {
			JTable table = new JTable(values, names);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setShowGrid(true);
			table.setGridColor(Color.LIGHT_GRAY);
			JScrollPane pane = new JScrollPane(table);
			Dimension d = table.getPreferredSize();
			Dimension de = exposureTask.getPreferredSize();
			pane.setPreferredSize(new Dimension(de.width-10, 4*d.height));
			exposureTask.add(pane);
			exposureTask.setVisible(true);
		}
		else {
			exposureTask.setVisible(false);
		}
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		boolean b = parent.hasDataToSave(fieldsGeneral);
		if (b) return true;
		b = detectorPane.hasDataToSave();
		if (b) return true;
		b = lightPane.hasDataToSave();
		if (b) return true;
		if (lightPathPane != null) {
			b = lightPathPane.hasDataToSave();
			if (b) return true;
		}
		if (filterSetPane != null) {
			b = filterSetPane.hasDataToSave();
			if (b) return true;
		}
		return false;
	}

	/**
	 * Prepares the data to save.
	 * 
	 * @return See above.
	 */
	List<Object> prepareDataToSave()
	{
		List<Object> data = new ArrayList<Object>();
		if (!hasDataToSave()) return data;
		String key;
		DataComponent comp;
		Object value;
		EnumerationObject enumObject;
		Number number;
		Iterator i; 
		Entry entry;
		if (channel.isDirty()) {
			i = fieldsGeneral.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				key = (String) entry.getKey();
				comp = (DataComponent) entry.getValue();
				if (comp.isDirty()) {
					value = comp.getAreaValue();
					if (EditorUtil.NAME.equals(key)) {
						channel.setName((String) value);
					} else if (EditorUtil.PIN_HOLE_SIZE.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Float.class);
						if (number != null)
							channel.setPinholeSize(new LengthI((Float) number, UnitsFactory.Channel_PinholeSize));
					} else if (EditorUtil.ND_FILTER.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Float.class);
						if (number != null)
							channel.setNDFilter((Float) number);
					} else if (EditorUtil.POCKEL_CELL.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Integer.class);
						if (number != null)
							channel.setPockelCell((Integer) value);
					} else if (EditorUtil.EMISSION.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
						        Double.class);
						if (number != null)
							channel.setEmissionWavelength(new LengthI((Double) number, UnitsFactory.Channel_EmissionWavelength));
					} else if (EditorUtil.EXCITATION.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Double.class);
						if (number != null)
							channel.setExcitationWavelength(new LengthI((Double) number, UnitsFactory.Channel_ExcitationWavelength));
					} else if (EditorUtil.ILLUMINATION.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof Illumination)
							channel.setIllumination(
								(Illumination) enumObject.getObject());
					} else if (EditorUtil.MODE.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof AcquisitionMode)
							channel.setMode(
								(AcquisitionMode) enumObject.getObject());
					} else if (EditorUtil.CONTRAST_METHOD.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof ContrastMethod)
							channel.setContrastMethod(
								(ContrastMethod) enumObject.getObject());
					}
				}
			}
			data.add(channel);
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
			displayUnsetGeneralFields();
		} else if (UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) {
			parent.loadPlaneInfo(channel.getIndex());
		}
	}

}
