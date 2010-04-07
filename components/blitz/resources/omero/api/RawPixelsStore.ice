/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RAWPIXELSSTORE_ICE
#define OMERO_API_RAWPIXELSSTORE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Binary data provider. Initialized with the ID of a
         * <code>omero.model.Pixels</code> instance, this service can provide
         * various slices, stacks, regions of the 5-dimensional (X-Y planes with
         * multiple Z-sections and Channels over Time). The byte array returned
         * by the getter methods and passed to the setter methods can and will
         * be interpreted according to results of {@link #getByteWidth()},
         * {@link #isFloat()}, and {@link #isSigned()}.
         **/
        ["ami", "amd"] interface RawPixelsStore extends StatefulServiceInterface
            {

                /**
                 * Initializes the stateful service for a given Pixels set.
                 * @param pixelsId Pixels set identifier.
                 * @param bypassOriginalFile Whether or not to bypass checking for an
                 * original file to back the pixel buffer used by this service. If requests
                 * are predominantly <code>write-only</code> or involve the population of
                 * a brand new pixel buffer using <code>true</code> here is a safe
                 * optimization otherwise <code>false</code> is expected.
                 **/
                void setPixelsId(long pixelsId, bool bypassOriginalFile) throws ServerError;

                /**
                 * Prepares the stateful service with a cache of loaded Pixels objects.
                 * This method is designed to combat query overhead, where many sets of
                 * Pixels are to be read from or written to, by loading all the Pixels
                 * sets at once. Multiple calls will result in the existing cache being
                 * overwritten.
                 * @param pixelsIds Pixels IDs to cache.
                 **/
                idempotent void prepare(omero::sys::LongList pixelsIds) throws ServerError;

                /**
                 * Retrieves the in memory size of a 2D image plane in this pixel store.
                 * @return 2D image plane size in bytes (sizeX*sizeY*ByteWidth).
                 **/
                idempotent int getPlaneSize() throws ServerError;

                /**
                 * Retrieves the in memory size of a row or scanline of pixels in this
                 * pixel store.
                 * @return row or scanline size in bytes (sizeX*ByteWidth)
                 **/
                idempotent int getRowSize() throws ServerError;

                /**
                 * Retrieves the in memory size of the entire number of optical sections
                 * for a <b>single</b> wavelength or channel at a particular timepoint in
                 * this pixel store.
                 * @return stack size in bytes (sizeX*sizeY*sizeZ*ByteWidth).
                 **/
                idempotent int getStackSize() throws ServerError;

                /**
                 * Retrieves the in memory size of the entire number of optical sections for
                 * <b>all</b> wavelengths or channels at a particular timepoint in this
                 * pixel store.
                 * @return timepoint size in bytes (sizeX*sizeY*sizeZ*sizeC*ByteWidth).
                 **/
                idempotent int getTimepointSize() throws ServerError;

                /**
                 * Retrieves the in memory size of the entire pixel store.
                 * @return total size of the pixel size in bytes
                 * (sizeX*sizeY*sizeZ*sizeC*sizeT*ByteWidth).
                 **/
                idempotent int getTotalSize() throws ServerError;

                /**
                 * Retrieves the offset for a particular row or scanline in this pixel
                 * store.
                 * @param y offset across the Y-axis of the pixel buffer.
                 * @param z offset across the Z-axis of the pixel buffer.
                 * @param c offset across the C-axis of the pixel buffer.
                 * @param t offset across the T-axis of the pixel buffer.
                 * @return offset of the row or scaline.
                 **/
                idempotent long getRowOffset(int y, int z, int c, int t) throws ServerError;

                /**
                 * Retrieves the offset for a particular 2D image plane in this pixel
                 * store.
                 * @param z offset across the Z-axis of the pixel buffer.
                 * @param c offset across the C-axis of the pixel buffer.
                 * @param t offset across the T-axis of the pixel buffer.
                 * @return offset of the 2D image plane.
                 **/
                idempotent long getPlaneOffset(int z, int c, int t) throws ServerError;

                /**
                 * Retrieves the offset for the entire number of optical sections
                 * for a <b>single</b> wavelength or channel at a particular timepoint in
                 * this pixel store.
                 * @param c offset across the C-axis of the pixel buffer.
                 * @param t offset across the T-axis of the pixel buffer.
                 * @return offset of the stack.
                 **/
                idempotent long getStackOffset(int c, int t) throws ServerError;

                /**
                 * Retrieves the in memory size of the entire number of optical sections for
                 * <b>all</b> wavelengths or channels at a particular timepoint in this
                 * pixel store.
                 * @param t offset across the T-axis of the pixel buffer.
                 * @return offset of the timepoint.
                 **/
                idempotent long getTimepointOffset(int t) throws ServerError;

                /**
                 * Retrieves a region from this pixel store.
                 * @param size byte width of the region to retrieve.
                 * @param offset offset within the pixel store.
                 * @return buffer containing the data.
                 **/
                idempotent Ice::ByteSeq getRegion(int size, long offset) throws ServerError;

                /**
                 * Retrieves a particular row or scanline from this pixel store.
                 * @param y offset across the Y-axis of the pixel store.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 * @return buffer containing the data which comprises this row or scanline.
                 **/
                idempotent Ice::ByteSeq getRow(int y, int z, int c, int t) throws ServerError;

                /**
                 * Retrieves a particular column from this pixel store.
                 * @param x offset across the X-axis of the pixel store.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 * @return buffer containing the data which comprises this column.
                 **/
                idempotent Ice::ByteSeq getCol(int x, int z, int c, int t) throws ServerError;

                /**
                 * Retrieves a particular 2D image plane from this pixel store.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 * @return buffer containing the data which comprises this 2D image plane.
                 **/
                idempotent Ice::ByteSeq getPlane(int z, int c, int t) throws ServerError;

                /**
                 * Retrieves a region from a given plane from this pixel store.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 * @param count the number of pixels to retrieve.
                 * @param offset the offset at which to retrieve <code>count</code> pixels.
                 * @return buffer containing the data which comprises the region of the
                 * given 2D image plane. It is guaranteed that this buffer will have been
                 * byte swapped.
                 **/
                idempotent Ice::ByteSeq getPlaneRegion(int z, int c, int t, int size, int offset) throws ServerError;

                /**
                 * Retrieves the the entire number of optical sections for a <b>single</b>
                 * wavelength or channel at a particular timepoint in this pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 * @return buffer containing the data which comprises this stack.
                 **/
                idempotent Ice::ByteSeq getStack(int c, int t) throws ServerError;

                /**
                 * Retrieves the entire number of optical sections for <b>all</b>
                 * wavelengths or channels at a particular timepoint in this pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 **/
                idempotent Ice::ByteSeq getTimepoint(int t) throws ServerError;

                /**
                 * Sets a region in this pixel buffer.
                 * @param size byte width of the region to set.
                 * @param offset offset within the pixel buffer.
                 * @param buf a byte array of the data.
                 **/
                idempotent void setRegion(int size, long offset, Ice::ByteSeq buf) throws ServerError;

                /**
                 * Sets a particular row or scanline in this pixel store.
                 * @param buf a byte array of the data comprising this row or scanline.
                 * @param y offset across the Y-axis of the pixel store.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 **/
                idempotent void setRow(Ice::ByteSeq buf, int y, int z, int c, int t) throws ServerError;

                /**
                 * Sets a particular 2D image plane in this pixel store.
                 * @param buf a byte array of the data comprising this 2D image plane.
                 * @param z offset across the Z-axis of the pixel store.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 **/
                idempotent void setPlane(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;

                /**
                 * Sets the entire number of optical sections for a <b>single</b>
                 * wavelength or channel at a particular timepoint in this pixel store.
                 * @param buf a byte array of the data comprising this stack.
                 * @param c offset across the C-axis of the pixel store.
                 * @param t offset across the T-axis of the pixel store.
                 **/
                idempotent void setStack(Ice::ByteSeq buf, int z, int c, int t) throws ServerError;

                /**
                 * Sets the entire number of optical sections for <b>all</b>
                 * wavelengths or channels at a particular timepoint in this pixel store.
                 * @param buf a byte array of the data comprising this timepoint.
                 * @param t offset across the T-axis of the pixel buffer.
                 **/
                idempotent void setTimepoint(Ice::ByteSeq buf, int t) throws ServerError;

                /**
                 * Returns the byte width for the pixel store.
                 * @return See above.
                 **/
                idempotent int getByteWidth() throws ServerError;

                /**
                 * Returns whether or not the pixel store has signed pixels.
                 * @return See above.
                 **/
                idempotent bool isSigned() throws ServerError;

                /**
                 * Returns whether or not the pixel buffer has floating point pixels.
                 * @return
                 **/
                idempotent bool isFloat() throws ServerError;

                /**
                 * Calculates a SHA-1 message digest for the entire pixel store.
                 * @return byte array containing the message digest.
                 **/
                idempotent Ice::ByteSeq calculateMessageDigest() throws ServerError;

            };

    };
};

#endif
