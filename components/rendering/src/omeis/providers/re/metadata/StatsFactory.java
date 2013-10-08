/*
 * omeis.providers.re.metadata.StatsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.metadata;

// Java imports

// Third-party libraries

// Application-internal dependencies
import java.awt.Dimension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ome.io.nio.PixelBuffer;
import ome.io.nio.TileLoopIteration;
import ome.io.nio.Utils;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.stats.StatsInfo;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.RegionDef;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Computes two types of statistics: The PixelsStats and the location stats. The
 * location stats determine the location of the pixels' values in order to set
 * the inputWindow and the noiseReduction flag. This flag will then be used when
 * we map the pixels intensity values onto the device space.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date: 2005/06/20
 *          14:11:46 $) </small>
 * @since OME2.2
 */
public class StatsFactory {

    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(StatsFactory.class);

	/** The minimum range. */
	private static final int RANGE_RGB = 255;
	
	/** The number of bins. */
    private static final int NB_BIN = 2 * QuantumStrategy.DECILE;

    /** The default size of a bin. */
    private static final int BIN = 2;

    /** The error factor. */
    private static final int EPSILON = 4;

    /** The threshold value. */
    private static final double THRESHOLD = 0.99;

    /** The noise reduction threshold value. */
    private static final double NR_THRESHOLD = 0.95;

    /** The location statistics. */
    private double[] locationStats;

    /** Boolean flag to determine the mapping algorithm. */
    private boolean noiseReduction;

    /** Value determined according to the location of the pixels' value. */
    private double inputStart;

    /** Value determined according to the location of the pixels' value. */
    private double inputEnd;

    /** The size of the bin.*/
    private double sizeBin;
    
    /** The epsilon value.*/
    private double epsilon;
    
    /**
     * For the specified {@link Plane2D}, computes the bins, determines the
     * inputWindow and the noiseReduction flag.
     * 
     * @param p2D 	 The selected plane2d.
     * @param gMin The global minimum.
     * @param sizeX2 The size along one axis.
     * @param sizeX1 The size along the other axis.
     */
    private void computeBins(Plane2D p2D, double gMin, int sizeX2,
            int sizeX1) {
    	int[] totals = new int[NB_BIN];
        /*
         * Segment[] segments = new Segment[NB_BIN]; for (int i = 0; i < NB_BIN;
         * i++) { segments[i] = new Segment( gMin + i * sizeBin, 0, gMin + (i +
         * 1) * sizeBin, 0); }
         */
        BasicSegment[] segments = new BasicSegment[NB_BIN];
        for (int i = 0; i < NB_BIN; i++) {
            segments[i] = new BasicSegment(gMin + i * sizeBin, gMin + (i + 1)
                    * sizeBin);
        }

        // check segment [o,e[
        double v;
        BasicSegment segment;
        if (p2D.isXYPlanar()) {
            // modified code
            int size = sizeX1 * sizeX2;
            for (int j = 0; j < size; j++) {
                v = p2D.getPixelValue(j);
                for (int i = 0; i < segments.length; i++) {
                    segment = segments[i];
                    if (v >= segment.x1 && v < segment.x2) {
                        totals[i]++;
                        break;
                    }
                } // end i
            }
        } else {
            for (int x2 = 0; x2 < sizeX2; ++x2) {
                for (int x1 = 0; x1 < sizeX1; ++x1) {
                    v = p2D.getPixelValue(x1, x2);
                    for (int i = 0; i < segments.length; i++) {
                        segment = segments[i];
                        if (v >= segment.x1 && v < segment.x2) {
                            totals[i]++;
                            break;
                        }
                        /*
                         * if (!segments[i].equals(1, pointX1, pointX2) &&
                         * segments[i].lies(pointX1, pointX2)) { totals[i]++;
                         * break; }
                         */
                    } // end i
                } // end x1
            }// end x2
        }

        double total = sizeX2 * sizeX1;
        for (int i = 0; i < totals.length; i++) {
            locationStats[i] += totals[i] / total;
        }
        // Default, we assume that we have at least 3 sub-intervals.
        /*
        inputStart = segments[0].x2;// segments[0].getPoint(1).x1;
        inputEnd = segments[NB_BIN - 1].x2;// segments[NB_BIN -
                                            // 1].getPoint(1).x1;
        total = total - totals[0] - totals[NB_BIN - 1];
        if (totals[0] >= totals[NB_BIN - 1]) {
            inputEnd = accumulateCloseToMin(totals, segments, total, epsilon);
        } else {
            inputStart = accumulateCloseToMax(totals, segments, total, epsilon);
        }
        
        */
        double s = segments[0].x2;
        double end = segments[NB_BIN - 1].x2;
        total = total - totals[0] - totals[NB_BIN - 1];
        if (totals[0] >= totals[NB_BIN - 1]) {
            end = accumulateCloseToMin(totals, segments, total, epsilon);
        } else {
            s = accumulateCloseToMax(totals, segments, total, epsilon);
        }
        if (s < inputStart) inputStart = s;
        if (end > inputEnd) inputEnd = end;
        noiseReduction = noiseReduction();
    }

