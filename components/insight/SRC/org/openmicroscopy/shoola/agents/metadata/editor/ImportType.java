/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;

/**
 * Enum to indicate the import type
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public enum ImportType {
    
    /** Copy */
    COPY("ome.formats.importer.transfers.CopyFileTransfer", "cp"),
    
    /** Copy and remove source*/
    COPYREMOVE("ome.formats.importer.transfers.CopyMoveFileTransfer", "cp_rm"),
    
    /** Soft link to file (file is *not* in data repository)*/
    SOFTLINK("ome.formats.importer.transfers.SymlinkFileTransfer", "ln_s"), 
    
    /** Hard link to file */
    HARDLINK("ome.formats.importer.transfers.HardlinkFileTransfer", "ln"),
    
    /** Hard link and remove source */
    HARDLINKREMOVE("ome.formats.importer.transfers.MoveFileTransfer", "ln_rm"),
    
    /** Upload */
    UPLOAD("ome.formats.importer.transfers.UploadFileTransfer", ""),
    
    /** Upload and remove source */
    UPLOADREMOVE("ome.formats.importer.transfers.UploadRmFileTransfer", "upload_rm"),
    
    UNKNOWN("", "");
    
    /** The name of the ImportType */
    String name = "";

    /** The symbol (i. e. --transfer option used for import) */
    String symbol = "";

    ImportType() {
    }

    ImportType(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    /**
     * Get the name of the ImportType
     * 
     * @return See above
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the symbol (i. e. --transfer option used for import)
     * 
     * @return See above
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Determines the ImportType by name
     * 
     * @param name
     *            The name
     * @return See above
     */
    public static ImportType getImportType(String name) {
        for (ImportType t : ImportType.values())
            if (t.getName().equals(name))
                return t;
        return UNKNOWN;
    }
    
}
