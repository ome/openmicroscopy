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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvasMng;
import org.openmicroscopy.shoola.agents.roi.defs.ROIShape;
import org.openmicroscopy.shoola.agents.roi.editor.ROIEditor;
import org.openmicroscopy.shoola.agents.roi.util.ROIStats;
import org.openmicroscopy.shoola.agents.viewer.defs.ImageAffineTransform;
import org.openmicroscopy.shoola.env.config.Registry;
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
    
    private ImageAffineTransform    imgAffineTransform;
    
    ROIAgtCtrl(ROIAgt abstraction)
    {
        this.abstraction = abstraction;  
    }
  
    /** Attach a window listener. */
    private void attachListener()
    {
        presentation.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { onClosing(); }
        });
    }

    void setDefault()
    {
        drawingCanvas.getManager().setDefault();
    }
    
    void setImageAffineTransform(ImageAffineTransform iat)
    {
        imgAffineTransform = iat;
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
    
    void repaintDrawingCanvas() 
    {
        drawingCanvas.repaint();
    }
    
    DrawingCanvas getDrawingCanvas() { return drawingCanvas; }
    
    public ROIAgtUIF getReferenceFrame() { return presentation; }
    
    public Registry getRegistry() { return abstraction.getRegistry(); }
    
    public ImageAffineTransform getImageAffineTransform()
    { 
        return imgAffineTransform;
    }
    
    public String[] getChannels()
    {
        return abstraction.getChannels();
    }
  
    public void setState(int state)
    {
        drawingCanvas.getManager().setState(state);
    }
    
    public void setType(int type)
    {    
        drawingCanvas.getManager().setType(type);
    }
  
    public void setLineColor(Color lineColor)
    {
        drawingCanvas.getManager().setLineColor(lineColor);
    }
    
    public void setChannelIndex(int index)
    {
        drawingCanvas.getManager().setChannelIndex(index);
    }
    
    /** Erase all shapes. */
    public void eraseAll()
    {
        drawingCanvas.getManager().eraseAll();
        setAnnotation(null);
    }
  
    public void undoErase() 
    {
        drawingCanvas.getManager().undoErase();
    }
    
    /** Erase the current shape. */
    public void erase()
    {
        DrawingCanvasMng mng = drawingCanvas.getManager();
        ROIShape roi = mng.getCurrentROI();
        if (roi != null && roi.getAnnotation() != null && 
                mng.isAnnotationOnOff()) {
            setAnnotation(null);
        }
        if (roi != null) mng.erase(); 
    }
    
    /** Draw or not the ROI selections. */
    public void onOffDrawing(boolean b) { abstraction.onOffDrawing(b); }
    
    /** Draw or not the label. */
    public void onOffText(boolean b)
    {
        drawingCanvas.getManager().setTextOnOff(b); 
    }
    
    public boolean isOnOffAnnotation() 
    {
        return drawingCanvas.getManager().isAnnotationOnOff();
    }
    
    public void onOffAnnotation(boolean b)
    {
        drawingCanvas.getManager().setAnnotationOnOff(b); 
        if (!b) setAnnotation(null);
    }
    
    /** Bring up the editor modal widget. */
    public void annotateROI(ROIShape roi)
    {
        UIUtilities.centerAndShow(new ROIEditor(this, roi));
    }
    
    /** Set the text of the annotation. */
    public void setAnnotation(String txt)
    {
        abstraction.setAnnotation(txt);
    }
    
    public void analyse()
    {
        //Test
        UIUtilities.centerAndShow(new ROIStats(this, abstraction.getRegistry()));
        //need to retrieveROI from canvas
    }
    
    /** Return the results of the ROI. */
    public String[][] getROIStats()
    {
        String[][] stats = new String[2][8];
        for (int i = 0; i < 8; i++) {
            stats[0][i] = ""+i;
            stats[1][i] = ""+2*i;
        }
        return stats;
    }
    
    /** Handle window closing event. */
    private void onClosing()
    {
        abstraction.onOffDrawing(false);
        abstraction.setDrawOnOff(true);
        presentation.dispose();
    }
    
}

