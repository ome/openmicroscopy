package ome.api;

import java.util.List;
import java.util.Set;

import ome.model.display.RenderingDef;

public interface IRenderingSettings extends ServiceInterface {
	/**
	 * Returns the rendering settings for a given pixels for the current user.
	 * 
	 * @param pixelsId The Id of the <code>Pixels</code>
	 * @return See above.
	 * @throws ValidationException if the image qualified by 
	 * <code>imageId</code> is unlocatable.
	 */
	RenderingDef getRenderingSettings(long pixelsId);
	
    /**
     * Resets a pixels' rendering settings back to those that are specified
     * by the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * @param pixelsId The Id of the <code>Pixels</code>.
	 * @throws ValidationException if the image qualified by 
	 * <code>pixelsId</code> is unlocatable.
     */
	void resetDefaults(long pixelsId);
	
	/**
	 * Applies rendering settings to one or many containers. If a container such 
	 * as <code>Dataset</code> is to be copied to, all images within that 
	 * <code>Dataset</code> will have the rendering settings applied.
	 * 
	 * @param <T> The type of object to copy to. <code>Project</code>, 
	 * <code>Dataset</code> and <code>Image</code> are currently supported.
	 * @param from The Id of the rendering settings object to copy settings
	 * from.
	 * @param toType The type of the object to copy to as also declared by
	 * <code>T</code>
	 * @param to The list of containers to either apply the settings to
	 * directly (<code>Image</code>) or in-directly (<code>Project</code> and
	 * <code>Dataset</code>).
	 * @throws ValidationException if an illegal <code>toType</code> is
	 * passed in or the rendering settings <code>from</code> is unlocatable.
	 */
	<T> void applySettingsToSet(long from, Class<T> toType, Set<T> to);
	
	/**
	 * Applies rendering settings to all images in all <code>Datasets</code> 
	 * of a given <code>Project</code>.
	 * 
	 * @param from The Id of the rendering settings object to copy settings
	 * from.
	 * @param to The Id of the project container to apply settings to.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the project <code>to</code> is unlocatable.
	 */
	Set applySettingsToProject(long from, long to);
	
	/**
	 * Applies rendering settings to all images in a given <code>Dataset</code>. 
	 * 
	 * @param from The Id of the rendering settings object to copy settings
	 * from.
	 * @param to The Id of the dataset container to apply settings to.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the dataset <code>to</code> is unlocatable.
	 */
	Set applySettingsToDataset(long from, long to);
	
	/**
	 * Applies rendering settings to a given <code>Image</code>. 
	 * 
	 * @param from The Id of the rendering settings object to copy settings
	 * from.
	 * @param to The Id of the image container to apply settings to.
	 * @throws ValidationException if the rendering settings <code>from</code> 
	 * is unlocatable or the image <code>to</code> is unlocatable.
	 */
	boolean applySettingsToPixel(long from, long to);

}
