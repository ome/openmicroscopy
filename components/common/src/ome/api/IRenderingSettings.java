/*
 * ome.api.IRenderingSettings
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;


//Java imports
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.IObject;
import ome.model.core.Pixels;
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
     * Checks if the specified sets of pixels are compatible. Returns
     * <code>true</code> if the pixels set is valid, <code>false</code>
     * otherwise. 
     * 
     * @param pFrom
     *            The pixels set to copy the settings from.
     * @param pTo
     *            The pixels set to copy the settings to.
     * @return See above.
     */
    boolean sanityCheckPixels(Pixels pFrom, Pixels pTo);
    
	/**
	 * Returns the default rendering settings for a given pixels for the 
	 * current user.
	 * 
	 * @param pixelsId The Id of the <code>Pixels</code>
	 * @return See above.
	 * @throws ValidationException if the image qualified by 
	 * <code>imageId</code> is unlocatable.
	 */
	RenderingDef getRenderingSettings(@NotNull long pixelsId);
	
    /**
     * Creates a new rendering definition object along with its sub-objects.
     * 
     * @param pixels The Pixels set to link to the rendering definition.
     * @return A new, blank rendering definition and sub-objects. <b>NOTE:</b>
     * the linked <code>Pixels</code> has been unloaded.
     */
	RenderingDef createNewRenderingDef(@NotNull Pixels pixels);

	/**
     * Resets the given rendering settings to those that are specified by the 
     * rendering engine intelligent <i>pretty good image (PG)</i> logic for
     * the pixels set linked to that set of rendering settings. <b>NOTE:</b> 
     * This method should only be used to reset a rendering definition that has
     * been retrieved via {@link IPixels.retrieveRenderingSettings(long)} as
     * it relies on certain objects being loaded. The rendering settings are
     * saved upon completion.
     * 
     * @param def A <code>RenderingDef</code> to reset. It is expected that
     * def.pixels will be <i>unloaded</i> and that the actual linked Pixels set
     * will be provided in the <code>pixels</code> argument.
     * @param pixels The Pixels set for <code>def</code>.
     * @throws ValidationException if the image qualified by 
     * <code>pixelsId</code> is unlocatable.
     * @see resetDefaultsNoSave(RenderingDef)
     */
    void resetDefaults(@NotNull RenderingDef def, @NotNull Pixels pixels);
    
    /**
     * Resets the given rendering settings to those that are specified by the 
     * rendering engine intelligent <i>pretty good image (PG)</i> logic for
     * the pixels set linked to that set of rendering settings. <b>NOTE:</b> 
     * This method should only be used to reset a rendering definition that has
     * been retrieved via {@link IPixels.retrieveRenderingSettings(long)} as
     * it relies on certain objects being loaded. The rendering settings are
     * not saved.
     * 
     * @param def A <code>RenderingDef</code> to reset. It is expected that
     * def.pixels will be <i>unloaded</i> and that the actual linked Pixels set
     * will be provided in the <code>pixels</code> argument.
     * @param pixels The Pixels set for <code>def</code>.
     * @throws ValidationException if the image qualified by 
     * <code>pixelsId</code> is unlocatable.
     * @returns <code>def</code> with the rendering settings reset.
     * @see resetDefaults(RenderingDef)
     */
    RenderingDef resetDefaultsNoSave(@NotNull RenderingDef def,
                                     @NotNull Pixels pixels);
    
	/**
	 * Resets an image's default rendering settings back to those that are 
	 * specified by the rendering engine intelligent <i>pretty good image 
	 * (PG)</i> logic.
	 * 
	 * @param imageId The Id of the <code>Image</code>.
	 * @throws ValidationException if the image qualified by 
	 * <code>pixelsId</code> is unlocatable.
	 */
	void resetDefaultsInImage(@NotNull long imageId);
	
	/**
	 * Resets a category's rendering settings back to those that are specified
	 * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
	 * 
	 * @param categoriesId The Id of the <code>Category</code>.
	 * @return A {@link java.util.Set} of image IDs that have had their 
	 * rendering settings reset. 
	 * @throws ValidationException if the image qualified by 
	 * <code>categoryId</code> is unlocatable.
	 */
	Set<Long> resetDefaultsInCategory(@NotNull long categoryId);
	
	/**
	 * Resets a dataset's rendering settings back to those that are specified
	 * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
	 * @param dataSetId The Id of the <code>DataSet</code>.
	 * 
	 * @return A {@link java.util.Set} of image IDs that have had their 
	 * rendering settings reset. 
	 * @throws ValidationException if the image qualified by 
	 * <code>dataSetId</code> is unlocatable.
	 */
	Set<Long> resetDefaultsInDataset(@NotNull long dataSetId);
	
	/**
	 * Resets a rendering settings back to one or many containers that are 
	 * specified by the rendering engine intelligent <i>pretty good image
	 * (PG)</i> logic.
	 * 
	 * @param nodeIds Ids of the node type.
	 * @return A {@link java.util.Set} of image IDs that have had their 
	 * rendering settings reset. 
	 * @throws ValidationException if the image qualified by 
	 */
	Set<Long> resetDefaultsInSet(Class<IObject> type, @NotNull @Validate(Long.class) Set<Long> noteIds);
	
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
	<T extends IObject> void applySettingsToSet(@NotNull long from, Class<T> toType, @NotNull Set<T> to);
	
	/**
	 * Applies rendering settings to all images in all <code>Datasets</code> 
	 * of a given <code>Project</code>.
	 * Returns a map with two boolean keys. The value of the 
	 * <code>TRUE</code> is a collection of images ID, the settings were 
	 * successfully applied to. The value of the 
	 * <code>FALSE</code> is a collection of images ID, the settings could not 
	 * be applied to. 
	 * 
	 * @param from The Id of the pixels set to copy the rendering settings from.
	 * @param to The Id of the project container to apply settings to.
	 * @return See above.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the project <code>to</code> is unlocatable.
	 */
	Map<Boolean, List<Long>> applySettingsToProject(@NotNull long from, @NotNull long to);
	
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
	Map<Boolean, List<Long>> applySettingsToDataset(@NotNull long from, @NotNull long to);
	
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
	Map<Boolean, List<Long>> applySettingsToCategory(@NotNull long from, @NotNull long to);
	
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
	boolean applySettingsToPixels(@NotNull long from, @NotNull long to);

}
