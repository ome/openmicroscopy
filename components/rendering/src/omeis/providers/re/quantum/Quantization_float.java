/*
 * omeis.providers.re.quantum.Quantization_32_bit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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

package omeis.providers.re.quantum;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ome.model.core.Pixels;
import ome.model.display.QuantumDef;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Quantization process. In charge of building a look-up table for each active
 * wavelength. The mapping process is done in three mapping steps, for some
 * computer reasons, we cannot compose (in the mathematical sense) the three
 * maps directly. Each wavelength initializes a strategy, in order to preserve
 * the 5D-notion of OME image, we first compute the normalized parameters. We
 * determine a pseudo-decile (not decile in maths terms) interval and compute
 * the associated parameters to reduce the irrelevant values (noiseReduction).
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since OME5.1
 */
public class Quantization_float extends QuantumStrategy {

    /** The lowest pixel intensity value. */
    private int min;

    /** The uppest pixel intensity value. */
    private int max;

    /** The input start normalized value. */
    private double ysNormalized;

    /** The input end normalized value. */
    private double yeNormalized;

    /** The slope of the normalized map. */
    private double aNormalized;

    /** The lower bound of the decile interval. */
    private double Q1;

    /** The upper bound of the decile interval. */
    private double Q9;

    /**
     * The mapping parameters from the sub-interval of [Q1, Q9] to the device
     * space.
     */
    private double aDecile, bDecile;

    /**
     * The device space sub-interval. The values aren't the ones stored in
     * {@link QuantumDef} if the noise reduction flag is <code>true</code>.
     */
    private int cdStart, cdEnd;

    /** The mapped values.*/
    private LoadingCache<Double, Integer> values;

    /**
     * Initializes the coefficient of the normalize mapping operation.
     *
     * @param k
     *            The coefficient of the selected curve.
     */
    private void initNormalizedMap(double k) {
        ysNormalized = valueMapper.transform(MIN, k);
        yeNormalized = valueMapper.transform(MAX, k);
        aNormalized = qDef.getBitResolution().intValue()
                / (yeNormalized - ysNormalized);
    }

    /**
     * Initializes the parameter to map the pixels intensities to the device
     * space and returned the default initial depending on the value of the
     * noise reduction flag.
     *
     * @param dStart
     *            The input window start.
     * @param dEnd
     *            The input window end.
     * @return See above.
     */
    private double initDecileMap(double dStart, double dEnd) {
        cdStart = qDef.getCdStart().intValue();
        cdEnd = qDef.getCdEnd().intValue();
        double denum = dEnd - dStart, num = MAX;

        double v = 0, b = dStart;
        int e = 0;
        double startMin = min;
        double startMax = max;
        Q1 = min;
        Q9 = max;

        if (dStart <= startMin) {
            Q1 = dStart;
        }
        if (dEnd >= startMax) Q9 = dEnd;
        if (startMin == startMax) v = 1;
        double decile = (startMax - startMin) / DECILE;
        if (getNoiseReduction()) {
            Q1 += decile;
            Q9 -= decile;
            denum = Q9 - Q1;
            v = DECILE;
            e = DECILE;
            num = MAX - 2 * DECILE;
            b = Q1;
            if (dStart >= Q1 && dEnd > Q9) {
                denum = Q9 - dStart;
                b = dStart;
            } else if (dStart >= Q1 && dEnd <= Q9) {
                denum = dEnd - dStart;
                b = dStart;
            } else if (dStart < Q1 && dEnd <= Q9) {
                denum = dEnd - Q1;
            }
            if (cdStart < DECILE) {
                cdStart = DECILE;
            }
            if (cdEnd > MAX - DECILE) {
                cdEnd = MAX - DECILE;
            }
        }
        aDecile = num / denum;
        bDecile = aDecile * b - e;

        return v;
    }

    /** The input window size changed, re-map the values. */
    @Override
    protected void onWindowChange() {
        values.invalidateAll();
    }

    /**
     * Creates a new strategy.
     *
     * @param qd
     *            Quantum definition object, contained mapping data.
     * @param pixels
     *            The pixels
     */
    public Quantization_float(QuantumDef qd, Pixels pixels) {
        super(qd, pixels);
        values = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<Double, Integer>() {
                    public Integer load(Double key) throws Exception {
                        return _quantize(key);
                    }
                });
    }

    /**
     * Maps the value.
     *
     * @param value The value to handle.
     * @return The mapped value.
     * @throws QuantizationException Thrown if an error occurred during
     *                               the mapping.
     */
    private int _quantize(double value)
                throws QuantizationException
    {
        double dStart = getWindowStart(), dEnd = getWindowEnd();
        double k = getCurveCoefficient();
        double a1 = (qDef.getCdEnd().intValue() - qDef.getCdStart().intValue())
                / qDef.getBitResolution().doubleValue();

        // Initializes the normalized map.
        initNormalizedMap(k);
        // Initializes the decile map.
        double v = initDecileMap(dStart, dEnd);
        QuantumMap normalize = new PolynomialMap();

        if (value > Q1) {
            if (value <= Q9) {
                v = aDecile * normalize.transform(value, 1) - bDecile;
            } else {
                v = cdEnd;
            }
        } else {
            v = cdStart;
        }

        v = aNormalized * (valueMapper.transform(v, k) - ysNormalized);
        v = Math.round(v);
        v = Math.round(a1 * v + cdStart);
        return ((byte) v) & 0xFF;
    }

    /**
     * Implemented as specified in {@link QuantumStrategy}.
     *
     * @see QuantumStrategy#quantize(double)
     */
    @Override
    public int quantize(double value) throws QuantizationException {
        try {
            return values.get(getMiddleRange(value));
        } catch (ExecutionException e) {
            throw new QuantizationException(e);
        }
    }

}
