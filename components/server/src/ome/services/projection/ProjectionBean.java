/**
 *
 *   Copyright 2008-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.projection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import ome.annotations.RolesAllowed;
import ome.api.IPixels;
import ome.api.IProjection;
import ome.api.ServiceInterface;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.util.PixelData;
import ome.io.nio.PixelsService;
import ome.logic.AbstractLevel2Service;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.stats.StatsInfo;

/**
 * Implements projection functionality for Pixels sets as declared in {@link
 * IProjection}.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta3.1
 */
@Transactional(readOnly = true)
public class ProjectionBean extends AbstractLevel2Service implements IProjection
{

    /** The logger for this class. */
    private static Logger log = LoggerFactory.getLogger(ProjectionBean.class);

    /** Reference to the service used to retrieve the pixels metadata. */
    protected transient IPixels iPixels;

    /** Reference to the service used to retrieve the pixels data. */
    protected transient PixelsService pixelsService;

    /**
     * Returns the interface this implementation is for.
     * @see AbstractLevel2Service#getServiceInterface()
     */
    public Class<? extends ServiceInterface> getServiceInterface()
    {
        return IProjection.class;
    }

    /**
     * IPixels bean injector. For use during configuration. Can only be called 
     * once.
     */
    public void setIPixels(IPixels iPixels)
    {
        getBeanHelper().throwIfAlreadySet(this.iPixels, iPixels);
        this.iPixels = iPixels;
    }

