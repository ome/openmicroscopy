/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

import java.util.Map;
import java.util.Set;

import ome.annotations.NotNull;
import ome.annotations.Validate;

/**
 * Provides methods for dealing with thumbnails. Provision is provided to
 * retrieve thumbnails using the on-disk cache (provided by <i>ROMIO</i>) or on
 * the fly.
 * <p>
 * NOTE: The calling order for the service is as follows:
 * <ol>
 * <li>setPixelsId()</li>
 * <li>any of the thumbnail accessor methods or resetDefaults()</li>
 * </ol>
 * </p>
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0
 * @since 3.0
 */
public interface ThumbnailStore extends StatefulServiceInterface {
    /**
     * This method manages the state of the service; it must be invoked before
     * using any other methods. As the <pre>ThumbnailStore</pre> relies on the
     * <pre>RenderingEngine</pre>, a valid rendering definition must be 
     * available for it to work. 
     * 
     * @param pixelsId
     *            an {@link ome.model.core.Pixels} id.
     * @throws ApiUsageException
     *             if no pixels object exists with the ID <i>pixelsId</i>.
     * @return <code>true</code> if a <code>RenderingDef</code> exists for the
     * <code>Pixels</code> set, otherwise <code>false</code>
     *
     */
    public boolean setPixelsId(long pixelsId);

    /**
     * This returns the last available <i>in progress</i> state for a
     * thumbnail. Its return value is <b>only</b> expected to be valid after
     * the call to any of the individual thumbnail retrieval methods.
     * @return <code>true</code> if the image is in the process of being
     * imported or a pyramid is being generated for it.
     *
     */
    public boolean isInProgress();

    /**
     * This method manages the state of the service; it should be invoked
     * directly after {@link #setPixelsId(long)}. If it is not invoked with a
     * valid rendering definition ID before using the thumbnail accessor
     * methods execution continues as if <i>renderingDefId</i> were set to
     * <code>null</code>.
     * 
     * @param renderingDefId
     *            an {@link ome.model.display.RenderingDef} id.
     *            <code>null</code> specifies the user's currently active
     *            rendering settings to be used.
     * @throws ValidationException
     *             if no rendering definition exists with the ID
     *             <i>renderingDefId</i>.
     */
    public void setRenderingDefId(long renderingDefId);

