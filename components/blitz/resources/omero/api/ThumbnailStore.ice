/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_THUMBNAILSTORE_ICE
#define OMERO_API_THUMBNAILSTORE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Provides methods for dealing with thumbnails. Provision is provided
         * to retrieve thumbnails using the on-disk cache (provided by
         * <i>ROMIO</i>) or on the fly.
         * <p>
         * NOTE: The calling order for the service is as follows:
         * <ol>
         * <li>{@link #setPixelsId}</li>
         * <li>any of the thumbnail accessor methods or
         * {@link #resetDefaults}</li>
         * </ol>
         * </p>
         **/
        ["ami", "amd"] interface ThumbnailStore extends StatefulServiceInterface
            {
                /**
                 * This method manages the state of the service; it must be
                 * invoked before using any other methods. As the
                 * {@link omero.api.ThumbnailStore} relies on the
                 * {@link omero.api.RenderingEngine}, a valid rendering
                 * definition must be available for it to work.
                 *
                 * @param pixelsId an {@link omero.model.Pixels} id.
                 * @throws ApiUsageException if no pixels object exists with
                 *         the ID <code>pixelsId</code>.
                 * @return <code>true</code> if a
                 *         {@link omero.model.RenderingDef} exists for the
                 *         {@link omero.model.Pixels} set, otherwise
                 *         <code>false</code>
                 */
                bool setPixelsId(long pixelsId) throws ServerError;

                /**
                 * This returns the last available <i>in progress</i> state
                 * for a thumbnail. Its return value is <b>only</b> expected
                 * to be valid after the call to any of the individual
                 * thumbnail retrieval methods.
                 * @return <code>true</code> if the image is in the process of
                 *         being imported or a pyramid is being generated for
                 *         it.
                 *
                 */
                idempotent bool isInProgress() throws ServerError;

                /**
                 * This method manages the state of the service; it should be
                 * invoked directly after {@link #setPixelsId}. If it is not
                 * invoked with a valid rendering definition ID before using
                 * the thumbnail accessor methods execution continues as if
                 * <code>renderingDefId</code> were set to <code>null</code>.
                 *
                 * @param renderingDefId
                 *            an {@link omero.model.RenderingDef} id.
                 *            <code>null</code> specifies the user's currently
                 *            active rendering settings to be used.
                 * @throws ValidationException
                 *             if no rendering definition exists with the ID
                 *             <code>renderingDefId</code>.
                 */
                idempotent void setRenderingDefId(long renderingDefId) throws ServerError;

                /**
                 * Return the id of the {@link omero.model.RenderingDef}
                 * loaded in this instance.
                 */
                idempotent long getRenderingDefId() throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef). If the thumbnail exists
                 * in the on-disk cache it will be returned directly,
                 * otherwise it will be created as in
                 * {@link #getThumbnailDirect}, placed in the on-disk
                 * cache and returned.
                 *
                 * @param sizeX the X-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @param sizeY the Y-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>sizeX</code> > pixels.sizeX</li>
                 *             <li><code>sizeX</code> is negative</li>
                 *             <li><code>sizeY</code> > pixels.sizeY</li>
                 *             <li><code>sizeY</code> is negative</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnailDirect
                 */
                idempotent Ice::ByteSeq getThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;

                /**
                 * Retrieves a number of thumbnails for pixels sets using
                 * given sets of rendering settings (RenderingDef). If the
                 * thumbnails exist in the on-disk cache they will be returned
                 * directly, otherwise they will be created as in
                 * {@link #getThumbnailDirect}, placed in the on-disk cache
                 * and returned. Unlike the other thumbnail retrieval methods,
                 * this method <b>may</b> be called without first calling
                 * {@link #setPixelsId}.
                 *
                 * @param sizeX the X-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @param sizeY the Y-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @param pixelsIds the Pixels sets to retrieve thumbnails for.
                 * @return a map whose keys are pixels ids and values are JPEG
                 *         thumbnail byte buffers or <code>null</code> if an
                 *         exception was thrown while attempting to retrieve
                 *         the thumbnail for that particular Pixels set.
                 * @see #getThumbnail
                 */
                idempotent omero::sys::IdByteMap getThumbnailSet(omero::RInt sizeX, omero::RInt sizeY, omero::sys::LongList pixelsIds) throws ServerError;

                /**
                 * Retrieves a number of thumbnails for pixels sets using
                 * given sets of rendering settings (RenderingDef). If the
                 * Thumbnails exist in the on-disk cache they will be returned
                 * directly, otherwise they will be created as in
                 * {@link #getThumbnailByLongestSideDirect}. The longest
                 * side of the image will be used to calculate the size for
                 * the smaller side in order to keep the aspect ratio of the
                 * original image. Unlike the other thumbnail retrieval
                 * methods, this method <b>may</b> be called without first
                 * calling {@link #setPixelsId}.
                 *
                 * @param size the size of the longest side of the thumbnail
                 *             requested. <code>null</code> specifies the
                 *             default size of 48.
                 * @param pixelsIds the Pixels sets to retrieve thumbnails for.
                 * @return a map whose keys are pixels ids and values are JPEG
                 *         thumbnail byte buffers or <code>null</code> if an
                 *         exception was thrown while attempting to retrieve
                 *         the thumbnail for that particular Pixels set.
                 * @see #getThumbnailSet
                 */
                idempotent omero::sys::IdByteMap getThumbnailByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef). If the thumbnail exists
                 * in the on-disk cache it will bereturned directly, otherwise
                 * it will be created as in {@link #getThumbnailDirect},
                 * placed in the on-disk cache and returned. The longest side
                 * of the image will be used to calculate the size for the
                 * smaller side in order to keep the aspect ratio of the
                 * original image.
                 *
                 * @param size the size of the longest side of the thumbnail
                 *             requested. <code>null</code> specifies the
                 *             default size of 48.
                 * @throws ApiUsageException if:
                 *         <ul>
                 *         <li><code>size</code> > pixels.sizeX and pixels.sizeY</li>
                 *         <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnail
                 */
                idempotent Ice::ByteSeq getThumbnailByLongestSide(omero::RInt size) throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef). The Thumbnail will
                 * always be created directly, ignoring the on-disk cache. The
                 * longest side of the image will be used to calculate the
                 * size for the smaller side in order to keep the aspect ratio
                 * of the original image.
                 *
                 * @param size the size of the longest side of the thumbnail
                 *             requested. <code>null</code> specifies the
                 *             default size of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>size</code> > pixels.sizeX and pixels.sizeY</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnailDirect
                 */
                idempotent Ice::ByteSeq getThumbnailByLongestSideDirect(omero::RInt size) throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef). The Thumbnail will
                 * always be created directly, ignoring the on-disk cache.
                 *
                 * @param sizeX the X-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @param sizeY the Y-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>sizeX</code> > pixels.sizeX</li>
                 *             <li><code>sizeX</code> is negative</li>
                 *             <li><code>sizeY</code> > pixels.sizeY</li>
                 *             <li><code>sizeY</code> is negative</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnail
                 */
                idempotent Ice::ByteSeq getThumbnailDirect(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef) for a particular section.
                 * The Thumbnail will always be created directly, ignoring the
                 * on-disk cache.
                 *
                 * @param theZ the optical section (offset across the Z-axis)
                 *             to use.
                 * @param theT the timepoint (offset across the T-axis) to use.
                 * @param sizeX the X-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @param sizeY the Y-axis width of the thumbnail.
                 *              <code>null</code> specifies the default size
                 *              of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>sizeX</code> > pixels.sizeX</li>
                 *             <li><code>sizeX</code> is negative</li>
                 *             <li><code>sizeY</code> > pixels.sizeY</li>
                 *             <li><code>sizeY</code> is negative</li>
                 *             <li><code>theZ</code> is out of range</li>
                 *             <li><code>theT</code> is out of range</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnail
                 */
                idempotent Ice::ByteSeq getThumbnailForSectionDirect(int theZ, int theT, omero::RInt sizeX, omero::RInt sizeY) throws ServerError;

                /**
                 * Retrieves a thumbnail for a pixels set using a given set of
                 * rendering settings (RenderingDef) for a particular section.
                 * The Thumbnail will always be created directly, ignoring the
                 * on-disk cache. The longest side of the image will be used
                 * to calculate the size for the smaller side in order to keep
                 * the aspect ratio of the original image.
                 *
                 * @param theZ the optical section (offset across the Z-axis)
                 *             to use.
                 * @param theT the timepoint (offset across the T-axis) to use.
                 * @param size the size of the longest side of the thumbnail
                 *             requested. <code>null</code> specifies the
                 *             default size of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>size</code> > pixels.sizeX and pixels.sizeY</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @return a JPEG thumbnail byte buffer.
                 * @see #getThumbnailDirect
                 */
                idempotent Ice::ByteSeq getThumbnailForSectionByLongestSideDirect(int theZ, int theT, omero::RInt size) throws ServerError;

                /**
                 * Creates thumbnails for a pixels set using a given set of
                 * rendering settings (RenderingDef) in the on-disk cache for
                 * <b>every</b> sizeX/sizeY combination already cached.
                 *
                 * @see #getThumbnail
                 * @see #getThumbnailDirect
                 */
                void createThumbnails() throws ServerError;

                 /**
                  * Creates a thumbnail for a pixels set using a given set of
                  * rendering settings (RenderingDef) in the on-disk cache.
                  *
                  * @param sizeX the X-axis width of the thumbnail.
                  *              <code>null</code> specifies the default size
                  *              of 48.
                  * @param sizeY the Y-axis width of the thumbnail.
                  *              <code>null</code> specifies the default size
                  *              of 48.
                  * @throws ApiUsageException
                  *             if:
                  *             <ul>
                  *             <li><code>sizeX</code> > pixels.sizeX</li>
                  *             <li><code>sizeX</code> is negative</li>
                  *             <li><code>sizeY</code> > pixels.sizeY</li>
                  *             <li><code>sizeY</code> is negative</li>
                  *             <li>{@link #setPixelsId} has not yet been called</li>
                  *             </ul>
                  * @see #getThumbnail
                  * @see #getThumbnailDirect
                  */
                void createThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;

                /**
                 * Creates thumbnails for a number of pixels sets using a
                 * given set of rendering settings (RenderingDef) in the
                 * on-disk cache. Unlike the other thumbnail creation methods,
                 * this method <b>may</b> be called without first calling
                 * {@link #setPixelsId}. This method <b>will not</b> reset or
                 * modify rendering settings in any way. If rendering settings
                 * for a pixels set are not present, thumbnail creation for
                 * that pixels set <b>will not</b> be performed.
                 *
                 * @param size the size of the longest side of the thumbnail
                 *             requested. <code>null</code> specifies the
                 *             default size of 48.
                 * @param pixelsIds the Pixels sets to retrieve thumbnails for.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>size</code> > pixels.sizeX and pixels.sizeY</li>
                 *             <li><code>size</code> is negative</li>
                 *             </ul>
                 * @see #createThumbnail
                 * @see #createThumbnails
                 */
                void createThumbnailsByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;

                /**
                 * Checks if a thumbnail of a particular size exists for a
                 * pixels set.
                 *
                  * @param sizeX the X-axis width of the thumbnail.
                  *              <code>null</code> specifies the default size
                  *              of 48.
                  * @param sizeY the Y-axis width of the thumbnail.
                  *              <code>null</code> specifies the default size
                  *              of 48.
                 * @throws ApiUsageException
                 *             if:
                 *             <ul>
                 *             <li><code>sizeX</code> is negative</li>
                 *             <li><code>sizeY</code> is negative</li>
                 *             <li>{@link #setPixelsId} has not yet been called</li>
                 *             </ul>
                 * @see #getThumbnail
                 * @see #getThumbnailDirect
                 */
                idempotent bool thumbnailExists(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;

                /**
                 * Resets the rendering definition for the active pixels set
                 * to its default settings.
                 */
                idempotent void resetDefaults() throws ServerError;
            };
    };
};

#endif
