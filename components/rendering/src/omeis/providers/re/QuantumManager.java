/*
 * omeis.providers.re.QuantumManager
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

import java.util.Iterator;
import java.util.List;

import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.stats.StatsInfo;

import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Manages the strategy objects for each wavelength.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
class QuantumManager {

    /** The pixels metadata. */
    private final Pixels metadata;

    /**
     * Contains a strategy object for each wavelength. Indexed according to the
     * wavelength indexes in the <i>OME</i> 5D pixels file.
     */
    private final QuantumStrategy[] wavesStg;

    /** A quantum factory instance for looking up enumerations. */
    private final QuantumFactory factory;

    /**
     * Creates a new instance.
     * 
     * @param metadata
     *            The pixels metadata.
     */
    QuantumManager(Pixels metadata, QuantumFactory factory) {
        this.factory = factory;
        this.metadata = metadata;
        wavesStg = new QuantumStrategy[metadata.getSizeC().intValue()];
    }

    /**
     * Creates and configures an appropriate strategy for each wavelength. The
     * previous window interval settings of each wavelength are retained by the
     * new strategy.
     * 
     * @param qd
     *            The quantum definition which dictates what strategy to use.
     * @param waves
     *            Rendering settings associated to each wavelength (channel).
     */
    void initStrategies(QuantumDef qd, List<ChannelBinding> waves) {
        ChannelBinding[] cb = waves.toArray(new ChannelBinding[waves.size()]);
        initStrategies(qd, cb);
    }

    /**
     * Creates and configures an appropriate strategy for each wavelength. The
     * previous window interval settings of each wavelength are retained by the
     * new strategy.
     * 
     * @param qd
     *            The quantum definition which dictates what strategy to use.
     * @param waves
     *            Rendering settings associated to each wavelength (channel).
     */
    void initStrategies(QuantumDef qd, ChannelBinding[] waves) {
        QuantumStrategy stg;
        double gMin, gMax;
        int w = 0;
        Channel channel;
        StatsFactory sf = new StatsFactory();
        double[] minmax;
        for (Iterator<Channel> i = metadata.iterateChannels(); i.hasNext();) {
            channel = i.next();
            stg = factory.getStrategy(qd, metadata);
            StatsInfo statsInfo = channel.getStatsInfo();
            if (statsInfo == null) {
                minmax = sf.initPixelsRange(metadata);
                gMin = minmax[0];
                gMax = minmax[1];
            } else {
            	gMin = statsInfo.getGlobalMin().doubleValue();
                gMax = statsInfo.getGlobalMax().doubleValue();
            }
            stg.setExtent(gMin, gMax);
            stg.setMapping(waves[w].getFamily(), waves[w].getCoefficient()
                            .doubleValue(), waves[w].getNoiseReduction()
                            .booleanValue());
            stg.setWindow(waves[w].getInputStart(), waves[w].getInputEnd());

            wavesStg[w] = stg;
            w++;
        }
    }

    /**
     * Retrieves the configured strategy for the specified wavelength.
     * 
     * @param w
     *            The wavelength index in the <i>OME</i> 5D-pixels file.
     * @return See above.
     */
    QuantumStrategy getStrategyFor(int w) {
        return wavesStg[w];
    }

}
