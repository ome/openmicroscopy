/*
 * Created on May 22, 2005
 */
package org.openmicroscopy.omero.tests.client;

import java.util.Random;

/**
 * @author josh
 */
public class OMEPerformanceData extends OMEData {

    // Main field
    double percent = 0.001;

    public OMEPerformanceData() {
    }

    public OMEPerformanceData(double percent) {
        this.percent = percent;
    }

    public OMEPerformanceData(double percent, long seed) {
        this.percent = percent;
        this.seed = seed;
        this.rnd = new Random(seed);
    }

    public void init() {
        // Test data : calculated before to not change times.
        if (!initialized) {
            super.init();
            imgsPDI = getPercentOfCollection(allImgs, percent);
            imgsCGCI = getPercentOfCollection(allImgs, percent);
            imgsAnn1 = getPercentOfCollection(allImgs, percent);
            imgsAnn2 = getPercentOfCollection(allImgs, percent);
            dsAnn1 = getPercentOfCollection(allDss, percent);
            dsAnn2 = getPercentOfCollection(allDss, percent);
        }
    }
}
