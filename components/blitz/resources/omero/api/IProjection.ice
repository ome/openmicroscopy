/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IPROJECTION_ICE
#define OMERO_API_IPROJECTION_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>
#include <omero/Constants.ice>


module omero {

    module api {

        /**
         * Provides methods for performing projections of Pixels sets.
         **/
        ["ami", "amd"] interface IProjection extends ServiceInterface
            {
                /**
                 * Performs a projection through the optical sections of a
                 * particular wavelength at a given time point of a Pixels set.
                 * @param pixelsId The source Pixels set Id.
                 * @param pixelsType The destination Pixels type. If
                 *                   <code>null</code>, the source Pixels set
                 *                   pixels type will be used.
                 * @param algorithm <code>MAXIMUM_INTENSITY</code>,
                 *                  <code>MEAN_INTENSITY</code> or
                 *                  <code>SUM_INTENSITY</code>. <b>NOTE:</b>
                 *                  When performing a
                 *                  <code>SUM_INTENSITY</code> projection,
                 *                  pixel values will be <i>pinned</i> to the
                 *                  maximum pixel value of the destination
                 *                  Pixels type.
                 * @param timepoint Timepoint to perform the projection.
                 * @param channelIndex Index of the channel to perform the
                 *                     projection.
                 * @param stepping Stepping value to use while calculating the
                 *                 projection.
                 *                 For example, <code>stepping=1</code> will
                 *                 use every optical section from
                 *                 <code>start</code> to <code>end</code> where
                 *                 <code>stepping=2</code> will use every
                 *                 other section from <code>start</code> to
                 *                 <code>end</code> to perform the projection.
                 * @param start Optical section to start projecting from.
                 * @param end Optical section to finish projecting.
                 * @return A byte array of projected pixel values whose length
                 *         is equal to the Pixels set
                 8         <code>sizeX * sizeY * bytesPerPixel</code> in
                 *         big-endian format.
                 * @throws ValidationException Where:
                 * <ul>
                 *   <li><code>algorithm</code> is unknown</li>
                 *   <li><code>timepoint</code> is out of range</li>
                 *   <li><code>channelIndex</code> is out of range</li>
                 *   <li><code>start</code> is out of range</li>
                 *   <li><code>end</code> is out of range</li>
                 *   <li><code>start > end</code></li>
                 *   <li>the Pixels set qualified by <code>pixelsId</code> is
                 *       unlocatable.</li>
                 * </ul>
                 * @see #projectPixels
                 **/
                Ice::ByteSeq projectStack(long pixelsId,
                                          omero::model::PixelsType pixelsType,
                                          omero::constants::projection::ProjectionType algorithm,
                                          int timepoint, int channelIndex, int stepping,
                                          int start, int end) throws ServerError;

                /**
                 * Performs a projection through selected optical sections and
                 * optical sections for a given set of time points of a Pixels
                 * set. The Image which is linked to the Pixels set will be
                 * copied using
                 * {@link omero.api.IPixels#copyAndResizeImage}.
                 *
                 * @param pixelsId The source Pixels set Id.
                 * @param pixelsType The destination Pixels type. If
                 *                   <code>null</code>, the source Pixels set
                 *                   pixels type will be used.
                 * @param algorithm <code>MAXIMUM_INTENSITY</code>,
                 *                  <code>MEAN_INTENSITY</code> or
                 *                  <code>SUM_INTENSITY</code>. <b>NOTE:</b>
                 *                  When performing a
                 *                  <code>SUM_INTENSITY</code> projection,
                 *                  pixel values will be <i>pinned</i> to the
                 *                  maximum pixel value of the destination
                 *                  Pixels type.
                 * @param tStart Timepoint to start projecting from.
                 * @param tEnd Timepoint to finish projecting.
                 * @param channels List of the channel indexes to use while
                 *                 calculating the projection.
                 * @param stepping Stepping value to use while calculating the
                 *                 projection. For example,
                 *                 <code>stepping=1</code> will use every
                 *                 optical section from <code>start</code> to
                 *                 <code>end</code> where
                 *                 <code>stepping=2</code> will use every
                 *                 other section from <code>start</code> to
                 *                 <code>end</code> to perform the projection.
                 * @param zStart Optical section to start projecting from.
                 * @param zEnd Optical section to finish projecting.
                 * @param name Name for the newly created image. If
                 *             <code>null</code> the name of the Image linked
                 *             to the Pixels qualified by
                 *             <code>pixelsId</code> will be used with a
                 *             "Projection" suffix. For example,
                 *             <i>GFP-H2B Image of HeLa Cells</i> will have an
                 *             Image name of
                 *             <i>GFP-H2B Image of HeLa Cells Projection</i>
                 *             used for the projection.
                 * @return The Id of the newly created Image which has been
                 *         projected.
                 * @throws ValidationException Where:
                 * <ul>
                 *   <li><code>algorithm</code> is unknown</li>
                 *   <li><code>tStart</code> is out of range</li>
                 *   <li><code>tEnd</code> is out of range</li>
                 *   <li><code>tStart > tEnd</code></li>
                 *   <li><code>channels</code> is null or has indexes out of
                 *       range</li>
                 *   <li><code>zStart</code> is out of range</li>
                 *   <li><code>zEnd</code> is out of range</li>
                 *   <li><code>zStart > zEnd</code></li>
                 *   <li>the Pixels set qualified by <code>pixelsId</code> is
                 *       unlocatable.</li>
                 * </ul>
                 * @see #projectStack
                 **/
                long projectPixels(long pixelsId, omero::model::PixelsType pixelsType,
                                   omero::constants::projection::ProjectionType algorithm,
                                   int tStart, int tEnd,
                                   omero::sys::IntList channelList, int stepping,
                                   int zStart, int zEnd, string name)
                    throws ServerError;
            };

    };
};

#endif
