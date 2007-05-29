/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.conditions.InternalException;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

/**
 * @author callan
 * 
 */
public class PixelsService extends AbstractFileSystemService {

	/* The logger for this class. */
	private transient static Log log = LogFactory.getLog(PixelsService.class);

	/**
	 * Constructor
	 * 
	 * @param path
	 */
	public PixelsService(String path) {
		super(path);
	}

	/* null plane size constant */
	public static final int NULL_PLANE_SIZE = 64;

	/* null plane byte array */
	public static final byte[] nullPlane = new byte[] { -128, 127, -128, 127,
			-128, 127, -128, 127, -128, 127, // 10
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 20
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 30
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 40
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 50
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 60
			-128, 127, -128, 127 }; // 64

	/**
	 * Creates a PixelBuffer from a Pixels object
	 * 
	 * @param pixels
	 * @return
	 * @throws IOException
	 */
	public PixelBuffer createPixelBuffer(Pixels pixels) throws IOException {
		RomioPixelBuffer pixbuf = new RomioPixelBuffer(getPixelsPath(pixels
				.getId()), pixels);
		initPixelBuffer(pixbuf);
		return pixbuf;
	}

	/**
	 * Get method for PixelBuffer
	 * 
	 * @param pixels
	 * @return PixelBuffer
	 */
	public PixelBuffer getPixelBuffer(Pixels pixels) {
		String path = getPixelsPath(pixels.getId());
		createSubpath(path);
		return new RomioPixelBuffer(path, pixels);
	}

	/**
	 * Initializes the PixelBuffer using a null plane byte array
	 * 
	 * @param pixbuf
	 * @throws IOException
	 */
	private void initPixelBuffer(RomioPixelBuffer pixbuf) throws IOException {
		String path = getPixelsPath(pixbuf.getId());
		createSubpath(path);
		byte[] padding = new byte[pixbuf.getPlaneSize() - NULL_PLANE_SIZE];
		FileOutputStream stream = new FileOutputStream(path);

		for (int z = 0; z < pixbuf.getSizeZ(); z++) {
			for (int c = 0; c < pixbuf.getSizeC(); c++) {
				for (int t = 0; t < pixbuf.getSizeT(); t++) {
					stream.write(nullPlane);
					stream.write(padding);
				}
			}
		}
	}

	/**
	 * Retrieves the bit width of a particular <code>PixelsType</code>.
	 * 
	 * @param type
	 *            a pixel type.
	 * @return width of a single pixel value in bits.
	 */
	public static int getBitDepth(PixelsType type) {
		if (type.getValue().equals("int8") || type.getValue().equals("uint8")) {
			return 8;
		} else if (type.getValue().equals("int16")
				|| type.getValue().equals("uint16")) {
			return 16;
		} else if (type.getValue().equals("int32")
				|| type.getValue().equals("uint32")
				|| type.getValue().equals("float")) {
			return 32;
		} else if (type.getValue().equals("double")) {
			return 64;
		}

		throw new RuntimeException("Pixels type '" + type.getValue()
				+ "' unsupported by nio.");
	}

	/**
	 * Removes files from data repository based on a parameterized List of Long
	 * pixels ids
	 * 
	 * @param pixelsIds -
	 *            Long file keys to be deleted
	 */
	public void removePixels(List<Long> pixelIds) {
		File file;
		boolean success = false;

		for (Iterator iter = pixelIds.iterator(); iter.hasNext();) {
			Long id = (Long) iter.next();

			String pixelPath = getPixelsPath(id);
			file = new File(pixelPath);
			if (file.exists()) {
				success = file.delete();
				if (!success) {
					throw new InternalException("Pixels " + file.getName()
							+ " deletion failed");
				} else {
					if (log.isInfoEnabled()) {
						log
								.info("INFO: Pixels " + file.getName()
										+ " deleted.");
					}
				}
			}
		}

	}
}
