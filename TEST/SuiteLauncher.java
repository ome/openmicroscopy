/*
 * .SuiteLauncher
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

//Java imports
import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Stack;

//Third-party libraries
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//Application-internal dependencies


/** 
 * The entry point to the regression test suite.
 * To run the tests:<br>
 * <code>java RegressionTests</code>
 * <p>Your classpath must contain all <i>jar</i> files in the <i>LIB</i>
 * directory for the above to work (you can alternatively specify the libraries
 * via the <code>-cp</code> option).</p>
 * <p>This tool requires a configuration file, <i>launcher.cfg</i>, to be put
 * in the same directory as the class file.  The main purpose of the
 * configuration file is to specify what test cases are going to be part of
 * the suite.  The test suite is automatically built by composition of all
 * (recognized) test cases within the compiled tree, starting from a
 * specified package.  A recognized test case is a class that extends
 * <code>TestCase</code> and whose file name matches a given pattern.  Also,
 * the configuration file tells whether the suite should be run in the
 * <i>Swing</i> UI or in console-mode.  Open up the configuration file for
 * the details.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class SuiteLauncher
{
	
	/** Points to the configuration file. */
	private static final String		CFG_FILE = "launcher.cfg";
	
	/**
	 * Property name within the configuration file. 
	 * The corresponding configuration value is the name of this regression
	 * test suite.
	 */
	private static final String		RTS_NAME = "RTS_NAME";

	/** 
	 * Property name within the configuration file.
	 * The corresponding configuration value is the base package from which we
	 * start looking for test classes.  If no value is specified, then we scan
	 * all packages.
	 */
	private static final String		BASE_PACKAGE = "BASE_PACKAGE";

	/** 
	 * Property name within the configuration file.
	 * The corresponding configuration value is the regex to recognize a test
	 * case class' name.
	 */
	private static final String		FILE_PATTERN = "FILE_PATTERN";
	
	/** 
	 * Property name within the configuration file.
	 * The corresponding configuration value specifies whether or not the suite
	 * has to be run in the <i>Swing</i> UI.
	 */
	private static final String		SWING_MODE = "SWING_MODE";

	/** Extension of compiled java class files. */
	private static final String		FILE_EXT = ".class";




	/** 
	 * Entry point.
	 * Args as described in the class docs.
	 */
	public static void main(String[] args) 
	{
		SuiteLauncher launcher = new SuiteLauncher();
		launcher.buildSuite();
		launcher.run();
	}
	
	
	
		
	
	/** 
	 * The test suite that we build by composition of all (recognized) test 
	 * cases within the compiled tree, starting from the specified package.
	 */
	private TestSuite       suite;
 
	/** Stack used for navigation through the compiled directory tree. */
	private Stack           dirs;
	
	/**
	 * Character count of the pahtname of the directory containing the
	 * compiled tree. 
	 */
	private int				basePathLength;

	/** 
	 * The filter to select the compiled files that follows the pattern of a 
	 * test suite file name. 
	 */
	private FileFilter      fileFilter;
	
	/** Tells whether or not to run the suite in the Swing ui. */
	private boolean         swingMode;	
		
		
	/**
	 * Reads in the configuration file and figures out the absolute path of
	 * the directory from which we start looking for test case classes. 
	 */
	private SuiteLauncher()
	{
		Properties config = readConfiguration();
		File baseDir = locatePackage(config.getProperty(BASE_PACKAGE));
		suite = new TestSuite(config.getProperty(RTS_NAME));
		dirs = new Stack();
		dirs.push(baseDir);
		basePathLength = locatePackage(null).getPath().length();
		final String filePattern = config.getProperty(FILE_PATTERN);
		fileFilter = new FileFilter() {
			public boolean accept(File f) {
				boolean isDir = f.isDirectory(),
						nameMatches = f.getName().matches(filePattern),
						isNestedClass = (f.getName().indexOf('$') != -1);
				return (isDir || (nameMatches && !isNestedClass));
			}
		};
		swingMode = ("true".equals(config.getProperty(SWING_MODE)));
	}
		
	/**
	 * Helper method to read in the configuration file.
	 * 
	 * @return	A map of name-value pairs built from the configuration file.
	 */
	private Properties readConfiguration()
	{
		Properties config = new Properties();
		try {
			config.load(getClass().getResourceAsStream(CFG_FILE));	
		} catch (Exception e) {
			throw new RuntimeException("Can't read configuration file. ", e);
		}
		return config;
	}
	
	/**
	 * Helper method to locate the specified package on the file system.
	 * This method is called by the constructor to find out what is the
	 * absolute path of the directory from which we start looking for test
	 * case classes.
	 * 
	 * @param pkgName	The <i>FQN</i> of the package. If one out of 
	 * 					<code>"."</code>, <code>null</code>, or the empty
	 * 					string is passed in, we assume the default package.
	 * @return	A {@link File} object that contains the absolute path to the
	 * 			specified package.
	 */
	private File locatePackage(String pkgName)
	{
		File f = null;
		try {
			if (pkgName == null || pkgName.length() == 0)	pkgName = ".";
			String path = getClass().getResource(".").getPath();
			String decoded = URLDecoder.decode(path, "UTF-8");
			f = new File(decoded, pkgName.replace('.', File.separatorChar));
		} catch (Exception e) {
			throw new RuntimeException("Can't locate base package. ", e);
		}
		if (!f.exists() || !f.isDirectory())
			throw new RuntimeException("Can't locate base package: "+f);
		return f;
	}
	
	/** 
	 * Helper method to convert a path into a fully qualified class name.
	 *
	 * @param   f   Path and name of the class file.
	 * @return  The fully qualified name that corresponds to the class file 
	 * 			specified by <code>f</code>. 
	 */
	private String fileToFQN(File f) 
	{
		//The path will always be absolute, b/c the first file pushed into the
		//stack by this class' constructor is absolute.
		StringBuffer buf = new StringBuffer(f.getPath()); 
		
		//Strip base path out (that is path to the compiled dir tree).
		buf.delete(0, basePathLength); 
		
		//Strip name separator, if any, at the start of the string.
		if (buf.charAt(0) == File.separatorChar)	buf.deleteCharAt(0);
		
		//Strip class file extension out.
		buf.delete(buf.lastIndexOf(FILE_EXT), buf.length());
		
		//Replace name separators with '.' and return.  
		String  fqn = buf.toString();
		return fqn.replace(File.separatorChar, '.');
	}
	
	/** 
	 * Adds the test case compiled in the specified class file to the regression 
	 * suite.
	 * All tests within the test case are extracted, composed into a suite and 
	 * the suite is added to the regression suite.
	 *
	 * @param   classFile  A file containing the bytecode of a test case class.
	 */
	private void addSuite(File classFile) 
	{
		String fqn = fileToFQN(classFile);
		try {
			Class c = Class.forName(fqn);
			if (TestCase.class.isAssignableFrom(c))
				suite.addTestSuite(c);
			else
				throw new Exception("Class "+c+" is not a TestCase. ");
		} catch (Exception e) {
			throw new RuntimeException("Can't add "+classFile+
										" to the suite. ", e);
		}
	}
	
	/** 
	 * Builds the regression suite by composition of all (recognized) test 
	 * cases within the compiled tree, starting from the package specified in
	 * the configuration file.
	 * Searches all sub-directories of the starting directory for files that
	 * match the pattern that a test case class' file is expected to follow.
	 * For each matching class file, its corresponding fully qualified name is
	 * used to add the tests within that class to a suite which is then added
	 * to the regression suite.
	 */
	private void buildSuite() 
	{
		int k;
		File f;
		File[] dirContents;
		while (!dirs.empty()) {
			f = (File) dirs.pop();
			dirContents = f.listFiles(fileFilter);
			k = dirContents.length-1;
			while (0<= k) {
				f = dirContents[k--];
				if (f.isDirectory())   dirs.push(f);
				else  //f matches the file pattern and is not a nested class.
					addSuite(f);
			}
		}
	}
	
	/** 
	 * Runs the tests.
	 * Depending on the specified command line option, the tests will be run 
	 * either in console mode (default) or in GUI mode.
	 */
	private void run() 
	{
		if (swingMode) {
			junit.swingui.TestRunner runner = new junit.swingui.TestRunner() {
				//Hack to return the suite that we've already built.
				public Test getTest(String suiteClassName) {
					return suite;  
				}
			};
			runner.start(new String[]{suite.getName()});
		} else
			junit.textui.TestRunner.run(suite);
	}

}
