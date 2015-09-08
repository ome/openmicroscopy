/*
 * Copyright (C) 2012-2014 Glencoe Software, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import loci.formats.FormatException;
import loci.formats.ReaderWrapper;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableSet;

import ome.io.nio.FileBuffer;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.ServerFilePathTransformer;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumType;
import omero.ValidationException;
import omero.model.ChecksumAlgorithm;

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
 *
 * @author josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class CheckedPath {
    private static final String SAME_DIR = ".";
    private static final String PARENT_DIR = "..";
    private static final ImmutableSet<String> SPECIAL_DIRS = ImmutableSet.of(SAME_DIR, PARENT_DIR);

    public final FsFile fsFile;
    public /*final*/ boolean isRoot;
    private final File file;
    private /*final*/ String parentDir;
    private /*final*/ String baseName;
    private final String original;  // for error reporting
    private final ChecksumProvider checksumProvider;

    // HIGH-OVERHEAD FIELDS (non-final)

    protected Long id;
    protected String hash;
    protected String mime;

    /**
     * Adjust an FsFile to remove "." components and to remove ".." components with the previous component.
     * TODO: May not actually be necessary.
     * @param fsFile a file path
     * @return a file path with "." and ".." processed away
     * @throws ValidationException if ".." components rise above root
     */
    private FsFile processSpecialDirectories(FsFile fsFile) throws ValidationException {
        final List<String> oldComponents = fsFile.getComponents();
        final Deque<String> newComponents = new ArrayDeque<String>(oldComponents.size());
        for (final String oldComponent : oldComponents) {
            if (PARENT_DIR.equals(oldComponent)) {
                if (newComponents.isEmpty()) {
                    throw new ValidationException(null, null, "Path may not make references above root");
                } else {
                    newComponents.removeLast();
                }
            }
            else if (!SAME_DIR.equals(oldComponent)) {
                newComponents.addLast(oldComponent);
            }
        }
        return new FsFile(newComponents);
    }

    /**
     * Construct a CheckedPath from a relative "/"-delimited path rooted at the repository.
     * The path may not contain weird or special-meaning path components,
     * though <q>.</q> and <q>..</q> are understood to have their usual meaning.
     * An empty path is the repository root.
     * @param serverPaths the server path handling service
     * @param path a repository path
     * @param checksumProviderFactory a source of checksum providers,
     * may be <code>null</code> if <code>checksumAlgorithm</code> is also <code>null</code>
     * @param checksumAlgorithm the algorithm to use in {@link #hash()}'s calculations
     * @throws ValidationException if the path is empty or contains illegal components
     */
    public CheckedPath(ServerFilePathTransformer serverPaths, String path,
            ChecksumProviderFactory checksumProviderFactory, ChecksumAlgorithm checksumAlgorithm)
            throws ValidationException {
        this.original = path;
        if (checksumAlgorithm == null) {
            this.checksumProvider = null;
        } else {
            final ChecksumType checksumType = ChecksumAlgorithmMapper.getChecksumType(checksumAlgorithm);
            if (checksumType == null) {
                throw new ValidationException(null, null,
                        "unknown checksum algorithm: " + checksumAlgorithm.getValue().getValue());
            }
            this.checksumProvider = checksumProviderFactory.getProvider(checksumType);
        }
        this.fsFile = processSpecialDirectories(new FsFile(path));
        if (!serverPaths.isLegalFsFile(fsFile)) // unsanitary
            throw new ValidationException(null, null, "Path contains illegal components");
        this.file = serverPaths.getServerFileFromFsFile(fsFile);
        breakPath();
    }

    private CheckedPath(File filePath, FsFile fsFilePath) throws ValidationException {
        this.original = filePath.getPath();
        this.fsFile = fsFilePath;
        this.file = filePath;
        this.checksumProvider = null;
        breakPath();
    }

    /**
     * Set parentDir and baseName according to the last separator in the fsFile.
     * @throws ValidationException if the path is empty
     */
    private void breakPath() throws ValidationException {
        final String fullPath = fsFile.toString();
        this.isRoot = "".equals(fullPath);
        final int lastSeparator = fullPath.lastIndexOf(FsFile.separatorChar);
        if (lastSeparator < 0) {
            this.parentDir = "";
            this.baseName = fullPath;
        } else {
            this.parentDir = fullPath.substring(0,  lastSeparator);
            this.baseName = fullPath.substring(lastSeparator + 1);
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

    public String hash() {
        if (hash == null && this.checksumProvider != null) {
            hash = this.checksumProvider
                       .putFile(file.getPath())
                       .checksumAsString();
        }
        return hash;
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
     * {@link CheckedPath}s generated with this method always return a
     * <code>null</code> hash.
     */
    public CheckedPath parent() throws ValidationException {
        List<String> components = this.fsFile.getComponents();
        if (components.isEmpty())
            throw new ValidationException(null, null, "May not obtain parent of repository root");
        components = components.subList(0, components.size() - 1);
        return new CheckedPath(this.file.getParentFile(), new FsFile(components));
    }

    /**
     * Returns a new {@link CheckedPath} that has the given path appended
     * to the end of this instances path. A check is made that the name does
     * not contain "/" (i.e. subpaths) nor that it is ".." or ".".
     * {@link CheckedPath}s generated with this method always return a
     * <code>null</code> hash.
     *
     * @param name
     * @return See above.
     */
    public CheckedPath child(String name) throws ValidationException {
        if (name == null || "".equals(name)) {
            throw new ValidationException(null, null, "null or empty name");
        } else if (SPECIAL_DIRS.contains(name)) {
            final StringBuffer message = new StringBuffer();
            message.append("Only proper child name is allowed, not ");
            for (final String dir : SPECIAL_DIRS) {
                message.append('\'');
                message.append(dir);
                message.append('\'');
                message.append(", ");
            }
            message.setLength(message.length() - 2);  // remove trailing ", "
            message.append('.');
            throw new ValidationException(null, null, message.toString());
        } else if (name.indexOf(FsFile.separatorChar)>=0) {
            throw new ValidationException(null, null,
                    "No subpaths allowed. Path contains '" + FsFile.separatorChar + "'");
        }
        final FsFile fullChild = FsFile.concatenate(this.fsFile, new FsFile(name));
        return new CheckedPath(new File(original, name), fullChild);
    }

    /**
     * Check if this file actually exists on the underlying filesystem.
     * Analogous to {@link java.io.File#exists()}.
     * @return <code>true</code> if the file exists, <code>false</code> otherwise
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Checks for existence of the original path, throwing an exception if
     * not present.
     *
     * @return this instance for chaining.
     * @throws ValidationException
     */
    public CheckedPath mustExist() throws ValidationException {
        if (!exists()) {
            throw new ValidationException(null, null, original
                    + " does not exist");
        }
        return this;
    }

    boolean renameTo(CheckedPath target) {
        return file.renameTo(target.file);
    }

    void moveToDir(CheckedPath target, boolean createDestDir) throws IOException {
        FileUtils.moveToDirectory(file, target.file, createDestDir);
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

    /**
     * Check if this file is actually readable on the underlying filesystem.
     * Analogous to {@link java.io.File#canRead()}.
     * @return <code>true</code> if the file is readable, <code>false</code> otherwise
     */
    public boolean canRead() {
        return this.file.canRead();
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
        return this.fsFile.toString() + FsFile.separatorChar;
    }

    /**
     * Get the last component of this path, the entity to which the path corresponds.
     * If this entity {@link #isRoot} then this is the empty string.
     * @return the last path component
     */
    protected String getName() {
        return this.baseName;
    }

    /**
     * Get the parent path of the entity to which this path corresponds.
     * If this entity is not in some sub-directory below root,
     * then this relative path is just the {@link FsFile#separatorChar}.
     * @return the path components above the last,
     * with separators including a trailing {@link FsFile#separatorChar}.
     */
    protected String getRelativePath() {
        return this.parentDir + FsFile.separatorChar;
    }
    
    /**
     * The full path of the entity to which this path corresponds.
     * Path components are separated by {@link FsFile#separatorChar}.
     * @return the full path
     */
    protected String getFullFsPath() {
        return this.fsFile.toString();
    }
    
    /**
     * Get a {@link FileBuffer} corresponding to this instance.
     * It is the caller's responsibility to {@link FileBuffer#close()} it.
     * @param mode as for {@link java.io#RandomAccessFile(File, String)},
     * <code>"r"</code> and <code>"rw"</code> being common choices
     * @return a new {@link FileBuffer}
     */
    public FileBuffer getFileBuffer(String mode) {
        return new FileBuffer(this.file.getPath(), mode);
    }

    /**
     * Return the size of this file on the underlying filesystem.
     * Analogous to {@link java.io.File#length()}.
     * @return the file size
     */
    public long size() {
        return this.file.length();
    }

    /**
     * Create this directory on the underlying filesystem.
     * Analogous to {@link java.io.File#mkdir()}.
     * @return <code>true</code> if the directory was created, <code>false</code> otherwise
     */
    public boolean mkdir() {
        return this.file.mkdir();
    }

    /**
     * Create this directory, and parents if necessary, on the underlying filesystem.
     * Analogous to {@link java.io.File#mkdirs()}.
     * @return <code>true</code> if the directory was created, <code>false</code> otherwise
     */
    public boolean mkdirs() {
        return this.file.mkdirs();
    }

    /**
     * Mark this existing file as having been modified at the present moment.
     * @return <code>true</code> if the file's modification time was updated, <code>false</code> otherwise
     */
    public boolean markModified() {
        return this.file.setLastModified(System.currentTimeMillis());
    }

    /**
     * Perform BioFormats {@link ReaderWrapper#setId(String)} for this file.
     * @param reader the BioFormats reader upon which to operate
     * @throws FormatException passed up from {@link ReaderWrapper#setId(String)}
     * @throws IOException passed up from {@link ReaderWrapper#setId(String)}
     */
    public void bfSetId(ReaderWrapper reader) throws FormatException, IOException {
        reader.setId(file.getPath());
    }
    
    public String toString() {
        return getClass().getSimpleName() + '(' + this.fsFile + ')';
    }

    /**
     * Creates an {@link ome.model.core.OriginalFile} instance for the given
     * {@link CheckedPath} even if it doesn't exist. If it does exist, then
     * the size and hash will be properly set. Further, if it's a directory,
     * the mimetype passed in by the user must either be null, in which case
     * "Directory" will be used, or must be that correct value.
     *
     * @param mimetype The mimetype to handle.
     * @return See above.
     */
    public ome.model.core.OriginalFile asOriginalFile(String mimetype) {

        ome.model.core.OriginalFile ofile =
                new ome.model.core.OriginalFile();

        // only non-conditional properties
        ofile.setName(getName());
        ofile.setMimetype(mimetype); // null takes DB default
        ofile.setPath(getRelativePath());

        final boolean mimeDir = PublicRepositoryI.DIRECTORY_MIMETYPE.equals(mimetype);
        final boolean actualDir = file.isDirectory();

        if (file.exists()) {
            ofile.setMtime(new Timestamp(file.lastModified()));
            if (actualDir) {
                // TODO: model directories as a subclass?
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
                ofile.setHash(hash());
                ofile.setSize(file.length());
            }
        }

        // TODO atime/ctime??

        return ofile;
    }

    /**
     * {@inheritDoc}
     * Instances are equal if their string representations match.
     * On Windows systems the comparison is not case-sensitive.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof CheckedPath))
            return false;
        return this.file.equals(((CheckedPath) object).file);
    }

    /**
     * {@inheritDoc}
     * On Windows systems the calculation is not case-sensitive.
     */
    @Override
    public int hashCode() {
        return this.file.hashCode() * 98;
    }
}
