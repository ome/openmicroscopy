/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package ome.services.scripts;

import ome.model.core.OriginalFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;


/**
 * Definition of the script types that will be accepted by the server.
 * Every instance of {@link LUTFileType} defined in the Spring
 * context will be registered with the {@link LUTRepoHelper}.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
public class LUTFileType {

    /**
    * Pattern that will be used by the default
    * {@link #getFileFilter()} implementation. Glob style such
    * as "*.lut" is acceptable.
    */
   private final String pattern;

   /**
    * Mimetype that's required in the OriginalFile table
    * for the script to match this file type.
    */
   private final String mimetype;

   public LUTFileType(String pattern, String mimetype) {
       this.pattern = pattern;
       this.mimetype = mimetype;
   }

   /**
    * Returns the mimetype that must be set on an original file instance
    * in order to be considered of this type. Used in conjunction with
    * {@link #getFileFilter()}.
    */
   public String getMimetype() {
       return this.mimetype;
   }

   /**
    * A file-pattern (most likely of the form "*.EXT") which will be used
    * to determine if a file is of this type. Used in conjunction with
    * {@link #getMimetype()}.
    */
   public IOFileFilter getFileFilter() {
       return new WildcardFileFilter(pattern);
   }

   /**
    * Sets the mimetype on the given {@link OriginalFile}
    * if the name field matches the {@link #pattern wildcard pattern}
    * for this instance.
    */
   public boolean setMimetype(OriginalFile ofile) {
       if (FilenameUtils.wildcardMatch(ofile.getName(), pattern)) {
           ofile.setMimetype(mimetype);
           return true;
       }
       return false;
   }

}
