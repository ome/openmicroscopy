/*
 * Created on Jul 21, 2004
 */
package org.ome.tests.srv;

import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/** provides all objects needed for working with the meta package 
 * within the Spring Framework.
 * @author moore
 */
public class SpringTestHarness {

    private final static String springConfFile = "config-test.xml";
    private final static String springCtxFile = "spring.xml";
    public static URL ctxPath,confPath;
    public static ApplicationContext ctx;
    
    static {
        ctxPath = SpringTestHarness.class.getClassLoader().getResource(springCtxFile);
        confPath = SpringTestHarness.class.getClassLoader().getResource(springConfFile);
        if (ctxPath==null||confPath==null){
            throw new RuntimeException("Can't find spring conf files:" +springCtxFile+","+springConfFile);
        }
        ctx = new FileSystemXmlApplicationContext( new String[]{confPath.toString(),ctxPath.toString()});
        
    }
	
    
}
