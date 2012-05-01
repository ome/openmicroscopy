/*
 * omeis.providers.re.metadata.StatsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.metadata;

import java.awt.Dimension;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.io.nio.TileLoopIteration;
import ome.io.nio.Utils;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.StatsInfo;
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
     * @param stats  The statistics for the selected channel.
     * @param sizeX2 The size along one axis.
     * @param sizeX1 The size along the other axis.
     */
    private void computeBins(Plane2D p2D, StatsInfo stats, int sizeX2,
            int sizeX1) {
    	double gMin = stats.getGlobalMin().doubleValue();
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
     * Returns a Channel model object based on its indexes within the
     * OMERO data model.
     * @param pixels The pixels to handle.
     * @param channelIndex channel index.
     * @return See above.
     */
    private Channel getChannel(Pixels pixels, int channelIndex)
    {
    	if (pixels == null) return null;
    	if (channelIndex >= pixels.sizeOfChannels()) return null;
    	Iterator<Channel> i = pixels.iterateChannels();
    	int index = 0;
    	Channel channel;
    	while (i.hasNext()) {
    		channel = i.next();
    		if (index == channelIndex) return channel;
			index++;
		}
    	return null;
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
        //TODO: Retrieve channel.
        Channel channel = getChannel(metadata, index);
        if (channel == null)
        {
        	throw new ResourceError("Channels index not valid: "+index);
        }
        final StatsInfo stats = channel.getStatsInfo();
        if (stats == null)
        {
        	throw new ResourceError("Pixels set is missing statistics for " +
        			"channel '" + index + "'. This suggests an image import " +
        			"error or failed image import.");
        }
        double gMin = stats.getGlobalMin().doubleValue();
        double gMax = stats.getGlobalMax().doubleValue();
        Dimension tileSize = pixelsData.getTileSize();
        double range = gMax-gMin;
        if (range <= RANGE_RGB) {
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
        inputStart = gMax;
        inputEnd = gMin;
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
                computeBins(plane2D, stats, tileHeight, tileWidth);
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
