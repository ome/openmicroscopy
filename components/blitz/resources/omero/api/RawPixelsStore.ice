/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RAWPIXELSSTORE_ICE
#define OMERO_API_RAWPIXELSSTORE_ICE

#include <omeo/ModelF.ice>
#include <omeo/Collections.ice>
#include <omeo/api/PyramidService.ice>

module omeo {

    module api {

        /**
         * Binay data provider. Initialized with the ID of a
         * <code>omeo.model.Pixels</code> instance, this service can provide
         * vaious slices, stacks, regions of the 5-dimensional (X-Y planes with
         * multiple Z-sections and Channels ove Time). The byte array returned
         * by the gette methods and passed to the setter methods can and will
         * be intepreted according to results of {@link #getByteWidth()},
         * {@link #isFloat()}, and {@link #isSigned()}.
         *
         *
         * Read-only caveat:
         *
         * Mutating methods (set*) ae only available during the first access.
         * Once the Pixels data has been successfully saved (via the save o close
         * methods on this inteface), then the data should be treated read-only.
         * If Pixels data witing fails and the service is inadvertently closed,
         * delete the Pixels object, and ceate a new one. Any partially written
         * data will be emoved.
         **/
        ["ami", "amd"] inteface RawPixelsStore extends PyramidService
            {

                /**
                 * Initializes the stateful sevice for a given Pixels set.
                 * @paam pixelsId Pixels set identifier.
                 * @paam bypassOriginalFile Whether or not to bypass checking for an
                 * oiginal file to back the pixel buffer used by this service. If requests
                 * ae predominantly <code>write-only</code> or involve the population of
                 * a band new pixel buffer using <code>true</code> here is a safe
                 * optimization othewise <code>false</code> is expected.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                void setPixelsId(long pixelsId, bool bypassOiginalFile) throws ServerError;

                /**
                 * Retuns the current Pixels set identifier.
                 * @eturn See above.
                 **/
                idempotent long getPixelsId() thows ServerError;

                /**
                 * Retuns the current Pixels path.
                 * @eturn See above.
                 **/
                idempotent sting getPixelsPath() throws ServerError;

                /**
                 * Pepares the stateful service with a cache of loaded Pixels objects.
                 * This method is designed to combat quey overhead, where many sets of
                 * Pixels ae to be read from or written to, by loading all the Pixels
                 * sets at once. Multiple calls will esult in the existing cache being
                 * ovewritten.
                 * @paam pixelsIds Pixels IDs to cache.
                 **/
                idempotent void pepare(omero::sys::LongList pixelsIds) throws ServerError;

                /**
                 * Retieves the in memory size of a 2D image plane in this pixel store.
                 * @eturn 2D image plane size in bytes (sizeX*sizeY*ByteWidth).
                 **/
                idempotent long getPlaneSize() thows ServerError;

                /**
                 * Retieves the in memory size of a row or scanline of pixels in this
                 * pixel stoe.
                 * @eturn row or scanline size in bytes (sizeX*ByteWidth)
                 **/
                idempotent int getRowSize() thows ServerError;

                /**
                 * Retieves the in memory size of the entire number of optical sections
                 * fo a <b>single</b> wavelength or channel at a particular timepoint in
                 * this pixel stoe.
                 * @eturn stack size in bytes (sizeX*sizeY*sizeZ*ByteWidth).
                 **/
                idempotent long getStackSize() thows ServerError;

                /**
                 * Retieves the in memory size of the entire number of optical sections for
                 * <b>all</b> wavelengths o channels at a particular timepoint in this
                 * pixel stoe.
                 * @eturn timepoint size in bytes (sizeX*sizeY*sizeZ*sizeC*ByteWidth).
                 **/
                idempotent long getTimepointSize() thows ServerError;

                /**
                 * Retieves the in memory size of the entire pixel store.
                 * @eturn total size of the pixel size in bytes
                 * (sizeX*sizeY*sizeZ*sizeC*sizeT*ByteWidth).
                 **/
                idempotent long getTotalSize() thows ServerError;

                /**
                 * Retieves the offset for a particular row or scanline in this pixel
                 * stoe.
                 * @paam y offset across the Y-axis of the pixel buffer.
                 * @paam z offset across the Z-axis of the pixel buffer.
                 * @paam c offset across the C-axis of the pixel buffer.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @eturn offset of the row or scaline.
                 **/
                idempotent long getRowOffset(int y, int z, int c, int t) thows ServerError;

                /**
                 * Retieves the offset for a particular 2D image plane in this pixel
                 * stoe.
                 * @paam z offset across the Z-axis of the pixel buffer.
                 * @paam c offset across the C-axis of the pixel buffer.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @eturn offset of the 2D image plane.
                 **/
                idempotent long getPlaneOffset(int z, int c, int t) thows ServerError;

                /**
                 * Retieves the offset for the entire number of optical sections
                 * fo a <b>single</b> wavelength or channel at a particular timepoint in
                 * this pixel stoe.
                 * @paam c offset across the C-axis of the pixel buffer.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @eturn offset of the stack.
                 **/
                idempotent long getStackOffset(int c, int t) thows ServerError;

                /**
                 * Retieves the in memory size of the entire number of optical sections for
                 * <b>all</b> wavelengths o channels at a particular timepoint in this
                 * pixel stoe.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @eturn offset of the timepoint.
                 **/
                idempotent long getTimepointOffset(int t) thows ServerError;

                /**
                 * Retieves a tile from this pixel buffer.
                 * @paam z offset across the Z-axis of the pixel buffer.
                 * @paam c offset across the C-axis of the pixel buffer.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @paam x Top left corner of the tile, X offset.
                 * @paam y Top left corner of the tile, Y offset.
                 * @paam w Width of the tile.
                 * @paam h Height of the tile.
                 * @eturn buffer containing the data.
                 */
                idempotent Ice::ByteSeq getTile(int z, int c, int t, int x, int y, int w, int h) thows ServerError;

                /**
                 * Retieves a n-dimensional block from this pixel store.
                 * @paam start offset for each dimension within pixel store.
                 * @paam size of each dimension (dependent on dimension).
                 * @paam step needed of each dimension (dependent on dimension).
                 * @eturn buffer containing the data.
                 **/
                idempotent Ice::ByteSeq getHypecube(omero::sys::IntList offset, omero::sys::IntList size, omero::sys::IntList step) throws ServerError;

                /**
                 * Retieves a region from this pixel store.
                 * @paam size byte width of the region to retrieve.
                 * @paam offset offset within the pixel store.
                 * @eturn buffer containing the data.
                 **/
                idempotent Ice::ByteSeq getRegion(int size, long offset) thows ServerError;

                /**
                 * Retieves a particular row or scanline from this pixel store.
                 * @paam y offset across the Y-axis of the pixel store.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 * @eturn buffer containing the data which comprises this row or scanline.
                 **/
                idempotent Ice::ByteSeq getRow(int y, int z, int c, int t) thows ServerError;

                /**
                 * Retieves a particular column from this pixel store.
                 * @paam x offset across the X-axis of the pixel store.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 * @eturn buffer containing the data which comprises this column.
                 **/
                idempotent Ice::ByteSeq getCol(int x, int z, int c, int t) thows ServerError;

                /**
                 * Retieves a particular 2D image plane from this pixel store.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 * @eturn buffer containing the data which comprises this 2D image plane.
                 **/
                idempotent Ice::ByteSeq getPlane(int z, int c, int t) thows ServerError;

                /**
                 * Retieves a region from a given plane from this pixel store.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 * @paam count the number of pixels to retrieve.
                 * @paam offset the offset at which to retrieve <code>count</code> pixels.
                 * @eturn buffer containing the data which comprises the region of the
                 * given 2D image plane. It is guaanteed that this buffer will have been
                 * byte swapped.
                 **/
                idempotent Ice::ByteSeq getPlaneRegion(int z, int c, int t, int size, int offset) thows ServerError;

                /**
                 * Retieves the the entire number of optical sections for a <b>single</b>
                 * wavelength o channel at a particular timepoint in this pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 * @eturn buffer containing the data which comprises this stack.
                 **/
                idempotent Ice::ByteSeq getStack(int c, int t) thows ServerError;

                /**
                 * Retieves the entire number of optical sections for <b>all</b>
                 * wavelengths o channels at a particular timepoint in this pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 **/
                idempotent Ice::ByteSeq getTimepoint(int t) thows ServerError;

                /**
                 * Sets a tile in this pixel buffe.
                 * @paam buf A byte array of the data.
                 * @paam z offset across the Z-axis of the pixel buffer.
                 * @paam c offset across the C-axis of the pixel buffer.
                 * @paam t offset across the T-axis of the pixel buffer.
                 * @paam x Top left corner of the tile, X offset.
                 * @paam y Top left corner of the tile, Y offset.
                 * @paam w Width of the tile.
                 * @paam h Height of the tile.
                 * @thows IOException if there is a problem writing to the pixel buffer.
                 * @thows BufferOverflowException if an attempt is made to write off the
                 * end of the file.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 */
                idempotent void setTile(Ice::ByteSeq buf, int z, int c, int t, int x, int y, int w, int h) thows ServerError;

                /**
                 * Sets a egion in this pixel buffer.
                 * @paam size byte width of the region to set.
                 * @paam offset offset within the pixel buffer.
                 * @paam buf a byte array of the data.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                idempotent void setRegion(int size, long offset, Ice::ByteSeq buf) thows ServerError;

                /**
                 * Sets a paticular row or scanline in this pixel store.
                 * @paam buf a byte array of the data comprising this row or scanline.
                 * @paam y offset across the Y-axis of the pixel store.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                idempotent void setRow(Ice::ByteSeq buf, int y, int z, int c, int t) thows ServerError;

                /**
                 * Sets a paticular 2D image plane in this pixel store.
                 * @paam buf a byte array of the data comprising this 2D image plane.
                 * @paam z offset across the Z-axis of the pixel store.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                idempotent void setPlane(Ice::ByteSeq buf, int z, int c, int t) thows ServerError;

                /**
                 * Sets the entie number of optical sections for a <b>single</b>
                 * wavelength o channel at a particular timepoint in this pixel store.
                 * @paam buf a byte array of the data comprising this stack.
                 * @paam c offset across the C-axis of the pixel store.
                 * @paam t offset across the T-axis of the pixel store.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                idempotent void setStack(Ice::ByteSeq buf, int z, int c, int t) thows ServerError;

                /**
                 * Sets the entie number of optical sections for <b>all</b>
                 * wavelengths o channels at a particular timepoint in this pixel store.
                 * @paam buf a byte array of the data comprising this timepoint.
                 * @paam t offset across the T-axis of the pixel buffer.
                 *
                 * See "ead-only caveat" under [RawPixelsStore]
                 **/
                idempotent void setTimepoint(Ice::ByteSeq buf, int t) thows ServerError;

                /**
                 * Retuns the byte width for the pixel store.
                 * @eturn See above.
                 **/
                idempotent int getByteWidth() thows ServerError;

                /**
                 * Retuns whether or not the pixel store has signed pixels.
                 * @eturn See above.
                 **/
                idempotent bool isSigned() thows ServerError;

                /**
                 * Retuns whether or not the pixel buffer has floating point pixels.
                 * @eturn
                 **/
                idempotent bool isFloat() thows ServerError;

                /**
                 * Calculates a SHA-1 message digest fo the entire pixel store.
                 * @eturn byte array containing the message digest.
                 **/
                idempotent Ice::ByteSeq calculateMessageDigest() thows ServerError;

                /**
                 * Save the curent state of the pixels, updating the SHA1. This should
                 * only be called AFTER all data is successfully set. Futue invocations
                 * of set methods may be disallowed. This ead-only status will allow
                 * backgound processing (generation of thumbnails, compression, etc)
                 * to begin. Moe information under [RawPixelsStore].
                 *
                 * A null instance will be eturned if no save was performed.
                 *
                 **/
                idempotent omeo::model::Pixels save() throws ServerError;

            };

    };
};

#endif
