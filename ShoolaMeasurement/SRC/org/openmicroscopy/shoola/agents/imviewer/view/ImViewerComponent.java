/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerComponent
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

package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.MeasurementAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog;
import org.openmicroscopy.shoola.agents.util.archived.view.Downloader;
import org.openmicroscopy.shoola.agents.util.archived.view.DownloaderFactory;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.MessageBox;
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
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
    private ImViewerModel       model;
    
    /** The Control sub-component. */
    private ImViewerControl     controller;
    
    /** The View sub-component. */
    private ImViewerUI          view;
    
    /** List of active channels before switching between color mode. */
    private List                historyActiveChannels;
        
    /** 
     * Returns the description displayed in the status bar.
     * 
     * @return See above
     */
    private String getStatusText()
    {
        String text = "";
        text += "Z="+model.getDefaultZ()+"/"+model.getMaxZ();
        text += " T="+model.getDefaultT()+"/"+model.getMaxT();
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
        	//First make sure that we save the annotation.
        	if (model.getBrowser().hasAnnotationToSave()) {
        		MessageBox msg = new MessageBox(view, "Save Annotation", 
    			"Do you want to save the annotation before closing?");
        		if (msg.centerMsgBox() == MessageBox.YES_OPTION)
        			model.getBrowser().saveAnnotation();
        			
        	}
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
        view.setStatus(description);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setZoomFactor(double)
     */
    public void setZoomFactor(double factor)
    {
    	if (factor != -1 && (factor > ZoomAction.MAX_ZOOM_FACTOR ||
                    factor < ZoomAction.MIN_ZOOM_FACTOR))
    		throw new IllegalArgumentException("The zoom factor is value " +
    				"between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
    				ZoomAction.MAX_ZOOM_FACTOR);
    	model.setZoomFitToWindow(factor == -1);
    	model.setZoomFactor(factor);
    	if (view.isLensVisible() && 
        		model.getTabbedIndex() == ImViewer.VIEW_INDEX) {
        		view.setImageZoomFactor((float) model.getZoomFactor());
        		view.scrollLens();	
    	}
        if (view.isMeasurementToolVisible() && 
           		model.getTabbedIndex() == ImViewer.VIEW_INDEX) {
           		view.setImageZoomFactor((float) model.getZoomFactor());
          	}	
    }

    /**
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#zoomFitToWindow()
     */
    public boolean zoomFitToWindow() { return model.getZoomFitToWindow(); }
    
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
        if (map == null || map.size() != 1) return;
        Iterator i = map.keySet().iterator();
        Integer key = null;
        ViewerAction value = null;
        while (i.hasNext()) {
            key = (Integer) i.next();
            value = (ViewerAction) map.get(key);
        }
        try {
        	 List channels = model.getActiveChannels();
             switch (key.intValue()) {
     	        case ColorModelAction.GREY_SCALE_MODEL:
     	        	historyActiveChannels = model.getActiveChannels();
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
     	        		//no channel so one will be active.
     	        		setChannelActive(0, true);
     	        	}
     	        	break;
     	        case ColorModelAction.RGB_MODEL:
     	        case ColorModelAction.HSB_MODEL:
     	        	model.setColorModel(HSB_MODEL);
     	        	if (historyActiveChannels != null && 
     	        			historyActiveChannels.size() != 0) {
     	        		i = historyActiveChannels.iterator();
     	        		while (i.hasNext()) 
     	        			setChannelActive(((Integer) i.next()).intValue(), 
     	        					true);
     	        	} else {
     	        		if (channels == null || channels.size() == 0) {
     	        			//no channel so one will be active.
     	        			setChannelActive(0, true);
     	        		} else {
     	        			i = channels.iterator();
     	        			while (i.hasNext()) 
     	        				setChannelActive(
     	        						((Integer) i.next()).intValue(), true);
     	        		}
     	        	}
     	        	break;
     	        default:
     	        	throw new IllegalArgumentException("Color model not " +
     	        	"supported");
             }
             //need
             firePropertyChange(COLOR_MODEL_CHANGE_PROPERTY, new Integer(1), 
                                                             new Integer(-1));
             view.setColorModel(value);
             renderXYPlane();
		} catch (Exception ex) {
			reload(ex);
		}
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
        try {
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
		} catch (Exception ex) {
			reload(ex);
		}
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
        view.setStatus(getStatusText());
        view.setIconImage(model.getImageIcon());
        if (view.isLensVisible()) view.setLensPlaneImage();
        System.err.println("ImViewerComponent : Setting measurement tool coord");
        if (view.isMeasurementToolVisible()) view.setMeasurementToolCoord();
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
        if (model.getState() != READY) return;
        try {
        	 model.playMovie(play);
             if (!play) {
                 displayChannelMovie();
                 controller.setHistoryState(READY);
             }
             fireStateChange();
		} catch (Exception ex) {
			reload(ex);
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
        //Handle Exception
        try {
        	model.setChannelColor(index, c);
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        view.setChannelColor(index, c);
        firePropertyChange(CHANNEL_COLOR_CHANGE_PROPERTY, new Integer(index-1),
                new Integer(index));
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
        try {
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
                firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
                				new Integer(index-1), new Integer(index));
            }
            
            view.setChannelsSelection();
            renderXYPlane();
		} catch (Exception ex) {
			reload(ex);
		}
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
        if (model.getMaxC() >= 4) model.setRGBSplit(false);
        else {
        	boolean[] rgb = model.hasRGB();
        	model.setRGBSplit(rgb[0] && rgb[1] && rgb[2]);
        }
        fireStateChange();
        //Register the renderer
        model.getRenderer().addPropertyChangeListener(controller);
        LoadingWindow window = view.getLoadingWindow();
        window.setStatus("rendering settings. Loading: metadata");
        window.setProgress(50);
        view.buildComponents();
        view.setOnScreen();
        view.setStatus(RENDERING_MSG);
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
        //if (model.getColorModel().equals(RGB_MODEL)) {
        //    if (model.getActiveChannels().size() == MAX_CHANNELS_RGB);
         //   return;
        //}
        try {
        	model.setChannelActive(index, b);
            if (b)
                firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
                		new Integer(index-1), new Integer(index));
		} catch (Exception ex) {
			reload(ex);
		}
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
        JFrame f = model.getRenderer().getUI();
        UIUtilities.setLocationRelativeToAndShow(view, f);
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
                //throw new IllegalStateException(
                //"This method can't be invoked in the DISCARDED, NEW state.");
            	return;
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
     * @see ImViewer#getImageComponents(String)
     */
    public List getImageComponents(String colorModel)
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
        String oldColorModel = model.getColorModel();
        List<BufferedImage> images = new ArrayList<BufferedImage>(l.size());
        try {
        	model.setColorModel(colorModel);
        	while (i.hasNext()) {
                index = ((Integer) i.next()).intValue();
                for (int j = 0; j < model.getMaxC(); j++)
                    model.setChannelActive(j, j == index); 
                images.add(model.getSplitComponentImage());
            }
        	model.setColorModel(oldColorModel);
            i = l.iterator();
            while (i.hasNext()) { //reset values.
                index = ((Integer) i.next()).intValue();
                model.setChannelActive(index, true);
            }
		} catch (Exception ex) {
			reload(ex);
		}
        return images;
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getGridImages()
     */
    public List getGridImages()
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
	    int index;
	    List active = model.getActiveChannels();
	    int maxC = model.getMaxC();
	    List<BufferedImage> images = new ArrayList<BufferedImage>(maxC);
	    try {
	    	Iterator i;
	    	for (int j = 0; j < maxC; j++) {
	    		if (model.isChannelActive(j)) {
	    			for (int k = 0; k < maxC; k++) {
	    				model.setChannelActive(k, k == j);
					}
	    			images.add(model.getSplitComponentImage());
	    			i = active.iterator();
		            while (i.hasNext()) { //reset values.
		                index = ((Integer) i.next()).intValue();
		                model.setChannelActive(index, true);
		            }
	    		} else {
	    			images.add(null);
	    		}
	    		
	    		
			}
		} catch (Exception ex) {
			reload(ex);
		}
	    return images;
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getDisplayedImage()
     */
    public BufferedImage getDisplayedImage()
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
     * @see ImViewer#getPixelsSizeY()
     */
    public float getPixelsSizeY()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getPixelsSizeY();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getPixelsSizeZ()
     */
    public float getPixelsSizeZ()
    {
        switch (model.getState()) {
            case NEW:
            case LOADING_RENDERING_CONTROL:
            case DISCARDED:
                throw new IllegalStateException(
                "This method can't be invoked in the DISCARDED, NEW or" +
                "LOADING_RENDERING_CONTROL state.");
        }
        return model.getPixelsSizeZ();
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
        model.getBrowser().setUnitBar(b);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getHistoryState()
     */
    public int getHistoryState()
    {
        return controller.getHistoryState();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getChannelColor(int)
     */
    public Color getChannelColor(int index)
    {
        // TODO Check state
        return model.getChannelColor(index);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setUnitBarSize(double)
     */
    public void setUnitBarSize(double size)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        model.getBrowser().setUnitBarSize(size);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#showUnitBarSelection()
     */
    public void showUnitBarSelection()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        UnitBarSizeDialog d = new UnitBarSizeDialog(view);
        d.addPropertyChangeListener(controller);
        UIUtilities.centerAndShow(d);
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#resetDefaults()
     */
    public void resetDefaults()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        view.setStatus(getStatusText());
        view.resetDefaults(); 
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getUnitBarValue()
     */
    public String getUnitBarValue()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        return model.getBrowser().getUnitBarValue();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getUnitBarSize()
     */
    public double getUnitBarSize()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        return model.getBrowser().getUnitBarSize();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getUnitBarColor()
     */
    public Color getUnitBarColor()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("The method cannot be invoked in " +
                    "the DISCARDED state.");
        return model.getBrowser().getUnitBarColor();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getImageIcon()
     */
    public ImageIcon getImageIcon()
    {
        return new ImageIcon(model.getImageIcon());
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#isLensVisible()
     */
    public boolean isLensVisible()
    {
    	if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
    	return view.isLensVisible();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setLensVisible(boolean)
     */
    public void setLensVisible(boolean b)
    {
    	if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
    	view.setLensVisible(b);
    }
   
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#isMeasurementToolVisible()
     */
    public boolean isMeasurementToolVisible()
    {
    	if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
    	return view.isMeasurementToolVisible();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setMeasurementToolVisible(boolean)
     */
    public void setMeasurementToolVisible(boolean b)
    {
    	if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
    	view.setMeasurementToolVisible(b);
    }
   
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getZoomedLensImage()
     */
    public BufferedImage getZoomedLensImage()
    {
    	if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
    	return view.getZoomedLensImage();
    }

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#showMenu(int, Component, Point)
     */
	public void showMenu(int menuID, Component source, Point location)
	{
		if (model.getState() == DISCARDED)
    		throw new IllegalStateException("The method cannot be invoked in " +
    		"the DISCARDED state.");
		switch (menuID) {
	        case COLOR_PICKER_MENU:
	            break;
	        default:
	            throw new IllegalArgumentException("Menu not supported.");
		}
		if (source == null) throw new IllegalArgumentException("No component.");
        if (location == null) throw new IllegalArgumentException("No point.");
		view.showMenu(menuID, source, location);
	}

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setReloaded()
     */
	public void setReloaded()
	{
		if (model.getState() != LOADING_RENDERING_CONTROL)
    		throw new IllegalStateException("The method can only be invoked " +
    				"in the LOADING_RENDERING_CONTROL state.");
		//model.setState(READY);
		renderXYPlane();
	}

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#reload(Exception)
     */
	public void reload(Exception e)
	{
		Logger logger = ImViewerAgent.getRegistry().getLogger();
    	UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
    	if (e instanceof RenderingServiceException) {
    		RenderingServiceException rse = (RenderingServiceException) e;
    		logger.error(this, rse.getExtendedMessage());
    		un.notifyError(ImViewerAgent.ERROR, rse.getExtendedMessage(), 
    						e.getCause());
    		discard();
    	} else if (e instanceof DSOutOfServiceException) {
    		MessageBox msg = new MessageBox(view, "Rendering timeout", 
    			"The rendering engine has timed out. " +
    			"Do you want to reload it?");
    		if (msg.centerMsgBox() == MessageBox.YES_OPTION) {
    			logger.debug(this, "Reload rendering Engine.");
    			model.reloadRenderingControl();
    			fireStateChange();
    		} else {
    			logger.debug(this, e.getMessage());
    			discard();
    		}
    	}
	}

    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#download()
     */
	public void download()
	{
		Downloader dl = DownloaderFactory.getDownloader(view, 
						ImViewerAgent.getRegistry(), model.getPixelsID());
		dl.activate();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxX()
     */
	public int getMaxX() 
	{
		switch (model.getState()) {
	        case NEW:
	        case LOADING_RENDERING_CONTROL:
	        case DISCARDED:
	            throw new IllegalStateException(
	            "This method can't be invoked in the DISCARDED, NEW or" +
	            "LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxX();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMaxY()
     */
	public int getMaxY() 
	{
		switch (model.getState()) {
	        case NEW:
	        case LOADING_RENDERING_CONTROL:
	        case DISCARDED:
	            throw new IllegalStateException(
	            "This method can't be invoked in the DISCARDED, NEW or" +
	            "LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxY();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getSelectedIndex()
     */
	public int getSelectedIndex()
	{
		return model.getTabbedIndex();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#playMovie(boolean)
     */
	public void playMovie(boolean b)
	{
		controller.getAction(ImViewerControl.CHANNEL_MOVIE).setEnabled(!b);
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getRGBSplit()
     */
	public boolean getRGBSplit() { return model.getRGBSplit(); }

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setRGBSplit(boolean)
     */
	public void setRGBSplit(boolean b)
	{
		if (b == model.getRGBSplit()) return;
		model.setRGBSplit(b);
		model.getBrowser().viewSplitImages();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#hasRGB()
     */
	public boolean[] hasRGB()
	{
		//TODO:Check state
		return model.hasRGB();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getGridImage()
     */
	public BufferedImage getGridImage()
	{
		//TODO:Check state
		return model.getBrowser().getGridImage();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getLensImageComponents(String)
     */
	public List getLensImageComponents(String colorModel)
	{
		if (!view.hasLensImage()) return null;
		if (model.getTabbedIndex() != ImViewer.VIEW_INDEX) return null;
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List l = model.getActiveChannels();
		if (l.size() < 2) return null;
		Iterator i = l.iterator();
		int index;
		String oldColorModel = model.getColorModel();
		List<BufferedImage> images = new ArrayList<BufferedImage>(l.size());
		try {
			model.setColorModel(colorModel);
			while (i.hasNext()) {
				index = ((Integer) i.next()).intValue();
				for (int j = 0; j < model.getMaxC(); j++)
					model.setChannelActive(j, j == index); 
				images.add(view.createZoomedLensImage(
						model.getSplitComponentImage()));
			}
			model.setColorModel(oldColorModel);
			i = l.iterator();
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
				model.setChannelActive(index, true);
			}
			//view.setLensPlaneImage(model.getOriginalImage());
		} catch (Exception ex) {
			reload(ex);
		}
		return images;
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#isTextVisible()
     */
	public boolean isTextVisible()
	{
		return model.isTextVisible();
	}

	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#setTextVisible(boolean)
     */
	public void setTextVisible(boolean b)
	{
		model.setTextVisible(b);
		model.getBrowser().viewSplitImages();
	}
    
}
