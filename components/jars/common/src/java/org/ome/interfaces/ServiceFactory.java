/*
 * Created on Feb 12, 2005
 */
package org.ome.interfaces;

/**
 * 
 * NB: ServiceFactory implementations on the client side will be communicating
 * with implementors of the Service interfaces in the srv.net package; the
 * srv.net implementaitons will use the Service Factories under srv.db
 * 
 * @author josh
 */
public interface ServiceFactory {
	public AdministrationService getAdministrationService();
	public GenericService getGenericService();
	public FollowGroupService getFollowGroupService();
}