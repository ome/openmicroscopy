/*
 * org.openmicroscopy.shoola.agents.rnd.model.WavelengthPane
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.rnd.editor;

//Java imports
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ChannelPane
	extends JPanel
{
	
    private static final String    NANOMETER = " \u00B5m";
    
	private ChannelEditorManager   manager;
    
	MultilineLabel                 interpretationArea;
    
	JTextField                     excitation, fluor,  ndFilter, 
                                   auxLightAttenuation, detectorGain,
                                   detectorOffset, lightAttenuation,
                                   auxLightWavelength, pinholeSize,
                                   lightWavelength, samplesPerPixel,
                                   auxTechnique, contrastMethod, mode,
                                   illuminationType;
    
	ChannelPane(ChannelEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildSummaryPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}

	/** Build the panel with info. */
	private JPanel buildSummaryPanel()
	{
		JPanel  p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildTable());
		p.setOpaque(false);
		return p;
	}
	
	/** 
	 * A <code>2x16</code> table model to view channel summary.
	 * The first column contains the property names 
	 * (emission, interpretation, excitation, fluorescence)
	 * and the second column holds the corresponding values. 
	 * <code>interpretation</code>, <code>excitation</code>, 
	 * <code>fluorescence</code> values are marked as editable. 
	 */
	private JScrollPane buildTable()
	{
		TableComponent table = new TableComponent(17, 2);
		setTableLayout(table);
		ChannelData wd = manager.getChannelData();
		
		//First row 
        String s = " Emission (in "+NANOMETER+")";
		JLabel label = new JLabel(s);
		table.setValueAt(label, 0, 0);
		table.setValueAt(new JLabel(""+wd.getNanometer()), 0, 1);
		
		//Third row.
        s = " Excitation (in "+NANOMETER+")";
		label = new JLabel(s);
        s = "";
        if (wd.getExcitation() >= 0) s = ""+wd.getExcitation();   
	  	excitation = new JTextField(s);
	  	excitation.setForeground(ChannelEditor.STEELBLUE);
	  	excitation.setEnabled(true);

	  	table.setValueAt(label, 1, 0);	
	  	table.setValueAt(excitation, 1, 1);	
	  	
		//Second row
		label = new JLabel(" Interpretation");
		interpretationArea = new MultilineLabel(wd.getInterpretation());
		interpretationArea.setForeground(ChannelEditor.STEELBLUE);
		interpretationArea.setEditable(true);
		JScrollPane scrollPane = new JScrollPane(interpretationArea);
		scrollPane.setPreferredSize(ChannelEditor.DIM_SCROLL_TABLE);
		table.setValueAt(label, 2, 0);
		table.setValueAt(scrollPane, 2, 1);
		
		//Fourth row.
		label = new JLabel(" Fluor");
		fluor = new JTextField(wd.getFluor());
		fluor.setForeground(ChannelEditor.STEELBLUE);
		fluor.setEnabled(true);
		table.setValueAt(label, 3, 0);
		table.setValueAt(fluor, 3, 1);
        
        int i = 4;
        //Fith
        label = new JLabel("ND filter");
        ndFilter =  new JTextField(""+wd.getNDFilter());
        ndFilter.setForeground(ChannelEditor.STEELBLUE);
        ndFilter.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(ndFilter, i, 1);
        i++;
        
        label = new JLabel("Aux Light Attenuation");
        auxLightAttenuation =  new JTextField(""+wd.getAuxLightAttenuation());
        auxLightAttenuation.setForeground(ChannelEditor.STEELBLUE);
        auxLightAttenuation.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(auxLightAttenuation, i, 1);
        i++;
        label = new JLabel("Aux Light Wavelength");
        auxLightWavelength =  new JTextField(wd.getAuxLightWavelength());
        auxLightWavelength.setForeground(ChannelEditor.STEELBLUE);
        auxLightWavelength.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(auxLightWavelength, i, 1);
        i++;
        label = new JLabel("Aux Technique");
        auxTechnique =  new JTextField(wd.getAuxTechnique());
        auxTechnique.setForeground(ChannelEditor.STEELBLUE);
        auxTechnique.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(auxTechnique, i, 1);
        i++;
        label = new JLabel("Light Attenuation");
        lightAttenuation =  new JTextField(""+wd.getLightAttenuation());
        lightAttenuation.setForeground(ChannelEditor.STEELBLUE);
        lightAttenuation.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(lightAttenuation, i, 1);
        i++;
        label = new JLabel("Light Wavelength");
        lightWavelength =  new JTextField(wd.getLightWavelength());
        lightWavelength.setForeground(ChannelEditor.STEELBLUE);
        lightWavelength.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(lightWavelength, i, 1);
        i++;
        label = new JLabel("Detector Gain");
        detectorGain =  new JTextField(""+wd.getDetectorGain());
        detectorGain.setForeground(ChannelEditor.STEELBLUE);
        detectorGain.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(detectorGain, i, 1);
        i++;
        label = new JLabel("Detector Offset");
        detectorOffset =  new JTextField(""+wd.getDetectorOffset());
        detectorOffset.setForeground(ChannelEditor.STEELBLUE);
        detectorOffset.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(detectorOffset, i, 1);
        i++;
        label = new JLabel("Pin hole Size");
        pinholeSize =  new JTextField(wd.getPinholeSize());
        pinholeSize.setForeground(ChannelEditor.STEELBLUE);
        pinholeSize.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(pinholeSize, i, 1);
        i++;
        label = new JLabel("Contrast Method");
        contrastMethod =  new JTextField(wd.getContrastMethod());
        contrastMethod.setForeground(ChannelEditor.STEELBLUE);
        contrastMethod.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(contrastMethod, i, 1);
        i++;
        label = new JLabel("Mode");
        mode =  new JTextField(wd.getMode());
        mode.setForeground(ChannelEditor.STEELBLUE);
        mode.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(mode, i, 1);
        i++;
        label = new JLabel("Illumination type");
        illuminationType =  new JTextField(wd.getIlluminationType());
        illuminationType.setForeground(ChannelEditor.STEELBLUE);
        illuminationType.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(illuminationType, i, 1);
        i++;
        label = new JLabel("Samples per pixel");
        samplesPerPixel =  new JTextField(wd.getSamplesPerPixel());
        samplesPerPixel.setForeground(ChannelEditor.STEELBLUE);
        samplesPerPixel.setEnabled(true);
        table.setValueAt(label, i, 0);
        table.setValueAt(samplesPerPixel, i, 1);
		return new JScrollPane(table);
	}
	
	/** Set the layout of the table. */
	private void setTableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setRowHeight(ChannelEditor.ROW_HEIGHT);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}
	
}
