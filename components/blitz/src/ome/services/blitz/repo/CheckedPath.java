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

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import ome.util.Utils;
import ome.services.util.Executor;

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

    public CheckedPath mustDB() {
        return this;
    }

    /**
     * Create an OriginalFile object corresponding to a File object
     * using the user supplied mimetype string
     *
     * @param f
     *            A File object.
     * @param mimetype
     *            Mimetype as an RString
     * @return An OriginalFile object
     *
     * TODO populate more attribute fields than the few set here?
     */
    public OriginalFile createOriginalFile(omero.RString mimetype) {
        OriginalFile ofile = new OriginalFileI();
        ofile.setName(rstring(file.getName()));
        // This first case deals with registering the repos themselves.
        if (isRoot) {
            ofile.setPath(rstring(file.getParent()));
        } else { // Path should be relative to root?
            ofile.setPath(rstring(getRelativePath(file)));
        }
        ofile.setSha1(rstring(sha1()));
        ofile.setMimetype(mimetype);
        ofile.setMtime(rtime(this.file.lastModified()));
        ofile.setSize(rlong(this.file.length()));
        // atime/ctime??

        return ofile;
    }

    protected String getRelativePath(File f) {
        String path = f.getParent()
                .substring(root.file.getAbsolutePath().length(), f.getParent().length());
        // The parent doesn't contain a trailing slash.
        path = path + "/";
        return path;
    }
}
