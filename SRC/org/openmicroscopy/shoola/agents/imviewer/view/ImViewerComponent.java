/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerComponent
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

package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements the {@link ImViewer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerModel
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerUI
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerControl
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
class ImViewerComponent
    extends AbstractComponent
    implements ImViewer
{

    /** Text message displayed when an image is rendered. */
    private static final String RENDERING_MSG = "Render image";
    
    /** The Model sub-component. */
    private ImViewerModel    model;
    
    /** The Control sub-component. */
    private ImViewerControl  controller;
    
    /** The View sub-component. */
    private ImViewerUI       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component. Mustn't be <code>null</code>.
     */
    ImViewerComponent(ImViewerModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new ImViewerControl();
        view = new ImViewerUI(model.getImageName());
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(controller, model);
    }

    /**
     * Returns the Model sub-component.
     * 
     * @return See above.
     */
    ImViewerModel getModel() { return model; }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#activate()
     */
    public void activate()
    {
        int state = model.getState();
        switch (state) {
            case NEW:
                model.fireRenderingControlLoading();
                fireStateChange();
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            default:
        }
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#discard()
     */
    public void discard()
    {
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getState()
     */
    public int getState() { return model.getState(); }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setStatus(String, int)
     */
    public void setStatus(String description, int perc)
    {
        if (model.getState() == DISCARDED) return;
        view.setStatus(description, perc, false);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setZoomFactor(double)
     */
    public void setZoomFactor(double factor)
    {
        if (factor > ZoomAction.MAX_ZOOM_FACTOR ||
                factor < ZoomAction.MIN_ZOOM_FACTOR)
                throw new IllegalArgumentException("The zoom factor is value " +
                        "between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
                        ZoomAction.MAX_ZOOM_FACTOR);
        model.setZoomFactor(factor);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setRateImage(int)
     */
    public void setRateImage(int level)
    {

    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setColorModel(int)
     */
    public void setColorModel(int colorModel)
    {
        String m;
        switch (colorModel) {
            case ColorModelAction.GREY_SCALE_MODEL:
                m = GREY_SCALE_MODEL;
                break;
            case ColorModelAction.RGB_MODEL:
                m = RGB_MODEL;
                break;
            case ColorModelAction.HSB_MODEL:
                m = HSB_MODEL;
                break;
            default:
                throw new IllegalArgumentException("Color model not supported");
        }
        model.setColorModel(m);
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setSelectedXYPlane(int, int)
     */
    public void setSelectedXYPlane(int z, int t)
    {
        int defaultZ = model.getDefaultZ();
        int defaultT = model.getDefaultT();
        
        if (defaultZ == z && defaultT == t) return;
        if (defaultZ != z) {
            firePropertyChange(ImViewer.Z_SELECTED_PROPERTY, 
                    new Integer(defaultZ), new Integer(z));
        }
        if (defaultT != t) {
            firePropertyChange(ImViewer.T_SELECTED_PROPERTY, 
                    new Integer(defaultT), new Integer(t));
        }
        model.setSelectedXYPlane(z, t);
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setImage(BufferedImage)
     */
    public void setImage(BufferedImage image)
    {
        if (model.getState() != LOADING_IMAGE) 
            throw new IllegalStateException("This method can only be invoked " +
                    "in the LOADING_IMAGE state.");
        model.setImage(image);
        view.setStatus("", -1, true);
        fireStateChange();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#playChannelMovie()
     */
    public void playChannelMovie()
    {
        if (model.getState() == READY) {
            model.playMovie();
            fireStateChange();
        }    
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelColor(int, Color)
     */
    public void setChannelColor(int index, Color c)
    {
        model.setChannelColor(index, c);
        if (model.isChannelActive(index)) renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelSelection(int, boolean)
     */
    public void setChannelSelection(int index, boolean b)
    {
        //depends on model
        if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
            if (model.isChannelActive(index)) return;
            for (int i = 0; i < model.getMaxC(); i++)
                model.setChannelActive(i, i == index);  
        } else {
            model.setChannelActive(index, b);
        }
        view.setChannelsSelection();
        renderXYPlane();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelMetadata(Object)
     */
    public void setChannelMetadata(Object metadata)
    {
        model.setChannelMetadata(metadata);
        view.buildComponents();
        view.setSize(500, 500);
        view.setOnScreen();
        view.setStatus(RENDERING_MSG, -1, false);
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setRenderingControl(RenderingControl)
     */
    public void setRenderingControl(RenderingControl result)
    {
        model.setRenderingControl(result);
        LoadingWindow window = view.getLoadingWindow();
        window.setStatus("rendering settings. Loading: metadata");
        window.setProgress(50);
        model.fireChannelMetadataLoading();
        fireStateChange();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#renderXYPlane()
     */
    public void renderXYPlane()
    {
        //Check state
        model.fireImageRetrieval();
        fireStateChange();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelActive(int, boolean)
     */
    public void setChannelActive(int index, boolean b)
    {
        model.setChannelActive(index, b);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#displayChannelMovie()
     */
    public void displayChannelMovie()
    {
        view.setChannelsSelection();
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxC()
     */
    public int getMaxC()
    {
        return model.getMaxC();
    }
   
}
