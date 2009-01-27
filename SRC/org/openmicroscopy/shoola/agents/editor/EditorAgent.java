/*
 * org.openmicroscopy.shoola.agents.editor.EditorAgent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor;


//Java imports
import java.io.File;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.agents.editor.view.EditorFactory;
import org.openmicroscopy.shoola.agents.events.editor.CopyEvent;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;

/** 
 * The Editor agent. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class EditorAgent 
	implements Agent, AgentEventListener
{
	
	/** Reference to the registry. */
    private static Registry         registry; 
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }
    
    /**
	 * Helper method returningthe current user's details.
	 * 
	 * @return See above.
	 */
	public static ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) registry.lookup(
								LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Returns <code>true</code> if an OMERO server is available,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isServerAvailable()
	{
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	return env.isServerAvailable();
	}
	
	/**
	 * Static method to open a local file. 
	 * Creates or recycles an Editor to display the file. 
	 * 
	 * @param file		The local file to open. 
	 */
	public static void openLocalFile(File file) {
		
		if (file == null)		return;
		if (!file.exists())		return;
		
		// gets a blank editor (one that has been created with a 'blank' model), 
		// OR an existing editor if one has the same 
		// file ID (will be 0 if editor file is local) and same file name, OR
		// creates a new editor model and editor with this new file. 
		Editor editor = EditorFactory.getEditor(file);
	
		// activates the editor
		// if the editor is 'blank' or has just been created (above), 
		// need to set the file
		if (editor != null) {
			if (editor.getState() == Editor.NEW)
				editor.setFileToEdit(file);
			
			// this simply brings the editor to the front / de-iconifies it.
			editor.activate();
		}
	}
	
	/**
	 * Creates or recycles an editor.
	 * @param event The event to handle.
	 */
	private void handleFileEdition(EditFileEvent event)
	{
		if (event == null) return;
		Editor editor = null;
		FileAnnotationData data = event.getFileAnnotation();
		if (data == null) {
			if (event.getFileAnnotationID() > 0)
				editor = EditorFactory.getEditor(event.getFileAnnotationID());
		} else {
			String name = data.getFileName();
			if (name == null) return;
			EditorFileFilter filter = new EditorFileFilter();
			if (!filter.accept(name)) return;
			editor = EditorFactory.getEditor(data);
		}
		if (editor != null)
			editor.activate();		// starts file downloading
	}
	
	/**
	 * Creates a {@link Editor#NEW} editor with no file, or recycles an editor.
	 */
	private void handleShowEditor()
	{
		Editor editor = EditorFactory.getEditor();
		if (editor != null) editor.activate();
	}
	
	/**
	 * Handles the copying event, by passing the copied data from this event
	 * to the {@link EditorFactory} where it can be accessed by any Editor 
	 * instance. 
	 * 
	 * @param evt		The CopyEvent event. 
	 */
	private void handleCopyData(CopyEvent evt)
	{
		EditorFactory.setCopiedData(evt.getCopiedData());
	}
	
	 /** Creates a new instance. */
    public EditorAgent() {}
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
    public void activate()
    {
    	if (!isServerAvailable()) handleShowEditor();
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#terminate()
     */
    public void terminate() {}

    /** 
     * Implemented as specified by {@link Agent}. 
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, EditFileEvent.class);
        bus.register(this, ShowEditorEvent.class);
        bus.register(this, CopyEvent.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate()
    { 
    	//Map m = ImViewerFactory.hasDataToSave();
    	return true; 
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent# hasDataToSave()
     */
    public Map<String, Set> hasDataToSave()
    {
		// TODO Auto-generated method stub
		//return EditorFactory.hasDataToSave();
    	return null;
	}
    
    /**
     * Responds to an event fired trigger on the bus.
     * 
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
       if (e instanceof EditFileEvent)
    	   handleFileEdition((EditFileEvent) e);
       
       if (e instanceof ShowEditorEvent)
    	   handleShowEditor();
       
       if (e instanceof CopyEvent)
    	   handleCopyData((CopyEvent)e);
    }

}
