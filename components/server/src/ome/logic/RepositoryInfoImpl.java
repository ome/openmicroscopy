package ome.logic;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class implementation of the IRepositoryInfo service interface.
 * <p>
 * Stateless ome.logic to determine disk space utilization at the server's data image
 * mount point, e.g. /OMERO See source code documentation for more.
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt 
 * <p/>
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
@Interceptors({ SimpleLifecycle.class })
public class RepositoryInfoImpl extends AbstractLevel2Service implements IRepositoryInfo {
	
	/* The logger for this class. */
    private transient static Log log = LogFactory.getLog(RepositoryInfoImpl.class);

    /* root of the repository mount */
    private transient String rootdir;
    
    /* repository filesystem */
    private transient String datadir;
    
	/* (non-Javadoc)
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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see ome.api.IRepositoryInfo#getUsageFraction()
	 */
	@RolesAllowed("user")
	public double getUsageFraction() throws InternalException {
		double result = 0.0;

		try {
			if (getUsedSpaceInKilobytes() > 0) {
				result = getUsedSpaceInKilobytes()/getFreeSpaceInKilobytes();
			}
		}
		catch (InternalException iex) {
			throw new InternalException("Error in getUsageFraction");
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see ome.logic.AbstractBean#getServiceInterface()
	 */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IRepositoryInfo.class;
    }

	/**
	 * Bean injection setter for data repository directory
	 * @param datadir
	 */
	public void setDatadir(String datadir) {
		this.datadir = datadir;
	}

	/**
	 * Bean injection setter for the root directory or mount of data repository
	 * @param rootdir
	 */
	public void setRootdir(String rootdir) {
		this.rootdir = rootdir;
	}
   
	/* (non-Javadoc)
	 * @see ome.api.IRepository#sanityCheckRepository()
	 */
	@RolesAllowed("user")
	public void sanityCheckRepository() throws InternalException {
   	 try {
		if (getUsageFraction() > 0.95) {
   		 throw new InternalException ("server repository disk space usage at 95% level");
   	 }
   	 } catch (InternalException iex) {
   		 throw new InternalException ("Error in sanityCheckRepository()");
   	 }
   }

}
