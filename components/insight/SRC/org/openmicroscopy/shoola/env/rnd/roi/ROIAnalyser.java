/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIAnalyser 
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
package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports
import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * Does some basic statistic analysis on a collection of {@link ROIShape} 
 * which all refer to the same pixels set.
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
public class ROIAnalyser
{

	 /** 
     * Iterates an {@link ROIShape} over our pixels set.
     * Observers compute the stats as the iteration moves forward. 
     */
	private PointIterator 		runner;
	
	/** The number of z-sections. */
	private int					sizeZ;   
	
	/** The number of timepoints. */
	private int					sizeT;   
	
	/** The number of channels. */
	private int					sizeC;   
    
	/**
	 * Controls if the specified coordinates are valid.
	 * 
	 * @param z The z coordinate. Must be in the range <code>[0, sizeZ)</code>.
	 * @param t The t coordinate. Must be in the range <code>[0, sizeT)</code>.
	 */
	private void checkPlane(int z, int t)
	{
		if (z < 0 || sizeZ <= z) 
            throw new IllegalArgumentException(
                    "z out of range [0, "+sizeZ+"): "+z+".");
		if (t < 0 || sizeT <= t) 
            throw new IllegalArgumentException(
                    "t out of range [0, "+sizeT+"): "+t+".");
	}
	
	/**
	 * Controls if the specified channel is valid. 
	 * 
	 * @param w The w coordinate. Must be in the range <code>[0, sizeW)</code>.
	 */
	private void checkChannel(int w)
	{
		 if (w < 0 || sizeC <= w) 
	            throw new IllegalArgumentException(
	                    "w out of range [0, "+sizeC+"): "+w+".");
	}
	
    /**
     * Creates a new instance to analyze the pixels set accessible through
     * <code>source</code>.
     * 
     * @param source	Gateway to the raw data of the pixels set this iterator
     *                  will work on. Mustn't be <code>null</code>.
     * @param sizeZ     The number of z-sections.
     * @param sizeT     The number of timepoints.
     * @param sizeC     The number of channels.
     * @param sizeX     The number of pixels along the x-axis.
     * @param sizeY     The number of pixels along the y-axis.
     */
	public ROIAnalyser(DataSink source, int sizeZ, int sizeT, int sizeC, int
						sizeX, int sizeY)
	{
		//Constructor will check source and dims.
        runner = new PointIterator(source, sizeZ, sizeT, sizeC, sizeX, sizeY);
        this.sizeZ = sizeZ;
        this.sizeT = sizeT;
        this.sizeC = sizeC;
	}
	
	/**
     * Computes an {@link ROIShapeStats} object for each {@link ROIShape} 
     * specified
     * 
     * @param shapes 	The shapes to analyse.
     * @param channels	Collection of selected channels.
     * @return 	A map whose keys are the {@link ROIShape} objects specified 
     * 			and whose values are a map (keys: channel index, value 
     * 			the corresponding {@link ROIShapeStats} objects computed by 
     * 			this method).
     * @throws DataSourceException  If an error occurs while retrieving plane
     *                              data from the pixels source.
     */
    public Map analyze(ROIShape[] shapes, List channels) 
        throws DataSourceException
    {
    	if (shapes == null) throw new NullPointerException("No shapes.");
    	if (shapes.length == 0) 
            throw new IllegalArgumentException("No shapes defined.");
    	if (channels == null || channels.size() == 0)
    		throw new IllegalArgumentException("No channels defined.");
        Map<ROIShape, Map> r = new HashMap<ROIShape, Map>();
        ROIShapeStats computer;
        Map<Integer, ROIShapeStats> stats;
        Iterator j;
        int n = channels.size();
        Integer w;
        ROIShape shape;
        for (int i = 0; i < shapes.length; ++i) {
        	shape = shapes[i];
        	checkPlane(shape.getZ(), shape.getT());
        	stats = new HashMap<Integer, ROIShapeStats>(n);
        	j = channels.iterator();
        	List<Point> points = shape.getFigure().getPoints();
        	while (j.hasNext()) {
				w = (Integer) j.next();
				checkChannel(w.intValue());
				computer =  new ROIShapeStats();
				runner.register(computer);
				runner.iterate(shape, points, w.intValue());
				runner.remove(computer);
				stats.put(w, computer);
			}
            r.put(shape, stats);
        }
        return r;
    }
    
}
