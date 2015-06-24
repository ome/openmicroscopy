/*
 * org.openmicroscopy.shoola.svc.proxy.ProxyUtil
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.svc.proxy;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Collection of static method used to collect information about
 * the application.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ProxyUtil
{

    /** Identifies the version of java used. */
    public static final String JAVA_VERSION = "java_version";

    /** Identifies the class path of java. */
    public static final String JAVA_CLASS_PATH = "java_class_path";

    /** Identifies the class path of java. */
    public static final String JAVA_CLASS_PATH_OTHER = "java_classpath";

    /** Identifies the name of the operating system. */
    public static final String OS_NAME = "os_name";

    /** Identifies the architecture of the operating system. */
    public static final String OS_ARCH = "os_arch";

    /** Identifies the version of the operating system. */
    public static final String OS_VERSION = "os_version";

    /** Identifies the number associated to the application. */
    public static final String APP_NAME = "app_name";

    /** Identifies the <code>application version</code>. */
    public static final String APP_VERSION = "app_version";

    /**
     * Collects information useful when debugging e.g. Java version, OS, etc.
     * 
     * @return See above.
     */
    public static final Map<String, String> collectInfo()
    {
        Map<String, String> info = new HashMap<String, String>();
        info.put(JAVA_VERSION, System.getProperty("java.version"));
        info.put(JAVA_CLASS_PATH, System.getProperty("java.class.path"));
        info.put(OS_NAME, System.getProperty("os.name"));
        info.put(OS_ARCH, System.getProperty("os.arch"));
        info.put(OS_VERSION, System.getProperty("os.version"));
        return info;
    }

    /**
     * Collects information useful when debugging e.g. Java version, OS, etc.
     * 
     * @return See above.
     */
    public static final Map<String, String> collectOsInfoAndJavaVersion()
    {
        Map<String, String> info = collectInfo();
        info.remove(JAVA_CLASS_PATH);
        return info;
    }

}
