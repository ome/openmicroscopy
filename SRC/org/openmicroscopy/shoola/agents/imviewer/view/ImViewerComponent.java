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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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
    
    /** 
     * The maximum number of channels that can be mapped using 
     * the RGB color model.
     */
    private static final int    MAX_CHANNELS_RGB = 3;
    
    /** The Model sub-component. */
    private ImViewerModel    model;
    
    /** The Control sub-component. */
    private ImViewerControl  controller;
    
    /** The View sub-component. */
    private ImViewerUI       view;
    
    /** 
     * Returns the description displayed in the status bar.
     * 
     * @return See above
     */
    private String getStatusText()
    {
        String text = "";
        text += "Z="+model.getDefaultZ()+" T="+model.getDefaultT();
        return text;
    }
    
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
                view.deIconify();
                UIUtilities.centerOnScreen(view);
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
     * @see ImViewer#setColorModel(Map)
     */
    public void setColorModel(Map map)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        if (map == null || map.size() != 1)
            return;
        Iterator i = map.keySet().iterator();
        Integer key = null;
        ViewerAction value = null;
        while (i.hasNext()) {
            key = (Integer) i.next();
            value = (ViewerAction) map.get(key);
        }
        String m;
        List channels = model.getActiveChannels();
        
        switch (key.intValue()) {
            case ColorModelAction.GREY_SCALE_MODEL:
                model.setColorModel(GREY_SCALE_MODEL);
                if (channels != null && channels.size() > 1) {
                    i = channels.iterator();
                    int index;
                    int j = 0;
                    while (i.hasNext()) {
                        index = ((Integer) i.next()).intValue();
                        setChannelActive(index, j == 0);
                        j++;
                    }
                } else if (channels == null || channels.size() == 0) {
                    //no channel.
                    setChannelActive(0, true);
                }
                break;
            case ColorModelAction.RGB_MODEL:
                model.setColorModel(RGB_MODEL);
                if (channels != null && channels.size() > 1) {
                    i = channels.iterator();
                    int index;
                    int j = 0;
                    while (i.hasNext()) {
                        index = ((Integer) i.next()).intValue();
                        setChannelActive(index, j < MAX_CHANNELS_RGB);
                        j++;
                    }
                } else if (channels == null || channels.size() == 0) {
                    //no channel.
                    setChannelActive(0, true);
                }
                break;
            case ColorModelAction.HSB_MODEL:
                m = HSB_MODEL;
                if (channels == null || channels.size() == 0) {
                    //no channel.
                    setChannelActive(0, true);
                }
                break;
            default:
                throw new IllegalArgumentException("Color model not supported");
        }
        //need
        view.setColorModel(value);
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setSelectedXYPlane(int, int)
     */
    public void setSelectedXYPlane(int z, int t)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
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
        view.setStatus(getStatusText(), -1, true);
        fireStateChange();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#playChannelMovie(boolean)
     */
    public void playChannelMovie(boolean play)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        if (model.getState() == READY) {
            model.playMovie(play);
            if (!play) {
                displayChannelMovie();
                controller.setHistoryState(READY);
            }
            fireStateChange();
        }    
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelColor(int, Color)
     */
    public void setChannelColor(int index, Color c)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        model.setChannelColor(index, c);
        if (model.isChannelActive(index)) renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelSelection(int, boolean)
     */
    public void setChannelSelection(int index, boolean b)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        //depends on model
        if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
            if (model.isChannelActive(index)) return;
            boolean c;
            for (int i = 0; i < model.getMaxC(); i++) {
                c = i == index;
                model.setChannelActive(i, c);  
                if (c) 
                    firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
                            new Integer(index-1), new Integer(index));
            }
        } else {
            model.setChannelActive(index, b);
            firePropertyChange(CHANNEL_ACTIVE_PROPERTY, new Integer(index-1),
                    new Integer(index));
        }
        view.setChannelsSelection();
        renderXYPlane();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setRenderingControl(RenderingControl)
     */
    public void setRenderingControl(RenderingControl result)
    {
        if (model.getState() != LOADING_RENDERING_CONTROL)
            throw new IllegalStateException(
            "This method can't be invoked in the LOADING_RENDERING_CONTROL.");
        model.setRenderingControl(result);
        //Register the renderer
        model.getRenderer().addPropertyChangeListener(controller);
        LoadingWindow window = view.getLoadingWindow();
        window.setStatus("rendering settings. Loading: metadata");
        window.setProgress(50);
        view.buildComponents();
        view.setOnScreen();
        view.setStatus(RENDERING_MSG, -1, false);
        renderXYPlane();
        //model.fireChannelMetadataLoading();
        //fireStateChange();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#renderXYPlane()
     */
    public void renderXYPlane()
    {
        //Check state
        switch (model.getState()) {
            case NEW:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        model.fireImageRetrieval();
        fireStateChange();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setChannelActive(int, boolean)
     */
    public void setChannelActive(int index, boolean b)
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        if (model.getColorModel().equals(RGB_MODEL)) {
            if (model.getActiveChannels().size() == MAX_CHANNELS_RGB);
            return;
        }
        model.setChannelActive(index, b);
        if (b)
            firePropertyChange(CHANNEL_ACTIVE_PROPERTY, new Integer(index-1),
                                new Integer(index));
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#displayChannelMovie()
     */
    public void displayChannelMovie()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        view.setChannelsSelection();
        renderXYPlane();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxC()
     */
    public int getMaxC()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getMaxC();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxT()
     */
    public int getMaxT()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getMaxT();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxZ()
     */
    public int getMaxZ()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getMaxZ();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#showRenderer()
     */
    public void showRenderer()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        model.getRenderer().moveToFront();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getImageName()
     */
    public String getImageName()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method can't be invoked in the DISCARDED state.");
        return model.getImageName();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getColorModel()
     */
    public String getColorModel()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getColorModel();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getUI()
     */
    public JFrame getUI()
    {
        switch (model.getState()) {
            case NEW:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW state.");
        }
        return view;
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#iconified(boolean)
     */
    public void iconified(boolean b)
    {
        switch (model.getState()) {
            case NEW:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW state.");
        }
        Boolean newValue =  Boolean.FALSE;
        Boolean oldValue = Boolean.TRUE;
        
        if (b) {
            newValue = Boolean.TRUE;
            oldValue = Boolean.FALSE;
        } 
        firePropertyChange(ICONIFIED_PROPERTY, oldValue, newValue);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getDefaultZ()
     */
    public int getDefaultZ()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getDefaultZ();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getDefaultT()
     */
    public int getDefaultT()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getDefaultT();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getImageComponents()
     */
    public List getImageComponents()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
        List l = model.getActiveChannels();
        if (l.size() < 2) return null;
        Iterator i = l.iterator();
        int index;
        ArrayList images = new ArrayList(l.size());
        while (i.hasNext()) {
            index = ((Integer) i.next()).intValue();
            for (int j = 0; j < model.getMaxC(); j++)
                model.setChannelActive(j, j == index);
            images.add(model.getRenderedImage());
        }
        i = l.iterator();
        while (i.hasNext()) { //reset values.
            index = ((Integer) i.next()).intValue();
            model.setChannelActive(index, true);
        }
        return images;
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getImage()
     */
    public BufferedImage getImage()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getDisplayedImage();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getPixelsSizeX()
     */
    public float getPixelsSizeX()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getPixelsSizeX();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getViewTitle()
     */
    public String getViewTitle()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method can't be invoked in the DISCARDED state.");
        return view.getTitle();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getChannelMetadata(int)
     */
    public ChannelMetadata getChannelMetadata(int index)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method can't be invoked in the DISCARDED state.");
        return model.getChannelData(index);
        
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getActiveChannels()
     */
    public List getActiveChannels()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getActiveChannels();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#isUnitBar()
     */
    public boolean isUnitBar()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method can't be invoked in the DISCARDED state.");
        return model.isUnitBar();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setUnitBar(boolean)
     */
    public void setUnitBar(boolean b)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method can't be invoked in the DISCARDED state.");
        model.setUnitBar(b);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getHistoryState()
     */
    public int getHistoryState()
    {
        // TODO Auto-generated method stub
        return controller.getHistoryState();
    }
   
}
