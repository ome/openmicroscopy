/*
 * pojos.ROIData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.model.Ellipse;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Shape;

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
	
	/** Initializes the map. */
	private void initialize()
	{
		roiShapes = new TreeMap<ROICoordinate, List<ShapeData>>
						(new ROICoordinate());
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
		if (roi != null) initialize();
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
	
	
}
