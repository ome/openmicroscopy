/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.bioformats;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import loci.formats.CoreMetadata;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import ome.conditions.ResourceError;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.RomioPixelBuffer;
import ome.util.PixelData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PixelBuffer} implementation which uses Bio-Formats to
 * read pixels data directly from original files.
 *
 * @since Beta4.1
 */
public class BfPixelBuffer implements PixelBuffer, Serializable {

    private final static Logger log = LoggerFactory.getLogger(BfPixelBuffer.class);

    protected final String filePath;

    protected final IFormatReader bfReader;

    protected final AtomicReference<BfPixelsWrapper> reader = new AtomicReference<BfPixelsWrapper>();

    private int seriesIndex = 0;

    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object?
     * There should ultimately be some sort of check here that the
     * file is in a/the repository.
     */
    public BfPixelBuffer(String filePath, IFormatReader bfReader) throws IOException, FormatException {
        this.filePath = filePath;
        this.bfReader = bfReader;
    }

    protected BfPixelsWrapper reader() {
        BfPixelsWrapper wrapper = reader.get();
        if (wrapper == null) {
            try {
                // Note: the call to bfReader.setid inside the BfPixelsWrapper
                // ctor should be a no-op since the filePath is the same for
                // both calls.
                if (reader.compareAndSet(null, new BfPixelsWrapper(filePath, bfReader))) {
                    wrapper = reader.get();
                }
            } catch (FormatException fe) {
                log.debug("FormatException: " + filePath, fe);
                throw new ResourceError("FormatException: " + filePath + "\n" + fe.getMessage());
            } catch (Exception e) {
                log.error("Failed to instantiate BfPixelsWrapper with " + filePath);
                throw new RuntimeException(e);
            }
            // Ensure that we're using the highest resolution level (100%) by
            // default.
            setSeries(seriesIndex);
            setResolutionLevel(getResolutionLevels() - 1);
        }
        return wrapper;
    }

    /**
     * Delegates to {@link IFormatReader#isLittleEndian()}.
     * @return See above.
     */
    public boolean isLittleEndian()
    {
        // Ensure the reader has been initialized
        reader();
        return bfReader.isLittleEndian();
    }

    /**
     * Sets the current series in the underlying Bio-Formats reader.
     * @param series The series to set.
     */
    public void setSeries(int series)
    {
        // Ensure the reader has been initialized
        reader();
        bfReader.setSeries(series);
        seriesIndex = series;
    }

    /**
     * Retrieves the current series of the underlying Bio-Formats reader.
     * @return The series.
     */
    public int getSeries()
    {
        // Ensure the reader has been initialized
        reader();
        return bfReader.getSeries();
    }

    public byte[] calculateMessageDigest() throws IOException {
        return reader().getMessageDigest();
    }

    public void checkBounds(Integer x, Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        reader().checkBounds(x, y, z, c, t);
    }

    public void close() throws IOException {
        reader().close();
        reader.set(null);
    }

