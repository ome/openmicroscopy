/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.AnnotatorUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;

/** 
 * UI component hosting the annotator and the canvas displaying a smaller
 * version of the original image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorUI 
	extends JPanel
	implements MouseListener, MouseMotionListener, MouseWheelListener
{

	/** Canvas displaying a smaller version of the rendered image. */
	private AnnotatorCanvas canvas;
	
	/** Reference to the Model. */
    private BrowserModel	model;
    
    /** Reference to the Control. */
    private BrowserControl	controller;
    
    /** Reference to the editor. */
    private AnnotatorEditor editor;

    /** UI component hosting the info about the image. */
    private InfoPane		infoPane;
    
    /** 
     * Flag indicating that the mouse entered or not the area. Control
     * used to handle mouse wheel events.
     */
    private boolean			mouseOnCanvas;
    
    /** The image area. */
    private Rectangle		area;
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BorderLayout(0, 0));
    	JPanel p = new JPanel();
    	double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.FILL}};
        p.setLayout(new TableLayout(tl));
        p.add(canvas, "0, 0");
        p.add(infoPane, "2, 0");
        p.add(new JSeparator(), "0, 2, 2, 2");
    	add(p, BorderLayout.NORTH);
    	add(editor.getUI(), BorderLayout.CENTER);
    }
    
    /** Creates a new instance. */
	AnnotatorUI() {}

	/**
	 * Links the components.
	 * 
	 * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
	 * @param model 		Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 */
	void initialize(BrowserControl controller, BrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		if (controller == null) throw new NullPointerException("No control.");
		canvas = new AnnotatorCanvas(model);
		editor = AnnotatorFactory.getEditor(ImViewerAgent.getRegistry(), 
									model.getImageData(), 
									AnnotatorEditor.HORIZONTAL_LAYOUT);
		infoPane = new InfoPane(model);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		editor.addPropertyChangeListener(controller);
		buildGUI();
	}

	/**
     * Paints the annotated image.
     * This method should be called straight after setting the 
     * rendered image.
     */
    void paintImage()
    {
        if (model.getRenderedImage() == null) return;
        BufferedImage img = model.getAnnotateImage();
        area = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        canvas.setPreferredSize(new Dimension(area.width, area.height));
        canvas.repaint();
    }
    
	/** Retrieves the annotations linked to the viewed image. */
	void activateEditor()
	{
		if (editor != null) editor.activate();
		if (infoPane != null) infoPane.buildGUI();
	}
	
	/**
	 * Selects a new z-section and timepoint.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
	{
		Point p = e.getPoint();
		int pressedZ = -1;
		int pressedT = -1;
		int maxZ = model.getMaxZ();
		pressedZ = (p.y*maxZ)/area.height;
		if (pressedZ < 0) return;
		pressedZ = maxZ-pressedZ;
		if (pressedZ > model.getMaxZ()) pressedZ = -1;
		pressedT = (p.x*model.getMaxT())/area.width;
		if (pressedT < 0) return;
		if (pressedT > model.getMaxT())  return;
		model.setSelectedXYPlane(pressedZ, pressedT);
		canvas.setPaintedString(pressedZ, pressedT);
	}

	/**
	 * Removes the painted value.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		canvas.setPaintedString(-1, -1);
	}
	
	/**
	 * Determines the value of the z-section.
	 * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (!mouseOnCanvas) return;
		boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxZ()) {
                	model.setSelectedXYPlane(v,  -1);
                	canvas.setPaintedString(v,  model.getDefaultT());
                } else
                	canvas.setPaintedString(-1,  -1);
            } else { //moving down
                if (v >= 0) {
                	model.setSelectedXYPlane(v,  -1);
                	canvas.setPaintedString(v,  model.getDefaultT());
                } else
                	canvas.setPaintedString(-1,  -1);
            }
        } else {
     
        }
	}
	
	/**
	 * Sets the value of the {@link #mouseOnCanvas} flag.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) 
	{
		mouseOnCanvas =  area.contains(e.getPoint());
	}

	/**
	 * Sets the value of the {@link #mouseOnCanvas} flag to <code>false</code>.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) { mouseOnCanvas = false; }
	
	/**
	 * Required by the {@link MouseMotionListener} interface but no-op 
	 * implementation in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}

	/**
	 * Required by the {@link MouseMotionListener} interface but no-op 
	 * implementation in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Required by the {@link MouseMotionListener} interface but no-op 
	 * implementation in our case.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {}

}
