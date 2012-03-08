/*
 * pojos.ROIData
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.rtypes;
import omero.model.Ellipse;
import omero.model.Image;
import omero.model.Line;
import omero.model.Mask;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.model.Label;

/**
 * Converts the ROI object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ROIData 
	extends DataObject
{

	/** Map hosting the shapes per plane. */
	private TreeMap<ROICoordinate, List<ShapeData>> roiShapes;
	
	/** Is the object client side. */
	private boolean clientSide;
	
	/** Initializes the map. */
	private void initialize()
	{
		Roi roi = (Roi) asIObject();
		List<Shape> shapes = roi.copyShapes();
		if (shapes == null) return;
		Iterator<Shape> i = shapes.iterator();
		ShapeData s;
		ROICoordinate coord;
		List<ShapeData> data;
		Shape shape;
		while (i.hasNext()) {
			shape = i .next();
			s = null;
			if (shape instanceof Rect) 
				s = new RectangleData(shape);
			else if (shape instanceof Ellipse)
				s = new EllipseData(shape);
			else if (shape instanceof Point)
				s = new PointData(shape);
			else if (shape instanceof Polyline)
				s = new PolylineData(shape);
			else if (shape instanceof Polygon)
				s = new PolygonData(shape);
			else if (shape instanceof Label)
				s = new TextData(shape);
			else if (shape instanceof Line)
				s = new LineData(shape);
			else if (shape instanceof Mask)
				s = new MaskData(shape);
			if (s != null) {
				coord = new ROICoordinate(s.getZ(), s.getT());
				if (!roiShapes.containsKey(coord)) {
					data = new ArrayList<ShapeData>();
					roiShapes.put(coord, data);
				} else data = roiShapes.get(coord);
				data.add(s);
			}
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param roi The ROI hosted by the component.
	 */
	public ROIData(Roi roi)
	{
		super();
		setValue(roi);
		roiShapes = new TreeMap<ROICoordinate, List<ShapeData>>
		(new ROICoordinate());
		if (roi != null) initialize();
	}
	
	/** Create a new instance of an ROIData object. */
	public ROIData()
	{
		super();
		setValue(new RoiI());
		roiShapes = new TreeMap<ROICoordinate, List<ShapeData>>
		(new ROICoordinate());
	}

	/**
	 * Sets the imageId for the ROI.
	 * 
	 * @param imageId See above.
	 */
	public void setImage(Image image)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) 
			throw new IllegalArgumentException("No Roi specified.");
		roi.setImage(image);
		setDirty(true);
	}
	
	/**
	 * Returns the image for the ROI.
	 * 
	 * @return See above.
	 */
	public ImageData getImage()
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) 
			throw new IllegalArgumentException("No Roi specified.");
		Image image = roi.getImage();
		if (image == null) return null;
		return new ImageData(image);
	}
	
	/**
	 * Adds ShapeData object to ROIData.
	 * 
	 * @param shape See above.
	 */
	public void addShapeData(ShapeData shape)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) 
			throw new IllegalArgumentException("No Roi specified.");
		ROICoordinate coord = shape.getROICoordinate();
		List<ShapeData> shapeList;
		if (!roiShapes.containsKey(coord))
		{
			shapeList = new ArrayList<ShapeData>();
			roiShapes.put(coord, shapeList);
		}
		else
			shapeList = roiShapes.get(coord);
		shapeList.add(shape);
		roi.addShape((Shape) shape.asIObject());
		setDirty(true);
	}
	
    /**
	 * Removes the ShapeData object from ROIData.
	 * 
	 * @param shape See above.
	 */
	public void removeShapeData(ShapeData shape)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) 
			throw new IllegalArgumentException("No Roi specified.");
		ROICoordinate coord = shape.getROICoordinate();
		List<ShapeData> shapeList;
		shapeList = roiShapes.get(coord);
		shapeList.remove(shape);
		roi.removeShape((Shape) shape.asIObject());
		setDirty(true);
	}
	
	/**
	 * Returns the number of planes occupied by the ROI.
	 * 
	 * @return See above.
	 */
	public int getPlaneCount() { return roiShapes.size(); }
	
	/**
	 * Returns the number of shapes in the ROI. 
	 * @return See above.
	 */
	public int getShapeCount()
	{
		Iterator<ROICoordinate> i = roiShapes.keySet().iterator();
		int cnt = 0;
		List shapeList;
		while(i.hasNext()) {
			shapeList = roiShapes.get(i.next());
			cnt += shapeList.size();
		}
		return cnt;
	}
	
	/**
	 * Returns the list of shapes on a given plane.
	 * 
	 * @param z The z-section.
	 * @param t The timepoint.
	 * @return See above.
	 */
	public List<ShapeData> getShapes(int z, int t)
	{
		return roiShapes.get(new ROICoordinate(z, t));
	}
	
	/**
	* Returns the iterator of the collection of the map.
	* 
	* @return See above.
	*/
	public Iterator<List<ShapeData>> getIterator()
	{
		return roiShapes.values().iterator();
	}
	
	/** 
	* Return the first plane that the ROI starts on.
	* 
	* @return See above.
	*/
	public ROICoordinate firstPlane()
	{
		return roiShapes.firstKey();
	}
	
	/** 
	* Returns the last plane that the ROI ends on.
	* 
	* @return See above.
	*/
	public ROICoordinate lastPlane()
	{
		return roiShapes.lastKey();
	}
	
	/**
	 * Returns an iterator of the Shapes in the ROI in the range [start, end].
	 * 
	 * @param start The starting plane where the Shapes should reside.
	 * @param end The final plane where the Shapes should reside.
	 * @return See above.
	 */
	public Iterator<List<ShapeData>> getShapesInRange(ROICoordinate start, 
			ROICoordinate end)
	{
		return roiShapes.subMap(start, end).values().iterator();
	}
	
	/**
	 * Returns <code>true</code> if the object a client-side object,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isClientSide() { return clientSide; }
	
	/**
	 * Sets the flag indicating if the object is a client-side object or not.
	 * 
	 * @param clientSide Passed <code>true</code> if it is a client-side object,
	 *                   <code>false</code> otherwise.
	 */
	public void setClientSide(boolean clientSide)
	{
		this.clientSide = clientSide;
	}
	
	/**
	 * Returns the description.
	 * 
	 * @return See above.
	 */
	public String getDescription()
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return "";
		RString value = roi.getDescription();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Sets the description of the roi.
	 * 
	 * @param description The value to set.
	 */
	public void setDescription(String description)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return;
		if (description == null) description = "";
		roi.setDescription(rtypes.rstring(description));
	}
	
	/**
	 * Sets the name of the roi.
	 * 
	 * @param name The value to set.
	 */
	public void setName(String name)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return;
		if (name == null) name = "";
		roi.setDescription(rtypes.rstring(name));
	}
	
	/**
	 * Returns the name.
	 * 
	 * @return See above.
	 */
	public String getName()
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return "";
		RString value = roi.getName();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Sets the name space.
	 * 
	 * @param name The value to set.
	 */
	public void setNamespace(String name)
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return;
		if (name == null) name = "";
		roi.setNamespace(rtypes.rstring(name));
	}
	
	/**
	 * Returns the namespace.
	 * 
	 * @return See above.
	 */
	public String getNamespace()
	{
		Roi roi = (Roi) asIObject();
		if (roi == null) return "";
		RString value = roi.getNamespace();
		if (value == null) return "";
		return value.getValue();
	}
	
}
