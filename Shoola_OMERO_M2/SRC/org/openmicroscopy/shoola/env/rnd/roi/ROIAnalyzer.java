/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIAnalyzer
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

package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.image.roi.ROI5D;

/** 
 * Does some basic statistic analysis on a set of {@link ROI5D} which all
 * refer to the same pixels set.
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
public class ROIAnalyzer
{

    /** 
     * Iterates an {@link ROI5D} over our pixels set.
     * Observers compute the stats as the iteration moves forward. 
     */
    private PointIterator       runner;
    
    /** The ROIs to analyze. */
    private ROI5D[]             rois;
    
    /** The dimensions of the pixels set. */
    private PixelsDimensions    dims;
    
    
    /**
     * Creates a new instance to analyze the pixels set accessible through
     * <code>source</code>.
     * 
     * @param rois     The ROIs to analyze.  Mustn't be <code>null</code> nor
     *                  contain <code>null</code> elements and must have size
     *                  <code>1</code> at least.
     * @param source   Gateway to the raw data of the pixels set this iterator
     *                  will work on.  Mustn't be <code>null</code>.
     * @param dims     The dimensions of the pixels set.  Mustn't be 
     *                  <code>null</code>.
     */
    public ROIAnalyzer(ROI5D[] rois, DataSink source, PixelsDimensions dims)
    {
        //Fail-fast checks.  Failing in the middle of an analysis is generally
        //very expensive.  (Think of rois[0]!=null and rois[1]==null and a
        //200 timepoints image; assume rois[0] defines at least one 2D selection
        //per timepoint...)
        if (rois == null) throw new NullPointerException("No rois.");
        if (rois.length == 0) 
            throw new IllegalArgumentException("No ROI defined.");
        for (int i = 0; i < rois.length; ++i) 
            if (rois[i] == null)
                throw new NullPointerException("No ROI at: "+i+".");   
        this.rois = rois;
        
        //Constructor will check source and dims.
        runner = new PointIterator(source, dims);
        this.dims = dims;
    }
    
    /**
     * Computes an {@link ROIStats} object for each {@link ROI5D} that was
     * specified at creation time.
     * 
     * @return A map whose keys are the {@link ROI5D} objects specified at
     *          creation time and whose values are the corresponding 
     *          {@link ROIStats} objects computed by this method.
     * @throws DataSourceException  If an error occurs while retrieving plane
     *                              data from the pixels source.
     */
    public Map analyze() 
        throws DataSourceException
    {
        Map results = new HashMap();
        ROIStats computer;
        for (int i = 0; i < rois.length; ++i) {
            computer = new ROIStats(dims);
            runner.register(computer);
            runner.iterate(rois[i]);
            runner.remove(computer);
            results.put(rois[i], computer);
        }
        return results;
    }
    
}
