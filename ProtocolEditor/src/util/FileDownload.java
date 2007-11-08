package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
			byte[] buffer = new byte[8 * 1024];
			
			OutputStream out = new BufferedOutputStream(
					new FileOutputStream(outputFile));
			
			int inputByte;
			while ((inputByte = in.read(buffer)) != -1) {
				out.write(buffer, 0, inputByte);
				System.out.println(inputByte);
			}
			System.out.println("FileDownload file downloaded to " + outputFile.getAbsolutePath());
			
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return outputFile;
		
	}
}

