/*
 * RegressionTests
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

/*------------------------------------------------------------------------------
 *
 * Written by:     Jean-Marie Burel     <j.burel@dundee.ac.uk>
 *                      Andrea Falconi         <a.falconi@dundee.ac.uk>
 *
 *------------------------------------------------------------------------------
 */


//Java imports
import java.io.File;
import java.io.FileFilter;
import java.util.Stack;

//Third-party libs
import junit.framework.*;
import junit.textui.*;
import junit.swingui.*;


/** The entry point to the regression test suite.
 * To run the tests:<br>
 * <code>java RegressionTests</code>
 * <p>Your classpath must contain all Shoola's required libraries for the above to work (you can also 
 * specify the libraries via the <code>-cp</code> option).</p>
 * <p>If you want to run the above command in a directory another than the root directory where
 * the compiled tree sits, you can do so by specifying the path (either absolute or relative) to the
 * compiled tree on the command line (<code>... RegressionTests path/to/compiled/tree</code>). The 
 * suite is run in console-mode by default. You can change this through a command line option:
 * <br>
 * <code>java RegressionTests -ui</code>
 * </p>
 * <p>The test suite is automatically built by composition of all (recognized) test cases within the
 *  compiled tree. A recognized test case is a class that extends <code>TestCase</code> and whose
 * name starts with <code>test__</code> (two underscore characters).
 * </p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public class RegressionTests {
  
/** The name of this regression test suite. */
    private static String   RTS_NAME = "Shoola Regression Test Suite";
/** The base directory from which we start looking for test suites. 
 * This is a path relative to the base directory containing the compiled tree.
 */
    private static String   START_DIR = "org/openmicroscopy/shoola";
/** The regex to recognize a test case class' name. */
    private static String   FILE_PATTERN = "^test__.*\\.class$";
/** Extension of compiled java classes. */
    private static String   FILE_EXT = ".class";

    
   
/** The test suite that we build by composition of all (recognized) test cases within the
 *  compiled tree. 
 */
    private TestSuite   suite; 
/** This stack is used for navigation through the compiled directory tree. */
    private Stack       dirs;
/** The filter to select the compiled files that follows the pattern of a test suite file name. */
    private FileFilter  fileFilter;
/** Path to the directory containing the compiled tree. If realtive, then it has to be expressed in 
 * terms of the current working directory. 
 */
    private String      basePath;
/** Tells whether or not to run the suite in the Swing ui. */
    private boolean     swingMode;
    
   
    
/** Locates the compiled tree and initializes fields.
 *
 * @param   opt     The command line arguments.
 */
    private RegressionTests(String[] opt) {
        processOptions(opt);
        File    baseDir = new File(basePath, START_DIR);
        if( !baseDir.isDirectory() )
            throw new RuntimeException("Can't locate compiled tree: "+baseDir);
        suite = new TestSuite(RTS_NAME);
        dirs = new Stack();
        dirs.push(baseDir);
        fileFilter = new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().matches(FILE_PATTERN));
            }
        };
    }
    
/** Helper method to process the command line argumets.
 *
 * @param   opt     The command line arguments.
 */
    private void processOptions(String[] opt) {
        int k = opt.length;
        while( 0 <= --k ) {
            if( opt[k].equals("-ui") )      swingMode = true;
            else        basePath = opt[k];
        }
    }
    
/** Helper method to convert a path into a fully qualified class name.
 *
 * @param   f   Path and name of the class file.
 * @return  The fully qualified name that corresponds to the class file specified by <code>f</code>. 
 */
    private String fileToFQN(File f) {
        StringBuffer  buf = new StringBuffer(f.getPath());  //avoids errors if basePath was relative
        if( basePath != null )  //can either be null or hold a path (can't be "", see constructor)
            buf.delete(0, basePath.length());  //strip base path out
        if( buf.charAt(0) == File.separatorChar )  //strip name separator, if any, at start of string
            buf.deleteCharAt(0);
        buf.delete(buf.lastIndexOf(FILE_EXT), buf.length());  //strip extension out
        String  fqn = buf.toString();
        return fqn.replace(File.separatorChar, '.');  //turn it into fqn
    }
    
/** Adds the test case compiled in the specified class file to the regression suite.
 * All tests within the test case are extracted, composed into a suite and the suite is added
 * to the regression suite.
 *
 * @param   classFile  A file containing the bytecode of a test case class.
 */
    private void addSuite(File classFile) {
        String  fqn = fileToFQN(classFile);
        Class   c = null;
        try {
            c = Class.forName(fqn);
            if( TestCase.class.isAssignableFrom(c) )
                suite.addTestSuite(c);
            else
                throw new Exception("Class "+c+" is not a TestCase. ");
        } catch(Exception e) {
            throw new RuntimeException("Can't add "+classFile+" to the suite. ", e);
        }
    }
    
/** Builds the regression suite by composition of all (recognized) test suites within the base
 * directory of the  compiled tree.
 * Searches all sub-directories of the base directory for files that match the pattern that a test
 * case class' file is expected to follow. For each matching class file, its corresponding fully 
 * qualified name is used to add the tests within that class to a suite which is then added to
 * the regression suite.
 */
    private void buildSuite() {
        int     k;
        File    f;
        File[]  dirContents;
        while( !dirs.empty() ) {
            f = (File)dirs.pop();
            dirContents = f.listFiles(fileFilter);
            k = dirContents.length-1;
            while( 0 <= k ) {
                f = dirContents[k--];
                if( f.isDirectory() )   dirs.push(f);
                else        addSuite(f);  //f matches the FILE_PATTERN
            }
        }
    }
    
/** Runs the tests.
 * Depending on the specified command line option, the tests will be run either in console mode 
 * (default) or in GUI mode.
 */
    private void run() {
        if( swingMode ) {
            junit.swingui.TestRunner runner = new junit.swingui.TestRunner() {
                public Test getTest(String suiteClassName) {
                    return suite;  //hack to return the suite that we've already built
                }
            };
            runner.start(new String[]{suite.getName()});
        } else
            junit.textui.TestRunner.run(suite);
    }
    
    
    
/** Entry point.
 * Args as described in the class docs.
 */
    public static void main(String[] args) {
        RegressionTests rs = new RegressionTests(args);
        rs.buildSuite();
        rs.run();
    }
    
}
