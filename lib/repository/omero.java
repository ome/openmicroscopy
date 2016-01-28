/*
 * omero
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Properties;

/** 
 * The Build Tool.
 * This class is in charge of launching Ant with a suitable environment.
 * <p>The workflow is (in principle) identical to the Ant Launcher class (see
 * <code>org.apache.tools.ant.launch.Launcher</code>): set up the required
 * system properties, locate all the required libraries (including the JDK
 * tools), and launch Ant with a suitable classpath and with whatever arguments
 * were supplied on the command line.</p>
 * <p>The implementation however is quite different.  In fact, on one hand we
 * know exactly how the tool is deployed and run, so some aspects of the 
 * launch procedure can be simplified.  On the other hand we tailor this
 * procedure to the needs of our build system.</p>
 * <p>The launch procedure assumes the following:</p>
 * <ul>
 *  <li>This class is compiled in the <i>build</i> directory under the
 *      Shoola CVS root.  Its file name is <i>"omero.class"</i> and it 
 *      belongs to the default namespace (unnamed package).</li>
 *  <li>The <i>build</i> directory contains a sub-directory named <i>tools</i>.
 *      The latter contains the Ant core jars, the Ant jars for the optional 
 *      tasks that we use, and all the external libraries required for those
 *      optional tasks.  Optionally, it may contain the Sun JDK tools jar.
 *      (Because of licensing issues, we can't add the <i>tools.jar</i> to CVS;
 *      however developers may want to drop that file into this directory after 
 *      downloading from CVS.)</li>
 *  <li>The Sun JDK tools can be accessed because the classes:
 *    <ul>
 *     <li>Are in the default classpath (boot, ext) of the JVM that is used to 
 *         invoke the Build Tool, or</li>
 *     <li>Are contained in a jar file named <i>"tools.jar"</i> under 
 *         <i>{java.home}/lib</i> (JDK installed; if {java.home} points to the
 *         <i>jre</i> directory within the JDK, then the jar is expected to be 
 *         in the <i>lib</i> sibling directory), or</li>
 *     <li>Are contained in a jar file under the <i>build/tools</i> directory.
 *         In this case, the version of the JDK tools is expected to be the
 *         same as the one of the JVM that is used to invoke the Build Tool.  
 *         (Failure to comply may result in compilation errors due to class 
 *         file version incompatibilities.)</li>
 *    </ul>
 *  </li>
 *  <li>The Build Tool is invoked with the following syntax: <code>java build 
 *      [options] [target1 [target2 [target3] ...]]</code>, where 
 *      <code>options</code> are any of the Ant options and <code>targetN</code>
 *      is any of the available targets.  Note that we assume no classpath is
 *      ever specified to <code>java</code>.</li>
 * </ul>
 * <p>A final important remark.  Before dropping the JDK tools into
 * <i>build/tools</i>, you should make sure they're not available to the JVM
 * in use.  (Just run the Build Tool to compile, if it fails the JDK tools are
 * not available.)  This is important because if the JDK tools are already
 * available, adding a copy under <i>build/tools</i> would result in having
 * two sets of JDK tools classes on the classpath.  As you can imagine, some
 * nasty runtime behavior could originate from that if the JDK tools have
 * different versions.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class omero
    implements FileFilter
{

    /** 
     * The <i>build</i> directory under the Shoola CVS root.
     * This is the directory where this class is compiled.
     */
    private File    buildDir;
    
    /** 
     * The <i>tools</i> directory under {@link #buildDir}.
     * This directory contains all the libraries required by the Build Tool,
     * except the JDK tools which can be added after downloading from CVS.  
     */
    private File    toolsDir;
    
    /**
     * Points to the Sun JDK tools jar file, if any, in the java installation
     * directory.
     * This is the installation directory of the JVM used to launch the build 
     * tool and is given by the <i>java.home</i> system property.
     * Note that this file may not exist.  (For example if the Sun JDK is not
     * installed or the JVM used to run the Build Tool is not part of the JDK.)
     */
    private File    jdkTools;
    
    
    /**
     * Locates the needed filesystem entries and initializes the corresponding
     * fields.
     * We throw an exception if the <i>build</i> and <i>tools</i> directories
     * can't be located from the position of the <code>build</code> class on 
     * the filesystem.  We try and guess the position of the Sun JDK tools, but
     * we don't check whether this file actually exists.  In fact, the JDK tools
     * classes may turn out to be available to the current JVM by some other
     * means.  (For example, they are already in the bootclasspath or the tools
     * jar file has been dropped into our <i>tools</i> directory.)
     * 
     * @throws Exception If <i>build</i> and <i>tools</i> directories can't be
     *                   located.
     */
    private omero()
        throws Exception
    {
        //First locate the build directory.
        //We assume this class always belongs to the default package and
        //is compiled in the build directory.
        URL url = omero.class.getClassLoader().getResource("omero.class");
        if (url == null) throw new Exception("Can't locate omero class.");
        File build = new File(new URI(url.toExternalForm()));
        buildDir = build.getParentFile();
        if (!buildDir.exists() || !buildDir.isDirectory())
            throw new Exception("Invalid location: "+buildDir.getAbsolutePath()+
                    " is not a directory.");
        
        //Now locate the tools sub-directory.
        toolsDir = new File(buildDir, "lib");
        if (!toolsDir.exists() || !toolsDir.isDirectory())
            throw new Exception("Invalid location: "+toolsDir.getAbsolutePath()+
                    " is not a directory.");
        
        //Finally guess the location of the JDK tools.jar file.
        String javaHome = System.getProperty("java.home");
        if (javaHome.toLowerCase(Locale.US).endsWith("jre"))
            javaHome = javaHome.substring(0, javaHome.length()-4);
        jdkTools = new File(javaHome+"/lib/tools.jar");
    }
    
    /**
     * Tests for availability of the JDK tools in the JVM default classpath.
     * (Recall that the Build Tool is invoked like: java build, so no -cp
     * is ever specified.)
     * 
     * @return <code>true</code> if the JDK tools are found, <code>false</code>
     *          otherwise.
     */
    private boolean isJDKToolsAvailable()
    {
        String[] main = {"com.sun.tools.javac.Main", "sun.tools.javac.Main"};
        for (int i = 0; i < main.length; ++i)
           try {
               Class.forName(main[i]);
               return true;
           } catch (ClassNotFoundException cnfe) {}
        return false;
    }
    
    /**
     * Locates all the libraries required by the Build Tool.
     * We consider <i>any jar</i> file in <i>build/tools</i> under the
     * Shoola CVS root to be a library required by the Build Tool.
     * Moreover, if the JDK tools are not already in the JVM default classpath,
     * we try and grab the <i>tools.jar</i> file and then add it to the 
     * returned array.  This could potentially be a problem if a jar file
     * containing the JDK tools is also present in <i>build/tools</i>.  In this
     * case we would end up with two sets of JDK tools classes on the classpath,
     * which could originate some nasty runtime behavior if the JDK tools have 
     * different versions.
     * 
     * @return A file object for each jar file found in <i>build/tools</i>.
     * @throws Exception If no libraries were found in <i>build/tools</i>.
     */
    private File[] locateBuildJars()
        throws Exception
    {
        File[] jars = new File[8];
	jars[0] = new File("lib/repository/junit-3.8.1.jar");
	jars[1] = new File("lib/repository/ant-1.8.0.jar");
	jars[2] = new File("lib/repository/ant-launcher-1.8.0.jar");
	jars[3] = new File("lib/repository/ant-junit-1.8.0.jar");
	jars[4] = new File("lib/repository/ant-trax-1.8.0.jar");
	jars[5] = new File("lib/repository/ant-nodeps-1.8.0.jar");
	jars[6] = new File("lib/repository/ant-contrib-1.0b3.jar");
	jars[7] = new File("lib/repository/bsh-2.0b4.jar");

        if (!isJDKToolsAvailable()) {  //Try and grab tools.jar.
            if (jdkTools.exists()) {  //Add it to the other jars.
                File[] tmp = new File[jars.length+1];
                System.arraycopy(jars, 0, tmp, 0, jars.length);
                tmp[jars.length] = jdkTools;
                jars = tmp;
            }
            //Else we assume the JDK tools are in a jar under build/tools.
        }
        //Else the jdk tools are either in the bootclasspath or in the ext.
        //In fact, the Build Tool is never invoked w/ -cp.
        
        return jars;
    }

    String mavenPath(String grp, String art, String ver){
	return "lib/repository/"+grp+"/"+art+"/"+ver+"/"+art+"-"+ver+".jar";
    }
    
    /**
     * Sets the classpath to the libraries required by the Build Tool.
     * 
     * @param buildJars The list of the required libraries.
     */
    private void setClasspath(File[] buildJars)
    {
        StringBuffer cp = new StringBuffer();
        for (int i = 0; i < buildJars.length; ++i) {
            cp.append(buildJars[i].getAbsolutePath());
            cp.append(File.pathSeparator);
        }
        System.setProperty("java.class.path", cp.toString());
        //The above wipes out the current classpath.  This is not a problem 
        //b/c the Build Tool is never invoked w/ -cp and . contains no other
        //classes besides this one.
    }
    
    private void setSystemProperties()
    {
        //Set Ant properties.
        System.setProperty("ant.home", toolsDir.getAbsolutePath());
        System.setProperty("ant.library.dir", toolsDir.getAbsolutePath());
        System.setProperty("build.sysclasspath","last"); // or first, ignore, only
        // This solves the problem of users having conflicting
        // versions on their CLASSPATH
    }

    /**
     * Sets the environment and then starts Ant with the specified command line.
     *  
     * @param args Ant command line.
     * @throws Exception If the Build Tool can't be started.
     */
    private void startAnt(String[] args)
        throws Exception
    {
        try {
            //Set system properties, then set classpath and replace classloader
            //with one that is aware of the new classpath.
            setSystemProperties();
            File[] buildJars = locateBuildJars();
            setClasspath(buildJars);
            URL[] buildJarsURLs = new URL[buildJars.length];
            for (int i = 0; i < buildJarsURLs.length; i++)
                buildJarsURLs[i] = buildJars[i].toURL();
            URLClassLoader loader = new URLClassLoader(buildJarsURLs);
            Thread.currentThread().setContextClassLoader(loader);
           
	    //Emacs logging
	    String[] tmp = new String[args.length+3];
            System.arraycopy(args, 0, tmp, 0, args.length);
            tmp[args.length] 	= "-emacs";
	    tmp[args.length+1] 	= "-logger";
	    tmp[args.length+2]	= "org.apache.tools.ant.NoBannerLogger";
            args = tmp;

            //Load and start Ant.
            Class mainClass = loader.loadClass("org.apache.tools.ant.Main");
            Method entry = mainClass.getMethod("startAnt", 
                                new Class[] {String[].class, Properties.class, 
                                             ClassLoader.class});
            Object antMain = mainClass.newInstance();
            entry.invoke(antMain, new Object[] {args, null, null});
        } catch (Throwable t) {
            throw new Exception("Couldn't start the Build Tool. \n"+
                                t.getMessage(),t);
        }
    }

    /**
     * Grabs every file with a jar extension.
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname)
    {
        String name = pathname.getName();
        if (name.toLowerCase().endsWith(".jar")) return true;
        return false;
    }
    
    /**
     * Build Tool entry point.
     * 
     * @param args Any valid Ant options and targets.
     * @throws Exception If the Build Tool can't be started.
     */
    public static void main(String[] args) 
        throws Exception
    {
        omero launcher = new omero();
        launcher.startAnt(args);
    }
    
}
