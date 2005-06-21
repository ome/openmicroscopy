/*
 * Created on Feb 7, 2005
*/
package org.openmicroscopy.omero.logic;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.texen.ant.TexenTask;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author josh
 * @deprecated
*/
public class ModelUtilsTexenTask extends TexenTask {

    private static Log log = LogFactory.getLog(ModelUtilsTexenTask.class);
    
    ApplicationContext ctx = null;
    Context context = new VelocityContext();
	InputStream template = null;
	StringWriter writer = null;
	String outputDirectory = "/tmp";
	String templateDirectory = "/tmp";

	public ModelUtilsTexenTask(){
	    log.debug("Texen task started");
	}
	
    protected void populateInitialContext(Context context)
    	throws java.lang.Exception {
        log.debug("Starting initialization...");
        super.populateInitialContext(context);
        log.debug("Loading Spring...");
        this.loadSpring();
        log.debug("Loading Context....");
        this.context = context;
        this.loadContext();
        log.debug("Context loaded.");
    }
	
	public static void main(String[] args) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
		ModelUtilsTexenTask m = new ModelUtilsTexenTask(args);
		m.writer = new StringWriter();
		m.loadTemplate();
		m.loadSpring();
		m.loadContext();
		m.generate();
	}

	public ModelUtilsTexenTask(String[] args) throws Exception {
		/* first, we init the runtime engine. Defaults are fine. */
		Velocity.init();
		
		if (0 < args.length && null != args[0] ){
			outputDirectory = args[0];
		}
	}

	public void loadSpring(){
		ctx = new ClassPathXmlApplicationContext(
	            new String[] {"WEB-INF/data.xml", "WEB-INF/dao.xml","WEB-INF/test/config-test.xml"});
	}
	
	public void loadContext() {
	
		SessionFactory sessions = (SessionFactory) ctx.getBean("sessionFactory");
		Map metas = sessions.getAllClassMetadata();
		context.put("classes",(String[]) metas.keySet().toArray(new String[metas.keySet().size()]));
		context.put("sessions",sessions);
		context.put("task",this);
	}

	/**
	 * the real implementation of templates should either
	 * 
	 * 1) be generated as a String from comments in the OWL file or 2) be
	 * multiply evaluated like macros
	 * 
	 * @throws Exception
	 */
	public void loadTemplate() {
		try {
		    template = this.getClass().getClassLoader().getResourceAsStream("Control.vm");
		} catch (Exception e){
		    throw new RuntimeException("No file found: modelUtils");
		}
	}

	
	public void generate() throws ResourceNotFoundException,
			ParseErrorException, MethodInvocationException, Exception {

		Velocity.evaluate(context, writer, "OMERO GENERATION", template);
		System.out.println("***************************************");
		System.out.println(writer);
		
	}
	
	public String nopkg(String className){
	    String[] parts = className.split("[.]");
	    return parts[parts.length-1];
	}

}