/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.test.utests;

/**
 * Methods for use with {@literal @}Assumption annotations
 * to limit unit tests to specific test platforms.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class PlatformAssumptions {
    /**
     * A enumeration of operating systems under which tests may be occurring.
     * Some file path tests are platform-specific.
     * 
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0
     */
    private enum OperatingSystem {
        WINDOWS("Microsoft Windows"),
        LINUX("Linux"),
        MAC("Apple Mac OS X");

        private final String name;

        OperatingSystem(String name) {
            this.name = name;
        }
    
        @Override
        public String toString() {
            return this.name;
        }
    }

    /* the current platform's operating system */
    private static final OperatingSystem os;

    static {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows "))
            os = OperatingSystem.WINDOWS;
        else if (osName.equals("Linux"))
            os = OperatingSystem.LINUX;
        else if (osName.equals("Mac OS X"))
            os = OperatingSystem.MAC;
        else
            os = null;
    }

    /**
     * @return if this platform is Microsoft Windows
     */
    public static boolean isWindows() {
        return os == OperatingSystem.WINDOWS;
    }

    /**
     * @return if this platform is Linux
     */
    public static boolean isLinux() {
        return os == OperatingSystem.LINUX;
    }

    /**
     * @return if this platform is Mac OS X
     */
    public static boolean isMacOSX() {
        return os == OperatingSystem.MAC;
    }
}
