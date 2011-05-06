/*
 * org.openmicroscopy.shoola.util.ui.lens.LensController.java
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Cursor;

//Third-party libraries

//Application-internal dependencies

/** 
 * The LensController is the main controlling class which manipulates the lens, 
 * Zoomwindow and allows the user to change the size and magnification of the
 * lens.
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
class LensController 
{
	
	/** The default zoom value.*/
	static final float DEFAULT_ZOOM = 2.0f;
	
	/** Reference to the Lens Model.  */
	private	LensModel	lensModel;
	
	/** Reference to the lens object which will be displayed on the Browser. */
	private LensUI		lens;
	
	/** X-coordinate of offset used to determine the pick spot on the lens. */
	private int			offsetX;
	
	/** Y-coordinate of offset used to determine the pick spot on the lens. */
	private int			offsetY;
	
	/** X-coordinate of the starting position of the lens. */
	private int			startX;

	/** Y-coordinate of the starting position of the lens. */
	private int			startY;
	
	/** Zoom Window which displays the area under the lens zoomed. */
	private ZoomWindow  zoomWindow;
	 
	/** The direction in which the lens will be resized. */
	private	int			resizeDir;
	
	/** This will be set to <code>true</code> if the lens is being dragged. */
  	private boolean		lensDrag;
	
  	/** This will be set to <code>true</code> if the border is being dragged. */
  	private boolean		borderDrag;
	
	/**
	 * Checks to see if the bounds of the lens are within the constraints;
	 * {@link LensUI#MINIMUM_WIDTH}, {@link LensUI#MINIMUM_HEIGHT}, 
	 * {@link LensUI#MAXIMUM_WIDTH} or {@link LensUI#MAXIMUM_HEIGHT}.
	 * 
	 * @param dx change in x-coordinate.
	 * @param dy change in y-coordinate.
	 * @param dw change in width.
	 * @param dh change in height.
	 * 
	 * @return true if in bounds.
	 */
	private boolean checkBounds(int dx, int dy, int dw , int dh)
	{
		int newX = lens.getX()+dx;
		int newY = lens.getY()+dy;
		int newWidth = (int) ((lens.getWidth()+dw)/
							lensModel.getImageZoomFactor());
		int newHeight = (int) ((lens.getHeight()+dh)/
								lensModel.getImageZoomFactor());
	
		return (newX > 0 && newY > 0 && newWidth >= LensUI.MINIMUM_WIDTH 
				&& newWidth <= LensUI.MAXIMUM_WIDTH	
				&& newHeight >= LensUI.MINIMUM_HEIGHT 
				&& newHeight <= LensUI.MAXIMUM_HEIGHT &&
				newHeight+newY <= lensModel.getImageScaledHeight() &&
				newWidth+newX <= lensModel.getImageScaledWidth());
	}
	
	/**
	 * Sets the size and position of the lens based on the parameters provided.
	 * Updates, model and zoomwindowUI at the same time. 
	 * 
	 * @param dx change in x-coordinate.
	 * @param dy change in y-coordinate.
	 * @param dw change in width.
	 * @param dh change in height.
	 * @param keepSquare adjust the deltas so the lens is square when moving.
	 */
	private void setLensBounds(int dx, int dy, int dw, int dh, 
														boolean keepSquare)
	{
		int newX = lens.getX()+dx;
		int newY = lens.getY()+dy;
		int newWidth, newHeight;
		
		if (keepSquare) {
			int mx = Math.max(dw, dh);
			int mn = Math.min(dw, dh);
			if (Math.abs(mx) > Math.abs(mn)) {
				dw = mx;
				dh = mx;
			} else {
				dw = mn;
				dh = mn;
			}
		}

		if (checkBounds(dx, dy, dw, dh)) {
			newWidth = lens.getWidth()+dw;
			newHeight = lens.getHeight()+dh;			
			
			lens.setBounds(newX, newY, newWidth, newHeight);
			double f = lensModel.getImageZoomFactor();
			lensModel.setLensLocation((int) (newX/f), (int)(newY/f));
			lensModel.setWidth((int) (newWidth/f));
			lensModel.setHeight((int) (newHeight/f));
			zoomWindow.setLensXY(lensModel.getX(), lensModel.getY());
			zoomWindow.setLensWidthHeight(lensModel.getWidth(), 
					lensModel.getHeight());
			setZoomUISize();
		}
	}
	
	/**
	 * Sets the new UI size of the zoom window based on the zoomfactor and
	 * lens size. 
	 */
	private void setZoomUISize()
	{
		float zoomFactor = lensModel.getZoomFactor();
		int width = lensModel.getWidth();
		int height = lensModel.getHeight();
		zoomWindow.setZoomUISize(width*zoomFactor, height*zoomFactor);
		zoomWindow.setLensZoomFactor(zoomFactor);
		//zoomWindow.setZoomImage(lensModel.getZoomedImage());
	}
	
	/**
	 * Returns the correct cursor type for the border edge selected.
	 * 
	 * @param resizeDir Direction of the border picked. 
	 * @return See above.
	 */
	private Cursor getCursorForDir(int resizeDir)
	{
		Cursor s = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        switch (resizeDir) 
        {
            case LensUI.SOUTH:
                s = new Cursor(Cursor.S_RESIZE_CURSOR);
                break; 
            case LensUI.NORTH:
                s = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                break; 
            case LensUI.WEST:
                s = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                break; 
            case LensUI.EAST:
                s = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                break; 
            case LensUI.SOUTH_EAST:
                s = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                break; 
            case LensUI.SOUTH_WEST:
                s = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                break; 
            case LensUI.NORTH_WEST:
                s = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                break; 
            case LensUI.NORTH_EAST:
                s = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                break;
        } 
        return s;
	}
	
  	/**
	 * Creates a new instance.
	 * 
	 * @param model			Model of the data. Mustn't be <code>null</code>.
	 * @param lens 			Viewport on image canvas. 
	 * 						Mustn't be <code>null</code>.
	 * @param zoomWindow 	Zoomed version of the viewport on image canvas.
	 * 						Mustn't be <code>null</code>.
	 */
	LensController(LensModel model, LensUI lens, ZoomWindow zoomWindow)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (lens == null)
			throw new IllegalArgumentException("No view.");
		if (zoomWindow == null)
			throw new IllegalArgumentException("No window.");
		lensModel = model;
		this.lens = lens;
		this.zoomWindow = zoomWindow;
		lensDrag = false;
		borderDrag = false;
	} 
	
	/**
	 * Sets the position of the lens to x,y  on the image.
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensLocation(int x, int y)
	{
		lensModel.setLensLocation(x,y);
		lens.setLocation(lensModel.getScaledX(),lensModel.getScaledY());
		//zoomWindow.setZoomImage(lensModel.getZoomedImage());
		zoomWindow.setLensXY(lensModel.getX(), lensModel.getY());
		zoomWindow.setLensWidthHeight(lensModel.getWidth(), 
										lensModel.getHeight());
	}
	
	/**
	 * Sets the size of the lens to width, height  on the image.
	 * 
	 * @param w See above.
	 * @param h See above.
	 */
	void setLensSize(int w, int h)
	{
		int scaledW, scaledH;
		
		scaledW = (int) (w*lensModel.getImageZoomFactor());
		scaledH = (int) (h*lensModel.getImageZoomFactor());
		if (lens.getX()+scaledW > lensModel.getImageScaledWidth())
		{
			scaledW = lensModel.getImageScaledWidth()-lens.getX();
			w = (int) (scaledW/lensModel.getImageZoomFactor());
		}
		
		if (lens.getY()+scaledH > lensModel.getImageScaledHeight())
		{
			scaledH = lensModel.getImageScaledHeight()-lens.getY();
			h = (int) (scaledH/lensModel.getImageZoomFactor());
		}
		
		lensModel.setWidth(w);
		lensModel.setHeight(h);
		lens.setSize(scaledW, scaledH);
		//zoomWindow.setZoomImage(lensModel.getZoomedImage());
		zoomWindow.setLensWidthHeight(lensModel.getWidth(), 
									lensModel.getHeight());
		setZoomUISize();
	}
	
	/** 
	 * Sets the zoomfactor for the lens. 
	 * 
	 * @param zoomFactor The value to set.
	 */
	void setZoomFactor(float zoomFactor)
	{
		lensModel.setZoomFactor(zoomFactor);
		setZoomUISize();
		//zoomWindow.repaint();
		//zoomWindow.setZoomImage(lensModel.getZoomedImage());
	}
	
	/**
	 * Shows or hides the crosshairs on the lens.
	 * 
	 * @param isVisible Pass <code>true</code> to show, <code>false</code>
	 * 					to hide.
	 */
	void setShowCrossHairs(boolean isVisible)
	{
		lens.setShowCrossHair(isVisible);
	}
	
	/**
	 * Mouse up event triggered from lens.
	 * 
	 * @param x x mouse position.
	 * @param y y mouse position.
	 */
	void lensMouseUp(int x, int y)
	{
		lensDrag = false;
		borderDrag = false;
		offsetX = 0;
		offsetY = 0;
		lens.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	/**
	 * Mouse moved event triggered from lens.
	 * 
	 * @param x x mouse position.
	 * @param y y mouse position.
	 */
	void lensMouseMoved(int x, int y)
	{
		if (lens.lensPicked(x, y))
		{
			Cursor s = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			lens.setCursor(s);
		}	
		else if (lens.lensBorderPicked(x, y))
		{
			resizeDir = lens.getPickDir(x, y);
		    Cursor s = getCursorForDir(resizeDir);
            lens.setCursor(s);
		}
	}
	
	/**
	 * Mouse down event triggered from lens.
	 * 
	 * @param x x mouse position.
	 * @param y y mouse position.
	 */
	void lensMouseDown(int x, int y)
	{
		if (lens.lensBorderPicked(x, y))
		{
			borderDrag = true;
			resizeDir = lens.getPickDir(x, y);
		    Cursor s = getCursorForDir(resizeDir);
            lens.setCursor(s);
            startX = lens.getX();
            startY = lens.getY();
        	offsetX = x;
			offsetY = y;
		
		}
		else if (lens.lensPicked(x, y))
		{
			offsetX = x;
			offsetY = y;
			startX = lens.getX();
			startY = lens.getY();
			lensDrag = true;
		}
	}
	
	/**
	 * Mouse drag event triggered from lens, if the shift key is pressed 
	 * dX and dY will be the same; equal to which ever one is greater, this 
	 * is used to keep the lens square when shift pressed. 
	 * 
	 * @param x x mouse position.
	 * @param y y mouse position.
	 * @param isShiftDown Pass <code>true</true> if the shift key is pressed, 
	 * 				<code>false</true> otherwise.
	 */
	void lensMouseDrag(int x, int y, boolean isShiftDown)
	{
		if (borderDrag)
		{
			int deltaX = 0;
			int deltaY = 0;
			int deltaW = 0;
			int deltaH = 0;
			
			switch(resizeDir)
			{
				case LensUI.NORTH:
					if (y-offsetY < 0)
					{
						deltaY = (y-offsetY);
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
					else
					{
						deltaY = (y-offsetY);
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
				break;
				case LensUI.SOUTH:
					if (y-offsetY > 0)
					{
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
					else
					{
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
				break;		
				case LensUI.EAST:
					if (x-offsetX > 0)
					{
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
					else
					{
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
				break;		
				case LensUI.WEST:
					if (x-offsetX > 0)
					{
						deltaX = (x-offsetX);
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
					else
					{
						deltaX = -Math.abs(x-offsetX);
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
				break;		
				case LensUI.NORTH_EAST:
					if (y-offsetY < 0)
					{
						deltaY = (y-offsetY);
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
					else
					{
						deltaY = (y-offsetY);
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
					if (x-offsetX > 0)
					{
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
					else
					{
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
				break;
				case LensUI.NORTH_WEST:
					if (y-offsetY < 0)
					{
						deltaY = (y-offsetY);
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
					else
					{
						deltaY = (y-offsetY);
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startY = startY+(y-offsetY);
					}
					if (x-offsetX > 0)
					{
						deltaX = (x-offsetX);
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
					else
					{
						deltaX = -Math.abs(x-offsetX);
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
					break;
				case LensUI.SOUTH_EAST:
					if (y-offsetY > 0)
					{
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
					else
					{
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
					if (x-offsetX > 0)
					{
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
					else
					{
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetX = x;
					}
				break;
				case LensUI.SOUTH_WEST:
					if (y-offsetY > 0)
					{
						deltaH = Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
					else
					{
						deltaH = -Math.abs(y-offsetY);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							offsetY = y;
					}
					if (x-offsetX > 0)
					{
						deltaX = (x-offsetX);
						deltaW = -Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
					else
					{
						deltaX = -Math.abs(x-offsetX);
						deltaW = Math.abs(x-offsetX);
						if (checkBounds(deltaX, deltaY, deltaW, deltaH))
							startX = startX+(x-offsetX);
					}
				break;
			}
			setLensBounds(deltaX, deltaY, deltaW, deltaH, isShiftDown);
		}
		if (lensDrag)
		{
			boolean moveX = true;
			boolean moveY = true;
			if(startX+(x-offsetX) < 0)
			{
				moveX = false;
				startX = 0;
			}
			double v = (startX+(x-offsetX))/lensModel.getImageZoomFactor()+
						lensModel.getWidth();
			if (v > lensModel.getImageWidth())
			{
				moveX = false;
				startX = lensModel.getImageScaledWidth()-
						lensModel.getScaledWidth();
			}
				
			if (startY+(y-offsetY) < 0)
			{
				moveY = false;
				startY = 0;
			}
			if (startY+(y-offsetY)+lens.getHeight() > 
				lensModel.getImageScaledHeight())
			{
				moveY = false;
				startY = lensModel.getImageScaledHeight()-
						lensModel.getScaledHeight();
			}
			
			if (moveX)
				startX = startX+(x-offsetX);
			if (moveY)
				startY = startY+(y-offsetY);
			setLensLocation((int) (startX/lensModel.getImageZoomFactor()),
					(int) (startY/lensModel.getImageZoomFactor()));
		}
	}

}
