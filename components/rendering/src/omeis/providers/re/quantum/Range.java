/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeis.providers.re.quantum;

//Java imports

/**
 * Utility class. Temporary class until we update guava.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
class Range {

    private double upperBound;

    private double lowerBound;

    /**
     * Creates a new instance.
     *
     * @param lowerBound The lower bound of the interval.
     * @param upperBound The upper bound of the interval.
     */
    Range(double lowerBound, double upperBound)
    {
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    double upperEndpoint() { return upperBound; }

    double lowerEndpoint() { return lowerBound; }
}
