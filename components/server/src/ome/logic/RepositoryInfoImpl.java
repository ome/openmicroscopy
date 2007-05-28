package ome.logic;

import java.io.File;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IRepositoryInfo;
import ome.api.ServiceInterface;
import ome.conditions.InternalException;
import ome.tools.FileSystem;
import ome.tools.RepositoryTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class implementation of the IRepositoryInfo service interface.
 * <p>
 * Stateless ome.logic to determine disk space utilization at the server's data
 * image mount point, e.g. /OMERO See source code documentation for more.
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt <p/>
 * 
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see IRepositoryInfo
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(IRepositoryInfo.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.IRepositoryInfo")
@Local(IRepositoryInfo.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IRepositoryInfo")
@SecurityDomain("OmeroSecurity")
@Interceptors( { SimpleLifecycle.class })
public class RepositoryInfoImpl extends AbstractLevel2Service implements
		IRepositoryInfo {

	/* The logger for this class. */
	private transient static Log log = LogFactory
			.getLog(RepositoryInfoImpl.class);

	/* root of the repository mount */
	private transient String rootdir;

	/* repository filesystem */
	private transient String datadir;

	public final static String PIXELS_PATH = "Pixels" + File.separator;

	public final static String FILES_PATH = "Files" + File.separator;

	public final static String THUMBNAILS_PATH = "Thumbnails" + File.separator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.api.IRepositoryInfo#getFreeSpaceInKilobytes()
	 */
	@RolesAllowed("user")
	public long getFreeSpaceInKilobytes() throws InternalException {

		FileSystem f;
		long result = 0L;

		try {
			f = new FileSystem(datadir);
			result = f.free(rootdir);
			if (log.isInfoEnabled()) {
				log.info("Total kilobytes free: " + f.free(rootdir));
			}
		} catch (RuntimeException rtex) {
			throw new InternalException(rtex.getMessage());
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.api.IRepositoryInfo#getUsedSpaceInKilobytes()
	 */
	@RolesAllowed("user")
	public long getUsedSpaceInKilobytes() throws InternalException {

		FileSystem f;
		long result = 0L;

		try {
			f = new FileSystem(datadir);
			result = f.used();
			if (log.isInfoEnabled()) {
				log.info("Total kilobytes used: " + f.used());
			}
		} catch (RuntimeException rtex) {
			throw new InternalException(rtex.getMessage());
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.api.IRepositoryInfo#getUsageFraction()
	 */
	@RolesAllowed("user")
	public double getUsageFraction() throws InternalException {
		double result = 0.0;

		try {
			if (getUsedSpaceInKilobytes() > 0) {
				Double used = new Double(getUsedSpaceInKilobytes());
				Double free = new Double(getFreeSpaceInKilobytes());
				result = used / free;
			}
		} catch (InternalException iex) {
			throw new InternalException("Error in getUsageFraction");
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.logic.AbstractBean#getServiceInterface()
	 */
	public final Class<? extends ServiceInterface> getServiceInterface() {
		return IRepositoryInfo.class;
	}

	/**
	 * Bean injection setter for data repository directory
	 * 
	 * @param datadir
	 */
	public void setDatadir(String datadir) {
		this.datadir = datadir;
	}

	/**
	 * Bean injection setter for the root directory or mount of data repository
	 * 
	 * @param rootdir
	 */
	public void setRootdir(String rootdir) {
		this.rootdir = rootdir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.api.IRepository#sanityCheckRepository()
	 */
	@RolesAllowed("user")
	public void sanityCheckRepository() throws InternalException {
		try {
			if (getUsageFraction() > 0.95) {
				throw new InternalException(
						"server repository disk space usage at 95% level");
			}
		} catch (InternalException iex) {
			throw new InternalException("Error in sanityCheckRepository()");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ome.api.IRepository#removeUnusedFiles()
	 */
	@RolesAllowed("user")
	public void removeUnusedFiles() throws InternalException {

		File file;

		try {

			RepositoryTask task = new RepositoryTask();
			
			List<Long> files = task.getFileIds();
			List<Long> pixels = task.getPixelIds();
			List<Long> thumbs = task.getThumbnailIds();
			
			
			// manually chosen file ids
			List<Long> list = task.getTestIds();
			
			if (log.isInfoEnabled()) {
				log.info("INFO: File ids obtained (" + list.size() + ")");
			}
			
			boolean success = false;
			
			// cleanup any files
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				Long id = (Long) iter.next();

				String filePath = getPath(FILES_PATH, id);
				file = new File(filePath);
				if (file.exists()) {
					success = file.delete();
					if (!success) {
						throw new InternalException("File " + file.getName()
								+ " deletion failed");
					}
				}
			}
			
			// cleanup any pixels
			for (Iterator iter = pixels.iterator(); iter.hasNext();) {
					Long id = (Long) iter.next();
				
				String pixelPath = getPath(PIXELS_PATH, id);
				file = new File(pixelPath);
				if (file.exists()) {
					success = file.delete();
					if (!success) {
						throw new InternalException("Pixels " + file.getName()
								+ " deletion failed");
					}
				}
			}
			
			// cleanup any thumbnails
			for (Iterator iter = thumbs.iterator(); iter.hasNext();) {
					Long id = (Long) iter.next();
				
				String thumbnailPath = getPath(THUMBNAILS_PATH, id);
				file = new File(thumbnailPath);
				if (file.exists()) {
					success = file.delete();
					if (!success) {
						throw new InternalException("Thumbnail " + file.getName()
								+ " deletion failed");
					}
				}

			}

		} catch (RuntimeException rtex) {
			throw new InternalException(rtex.getMessage());
		}
	}

	/**
	 * This method is used to return the full path of file on the disk using
	 * it's entityid and the prefix (choice of Files, Pixels, Thumbnails)
	 * 
	 * @param prefix - subdirectory Files, Pixels, or Thumbnails
	 * @param id - entityid and filename
	 * @return String representing full path of filename
	 */
	private String getPath(String prefix, Long id) {
		String suffix = "";
		Long remaining = id;
		Long dirno = 0L;

		if (id == null) {
			throw new NullPointerException("Expecting a not-null id.");
		}

		while (remaining > 999) {
			remaining /= 1000;

			if (remaining > 0) {
				Formatter formatter = new Formatter();
				dirno = remaining % 1000;
				suffix = formatter.format("Dir-%03d", dirno).out().toString()
						+ File.separator + suffix;
			}
		}

		return datadir + prefix + suffix + id;
	}

}
