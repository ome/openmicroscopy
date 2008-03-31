/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.DataObject;
import pojos.ImageData;

/** 
 * 
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
class BrowserComponent 
	extends AbstractComponent
	implements Browser
{

	private BrowserModel 	model;
	
	private BrowserUI 		view;
	
	private BrowserControl	control;
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#accept(ImageDisplayVisitor)
	 */
	public void accept(ImageDisplayVisitor visitor)
	{
		 accept(visitor, ImageDisplayVisitor.ALL_NODES);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#accept(ImageDisplayVisitor, int)
	 */
	public void accept(ImageDisplayVisitor visitor, int algoType)
	{
		 //rootDisplay.accept(visitor, algoType);
	}

	public Set getImageNodes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<ImageData> getImages() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageDisplay getLastSelectedDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<DataObject> getOriginal() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getPopupPoint()
	 */
	public Point getPopupPoint() { return model.getPopupPoint(); }

	public Set getRootNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getSelectedDisplays() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSelectedLayout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public JComponent getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isMouseOver() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMultiSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRollOver() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isThumbSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTitleBarVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public void resetChildDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void setFilterNodes(Collection<ImageDisplay> nodes) {
		// TODO Auto-generated method stub
		
	}

	public void setMouseOver(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setPopupPoint(java.awt.Point)
	 */
	public void setPopupPoint(Point p)
	{
		Point oldValue = model.getPopupPoint();
		if (oldValue.x == p.x && oldValue.y == p.y) return;
		firePropertyChange(POPUP_POINT_PROPERTY, oldValue, p);
	}

	public void setRollOver(boolean rollOver) {
		// TODO Auto-generated method stub
		
	}

	public void setSelectedDisplay(ImageDisplay node) {
		// TODO Auto-generated method stub
		
	}

	public void setSelectedDisplays(ImageDisplay[] nodes) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedLayout(int)
	 */
	public void setSelectedLayout(int index)
	{
	    int oldIndex = model.getSelectedLayout();//selectedLayout;
	    /*
	    switch (index) {
	        case LayoutFactory.SQUARY_LAYOUT:
	        case LayoutFactory.FLAT_LAYOUT:  
	            selectedLayout = index;
	            break;
	        default:
	            selectedLayout = LayoutFactory.SQUARY_LAYOUT;
	    }
	    firePropertyChange(LAYOUT_PROPERTY, new Integer(oldIndex), 
	                    new Integer(selectedLayout));
	                    */
	}

	public void setSelectedNodes(List<DataObject> nodes) {
		// TODO Auto-generated method stub
		
	}

	public void setThumbSelected(boolean selected, ImageNode node) {
		// TODO Auto-generated method stub
		
	}

	public void setTitleBarVisible(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void showAll() {
		// TODO Auto-generated method stub
		
	}

}
