package ome.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class PathUtil {
	
	private final String SPRING_FILE_PATH = "components/romio/resources/beanRefContext.xml"; 
	private static PathUtil instance = null;
	private static Properties properties;
	
	private PathUtil() {
		properties = new Properties();
	    try {
	        properties.load(new FileInputStream("etc/omero.properties"));
	    } catch (IOException e) {
			// TODO - something
		}		
	}
	
	public static PathUtil getInstance() {
		if (instance == null) {
			return new PathUtil();
		} else {
			return instance;
		}
	}
	
	public String getDataFilePath() {
		String path = properties.getProperty("omero.data.dir");				
		return path;
	}

}
