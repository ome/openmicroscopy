/*
 * org.openmicroscopy.shoola.util.file.ImportErrorObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.file;


import java.io.File;

import ome.formats.importer.ImportContainer;

/** 
 * Object information about files that cannot be imported.
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
public class ImportErrorObject
{

    /** The file that could not be imported. */
    private File file;

    /** The exception thrown during the import. */
    private Exception exception;

    /** The id of the log file.*/
    private long logFileID;

    /** The group indicating the security context.*/
    private long groupID;

    /** Retrieve the log file from the annotation.*/
    private boolean retrieveFromAnnotation;

    /** Object hosting information about the import.*/
    private ImportContainer ic;

    /**
     * Creates a new instance.
     *
     * @param file The file that could not be imported.
     * @param exception The exception.
     * @param groupID The id of the group.
     */
    public ImportErrorObject(File file, Exception exception, long groupID)
    {
        this.file = file;
        this.exception = exception;
        this.groupID = groupID;
        retrieveFromAnnotation = false;
    }

    /** Sets the file to submit to <code>null</code>.*/
    public void resetFile() { file = null; }

    /**
     * Sets to <code>true</code> if the log file needs to be retrieved
     * from the annotation, <code>false</code> otherwise.
     * If <code>true</code>, the {@link #logFileID} is the id of the 
     * annotation.
     *
     * @param retrieveFromAnnotation The value to set.
     */
    public void setRetrieveFromAnnotation(boolean retrieveFromAnnotation)
    {
        this.retrieveFromAnnotation = retrieveFromAnnotation;
    }

    /**
     * Returns <code>true</code> if the log file needs to be retrieved
     * from the annotation, <code>false</code> otherwise.
     * If <code>true</code>, the {@link #logFileID} is the id of the 
     * annotation.
     *
     * @return See above.
     */
    public boolean isRetrieveFromAnnotation() { return retrieveFromAnnotation; }

    /**
     * Returns the security context.
     *
     * @return See above.
     */
    public long getSecurityContext() { return groupID; }

    /**
     * Sets the identifier of the log file.
     *
     * @param logFileID The value to set.
     */
    public void setLogFileID(long logFileID) { this.logFileID = logFileID; }

    /**
     * Returns the id of the log file.
     *
     * @return See above.
     */
    public long getLogFileID() { return logFileID; }

    /**
     * Returns the type of reader used.
     *
     * @return See above.
     */
    public String getReaderType()
    {
        if (ic == null) return "";
        return ic.getReader();
    }

    /**
     * Returns the files associated to the file failing to import.
     *
     * @return See above.
     */
    public String[] getUsedFiles()
    {
        if (ic == null) return null;
        return ic.getUsedFiles();
    }

    /**
     * Returns the file that could not be imported.
     *
     * @return See above.
     */
    public File getFile() { return file; }

    /**
     * Returns the exception thrown during the import.
     *
     * @return See above.
     */
    public Exception getException() { return exception; }

    /**
     * Sets the importer container.
     *
     * @param ic The value to set.
     */
    public void setImportContainer(ImportContainer ic)
    {
        this.ic = ic;
    }

    /**
     * Indicates if the error occurred while importing HCS data or not.
     *
     * @return See above.
     */
    public Boolean isHCS()
    {
        if (ic == null) return null;
        return ic.getIsSPW();
    }

}
