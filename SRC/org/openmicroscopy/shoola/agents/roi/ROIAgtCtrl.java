/*
 * org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl
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

package org.openmicroscopy.shoola.agents.roi;

//Java imports
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvasMng;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenPlaneArea;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.agents.roi.editor.ROIEditor;
import org.openmicroscopy.shoola.agents.roi.pane.AnalysisControlsMng;
import org.openmicroscopy.shoola.agents.roi.pane.ToolBar;
import org.openmicroscopy.shoola.agents.roi.pane.ToolBarMng;
import org.openmicroscopy.shoola.agents.roi.results.ROIResults;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
* 
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
*               <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
*               <a href="mailto:a.falconi@dundee.ac.uk">
*                   a.falconi@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
public class ROIAgtCtrl
{

    private ROIAgt                  abstraction;
  
    private ROIAgtUIF               presentation;
    
    private DrawingCanvas           drawingCanvas;
    
    private String                  newName, newAnnotation;
    
    private Color                   newColor;
    
    ROIAgtCtrl(ROIAgt abstraction)
    {
        this.abstraction = abstraction;  
        setDefaultsNew();
    }
  
    /** Attach a window listener. */
    private void attachListener()
    {
        presentation.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { onClosing(); }
        });
    }

    public BufferedImage getROIImage()
    {
        BufferedImage roiImg = null;
        int index = presentation.getToolBar().getSelectedIndex();
        ScreenPlaneArea spa = drawingCanvas.getScreenPlaneArea(index);
        if (spa != null) {
            PlaneArea pa = spa.getPlaneArea();
            if (pa != null) {
                Rectangle r = pa.getBounds();
                BufferedImage image = abstraction.getImageOnScreen();
                if (image != null) //to be on the save-side, shouldn't happen
                roiImg = image.getSubimage(r.x, r.y, r.width, r.height);
            }
        }
        return roiImg;
    }
    
    /** Invoke when resize or move. */
    public void setROIThumbnail(int x, int y, int w, int h)
    {
        ToolBarMng tbm = presentation.getToolBar().getManager();
        if (tbm.isViewerOn()) {
            BufferedImage image = abstraction.getImageOnScreen();
            if (image != null) //to be on the save-side, shouldn't happen
                tbm.setROIImage(image.getSubimage(x, y, w, h));
        }
    }
    
    private void setROIThumbnail()
    {
        ToolBarMng tbm = presentation.getToolBar().getManager();
        if (tbm.isViewerOn()) tbm.setROIImage(getROIImage());
    }

    /** Set the new shape selection for the current 5D-selection. */
    public void setPlaneArea(PlaneArea pa)
    {
        int currentIndex = presentation.getToolBar().getSelectedIndex();
        int z = getCurrentZ(), t = getCurrentT();
        presentation.getToolBar().getManager().setCurrentPlane(z, t);
        abstraction.setPlaneArea(pa, currentIndex);
    }
    
    /** Get the shape selection of the current 5D-selection. */
    public PlaneArea getPlaneArea(int z, int t)
    {
        int currentIndex = presentation.getToolBar().getSelectedIndex();
        return abstraction.getPlaneArea(z, t, currentIndex);
    }
    
    public int getCurrentZ() { return abstraction.getCurrentZ(); }
    
    public int getCurrentT() { return abstraction.getCurrentT(); }
    
    public ScreenROI getScreenROI()
    {
        int currentIndex = presentation.getToolBar().getSelectedIndex();
        return abstraction.getScreenROI(currentIndex);
    }
    
    /** ADD comments. */
    public void setROI5DDescription(String name, String annotation, Color c)
    {
        newName = name;
        newAnnotation = annotation;
        newColor = c;
    }

    /** ADD comments. */
    public void saveROI5DDescription(String name, String annotation, Color c)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        ScreenROI roi = abstraction.getScreenROI(index);
        Color oldColor = roi.getAreaColor();
        roi.setAnnotation(annotation);
        roi.setName(name);
        roi.setAreaColor(c);
        if (oldColor != c) {
            ScreenPlaneArea spa = drawingCanvas.getScreenPlaneArea(index);
            spa.setAreaColor(c);
            drawingCanvas.repaint();
        }
    }
    
    /** Create an ROI5D object and update the different views. */
    public void createROI5D()
    {
        //Bring up the ROIEditor.
        UIUtilities.centerAndShow(new ROIEditor(this, "", "", -1));
        if (newColor != null && newName != null && newAnnotation != null) {
            ToolBar tb = presentation.getToolBar();
            int index = tb.getListModelSize();
            abstraction.createScreenROI(index, newName, newAnnotation, 
                                        newColor);
            //create a ScreenObject.
            drawingCanvas.addSPAToCanvas(
                    new ScreenPlaneArea(index, null, newColor));
            //update the view;
            tb.addROI5D(index);
            presentation.getAnalysisControls().addROI5D(index);
            DrawingCanvasMng dcm = drawingCanvas.getManager();
            dcm.setDefault(index, newColor, ROIAgtUIF.CONSTRUCTING);
            presentation.getPaintingControls().paintButton(dcm.getShapeType());
            newColor = null;
            newName = null;
            newAnnotation = null;
        }
    }
    
    /** Erase the current ROI5D object and update the different views. */
    public void removeROI5D()
    {
        ToolBar tb = presentation.getToolBar();
        int index = tb.getSelectedIndex();
        tb.removeROI5D();
        presentation.getAnalysisControls().removeROI5D(index);
        //remove the dialog Assistant and ROIViewer.
    }

    public void showROIEditor()
    {
        ScreenROI roi = getScreenROI();
        UIUtilities.centerAndShow(new ROIEditor(this, roi.getName(), 
                roi.getAnnotation(), roi.getIndex())); 
    }
    
    /** Draw or not the label. */
    public void onOffText(boolean b)
    {
        drawingCanvas.setTextOnOff(b); 
    }

    public void displayROIDescription(int index)
    {
        abstraction.displayROIDescription(index);
    }
    
    public void setSelectedIndex(int index)
    {
        //int curIndex = presentation.getToolBar().getSelectedIndex();
        //if (curIndex != index) {
        presentation.getToolBar().setSelectedROIIndex(index);
        drawingCanvas.setSelectedIndex(index);
        PlaneArea pa = getPlaneArea(getCurrentZ(), getCurrentT());
        ScreenROI roi = getScreenROI();
        int state = ROIAgtUIF.NOT_ACTIVE_STATE;
        if (pa == null) state =  ROIAgtUIF.CONSTRUCTING;
        drawingCanvas.getManager().setDefault(roi.getIndex(), 
                roi.getAreaColor(), state);
        //}
    }
    
    /** Erase all the {@link PlaneArea planeAreas}. */
    public void removeAllPlaneAreas()
    {
        abstraction.removeAllPlaneAreas();
        drawingCanvas.clearPreviousView();
        drawingCanvas.repaint();
        ToolBarMng tbm = presentation.getToolBar().getManager();
        tbm.removeCurrentPlane(getCurrentZ(), getCurrentT());
        ScreenROI roi = getScreenROI();
        drawingCanvas.getManager().setDefault(roi.getIndex(), 
                roi.getAreaColor(), ROIAgtUIF.CONSTRUCTING);
        setROIThumbnail();
    }
    
    /** Erase the current {@link PlaneArea}, call by the paitingControls. */
    public void removePlaneArea()
    {
        removePlaneArea(getCurrentZ(), getCurrentT());
        ToolBarMng tbm = presentation.getToolBar().getManager();
        tbm.removeCurrentPlane(getCurrentZ(), getCurrentT());
    }
    
    /** Erase the current {@link PlaneArea}, call by the Assistant. */
    public void removePlaneArea(int z, int t)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyPlaneArea(null, index, z, t);
        if (z == getCurrentZ() && t == getCurrentT()) {
            drawingCanvas.removeSPAFromCanvas(index);
            drawingCanvas.repaint();
            ScreenROI roi = getScreenROI();
            drawingCanvas.getManager().setDefault(roi.getIndex(), 
                    roi.getAreaColor(), ROIAgtUIF.CONSTRUCTING);
            setROIThumbnail();
        }
    }
    
    public void copyPlaneArea(PlaneArea pa, int newZ, int newT)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyPlaneArea(pa, index, newZ, newT);
        if (newT == getCurrentT() && newZ == getCurrentZ()) {
            PlaneArea p = abstraction.getPlaneArea(newZ, newT, index);
            p.scale(abstraction.getMagFactor());
            drawingCanvas.setPlaneArea(p, index);
        }
    }
    
    public void copyAcrossZ(PlaneArea pa, int from, int to, int t)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyAcrossZ(pa, index, from, to, t);
        //Need to refresh the view.
        int z = getCurrentZ();
        if (t == getCurrentT() && (z >= from || z <= to)) {
            PlaneArea p = abstraction.getPlaneArea(z, t, index);
            p.scale(abstraction.getMagFactor());
            drawingCanvas.setPlaneArea(p, index);
        }
    }
    
    public void copyAcrossT(PlaneArea pa, int from, int to, int z)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyAcrossT(pa, index, from, to, z);
        int t = getCurrentT();
        if (z == getCurrentZ() && (t >= from || t <= to)) {
            PlaneArea p = abstraction.getPlaneArea(z, t, index);
            p.scale(abstraction.getMagFactor());
            drawingCanvas.setPlaneArea(p, index);
        }
    }
    
    public void copyStackAcrossT(int from, int to)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyStackAcrossT(index, from, to);
        int t = getCurrentT();
        if (t >= from || t <= to) {
            PlaneArea p = abstraction.getPlaneArea(getCurrentZ(), t, index);
            p.scale(abstraction.getMagFactor());
            drawingCanvas.setPlaneArea(p, index);
        }
    }
    
    public void copyStack(int from, int to)
    {
        int index = presentation.getToolBar().getSelectedIndex();
        abstraction.copyStack(index, from, to);
        int t = getCurrentT();
        if (t == from || t == to) {
            PlaneArea p = abstraction.getPlaneArea(getCurrentZ(), t, index);
            p.scale(abstraction.getMagFactor());
            drawingCanvas.setPlaneArea(p, index);
        }
    }
    
    /** 
     * Return the buffered image dispplayed in the 
     * {@link org.openmicroscopy.shoola.agents.viewer.Viewer}. 
     */
    public BufferedImage getImageOnScreen()
    {
        return abstraction.getImageOnScreen();
    }
    
    /** Repaint the {@link PlaneArea}s */
    void magnifyScreenROIs(double f)
    {
        drawingCanvas.magnifyPlaneAreas(f);
        setROIThumbnail();
    }
    
    /** Paint screen ROI on screen only invoke when a new Image is rendered. */
    void paintScreenROIs(int currentZ, int currentT, double magFactor)
    {
        //clear previous view
        drawingCanvas.clearPreviousView();
        int index = presentation.getToolBar().getSelectedIndex(); 
        Iterator i = abstraction.getListScreenROI().values().iterator();
        ScreenROI roi;
        PlaneArea pa;
        ScreenPlaneArea spa;
        while (i.hasNext()) {
            roi = (ScreenROI) i.next();
            pa = roi.getLogicalROI().getPlaneArea(currentZ, currentT);
            if (pa != null) {
                pa.scale(magFactor);
                spa = drawingCanvas.getScreenPlaneArea(roi.getIndex());
                spa.setPlaneArea(pa);
            } else {
                //We have a 4D-ROI but no planeArea
                if (index == roi.getIndex())
                    drawingCanvas.getManager().setDefault(index, 
                            roi.getAreaColor(), ROIAgtUIF.CONSTRUCTING);
            }
        }
        drawingCanvas.repaint();
        setROIThumbnail();
    } 

    private void paintScreenROIs()
    {
        paintScreenROIs(getCurrentZ(), getCurrentT(), 
                abstraction.getMagFactor());
    }
    
    void setPresentation(ROIAgtUIF presentation)
    {
        this.presentation = presentation;
        attachListener();
    }
    
    void setDrawingCanvas(DrawingCanvas drawingCanvas)
    {
        this.drawingCanvas = drawingCanvas;
        drawingCanvas.getManager().setControl(this);
    }
    
    DrawingCanvas getDrawingCanvas() { return drawingCanvas; }
    
    public ROIAgtUIF getReferenceFrame() { return presentation; }
    
    public Registry getRegistry() { return abstraction.getRegistry(); }

    public String[] getChannels()
    {
        return abstraction.getChannels();
    }

    public void setType(int type)
    {    
        drawingCanvas.getManager().setType(type);
    }
    
    /** Draw or not the ROI selections. */
    public void onOffDrawing(boolean b) { abstraction.onOffDrawing(b); }

    /** Analyse data. */
    public void analyseStats()
    {
        /*
        AnalysisControlsMng
            mng = presentation.getAnalysisControls().getManager();
        List selectedChannels = mng.getSelectedChannels();
        List selectedROI = abstraction.getListROI();
        if (selectedChannels.size() != 0 && selectedROI.size() != 0) 
            retrieveAnalyseContext(mng, selectedROI);
        else {
            String msg = "No ROI selected.";
            if (selectedChannels.size() == 0) msg = "No channel selected.";
            if (selectedChannels.size() == 0 && selectedROI.size() == 0)
                msg = "No channel and ROI selected.";
            UserNotifier un = getRegistry().getUserNotifier();
            un.notifyInfo("Invalid selection", msg);   
        }  
        */
    }
    
    private void retrieveAnalyseContext(AnalysisControlsMng mng, 
                                        List selectedROI)
    {
        //Test
        UIUtilities.centerAndShow(new ROIResults(this, abstraction.getRegistry()));
        //need to retrieveROI from canvas
    }
    
    /** Return the results of the ROI. */
    public String[][] getROIStats()
    {
        String[][] stats = new String[2][9];
        for (int i = 0; i < 9; i++) {
            stats[0][i] = ""+i;
            stats[1][i] = ""+2*i;
        }
        return stats;
    }
    
    /** Handle window closing event. */
    private void onClosing()
    {
        abstraction.onOffDrawing(false);
        presentation.dispose();
    }
    
    private void setDefaultsNew()
    {
        newName = null;
        newAnnotation = null;
        newColor = null;
    }
    
}

