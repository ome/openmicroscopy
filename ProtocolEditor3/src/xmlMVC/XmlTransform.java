package xmlMVC;

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
