/*
 * org.openmicroscopy.shoola.util.image.io.IconReader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.image.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * Reads ICNS file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class IconReader
{

	/** The extension of an <code>ICNS</code> icon file. */
	public static final String ICNS = "icns";
	
	/** The 16x16 icon. */
	public static final int	ICON_16 = 16;
	
	 /** The 32x32 icon. */
	public static final int ICON_32 = 32;

    /** The 48x48 icon. */
	public static final int ICON_48 = 48;

    /** The 128x128 icon. */
	public static final int ICON_128 = 128;
    
	/** Identifies the 16x16 icon within an <code>ICNS</code> file. */
	private static final String ICON_SMALL_32_BIT_RGB = "is32";
	
	/** Identifies the 16x16 mask icon within an <code>ICNS</code> file. */
	private static final String ICON_SMALL_8_BIT_MASK = "s8mk";

	/** Identifies the 32x32 icon within an <code>ICNS</code> file. */
    private static final String ICON_LARGE_32_BIT_RGB = "il32";
    
    /** Identifies the 32x32 mask icon within an <code>ICNS</code> file. */
    private static final String ICON_LARGE_8_BIT_MASK = "l8mk";

    /** Identifies the 48x48 icon within an <code>ICNS</code> file. */
    private static final String ICON_HUGE_32_BIT_RGB = "ih32";
    
    /** Identifies the 48x48 mask icon within an <code>ICNS</code> file. */
    private static final String ICON_HUGE_8_BIT_MASK = "h8mk";


    /** Identifies the 128x128 icon within an <code>ICNS</code> file. */
    private static final String THUMBNAIL_32_BIT_RGB = "it32";
    
    /** Identifies the 128x128 mask icon within an <code>ICNS</code> file. */
    private static final String THUMBNAIL_8_BIT_MASK = "t8mk";

    /** The length of data to read. */
    private static final int SIZE = 4;
    
	/** The file to decode. */
	private FileInputStream stream;

	/**
	 * Reads the passed array of bytes.
	 * 
	 * @param array The array to handle.
	 * @throws IOException Thrown if an error occurred while reading.
	 */
	private void read(byte[] array)
		throws IOException
	{
		int toRead = array.length;
        int read = 0;
        int n;
        while (read < toRead) {
            n = stream.read(array, read, toRead - read);
            if (n < 0)
                throw new IOException("Value cannot be negative.");
            read += n;
        }
	}
	
	/**
	 * Ignores the passed number of bytes.
	 * 
	 * @param toIgnore The number of bytes to ignore.
	 * @throws IOException Thrown if an error occurred while reading.
	 */
    private void ignore(long toIgnore) 
    	throws IOException
    {
        long ignored = 0;
        long n;
        while (ignored < toIgnore) {
            n = stream.skip(toIgnore-ignored);
            if (n < 0)
                throw new IOException("Value cannot be negative.");
            ignored += n;
        }
    }
    
	/** 
	 * Creates a new instance.
	 * 
	 * @param path The path to the file to decode.
	 */
	public IconReader(String path)
	{
		if (path == null || path.length() == 0)
			throw new IllegalArgumentException("No path specified.");
		try {
			stream = new FileInputStream(path);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to initialize the " +
					"file stream.");
		}
	}
	

	/**
	 * Decodes the passed array of bytes.
	 * 
	 * @param data The data to decode.
	 * @param destination The array hosting the decoded value.
	 * @param size The size of the icon to handle.
	 * @return See above.
	 */
	private int[] decode32bitIcon(byte[] data, int[] destination, int size) {
        int nbPixels = size*size;
        byte[] unpackedData;

        if (data.length == nbPixels*44) {
            unpackedData = data;
        } else {
            unpackedData = new byte[nbPixels*33];
            unpackIconData(data, unpackedData);
        }

        int[] pixels;
        if (destination == null) {
            pixels = new int[nbPixels];
            // Make all pixels opaque
            for (int i = 0; i < pixels.length; i++) 
            	pixels[i] = 0xFF000000;
        } else {
            pixels = destination;
        }

        //assert pixels.length == size *size : "Incorrect pixel buffer size";

        int unpackedIndex = 0;
        for (int i = 0; i < pixels.length; i++) 
            pixels[i] |= (unpackedData[unpackedIndex++] & 0xFF) << 16;
        
        for (int i = 0; i < pixels.length; i++) 
            pixels[i] |= (unpackedData[unpackedIndex++] & 0xFF) << 8;
        
        for (int i = 0; i < pixels.length; i++) 
            pixels[i] |= (unpackedData[unpackedIndex++] & 0xFF);

        return pixels;
    }

	/**
	 * Decodes the passed data.
	 * 
	 * @param data The data to decode.
	 * @param destination The array hosting the decoded value.
	 * @param size The size of the icon to handle.
	 * @return See above.
	 */
    private int[] decode8bitMask(byte[] data, int[] destination, int size)
    {
        int[] pixels;
        int arraySize = size*size;
        if (destination == null) pixels = new int[arraySize];
        else pixels = destination;

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] &= 0x00FFFFFF; 
            pixels[i] |= (data[i] & 0xFF) << 24; 
        }
        return pixels;
    }
    
    /**
     * Unpacked the icon.
     * 
     * @param packedData The packed data.
     * @param unpackedData The unpacked one.
     */
    private void unpackIconData(byte[] packedData, byte[] unpackedData)
    {
        int in = 0;
        int out = 0;
        int h;
        int n;
        while (in < packedData.length && out < unpackedData.length) {
            h = packedData[in++] & 0xFF;
            if ((h & 0x80) == 0) {
                n = h+1;
                System.arraycopy(packedData, in, unpackedData, out, n);
                in += n;
                out += n;
            } else {
                n = h-125;
                byte data = packedData[in++];
                Arrays.fill(unpackedData, out, out+n, data);
                out += n;
            }
        }
    }
    
    /**
     * Returns the size.
     * 
     * @return See above.
     * @throws IOException Thrown if an error occurred while reading.
     */
    private int getSize()
    	throws IOException
    {
    	 byte[] data = new byte[SIZE];
         read(data);
         return ((data[0] & 0xFF) << 24)+((data[1] & 0xFF) << 16)+
         		((data[2] & 0xFF) << 8)+(data[3] & 0xFF);
    }
    
    /**
     * Decodes the <code>ICNS</code> file.
     * 
     * @param iconIndex The type of icon to create.
     * @return See above.
     * @throws IOException Thrown if an error occurred while reading.
     */
    private BufferedImage decodeICNS(int iconIndex)
    	throws IOException
    {
        int[] icon16 = null;
        int[] icon32 = null;
        int[] icon48 = null;
        int[] icon128 = null;

        int fileSize = getSize();
        int left = fileSize-2*SIZE;
        String type;
        int size;
        int dataSize;
        byte[] data;
        while (left > 0) {
            data = new byte[SIZE];
            read(data);
            type = new String(data);
            size = getSize();
            dataSize = size-2*SIZE;
            if (ICON_SMALL_32_BIT_RGB.equals(type)) {
                data = new byte[dataSize];
                read(data);
                icon16 = decode32bitIcon(data, icon16, ICON_16);
            } else if (ICON_LARGE_32_BIT_RGB.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon32 = decode32bitIcon(data, icon32, ICON_32);
            } else if (ICON_HUGE_32_BIT_RGB.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon48 = decode32bitIcon(data, icon48, ICON_48);
            } else if (THUMBNAIL_32_BIT_RGB.equals(type)) {
                // unknown value
                ignore(SIZE);
                data = new byte[dataSize-SIZE];
                read(data);
                icon128 = decode32bitIcon(data, icon128, ICON_128);
            } else if (ICON_SMALL_8_BIT_MASK.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon16 = decode8bitMask(data, icon16, ICON_16);
            } else if (ICON_LARGE_8_BIT_MASK.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon32 = decode8bitMask(data, icon32, ICON_32);
            } else if (ICON_HUGE_8_BIT_MASK.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon48 = decode8bitMask(data, icon48, ICON_48);
            } else if (THUMBNAIL_8_BIT_MASK.equals(type)) {
            	data = new byte[dataSize];
            	read(data);
            	icon128 = decode8bitMask(data, icon128, ICON_128);
            } else {
                ignore(dataSize);
            }

            left -= size;
        }
		
        switch (iconIndex) {
			case ICON_16:
			default:
				return Factory.create(ICON_16, ICON_16, icon16);
			case ICON_32:
				return Factory.create(ICON_32, ICON_32, icon32);
			case ICON_48:
				return Factory.create(ICON_48, ICON_48, icon48);
			case ICON_128:
				return Factory.create(ICON_128, ICON_128, icon128);
		}
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file to decode.
	 */
	public IconReader(File file)
	{
		if (file == null) 
			throw new IllegalArgumentException("No file specified.");
		try {
			stream = new FileInputStream(file);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to initialize the " +
					"file stream.");
		}
	}
    
	/**
	 * Returns the icon identified by the passed type.
	 * 
	 * @param iconIndex One of the constants defined by this class.
	 * @return See above.
	 * @throws IOException If an error occurred while reading the file.
	 */
	public BufferedImage decode(int iconIndex)
		throws IOException
	{
		byte[] data = new byte[SIZE];
        read(data);
        String header = new String(data);
        if (ICNS.equals(header))return decodeICNS(iconIndex);
        return null;
	}

}