    /** Determines the value of the noiseReduction flag. */
    private boolean noiseReduction() {
        double sumMin = 0, sumMax = 0;
        for (int i = 0; i < locationStats.length; i++) {
            if (i < BIN) {
                sumMin += locationStats[i];
            }
            if (i >= locationStats.length - BIN) {
                sumMax += locationStats[i];
            }
        }
        if (sumMin >= NR_THRESHOLD || sumMax >= NR_THRESHOLD) {
           return false;
        }
        return true;
    }

    /**
     * Determines the value of inputEnd when the pixels' values accumulated
     * closed to the minimum.
     * 
     * @param totals The accumulated values.
     * @param segments The segments to analyze.
     * @param total The total value.
     * @param epsilon The error value.
     */
    private double accumulateCloseToMin(int[] totals, BasicSegment[] segments,
            double total, double epsilon) {
        double e = segments[NB_BIN - 1].x2, sum = 0;
        for (int i = 1; i < totals.length - 1; i++) {
            sum += totals[i];
            if (sum / total > THRESHOLD) {
                e = segments[i].x1 + epsilon;
                break;
            }
        }
        return e;
    }

    /**
     * Determines the value of inputStart when the pixels' values accumulated
     * closed to the max.
     * 
     * @param totals The accumulated values.
     * @param segments The segments to analyze.
     * @param total The total value.
     * @param epsilon The error value.
     */
    private double accumulateCloseToMax(int[] totals, BasicSegment[] segments,
            double total, double epsilon) {
        double s = segments[0].x2, sum = 0;
        for (int i = totals.length - 2; i > 0; i--) {
            sum += totals[i];
            if (sum / total > THRESHOLD) {
                s = segments[i].x2 - epsilon;
                break;
            }
        }
        return s;
    }


    /** 
     * Determines the minimum and maximum corresponding to the passed
     * pixels type.
     * 
     * @param type The pixels type to handle.
     */
    public double[] initPixelsRange(PixelsType type)
    {
    	double[] minmax = new double[2];
    	minmax[0] = 0;
    	minmax[1] = 1;
    	if (type == null) return minmax;
    	String typeAsString = type.getValue();
    	if (PlaneFactory.INT8.equals(typeAsString)) {
    		minmax[0] = -128;
    		minmax[1] = 127;
    	} else if (PlaneFactory.UINT8.equals(typeAsString)) {
    		minmax[0] = 0;
    		minmax[1] = 255;
    	} else if (PlaneFactory.INT16.equals(typeAsString)) {
    		minmax[0] = -32768;
    		minmax[1] = 32767;
    	} else if (PlaneFactory.UINT16.equals(typeAsString)) {
    		minmax[0] = 0;
    		minmax[1] = 65535;
    	} else if (PlaneFactory.INT32.equals(typeAsString)) {
    		minmax[0] = -32768;
			minmax[1] = 32767;
    	} else if (PlaneFactory.UINT32.equals(typeAsString)) {
    		minmax[0] = 0;
			minmax[1] = 65535;
    	} else if (PlaneFactory.FLOAT_TYPE.equals(typeAsString) ||
    			PlaneFactory.DOUBLE_TYPE.equals(typeAsString)) {
    		//b/c we don't know if it is signed or not
    		minmax[0] = 0;
			minmax[1] = 32767;
    	}
    	return minmax;
    }
    
