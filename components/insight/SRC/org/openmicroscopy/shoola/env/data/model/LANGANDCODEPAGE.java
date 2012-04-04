package org.openmicroscopy.shoola.env.data.model;

import com.sun.jna.Pointer;

/**
 * Structure used to store enumerated languages and code pages on windows
 * @author Scott Littlewood
 *
 */
public class LANGANDCODEPAGE extends com.sun.jna.Structure {
	public short wLanguage;
	public short wCodePage;

	public LANGANDCODEPAGE(Pointer p) {
		super(p);
	}
}