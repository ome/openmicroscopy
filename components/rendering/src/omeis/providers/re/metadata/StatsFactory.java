/*
 * omeis.providers.re.metadata.StatsFactory
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

package omeis.providers.re.metadata;


//Java imports


//Third-party libraries

//Application-internal dependencies
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.util.math.geom2D.PlanePoint;
import ome.util.math.geom2D.Segment;
import omeis.io.StackStatistics;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantumStrategy;
import tmp.Helper;

/** 
 * Computes two types of statistics: The PixelsStats and the location stats.
 * The location stats determine the location of the pixels' values in order 
 * to set the inputWindow and the noiseReduction flag.
 * This flag will then be used when we map the pixels intensity values onto 
 * the device space. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.5 $ $Date: 2005/06/20 14:11:46 $)
 * </small>
 * @since OME2.2
 */
public class StatsFactory
{

    private static final int    NB_BIN = 2*QuantumStrategy.DECILE;
    
    private static final int    BIN = 2;
    
    /** The error factor. */
    private static final int    EPSILON = 4;
    
    private static final double THRESHOLD = 0.99;
    
    private static final double NR_THRESHOLD = 0.95;
    
    
    private double[]        locationStats;
    
    /** Boolean flag to determine the mapping algorithm. */
    private boolean         noiseReduction;
    
    /** Value determined according to the location of the pixels' value. */
    private double          inputStart;
    
    /** Value determined according to the location of the pixels' value. */
    private double          inputEnd;
    
    
    /**
     * Helper object to extract relevant statistic from the more general
     * purpose stats stored in <i>OME</i>.
     * 
     * @param s The <i>OME</i> stats.
     * @param h The header in order to get the dimensions of the pixels set.
     */
    private PixelsStats computePixelsStats(StackStatistics s, 
                                                Pixels h)
    {
        double globalMin = 0;
        double globalMax = 1;
        double min, max;
        int c_size = h.getSizeC().intValue(), t_size = h.getSizeT().intValue();
        PixelsStats pixelsStats = new PixelsStats(c_size,t_size);
        for (int c = 0; c < c_size; c++) {
            for (int t = 0; t < t_size; t++) {
                min = s.min[c][t];
                max = s.max[c][t];
                if (t == 0) {
                    globalMin = min;
                    globalMax = max;
                } else {
                    globalMin = Math.min(globalMin, min);
                    globalMax = Math.max(globalMax, max);
                }
                pixelsStats.setEntry(c, t, min, max); 
            }
            pixelsStats.setGlobalEntry(c, globalMin, globalMax);
        }
        return pixelsStats;
    }

    /**
     * For the specified {@link Plane2D}, computes the bins, determines 
     * the inputWindow and the noiseReduction flag.
     * 
     * @param p2D       The selected plane2d.
     * @param pixTStats The stats of for the selected channel.
     * @param sizeX2    
     * @param sizeX1
     */
    private void computeBins(Plane2D p2D, PixTStatsEntry pixTStats, 
                            int sizeX2, int sizeX1)
    {
        double sizeBin = (pixTStats.globalMax-pixTStats.globalMin)/NB_BIN;
        double epsilon = sizeBin/EPSILON;
        int[] totals = new int[NB_BIN];
        locationStats = new double[NB_BIN];
        PlanePoint o, e;
        Segment[] segments = new Segment[NB_BIN];
        for (int i = 0; i < NB_BIN; i++) {
            o = new PlanePoint(pixTStats.globalMin+i*sizeBin, 0);
            e = new PlanePoint(pixTStats.globalMin+(i+1)*sizeBin, 0);
            segments[i] = new Segment(o, e);
        }
        PlanePoint point;
        //check segment [o,e[
        for (int x2 = 0; x2 < sizeX2; ++x2) {
            for (int x1 = 0; x1 < sizeX1; ++x1) {
                point = new PlanePoint(p2D.getPixelValue(x1, x2), 0);
                for (int i = 0; i < segments.length; i++) {
                    if (segments[i].lies(point) && 
                            !segments[i].getPoint(1).equals(point)) {
                        totals[i]++;
                        break;
                    }
                } // end i
            } //end x1
        }// end x2
        
        double total = sizeX2*sizeX1;
        for (int i = 0; i < totals.length; i++) 
            locationStats[i] = totals[i]/total;
        //Default, we assume that we have at least 3 sub-intervals.
        inputStart = segments[0].getPoint(1).x1;
        inputEnd = segments[NB_BIN-1].getPoint(1).x1;;
        total = total-totals[0]-totals[NB_BIN-1];
        if (totals[0] >= totals[NB_BIN-1])
            inputEnd = accumulateCloseToMin(totals, segments, total, epsilon);
        else
            inputStart = accumulateCloseToMax(totals, segments, total, epsilon);
        noiseReduction = noiseReduction();
    }
    
