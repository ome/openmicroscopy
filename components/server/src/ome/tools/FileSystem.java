package ome.tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extends Sun Microsystems java.io.File for J2SE 5 only. This class provides
 * methods that are available in version Java 2 Platform Standard Edition
 * version 6.0. Only two of these methods are used for a specific purpose within
 * this application. The methods are getTotalSpace() and getFreeSpace(). These
 * methods will be used to monitor a specific filesystem and determine if
 * maximum points may be exceeded.
 * 
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt 
 * <p/>
 *
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 */
public class FileSystem extends File {

    private static Log log = LogFactory.getLog(FileSystem.class);
    
	/**
	 * unique serial identifier
	 */
	private static final long serialVersionUID = 8696186585793324755L;

	/**
	 * pass-thru constructor
	 * 
	 * @see java.io.File#File(String)
	 * @param pathname
	 */
	public FileSystem(String pathname) {
		super(pathname);
	}

	/**
	 * pass-thru constructor
	 * 
	 * @see java.io.File#File(URI)
	 * @param uri
	 */
	public FileSystem(URI uri) {
		super(uri);
	}

	/**
	 * pass-thru constructor
	 * 
	 * @see java.io.File#File(String, String)
	 * @param parent
	 * @param child
	 */
	public FileSystem(String parent, String child) {
		super(parent, child);
	}

	/**
	 * pass-thru constructor
	 * 
	 * @see java.io.File#File(File, String)
	 * @param parent
	 * @param child
	 */
	public FileSystem(File parent, String child) {
		super(parent, child);
	}

	/**
	 * This method is used to get the used space of a File or FileSystem in
	 * bytes
	 * 
	 * @return
	 */
	public long size() {
		long totalSpace = length();

		if (isDirectory()) {
			String[] entries = list();
			int len = entries.length;

			for (int i = 0; i < len; i++) {
				FileSystem child = new FileSystem(getPath() + separatorChar
						+ entries[i]);
				totalSpace += child.size();
			}
		}
		return totalSpace;
	}

	/**
	 * This method is used to get the used space of a File or FileSystem in
	 * kilobytes
	 * 
	 * @return
	 */
	public long used() {
		long result = 0;

		if (size() > 0) {
			result = size() / 1024; // in kilobytes
		}
		return result;
	}

	/**
	 * This method will return the free space in kilobytes TODO - resolve the
	 * slash mount
	 * 
	 * @return long of free space
	 */
	public long free(String mountRoot) throws RuntimeException {
		long result = 0L;

		try {
			result = FileSystemUtils.freeSpaceKb(mountRoot);
		} catch (IOException ioex) {
			throw new RuntimeException(ioex.getMessage());
		} catch (IllegalArgumentException illex) {
			throw new RuntimeException(illex.getMessage());
		} catch (IllegalStateException stex) {
			throw new RuntimeException(stex.getMessage());
		}

		return result;
	}

	/**
	 * Public toString implementation
	 * 
	 * @return
	 */
	public String toString() {
        StringBuffer s = new StringBuffer("FileSystem: ");
        s.append(getName());
		return s.toString();
	}

	/**
	 * Public equals implementation based on name only (very loose equals)
	 * 
	 * @param object
	 * @return
	 */
	public boolean equals(Object object) {
		boolean result = false;
		if (this == object)
			return true;
		if (object instanceof FileSystem) {
			FileSystem unknown = (FileSystem) object;
			if (unknown.getName().equals(this.getName())) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Public hashcode implementation based on String hash() return
	 * 
	 * @return
	 */
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}
	
	public static void main(String[] args) throws IOException {
		FileSystem file = new FileSystem("/OMERO");
		long free = file.free("/");

		if (log.isDebugEnabled()) {
		    log.debug("Free kilobytes: " + free);
            log.debug("Time prior to check: " + System.currentTimeMillis());
        }
		
		long kb = file.free("/");
        if (log.isDebugEnabled()) {
            log.debug("Time after check: " + System.currentTimeMillis());
            log.debug("kb free was " + kb);
        }
	}
}
