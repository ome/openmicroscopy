/*
 * Created on Jun 29, 2005
 */
package org.openmicroscopy.omero.logic.dynamic;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.launch.Launcher;


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
public @BuildTime
class BuildRunner {

	public static void main(String[] args) {
		BuildRunner.run("generation.xml");
	}

	public static URL getPath(String fileName) {
		return BuildRunner.class.getClassLoader().getResource(fileName);
	}

	public static String getBase(String path){
		return path.substring(0,path.lastIndexOf(File.separator));
	}	

	public static void launch(String fileName) {
		URL url = getPath(fileName);
		String path = url.getFile();
		String base = getBase(path);
		String args[] = new String[] { "-f", path, "-d", "-Dbasedir=" + base };
		Launcher.main(args);
	}

	public static void load(String fileName) {
		URL url = getPath(fileName);
		String classes = getBase(url.getPath())+"/";
		String libs = classes.substring(0,classes.lastIndexOf("target"));

		String[] jars = new String[] { 
				"st/lib/middlegen/middlegen-2.1.jar",
				"st/lib/middlegen/middlegen-hibernate-plugin-2.1.jar",
				"st/lib/middlegen/log4j-1.2.7.jar", "middlegen/velocity-1.4-dev.jar",
				"st/lib/middlegen/commons-collections-2.1.jar",
				"st/lib/hibernate3/hibernate-tools.jar", "hibernate3/hibernate3.jar",
				"st/lib/hibernate3/commons-logging-1.0.4.jar",
				"st/lib/hibernate3/dom4j-1.6.jar",
				"st/lib/hibernate3/commons-collections-2.1.1.jar",
				"st/lib/hibernate3/velocity-1.4.jar",
				"st/lib/hibernate3/velocity-tools-generic-1.1.jar"};
//				"st/lib/ant/ant-1.6.5.jar", 
//				"st/lib/ant/ant-launcher-1.6.5.jar" };

		AntClassLoader loader = new AntClassLoader(null,false);
		for (String jar : jars){
			loader.addPathElement(libs+jar);
		}
		loader.addPathElement(classes);
		loader.addLoaderPackageRoot("middlegen");
		loader.addJavaLibraries();
		
//		URL[] urls = new URL[jars.length + 1];
//
//		try {
//			
//			urls[jars.length] = new URL("file://"+classes);
//			
//			for (int i = 0; i < jars.length; i++) {
//				urls[i] = new URL("file://"+libs + jars[i]);
//
//			}
//			
//		} catch (MalformedURLException e) {
//			throw new RuntimeException("Jar file not found.");
//		}
//		URLClassLoader loader = new URLClassLoader(urls) {
//			@Override
//			protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//
//				Class c = null;
//				
//				c = super.findLoadedClass(name);
//				if (c != null) {
//					System.out.println("xxxxxxxxxxxxxxxxxxxxxxxAlready loaded "+name+" in urls.");
//					return c;
//				}
//				
//				try {
//					c = super.findClass(name);
//					System.out.println("xxxxxxxxxxxxxxxxxxxxxxxFound "+name+" in urls.");
//				} catch (Exception e){
//					if (c == null) 
//						c = super.loadClass(name,resolve);	
//					System.out.println("xxxxxxxxxxxxxxxxxxxxxxxDefault "+name+" used.");
//				}
//				
//				return c;
//			}
//		};

		Class klass;
		String klassName = "ome.Ant";
		
		try {
			//klass = loader.loadClass(klassName);
			klass = loader.forceLoadClass(klassName);
		} catch (Exception ex) {
			throw new RuntimeException("Can't load class: "+klassName);
		}
		
		Method run;
		try {
			run = klass.getMethod("run",new Class[]{String.class});
			run.invoke(null,new Object[]{url.getFile()});
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unimplemented exception.",e);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unimplemented exception.",e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unimplemented exception.",e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unimplemented exception.",e);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unimplemented exception.",e);
		}
		
		
		
	}

	public static void run(String fileName) {
		// http://www.onjava.com/pub/a/onjava/2002/07/24/antauto.html?page=2
		// String basePath = "/WEB-INF/classes/"; // TODO Get base of path
		String path = getPath(fileName).getFile();

		// From:
		// http://www-128.ibm.com/developerworks/websphere/library/
		// techarticles/0502_gawor/0502_gawor.html
		File buildFile = new File(path);
		Project p = new Project();

		// Log4j logger also possible. (this stuff goes into catalina.out)
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_DEBUG);
		p.addBuildListener(consoleLogger);

		try {
			p.fireBuildStarted();
			p.init();
			p.setBasedir("./st");// FIXME
			System.setProperty("build.sysclasspath", "ignore");
			p.setUserProperty("build.sysclasspath", "ignore");
			p.setUserProperty("ant.file", buildFile.getAbsolutePath());
			p.setUserProperty("message", "Overrode message");
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			helper.parse(p, buildFile);
			// p.executeTarget(p.getDefaultTarget());
			p.executeTarget("ant");
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
	
	/** Special loader that just knows how to find TEST_RESOURCE. */
	private static final class ParentLoader extends ClassLoader {

	    public ParentLoader() {}

	}
	
}

