/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;
import ome.model.enums.PixelsType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @since Beta4.1
 */
public class BfPixelBuffer implements PixelBuffer, Serializable {

    private final static Log log = LogFactory.getLog(BfPixelBuffer.class);

    private final BfPixelsWrapper reader;

    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object?
     * There should ultimately be some sort of check here that the
     * file is in a/the repository.
     */
    public BfPixelBuffer(String path) throws IOException, FormatException {
        reader = new BfPixelsWrapper(path);
    }

    public byte[] calculateMessageDigest() throws IOException {
        return reader.getMessageDigest();
    }

    public void checkBounds(Integer x, Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        reader.checkBounds(x, y, z, c, t);
    }

    public void close() throws IOException {
        reader.close();
    }

    public int getByteWidth() {
        return reader.getByteWidth();
    }

    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        PixelData d;
        byte[] buffer = new byte[reader.getColSize()];
        reader.getCol(x,z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        try {
            reader.getCol(x,z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Integer getColSize() {
        return reader.getColSize();
    }

    public long getId() {
        return reader.getId();
    }

    public String getPath() {
        return reader.getPath();
    }

    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        PixelData d;
        byte[] buffer = new byte[reader.getPlaneSize()];
        reader.getPlane(z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            reader.getPlane(z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader.getPlaneOffset(z,c,t);
    }

    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException {
        throw new UnsupportedOperationException(
                "Not yet supported, raise ticket to implement if required");
    }

    public Integer getPlaneSize() {
        return reader.getPlaneSize();
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
        PixelData d;
        byte[] buffer = new byte[reader.getRowSize()];
        reader.getRow(y,z,c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        try {
            reader.getRow(y,z,c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader.getRowOffset(y,z,c,t);
    }

    public Integer getRowSize() {
        return reader.getRowSize();
    }

    public int getSizeC() {
        return reader.getSizeC();
    }

    public int getSizeT() {
        return reader.getSizeT();
    }

    public int getSizeX() {
        return reader.getSizeX();
    }

    public int getSizeY() {
        return reader.getSizeY();
    }

    public int getSizeZ() {
        return reader.getSizeZ();
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        PixelData d;
        byte[] buffer = new byte[reader.getColSize()];
        reader.getStack(c,t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            reader.getStack(c,t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        return reader.getStackOffset(c,t);
    }

    public Integer getStackSize() {
        return reader.getStackSize();
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        PixelData d;
        byte[] buffer = new byte[reader.getTimepointSize()];
        reader.getTimepoint(t,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
            reader.getTimepoint(t,buffer);
            reader.swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        return reader.getTimepointOffset(t);
    }

    public Integer getTimepointSize() {
        return reader.getTimepointSize();
    }

    public Integer getTotalSize() {
        return reader.getTotalSize();
    }

    public boolean isFloat() {
        return reader.isFloat();
    }

    public boolean isSigned() {
        return reader.isSigned();
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

    public PixelData getHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException
    {
        PixelData d;
        byte[] buffer = new byte[reader.getCubeSize(offset,size,step)];
        reader.getHypercube(offset,size,step,buffer);
        d = new PixelData(reader.getPixelsType(), ByteBuffer.wrap(buffer));
        return d;
    }

    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        try {
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
}