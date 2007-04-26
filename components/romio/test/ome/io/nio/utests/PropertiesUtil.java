package ome.io.nio.utests;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class PropertiesUtil {
	private static Properties properties;
	
	public PropertiesUtil() {
		properties = new Properties();
	    try {
	        properties.load(new FileInputStream("etc/omero.properties"));
	    } catch (IOException e) {
			// TODO - something
		}
		
	}
    
    public static void main(String[] args) {
    	PropertiesUtil util = new PropertiesUtil();
    	String path = properties.getProperty("omero.data.dir");
    	System.out.println("The path is " + path);
    }

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
