/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownload {

	// http://trac.openmicroscopy.org.uk/~will/protocolFiles/experiments/arwen_slice_1.exp"
	public static void main (String[] args) {
		
		try {
			downloadFile("http://cvs.openmicroscopy.org.uk/svn/specification/Xml/Working/completesample.xml");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// downloads a url to temp file and returns an absolute path to it
	public static File downloadFile (String fileUrl) throws MalformedURLException{
		
		File outputFile = new File("temp");
		
		try {
			URL url = new URL (fileUrl);
			InputStream in = url.openStream();
			byte[] buffer = new byte[128];
			
			OutputStream out = new BufferedOutputStream(
					new FileOutputStream(outputFile));
			
			 FileWriter fw = new FileWriter(outputFile);
			
			 Reader reader = new InputStreamReader(in);
	         BufferedReader bufferedReader = new BufferedReader(reader);
	         String strLine = "";

	         strLine = bufferedReader.readLine();
	         while(strLine != null)
	          {
	               // System.out.println(strLine);
	                fw.write(strLine);
	               // fw.write("\n");
	               
	                strLine = bufferedReader.readLine();
	          }
	         fw.close();
			
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return outputFile;
		
	}
}