    /**
     * Helper object to determine the location of the pixels' values, the
     * inputWindow i.e. <code>inputStart</code> and <code>inputEnd</code>
     * and to initialize the <code>noiseReduction</code> flag.
     * 
     * @param metadata The pixels to parse.
     * @param pixelsData The buffer.
     * @param pd The plane to handle.
     * @param index The channel index.
     * @throws PixMetadataException
     */
    public void computeLocationStats(final Pixels metadata,
            final PixelBuffer pixelsData, final PlaneDef pd, final int index) {
        log.debug("Computing location stats for Pixels:" + metadata.getId());
        Channel channel = metadata.getChannel(index);
        final StatsInfo stats = channel.getStatsInfo();
        /*
        if (stats == null)
        {
        	throw new ResourceError("Pixels set is missing statistics for " +
        			"channel '" + index + "'. This suggests an image import " +
        			"error or failed image import.");
        }
        */
        double gMin = 0;
        double gMax = 1;
        if (stats == null) {
        	double[] values = initPixelsRange(metadata.getPixelsType());
        	gMin = values[0];
        	gMax = values[1];
        } else {
        	gMin = stats.getGlobalMin().doubleValue();
            gMax = stats.getGlobalMax().doubleValue();
        }
        
        Dimension tileSize = pixelsData.getTileSize();
        double range = gMax-gMin;
        int resolutionLevels = pixelsData.getResolutionLevels();
        if (range <= RANGE_RGB || resolutionLevels > 1) {
            // A number of resolution levels greater than one tends to
            // signify a big image.  We *really* do not want to be
            // iterating through potentially GBs of data on every big image
            // whenever a calculation of the location statistics is requested.
            //
            // Furthermore, as this computation is exclusively used to
            // prime "pretty good image" rendering settings calculating
            // them is pointless when the range does not exceed that
            // available in the device space.
            inputEnd = gMax;
            inputStart = gMin;
            return;
        }
        sizeBin = range / NB_BIN;
        epsilon = sizeBin / EPSILON;
        if (locationStats == null) {
            locationStats = new double[NB_BIN];
        }
        //value will be reset when calculating data.
        inputEnd = gMax;
        inputStart = gMin;
        final double globalMin = gMin;
        Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int tileWidth,
                    int tileHeight, int tileCount)
            {
                if (z == 1 || c == 1 || t == 1)
                {
                    // We're not going through the entire pixel buffer
                    return;
                }
                RegionDef regionDef = new RegionDef();
                regionDef.setX(x);
                regionDef.setY(y);
                regionDef.setWidth(tileWidth);
                regionDef.setHeight(tileHeight);
                pd.setRegion(regionDef);
                Plane2D plane2D = PlaneFactory.createPlane(pd, index, metadata,
                        pixelsData);
                computeBins(plane2D, globalMin, tileHeight, tileWidth);
            }
        }, pixelsData, (int) tileSize.getWidth(), (int) tileSize.getHeight());
    }

    /**
     * Returns the statistics.
     * 
     * @return See above.
     */
    public double[] getLocationStats() {
        return locationStats;
    }

    /**
     * Returns <code>true</code> if the flag is on, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isNoiseReduction() {
        return noiseReduction;
    }

    /**
     * Returns the input start.
     * 
     * @return See above.
     */
    public double getInputStart() {
        return inputStart;
    }

    /**
     * Returns the input end.
     * 
     * @return See above.
     */
    public double getInputEnd() {
        return inputEnd;
    }

    // inner class
    class BasicSegment {

        /** Left bound of the segment. */
        double x1;

        /** Right bound of the segment. */
        double x2;

        /**
         * Creates a new instance.
         * 
         * @param x1
         *            The left bound of the segment.
         * @param x2
         *            The right bound of the segment.
         */
        BasicSegment(double x1, double x2) {
            if (x2 < x1) {
                throw new IllegalArgumentException("Segment not valid.");
            }
            this.x2 = x2;
            this.x1 = x1;
        }

    }

}
