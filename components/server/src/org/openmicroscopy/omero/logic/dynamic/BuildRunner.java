/*
 * Created on Jun 29, 2005
 */
package org.openmicroscopy.omero.logic.dynamic;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * rather than code generation perhaps is the best thing to use ant flow would
 * be: 1) new SemanticType definition in DB 2) server calls BuildRunner 3) this:
 * a) adds the classes to webapp classpath b) and adds a jar to the repository
 * 4) clients: a) either get individual classes from server (per call) b) or
 * download a new jar (at startup)
 * 
 * Need to be careful that only NEW files are changed (with MD5, dates, etc.)
 * otherwise we may get class cast exceptions.
 * 
 * Trickiest part will be the ant/middlegen/hibertool dependencies. See
 * omero/AntServlet.zip for an example (Tomcat has some of these deps. see:
 * http://jakarta.apache.org/tomcat/tomcat-5.0-doc/class-loader-howto.html )
 * 
 * @author josh
 */
public @BuildTime class BuildRunner {

	public static void main(String[] args) {

		// http://www.onjava.com/pub/a/onjava/2002/07/24/antauto.html?page=2
		//String basePath = "/WEB-INF/classes/"; // TODO Get base of path
		String path = BuildRunner.class.getClassLoader().getResource("generation.xml").getFile();

		// From:
		// http://www-128.ibm.com/developerworks/websphere/library/
		// techarticles/0502_gawor/0502_gawor.html
		File buildFile = new File(path);
		Project p = new Project();

		// Log4j logger also possible. (this stuff goes into catalina.out)
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		try {
			p.fireBuildStarted();
			p.init();
			p.setUserProperty("ant.file", buildFile.getAbsolutePath());
			p.setUserProperty("message", "Overrode message");
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			helper.parse(p, buildFile);
			p.executeTarget(p.getDefaultTarget());
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			p.fireBuildFinished(e);
		}

		/*
		 * to come: from -
		 * http://www.mail-archive.com/log4j-dev@jakarta.apache.org/msg05229.html
		 * Context ctx = new InitialContext(); loggingContextName = (String)
		 * ctx.lookup("java:comp/env/log4j/logging-context");
		 * 
		 * 
		 * And apps set the following in their web.xml...
		 * 
		 * <env-entry> <description>JNDI logging context for this webapp</description>
		 * <env-entry-name>log4j/logging-context</env-entry-name>
		 * <env-entry-value>BarracudaMVC</env-entry-value>
		 * <env-entry-type>java.lang.String</env-entry-type> </env-entry>
		 * 
		 * AND OR Log4JConfigurer (Spring)
		 */
	}
}
