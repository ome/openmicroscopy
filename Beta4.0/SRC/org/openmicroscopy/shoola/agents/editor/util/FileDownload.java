 /*
 * org.openmicroscopy.shoola.agents.editor.util.FileDownload 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.util;

//Java imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class with a static method for downloading a file from URL. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FileDownload 
{

	/**
	 * Downloads a file from a specified URL, creating a new file of 
	 * the specified name. The new file is returned by this method.
	 * Throws an {@link IOException} if there is a problem downloading file. 
	 * 
	 * @param fileUrl  The url to the file.
	 * @param newFileName The name of the file.
	 * @return See above.
	 * @throws IOException Thrown if an exception occured while downloading the
	 * file.
	 */
	public static File downloadFile(String fileUrl, String newFileName) 
		throws IOException
	{
		
		File outputFile = new File(newFileName);
		
		try {
			URL url = new URL (fileUrl);
			InputStream in = url.openStream();
			
			FileWriter fw = new FileWriter(outputFile);
			
			Reader reader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String strLine = "";
			
			strLine = bufferedReader.readLine();
			while(strLine != null) {
				fw.write(strLine);
			    strLine = bufferedReader.readLine();
			}
			fw.close();
			
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}catch (IllegalArgumentException e) {
			throw e;
		}
		return outputFile;
	}
	
}