    public int getByteWidth() {
        return reader().getByteWidth();
    }

    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        final BfPixelsWrapper reader = reader();
        PixelData d;
        byte[] buffer = new byte[reader.getColSize()];
        reader.getCol(x,z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        try {
            final BfPixelsWrapper reader = reader();
            reader.getCol(x,z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Integer getColSize() {
        return reader().getColSize();
    }

    public long getId() {
        return reader().getId();
    }

    public String getPath() {
        return reader().getPath();
    }

    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        final BfPixelsWrapper reader = reader();
        PixelData d;
        int size = RomioPixelBuffer.safeLongToInteger(reader.getPlaneSize());
        byte[] buffer = new byte[size];
        reader.getPlane(z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            final BfPixelsWrapper reader = reader();
            reader.getPlane(z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader().getPlaneOffset(z,c,t);
    }

    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException {
        throw new UnsupportedOperationException(
                "Not yet supported, raise ticket to implement if required");
    }

    public Long getPlaneSize() {
        return reader().getPlaneSize();
    }

    public PixelData getRegion(Integer size, Long offset) throws IOException {
        throw new UnsupportedOperationException(
                "Not yet supported, raise ticket to implement if required");
    }

    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
            throws IOException {
        throw new UnsupportedOperationException(
                "Not yet supported, raise ticket to implement if required");
    }

    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        final BfPixelsWrapper reader = reader();
        PixelData d;
        byte[] buffer = new byte[reader.getRowSize()];
        reader.getRow(y,z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        try {
            final BfPixelsWrapper reader = reader();
            reader.getRow(y,z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader().getRowOffset(y,z,c,t);
    }

    public Integer getRowSize() {
        return reader().getRowSize();
    }

    public int getSizeC() {
        return reader().getSizeC();
    }

    public int getSizeT() {
        return reader().getSizeT();
    }

    public int getSizeX() {
        return reader().getSizeX();
    }

    public int getSizeY() {
        return reader().getSizeY();
    }

    public int getSizeZ() {
        return reader().getSizeZ();
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        final BfPixelsWrapper reader = reader();
        PixelData d;

        int size = RomioPixelBuffer.safeLongToInteger(reader.getStackSize());
        byte[] buffer = new byte[size];
        reader.getStack(c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            final BfPixelsWrapper reader = reader();
            reader.getStack(c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader().getStackOffset(c,t);
    }

    public Long getStackSize() {
        return reader().getStackSize();
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        final BfPixelsWrapper reader = reader();
        PixelData d;
        int size = RomioPixelBuffer.safeLongToInteger(
                reader.getTimepointSize());
        byte[] buffer = new byte[size];
        reader.getTimepoint(t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            BfPixelsWrapper reader = reader();
            reader.getTimepoint(t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        return reader().getTimepointOffset(t);
    }

    public Long getTimepointSize() {
        return reader().getTimepointSize();
    }

    public Long getTotalSize() {
        return reader().getTotalSize();
    }

    public boolean isFloat() {
        return reader().isFloat();
    }

    public boolean isSigned() {
        return reader().isSigned();
    }

    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");

    }

    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");

    }

    public Long getHypercubeSize(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws DimensionsOutOfBoundsException
    {
        final BfPixelsWrapper reader = reader();
        return reader.getHypercubeSize(offset,size,step);
    }

    public PixelData getHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException
    {
        final BfPixelsWrapper reader = reader();
        PixelData d;
        int hypercubeSize = RomioPixelBuffer.safeLongToInteger(
                getHypercubeSize(offset,size,step));
        byte[] buffer = new byte[hypercubeSize];
        reader.getHypercube(offset,size,step,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        d.setOrder(isLittleEndian()?
                ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return d;
    }

    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            final BfPixelsWrapper reader = reader();
            reader.getHypercube(offset,size,step,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public PixelData getPlaneRegion(Integer x, Integer y, Integer width,
            Integer height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException
            {
        List<Integer> offset = Arrays.asList(new Integer[]{x,y,z,c,t});
        List<Integer> size = Arrays.asList(new Integer[]{width,height,1,1,1});
        List<Integer> step = Arrays.asList(new Integer[]{stride+1,stride+1,1,1,1});
        return getHypercube(offset, size, step);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        final BfPixelsWrapper reader = reader();
        byte[] buffer = new byte[
                w * h * FormatTools.getBytesPerPixel(reader.getPixelsType())];
        try {
            // Call getTile on reader() rather than on this
            // so as not to swap the bytes twice.
            reader().getTile(z, c, t, x, y, w, h, buffer);
            PixelData d = new PixelData(
                    reader.getPixelsType(), ByteBuffer.wrap(buffer));
            d.setOrder(isLittleEndian()?
                    ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            return d;
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        try
        {
            reader().getTile(z, c, t, x, y, w, h, buffer);
            reader().swapIfRequired(buffer);
            return buffer;
        }
        catch (FormatException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevel()
     */
    public int getResolutionLevel()
    {
        // Ensure the reader has been initialized
        reader();
        // The highest resolution level (100%) is actually the first series
        return Math.abs(
                bfReader.getResolution() - (getResolutionLevels() - 1));
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevels()
     */
    public int getResolutionLevels()
    {
        // Ensure the reader has been initialized
        reader();
        return bfReader.getResolutionCount();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileSize()
     */
    public Dimension getTileSize()
    {
        // Ensure the reader has been initialized
        reader();
        return new Dimension(bfReader.getOptimalTileWidth(),
                             bfReader.getOptimalTileHeight());
    }

    public List<List<Integer>> getResolutionDescriptions()
    {
        final List<List<Integer>> rv = new ArrayList<List<Integer>>();
        final int no = bfReader.getResolutionCount();
        final List<CoreMetadata> cms = bfReader.getCoreMetadataList();
        for (int i = 0; i < no; i++)
        {
            int coreIndex = bfReader.seriesToCoreIndex(bfReader.getSeries()) + i;
            CoreMetadata cm = cms.get(coreIndex);
            List<Integer> sizes = Arrays.asList(cm.sizeX, cm.sizeY);
            rv.add(sizes);
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     */
    public void setResolutionLevel(int resolutionLevel)
    {
        // Ensure the reader has been initialized
        reader();
        // The highest resolution level (100%) is actually the first series
        bfReader.setResolution(Math.abs(
                resolutionLevel - (getResolutionLevels() - 1)));
    }

}
