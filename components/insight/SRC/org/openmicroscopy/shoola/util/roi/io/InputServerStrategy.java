/*
 * org.openmicroscopy.shoola.util.roi.io.InputServerStrategy
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.roi.io;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_BOLD;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.FONT_ITALIC;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.LineDecoration;
import org.jhotdraw.geom.BezierPath.Node;

import omero.model.Length;
import omero.model.enums.UnitsLength;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.Cap;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.PointFigure;

import omero.gateway.model.EllipseData;
import omero.gateway.model.LineData;
import omero.gateway.model.PointData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ShapeSettingsData;
import omero.gateway.model.TextData;


/** 
 * Transforms the ROI server into the corresponding UI objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class InputServerStrategy
{

	/** Identifies the transformation attribute. */
	public final static AttributeKey<AffineTransform> TRANSFORM = 
		  new AttributeKey<AffineTransform>("transform", null, true);
	
	/**
	 * The map of the default values of each object along with the keys used.
	 */
	private final static Map<AttributeKey, Object>	DEFAULT_ATTRIBUTES;
	static
	{
		DEFAULT_ATTRIBUTES = new HashMap<AttributeKey, Object>();
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.FILL_COLOR,
				ShapeSettingsData.DEFAULT_FILL_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.STROKE_COLOR,
			ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.TEXT_COLOR,
				ShapeSettingsData.DEFAULT_FILL_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.FONT_SIZE, 
				ShapeSettingsData.DEFAULT_FONT_SIZE);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.STROKE_WIDTH, 
				ShapeSettingsData.DEFAULT_STROKE_WIDTH);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.TEXT, 
				ROIFigure.DEFAULT_TEXT);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
				ShapeSettingsData.DEFAULT_FILL_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.SHOWMEASUREMENT, 
				Boolean.valueOf(false));
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.SHOWTEXT, 
				Boolean.valueOf(false));
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.SCALE_PROPORTIONALLY,
				Boolean.valueOf(false));
	}
	
	/** Holds the ROIs which have been created. */
	private List<ROI>			roiList;
	
	/** The ROIComponent. */
	private ROIComponent		component;
	
	/**
	 * Adds any missing basic attributes from the default attributes map, 
	 * to the figure.
	 * 
	 * @param figure The figure to handle.
	 */
	private void addMissingAttributes(ROIFigure figure)
	{
		Map<AttributeKey, Object> attributes=figure.getAttributes();
		Iterator<AttributeKey> i = DEFAULT_ATTRIBUTES.keySet().iterator();
		AttributeKey key;
		while (i.hasNext())
		{
			key = i.next();
			if (!attributes.containsKey(key)) 
				key.set(figure, DEFAULT_ATTRIBUTES.get(key));
		}
	}
	
	/**
	 * Transforms a server ROI into its UI representation.
	 * 
	 * @param roi The object to transform.
	 * @param userID The id of the user currently logged in.
	 * @return See above.
	 */
	private ROI createROI(ROIData roi, long userID)
		throws NoSuchROIException, ROICreationException
	{
		long id = roi.getId();
		boolean edit = roi.canEdit();
		if (edit) {
			edit = roi.getOwner().getId() == userID;
		}
		ROI newROI = component.createROI(id, id <= 0, edit,
				roi.canDelete(), roi.canAnnotate(), roi.getFolders());
		newROI.setOwnerID(roi.getOwner().getId());
		ROIShape shape;
		ShapeData shapeData;
		Iterator<List<ShapeData>> i = roi.getIterator();
		List<ShapeData> list;
		Iterator<ShapeData> j;
		Coord3D c;
		while (i.hasNext()) {
			list = (List<ShapeData>) i.next();
			j = list.iterator();
			while (j.hasNext()) {
				shapeData = (ShapeData) j.next();
				shape = createROIShape(shapeData, newROI, userID);
				if (shape != null) {
					shape.getFigure().setMeasurementUnits(
							component.getMeasurementUnits());
					c = shape.getCoord3D();
					if (c != null) {
						if (!component.containsShape(newROI.getID(), c)) {
							component.addShape(newROI.getID(), c, shape);
						}
					}
				}
			}
		}
		return newROI;
	}
	
	/**
	 * Transforms the shape into its corresponding the UI object.
	 * 
	 * @param data 	The object to transform.
	 * @param roi	The UI ROI hosting the newly created shape.
	 * @param userID The id of the user currently logged in.
	 * @return See above.
	 */
	private ROIShape createROIShape(ShapeData data, ROI roi, long userID)
	{
		int z = data.getZ();
		int t = data.getT();
		Coord3D coord = new Coord3D(z, t);
		ROIFigure fig = createROIFigure(data);
		fig.setReadOnly(data.isReadOnly());
		long id = data.getOwner().getId();
		if (id >= 0) fig.setInteractable(id == userID);
		try {
			coord.setChannel(data.getC());
		} catch (Exception e) {
		}
		
		// Check that the parent element is not a text element, as they have not
		// got any other text associated with them.
		addMissingAttributes(fig);
		ROIShape shape = new ROIShape(roi, coord, fig, fig.getBounds());
		shape.setROIShapeID(data.getId());
		shape.setData(data);
		return shape;
	}

	
	/**
	 * Creates a figure corresponding to the passed shape.
	 * 
	 * @param shape The shape to transform.
	 * @return See above.
	 */
	private ROIFigure createROIFigure(ShapeData shape)
	{
		if (shape instanceof RectangleData) {
			return createRectangleFigure((RectangleData) shape);
		} else if (shape instanceof EllipseData) {
			return createEllipseFigure((EllipseData) shape);
		} else if (shape instanceof LineData) {
			return createLineFigure((LineData) shape);
		} else if (shape instanceof PointData) {
			return createPointFigure((PointData) shape);
		} else if (shape instanceof PolylineData) {
			return createPolyOrlineFigure((PolylineData) shape);
		} else if (shape instanceof PolygonData) {
			return createPolygonFigure((PolygonData) shape);
		} else if (shape instanceof MaskData) {
			return createMaskFigure((MaskData) shape);
		} else if (shape instanceof TextData) {
			return createTextFigure((TextData) shape);
		}
		return null;
	}
	
	/**
	 * Transforms the passed ellipse into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasureEllipseFigure createEllipseFigure(EllipseData data)
	{
		double x = data.getX();
		double y = data.getY();
		double radiusx = data.getRadiusX();
		double radiusy = data.getRadiusY();
		
		double x0 = x-radiusx;
		double y0 = y-radiusy;
		double width = radiusx*2d;
		double height = radiusy*2d;
		MeasureEllipseFigure fig = new MeasureEllipseFigure(data.getText(), 
				x0, y0, width, height, data.isReadOnly(),
					data.isClientObject(), data.canEdit(), data.canDelete(), 
					data.canAnnotate());
		fig.setEllipse(x0, y0, width, height);
		fig.setText(data.getText());
		addShapeSettings(fig, data.getShapeSettings());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	/**
	 * Transforms the passed ellipse into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasurePointFigure createPointFigure(PointData data)
	{
		double r = PointFigure.FIGURE_SIZE/2;
		double x = data.getX()-r;
		double y = data.getY()-r;
		
		MeasurePointFigure fig = new MeasurePointFigure(data.getText(), x, y, 
		        PointFigure.FIGURE_SIZE, PointFigure.FIGURE_SIZE, data.isReadOnly(), data.isClientObject(), 
				data.canEdit(), data.canDelete(), data.canAnnotate());
		addShapeSettings(fig, data.getShapeSettings());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	/**
	 * Transforms the passed textData into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasureTextFigure createTextFigure(TextData data)
	{
		double x = data.getX();
		double y = data.getY();
		
		MeasureTextFigure fig = new MeasureTextFigure(x, y, 
					data.isReadOnly(), data.isClientObject(), data.canEdit(), 
					data.canDelete(), data.canAnnotate());
		fig.setText(data.getText());
		addShapeSettings(fig, data.getShapeSettings());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	/**
	 * Transforms the passed rectangle into its UI corresponding object.
	 * 
	 * @param data The rectangle to transform.
	 * @return See above.
	 */
	private MeasureRectangleFigure createRectangleFigure(RectangleData data)
	{
		double x = data.getX();
		double y = data.getY();
		double width = data.getWidth();
		double height = data.getHeight();
		
		MeasureRectangleFigure fig = new MeasureRectangleFigure(x, y, width, 
				height, data.isReadOnly(), data.isClientObject(),
				data.canEdit(), data.canDelete(), data.canAnnotate());
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	/**
	 * Transforms the passed mask into its UI corresponding object.
	 * 
	 * @param data The mask to transform.
	 * @return See above.
	 */
	private MeasureMaskFigure createMaskFigure(MaskData data)
	{
		double x = data.getX();
		double y = data.getY();
		double width = data.getWidth();
		double height = data.getHeight();
		BufferedImage mask = data.getMaskAsBufferedImage();
		MeasureMaskFigure fig = new MeasureMaskFigure(x, y, width, 
				height, mask, data.isReadOnly(), data.isClientObject(),
				data.canEdit(), data.canDelete(), data.canAnnotate());
		fig.setVisible(true);
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	
	/**
	 * Transforms the passed line into its UI corresponding object.
	 * 
	 * @param data The line to transform.
	 * @return See above.
	 */
	private MeasureLineFigure createLineFigure(LineData data)
	{
		double x1 = data.getX1();
		double y1 = data.getY1();
		double x2 = data.getX2();
		double y2 = data.getY2();
		
		MeasureLineFigure fig = new MeasureLineFigure(data.isReadOnly(), 
				data.isClientObject(), data.canEdit(), data.canDelete(),
				data.canAnnotate());
		fig.removeAllNodes();
		fig.addNode(new Node(x1, y1));
		fig.addNode(new Node(x2, y2));
		
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	

	/**
	 * Transforms the polygon into its UI corresponding object.
	 * 
	 * @param data The polygon to transform.
	 * @return See above.
	 */
	private MeasureBezierFigure createPolygonFigure(PolygonData data)
	{
		MeasureBezierFigure fig = new MeasureBezierFigure(false, 
				data.isReadOnly(), data.isClientObject(), data.canEdit(),
				data.canDelete(), data.canAnnotate());
		List<Point2D.Double> points = data.getPoints();
		for (int i = 0; i < points.size(); i++)
		{
			fig.addNode(new Node(i, points.get(i), points.get(i), 
					points.get(i)));
		}
		
		addShapeSettings(fig, data.getShapeSettings());
		String text = data.getText();
		if (text == null || text.trim().length() == 0)
			text = ROIFigure.DEFAULT_TEXT;
		fig.setText(text);
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		fig.setClosed(true);		
		return fig;
	}
	
	/**
	 * Test to see if the polyline object passed in is actually a line/multi
	 * segment line or a polyline.
	 * 
	 * @param data The polyline to transform.
	 * @return See above.
	 */
	private ROIFigure createPolyOrlineFigure(PolylineData data)
	{
		return createPolylineFromPolylineFigure(data);
	}	
	
	/**
	 * Transforms the passed polyline into a Polyline shape.
	 * 
	 * @param data The polyline to transform.
	 * @return See above.
	 */
	private ROIFigure createPolylineFromPolylineFigure(PolylineData data)
	{
		List<Point2D.Double> points = data.getPoints();
		MeasureBezierFigure fig = new MeasureBezierFigure(false, 
				data.isReadOnly(), data.isClientObject(), data.canEdit(),
				data.canDelete(), data.canAnnotate());
		for (int i = 0; i < points.size(); i++)
			fig.addNode(new Node(i, points.get(i), points.get(i),
			        points.get(i)));
		
		addShapeSettings(fig, data.getShapeSettings());
		String text = data.getText();
		if (text == null || text.trim().length() == 0)
			text = ROIFigure.DEFAULT_TEXT;
		fig.setText(text);
		TRANSFORM.set(fig, toTransform(data.getTransform()));
		return fig;
	}
	
	/**
	 * Adds the settings to the figure.
	 * 
	 * @param figure	The figure to handle.
	 * @param data		The settings to set.
	 */
	private void addShapeSettings(ROIFigure figure, ShapeSettingsData data)
	{
	    Double value = ShapeSettingsData.DEFAULT_STROKE_WIDTH;
	    Length l;
        try {
            l = data.getStrokeWidth(UnitsLength.PIXEL);
            if (l != null && l.getValue() >= 1) {
                value = l.getValue();
            }
        } catch (Exception e) {
        }
		STROKE_WIDTH.set(figure, value);
		STROKE_COLOR.set(figure, data.getStroke());
		FILL_COLOR.set(figure, data.getFill());
		Font f = data.getFont();
		FONT_FACE.set(figure, f);
		FONT_SIZE.set(figure, (double) f.getSize());
		FONT_ITALIC.set(figure, data.isFontItalic());
		FONT_BOLD.set(figure, data.isFontBold());
		TEXT_COLOR.set(figure, data.getStroke());
		
        if (CommonsLangUtils.isNotBlank(data.getMarkerStart())) {
            LineDecoration dec = null;
            Cap c = Cap.findByValue(data.getMarkerStart());
            if (c != null) {
                dec = c.newLineDecorationInstance();
            }
            MeasurementAttributes.START_DECORATION.set(figure, dec);
        }
        if (CommonsLangUtils.isNotBlank(data.getMarkerEnd())) {
            LineDecoration dec = null;
            Cap c = Cap.findByValue(data.getMarkerEnd());
            if (c != null) {
                dec = c.newLineDecorationInstance();
            }
            MeasurementAttributes.END_DECORATION.set(figure, dec);
        }
	}

    /**
     * Convert an OMERO affine transform to an AWT affine transform.
     * @param omeroTransform an OMERO affine transform, may be {@code null}
     * @return the corresponding AWT affine transform, never {@code null}
     */
    private static AffineTransform toTransform(omero.model.AffineTransform omeroTransform)
    {
        if (omeroTransform == null ||
                omeroTransform.getA00() == null || omeroTransform.getA01() == null || omeroTransform.getA02() == null ||
                omeroTransform.getA10() == null || omeroTransform.getA11() == null || omeroTransform.getA12() == null) {
            return new AffineTransform();
        } else {
            return new AffineTransform(omeroTransform.getA00().getValue(), omeroTransform.getA10().getValue(),
                                       omeroTransform.getA01().getValue(), omeroTransform.getA11().getValue(),
                                       omeroTransform.getA02().getValue(), omeroTransform.getA12().getValue());
        }
    }

	/** Creates a new instance. */
	InputServerStrategy()
	{
		roiList = new ArrayList<ROI>();
	}
	
	/**
	 * Converts the server ROIs and adds them to <code>ROIComponent</code>.
	 * 
	 * @param rois The ROIs to convert.
	 * @param component ROIComponent.
	 * @param userID The identifier of the user.
	 * @return See above.
	 * @throws ROICreationException if ROI cannot be created.
	 * @throws NoSuchROIException if there is an error creating line connection 
	 * figure.
	 */
	List<ROI> readROI(Collection rois, ROIComponent component, long userID)
			throws ROICreationException, NoSuchROIException
	{
		if (component == null)
			throw new IllegalArgumentException("No component.");
		this.component = component;
		Iterator i = rois.iterator();
		Object o;
		ROIData roi;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof ROIData) {
				roi = (ROIData) o;
				roiList.add(createROI(roi, userID));
			}
		}
		return roiList;
	}
	
}
