/*
 * measurement.ui.DrawingCanvas 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.drawingcanvas;


//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

//Third-party libraries
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.draw.FigureSelectionListener;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.TextFigure;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingEventList;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIControl;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIModel;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DrawingCanvas
	extends Component
	implements PropertyChangeListener
{
	private 	JPanel				panel;
	private 	BufferedImage 		image;
	private		UIModel				model;
	private		UIControl			control;
	private		Drawing				drawing;
	
	public DrawingCanvas(JPanel panel, UIControl control, UIModel model)
	{
		this.image = image;
		this.panel = panel;
		this.model = model;
		this.control = control;
		createUI();
	}

	private void createUI()
	{
		model.addPropertyChangeListener(this);
		panel.setLayout(new BorderLayout());
		model.getView().getComponent().setOpaque(false);
		panel.add(model.getView().getComponent(), BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent changeEvent) 
	{
		
	}

}


