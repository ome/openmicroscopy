/*
 * org.openmicroscopy.is.StackStatistics
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.is;

public class StackStatistics
{
    // Each of these is a [c][t] array

    public double[][] minimum;
    public double[][] maximum;
    public double[][] mean;
    public double[][] sigma;
    public double[][] geometricMean;
    public double[][] geometricSigma;
    public double[][] centroidX;
    public double[][] centroidY;
    public double[][] centroidZ;
    public double[][] sumI;
    public double[][] sumI2;
    public double[][] sumLogI;
    public double[][] sumXI;
    public double[][] sumYI;
    public double[][] sumZI;

    public StackStatistics()
    {
        super();
    }

    public StackStatistics(final double[][] minimum,
                           final double[][] maximum,
                           final double[][] mean,
                           final double[][] sigma,
                           final double[][] geometricMean,
                           final double[][] geometricSigma,
                           final double[][] centroidX,
                           final double[][] centroidY,
                           final double[][] centroidZ,
                           final double[][] sumI,
                           final double[][] sumI2,
                           final double[][] sumLogI,
                           final double[][] sumXI,
                           final double[][] sumYI,
                           final double[][] sumZI)
    {
        this.minimum = minimum;
        this.maximum = maximum;
        this.mean = mean;
        this.sigma = sigma;
        this.geometricMean = geometricMean;
        this.geometricSigma = geometricSigma;
        this.centroidX = centroidX;
        this.centroidY = centroidY;
        this.centroidZ = centroidZ;
        this.sumI = sumI;
        this.sumI2 = sumI2;
        this.sumLogI = sumLogI;
        this.sumXI = sumXI;
        this.sumYI = sumYI;
        this.sumZI = sumZI;
    }
}