/*
 * org.openmicroscopy.shoola.agents.viewer.movie.Dummy
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

package org.openmicroscopy.shoola.agents.viewer.movie;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.movie.canvas.Canvas;
import org.openmicroscopy.shoola.env.config.Registry;

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
public class Player
	extends JDialog
{

	private PlayerManager			manager;
	
	private ToolBar 				toolBar;
	
	private Canvas					canvas;
	
	private JScrollPane 			scrollPane;

	/**
	 * 
	 * @param control	reference to the {@link ViewerCtrl control}.
	 * @param maxValue	timepoint-1 b/c OME values start at 0 or section s-1.
	 */
	public Player(ViewerCtrl control, int maxValue, int index, String movie)
	{
		super(control.getReferenceFrame(), movie);
        setModal(true);
		init(control, maxValue, index);
		buildGUI();
	}
	
	public ToolBar getToolBar() { return toolBar; }
	
	public Canvas getCanvas() { return canvas; }
	
	public JScrollPane getScrollPane() { return scrollPane; }
	
	/** Initializes the components. */
	private void init(ViewerCtrl control, int max, int index)
	{
		//register for the ImageRendered event.
		Registry reg = control.getRegistry();
		manager = new PlayerManager(this, control, max, index);// to check
		canvas = new Canvas(this);
		BufferedImage img = control.getBufferedImage();
		int w = img.getWidth();
		int h = img.getHeight();
		Dimension d = new Dimension(w, h);
		canvas.setPreferredSize(d);
		canvas.setSize(d);
		canvas.paintImage(img);
        if (index == ViewerCtrl.MOVIE_T)
            toolBar = new ToolBar(manager, reg, max, control.getDefaultT());
        else if (index == ViewerCtrl.MOVIE_Z)
            toolBar = new ToolBar(manager, reg, max, control.getDefaultZ());
		setWindowSize(w, h);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		scrollPane = new JScrollPane(canvas);
		scrollPane.setBackground(Viewer.BACKGROUND_COLOR);
		getContentPane().add(toolBar, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
	}	
	
	/** Set the size of the window w.r.t the size of the screen. */
	private void setWindowSize(int w, int h)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 8*(screenSize.width/10);
		int height = 8*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;
		setTBSize(w);
		setSize(w, h);		
	}
	
	/** Add a rigid area to the toolBar. */
	private void setTBSize(int w)
	{
		Dimension d = toolBar.getSize();
		if (w-d.width>0)
			toolBar.add(Box.createRigidArea(new Dimension(w-d.width, 1)));		
	}

}
