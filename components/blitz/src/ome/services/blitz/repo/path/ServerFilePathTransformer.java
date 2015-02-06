/*
 * Copyright (C) 2012 - 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo.path;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Function;

/**
 * Transform between repository {@link FsFile} path and server-local {@link File}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class ServerFilePathTransformer {
    /* where the repository should store its files */
    private File baseDirFile;
    /* the parent path components that should be omitted from repository paths */
    private FsFile baseDirFsFile;
    /* a function to make file-path components safe across platforms */
    private Function<String, String> pathSanitizer;
    
    /**
     * Given a repository path, returns the corresponding server-local {@link File}.
     * Must be executed server-side.
     * @param fsFile a repository path
     * @return the corresponding server-local {@link File}
     */
    public File getServerFileFromFsFile(FsFile fsFile) {
        return fsFile.toFile(this.baseDirFile);
    }
    
    /**
     * Given a server-local {@link File}, returns the corresponding repository path.
     * Must be executed server-side.
     * @param serverFile a server-local {@link File} within the repository
     * @return the corresponding repository path
     * @throws IOException if the absolute path of the {@link File} could not be found
     */
    public FsFile getFsFileFromServerFile(File serverFile) throws IOException {
        final FsFile fullFsFile = new FsFile(serverFile);
        final FsFile childFsFile = fullFsFile.getPathFrom(this.baseDirFsFile);
        if (childFsFile == null)
            throw new IllegalArgumentException("server files must be within the repository");
        return childFsFile;
    }
    
    /**
     * Test if the given {@link FsFile} has been properly sanitized by the client.
     * @param fsFile a repository path
     * @return if the path is sanitary
     */
    public boolean isLegalFsFile(FsFile fsFile) {
        return fsFile.transform(this.pathSanitizer).equals(fsFile);
    }
    
    /**
     * Get the string transformer that is used to make file-path components safe across platforms.
     * @return the file-path component string transformer
     */
    public Function<String, String> getPathSanitizer() {
        return this.pathSanitizer;
    }
    
    /**
     * Set the string transformer that is used to make file-path components safe across platforms.
     * This is not required to be an injective function; two different components may transform to the same.
     * @param pathSanitizer the file-path component string transformer
     */
    public void setPathSanitizer(Function<String, String> pathSanitizer) {
        this.pathSanitizer = pathSanitizer;
    }
    
    /**
     * Set the repository root directory, to which {@link FsFile} instances are considered to be relative.
     * @throws IOException if the absolute path of the root directory could not be found
     * @throws IllegalArgumentException if the root directory does not exist
     * @param baseDirFile the repository root directory
     */
    public void setBaseDirFile(File baseDirFile) {
        if (!baseDirFile.isDirectory())
            throw new IllegalArgumentException(baseDirFile.getPath() + " must specify an existing FS repository directory");
        this.baseDirFile = baseDirFile.getAbsoluteFile();
        this.baseDirFsFile = new FsFile(this.baseDirFile);
    }
}
