/*
 * Created on Jul 21, 2004
 */
package org.ome.omero.tests;

import java.net.URL;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.context.security.SecureContextImpl;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/** provides all objects needed for working with the meta package 
 * within the Spring Framework.
 * @author moore
 */
public class SpringTestHarness {

    private final static String[] springConfFiles = new String[]{"config-test.xml","spring.xml"};//TODO,"security.xml"};
    private static URL[] ctxURLs = new URL[springConfFiles.length];
    private static String[] ctxPaths = new String[springConfFiles.length];
    
    
    public static ApplicationContext ctx;
    
    static {
        for (int i = 0; i < springConfFiles.length; i++) {
            ctxURLs[i]= SpringTestHarness.class.getClassLoader().getResource(springConfFiles[i]);
            if (ctxURLs[i]==null){
                throw new RuntimeException("Can't find spring conf file:" + springConfFiles[i]);
            }
            
        }
        for (int i = 0; i < ctxURLs.length; i++) {
            ctxPaths[i]=ctxURLs[i].toString();
        }
        ctx = new FileSystemXmlApplicationContext( ctxPaths );
        
    }
    
    static {
        setAdminAuth();
    }
    
    public static void setUserAuth(){
        Authentication auth = 
            new UsernamePasswordAuthenticationToken(
                "Josh","Moore");
        setAuth(auth);
    }
    
    public static void setAdminAuth(){
        Authentication auth = 
            new UsernamePasswordAuthenticationToken(
                "admin","admin");
        setAuth(auth);
    }
    
    public static void setAuth(Authentication auth){
        SecureContext secureContext = new SecureContextImpl();
        secureContext.setAuthentication(auth);
        ContextHolder.setContext(secureContext);
    }
	
    
}