    /** Determines the value of the noiseReduction flag. */
    private boolean noiseReduction()
    {
        double sumMin = 0, sumMax = 0;
        for (int i = 0; i < locationStats.length; i++) {
            if (i < BIN) sumMin += locationStats[i];
            if (i >= locationStats.length-BIN) sumMax += locationStats[i];
        }
        boolean nr = true;
        if (sumMin >= NR_THRESHOLD || sumMax >= NR_THRESHOLD) nr = false;
        return nr;
    }
    
    /** 
     * Determines the value of inputEnd when the pixels' values accumulated 
     * closed to the min.
     */
    private double accumulateCloseToMin(int[] totals, Segment[] segments, 
                                        double total, double epsilon)
    {
        double e = segments[NB_BIN-1].getPoint(1).x1, sum = 0;
        for (int i = 1; i < totals.length-1; i++) {
            sum += totals[i];
            if (sum/total > THRESHOLD) {
                e = segments[i].getPoint(1).x1+epsilon;
                break;
            }
        }
        return e;
    }
    
    /** 
     * Determines the value of inputStart when the pixels' values accumulated 
     * closed to the max.
     */
    private double accumulateCloseToMax(int[] totals, Segment[] segments, 
                                    double total, double epsilon)
    {
        double s = segments[0].getPoint(1).x1, sum = 0;
        for (int i = totals.length-2; i > 0; i--) {
            sum += totals[i];
            if (sum/total > THRESHOLD) {
                s = segments[i].getPoint(1).x1-epsilon;
                break;
            }
        }
        return s;
    }
    
    /**
     * Computes the two sets of statistics, determines the inputWindow and the 
     * noiseReduction.
     * 
     */
    public PixelsStats compute(Pixels metadata, PixelBuffer buffer)
    {
            StackStatistics stackStats = null; // FIXME repFile.getStackStatistics();
            return computePixelsStats(stackStats, metadata);
    }
    

    /**
     * Helper object to determine the location of the pixels' values, the 
     * inputWindow i.e. <code>inputStart</code> and <code>inputEnd</code>
     * and to initialize the <code>noiseReduction</code> flag.
     * 
     * @param header
     * @param pixelsData
     * @param pixelsStats
     * @param pd
     * @param index
     * @throws PixMetadataException
     */
    public void computeLocationStats(Pixels header, PixelBuffer pixelsData, 
                            PixelsStats pixelsStats, PlaneDef pd, int index)
        throws PixMetadataException
    {
        try {
            PixTStatsEntry pixTStats = pixelsStats.getGlobalEntry(index);
            Plane2D plane2D = Helper.createPlane(pd, index, header, pixelsData);
            if (pixTStats.globalMax-pixTStats.globalMin >= NB_BIN) 
                computeBins(plane2D, pixTStats, header.getSizeY().intValue(), header.getSizeX().intValue());
        } catch (Exception ioe) {
            throw new PixMetadataException("Cannot retrieve the " +
                    "Plane2D to compute the locationStats.", ioe);
        }
    }
    
    public double[] getLocationStats() { return locationStats; }

    public boolean isNoiseReduction() { return noiseReduction; }
    
    public double getInputStart() { return inputStart; }
    
    public double getInputEnd() { return inputEnd; }
    
}
