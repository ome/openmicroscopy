/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerModel
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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.agents.imviewer.ChannelMetadataLoader;
import org.openmicroscopy.shoola.agents.imviewer.DataLoader;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.agents.imviewer.rnd.RendererFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.player.ChannelPlayer;
import org.openmicroscopy.shoola.agents.imviewer.util.player.Player;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * The Model component in the <code>ImViewer</code> MVC triad.
 * This class tracks the <code>ImViewer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class provides a suitable data loader.
 * The {@link ImViewerComponent} intercepts the results of data loadings, feeds
 * them back to this class and fires state transitions as appropriate.
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
class ImViewerModel
{

    static final int     RATING_ONE = 3;
    
    static final int     RATING_TWO = 4;
    
    static final int     RATING_THREE = 5;
    
    static final int     RATING_FOUR = 6;
    
    static final int     RATING_FIVE = 7;
    
    /** The maximum width of the thumbnail. */
    private static final int    THUMB_MAX_WIDTH = 48; 
    
    /** The maximum height of the thumbnail. */
    private static final int    THUMB_MAX_HEIGHT = 48;
    
    /** The id of the set of pixels. */
    private long                pixelsID;
    
    /** The id of the image. */
    private long                imageID;
    
    /** The name of the image. */
    private String              imageName;
    
    /** Holds one of the state flags defined by {@link ImViewer}. */
    private int                 state;
    
    /** Reference to the component that embeds this model. */
    private ImViewer            component;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private DataLoader          currentLoader;
    
    /** The sub-component that hosts the display. */
    private Browser             browser;
    
    /** Reference to the rendering control. */
    private RenderingControl    rndControl;
    
    /** Reference to the {@link Renderer}. */
    private Renderer            renderer;
    
    /** Reference to the current player. */
    private ChannelPlayer       player;
    
    /** The width of the thumbnail if the window is iconified. */
    private int                 sizeX;
    
    /** The height of the thumbnail if the window is iconified. */
    private int                 sizeY;
    
    private double              factor;
    
    /** The image icon. */
    private BufferedImage       imageIcon;
    
    /** Computes the values of the {@link #sizeX} and {@link #sizeY} fields. */
    private void computeSizes()
    {
        if (sizeX == -1 && sizeY == -1) {
            sizeX = THUMB_MAX_WIDTH;
            sizeY = THUMB_MAX_HEIGHT;
            double x = sizeX/(double) getMaxX();
            double y =  sizeY/(double) getMaxY();
            if (x > y) factor = x;
            else factor = y;
            double ratio =  (double) getMaxX()/getMaxY();
            if (ratio < 1) sizeX *= ratio;
            else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
        }
    }
    
    /** 
     * Magnifies the specified image.
     * 
     * @param f the magnification factor.
     * @param img The image to magnify.
     * 
     * @return The magnified image.
     */
    private BufferedImage magnifyImage(double f, BufferedImage img)
    {
        if (img == null) return null;
        int width = img.getWidth(), height = img.getHeight();
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        BufferedImageOp biop = new AffineTransformOp(at, 
            AffineTransformOp.TYPE_BILINEAR); 
        BufferedImage rescaleBuff = new BufferedImage((int) (width*f), 
                        (int) (height*f), img.getType());
        biop.filter(img, rescaleBuff);
        return rescaleBuff;
    }
    
    /**
     * Creates a new object and sets its state to {@link ImViewer#NEW}.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param imageID   The id of the image.
     * @param name      The image's name.
     */
    ImViewerModel(long pixelsID, long imageID, String name)
    {
        this.pixelsID = pixelsID;
        this.imageID = imageID;
        imageName = name;
        state = ImViewer.NEW;
        sizeX = sizeY = -1;
    }
    
    /**
     * Called by the <code>ImViewer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(ImViewer component)
    { 
        this.component = component;
        browser = BrowserFactory.createBrowser(component);
    }
    
    /**
     * Compares another model to this one to tell if they would result in
     * having the same display.
     *  
     * @param other The other model to compare.
     * @return <code>true</code> if <code>other</code> would lead to a viewer
     *          with the same display as the one in which this model belongs;
     *          <code>false</code> otherwise.
     */
    boolean isSameDisplay(ImViewerModel other)
    {
        if (other == null) return false;
        return ((other.pixelsID == pixelsID) && (other.imageID == imageID));
    }

