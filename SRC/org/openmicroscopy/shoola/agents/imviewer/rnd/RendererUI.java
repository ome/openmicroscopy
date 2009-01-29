/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

//Third-party libraries

//Application-internal dependencies


/** 
* The {@link Renderer} view. Provides a menu bar, a status bar and a 
* panel hosting various controls.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class RendererUI
	extends JTabbedPane
{
  
	/** Identifies the {@link DomainPane}. */
	static final Integer        	DOMAIN = new Integer(0);

	/** Identifies the {@link CodomainPane}. */
	static final Integer        	CODOMAIN = new Integer(1);
	
	/** Reference to the control. */
	private RendererControl     			controller;

	/** Reference to the model. */
	private RendererModel       			model;

	/** The map hosting the controls pane. */
	private HashMap<Integer, ControlPane>	controlPanes;

	/** Initializes the components. */
	private void initComponents()
	{
	}

	/** Creates the panels hosting the rendering controls. */
	private void createControlPanes()
	{
		ControlPane p = new DomainPane(model, controller);
		p.addPropertyChangeListener(controller);
		controlPanes.put(DOMAIN, p);
		p = new CodomainPane(model, controller);
		p.addPropertyChangeListener(controller);
		controlPanes.put(CODOMAIN, p);
	}

	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param metadataView	The view of the metadata.
	 */
	private void buildGUI(JComponent metadataView)
	{
		setAlignmentX(LEFT_ALIGNMENT);
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		insertTab(pane.getPaneName(), null, new JScrollPane(pane), 
				pane.getPaneDescription(), pane.getPaneIndex());
		insertTab("Metadata", null, new JScrollPane(metadataView), 
				"Display the annotation and acquisition metadata", 
						ControlPane.METADATA_PANE_INDEX);
	}

	/**
	 * Creates a new instance. The method 
	 * {@link #initialize(RendererControl, RendererModel) initialize}
	 * should be called straight after.
	 */
	RendererUI()
	{
		controlPanes = new HashMap<Integer, ControlPane>(2);
	}

	/**
	 * Links the MVC triad.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 * @param metadataView	The view of the metadata.
	 */
	void initialize(RendererControl controller, RendererModel model, 
			JComponent metadataView)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		initComponents();
		createControlPanes();
		buildGUI(metadataView);
	}

	/**
	 * Updates the corresponding controls when a codomain transformation
	 * is added.
	 * 
	 * @param mapType The type of codomain transformation. 
	 */
	void addCodomainMap(Class mapType)
	{
		CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
		pane.addCodomainMap(mapType);
	}

	/**
	 * Updates the corresponding controls when a codomain transformation
	 * is added.
	 * 
	 * @param mapType The type of codomain transformation. 
	 */
	void removeCodomainMap(Class mapType)
	{
		CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
		pane.removeCodomainMap(mapType);
	}

	/** Sets the specified channel as current. */
	void setSelectedChannel()
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setSelectedChannel();
	}

	/** 
	 * Sets the color of the specified channel.
	 * 
	 * @param index The channel's index.
	 */
	void setChannelColor(int index)
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setChannelColor(index);
	}

	/** Sets the pixels intensity interval. */
	void setInputInterval()
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setInputInterval();
	}

	/** Resets the UI controls. */
	void resetDefaultRndSettings()
	{
		Iterator i = controlPanes.keySet().iterator();
		ControlPane pane;
		while (i.hasNext()) {
			pane = controlPanes.get(i.next());
			pane.resetDefaultRndSettings();
		}
	}

	/**
	 * This is a method which is triggered from the {@link RendererControl} 
	 * if the colour model has changed.
	 */
	void setColorModelChanged() 
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setColorModelChanged();
	}

	/** 
	 * Updates the UI when a new curve is selected i.e. when a new family
	 * is selected or when a new gamma value is selected.
	 */
	void onCurveChange()
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.onCurveChange();
	}
	
	/** Loads the metadata. */
	void loadMetadata() { model.loadMetadata(); }

	/** 
	 * Reacts to {@link ImViewer} change events.
	 * 
	 * @param b Pass <code>true</code> to enable the UI components, 
	 *          <code>false</code> otherwise.
	 */
	void onStateChange(boolean b)
	{
		if (controlPanes != null && controlPanes.size() > 0) {
			Iterator i = controlPanes.keySet().iterator();
			ControlPane pane;
			while (i.hasNext()) {
				pane = controlPanes.get(i.next());
				pane.onStateChange(b);
			}
		}
	}
	
	/** 
	 * Sets the index of the pane.
	 * 
	 * @param index The index of the selected pane.
	 */
	void setPaneIndex(int index)
	{
		if (index == ImViewer.RENDERER_INDEX) setSelectedIndex(0);
		else if (index == ImViewer.METADATA_INDEX) setSelectedIndex(1);
	}

	/**
	 * Brings up the color picker.
	 * 
	 * @param index The index of the channel.
	 */
	void showColorPicker(int index) { model.showColorPicker(index); }
	
}
