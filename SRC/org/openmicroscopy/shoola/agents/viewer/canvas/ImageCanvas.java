/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas
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

package org.openmicroscopy.shoola.agents.viewer.canvas;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;

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
public class ImageCanvas
	extends JPanel
{
	/** default space size between images i.e. top-centre left-centre*/
	static final int wSpace = 20, hSpace = 20; 
	 
	private static final Color	BACKGROUND_COLOR = new Color(204, 204, 255); 
	
	/** Its icon holds the current image. */        
	private JLabel          	picture;

	/** Two layers, bottom for the image and top for the glass pane. */
	private JLayeredPane    	imgLayers;
	
	/** Glass pane laid on top of the image. */
	private Component       	imgGlassPane;
	
	/** 
	* A reference to the content pane of the viewer's internal frame. 
	* Needed to do better layout.
	*/
	private Component       	contentPane;
	
	/** A reference to viewer . */
	private ViewerUIF       	viewer; 
	   
	/** 
	* Creates the panel to host the image. 
	* 
	* @param contentPane	A reference to the content pane of the viewer's 
	* 						internal frame. Needed to do better layout.
	*/
	public ImageCanvas(ViewerUIF viewer, Component contentPane)
	{
		this.viewer = viewer;
		this.contentPane = contentPane;
		setBackground(BACKGROUND_COLOR);  
		setLayout(new ImageCanavasLM());
		imgLayers = new JLayeredPane();
		add(imgLayers);
	}
	
	/** 
	* Sets the specified <code>pane</code> to be the image glass pane.
	* The passed component will be laid over the image and will have the same 
	* origin and dimensions as the underlying image. 
	* The glass pane should normally be transparent.
	*
	* @param   pane    The glass pane.
	*/
	void setImageGlassPane(Component pane)
	{
		if (pane != null) {
			if (imgGlassPane != null)	imgLayers.remove(imgGlassPane);
			imgGlassPane = pane;
			imgLayers.add(imgGlassPane, new Integer(1));
			revalidate();
		}
	}
	
	void removeImageGlassPane()
	{
		if (imgGlassPane != null)	imgLayers.remove(imgGlassPane);
		imgGlassPane = null;
		revalidate();   
	}
    
	//TODO: override paintComponent?
	public void display(BufferedImage img)
	{
		//TODO: check that double-buffering is performed under the hood
		ImageIcon icon = new ImageIcon(img); 
		if (picture != null) imgLayers.remove(picture);
		picture = new JLabel(icon);
		imgLayers.add(picture, new Integer(0));
	}

	/** 
	 * Custom manager that takes care of the layout of {@link ImageCanvas}.
	 * This layout manager is tightly coupled to the specific structure of 
	 * {@link ImageCanvas} and its constituent components.
	 */
	private class ImageCanavasLM implements LayoutManager 
	{
		/** 
		 * Lays out the components of the image canvas.
		 *
		 * @param canvas     The image canvas. 
		 */
		public void layoutContainer(Container canvas)
		{
			ImageCanvas c = (ImageCanvas) canvas;
			if (c.picture != null) {
				int imgW = c.picture.getIcon().getIconWidth(),
					imgH = c.picture.getIcon().getIconHeight(),
					x = 0, y = 0;
				Dimension d = c.contentPane.getSize();
				if (imgW < d.width) x = (d.width-imgW)/2;
				if (imgH < d.height) y = (d.height-imgH)/2; 
				c.imgLayers.setBounds(x, y, imgW, imgH);
				c.picture.setBounds(wSpace, hSpace, imgW, imgH);	
				//if (c.imgGlassPane != null) 
				//	c.imgGlassPane.setBounds(wSpace, hSpace, imgW, imgH);
			}
		}

		/** 
		 * Returns the preferred amount of space for the layout, 
		 * which is the image size.
		 *
		 * @param canvas	The image canvas. 
		 * @return The above mentioned dimensions.
		 */
		public Dimension preferredLayoutSize(Container canvas)
		{
			ImageCanvas c = (ImageCanvas)canvas;
			int w = 0, h = 0;
			if (c.picture != null) {
				w = c.picture.getIcon().getIconWidth()+wSpace;
				h = c.picture.getIcon().getIconHeight()+hSpace;
			}
			return new Dimension(w, h);
		}
        
		/** 
		 * Returns the minimum amount of space the layout needs.
		 * This is the same as the preferred dimensions of the image canvas.
		 *
		 * @param   canvas     The image canvas. 
		 * @return  The above mentioned dimensions.
		 */ 
		public Dimension minimumLayoutSize(Container canvas)
		{
			return preferredLayoutSize(canvas);
		}

		/** 
		 * Required by I/F but not actually needed in our case, 
		 * no op implementation.
		 */
		public void addLayoutComponent(String name, Component comp) {}
		
		/** 
		 * Required by I/F but not actually needed in our case, 
		 * no op implementation.
		 */
		public void removeLayoutComponent(Component comp) {}
   
	}
    
}
