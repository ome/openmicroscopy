/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ImageInfoUI 
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
import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageInfoUI 
	extends AnnotationUI
{

	/** The title associated to this component. */
	private static final String 	TITLE = "Image's Info ";
	
	/** Flag indicating if the channels have to be loaded. */
	private boolean		expanded;
	
	/** The area displaying the channels information. */
	private JTextField	channelsArea;

	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BorderLayout(0, 0));
		channelsArea =  new JTextField();
		channelsArea.setEditable(false);
		channelsArea.setEnabled(false);
	}
	
	/**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details)
    {
    	JPanel content = new JPanel();
    	double[] columns = {TableLayout.PREFERRED, TableLayout.FILL};
    	TableLayout layout = new TableLayout();
    	content.setLayout(layout);
    	layout.setColumn(columns);
    	Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        int index = 0;
        while (i.hasNext()) {
        	layout.insertRow(index, TableLayout.PREFERRED);
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            content.add(label, "0, "+index);
            if (key.equals(EditorUtil.WAVELENGTHS)) {
            	area = channelsArea;
            } else {
            	area = new JTextField(value);
                area.setEditable(false);
                area.setEnabled(false);
            }
            label.setLabelFor(area);
            content.add(area, "1, "+index);
            index++;
        }
        return content;
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	ImageInfoUI(EditorModel model)
	{
		super(model);
		initComponents();
		title = TITLE;
		TitledLineBorder border = new TitledLineBorder(title, getBackground());
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
	}
	
	/**
	 * Returns <code>true</code> if the node is expanded and the 
	 * channels are loaded, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isExpanded() { return expanded; }
	
	/**
	 * Sets the {@link #expanded} flag.
	 * 
	 * @param expanded The value to set.
	 */
	void setExpanded(boolean expanded) { this.expanded = expanded; }
	
	/**
	 * Sets the channels when loaded.
	 * 
	 * @param waves The value to set.
	 */
	void setChannelData(List waves)
	{
		if (waves == null) return;
		expanded = true;
		String s = "";
		Iterator k = waves.iterator();
		int j = 0;
		while (k.hasNext()) {
			s += ((ChannelMetadata) k.next()).getEmissionWavelength();
			if (j != waves.size()-1) s +=", ";
			j++;
		}
		channelsArea.setText(s);
		channelsArea.revalidate();
		channelsArea.repaint();
	}
	
	/**
	 * Overridden to lay out the image information.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		Object refObject = model.getRefObject();
    	if (!(refObject instanceof ImageData)) return;
    	PixelsData data = ((ImageData) refObject).getDefaultPixels();
		Map<String, String> details = EditorUtil.transformPixelsData(data);
		details.put(EditorUtil.WAVELENGTHS, "");
    	add(buildContentPanel(details));
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * No data to remove for now.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 *  No data to add for now.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }

	/**
	 * No data to save for now.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave() { return false; }
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		channelsArea.setText("");
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		removeAll();
	}

}
