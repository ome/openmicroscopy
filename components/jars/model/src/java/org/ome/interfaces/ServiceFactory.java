/*
 * Created on Feb 12, 2005
 */
package org.ome.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * NB: ServiceFactory implementations on the client side will be communicating
 * with implementors of the Service interfaces in the srv.net package; the
 * srv.net implementaitons will use the Service Factories under srv.db
 * 
 * @author josh
 */
public interface ServiceFactory extends Remote{
	public AdministrationService getAdministrationService() throws RemoteException;
	public ContainerService getContainerService() throws RemoteException;
	public GenericService getGenericService() throws RemoteException;
	public FollowGroupService getFollowGroupService() throws RemoteException;
	public ImageService getImageService() throws RemoteException;
	public AttributeService getAttributeService() throws RemoteException;
	public AnalysisService getAnalysisService() throws RemoteException;
}