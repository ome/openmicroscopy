/*
 * ome.util.tasks.Run
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

//Java imports
import java.net.URL;
import java.util.Properties;

//Third-party libraries
import org.apache.tools.ant.BuildException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

/** 
 * Adapter for running tasks from Ant (http://ant.apache.org) Currently not 
 * functional for class loading reasons. 
 * 
 * See https://trac.openmicroscopy.org.uk/omero/ticket/464
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see		Configuration
 * @see     Task
 * @since   3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class AntAdapter extends org.apache.tools.ant.Task
{

	String task, user, pass, group, type;
	
	/** main method */
	@Override
	public void execute() 
	{
		Properties props = new Properties();
		props.setProperty("task",task);
		props.setProperty("user",user);
		props.setProperty("pass",pass);
		props.setProperty("group",group);
		props.setProperty("type",type);

		ClassLoader p = getProject().getClass().getClassLoader();
		ClassLoader c = getProject().getCoreLoader();
		ClassLoader f = ServiceFactory.class.getClassLoader();
		ClassLoader a = AntAdapter.class.getClassLoader();
		ClassLoader t = Task.class.getClassLoader();
		ClassLoader s = NoSuchBeanDefinitionException.class.getClassLoader();
		URL url = s.getResource("beanRefContext.xml");
		getProject().setCoreLoader(p);

		System.out.println(p);
		System.out.println(c);
		System.out.println(a);
		System.out.println(t);
		System.out.println(s);
		System.out.println(f);

		System.out.println(url);
		try {
			Configuration config = new Configuration(props);
			Task omeroTask = config.createTask();
			omeroTask.run();
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}

	// String setters

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @param pass the pass to set
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(String task) {
		this.task = task;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
		
}
