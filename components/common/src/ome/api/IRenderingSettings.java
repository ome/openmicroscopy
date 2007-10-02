/*
 * ome.api.IRenderingSettings
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;


//Java imports
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.model.display.RenderingDef;


/**
 * Provides method to apply rendering settings to a collection of images.
 * All methods will receive the id of the pixels set to copy the rendering
 * settings from.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev: 1187 $ $Date: 2007-01-14 22:36:45 +0000 (Sun, 14 Jan 2007) $) </small>
 * @since 3.0
 */
public interface IRenderingSettings extends ServiceInterface {
	/**
	 * Returns the rendering settings for a given pixels for the current user.
	 * 
	 * @param pixelsId The Id of the <code>Pixels</code>
	 * @return See above.
	 * @throws ValidationException if the image qualified by 
	 * <code>imageId</code> is unlocatable.
	 */
	RenderingDef getRenderingSettings(@NotNull long pixelsId);
	
    /**
     * Resets a image's rendering settings back to those that are specified
     * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * @param imageId The Id of the <code>Image</code>.
	 * @throws ValidationException if the image qualified by 
	 * <code>pixelsId</code> is unlocatable.
     */
	void resetDefaultsToImage(@NotNull long imageId);
	
	/**
     * Resets a category's rendering settings back to those that are specified
     * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * @param categoriesId The Id of the <code>Category</code>.
	 * @throws ValidationException if the image qualified by 
	 * <code>categoryId</code> is unlocatable.
     */
	void resetDefaultsToCategory(@NotNull long categoryId);
	
	/**
     * Resets a dataset's rendering settings back to those that are specified
     * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * @param dataSetId The Id of the <code>DataSet</code>.
	 * @throws ValidationException if the image qualified by 
	 * <code>dataSetId</code> is unlocatable.
     */
	void resetDefaultsToDataSet(@NotNull long dataSetId);
	
	/**
	 * Applies rendering settings to one or many containers. If a container such 
	 * as <code>Dataset</code> is to be copied to, all images within that 
	 * <code>Dataset</code> will have the rendering settings applied.
	 * 
	 * @param <T> The type of object to copy to. <code>Project</code>, 
	 * <code>Dataset</code> and <code>Image</code> are currently supported.
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param toType The type of the object to copy to as also declared by
	 * <code>T</code>
	 * @param to The list of containers to either apply the settings to
	 * directly (<code>Image</code>) or in-directly (<code>Project</code> and
	 * <code>Dataset</code>).
	 * @throws ValidationException if an illegal <code>toType</code> is
	 * passed in or the rendering settings <code>from</code> is unlocatable.
	 */
	<T> void applySettingsToSet(@NotNull long from, Class<T> toType, Set<T> to);
	
	/**
	 * Applies rendering settings to all images in all <code>Datasets</code> 
	 * of a given <code>Project</code>.
	 * Returns a map with two boolean keys. The value of the 
	 * <code>TRUE</code> is a collection of images ID, the settings were 
	 * successfully applied to. The value of the 
	 * <code>FALSE</code> is a collection of images ID, the settings could not 
	 * be applied to. 
	 * 
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the project container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the project <code>to</code> is unlocatable.
	 */
	Map applySettingsToProject(@NotNull long from, @NotNull long to);
	
	/**
	 * Applies rendering settings to all images in a given <code>Dataset</code>. 
	 * Returns a map with two boolean keys. The value of the 
	 * <code>TRUE</code> is a collection of images ID, the settings were 
	 * successfully applied to. The value of the 
	 * <code>FALSE</code> is a collection of images ID, the settings could not 
	 * be applied to. 
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the dataset container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the dataset <code>to</code> is unlocatable.
	 */
	Map applySettingsToDataset(@NotNull long from, @NotNull long to);
	
	/**
	 * Applies rendering settings to all images in all <code>Categories</code>.
	 * Returns a map with two boolean keys. The value of the 
	 * <code>TRUE</code> is a collection of images ID, the settings were 
	 * successfully applied to. The value of the 
	 * <code>FALSE</code> is a collection of images ID, the settings could not 
	 * be applied to. 
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the categories container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the project <code>to</code> is unlocatable.
	 */
	Map applySettingsToCategory(@NotNull long from, @NotNull long to);
	
	/**
	 * Applies rendering settings to a given <code>Image</code>. 
	 * Returns <code>true</code> if the settings were 
	 * successfully applied to, <code>false</code> otherwise.
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the image container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the image <code>to</code> is unlocatable.
	 */
	boolean applySettingsToImage(@NotNull long from, @NotNull long to);
	
	/**
	 * Applies rendering settings to a given <code>Pixels</code>. 
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the pixels container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the pixels<code>to</code> is unlocatable.
	 */
	boolean applySettingsToPixel(@NotNull long from, @NotNull long to);

}
