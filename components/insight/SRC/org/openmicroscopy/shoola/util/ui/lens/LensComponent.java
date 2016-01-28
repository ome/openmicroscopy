/*
 * org.openmicroscopy.shoola.util.ui.lens.lensComponent.java
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import omero.model.Length;
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.Encoder;
import org.openmicroscopy.shoola.util.image.io.TIFFEncoder;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;


/** 
* The Lens Component is the main component of the lens accessible from outside
* of the lens Package. 
* 
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME2.2
*/
public class LensComponent
	extends AbstractComponent
{

	/** Bound property indicating that the location of the lens has changed. */
	public static final String	LENS_LOCATION_PROPERTY = "lensLocation";
	
	/** Default width of a lens */
	public final static int		LENS_DEFAULT_WIDTH	= 50;

	/** Default the zoom factor.*/
	public final static float	DEFAULT_ZOOM	= 2.0f;

	/** The collection of supported formats.*/
	private static final List<FileFilter> FILTERS;
	
	static {
		FILTERS = new ArrayList<FileFilter>();
		FILTERS.add(new BMPFilter());
		FILTERS.add(new JPEGFilter());
		FILTERS.add(new PNGFilter());
		FILTERS.add(new TIFFFilter());
	}
	
	/** Reference to the lens object which will render onto the image canvas */
	private LensUI			lens;
	
	/** Menu object which hold the pop-up and menu items. */
	private	LensMenu 		menu;
	
	/** 
	 * Reference to the lensController which will modify the position and 
	 * properties of the lens and the zoomWindow. 
	 */
	private LensController	lensController;
	
	/** Shows the current zoomed image specified by the lens. */
	private ZoomWindow  	zoomWindow;
		
	/** Holds the properties of the lens, x,y, width height. */
	private LensModel		lensModel;
	
	/**
	 * Displays in pixels if <code>true</code>, in microns otherwise.
	 * 
	 * @param b See above.
	 */
	void setDisplayInPixels(boolean b)
	{
		zoomWindow.setDisplayInPixels(b);
		zoomWindow.setLensXY(lens.getX(), lens.getY());
		zoomWindow.setLensWidthHeight(lens.getWidth(), lens.getHeight());	
	}
	
	/**
	 * Sets the lens Size to a value described in LensAction. 
	 * 
	 * @param width The width of the lens. 
	 * @param height The height of the lens. 
	 */
	void setLensSize(int width, int height) 
	{
		lensController.setLensSize(width, height);
	}
	
	/**
	 * Indicates the location of the lens. Fires a property change to notify 
	 * listeners of the new bounds.
	 */
	void updateLensLocation()
	{
		Rectangle bounds = lensModel.getLensScaledBounds();
		firePropertyChange(LENS_LOCATION_PROPERTY, null, bounds);
		zoomWindow.paintImage();
	}
	
	/** Selects the menu item if the size corresponds to a predefined size. */
	void updateLensSize()
	{
		int w = lensModel.getWidth();
		int h = lensModel.getHeight();
		int index = LensAction.sizeToIndex(w, h);
		menu.setSelectedSize(index);
		zoomWindow.setSelectedSize(index);
	}
	
	/**
	 * Sets the magnification factor for the lens. 
	 * 
	 * @param zoomFactor The magnification factor.
	 */
	public void setZoomFactor(float zoomFactor)
	{
		lensController.setZoomFactor(zoomFactor);
		int index = ZoomAction.factorToIndex(zoomFactor);
		menu.setZoomIndex(index);
		zoomWindow.setZoomIndex(index);
	}
	
	/**
	 * Magnifies the image.
	 * 
	 * @param tick The number of "clicks" the mouse wheel was rotated.
	 */
	void lensMouseWheelMoved(int tick)
	{
		float zoomFactor = lensModel.getZoomFactor();
		zoomFactor -= 0.1f*tick;
		zoomFactor = Math.round(zoomFactor*10)/10.0f;
		if (zoomFactor < LensModel.MINIMUM_ZOOM)
			zoomFactor = LensModel.MINIMUM_ZOOM;
		if (zoomFactor > LensModel.MAXIMUM_ZOOM)
			zoomFactor = LensModel.MAXIMUM_ZOOM;
		setZoomFactor(zoomFactor);
	}

	/**
	 * Creates a new instance which is the container for the lens 
	 * infrastructure.
	 * 
     * @param parent The parent of the Dialog.
	 */
	public LensComponent(JFrame parent, int lensWidth, int lensHeight)
	{ 
		lensModel = new LensModel(null);
		zoomWindow = new ZoomWindow(parent, this, lensModel);
		lens = new LensUI(this, lensWidth, lensHeight);
		lensController = new LensController(lensModel, lens, zoomWindow);
		
		lensModel.setWidth(lensWidth);
		lensModel.setHeight(lensHeight);
		lensModel.setImageZoomFactor(ZoomAction.ZOOMx2+1);
		lens.addController(lensController);
		lens.setLensColour(lensModel.getLensPreferredColour());
		menu = new LensMenu(this);
		lens.setPopupMenu(menu.getPopupMenu());
		zoomWindow.setJMenuBar(menu.getMenubar());
		setZoomFactor(DEFAULT_ZOOM);
	}
	
	/**
	 * Creates a new instance which is the container for the lens 
	 * infrastructure.
	 * 
     * @param parent The parent of the Dialog.
	 */
	public LensComponent(JFrame parent)
	{ 
		this(parent, LENS_DEFAULT_WIDTH, LENS_DEFAULT_WIDTH);
	}
	
	/** Saves the image as <code>JPEG</code>, <code>PNG</code> etc.*/
	void saveAs()
	{
		FileChooser d = new FileChooser((JFrame) zoomWindow.getParent(), 
				FileChooser.SAVE, SaveAction.NAME, SaveAction.DESCRIPTION, 
				FILTERS, false, true);
		d.setSelectedFile(UIUtilities.removeFileExtension(
				lensModel.getImageName()));
		d.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (! (evt.getSource() instanceof FileChooser)) return;
				FileChooser fileChooser = (FileChooser) evt.getSource();
				
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File[] files = (File[]) evt.getNewValue();
					File file = files[0];
					
					FileFilter filter = fileChooser.getSelectedFilter();
					String format = "";
					if (filter instanceof CustomizedFileFilter) {
						format = ((CustomizedFileFilter) filter).getExtension();
					}
					
					// check if file is allowed. If not, add extension. 
					if (!filter.accept(file)) {
						String filePath = file.getAbsolutePath();
						filePath = filePath + "." + format;
						file = new File(filePath);
					}
						
					// if file exists, get user to confirm. Otherwise exit! 
					JFrame frame = (JFrame) zoomWindow.getParent();
					if (file.exists()) {
						String title = "File Exists";
						String message = "An image with the same name" +
								"already exists. Do you want to overwrite it?";
						MessageBox msg = new MessageBox(frame, title, message);
						int option = msg.centerMsgBox();
						if (option != MessageBox.YES_OPTION) return;
					}
					NotificationDialog dialog;
					//Now save the image
					BufferedImage image = getZoomedImage();
					IconManager icons = IconManager.getInstance();
					try {
			            if (TIFFFilter.TIF.equals(format)) {
			                Encoder encoder = new TIFFEncoder(
			                		Factory.createImage(image), 
			                        new DataOutputStream(
			                        		new FileOutputStream(file)));
			                WriterImage.saveImage(encoder);
			            } else WriterImage.saveImage(file, image, format);
			        } catch (Exception e) {
			        	dialog = new NotificationDialog(
                                frame, SaveAction.NAME,
                                "An error occurred while saving the image.", 
                                icons.getIcon(IconManager.ERROR_32));
						dialog.pack();  
						UIUtilities.centerAndShow(dialog);
						return;
			        }
			        dialog = new NotificationDialog(
                            frame, SaveAction.NAME,
                            "The image has been successfully saved in\n"+
                            file.getParent(), 
                            icons.getIcon(IconManager.INFO_32));
					dialog.pack();  
					UIUtilities.centerAndShow(dialog);
				}
			}
		});
		UIUtilities.centerAndShow(d);
	}
	
	/**
	 * Sets the colour of the lens to better contrast with the 
	 * background of the image.
	 */
	public void setLensPreferredColour()
	{
		lens.setLensColour(lensModel.getLensPreferredColour());
	}
	
	/** Hides the lens and the control dialog. */
	public void zoomWindowClosed()
	{
		zoomWindow.setVisible(false);
		lens.setVisible(false);
	}
	
	/**
	 * Sets the mapping from pixel size to microns along the x and y axis. 
	 * 
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	public void setXYPixelMicron(Length  x, Length y)
	{
		zoomWindow.setXYPixelMicron(x, y);
	}
	
	/**
	 * Sets the plane image of the lens to a new image. 
	 * 
	 * @param img new Image.
	 */
	public void setPlaneImage(BufferedImage img)
	{
		lensModel.setPlaneImage(img);
		zoomWindow.paintImage();
	}
	
	/**
	 * Sets the visibility of the lens, and ZoomWindowUI.
	 * 
	 * @param makeVisible The value to set.
	 * 
	 */
	public void setVisible(boolean makeVisible)
	{
	    lens.setVisible(makeVisible);
		zoomWindow.setVisible(makeVisible);
	}
	
	/**
	 * Sets the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @param imageZoomFactor The amount of zooming that has occurred on the 
	 *                        image. 
	 */
	public void setImageZoomFactor(float imageZoomFactor)
	{
		/*
		if (imageZoomFactor < LensModel.MINIMUM_ZOOM)
			imageZoomFactor = LensModel.MINIMUM_ZOOM;
		if (imageZoomFactor > LensModel.MAXIMUM_ZOOM)
			imageZoomFactor = LensModel.MAXIMUM_ZOOM;
			*/
		lensModel.setImageZoomFactor(imageZoomFactor);
		lens.setImageZoomFactor();
	}
	
	/**
	 * Sets the location of the lens on the canvas.
	 * 
	 * @param x The x-coordinate.
	 * @param y The y-coordinate. 
	 */
	public void setLensLocation(int x, int y)
	{
		lensController.setLensLocation(x, y);
	}
	
	/**
	 * Returns the zoomed image.
	 * 
	 * @return See above.
	 */
	public BufferedImage getZoomedImage() { return lensModel.getZoomedImage(); }
	
	/**
	 * Returns the lens UI.
	 * 
	 * @return See above. 
	 */
	public JComponent getLensUI() { return lens; }
	
	/**
	 * Returns <code>true</code> if the lens and zoomWindow are visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isVisible()
	{
		return (lens.isVisible() && zoomWindow.isVisible());
	}

	/**
	 * Sets the location of the lens to point.
	 * 
	 * @param loc The value to set.
	 */
	public void setLensLocation(Point loc) { setLensLocation(loc.x, loc.y); }
	
	/** 
	 * Returns the scaled image size, takes into account the zoom factor of the 
	 * image viewer. 
	 * 
	 * @return size of lens, scaled by image zoom factor. 
	 */
	public Dimension getLensScaledSize()
	{ 
		return lensModel.getLensScaledSize();
	}
	
	/** 
	 * Gets the scaled image location, takes into account the zoom factor of the 
	 * image viewer. 
	 * 
	 * @return location of lens, scaled by image zoom factor. 
	 */
	public Point getLensScaledLocation()
	{
		return lensModel.getLensScaledLocation();
	}
	
	/**
	 * Sets the colour of the lens border to the colour specified. 
	 * 
	 * @param color The color to set.
	 */
	public void setLensColour(Color color)
	{
		if (color == null) return;
		lens.setLensColour(color);
	}
	
	/**
	 * Returns the bounds of the scaled image size, takes into account the zoom 
	 * factor of the image viewer.
	 *  
	 * @return See above.
	 */
	public Rectangle getLensScaledBounds()
	{
		return lensModel.getLensScaledBounds();
	}
	
	/** 
	 * Gets the image location.
	 * 
	 * @return The location of lens. 
	 */
	public Point getLensLocation() { return lensModel.getLensLocation(); }
	
	/**
	 * Returns the view of the zoom window.
	 * 
	 * @return See above.
	 */
	public Component getZoomWindow() { return zoomWindow; }
	
	/**
	 * Returns a zoomed image of the passed image.
	 * 
	 * @param image The image to zoom.
	 * @return See above.
	 */
	public BufferedImage createZoomedImage(BufferedImage image)
	{
		if (image == null) return null;
		return lensModel.createZoomedImage(image);
	}
	
	/**
	 * Sets the image to be magnified and the location of the lens.
	 * 
	 * @param image	The image to magnify.
	 * @param f		The amount of zooming that has occurred on the image. 
	 * @param x		The x-coordinate of the lens.
	 * @param y		The y-coordinate of the lens.
	 */
	public void resetLens(BufferedImage image, float f, int x, int y)
	{
		lensModel.setImageZoomFactor(f);
		lensModel.setPlaneImage(image);
		lensModel.setLensLocation(x, y);
		//from ZoomFactor
		lens.setImageZoomFactor();
		//from PlaneImage
		lensController.setLensLocation(x, y);
		setLensSize(lensModel.getWidth(), lensModel.getHeight());
		zoomWindow.paintImage();
		if (!zoomWindow.isVisible())
			zoomWindow.setSize(ZoomWindow.DEFAULT_SIZE);
	}

	/** Indicates to reset the zoomed buffer to <code>null</code>*/
	public void resetDataBuffered() { lensModel.resetDataBuffer(); }
	
	/** 
	 * Sets the background color.
	 * 
	 * @param color The color to set
	 */
	public void setBackgroundColor(Color color)
	{ 
		lensModel.setBackgroundColor(color);
		zoomWindow.updateBackgroundColor();
	}
	
	/**
	 * Sets the name of the image.
	 * 
	 * @param imageName The name of the image.
	 */
	public void setImageName(String imageName)
	{
		lensModel.setImageName(imageName);
	}
	
}
