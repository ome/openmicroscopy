/*
 * org.openmicroscopy.shoola.agents.viewer.ViewerCtrl
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

package org.openmicroscopy.shoola.agents.viewer;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvasMng;
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvasMng;
import org.openmicroscopy.shoola.agents.viewer.controls.BottomBar;
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBarManager;
import org.openmicroscopy.shoola.agents.viewer.defs.ImageAffineTransform;
import org.openmicroscopy.shoola.agents.viewer.movie.Player;
import org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspector;
import org.openmicroscopy.shoola.agents.viewer.util.ImageSaver;
import org.openmicroscopy.shoola.agents.viewer.util.ProgressNotifier;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3D;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class ViewerCtrl
    implements ActionListener, ChangeListener
{
    
    /** Action command ID to bring up the rendering Agent. */
    static final int                RENDERING = 1;
    
    /** Action command ID to bring up the saving widget. */
    static final int                SAVE_AS = 2;
    
    /** Action Command ID to bring up the inspector (with zooming) widget. */
    static final int                INSPECTOR = 3;
    
    /** Action command ID to bring up the viewer3D widget. */
    static final int                VIEWER3D = 4;
    
    /** Action command ID to bring up the movie widget. */
    public static final int         MOVIE = 5;
    
    /** Action command ID to bring up the roi widget. */
    public static final int         ROI = 6;
    
    /** Slider to control z-section selection and timepoint. */
    private JSlider                 tSlider, zSlider;
    
    /** zooming factor. */
    //private double                  magFactor;
    
    /** Drawing area for the purpose of the ROI. */
    private Rectangle               drawingArea;
    
    /** Control if the ROI is displayed on screen. */
    private boolean                 roiOnOff;
    
    /** 
     * Util object to retrieve the movie settings if the movie widget has been
     * brought up and re-open.
     */
    private MovieSettings           movieSettings;
    
    private ImageAffineTransform    iat;
    
    private Player                  moviePlayer;

    private ImageInspector          imageInspector;
    
    private Viewer                  abstraction;
    
    private ViewerUIF               presentation;
    
    private ProgressNotifier        progressNotifier;
    
    public ViewerCtrl(Viewer abstraction)
    {
        this.abstraction = abstraction;
        drawingArea = new Rectangle();
        iat = new ImageAffineTransform();
        iat.setMagFactor(ImageInspector.ZOOM_DEFAULT);
    }
    
    void setPresentation(ViewerUIF presentation)
    {
        this.presentation = presentation;
    }

    /** The non- modal dialogs are removed when a new image is selected. */
    void disposeDialogs()
    {
        if (moviePlayer != null) moviePlayer.dispose();
        if (imageInspector != null) imageInspector.dispose();
        setRoiOnOff(false);
        moviePlayer = null;
        imageInspector = null;
        movieSettings = null;
        iat.setMagFactor(ImageInspector.ZOOM_DEFAULT);
        //Need to remove the drawing canvas. and erase all color.
        // will post an even to kill the ROIAgt.
    }
    
    /** Attach listeners. */
    void attachListener() 
    {
        tSlider = presentation.getTSlider(); 
        zSlider = presentation.getZSlider();
        tSlider.addChangeListener(this);
        zSlider.addChangeListener(this);
        // window listener
        presentation.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { onWindowClose(); }
        });
    }
    
    /** Return the {@link Viewer abstraction}. */
    Viewer getAbstraction() { return abstraction; }
    
    /** Attach listener to a menu Item. */
    void attachItemListener(AbstractButton item, int id)
    {
        item.setActionCommand(""+id);
        item.addActionListener(this);
    }

    /** Forward event to {@link Viewer abstraction}. */
    public ViewerUIF getReferenceFrame()
    {
        return presentation;
    }
    
    /** Forward event to {@link Viewer abstraction}. */
    public Registry getRegistry()
    {
        return abstraction.getRegistry();
    }
    
    /** Forward event to {@link Viewer abstraction}. */
    public int getModel() { return abstraction.getModel(); } 

    /** Forward event to {@link Viewer abstraction}. */
    public void setModel(int model) { abstraction.setModel(model); } 
    
    /** Forward event to {@link Viewer abstraction}. */
    public int getCurPixelsID() { return abstraction.getCurPixelsID(); } 
    
    /** Forward event to {@link Viewer abstraction}. */
    public int getDefaultT() {return abstraction.getDefaultT(); }
    
    /** Forward event to {@link Viewer abstraction}. */
    public int getDefaultZ() {return abstraction.getDefaultZ(); }
    
    /** Forward event to {@link Viewer abstraction}. */
    public BufferedImage getBufferedImage()
    {
        return abstraction.getCurImage();
    }

    public BufferedImage getDisplayImageWithPinArea(boolean painting, Color c)
    {
        return presentation.getCanvas().getDisplayImageWithPinArea(painting, c); 
    }
    
    public BufferedImage getDisplayImage()
    {
        return presentation.getCanvas().getDisplayImage();
    }
    
    public BufferedImage getPinImage()
    {
        return presentation.getCanvas().getPinImage();
    }
    
    public BufferedImage getPinOnImage()
    {
        return presentation.getCanvas().getPinOnImage();
    }

    public BufferedImage getPinOnSideTopLeft(boolean painting, Color c)
    {
        return presentation.getCanvas().getPinOnSideTopLeft(painting, c);
    }
    
    public BufferedImage getPinOnSideTopRight(boolean painting, Color c)
    {
        return presentation.getCanvas().getPinOnSideTopRight(painting, c);
    }
    
    public BufferedImage getPinOnSideBottomLeft(boolean painting, Color c)
    {
        return presentation.getCanvas().getPinOnSideBottomLeft(painting, c);
    }
    
    public BufferedImage getPinOnSideBottomRight(boolean painting, Color c)
    {
        return presentation.getCanvas().getPinOnSideBottomRight(painting, c);
    }
    
    public BufferedImage getImageAndROIs()
    {
        List spa = presentation.getDrawingCanvas().getScreenPlaneAreas();
        BufferedImage img = null;
        if (spa.size() == 0) {
            UserNotifier un = getRegistry().getUserNotifier();
            un.notifyInfo("Save image", "No roi selected " +
                            "on the current image."); 
        } else {
            img = presentation.getCanvas().getImageAndROIs(spa);
        }
        return img;
    }
    
    /** Forward event to {@link Viewer abstraction}. */
    public String getCurImageName() { return abstraction.getCurImageName(); }
    
    /** Forward event to {@link Viewer abstraction}. */
    public void onPlaneSelected(int z, int t)
    {
        abstraction.onPlaneSelected(z, t);
    }
    
    /** 
     * Update t-slider, method called when a new value is set using 
     * the textField.
     */
    public void onTChange(int z, int t)
    {
        resetTSlider(t);
        abstraction.onPlaneSelected(z, t);
    }
    
    /** 
     * Update z-slider, method called when a new value is set using 
     * the textField.
     */
    public void onZChange(int z, int t)
    {
        resetZSlider(z);
        abstraction.onPlaneSelected(z, t);
    }
    
    void synchPlaneSelected(int z, int model)
    {
        resetZSlider(z);
        resetZField(z);
        abstraction.setModel(model);
        abstraction.onPlaneSelected(z);
    }

    /** Remove listener otherwise an event is fired. */
    public void resetZSlider(int z)
    {
        zSlider.removeChangeListener(this);
        zSlider.setValue(z);
        zSlider.addChangeListener(this);
    }
    
    /** Remove listener otherwise an event is fired. */
    public void resetTSlider(int t)
    {
        tSlider.removeChangeListener(this);
        tSlider.setValue(t);
        tSlider.addChangeListener(this);
    }

    public void resetTField(int t)
    {
        ToolBarManager tbm = presentation.getToolBar().getManager();
        tbm.onTChange(t); 
    }

    public void resetZField(int z)
    {
        ToolBarManager tbm = presentation.getToolBar().getManager();
        tbm.onZChange(z);
    }
    
    /** 
     * Invokes by the 
     * {@link org.openmicroscopy.shoola.agents.viewer.movie.PlayerManager}. 
     */
    public void renderImage(int z, int t)
    {
        resetTField(t);
        resetZField(z);
        abstraction.onPlaneSelected(z, t);   
    }
    
    /** Handles events. */
    public void actionPerformed(ActionEvent e) 
    {
        String s = e.getActionCommand();
        int index = Integer.parseInt(s);
        try {
           switch (index) { 
                case RENDERING:
                    showRendering(); break;     
                case SAVE_AS:
                    showImageSaver(); break;
                case INSPECTOR:
                    showInspector(); break;
                case VIEWER3D:
                    showImage3DViewer(); break;
                case MOVIE:
                    showMovie(); break;
                case ROI:
                    showROI(); break;
           }
        } catch(NumberFormatException nfe) {   
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }

    /** Handle events fired by the Slider. */
    public void stateChanged(ChangeEvent e)
    {
        Object src = e.getSource();
        int valT, valZ;
        valT = tSlider.getValue();
        valZ = zSlider.getValue();
        ToolBarManager tbm = presentation.getToolBar().getManager();
        if (src == tSlider) tbm.onTChange(valT);
        else  tbm.onZChange(valZ);
        abstraction.onPlaneSelected(valZ, valT);
    }

    /** Bring up the progressNotifier dialog. */
    void showProgressNotifier(String imageName)
    {
        progressNotifier = new ProgressNotifier(this, imageName);
        UIUtilities.centerAndShow(progressNotifier);
    }
    
    void removeProgressNotifier()
    {
        progressNotifier.dispose();
        progressNotifier = null;
    }

    /** Bring up the movie panel. */
    public void showMovie()
    {
        int maxZ = abstraction.getPixelsDims().sizeZ-1;
        int maxT = abstraction.getPixelsDims().sizeT-1;
        if (movieSettings == null) initMovieSettings(maxZ, maxT);
        moviePlayer = new Player(this, maxT, maxZ, movieSettings);
        UIUtilities.centerAndShow(moviePlayer);
    }

    /** Bring up the image3D viewer. */
    public void showImage3DViewer()
    {
        int model = getModel();
        Viewer3D v3D = new Viewer3D(this, abstraction.getPixelsDims().sizeZ, 
                                    model);
        if (v3D.isVisible()) UIUtilities.centerAndShow(v3D);
        else { //TODO: tempo work around before fix bug #321
            abstraction.setModel(model);
            v3D.dispose();
        }
        //abstraction.showViewer3D();
    }

    /** Bring up the image inspector widget. */
    public void showInspector()
    {
        if (imageInspector == null)
            imageInspector = new ImageInspector(this, presentation.getCanvas(), 
                             iat.getMagFactor(), abstraction.getImageWidth(), 
                             abstraction.getImageHeight());
        presentation.getCanvas().getManager().setClick(true);
        UIUtilities.centerAndShow(imageInspector);
        if (roiOnOff) imageInspector.getManager().setLensEnabled(false);
    }

    /** Bring up the rendering widget. */
    public void showRendering() { abstraction.showRendering(); }
    
    /** Forward event to {@link ViewerUIF presentation}. */
    public void showDialog(JDialog dialog)
    { 
        UIUtilities.centerAndShow(dialog);
    }

    /** 
     * Bring up the Save widget and save the current displayed bufferedImage.
     */
    public void showImageSaver()
    {
        BufferedImage image = getBufferedImage();
        if (image == null) {
            UserNotifier un = getRegistry().getUserNotifier();
            un.notifyError("Save image", "No current image displayed");
        } else  new ImageSaver(this);
    }

    /** 
     * Set the magFactor. 
     * Needed b/c the user can close the {@link ImageInspector} widget 
     * and re-open it with the same image displayed on screen.
     * Value sets to {@link ImageInspector#DEFAULT_ZOOM} when a new 5Dimage is 
     * selected.
     */
    public void imageDisplayedUpdated(BufferedImage img, double level)
    {
        iat.setMagFactor(level);
        abstraction.imageDisplayedUpdated(iat, img);   
    }
    
    ImageAffineTransform getImageAffineTransform() { return iat; }
    
    /** 
     * Cache the movie settings when the widget is closed.
     * Reset to <code>null</code> when a new image is selected.
     */
    public void setMovieSettings(int startZ, int endZ, int startT, int endT,
            int movieType, int index, int rate) 
    {
        if (movieSettings != null)
            movieSettings.setAll(startZ, endZ, startT, endT, movieType, index, 
                                rate);
    }

    /** Forward event to {@link Viewer abstraction}. */
    public void setSizePaintedComponents(Dimension d)
    {
        presentation.setSizePaintedComponents(d);
    }

    /** Lens stuff. */
    public void setLensMagFactor(double f)
    {
        ImageCanvasMng lcMng = presentation.getCanvas().getManager();
        lcMng.setMagFactorLens(f); 
    }

    /** Decrease the lens size. */
    public void setLensWidth(int w)
    {
        ImageCanvasMng lcMng = presentation.getCanvas().getManager();
        lcMng.setWidth(w);
    }
    
    /** Turn the lens on/off. */
    public void setLensOnOff(boolean b)
    {
        ImageCanvasMng lcMng = presentation.getCanvas().getManager();
        lcMng.setOnOff(b);
    }

    /** Pin the lens image. */
    public void setPinLens(boolean b)
    {
        ImageCanvasMng lcMng = presentation.getCanvas().getManager();
        lcMng.setPin(b);
    }

    /** Paint the lens' boder. */
    public void setPaintingLens(boolean b, Color c)
    {
        ImageCanvasMng lcMng = presentation.getCanvas().getManager();
        lcMng.setPainting(b, c);
    }
    
    /** Apply sharpen or low pass filter. */
    public void filterImage(float[] filter)
    {
        ImageCanvas canvas = presentation.getCanvas();
        canvas.filterImage(filter);
        //abstraction.affineTransformChanged(iat.copy());
    }

    public void undoFiltering() 
    {
        ImageCanvas canvas = presentation.getCanvas();
        canvas.undoFiltering();
    }
    
    /** ROI. */
    /** Forward event to the {@link Viewer agent}. */
    public void showROI()
    { 
        if (!roiOnOff) {
            abstraction.showROI(presentation.getDrawingCanvas(), iat.copy());
            setRoiOnOff(true);
        }
    }

    /** Turn on and off the ROI widget. */
    public void setRoiOnOff(boolean b)
    {
        roiOnOff = b;
        if (!roiOnOff){
            presentation.getBottomBar().resetMessage(BottomBar.LENS);
            presentation.removeCanvasFromLayer();
        } else {
            presentation.getBottomBar().resetMessage(BottomBar.ANNOTATE);
            paintDrawing();
            presentation.addCanvasToLayer(); 
            presentation.resetLens();
        }
        if (imageInspector != null) 
            imageInspector.getManager().setLensEnabled(!b);
    }

    /** Set the bounds of the drawing canvas. */
    public void setDrawingArea(int x, int y, int w, int h)
    {
        drawingArea.setBounds(x, y, w, h);
        if (roiOnOff) paintDrawing();    
    }

    void setAnnotationText(String text)
    {
        if (text != null)
            presentation.getBottomBar().setMessage(text);
        else presentation.getBottomBar().resetMessage(BottomBar.ANNOTATE);
    }
    
    /** PRIVATE METHODS. */
    private void paintDrawing()
    {
        DrawingCanvasMng dcMng = presentation.getDrawingCanvas().getManager();
        dcMng.setDrawingArea(drawingArea);
    }
    
    /** Set default movie settings. */ 
    private void initMovieSettings(int maxZ, int maxT)
    {
        int index = Player.MOVIE_T;
        if (maxT == 0) index = Player.MOVIE_Z;
        movieSettings = new MovieSettings(0, maxZ, 0, maxT, Player.LOOP, index, 
                                            Player.FPS_INIT);
        
    }

    /** Close the window. */
    private void onWindowClose()
    {
        //Post an event to close roi.
        if (roiOnOff) abstraction.addRoiCanvas(false);
    }
    
}
