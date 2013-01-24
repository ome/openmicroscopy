/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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

package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.sql.Timestamp;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import ome.util.Utils;
import ome.services.util.Executor;
import ome.system.Principal;

import omero.ValidationException;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;

/**
 * To prevent frequently re-calculating paths and re-creating File objects,
 * {@link CheckedPath} objects store various interpretations of paths that
 * are passed in by users. One of these objects should be created at the
 * very beginning of any {@link PublicRepositoryI} remote method (i.e. those
 * public methods which take {@link Ice.Current} instance arguments. Methods
 * are then available to check various capabilities by the current user. When
 * a null {@link CheckedPath} object is passed into the constructor the caller
 * indicates that the path is the root path, hence {@link CheckedPath#isRoot}
 * will not be called.
 */
public class CheckedPath {

    public final String original;
    public final String normPath;
    public final File file;
    public final boolean isRoot;
    protected final CheckedPath root;

    // HIGH-OVERHEAD FIELDS (non-final)

    protected Long id;
    protected String sha1;
    protected String mime;

    public CheckedPath(CheckedPath root, String path)
            throws ValidationException {

        this.root = root;
        this.original = path;

        if (this.root == null) {
            // Assumed absolute
            this.normPath = FilenameUtils.normalizeNoEndSeparator(path);
            this.file = new File(normPath);
            this.isRoot = true;
            return; // EARLY EXIT!
        }

        path = validate(path);

        // Handle the case where a relative path has been passed in.
        // It is currently appended to the root file to guarantee that
        // the object is in the repository.
        String p = FilenameUtils.normalizeNoEndSeparator(path);
        File f = new File(p);
        if (!f.isAbsolute()) {
            f = new File(root.file, p);
            p = f.toString();
        }

        this.normPath = p;
        this.isRoot = isRoot();

        if (this.isRoot) {
            this.file = root.file;
        }
        else {
            checkWithin();
            this.file = f;
        }
    }

    /**
     * Called before any other action in the constructor to guarantee that
     * the given path can be further processed. By default, if the value
     * is null or empty, then a {@link ValidationException} is thrown.
     */
    protected String validate(String path) throws ValidationException {
        if (path == null || path.length() == 0) {
            throw new ValidationException(null, null, "Path is empty");
        }
        return path;
    }

    /**
     * Checks if the requested path is the root of this repository.
     * Used during constructor to prevent unnecessary object creation.
     */
    protected boolean isRoot() {
        return normPath.equals(root.normPath);
    }

    /**
     * If the path is not the root itself, it must minimally be a subpath
     * of the root. If not, a {@link ValidationException} is thrown.
     */
    protected void checkWithin() throws ValidationException {
        // Could be replaced by commons-io 2.4 directoryContains.
        // But for the moment checking based on regionMatches with
        // case-sensitivity. Note we check against normRootSlash so that
        // two similar directories at the top-level can't cause issues.
        final String rootNormSlash = this.root.normPath + File.separator;
        if (!normPath.regionMatches(false, 0, rootNormSlash, 0,
                rootNormSlash.length())) {
            throw new ValidationException(null, null, normPath
                    + " is not within " + rootNormSlash);
        }
    }

    //
    // Public methods (mutable state)
    //

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String sha1() {
        if (sha1 == null) {
            sha1 = Utils.bytesToHex(Utils.pathToSha1(normPath));
        }
        return sha1;
    }

    /**
     * Get the mimetype for a file.
     *
     * @return A String representing the mimetype.
     */
    public String getMimetype() {
        if (mime == null) {
            mime = new MimetypesFileTypeMap().getContentType(file);
        }
        return mime;
    }

    //
    // Public methods (immutable state)
    //

    /**
     * Returns a new {@link CheckedPath} using {@link File#getParent()} and
     * passing in all other values. Just as if calling the constructor,
     * bad paths will cause a {@link ValidationException} to be thrown.
     */
    public CheckedPath parent() throws ValidationException {
        return new CheckedPath(root, file.getParent());
    }

    /**
     * Checks for existence of the original path, throwing an exception if
     * not present.
     *
     * @return this instance for chaining.
     * @throws ValidationException
     */
    public CheckedPath mustExist() throws ValidationException {
        if (!file.exists()) {
            throw new ValidationException(null, null, original
                    + " does not exist");
        }
        return this;
    }

    boolean delete() {
        return FileUtils.deleteQuietly(file);
    }

    /**
     * @return this instance for chaining
     * @throws omero.SecurityViolation
     */
    public CheckedPath mustEdit() throws omero.SecurityViolation {
        if (!canEdit()) {
            throw new omero.SecurityViolation(null, null,
                    original + " is not editable.");
        }
        return this;
    }

    public boolean canEdit() {
        return true;
    }

    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    /**
     * Assuming this is a directory, return relative path plus name with a final
     * slash.
     */
    protected String getDirname() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRelativePath());
        sb.append(getName());
        sb.append("/");
        return sb.toString();
    }

    protected String getName() {
        return file.getName();
    }

    protected String getRelativePath() {
        return getRelativePath(file);
    }

    protected String getRelativePath(File f) {

        String path = null;
        if (isRoot) {
            path = file.getParentFile().getAbsolutePath() + "/";
        } else {
            path = f.getParent()
                .substring(root.file.getAbsolutePath().length(), f.getParent().length());
        }

        // The parent doesn't contain a trailing slash.
        path = path + "/";
        return path;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("(");
        sb.append(getRelativePath()); // Has slash.
        sb.append(getName());
        sb.append(")");
        return sb.toString();
    }

    /**
     * Creates an {@link ome.model.core.OriginalFile} instance for the given
     * {@link CheckedPath} even if it doesn't exist. If it does exist, then
     * the size and sha1 value will be properly set. Further, if it's a directory,
     * the mimetype passed in by the user must either be null, in which case
     * "Directory" will be used, or must be that correct value.
     *
     * @param mimetype
     * @return
     */
    public ome.model.core.OriginalFile asOriginalFile(String mimetype) {

        ome.model.core.OriginalFile ofile =
                new ome.model.core.OriginalFile();

        // Only non conditional properties.
        ofile.setName(getName());
        ofile.setMimetype(mimetype); // null takes DB default

        // This first case deals with registering the repos themselves.
        if (isRoot) {
            ofile.setPath(file.getParent());
        } else { // Path should be relative to root?
            ofile.setPath(getRelativePath());
        }

        final boolean mimeDir = PublicRepositoryI.DIRECTORY_MIMETYPE.equals(mimetype);
        final boolean actualDir = file.isDirectory();

        if (file.exists()) {
            ofile.setMtime(new Timestamp(file.lastModified()));
            if (actualDir) {
                // Directories don't have these. TODO: model as a subclass?
                ofile.setSha1("");
                ofile.setSize(0L);
                ofile.setMimetype(PublicRepositoryI.DIRECTORY_MIMETYPE);
                if (mimetype != null && !mimeDir) {
                    // This is a directory, but the user has requested something
                    // else. Throw.
                    if (actualDir && !mimeDir) {
                        throw new ome.conditions.ValidationException(
                                "File is a directory but mimetype is: " + mimetype);
                    }
                }
            } else {
                ofile.setSha1(sha1());
                ofile.setSize(file.length());
            }
        } else {
            // File doesn't exist, therefore we know nothing
            ofile.setSha1("");
            ofile.setSize(0L);
        }

        // TODO atime/ctime??

        return ofile;
    }
}
