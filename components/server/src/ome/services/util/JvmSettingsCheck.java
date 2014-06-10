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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hook run by the context which prints out JVM-related
 * information, primarily Java version, max heap size
 * and available processors.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class JvmSettingsCheck {

    public final static Log log = LogFactory.getLog(JvmSettingsCheck.class);

    /**
     * TotalPhysicalMemorySize value from the OperatingSystem JMX bean
     * at startup.
     */
    public final static long TOTAL_PHYSICAL_MEMORY;

    /**
     * FreePhysicalMemorySize value from the OperatingSystem JMX bean
     * at startup.
     */
    public final static long INITIAL_FREE_PHYSICAL_MEMORY;

    static {
        TOTAL_PHYSICAL_MEMORY = _get("TotalPhysicalMemorySize");
        INITIAL_FREE_PHYSICAL_MEMORY = _get("FreePhysicalMemorySize");
    }

    public JvmSettingsCheck() {
        final String fmt = "%s = %6s";
        final Runtime rt = Runtime.getRuntime();
        final int mb = 1024 * 1024;

        StringBuilder version = new StringBuilder();
        for (String key : new String[]{
                "java.version", "os.name", "os.arch", "os.version"}) {
            if (version.length() != 0) {
                version.append("; ");
            }
            version.append(System.getProperty(key));
        }

        log.info("Java version: " + version);
        log.info(String.format(fmt, "Max Memory (MB):  ", (rt.maxMemory() / mb)));
        log.info(String.format(fmt, "OS Memory (MB):   ", (getPhysicalMemory() / mb)));
        log.info(String.format(fmt, "Processors:       ", rt.availableProcessors()));
    }

    public static long getPhysicalMemory() {
        return TOTAL_PHYSICAL_MEMORY;
    }

    private static long _get(String name) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Object attribute = mBeanServer.getAttribute(
                new ObjectName("java.lang","type","OperatingSystem"), name);
            return Long.valueOf(attribute.toString());
        } catch (Exception e) {
            log.debug("Failed to get: " + name, e);
            return -1;
        }
    }

    public static void main(String[] args) {
        if (args.length >= 1 && "--psutil".equals(args[0])) {
            System.out.println("Free:" + INITIAL_FREE_PHYSICAL_MEMORY);
            System.out.println("Total:" + TOTAL_PHYSICAL_MEMORY);
            return; // EARLY EXIT
        }
        new JvmSettingsCheck();
    }

}