    /**
     * Return the id of the {@link ome.model.display.RenderingDef} loaded in
     * this instance.
     */
    public long getRenderingDefId();

    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). If the thumbnail exists in the on-disk cache 
     * it will be returned directly, otherwise it will be created as in
     * {@link #getThumbnailDirect(Integer, Integer)}, placed in the on-disk
     * cache and returned.
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
     *             <li><i>sizeX</i> > pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnailDirect(Integer, Integer)
     */
    public byte[] getThumbnail(Integer sizeX, Integer sizeY);
    
    /**
     * Retrieves a number of thumbnails for pixels sets using given sets of 
     * rendering settings (RenderingDef). If the thumbnails exist in the 
     * on-disk cache they will be returned directly, otherwise they will be
     * created as in {@link #getThumbnailDirect(Integer, Integer)}, placed in
     * the on-disk cache and returned. Unlike the other thumbnail retrieval
     * methods, this method <b>may</b> be called without first calling
     * {@link #setPixelsId(long)}.
     * 
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @return a {@link Map} whose keys are pixels ids and values are JPEG 
     * thumbnail byte buffers or <code>null</code> if an exception was thrown 
     * while attempting to retrieve the thumbnail for that particular Pixels
     * set.
     * @see #getThumbnail(Integer, Integer)
     */
    public Map<Long, byte[]> getThumbnailSet(Integer sizeX, Integer sizeY, 
    		@NotNull @Validate(Long.class) Set<Long> pixelsIds);
    
    /**
     * Retrieves a number of thumbnails for pixels sets using given sets of 
     * rendering settings (RenderingDef). If the Thumbnails exist in the 
     * on-disk cache they will be returned directly, otherwise they will be
     * created as in {@link #getThumbnailByLongestSideDirect}. The longest 
     * side of the image will be used to calculate the size for the smaller 
     * side in order to keep the aspect ratio of the original image. Unlike the 
     * other thumbnail retrieval methods, this method <b>may</b> be called 
     * without first calling {@link #setPixelsId(long)}.
     * 
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @return a {@link Map} whose keys are pixels ids and values are JPEG 
     * thumbnail byte buffers or <code>null</code> if an exception was thrown 
     * while attempting to retrieve the thumbnail for that particular Pixels
     * set.
     * @see #getThumbnailSet(Integer, Integer, Set)
     */
    public Map<Long, byte[]> getThumbnailByLongestSideSet(Integer size,
    		@NotNull @Validate(Long.class) Set<Long> pixelsIds);

    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef). If the thumbnail exists in the on-disk cache it 
     * will bereturned directly, otherwise it will be created as in
     * {@link #getThumbnailDirect(Integer, Integer)}, placed in the on-disk
     * cache and returned. The longest side of the image will be used to
     * calculate the size for the smaller side in order to keep the aspect
     * ratio of the original image.
     * 
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>size</i> > pixels.sizeX and pixels.sizeY</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnail(Integer, Integer)
     */
    public byte[] getThumbnailByLongestSide(Integer size);

    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
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
     *             <li><i>sizeX</i> > pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnail(Integer, Integer)
     */
    public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY);
    
    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef) for a particular section. The Thumbnail will 
     * always be created directly, ignoring the on-disk cache.
     * 
     * @param theZ the optical section (offset across the Z-axis) to use.
     * @param theT the timepoint (offset across the T-axis) to use.
     * @param sizeX
     *            the X-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @param sizeY
     *            the Y-axis width of the thumbnail. <code>null</code>
     *            specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>sizeX</i> > pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li><i>theZ</i> is out of range</li>
     *             <li><i>theT</i> is out of range</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnail(Integer, Integer)
     */
    public byte[] getThumbnailForSectionDirect(int theZ, int theT,
                                               Integer sizeX, Integer sizeY);

    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
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
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnailDirect(Integer, Integer)
     */
    public byte[] getThumbnailByLongestSideDirect(Integer size);
    
    /**
     * Retrieves a thumbnail for a pixels set using a given set of rendering
     * settings (RenderingDef) for a particular section. The Thumbnail will 
     * always be created directly, ignoring the on-disk cache. The longest side 
     * of the image will be used to calculate the size for the smaller side in 
     * order to keep the aspect ratio of the original image.
     * 
     * @param theZ the optical section (offset across the Z-axis) to use.
     * @param theT the timepoint (offset across the T-axis) to use.
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>size</i> > pixels.sizeX and pixels.sizeY</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @return a JPEG thumbnail byte buffer.
     * @see #getThumbnailDirect(Integer, Integer)
     */
    public byte[] getThumbnailForSectionByLongestSideDirect(int theZ, int theT,
                                                            Integer size);

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
     *             <li><i>sizeX</i> > pixels.sizeX</li>
     *             <li><i>sizeX</i> is negative</li>
     *             <li><i>sizeY</i> > pixels.sizeY</li>
     *             <li><i>sizeY</i> is negative</li>
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @see #getThumbnail(Integer, Integer)
     * @see #getThumbnailDirect(Integer, Integer)
     */
    public void createThumbnail(Integer sizeX, Integer sizeY);
    
    /**
     * Creates thumbnails for a number of pixels sets using a given set of 
     * rendering settings (RenderingDef) in the on-disk cache. Unlike the 
     * other thumbnail creation methods, this method <b>may</b> be called 
     * without first calling {@link #setPixelsId(long)}. This method <b>will not</b>
     * reset or modify rendering settings in any way. If rendering settings for
     * a pixels set are not present, thumbnail creation for that pixels set
     * <b>will not</b> be performed.
     * 
     * @param size
     *            the size of the longest side of the thumbnail requested.
     *            <code>null</code> specifies the default size of 48.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @throws ApiUsageException
     *             if:
     *             <ul>
     *             <li><i>size</i> > pixels.sizeX and pixels.sizeY</li>
     *             <li><i>size</i> is negative</li>
     *             </ul>
     * @see #createThumbnail(Integer, Integer)
     * @see #createThumbnails()
     */
    public void createThumbnailsByLongestSideSet(Integer size, 
    		@NotNull @Validate(Long.class) Set<Long> pixelsIds);

    /**
     * Creates thumbnails for a pixels set using a given set of rendering
     * settings (RenderingDef) in the on-disk cache for <b>every</b>
     * sizeX/sizeY combination already cached.
     * 
     * @see #getThumbnail(Integer, Integer)
     * @see #getThumbnailDirect(Integer, Integer)
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
     *             <li>{@link #setPixelsId(long)} has not yet been called</li>
     *             </ul>
     * @see #getThumbnail(Integer, Integer)
     * @see #getThumbnailDirect(Integer, Integer)
     */
    public boolean thumbnailExists(Integer sizeX, Integer sizeY);
    
    /**
     * Resets the rendering definition for the active pixels set to its
     * default settings.
     */
    public void resetDefaults();
}
