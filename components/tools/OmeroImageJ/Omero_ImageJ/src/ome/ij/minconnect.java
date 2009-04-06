package ome.ij;
import java.util.ArrayList;
import java.util.List;

// for omero
import omero.client;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.api.IAdminPrx;
import omero.sys.EventContext;
import omero.api.IContainerPrx;
import omero.ServerError;

import omero.model.IObject;
import omero.model.Project;


// other possibles
//import omero.api.IQuery;
//import omero.api.RawPixelsStore;
//import omero.sys.Filter;
//import omero.sys.Parameters;
//import pojos.ImageData;
//import pojos.PixelsData;
//import pojos.ProjectData;

public class minconnect
{
	public static void connect()
	{
		String theServerName = "warlock";
		int thePort = 4063;
		String theUsername = "root";
		String thePassword = "ome";
		System.out.println("Creating Connection");
		

		client theClient = null;
		try
		{
			System.out.println("Creating Client");
			theClient = new client(theServerName, thePort);
		} 
		catch (Exception e)
		{
			System.out.println("Exception on client creation");
			e.printStackTrace();
		} 

		ServiceFactoryPrx theServiceFactory = null;
		try
		{
			System.out.println("Creating Service Factory Proxy");
			theServiceFactory = 
				theClient.createSession(theUsername, thePassword);
		} 
		catch (Exception e)
		{
			System.out.println("Exception on service factory creation");
			e.printStackTrace();
		} 
		
		@SuppressWarnings("unused")
		ThumbnailStorePrx theThumbnailStore = null;
		try
		{
			System.out.println("Creating Thumbnail Store Proxy");
			theThumbnailStore = 
				theServiceFactory.createThumbnailStore();
		} 
		catch (Exception e)
		{
			System.out.println("Exception on thumbnail store creation");
			e.printStackTrace();
		} 
		
		IAdminPrx theAdmin = null;
		try
		{
			System.out.println("Creating Admin Proxy");
			theAdmin = 
				theServiceFactory.getAdminService();
		} 
		catch (Exception e)
		{
			System.out.println("Exception on admin creation");
			e.printStackTrace();
		} 
		
		@SuppressWarnings("unused")
		EventContext theEventContext = null;
		try
		{
			System.out.println("Creating Event Context");
			theEventContext = 
				theAdmin.getEventContext();
		} 
		catch (Exception e)
		{
			System.out.println("Exception on event context creation");
			e.printStackTrace();
		} 
				
		
		IContainerPrx theContainer = null;
		try
		{
			System.out.println("Creating IContainer Proxy");
			theContainer = 
				theServiceFactory.getContainerService();
			List<Project> theProjectList = getProjects(theContainer);
		
		for  (Project aProject : theProjectList)
			{
				System.out.println("Project Info:");
				if (aProject.getName() != null)
				{
					System.out.println(aProject.getName().getValue());
				}
				if (aProject.getDescription() != null)
				{
					System.out.println(aProject.getDescription().getValue());
				}
			}
		} 
		catch (Exception e)
		{
			System.out.println("Exception on event context creation");
			e.printStackTrace();
		} 
				
	    if (theClient!= null)
		{
			System.out.println("Closing the client");
//			theClient.close();
		}
		
		System.out.println("Done");
	}
	
    public static List<Project> getProjects(IContainerPrx iContainer)
    {
        try
        {
                List<IObject> objects = 
                        iContainer.loadContainerHierarchy(Project.class.getName(), null, null);
                List<Project> projects = new ArrayList<Project>(objects.size());
                for (IObject object : objects)
                {
                        projects.add((Project) object);
                }
                return projects;
        }
        catch (ServerError e)
        {
                throw new RuntimeException(e);
        }
    }
}

