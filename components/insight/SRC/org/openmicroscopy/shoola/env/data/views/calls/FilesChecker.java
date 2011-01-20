/*
 * org.openmicroscopy.shoola.env.data.views.calls.FilesChecker 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Controls if the files can be imported.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FilesChecker 
	extends BatchCallTree
{

	/** Call to control if the files can be imported. */
    private BatchCall   loadCall;
    
    /** The result of the call. */
    private Object		result;
    
    /**
     * Returns <code>true</code> if the file is of a supported format,
     * <code>false</code> otherwise.
     * 
     * @param f The file to handle.
     * @param filters The collection of filters.
     * @return See above.
     */
    private boolean isFileSupported(File f, FileFilter[] filters)
    {
    	String path = f.getAbsolutePath();
    	path = path.toLowerCase();
    	if (path.endsWith(OmeroImageService.ZIP_EXTENSION)) return true;
    	for (int i = 0; i < filters.length; i++) {
    		if (filters[i].accept(f)) return true;
		}	
    	return false;
    }
    
    /**
     * Creates a {@link BatchCall} to control if the passed files can be
     * imported.
     * 
     * @param files The files to control.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCheckCall(final List<File> files)
    {
        return new BatchCall("Downloading files.") {
            public void doCall() 
            {
                OmeroImageService service = context.getImageService();
                FileFilter[] filters = service.getSupportedFileFormats();
            	
                Map<Boolean, List<File>> m = new HashMap<Boolean, List<File>>();
                m.put(Boolean.valueOf(false), new ArrayList<File>());
                m.put(Boolean.valueOf(true), new ArrayList<File>());
                Iterator<File> j = files.iterator();
                File f;
                List<File> l;
                while (j.hasNext()) {
					f = j.next();
					l = m.get(isFileSupported(f, filters));
					l.add(f);
				}
                result = m;
            }
        };
    }
 
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the collection of archives files.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param files The files to handle.
     */
    public FilesChecker(List<File> files)
    {
    	if (files == null)
    		throw new IllegalArgumentException("No files to check.");
    	loadCall = makeCheckCall(files);
    }
    
}