    /**
     * PixelsService bean injector. For use during configuration. Can only be 
     * called once.
     */
    public void setPixelsService(PixelsService pixelsService)
    {
        getBeanHelper().throwIfAlreadySet(this.pixelsService, pixelsService);
        this.pixelsService = pixelsService;
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#projectStack(long, ome.model.enums.PixelsType, int, int, int, int, int, int, int)
     */
    @RolesAllowed("user")
    public byte[] projectPlanes(long pixelsId, PixelsType pixelsType,
                               int algorithm, int axis, int plane, int channelIndex,
                               int stepping, int start, int end)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#projectStack(long, ome.model.enums.PixelsType, int, int, int, int, int, int)
     */
    @RolesAllowed("user")
    public byte[] projectStack(long pixelsId, PixelsType pixelsType,
                               int algorithm, int timepoint, int channelIndex, 
                               int stepping, int start, int end)
    {
        ProjectionContext ctx = new ProjectionContext();
        ctx.pixels = iQuery.get(Pixels.class, pixelsId);
        PixelBuffer pixelBuffer = pixelsService.getPixelBuffer(
                ctx.pixels, false);
        intervalBoundsCheck(start, end, ctx.pixels.getSizeZ());
        outOfBoundsStepping(stepping);
        outOfBoundsCheck(channelIndex, "channel");
        outOfBoundsCheck(timepoint, "timepoint");
        Integer v = ctx.pixels.getSizeT();
        if (timepoint >= v)
            throw new ValidationException("timepoint must be <"+v);
        v = ctx.pixels.getSizeC();
        if (channelIndex >= v)
            throw new ValidationException("channel index must be <"+v);
        try
        {

            if (pixelsType == null)
            {
                pixelsType = ctx.pixels.getPixelsType();
            }
            else
            {
                pixelsType = iQuery.get(PixelsType.class, pixelsType.getId());
            }

            ctx.planeSizeInPixels =
                ctx.pixels.getSizeX() * ctx.pixels.getSizeY();
            int planeSize = 
                ctx.planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
            byte[] buf = new byte[planeSize];
            ctx.from = pixelBuffer.getStack(channelIndex, timepoint);
            ctx.to = new PixelData(pixelsType.getValue(), ByteBuffer.wrap(buf));

            switch (algorithm)
            {
                case IProjection.MAXIMUM_INTENSITY:
                {
                    projectStackMax(ctx, stepping, start, end, false);
                    break;
                }
                case IProjection.MEAN_INTENSITY:
                {
                    projectStackMean(ctx, stepping, start, end, false);
                    break;
                }
                case IProjection.SUM_INTENSITY:
                {
                    projectStackSum(ctx, stepping, start, end, false);
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException(
                            "Unknown algorithm: " + algorithm);
                }
            }
            return buf;
        }
        catch (IOException e)
        {
            String error = String.format(
                    "I/O error retrieving stack C=%d T=%d: %s",
                    channelIndex, timepoint, e.getMessage());
            log.error(error, e);
            throw new ResourceError(error);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            String error = String.format(
                    "C=%d or T=%d out of range for Pixels Id %d: %s",
                    channelIndex, timepoint, ctx.pixels.getId(), e.getMessage());
            log.error(error, e);
            throw new ValidationException(error);
        }
        finally
        {
            try
            {
                pixelBuffer.close();
            }
            catch (IOException e)
            {
                log.error("Buffer did not close successfully.", e);
                throw new ResourceError(
                        e.getMessage() + " Please check server log.");
            }
            if (ctx.from != null) {
                ctx.from.dispose();
            }
        }
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#project(long, ome.model.enums.PixelsType, int, int, int, int, java.util.List, int, int, int, java.lang.String)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long project(long pixelsId, PixelsType pixelsType,
                              int algorithm, int axis, int planeStart, int planeEnd,
                              List<Integer> channels, int stepping,
                              int start, int end, String name)
    {
        switch (axis) {
            case IProjection.Z_AXIS:
            default:
                return projectPixelsAlongZ(pixelsId, pixelsType, algorithm, planeStart,
                        planeEnd, channels, stepping, start, end, name);
            case IProjection.T_AXIS:
                return projectPixelsAlongT(pixelsId, pixelsType, algorithm, planeStart,
                        planeEnd, channels, stepping, start, end, name);
        }
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#projectPixels(long, ome.model.enums.PixelsType, int, int, int, java.util.List, int, int, int, java.lang.String)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long projectPixels(long pixelsId, PixelsType pixelsType,
                              int algorithm, int tStart, int tEnd,
                              List<Integer> channels, int stepping,
                              int zStart, int zEnd, String name)
    {
        return project(pixelsId, pixelsType, algorithm, IProjection.Z_AXIS,
                tStart, tEnd, channels, stepping, zStart, zEnd, name);
    }

    /**
     * Ensures that a particular dimension value is not out of range (e.g. less
     * than zero).
     * @param value The value to check.
     * @param name The name of the value to be used for error reporting.
     * @throws ValidationException If <code>value</code> is out of range.
     */
    private void outOfBoundsCheck(Integer value, String name)
    {
        if (value != null && value < 0)
        {
            throw new ValidationException(name + ": " + value + " < 0");
        }
    }

    /**
     * Ensures that a particular dimension value is not out of range.
     * @param value The value to check.
     * @throws ValidationException If <code>value</code> is out of range.
     */
    private void outOfBoundsStepping(Integer value)
    {
        if (value != null && value <= 0)
        {
            throw new ValidationException("stepping: " + value + " <= 0");
        }
    }

    /**
     * Ensures that a particular dimension value is not out of range (e.g. less
     * than zero).
     * @param start The lower bound of the interval.
     * @param end The upper bound of the interval.
     * @param max The maximum value
     * @throws ValidationException If <code>value</code> is out of range.
     */
    private void intervalBoundsCheck(int start, int end, Integer max)
    {
        if (start < 0 || end < 0)
            throw new ValidationException("interval value cannot be negative.");
        if (start >= max || end >= max)
            throw new ValidationException("Interval value cannot be >= "+max);
    }

    /**
     * Projects a stack based on the maximum intensity at each XY coordinate.
     * @param ctx The context of our projection.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMax(ProjectionContext ctx, int stepping,
                                 int start, int end, boolean doMinMax)
    {
        int currentPlaneStart;
        double projectedValue, stackValue;
        double minimum = ctx.minimum;
        double maximum = ctx.maximum;
        for (int i = 0; i < ctx.planeSizeInPixels; i++)
        {
            projectedValue = 0;
            for (int v = start; v <= end; v += stepping)
            {
                currentPlaneStart = ctx.planeSizeInPixels * v;
                stackValue = ctx.from.getPixelValue(currentPlaneStart + i);
                if (stackValue > projectedValue)
                {
                    projectedValue = stackValue;
                }
            }
            ctx.to.setPixelValue(i, projectedValue);
            if (doMinMax)
            {
                minimum = projectedValue < minimum? projectedValue : minimum;
                maximum = projectedValue > maximum? projectedValue : maximum;
            }
        }
        ctx.minimum = minimum;
        ctx.maximum = maximum;
    }

    /**
     * Projects a stack based on the mean intensity at each XY coordinate.
     * @param from The raw pixel data from the stack to project from.
     * @param ctx The context of our projection.
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMean(ProjectionContext ctx, int stepping, 
                                  int start, int end, boolean doMinMax)
    {
        projectStackMeanOrSum(ctx, stepping, start, end, true, doMinMax);
    }

    /**
     * Projects a stack based on the sum intensity at each XY coordinate.
     * @param ctx The context of our projection.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackSum(ProjectionContext ctx, int stepping, 
                                 int start, int end, boolean doMinMax)
    {
        projectStackMeanOrSum(ctx, stepping, start, end, false, doMinMax);
    }

    /**
     * Projects a stack based on the sum intensity at each XY coordinate with
     * the option to also average the sum intensity.
     * @param ctx The context of our projection.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param mean Whether or not we're performing an average post sum
     * intensity projection.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMeanOrSum(ProjectionContext ctx, int stepping,
                                       int start, int end, 
                                       boolean mean, boolean doMinMax)
    {
        double planeMaximum = ctx.to.getMaximum();

        int currentPlaneStart;
        double projectedValue, stackValue;
        double minimum = ctx.minimum;
        double maximum = ctx.maximum;
        for (int i = 0; i < ctx.planeSizeInPixels; i++)
        {
            projectedValue = 0;
            int projectedPlaneCount = 0;
            for (int v = start; v < end; v += stepping)
            {
                currentPlaneStart = ctx.planeSizeInPixels * v;
                stackValue = ctx.from.getPixelValue(currentPlaneStart + i);
                projectedValue += stackValue;
                projectedPlaneCount++;
            }
            if (mean)
            {
                projectedValue = projectedValue / projectedPlaneCount;
            }
            if (projectedValue > planeMaximum)
            {
                projectedValue = planeMaximum;
            }
            ctx.to.setPixelValue(i, projectedValue);
            if (doMinMax)
            {
                minimum = projectedValue < minimum? projectedValue : minimum;
                maximum = projectedValue > maximum? projectedValue : maximum;
            }
        }
        ctx.minimum = minimum;
        ctx.maximum = maximum;
    }

    /**
     * Stores the context of a projection operation.
     * 
     * Class is static to prevent any instances from holding onto
     * {@link ProjectionBean} instances.
     *
     * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
     * @since OMERO-Beta3.1
     */
    private static class ProjectionContext
    {
        /** The Pixels set we're currently working on. */
        public Pixels pixels;

        /** Count of the number of pixels per plane for <code>pixels</code>. */
        public int planeSizeInPixels;

        /** Current minimum for the projected pixel data. */
        public double minimum = Double.MAX_VALUE;

        /** Current maximum for the projected pixel data. */
        public double maximum = Double.MIN_VALUE;

        /** The raw pixel data from the stack to project from. */
        public PixelData from;

        /** The raw pixel data buffer to project into. */
        public PixelData to;
    }

    /**
     * Performs a projection through selected optical sections and optical
     * sections for a given set of time points of a Pixels set.
     * @param pixelsId The source Pixels set Id.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param algorithm the selected algorithm
     * @param tStart Timepoint to start projecting from.
     * @param tEnd Timepoint to finish projecting.
     * @param channels List of the channel indexes to use while calculating the
     * projection.
     * @param stepping Stepping value to use while calculating the projection.
     * @param zStart Optical section to start projecting from.
     * @param zEnd Optical section to finish projecting.
     * @param name Name for the newly created image. If <code>null</code> the
     * name of the Image linked to the Pixels qualified by pixelsId will be used
     * with a "Projection" suffix.
     * @return The Id of the newly created Image which has been projected.
     */
    private long projectPixelsAlongZ(long pixelsId, PixelsType pixelsType,
                              int algorithm, int tStart, int tEnd,
                              List<Integer> channels, int stepping,
                              int zStart, int zEnd, String name)
    {
        // First, copy and resize our image with sizeZ = 1.
        ProjectionContext ctx = new ProjectionContext();
        ctx.pixels = iQuery.get(Pixels.class, pixelsId);
        Image image = ctx.pixels.getImage();
        name = name == null? image.getName() + " Projection" : name;
        //size of the new buffer.
        //Add control for z
        intervalBoundsCheck(zStart, zEnd, ctx.pixels.getSizeZ());
        outOfBoundsStepping(stepping);

        Integer sizeT = tEnd-tStart+1;
        if (tStart > tEnd)
            sizeT = tStart-tEnd+1;
        if (sizeT <= 0) sizeT = null;

        //Channels and timepoint validation done there
        long newImageId =
            iPixels.copyAndResizeImage(image.getId(), null, null, 1, sizeT,
                                       channels, name, false);
        Image newImage = iQuery.get(Image.class, newImageId);
        Pixels newPixels = newImage.getPixels(0);
        if (pixelsType == null)
        {
            pixelsType = ctx.pixels.getPixelsType();
        }
        else
        {
            pixelsType = iQuery.get(PixelsType.class, pixelsType.getId());
        }
        newPixels.setPixelsType(pixelsType);

        // Project each stack for each channel and each timepoint in the
        // entire image, copying into the pixel buffer the projected pixels.
        PixelBuffer sourceBuffer = pixelsService.getPixelBuffer(
                ctx.pixels, false);
        try {
            PixelBuffer destinationBuffer = pixelsService.getPixelBuffer(
                    newPixels, true);
            try
            {
                ctx.planeSizeInPixels = ctx.pixels.getSizeX() * ctx.pixels.getSizeY();
                int planeSize =
                    ctx.planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
                byte[] buf = new byte[planeSize];
                ctx.to = new PixelData(pixelsType.getValue(), ByteBuffer.wrap(buf));
                int newC = 0;
                for (Integer c : channels)
                {
                    ctx.minimum = Double.MAX_VALUE;
                    ctx.maximum = Double.MIN_VALUE;
                    for (int t = tStart; t <= tEnd; t++)
                    {
                        try
                        {
                            ctx.from = sourceBuffer.getStack(c, t);
                            switch (algorithm)
                            {
                                case IProjection.MAXIMUM_INTENSITY:
                                {
                                    projectStackMax(ctx, stepping, zStart, zEnd, true);
                                    break;
                                }
                                case IProjection.MEAN_INTENSITY:
                                {
                                    projectStackMean(ctx, stepping, zStart, zEnd, true);
                                    break;
                                }
                                case IProjection.SUM_INTENSITY:
                                {
                                    projectStackSum(ctx, stepping, zStart, zEnd, true);
                                    break;
                                }
                                default:
                                {
                                    throw new IllegalArgumentException(
                                            "Unknown algorithm: " + algorithm);
                                }
                            }
                            destinationBuffer.setPlane(buf, 0, newC, t);
                        }
                        catch (IOException e)
                        {
                            String error = String.format(
                                    "I/O error retrieving stack C=%d T=%d: %s",
                                    c, t, e.getMessage());
                            log.error(error, e);
                            throw new ResourceError(error);
                        }
                        catch (DimensionsOutOfBoundsException e)
                        {
                            String error = String.format(
                                    "C=%d or T=%d out of range for Pixels Id %d: %s",
                                    c, t, ctx.pixels.getId(), e.getMessage());
                            log.error(error, e);
                            throw new ValidationException(error);
                        } finally {
                            if (ctx.from != null) {
                                ctx.from.dispose();
                            }
                        }
                    }
                    // Handle the change of minimum and maximum for this channel.
                    Channel channel = newPixels.getChannel(newC);
                    StatsInfo si = new StatsInfo();
                    si.setGlobalMin(ctx.minimum);
                    si.setGlobalMax(ctx.maximum);
                    channel.setStatsInfo(si);
                    // Set our methodology
                    newPixels.setMethodology(
                            IProjection.METHODOLOGY_STRINGS[algorithm]);
                    newC++;
                }
            }
            finally
            {
                try
                {
                    destinationBuffer.close();
                }
                catch (IOException e)
                {
                    log.error("Buffer did not close successfully: " + destinationBuffer , e);
                    throw new ResourceError(
                            e.getMessage() + " Please check server log.");
                }
            }
        } finally {
            try
            {
                sourceBuffer.close();
            }
            catch (IOException e)
            {
                log.error("Buffer did not close successfully: " + sourceBuffer, e);
                throw new ResourceError(
                        e.getMessage() + " Please check server log.");
            }
        }
        newImage = iUpdate.saveAndReturnObject(newImage);
        return newImage.getId();
    }

    /**
     * Performs a projection through selected optical sections and optical
     * sections for a given set of time points of a Pixels set.
     * @param pixelsId The source Pixels set Id.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param algorithm the selected algorithm
     * @param planeStart The Optical section to start projecting from.
     * @param planeEnd The Optical section to finish projecting.
     * @param channels List of the channel indexes to use while calculating the
     * projection.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start The timepoint section to start projecting from.
     * @param end The timepoint to finish projecting.
     * @param name Name for the newly created image. If <code>null</code> the
     * name of the Image linked to the Pixels qualified by pixelsId will be used
     * with a "Projection" suffix.
     * @return The Id of the newly created Image which has been projected.
     */
    private long projectPixelsAlongT(long pixelsId, PixelsType pixelsType,
            int algorithm, int planeStart, int planeEnd,
            List<Integer> channels, int stepping, int start, int end,
            String name)
    {
        // First, copy and resize our image with sizeT = 1.
        ProjectionContext ctx = new ProjectionContext();
        ctx.pixels = iQuery.get(Pixels.class, pixelsId);
        Image image = ctx.pixels.getImage();
        name = name == null? image.getName() + " Projection" : name;
        //size of the new buffer.
        //Add control for z
        intervalBoundsCheck(start, end, ctx.pixels.getSizeT());
        outOfBoundsStepping(stepping);

        Integer sizeZ = planeEnd-planeStart+1;
        if (planeStart > planeEnd)
            sizeZ = planeStart-planeEnd+1;
        if (sizeZ <= 0) sizeZ = null;

        //Channels and timepoint validation done there
        long newImageId =
            iPixels.copyAndResizeImage(image.getId(), null, null, sizeZ, 1,
                                       channels, name, false);
        Image newImage = iQuery.get(Image.class, newImageId);
        Pixels newPixels = newImage.getPixels(0);
        if (pixelsType == null)
        {
            pixelsType = ctx.pixels.getPixelsType();
        }
        else
        {
            pixelsType = iQuery.get(PixelsType.class, pixelsType.getId());
        }
        newPixels.setPixelsType(pixelsType);

        // Project each timepoint for each channel and each z in the
        // entire image, copying into the pixel buffer the projected pixels.
        PixelBuffer sourceBuffer = pixelsService.getPixelBuffer(
                ctx.pixels, false);
        try {
            PixelBuffer destinationBuffer = pixelsService.getPixelBuffer(
                    newPixels, true);
            try
            {
                Integer sizeX = ctx.pixels.getSizeX();
                Integer sizeY = ctx.pixels.getSizeY();
                ctx.planeSizeInPixels = sizeX * sizeY;
                int planeSize =
                    ctx.planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
                byte[] buf = new byte[planeSize];
                ctx.to = new PixelData(pixelsType.getValue(), ByteBuffer.wrap(buf));
                int newC = 0;
                List<Integer> step = Arrays.asList(new Integer[]{1,1,1,1,1});
                Integer sizeT = end-start;
                List<Integer> size = Arrays.asList(new Integer[]{sizeX,sizeY, 1, 1, sizeT});
                for (Integer c : channels)
                {
                    ctx.minimum = Double.MAX_VALUE;
                    ctx.maximum = Double.MIN_VALUE;
                    for (int z = planeStart; z <= planeEnd; z++)
                    {
                        try
                        {
                            List<Integer> offset = Arrays.asList(new Integer[]{0, 0, z, c, 0});
                            ctx.from = sourceBuffer.getHypercube(offset, size, step);
                            switch (algorithm)
                            {
                                case IProjection.MAXIMUM_INTENSITY:
                                {
                                    projectStackMax(ctx, stepping, start, end, true);
                                    break;
                                }
                                case IProjection.MEAN_INTENSITY:
                                {
                                    projectStackMean(ctx, stepping, start, end, true);
                                    break;
                                }
                                case IProjection.SUM_INTENSITY:
                                {
                                    projectStackSum(ctx, stepping, start, end, true);
                                    break;
                                }
                                default:
                                {
                                    throw new IllegalArgumentException(
                                            "Unknown algorithm: " + algorithm);
                                }
                            }
                            destinationBuffer.setPlane(buf, z, newC, 0);
                        }
                        catch (IOException e)
                        {
                            String error = String.format(
                                    "I/O error retrieving stack Z=%d C=%d: %s",
                                    z, c, e.getMessage());
                            log.error(error, e);
                            throw new ResourceError(error);
                        }
                        catch (DimensionsOutOfBoundsException e)
                        {
                            String error = String.format(
                                    "Z=%d or C=%d out of range for Pixels Id %d: %s",
                                    z, c, ctx.pixels.getId(), e.getMessage());
                            log.error(error, e);
                            throw new ValidationException(error);
                        } finally {
                            if (ctx.from != null) {
                                ctx.from.dispose();
                            }
                        }
                    }
                    // Handle the change of minimum and maximum for this channel.
                    Channel channel = newPixels.getChannel(newC);
                    StatsInfo si = new StatsInfo();
                    si.setGlobalMin(ctx.minimum);
                    si.setGlobalMax(ctx.maximum);
                    channel.setStatsInfo(si);
                    // Set our methodology
                    newPixels.setMethodology(
                            IProjection.METHODOLOGY_STRINGS[algorithm]);
                    newC++;
                }
            }
            finally
            {
                try
                {
                    destinationBuffer.close();
                }
                catch (IOException e)
                {
                    log.error("Buffer did not close successfully: " + destinationBuffer , e);
                    throw new ResourceError(
                            e.getMessage() + " Please check server log.");
                }
            }
        } finally {
            try
            {
                sourceBuffer.close();
            }
            catch (IOException e)
            {
                log.error("Buffer did not close successfully: " + sourceBuffer, e);
                throw new ResourceError(
                        e.getMessage() + " Please check server log.");
            }
        }
        newImage = iUpdate.saveAndReturnObject(newImage);
        return newImage.getId();
    }

}
