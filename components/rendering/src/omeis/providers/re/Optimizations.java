/*
 * omeis.providers.re.Optimizations
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

/**
 * A container for any potential optimizations that the renderer can enable
 * based on the data and rendering settings it has been fed.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackat.ca</a>
 */
public class Optimizations
{
	/** The channel bindings are only "primary" colors. (Red, Green or Blue) */
	public boolean primaryColorEnabled = false;
	
	/** We can do alphaless rendering */
	public boolean alphalessRendering = false;

	/**
	 * Enable or disable the primary color optimization.
	 * @param enabled whether or not to enable the primary color optimization.
	 */
	public void setPrimaryColorEnabled(boolean enabled)
	{
		primaryColorEnabled = enabled;
	}
	
	/**
	 * Returns <code>true</code> if the primary color optimization is enabled,
	 * and <code>false</code> if it is not.
	 * @return See above.
	 */
	public boolean isPrimaryColorEnabled()
	{
		return primaryColorEnabled;
	}
	
	/**
	 * Enable or disable the alphaless rendering optimization. If 
	 * <code>enabled</code> is <code>false</code> it will also disable primary
	 * color rendering.
	 * @param enabled whether or not to enable the alphaless rendering
	 * optimization.
	 */
	public void setAlphalessRendering(boolean enabled)
	{
		if (enabled == false)
			setPrimaryColorEnabled(false);
		alphalessRendering = enabled;
	}
	
	/**
	 * Returns <code>true</code> if the alphaless rendering optimization is
	 * enabled, and <code>false</code> if it is not.
	 * @return See above.
	 */
	public boolean isAlphalessRendering()
	{
		return alphalessRendering;
	}
}
