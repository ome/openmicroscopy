/*
 * org.openmicroscopy.shoola.agents.rnd.pane.QuantumMapping
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

package org.openmicroscopy.shoola.agents.rnd.pane;



//Java imports
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

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
public class QuantumPane
	extends JPanel
{
	
	/** set the background color of the layered pane. */
	private static final Color		BACKGROUND = Color.WHITE;
	
	/** Default index. */
	private static final int		INDEX = 0;
	
	private CodomainPane			codomainPane;
	private DomainPane				domainPane;
	private GraphicsRepresentation  gRepresentation;
	private JLayeredPane			layeredPane;
	
	/** Reference to the {@link QuantumPaneManager manager}. */
	private QuantumPaneManager		manager;
	
	public QuantumPane(RenderingAgtCtrl eventManager)
	{
		QuantumDef qDef = eventManager.getQuantumDef();
		manager = new QuantumPaneManager(eventManager, this);
		int mini = (int) eventManager.getGlobalChannelWindowStart(INDEX);
		int maxi = (int) eventManager.getGlobalChannelWindowEnd(INDEX);
		int s = 
			((Integer) eventManager.getChannelWindowStart(INDEX)).intValue();
		int e = 
			((Integer) eventManager.getChannelWindowEnd(INDEX)).intValue();
		codomainPane = new CodomainPane(eventManager.getRegistry(), manager);
		domainPane = new DomainPane(eventManager.getRegistry(), manager, 
									eventManager.getChannelData(), qDef, INDEX);
		gRepresentation = new GraphicsRepresentation(manager, qDef, mini, maxi);
		
		if (qDef.family == QuantumFactory.EXPONENTIAL)
			 gRepresentation.setDefaultExponential(s, e);
		else gRepresentation.setDefaultLinear(s, e);
		layeredPane = new JLayeredPane();
		buildLayeredPane(gRepresentation);
	}
	
	/** Set enabled the wavelenghts combobox. */
	public void setSelectionWavelengthsEnable(boolean b)
	{
		domainPane.getWavelengths().setEnabled(b);
	}
	
	/** 
	 * Set the selected wavelength
	 * 
	 * @param index		wavelength index.
	 */
	public void setSelectedWavelength(int index)
	{
		domainPane.getWavelengths().setSelectedIndex(index);
	}
	
	public CodomainPane getCodomainPane() { return codomainPane; }

	public DomainPane getDomainPane() { return domainPane; }

	public JLayeredPane getLayeredPane() { return layeredPane; }
	
	GraphicsRepresentation getGRepresentation() { return gRepresentation; }
	
	QuantumPaneManager getManager() { return manager; }
	
	void setGRepresentation(GraphicsRepresentation gr) { gRepresentation = gr;}
	
	/** 
	 * Builds a layeredPane containing the GraphicsRepresentation.
	 *
	 * @return the above mentioned.
	 */   
	void buildLayeredPane(GraphicsRepresentation gr)
	{
		layeredPane.setPreferredSize(new Dimension(GraphicsRepresentation.width, 
										GraphicsRepresentation.height));
		layeredPane.setBounds(0, 0, GraphicsRepresentation.width, 
							GraphicsRepresentation.height);
		gr.setSize(GraphicsRepresentation.height, 
					GraphicsRepresentation.height);
		gr.setBackground(BACKGROUND);
		layeredPane.add(gr);
	}

}
