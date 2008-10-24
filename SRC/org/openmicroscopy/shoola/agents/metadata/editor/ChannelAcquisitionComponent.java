/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ChannelAcquisitionComponent 
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.data.model.Mapper;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
 * @since 3.0-Beta4
 */
class ChannelAcquisitionComponent
	extends JPanel
{

	/** The channel data. */
	private ChannelMetadata channel;
	
	/** The component displaying the illumination's options. */
	private JComboBox		illuminationBox;
	
	/** The component displaying the contrast Method options. */
	private JComboBox		contrastMethodBox;
	
	/** The component displaying the contrast Method options. */
	private JComboBox		modeBox;
	
	/** Initializes the components */
	private void initComponents()
	{
		illuminationBox = new JComboBox(Mapper.ILLUMINATION);
		illuminationBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		Font f = illuminationBox.getFont();
		int size = f.getSize()-2;
		illuminationBox.setBorder(null);
		illuminationBox.setFont(f.deriveFont(f.getStyle(), size));
		
		contrastMethodBox = new JComboBox(Mapper.CONSTRAST_METHOD);
		contrastMethodBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		contrastMethodBox.setBorder(null);
		contrastMethodBox.setFont(f.deriveFont(f.getStyle(), size));
		
		modeBox = new JComboBox(Mapper.MODE);
		modeBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		modeBox.setBorder(null);
		modeBox.setFont(f.deriveFont(f.getStyle(), size));
	}
	
	 /**
     * Builds and lays out the body displaying the channel info.
     * 
     * @return See above.
     */
    private JPanel buildChannelInfo()
    {
        Map<String, String> details = EditorUtil.transformChannelData(channel);
        JPanel content = new JPanel();
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        content.setLayout(new GridBagLayout());
        //content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JComponent area;
        String key, value;
        label = new JLabel();
        Font font = label.getFont();
        int sizeLabel = font.getSize()-2;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            if (key.equals(EditorUtil.ILLUMINATION)) {
            	area = illuminationBox;
            } else if (key.equals(EditorUtil.CONTRAST_METHOD)) {
            	area = contrastMethodBox;
            } else if (key.equals(EditorUtil.MODE)) {
            	area = modeBox;
            } else area = UIUtilities.createComponent(JTextArea.class, null);
            if (value == null || value.equals("")) {
            	value = AnnotationUI.DEFAULT_TEXT;
            }
            if (area instanceof JTextArea) {
            	 ((JTextArea) area).setEditable(false);
            	 ((JTextArea) area).setText(value);
            }
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        return content;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	add(buildChannelInfo());
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param channel The channel to display.
	 */
	ChannelAcquisitionComponent(ChannelMetadata channel)
	{
		this.channel = channel;
		initComponents();
		buildGUI();
	}
	
}