    /**
     * Returns the name of the image.
     * 
     * @return See above.
     */
    String getImageName() { return imageName; }

    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link ImViewer} interface.  
     */
    int getState() { return state; }
    
    /**
     * Sets the object in the {@link ImViewer#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        state = ImViewer.DISCARDED;
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        if (renderer != null) renderer.discard();
        if (player == null) return;
        player.setPlayerState(Player.STOP);
        player = null;
        //Shut down the service
        ImViewerAgent.getRegistry().getImageService().shutDown(pixelsID);
    }

    /**
     * Returns the sizeX.
     * 
     * @return See above.
     */
    int getMaxX() { return rndControl.getPixelsDimensionsX(); }
    
    /**
     * Returns the sizeY.
     * 
     * @return See above.
     */
    int getMaxY() { return rndControl.getPixelsDimensionsY(); }
    
    /**
     * Returns the maximum number of z-sections.
     * 
     * @return See above.
     */
    int getMaxZ() { return rndControl.getPixelsDimensionsZ()-1; }


    /**
     * Returns the maximum number of timepoints.
     * 
     * @return See above.
     */
    int getMaxT() { return rndControl.getPixelsDimensionsT()-1; }
    
    /**
     * Returns the currently selected z-section.
     * 
     * @return See above.
     */
    int getDefaultZ() { return rndControl.getDefaultZ(); }
    
    /**
     * Returns the currently selected timepoint.
     * 
     * @return See above.
     */
    int getDefaultT() { return rndControl.getDefaultT(); }
    
    /**
     * Returns the currently selected color model.
     * 
     * @return See above.
     */
    String getColorModel() { return rndControl.getModel(); }
    
    /**
     * Returns the rate image level. One of the constants defined by this class.
     * 
     * @return See above.
     */
    int getRatingLevel()
    {
        return RATING_TWO;
    }
    
    /**
     * Returns an array of <code>ChannelData</code> object.
     * 
     * @return See above.
     */
    ChannelMetadata[] getChannelData()
    {
        return rndControl.getChannelData();
    }
    
    /**
     * Returns the <code>ChannelData</code> object corresponding to the
     * given index.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    ChannelMetadata getChannelData(int index)
    {
        return rndControl.getChannelData(index);
    }
    
    /**
     * Returns the color associated to a channel.
     * 
     * @param w The OME index of the channel.
     * @return See above.
     */
    Color getChannelColor(int w) { return rndControl.getRGBA(w); }
    
    /**
     * Returns <code>true</code> if the channel is mapped, <code>false</code>
     * otherwise.
     * 
     * @param w     The channel's index.
     * @return      <code>true</code> if selected, <code>false</code> otherwise.
     */
    boolean isChannelActive(int w) { return rndControl.isActive(w); }
    
    /**
     * Fires an asynchronous retrieval of metadata associated to the logical 
     * channels composing the pixels set.
     */
    void fireChannelMetadataLoading()
    {
        currentLoader = new ChannelMetadataLoader(component, imageID);
        currentLoader.load();
        //state = ImViewer.LOADING_METADATA;
    }

    /**
     * Fires an asynchronous retrieval of the rendering settings.
     */
    void fireRenderingControlLoading()
    {
        currentLoader = new RenderingControlLoader(component, pixelsID);
        currentLoader.load();
        state = ImViewer.LOADING_RENDERING_CONTROL;
    }
    
    /** Fires an asynchronous retrieval of the rendered image. */
    void fireImageRetrieval()
    {
        PlaneDef pDef = new PlaneDef(PlaneDef.XY, getDefaultT());
        pDef.setZ(getDefaultZ());
        state = ImViewer.LOADING_IMAGE;
        OmeroImageService rs = ImViewerAgent.getRegistry().getImageService();
        try {
            component.setImage(rs.renderImage(pixelsID, pDef));
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        //currentLoader = new ImageLoader(component, pixelsID, pDef);
        //currentLoader.load();
        
    }
    
    /**
     * Returns the newly rendered image.
     * 
     * @return See above.
     */
    BufferedImage getRenderedImage()
    {
        PlaneDef pDef = new PlaneDef(PlaneDef.XY, getDefaultT());
        pDef.setZ(getDefaultZ());
        state = ImViewer.LOADING_IMAGE;
        //OmeroImageService rs = ImViewerAgent.getRegistry().getImageService();
        try {
            return rndControl.renderCopy(pDef);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
    
    /**
     * Sets the rendering control.
     * 
     * @param rndControl The object to set. 
     */
    void setRenderingControl(RenderingControl rndControl)
    {
        this.rndControl = rndControl;
        renderer = RendererFactory.createRenderer(component, rndControl);
    } 

    /**
     * Returns the {@link Browser}.
     * 
     * @return See above.
     */
    Browser getBrowser() { return browser; }
    
    /**
     * Sets the zoom factor.
     * 
     * @param factor The factor to set.
     */
    void setZoomFactor(double factor) { browser.setZoomFactor(factor); }

    /**
     * Sets the retrieved image.
     * 
     * @param image The image to set.
     */
    void setImage(BufferedImage image)
    {
        browser.setRenderedImage(image);
        //update image icon
        computeSizes();
        imageIcon = magnifyImage(factor, image);
        state = ImViewer.READY; 
    }

    /**
     * Sets the color model.
     * 
     * @param colorModel    The color model to set.
     */
    void setColorModel(String colorModel)
    {
        rndControl.setModel(colorModel);
    }

    /**
     * Sets the selected plane.
     * 
     * @param z The z-section to set.
     * @param t The timepoint to set.
     */
    void setSelectedXYPlane(int z, int t)
    {
        rndControl.setDefaultT(t);
        rndControl.setDefaultZ(z);
    }

    /**
     * Sets the color for the specified channel.
     * 
     * @param index The channel's index.
     * @param c     The color to set.
     */
    void setChannelColor(int index, Color c)
    {
        rndControl.setRGBA(index, c);
    }

    /**
     * Sets the channel active.
     * 
     * @param index The channel's index.
     * @param b     Pass <code>true</code> to select the channel,
     *              <code>false</code> otherwise.
     */
    void setChannelActive(int index, boolean b)
    {
        rndControl.setActive(index, b);
    }  

    /**
     * Returns the number of channels.
     * 
     * @return See above.
     */
    int getMaxC() { return rndControl.getPixelsDimensionsC();}
    
    /** 
     * Returns the number of active channels.
     * 
     * @return See above.
     */
    int getActiveChannelsCount()
    {
        int active = 0;
        for (int i = 0; i < getMaxC(); i++) {
            if (rndControl.isActive(i)) active++;
        }
        return active;
    }

    /**
     * Returns a list of active channels.
     * 
     * @return See above.
     */
    List getActiveChannels()
    {
        ArrayList active = new ArrayList();
        for (int i = 0; i < getMaxC(); i++) {
            if (rndControl.isActive(i)) active.add(new Integer(i));
        }
        return active;
    }
    
    /** 
     * Starts the channels movie player, invokes in the event-dispatcher 
     * thread for safety reason.
     * 
     * @param play  Pass <code>true</code> to play the movie, <code>false</code>
     *              to stop it.
     */
    void playMovie(boolean play)
    {
        if (player != null && !play) {
            player.setPlayerState(Player.STOP);
            List l = player.getChannels();
            if (l != null) {
                Iterator i = l.iterator();
                int index;
                while (i.hasNext()) { //reset values.
                    index = ((Integer) i.next()).intValue();
                    setChannelActive(index, true);
                }
            }
            player = null;
            state = ImViewer.READY;
            return;
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                player = new ChannelPlayer(component);
                player.setPlayerState(Player.START);
            }
        });
        state = ImViewer.CHANNEL_MOVIE;
    }
    
    /**
     * Returns the {@link Renderer}.
     * 
     * @return See above.
     */
    Renderer getRenderer() { return renderer; }

    /** 
     * Returns the global minimum of the channel i.e. the minimum of all minima.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    double getGlobalMinimum(int index)
    {
        return rndControl.getChannelData(index).getGlobalMin();
    }
    
    /** 
     * Returns the global maximum of the channel i.e. the maximum of all maxima.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    double getGlobalMaximum(int index)
    {
        return rndControl.getChannelData(index).getGlobalMax();
    }

    /**
     * Returned the displayed image.
     * 
     * @return See above.
     */
    BufferedImage getDisplayedImage() { return browser.getDisplayedImage(); }

    /**
     * The size in microns of a pixel along the X-axis.
     * 
     * @return See above.
     */
    float getPixelsSizeX() { return rndControl.getPixelsSizeX(); }

    /**
     * Returns <code>true</code> if the unit bar is painted on top of 
     * the displayed image, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isUnitBar() { return browser.isUnitBar(); }

    BufferedImage getImageIcon() { return imageIcon; }
    
}
