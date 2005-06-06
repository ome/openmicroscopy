/*
 * Created on Jul 21, 2004
 */
package org.openmicroscopy.omero.tests.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/** provides all objects needed for working with the meta package 
 * within the Spring Framework.
 * @author moore
 */
public class SpringTestHarness {

    private final static String files[] = new String[]{"test.xml"};
    public static String paths[] = new String[files.length];
    public static ApplicationContext ctx;
    
    static {
        for (int i = 0; i < files.length; i++) {
            	paths[i] = SpringTestHarness.class.getClassLoader().getResource(files[i]).toString();    
        }
        ctx = new FileSystemXmlApplicationContext(paths);
    }
	
    
}
