package ome.api;

/**
 * Provides methods for dealing with thumbnails. Provision is provided to
 * retrieve thumbnails using the on-disk cache (provided by <i>ROMIO</i>) or on
 * the fly.
 * <p>
 * NOTE: The calling order for the service is as follows:
 * <ol>
 * <li>setPixelsId()</li>
 * <li>setRenderingDefId()</li>
 * <li>any of the thumbnail accessor methods</li>
 * </ol>
 * </p>
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public interface ThumbnailStore extends StatefulServiceInterface {
    /**
     * This method manages the state of the service; it must be invoked before
     * using any other methods.
     * 
     * @param pixelsId
     *            an {@link ome.model.core.Pixels} id.
     * @throws ApiUsageException
     *             if no pixels object exists with the ID <i>pixelsId</i>.
     */
    public void setPixelsId(long pixelsId);

    /**
     * This method manages the state of the service; it should be invoked
     * directly after {@link setPixelsId}. If it is not invoked with a valid
     * rendering definition ID before using the thumbnail accessor methods
     * execution continues as if <i>renderingDefId</i> were set to
     * <code>null</code>.
     * 
     * @param renderingDefId
     *            an {@link ome.model.display.RenderingDef} id.
     *            <code>null</code> specifies the user's currently active
     *            rendering settings to be used.
     * @throws ApiUsageException
     *             if no rendering definition exists with the ID
     *             <i>renderingDefId</i>.
     */
    public void setRenderingDefId(Long renderingDefId);

    /**
     * Retrieves the a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). If the thumbnail exists on-disk cache it will be
     * returned directly, otherwise it will be created directly as in {@link
     * #getThumbDirect()}.
     * 
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>sizeX</i> pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see getThumbnailDirect()
     */
    public byte[] getThumbnail(Integer sizeX, Integer sizeY);

    /**
     * Retrieves the a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). If the thumbnail exists on-disk cache it will be
     * returned directly, otherwise it will be created directly as in {@link
     * #getThumbDirect()}. The longest side of the image will be used to
     * calculate the size for the smaller side in order to keep the aspect ratio
     * of the original image.
     * 
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>size</i> > pixels.sizeX and pixels.sizeY</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see getThumbnail()
     */
    public byte[] getThumbnailByLongestSide(Integer size);

    /**
     * Retrieves the a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). The Thumbnail will always be created directly,
     * ignoring the on-disk cache.
     * 
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>sizeX</i> pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see getThumbnail()
     */
    public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY);

    /**
     * Retrieves the a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). The Thumbnail will always be created directly,
     * ignoring the on-disk cache. The longest side of the image will be used to
     * calculate the size for the smaller side in order to keep the aspect ratio
     * of the original image.
     * 
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>size</i> > pixels.sizeX and pixels.sizeY</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see getThumbnailDirect()
     */
    public byte[] getThumbnailByLongestSideDirect(Integer size);

    /**
     * Creates a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef) in the on-disk cache.
     * 
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>sizeX</i> pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @see getThumb()
     * @see getThumbDirect()
     */
    public void createThumbnail(Integer sizeX, Integer sizeY);

    /**
     * Creates thumbnails for a pixels set using a given set of rendering
     * settings (RenderingDef) in the on-disk cache for <b>every</b>
     * sizeX/sizeY combination already cached.
     * 
     * @see getThumb()
     * @see getThumbDirect()
     */
    public void createThumbnails();

    /**
     * Checks if a thumbnail of a particular size exists for a pixels set.
     * 
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies use the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies user the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link setPixelsId()} has not yet been called</li>
     *             </ul>
     * @see getThumb()
     * @see getThumbDirect()
     */
    public boolean thumbnailExists(Integer sizeX, Integer sizeY);
}
