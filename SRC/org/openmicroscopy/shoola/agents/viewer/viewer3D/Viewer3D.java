/*
 * org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3D
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D;


//Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.ImagesCanvas;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.events.Image3DRendered;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage3D;

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
public class Viewer3D
	extends JDialog
	implements AgentEventListener
{

	/** Space between the images. */
	public static final int			SPACE = 25;

	/** 
	 * Color in sRGB of the vertical line drawn on top of the XYimage and 
	 * the XZimage.
	 */        
	public static final Color      	XlineColor = Color.RED;  

	/** 
	 * Color in sRGB of the horizontal line drawn on top of the XYimage and 
	 * the ZYimage.
	 */        
	public static final Color		YlineColor = Color.GREEN; 

	/** 
	 * Color in sRGB of the horizontal line drawn on top of the XYimage and 
	 * the ZYimage.
	 */        
	public static final Color		ZlineColor = Color.BLUE; 

	/** Background color. */
	public static final Color		BACKGROUND_COLOR = new Color(204, 204, 255);

	private Viewer3DManager			manager;
	
	private ViewerCtrl 				control;
	
	JPanel							backPanel;
	ImagesCanvas					canvas;
	DrawingCanvas					drawing;
	JLayeredPane					contents;
	
	private JScrollPane 			scrollPane;
	
	private int						model;
	
	private boolean					visible;
	
	private BufferedImage 			xyImage;
	
	public Viewer3D(ViewerCtrl control)
	{
		super(control.getReferenceFrame(), "Image3D Viewer", true);
		this.control = control;
		visible = false;
		init();
		buildGUI();
	}

	public JScrollPane getScrollPane() { return scrollPane; }
	
	/** 2D plane selected. */
	void onPlaneSelected(int x, int y)
	{
		int t = control.getDefaultT();
		Registry registry = control.getRegistry();
		int curPixelsID = control.getCurPixelsID();
		PlaneDef defXZ = new PlaneDef(PlaneDef.XZ, t);
		defXZ.setY(y);
		PlaneDef defYZ = new PlaneDef(PlaneDef.ZY, t);
		defYZ.setX(x);
		PlaneDef defXY = null;
		if (!visible) {
			defXY = new PlaneDef(PlaneDef.XY, t);
			defXY.setZ(control.getDefaultZ());
			visible = true;
		} 
		registry.getEventBus().post(new RenderImage3D(curPixelsID, defXY, defXZ, 
								defYZ));
			
	}
	
	private void init()
	{
		Registry reg = control.getRegistry();
		reg.getEventBus().register(this, Image3DRendered.class);
		manager = new Viewer3DManager(this);
		model = control.getModel();
		initComponents();
		control.setModel(RenderingDef.GS);
		onPlaneSelected(0, 0);
	}

	private void initComponents()
	{
		contents = new JLayeredPane();
		backPanel = new JPanel();
		backPanel.setBackground(BACKGROUND_COLOR); 
		canvas = new ImagesCanvas(this, manager);	
		drawing = new DrawingCanvas(manager);
		manager.setImagesCanvas(canvas);
		manager.setDrawingCanvas(drawing);
		contents.add(backPanel, new Integer(0));
		contents.add(canvas, new Integer(1));
		contents.add(drawing, new Integer(2));
		canvas.setBounds(0, 0, 1, 1); //default
		drawing.setBounds(0, 0, 1, 1); //default
		scrollPane = new JScrollPane(contents);
		scrollPane.setBackground(BACKGROUND_COLOR); 
	}

	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(scrollPane);
		setVisible(true);
	}

	public void eventFired(AgentEvent e)
	{
		if (e instanceof Image3DRendered)	
			handleImage3DRendered((Image3DRendered) e);
	}
	
	/** Display a 3DImage. */
	private void handleImage3DRendered(Image3DRendered response)
	{
		BufferedImage	xzImage = response.getRenderedXZImage(),
						yzImage = response.getRenderedZYImage();
		if (xyImage == null) {
			xyImage = response.getRenderedXYImage();
			manager.setImages(xyImage, xzImage, yzImage);
		} else manager.setImages(xzImage, yzImage);
		
	}
	
	void onClosing()
	{
		control.setModel(model);
		dispose();
	}
	
}
