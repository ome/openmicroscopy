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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class XmlTransform {
	
	// for printing
	// convert from XML to html using an xsl style sheet
	// not used currently due to problems packaging into .jar
	
	public static void transformXMLtoHTML(File xmlFile) {
		
		File htmlFile = new File("print.html");

		URL xslURL = XmlTransform.class.getResource("/xsl/print.xsl");
		
		//System.out.println(xslURL);
		
		File xslFile = new File(xslURL.getFile());
		
		//InputStream xslInputStream = ClassLoader.getSystemResourceAsStream("/xsl/print.xsl");
		
		StreamSource xmlStream = new StreamSource(xmlFile);
		StreamSource xslStream = new StreamSource(xslFile);
		StreamResult htmlStream = new StreamResult(htmlFile);

		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(xslStream);
			transformer.transform(xmlStream, htmlStream);
		} catch (TransformerException ex) {
			ex.printStackTrace();
		}
		
		
		
		String htmlFileName = htmlFile.getName();
		
        File findMyDirectory = new File("");
        String currentDirectory = findMyDirectory.getAbsolutePath();
        
        String outputFilePath = "file://" + currentDirectory + "/" + htmlFileName;
        
        outputFilePath = outputFilePath.replaceAll(" ", "%20");
        
        BareBonesBrowserLaunch.openURL(outputFilePath);
	}

}
