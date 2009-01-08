/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererComponent
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
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements the {@link Renderer} interface to provide the functionality
 * required of the renderer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 * 
 * @see org.openmicroscopy.shoola.agents.imviewer.rnd.RendererModel
 * @see org.openmicroscopy.shoola.agents.imviewer.rnd.RendererUI
 * @see org.openmicroscopy.shoola.agents.imviewer.rnd.RendererControl
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
class RendererComponent
    extends AbstractComponent
    implements Renderer
{
    
    /** The Model sub-component. */
    private RendererModel   model;
    
    /** The Control sub-component. */
    private RendererControl controller;
    
    /** The View sub-component. */
    private RendererUI      view;
    
    /**
     * Sets the index of the selected channel.
     * 
     * @param oldChannel	The index of the previously selected channel.
     * @param c				The index of the newly selected channel.
     */
    private void setSelectedChannel(int oldChannel, int c)
    {
    	if (oldChannel == c) return;
    	model.setSelectedChannel(c);
        view.setSelectedChannel(c);
        firePropertyChange(SELECTED_CHANNEL_PROPERTY, 
                    	new Integer(oldChannel), new Integer(c));
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component. Mustn't be <code>null</code>.
     */
    RendererComponent(RendererModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new RendererControl();
        view = new RendererUI();
    }
    
    /** 
     * Links up the MVC triad. 
     * 
     * @param metadataView The The view of the metadata.
     */
    void initialize(JComponent metadataView)
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(controller, model, metadataView);
    }
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#discard()
     */
    public void discard()
    {
        model.discard();
        //view.setVisible(false);
        //view.dispose();
    }
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#updateCodomainMap(CodomainMapContext)
     */
    /*
    public void updateCodomainMap(CodomainMapContext ctx)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	 model.getParentModel().addHistoryItem();
        	 model.updateCodomainMap(ctx);
             firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
            		 Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }
    */

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setInputInterval(double, double, boolean)
     */
    public void setInputInterval(double s, double e, boolean released)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	if (released) model.getParentModel().addHistoryItem();
        	model.setInputInterval(s, e);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
            					Boolean.TRUE);
            firePropertyChange(INPUT_INTERVAL_PROPERTY, Boolean.FALSE, 
                                Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setCodomainInterval(int, int, boolean)
     */
    public void setCodomainInterval(int s, int e, boolean released)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	if (released) model.getParentModel().addHistoryItem();
        	 model.setCodomainInterval(s, e);
             firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
            		 			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setBitResolution(int)
     */
    public void setBitResolution(int v)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	model.getParentModel().addHistoryItem();
        	model.setBitResolution(v);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setSelectedChannel(int, boolean)
     */
    public void setSelectedChannel(int c, boolean checkIfActive)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        int selectedChannel  = model.getSelectedChannel();
        //if (selectedChannel == c) return;
        if (!checkIfActive) { 
        	setSelectedChannel(selectedChannel, c);
        } else {
        	boolean active = model.isChannelActive(c);
        	if (active)
        		setSelectedChannel(selectedChannel, c);
        	else {
        		List actives = model.getActiveChannels();
        		if (actives != null && actives.size() > 0) {
        			int maxC = model.getMaxC();
        			int index = c+1;
        			int selected = -1;
        			while (selected == -1) {
						if (actives.contains(new Integer(index)))
							selected = index;
						if (index == maxC) index = 0;
						else index++;
					}
        			setSelectedChannel(selectedChannel, selected);
        		}
        	}
        }
       
    }

    /**
     * @see org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer
     * #setChannelButtonColor(int)
     * 
     */
    public void setChannelButtonColor(int changedChannel)
    {
    	view.setChannelButtonColor(changedChannel);  
    }
    
    /**
     *
     * @see org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer#
     * setColorModelChanged()
     */
    public void setColorModelChanged()
    {
    	view.setColorModelChanged();
    }
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setFamily(String)
     */
    public void setFamily(String family)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	model.getParentModel().addHistoryItem();
        	model.setFamily(family);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setCurveCoefficient(double)
     */
    public void setCurveCoefficient(double k)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	model.getParentModel().addHistoryItem();
        	model.setCurveCoefficient(k);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setNoiseReduction(boolean)
     */
    public void setNoiseReduction(boolean b)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	model.getParentModel().addHistoryItem();
        	model.setNoiseReduction(b);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getState()
     */
    public int getState() { return model.getState(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getParentModel()
     */
    public ImViewer getParentModel() { return model.getParentModel(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getCodomainMapContext(Class)
     */
    /*
    public CodomainMapContext getCodomainMapContext(Class mapType)
    {
        return model.getCodomainMap(mapType);
    }
*/
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#removeCodomainMap(Class mapType)
     */
    /*
    public void removeCodomainMap(Class mapType)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        try {
        	model.getParentModel().addHistoryItem();
        	model.removeCodomainMap(mapType);
            view.removeCodomainMap(mapType);
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
            					Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }
    */

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#addCodomainMap(Class mapType)
     */
    /*
    public void addCodomainMap(Class mapType)
    {
        if (model.getParentModel().getHistoryState() == ImViewer.CHANNEL_MOVIE)
            return;
        if (model.getCodomainMap(mapType) != null) return; //already
        try {
        	model.getParentModel().addHistoryItem();
        	model.addCodomainMap(mapType);
        	view.addCodomainMap(mapType);
        	firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
        			Boolean.TRUE);
		} catch (Exception ex) {
			model.getParentModel().reload(ex);
		}
    }
*/
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getWindowStart()
     */
    public double getWindowStart() { return model.getWindowStart(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getWindowEnd()
     */
    public double getWindowEnd() { return model.getWindowEnd(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getGlobalMin()
     */
    public double getGlobalMin() { return model.getGlobalMin(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getGlobalMax()
     */
    public double getGlobalMax() { return model.getGlobalMax(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getUI()
     */
    public JComponent getUI() { return view; }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setRenderingControl(RenderingControl)
     */
	public void setRenderingControl(RenderingControl rndControl)
	{
		if (rndControl == null)
			throw new IllegalArgumentException("No rendering " +
					"control specified.");		
		model.setRenderingControl(rndControl);
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setRenderingControl(RenderingControl)
     */
	public void resetRndSettings()
	{
		view.resetDefaultRndSettings();
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getHighestValue()
     */
	public double getHighestValue() { return model.getHighestValue(); }

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getLowestValue()
     */
	public double getLowestValue() { return model.getLowestValue(); }

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#switchRndControl()
     */
	public void switchRndControl()
	{
		// TODO Auto-generated method stub
		
	}
    
}
