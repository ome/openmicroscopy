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
	private static final Color		BACKGROUND = Color.WHITE;
	private CodomainPane			codomainPane;
	private DomainPane				domainPane;
	private GraphicsRepresentation  gRepresentation;
	private JLayeredPane			layeredPane;
	
	/** Reference to the {@link QuantumPaneManager manager}. */
	private QuantumPaneManager	manager;
	
	//TODO: retrive codomain settings.
	public QuantumPane(RenderingAgtCtrl eventManager, String[] waves, 
					int mini, int maxi)
	{
		//TEST
		QuantumDef qDef = new QuantumDef(QuantumFactory.LINEAR, 16, 1, 
									0, QuantumFactory.DEPTH_8BIT,
									QuantumFactory.DEPTH_8BIT);
		manager = new QuantumPaneManager(eventManager, this);
		
		//Retrieve user settings
		codomainPane = new CodomainPane(eventManager.getRegistry(), manager);
		//TODO: cannot pass quantumDef
		domainPane = new DomainPane(eventManager.getRegistry(), manager, waves, 
									qDef);
		gRepresentation = new GraphicsRepresentation(manager, qDef, mini, maxi);
		gRepresentation.setDefaultLinear(mini, maxi);
		manager.setMinimum(mini);
		manager.setMaximum(maxi);
		initLayeredPane();
		//buildGUI();
	}

	public QuantumPaneManager getManager()
	{
		return manager;
	}

	public CodomainPane getCodomainPane()
	{
		return codomainPane;
	}

	public DomainPane getDomainPane()
	{
		return domainPane;
	}

	public GraphicsRepresentation getGRepresentation()
	{
		return gRepresentation;
	}

	public JLayeredPane getLayeredPane()
	{
		return layeredPane;
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		//add(domainPane);
		//add(codomainPane);
		//add(buildCurveGUI());
	}
	
	/** 
	 * Builds a layeredPane containing the GraphicsRepresentation.
	 *
	 * @return the above mentioned.
	 */   
	private void initLayeredPane()
	{
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(GraphicsRepresentation.width, 
										GraphicsRepresentation.height));
		layeredPane.setBounds(0, 0, GraphicsRepresentation.width, 
							GraphicsRepresentation.height);
		gRepresentation.setSize(GraphicsRepresentation.height, 
								GraphicsRepresentation.height);
		gRepresentation.setBackground(BACKGROUND);
		layeredPane.add(gRepresentation);
	}



}
