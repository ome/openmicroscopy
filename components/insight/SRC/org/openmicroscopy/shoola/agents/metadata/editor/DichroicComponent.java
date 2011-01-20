/*
 * org.openmicroscopy.shoola.agents.metadata.editor.DichroicComponent
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
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Displays a dichroic.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author   Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class DichroicComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fields;
	
	/** Button to show or hides the unset fields. */
	private JLabelButton						unset;
	
	/** Flag indicating the unset fields displayed. */
	private boolean								unsetShown;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI	parent;
	
	/** Reference to the Model. */
	private EditorModel			model;

	/** Initializes the components. */
	private void initComponents()
	{
		fields = new LinkedHashMap<String, DataComponent>();
		unset = null;
		unsetShown = false;
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetFields()
	{
		unsetShown = !unsetShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetShown) s = AcquisitionDataUI.HIDE_UNSET;
		unset.setText(s);
		parent.layoutFields(this, unset, fields, unsetShown);
	}
	
	/**
	 * Transforms the dichroic.
	 * 
	 * @param details The value to transform.
	 */
	private void transformDichroicSource(Map<String, Object> details)
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
		if (notSet.size() > 0 && unset == null) {
			unset = parent.formatUnsetFieldsControl();
			unset.addPropertyChangeListener(this);
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
			fields.put(key, comp);
        }
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createTitledBorder("Dichroic"));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The UI reference.
	 * @param model  Reference to the model.
	 */
	DichroicComponent(AcquisitionDataUI parent, EditorModel model)
	{
		this.parent = parent;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Transforms the dichroic.
	 * 
	 * @param details The value to transform.
	 */
	void displayDichroic(Map<String, Object> details)
	{
		fields.clear();
		transformDichroicSource(details);
		parent.layoutFields(this, unset, fields, unsetShown);
    	parent.attachListener(fields);
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() { return parent.hasDataToSave(fields); }
	
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
			displayUnsetFields();
	}
	
}
