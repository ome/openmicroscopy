/*
 * org.openmicroscopy.shoola.env.ui.FileSubmit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries




import org.apache.commons.io.FilenameUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.FileTableNode;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;

/**
 * Uploads files to the QA system.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FileUploader
	extends UserNotifierLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /** Object hosting the data to upload. */
    private MessengerDetails details;
    
    /** The source dialog. */
    private MessengerDialog src;
    
    /** The files to upload. */
    private Map<ImportErrorObject, FileTableNode> nodes;
    
    /** The total number of files.*/
    private int total;

    /** The number of log files if any.*/
    private int logFileCount;

    /**
     * Creates a new instance.
     * 
     * @param viewer 	Reference to the parent.
     * @param reg    	Reference to the registry.
     * @param src 		The source object. 
     * @param details	Object hosting the data to upload.
     */
    FileUploader(UserNotifier viewer, Registry reg, SecurityContext ctx, 
    	MessengerDialog src, MessengerDetails details)
	{
		super(viewer, reg,ctx, null);
		if (details == null)
			throw new IllegalArgumentException("No files to upload.");
		this.details = details;
		this.src = src;
		nodes = new HashMap<ImportErrorObject, FileTableNode>();
		List l = (List) details.getObjectToSubmit();
		if (l != null) {
			Iterator i = l.iterator();
			FileTableNode node;
			ImportErrorObject object;
			while (i.hasNext()) {
				node = (FileTableNode) i.next();
				object = node.getFailure();
				nodes.put(object, node);
			}
		}
		logFileCount = 0;
	}
	
	/** 
	 * Uploads the file. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void load()
	{
		total = nodes.size();
		if (!details.isExceptionOnly()) {
			src.setSubmitStatus("0 out of "+total, false);
		}
		handle = mhView.submitFiles(ctx, details, this);
	}
    
	/** 
	 * Cancels the data uploading. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/** 
     * Feeds the results back. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
    	ImportErrorObject f = (ImportErrorObject) fe.getPartialResult();
        if (f != null) {
        	FileTableNode node = nodes.get(f);
        	if (node != null) node.setStatus(false);
        	File file = f.getFile();
        	String extension = FilenameUtils.getExtension(file.getName());
        	if (CommonsLangUtils.isNotBlank(extension)) {
        	    if (extension.toLowerCase().equals("log")) {
        	        logFileCount++;
        	    }
        	}
        	nodes.remove(f);
        }
        int v = total-nodes.size();
        if (v != total) {
    		if (!details.isExceptionOnly()) {
    			src.setSubmitStatus(v+" out of "+total, false);
    		}
        } else {
        	if (!details.isExceptionOnly())
        		src.setSubmitStatus("Done", true);
        }
        if (nodes.size() == 0) {
        	String s = "";
        	String verb = "has";
        	if (total > 1) {
        		verb = "have";
        	}
        	StringBuffer buf = new StringBuffer();
        	String term;
        	if (details.isExceptionOnly()) {
        	    term = "exception";
        	    if (total > 1) {
                    s = "s";
                }
        	} else {
        	    if (logFileCount > 0) {
        	        term = "log file";
        	        if (logFileCount > 1) {
        	            term +="s";
        	        }
        	        int diff = total-logFileCount;
        	        if (diff > 0) {
        	            term += " and file";
        	        }
        	        if (diff > 1) {
        	            s = "s";
        	        }
        	    } else {
        	        term = "file";
        	        if (total > 1) {
                        s = "s";
                    }
        	    }
        	}
        	buf.append("The ");
        	buf.append(term);
    		buf.append(s);
    		buf.append(" ");
    		buf.append(verb);
    		buf.append(" been successfully submitted.");
    		viewer.notifyInfo("Submit", buf.toString());
        	if (src != null) {
        		src.setVisible(false);
            	src.dispose();
        	}
        }
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load is delivered progressively
     * during the updates.
     * @see DataBrowserLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "File Upload Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("File Upload failure", s, exc);
    }
    
}
