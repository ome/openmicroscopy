/*
 * Created on Jun 29, 2005
 */
package ome;

import java.io.File;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.openmicroscopy.omero.logic.dynamic.BuildTime;

public @BuildTime class Ant {

	/* taken from BuildRunner. see comments there. */
	public static void run(String path){

		File buildFile = new File(path);
		Project p = new Project();
		
		AntClassLoader loader = (AntClassLoader) Ant.class.getClassLoader();
		System.out.println(loader);
		
		// Log4j logger also possible. (this stuff goes into catalina.out)
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_DEBUG);
		p.addBuildListener(consoleLogger);

		try {
			p.fireBuildStarted();
			p.init();
			p.setBasedir("./st");//FIXME
			System.setProperty("build.sysclasspath","ignore");
			p.setUserProperty("build.sysclasspath","ignore");
			p.setUserProperty("ant.file", buildFile.getAbsolutePath());
			p.setUserProperty("message", "Overrode message");
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			helper.parse(p, buildFile);
			//p.executeTarget(p.getDefaultTarget());

			p.executeTarget("ant");
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			p.fireBuildFinished(e);
		}

	}

}
