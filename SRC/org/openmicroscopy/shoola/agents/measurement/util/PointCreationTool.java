/*
 * org.openmicroscopy.shoola.agents.measurement.util.PointCreationTool 
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
package org.openmicroscopy.shoola.agents.measurement.util;

//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

//Third-party libraries
import org.jhotdraw.draw.AbstractTool;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.undo.CompositeEdit;
import org.openmicroscopy.shoola.util.roi.figures.PointAnnotationFigure;

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
public 	class PointCreationTool 
		extends AbstractTool
		implements MeasureCreationTool
{
	private boolean						resetToSelect = false;
	private Map<AttributeKey, Object>	prototypeAttributes;
	
	private String						name;
	
	private Dimension					minimalSizeTreshold	=	new Dimension(
										(int)PointAnnotationFigure.FIGURESIZE, 
										(int)PointAnnotationFigure.FIGURESIZE);
	
	/**
	 * We set the figure to the minimal size, if it is smaller than the minimal
	 * size treshold.
	 */
	private Dimension					minimalSize			=new Dimension(
										(int)PointAnnotationFigure.FIGURESIZE, 
										(int)PointAnnotationFigure.FIGURESIZE);

	/**
	 * The prototype for new figures.
	 */
	private Figure						prototype;
	
	/**
	 * The created figure.
	 */
	protected Figure					createdFigure;
	
	protected CompositeEdit				creationEdit;
	
	/** Creates a new instance. */
	public PointCreationTool(String prototypeClassName)
	{
		this(prototypeClassName, null, null);
	}
	
	public PointCreationTool(String prototypeClassName,
			Map<AttributeKey, Object> attributes)
	{
		this(prototypeClassName, attributes, null);
	}
	
	public PointCreationTool(String prototypeClassName,
			Map<AttributeKey, Object> attributes, String name)
	{
		try
		{
			this.prototype=
					(Figure) Class.forName(prototypeClassName).newInstance();
		}
		catch (Exception e)
		{
			InternalError error=
					new InternalError("Unable to create Figure from "
							+prototypeClassName);
			error.initCause(e);
			throw error;
		}
		this.prototypeAttributes=attributes;
		this.name=name;
	}
	
	public PointCreationTool(Figure prototype)
	{
		this(prototype, null, null);
	}
	
	/** Creates a new instance. */
	public PointCreationTool(Figure prototype,
			Map<AttributeKey, Object> attributes)
	{
		this(prototype, attributes, null);
	}
	
	/** Creates a new instance. */
	public PointCreationTool(Figure prototype,
			Map<AttributeKey, Object> attributes, String name)
	{
		this.prototype=prototype;
		this.prototypeAttributes=attributes;
		this.name=name;
	}
	
	
	public Figure getPrototype()
	{
		return prototype;
	}
	
	
	public void activate(DrawingEditor editor)
	{
		super.activate(editor);
		// getView().clearSelection();
		// getView().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	
	public void deactivate(DrawingEditor editor)
	{
		super.deactivate(editor);
		if (getView()!=null)
		{
			getView().setCursor(Cursor.getDefaultCursor());
		}
		if (createdFigure!=null)
		{
			if (createdFigure instanceof CompositeFigure)
			{
				((CompositeFigure) createdFigure).layout();
			}
			createdFigure=null;
		}
	}
	
	
	public void mousePressed(MouseEvent evt)
	{
		super.mousePressed(evt);
		getView().clearSelection();
		// FIXME - Localize this label
		creationEdit=new CompositeEdit("Figur erstellen");
		getDrawing().fireUndoableEditHappened(creationEdit);
		createdFigure=createFigure();
		
		Point2D.Double p=constrainPoint(viewToDrawing(anchor));
		anchor.x=(int)(evt.getX()-PointAnnotationFigure.FIGURESIZE/2);
		anchor.y=(int)(evt.getY()-PointAnnotationFigure.FIGURESIZE/2);
		Point2D.Double p1 = new Point2D.Double(anchor.x, anchor.y);
		Point2D.Double p2 = new Point2D.Double(anchor.x+
			(PointAnnotationFigure.FIGURESIZE), anchor.y+
			(PointAnnotationFigure.FIGURESIZE));
		createdFigure.willChange();
		createdFigure.basicSetBounds(p1, p2);
		createdFigure.changed();
		getDrawing().add(createdFigure);
	}
	
	
	public void mouseDragged(MouseEvent evt)
	{
		if (createdFigure!=null)
		{
			Point2D.Double p=constrainPoint(new 
				Point(	(int)(evt.getX()-PointAnnotationFigure.FIGURESIZE/2), 
						(int)(evt.getY()-PointAnnotationFigure.FIGURESIZE/2)));
			Point2D.Double p2=constrainPoint(new 
				Point(	(int)(evt.getX()+PointAnnotationFigure.FIGURESIZE/2), 
						(int)(evt.getY()+PointAnnotationFigure.FIGURESIZE/2)));
			createdFigure.willChange();
			createdFigure.basicSetBounds(constrainPoint(p), p2);
			createdFigure.changed();
		}
	}
	
	public void mouseReleased(MouseEvent evt)
	{
		if (createdFigure!=null)
		{
			Rectangle2D.Double bounds=createdFigure.getBounds();
			if (bounds.width==0&&bounds.height==0)
			{
				getDrawing().remove(createdFigure);
			}
			else
			{
			/*	if (Math.abs(anchor.x-evt.getX())<minimalSizeTreshold.width
						&&Math.abs(anchor.y-evt.getY())<minimalSizeTreshold.height)
				{
					createdFigure.basicSetBounds(constrainPoint(new Point(
						anchor.x, anchor.y)), constrainPoint(new Point(anchor.x
							+(int) Math.max(bounds.width, minimalSize.width),
						anchor.y
								+(int) Math.max(bounds.height,
									minimalSize.height))));
				}*/
				getView().addToSelection(createdFigure);
			}
			if (createdFigure instanceof CompositeFigure)
			{
				((CompositeFigure) createdFigure).layout();
			}
			getDrawing().fireUndoableEditHappened(creationEdit);
			creationFinished(createdFigure);
			createdFigure=null;
		}
	}
	
	
	protected Figure createFigure()
	{
		Figure f=(Figure) prototype.clone();
		getEditor().applyDefaultAttributesTo(f);
		if (prototypeAttributes!=null)
		{
			for (Map.Entry<AttributeKey, Object> entry : prototypeAttributes
				.entrySet())
			{
				f.setAttribute(entry.getKey(), entry.getValue());
			}
		}
		return f;
	}
	
	protected Figure getCreatedFigure()
	{
		return createdFigure;
	}
	
	protected Figure getAddedFigure()
	{
		return createdFigure;
	}

    protected void creationFinished(Figure createdFigure) 
    {
        if(resetToSelect)
        	fireToolDone();
    }
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#isResetToSelect()
	 */
	public boolean isResetToSelect()
	{
		return resetToSelect;
	}
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#setResetToSelect(boolean)
	 */
	public void setResetToSelect(boolean create)
	{
		resetToSelect = create;
	}
}
