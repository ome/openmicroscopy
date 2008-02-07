package ome.logic;

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
import ome.io.nio.OriginalFilesService;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.tools.FileSystem;
import ome.tools.RepositoryTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
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
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IRepositoryInfo"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IRepositoryInfo",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(IRepositoryInfo.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IRepositoryInfo")
@Interceptors( { SimpleLifecycle.class })
public class RepositoryInfoImpl extends AbstractLevel2Service implements
		IRepositoryInfo {

	/* The logger for this class. */
	private transient static Log log = LogFactory
			.getLog(RepositoryInfoImpl.class);

	/* repository filesystem */
	private transient String datadir;

	/* The ROMIO thumbnail service. */
	private transient ThumbnailService thumbnailService;

	/* The ROMIO pixels service. */
	private transient PixelsService pixelsService;

	/* The ROMIO file service. */
	private transient OriginalFilesService fileService;

	/**
	 * Bean injection setter for ROMIO thumbnail service
	 * 
	 * @param rootdir
	 */
	public void setThumbnailService(ThumbnailService thumbnailService) {
		getBeanHelper().throwIfAlreadySet(this.thumbnailService, thumbnailService);
		this.thumbnailService = thumbnailService;
	}

	/**
	 * Bean injection setter for ROMIO pixels service
	 * 
	 * @param rootdir
	 */
	public void setPixelsService(PixelsService pixelsService) {
		getBeanHelper().throwIfAlreadySet(this.pixelsService, pixelsService);
		this.pixelsService = pixelsService;
	}

	/**
	 * Bean injection setter for ROMIO file service
	 * 
	 * @param rootdir
	 */
	public void setFileService(OriginalFilesService fileService) {
		getBeanHelper().throwIfAlreadySet(this.fileService, fileService);
		this.fileService = fileService;
	}

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
			result = f.free(datadir);
			if (log.isInfoEnabled()) {
				log.info("Total kilobytes free: " + f.free(datadir));
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

		try {

			RepositoryTask task = new RepositoryTask();

			// get ids for any objects marked as deleted
			List<Long> files = task.getFileIds();
			List<Long> pixels = task.getPixelIds();
			List<Long> thumbs = task.getThumbnailIds();

			// cleanup any files
			fileService.removeFiles(files);

			// cleanup any pixels
			pixelsService.removePixels(pixels);

			// cleanup any thumbnails
			thumbnailService.removeThumbnails(thumbs);

		} catch (RuntimeException rtex) {
			throw new InternalException(rtex.getMessage());
		}
	}

}
