/*
 * org.openmicroscopy.shoola.env.data.util.OriginalMetadataParser
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import omero.RType;
import omero.cmd.OriginalMetadataResponse;

/**
 * Helper class used to read the content of
 * an <code>OriginalMetadataResponse</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class OriginalMetadataParser
{

	/** The file to handle.*/
	private final File file;
	
	/**
	 * Writes the content of the map to a String.
	 * 
	 * @param map The map to convert.
	 * @param separator Value used to separate key and value.
	 * @return See above.
	 */
	private String writeMap(Map<String, RType> map, String separator)
	{
		if (map == null || map.size() == 0) return null;
		if (CommonsLangUtils.isBlank(separator)) separator = " ";
		TreeSet<String> sortedSet = new TreeSet<String>(map.keySet());
		Iterator<String> j = sortedSet.iterator();
		String key;
		Object v;
		StringBuffer buffer = new StringBuffer();
		while (j.hasNext()) {
			key = j.next();
			buffer.append(key);
			buffer.append(separator);
			v = ModelMapper.convertRTypeToJava(map.get(key));
			if (v instanceof List) {
				List<Object> l = (List<Object>) v;
				Iterator<Object> k = l.iterator();
				while (k.hasNext()) {
					buffer.append(k.next());
					buffer.append(" ");
				}
			} else if (v instanceof Map) {
				Map<String, Object> l = (Map<String, Object>) v;
				Entry<String, Object> e;
				Iterator<Entry<String, Object>> k = l.entrySet().iterator();
				while (k.hasNext()) {
					e = k.next();
					buffer.append(e.getKey());
					buffer.append(separator);
					buffer.append(e.getValue());
				}
			} else buffer.append(v.toString());
			buffer.append(System.getProperty("line.separator"));
		}
		return buffer.toString();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file to write the content into
	 */
	public OriginalMetadataParser(File file)
	{
		if (file == null)
			throw new IllegalArgumentException("No file to write the " +
					"content to.");
		this.file = file;
	}
	
	/**
	 * Reads the content of the response and writes it to the file.
	 * 
	 * @param response The response to handle.
	 * @throws Exception The exception thrown if an error occurred while
	 * reading/writing.
	 */
	public void read(OriginalMetadataResponse response)
		throws Exception
	{
		read(response, null);
	}
	
	/**
	 * Reads the content of the response and writes it to the file.
	 * 
	 * @param response The response to handle.
	 * @param separator Value used to separate key and value.
	 * @throws Exception The exception thrown if an error occurred while
	 * reading/writing.
	 */
	public void read(OriginalMetadataResponse response, String separator)
		throws Exception
	{
		Writer writer = null;
		BufferedWriter bufferWriter = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
			bufferWriter = new BufferedWriter(writer);
			StringBuffer buffer = new StringBuffer();
			String value;
			
			value = writeMap(response.globalMetadata, separator);
			if (value != null) {
				buffer.append("[GlobalMetadata]");
				buffer.append(System.getProperty("line.separator"));
				buffer.append(writeMap(response.globalMetadata, separator));
				buffer.append(System.getProperty("line.separator"));
			}
			value = writeMap(response.seriesMetadata, separator);
			if (value != null) {
				buffer.append("[SeriesMetadata]");
				buffer.append(System.getProperty("line.separator"));
				buffer.append(writeMap(response.seriesMetadata, separator));
			}
			bufferWriter.write(buffer.toString());
		} catch (Exception e) {
			throw new Exception("An error while reading/writing the content.");
		} finally {
		    if (bufferWriter != null) {
		        bufferWriter.close();
		    }
		    if (writer != null) {
		        writer.close();
		    }
		}
	}
}
