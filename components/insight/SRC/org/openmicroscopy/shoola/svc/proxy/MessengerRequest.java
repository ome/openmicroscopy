/*
 * org.openmicroscopy.shoola.svc.proxy.MessengerRequest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.svc.proxy;


//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Prepares a request to post.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MessengerRequest
	extends Request
{
	
	/** Identifies the <code>e-mail</code> address. */
	private static final String EMAIL = "email";
	
	/** Identifies the <code>comment</code> sent. */
	private static final String COMMENT = "comment";
	
	/** Identifies the <code>error</code> message. */
	private static final String ERROR = "error";
	
	/** Identifies the <code>extra information</code>. */
	private static final String EXTRA = "extra";
	
	/** Identifies the <code>client</code> invoking the service. */
	private static final String INVOKER = "type";
	
	/** Identifies the version of java used. */
	private static final String JAVA_VERSION = "java_version";
	
	/** Identifies the class path of java. */
	private static final String JAVA_CLASS_PATH = "java_class_path";
	
	/** Identifies the class path of java. */
	private static final String JAVA_CLASS_PATH_OTHER = "java_classpath";
	
	/** Identifies the name of the operating system. */
	private static final String OS_NAME = "os_name";
	
	/** Identifies the architecture of the operating system. */
	private static final String OS_ARCH = "os_arch";
	
	/** Identifies the version of the operating system. */
	private static final String OS_VERSION = "os_version";
	
	/** Identifies the number associated to the application. */
	private static final String APP_NAME = "app_name";

	/** Identifies the <code>application version</code>. */
	private static final String APP_VERSION = "app_version";
	
	/** Identifies the name of <code>main file</code>. */
	private static final String MAIN_FILE_NAME = "selected_file";
	
	/** Identifies the path <code>main file</code>. */
	private static final String MAIN_FILE_PATH = "absolute_path";
	
	/** Identifies the name of <code>additional file</code>. */
	private static final String ADDITIONAL_FILE_NAME = "additional_files";
	
	/** Identifies the path of <code>additional file</code>. */
	private static final String ADDITIONAL_FILE_PATH = "additional_files_path";
	
	/** Identifies the size of <code>additional file</code>. */
	private static final String ADDITIONAL_FILE_SIZE = "additional_files_size";
	
	/** The error message. */
	private String error;
	
	/** The e-mail address of the user reporting an error. */
	private String email;
	
	/** The comment entered by the user. */
	private String comment;
	
	/** The extra information entered by the user. */
	private String extra;
	
	/** The client posting the message. */
	private String invoker;
	
	/** The number associated to the application. */
	private String applicationNumber;
	
	/** The version of the application. */
	private String applicationVersion;
	
	/** The main file. */
	private File mainFile;
	
	/** The associated files. */
	private List<File> associatedFiles;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param email		The e-mail address of the user reporting an error.
	 * @param comment	The comment entered by the user.
	 * @param extra		The extra information entered by the user.
	 * @param error		The error message. 
	 * @param applicationNumber The reference number for the application.
	 * @param invoker	The client posting the message.
	 * @param applicationVersion The version of the application.
	 * @param filesInfo The information about the files to submit or 
	 * 					<code>null</code>.
	 */
	MessengerRequest(String email, String comment, String extra, String error,
					String applicationNumber, String invoker, 
					String applicationVersion, File mainFile, 
					List<File> associatedFiles)
	{
		super();
		this.error = error;
		this.email = email;
		this.comment = comment;
		this.extra = extra;
		this.invoker = invoker;
		this.applicationNumber = applicationNumber;
		this.applicationVersion = applicationVersion;
		this.mainFile = mainFile;
		this.associatedFiles = associatedFiles;
	}
	
	/**
	 * Prepares the <code>method</code> to post.
	 * @see Request#marshal()
	 */
	public HttpMethod marshal() 
		throws TransportException
	{
		//Create request.
        PostMethod request = new PostMethod();
        //Marshal.
        if (email != null) request.addParameter(EMAIL, email);
        if (comment != null) request.addParameter(COMMENT, comment);
        if (error != null) request.addParameter(ERROR, error);
        if (extra != null) request.addParameter(EXTRA, extra);
        if (applicationNumber != null) 
        	request.addParameter(APP_NAME, applicationNumber);
        if (invoker != null) request.addParameter(INVOKER, invoker);
        if (applicationVersion != null)
        	request.addParameter(APP_VERSION, applicationVersion);
        request.addParameter(JAVA_VERSION, 
        							System.getProperty("java.version"));
        request.addParameter(JAVA_CLASS_PATH, 
        							System.getProperty("java.class.path"));
        request.addParameter(JAVA_CLASS_PATH_OTHER, 
				System.getProperty("java.class.path"));
        request.addParameter(OS_NAME, System.getProperty("os.name"));
        request.addParameter(OS_ARCH, System.getProperty("os.arch"));
        request.addParameter(OS_VERSION, System.getProperty("os.version"));
       
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (mainFile != null) {
        	pairs.add(new NameValuePair(MAIN_FILE_NAME, mainFile.getName()));
        	pairs.add(new NameValuePair(MAIN_FILE_PATH, 
        			mainFile.getAbsolutePath()));
        }
        if (associatedFiles != null) {
        	Iterator<File> i = associatedFiles.iterator();
        	File f;
        	while (i.hasNext()) {
				f = i.next();
				pairs.add(new NameValuePair(ADDITIONAL_FILE_NAME, f.getName()));
				if (f.getParent() != null) 
					pairs.add(new NameValuePair(ADDITIONAL_FILE_PATH, 
							f.getParent()));
	        	pairs.add(new NameValuePair(ADDITIONAL_FILE_SIZE, 
	        			((Long) f.length()).toString()));
			}
        }
        if (pairs.size() > 0) {
        	Iterator<NameValuePair> j = pairs.iterator();
        	NameValuePair[] values = new NameValuePair[pairs.size()];
        	int index = 0;
        	while (j.hasNext()) {
        		values[index] = j.next();
				index++;
			}
        	request.addParameters(values);
        }
        	
        return request;
	}
    
}
