/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.bioformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import ome.io.nio.RomioPixelBuffer;
import ome.io.nio.DimensionsOutOfBoundsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since Beta4.3
 */
public class BfPixelsWrapper {

    private final static Logger log = LoggerFactory.getLogger(BfPixelsWrapper.class);

    private final IFormatReader reader;

    private final String path;

    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object?
     * There should ultimately be some sort of check here that the
     * file is in a/the repository.
     */
    public BfPixelsWrapper(String path, IFormatReader reader) throws IOException, FormatException {
        this.path = path;
        this.reader = reader; // don't re-memoize
        reader.setFlattenedResolutions(false);
        reader.setId(path);
    }

    public byte[] getMessageDigest() throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        int sizeT = getSizeT();
        for (int t = 0; t < sizeT; t++) {
            try {
                int size = RomioPixelBuffer.safeLongToInteger(getTimepointSize());
                byte[] buffer = new byte[size];
                getTimepoint(t,buffer);
                md.update(ByteBuffer.wrap(buffer));
            } catch (DimensionsOutOfBoundsException e) {
                throw new RuntimeException(e);
            }
        }
        return md.digest();
    }

    public void checkBounds(Integer x, Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        int sizeX = getSizeX();
        int sizeY = getSizeY();
        int sizeZ = getSizeZ();
        int sizeC = getSizeC();
        int sizeT = getSizeT();
        if (x != null && (x > sizeX - 1 || x < 0)) {
            throw new DimensionsOutOfBoundsException("X '" + x
                    + "' greater than sizeX '" + sizeX + "'.");
        }
        if (y != null && (y > sizeY - 1 || y < 0)) {
            throw new DimensionsOutOfBoundsException("Y '" + y
                    + "' greater than sizeY '" + sizeY + "'.");
        }
        if (z != null && (z > sizeZ - 1 || z < 0)) {
            throw new DimensionsOutOfBoundsException("Z '" + z
                    + "' greater than sizeZ '" + sizeZ + "'.");
        }
        if (c != null && (c > sizeC - 1 || c < 0)) {
            throw new DimensionsOutOfBoundsException("C '" + c
                    + "' greater than sizeC '" + sizeC + "'.");
        }
        if (t != null && (t > sizeT - 1 || t < 0)) {
            throw new DimensionsOutOfBoundsException("T '" + t
                    + "' greater than sizeT '" + sizeT + "'.");
        }
    }

    private void checkCubeBounds(List<Integer> offset, List<Integer> size, List<Integer> step)
            throws DimensionsOutOfBoundsException {
        // At the moment the array must contain 5 values
        if(offset.size()!=5 || size.size()!=5 || step.size()!=5)
        {
            throw new DimensionsOutOfBoundsException(
                    "Invalid List length: each list must contain 5 elements XYZCT");
        }
        checkBounds(offset.get(0),offset.get(1),offset.get(2),offset.get(3),offset.get(4));
        checkBounds(offset.get(0)+size.get(0)-1,offset.get(1)+size.get(1)-1,
                offset.get(2)+size.get(2)-1,offset.get(3)+size.get(3)-1,offset.get(4)+size.get(4)-1);
        if(step.get(0) < 1 ||  step.get(1) < 1 ||  step.get(2) < 1
                ||  step.get(3) < 1 ||  step.get(4) < 1)
        {
            throw new DimensionsOutOfBoundsException(
                    "Invalid step size: steps sizes must be 1 or greater");
        }
    }

    public void close() throws IOException {
        reader.close();
    }

    public long getId() {
        // id may have no meaning when reading a file directly
        return 0;
    }

    public String getPath() {
        return path;
    }

    /*
     * Get dimension sizes
     */
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

    /*
     * Get data sizes
     */
    public int getByteWidth() {
        return FormatTools.getBytesPerPixel(reader.getPixelType());
    }

    public Integer getRowSize() {
        return getSizeX() * getByteWidth();
    }

    public Integer getColSize() {
        return getSizeY() * getByteWidth();
    }

    public Long getPlaneSize() {
        return (long) getSizeY() * (long) getRowSize();
    }

    public Long getStackSize() {
        return getSizeZ() * getPlaneSize();
    }

    public Long getTimepointSize() {
        return getSizeC() * getStackSize();
    }

    public Long getTotalSize() {
        return getSizeT() * getTimepointSize();
    }

    public Long getHypercubeSize(List<Integer> offset, List<Integer> size, List<Integer> step)
            throws DimensionsOutOfBoundsException {
        // only works for 5d at present
        checkCubeBounds(offset, size, step);
        int tStripes = (size.get(4) + step.get(4) - 1) / step.get(4);
        int cStripes = (size.get(3) + step.get(3) - 1) / step.get(3);
        int zStripes = (size.get(2) + step.get(2) - 1) / step.get(2);
        int yStripes = (size.get(1) + step.get(1) - 1) / step.get(1);
        int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
        long tileRowSize = (long) getByteWidth() * xStripes;
        long cubeSize = tileRowSize * yStripes * zStripes * cStripes * tStripes;

        return cubeSize;
    }

    /*
     * Get data offsets
     */
    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);
        return (long) getPlaneOffset(z,c,t) + y * getRowSize();
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);
        return (long) getStackOffset(c,t) + z * getPlaneSize();
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);
        return (long) getTimepointOffset(t) + c * getStackSize();
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        return (long) t * getTimepointSize();
    }

    public byte[] getCol(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        checkBounds(x, null, z, c, t);
        try {
            if (buffer.length != getColSize())
                throw new RuntimeException("Buffer size incorrect.");
            int size = RomioPixelBuffer.safeLongToInteger(getPlaneSize());
            byte[] plane = new byte[size];
            getWholePlane(z,c,t,plane);
            for(int y = 0; y < reader.getSizeY(); y++) {
                System.arraycopy(plane, (y*getRowSize())+(x*getByteWidth()),
                    buffer, y*getByteWidth(), getByteWidth());
            }
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public byte[] getPlane(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);
        try {
            if (buffer.length != getPlaneSize())
                throw new RuntimeException("Buffer size incorrect.");
            getWholePlane(z,c,t,buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public byte[] getPlaneRegion(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException {
        return null;
    }

    public byte[] getRegion(Integer size, Long offset, byte[] buffer)
            throws IOException {
        return null;
    }

    public byte[] getRow(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);
        try {
            if (buffer.length != getRowSize())
                throw new RuntimeException("Buffer size incorrect.");
            int size = RomioPixelBuffer.safeLongToInteger(getPlaneSize());
            byte[] plane = new byte[size];
            getWholePlane(z,c,t,plane);
            System.arraycopy(plane, y*getRowSize(), buffer, 0, getRowSize());
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public byte[] getStack(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);
        try {
            if (buffer.length != getStackSize())
                throw new RuntimeException("Buffer size incorrect.");
            int size = RomioPixelBuffer.safeLongToInteger(getPlaneSize());
            byte[] plane = new byte[size];
            int sizeZ = getSizeZ();
            for(int z = 0; z < sizeZ; z++)
            {
                getWholePlane(z,c,t,plane);
                System.arraycopy(plane, 0, buffer, z*size, size);
            }
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public byte[] getTimepoint(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        if (buffer.length != getTimepointSize())
            throw new RuntimeException("Buffer size incorrect.");
        int size = RomioPixelBuffer.safeLongToInteger(getStackSize());
        byte[] stack = new byte[size];
        int sizeC = getSizeC();
        for(int c = 0; c < sizeC; c++)
        {
            getStack(c, t, stack);
            System.arraycopy(stack, 0, buffer, c*size, size);
        }
        return buffer;
    }


    public byte[] getHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        if (buffer.length != getHypercubeSize(offset, size, step))
            throw new RuntimeException("Buffer size incorrect.");
        try {
            getWholeHypercube(offset,size,step,buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h,
                          byte[] buffer) throws FormatException, IOException {
        return reader.openBytes(reader.getIndex(z, c, t), buffer, x, y, w, h);
    }

    /*
     * Helper methods
     */

    /*
     * Get a plane dealing with rgb/interleaving if necessary
     */
    private byte[] getWholePlane(int z, int c, int t, byte[] plane)
            throws IOException, FormatException
    {
        int planeNumber;
        if (reader.getRGBChannelCount() == 1) {
            planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, plane);
        } else {
            int size = RomioPixelBuffer.safeLongToInteger(getPlaneSize());
            byte[] fullPlane = new byte[size*reader.getRGBChannelCount()];
            planeNumber = reader.getIndex(z, 0, t);
            reader.openBytes(planeNumber, fullPlane);
            if(reader.isInterleaved()) {
                for(int p = 0; p < size; p += getByteWidth()) {
                    System.arraycopy(fullPlane,
                        c*getByteWidth() + p*reader.getRGBChannelCount(),
                        plane, p, getByteWidth());
                }
            } else {
                System.arraycopy(fullPlane, c*size, plane, 0, size);
            }
        }
        return plane;
    }

    /*
     * Get a plane dealing with rgb/interleaving if necessary
     *  using openPlane2d doesn't seem to work
     */
    /*
    private byte[] getWholePlane(int z, int c, int t, byte[] plane)
            throws IOException, FormatException
    {
        if(rgbChannels == 1) {
            int planeNumber = reader.getIndex(z, c, t);
            Plane2D plane2d = reader.openPlane2D(path, planeNumber, plane);
            plane = plane2d.getData().array();
        } else {
            // Separate channels as openPlane2D doesn't do this
            byte[] fullPlane = new byte[getPlaneSize()*rgbChannels];
            int planeNumber = reader.getIndex(z, 0, t);
            Plane2D plane2d = reader.openPlane2D(path, planeNumber, plane);
            fullPlane = plane2d.getData().array();
            System.arraycopy(fullPlane, c*getPlaneSize(), plane, 0, getPlaneSize());
        }
        return plane;
    }
    */

    private byte[] getWholeHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] cube) throws IOException, FormatException {
        int cubeOffset = 0;
        int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
        int tileRowSize = getByteWidth() * xStripes;
        int planeSize = RomioPixelBuffer.safeLongToInteger(getPlaneSize());
        byte[] plane = new byte[planeSize];
        for(int t = offset.get(4); t < size.get(4)+offset.get(4); t += step.get(4))
        {
            for(int c = offset.get(3); c < size.get(3)+offset.get(3); c += step.get(3))
            {
                for(int z = offset.get(2); z < size.get(2)+offset.get(2); z += step.get(2))
                {
                    getWholePlane(z,c,t,plane);
                    int rowOffset = offset.get(1)*getRowSize();
                    if(step.get(0)==1)
                    {
                        int byteOffset = rowOffset + offset.get(0)*getByteWidth();
                        for(int y = offset.get(1); y < size.get(1)+offset.get(1); y += step.get(1))
                        {
                            System.arraycopy(plane, byteOffset, cube, cubeOffset, tileRowSize);
                            cubeOffset += tileRowSize;
                            byteOffset += getRowSize()*step.get(1);
                        }
                    }
                    else
                    {
                        for(int y = offset.get(1); y < size.get(1)+offset.get(1); y += step.get(1))
                        {
                            int byteOffset = offset.get(0)*getByteWidth();
                            for(int x = offset.get(0); x < size.get(0)+offset.get(0); x += step.get(0))
                            {
                                System.arraycopy(plane, rowOffset+byteOffset, cube, cubeOffset, getByteWidth());
                                cubeOffset += getByteWidth();
                                byteOffset += step.get(0)*getByteWidth();
                            }
                            rowOffset += getRowSize()*step.get(1);
                        }
                    }

                }
            }
        }
        return cube;
    }

    /**
     * cgb - created from the methods below?
     *
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    public String getPixelsType()
    {
        return FormatTools.getPixelTypeString(reader.getPixelType());
    }


    public boolean isFloat() {
        return FormatTools.isFloatingPoint(reader.getPixelType());
    }

    public boolean isSigned() {
        return FormatTools.isSigned(reader.getPixelType());
    }

    /**
     * cgb - stolen from ImportLibrary - slightly modified
     *
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param bytes The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    public byte[] swapIfRequired(byte[] bytes)
        throws FormatException, IOException
    {
        // We've got nothing to do if the samples are only 8-bits wide.
        if (getByteWidth() == 1)
            return bytes;

        boolean isLittleEndian = reader.isLittleEndian();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int length;
        if (isLittleEndian) {
            if (getByteWidth() == 2) { // short/ushort
                ShortBuffer buf = buffer.asShortBuffer();
                length = buffer.limit() / 2;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (getByteWidth() == 4) { // int/uint/float
                IntBuffer buf = buffer.asIntBuffer();
                length = buffer.limit() / 4;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (getByteWidth() == 8) // long/double
            {
                LongBuffer buf = buffer.asLongBuffer();
                length = buffer.limit() / 8;
                for (int i = 0; i < length ; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else {
                throw new FormatException(String.format(
                        "Unsupported sample bit width: %d", getByteWidth()));
            }
        }
        // We've got a big-endian file with a big-endian byte array.
        bytes = buffer.array();
        return bytes;
    }
}
