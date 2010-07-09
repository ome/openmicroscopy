/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

import java.io.File;

import ome.util.Utils;

import org.apache.commons.io.FilenameUtils;

/**
 * File type wrapper for paths which are intended for being stored in the
 * database as a part of this repository.
 */
public class RepoFile {

    /**
     * Somewhat complicated method to turn any path into a unique like path
     * rooted at "/".
     *
     * @param path Non-null;
     * @return
     */
    public static String norm(String path) {
        String s2u = separatorsToUnix(path);
        String tmp = normalizeNoEndSeparator(getPath(s2u));
        if (tmp == null) {
            tmp = "";
        }
        else if (tmp.equals("")) {
            // pass
        }
        else {
            tmp = "/" + tmp;
        }
        return String.format("%s/%s.%s", tmp, getBaseName(s2u), getExtension(s2u));
    }

    final private FsFile fs;
    final private String rel;
    final private String root;
    final private String absPath;

    /**
     * Assumes that path is relative under root, and constructs a new {@link File}
     * and calls {@link #RepoFile(File, File)}
     */
    public RepoFile(File root, String path) {
        this(root, new File(root, norm(path)));
    }

    /**
     * Both root and file are absolute paths to files. This constructor calculates
     * the relative part of the second argument based on the first.
     */
    public RepoFile(File root, File file) {
        this.fs = new FsFile(file);
        this.root = FilenameUtils.normalize(root.getAbsolutePath());
        this.rel = fs.path.substring((int) this.root.length());
        this.absPath = new File(root, rel).getAbsolutePath();
    }

    public boolean matches(File file) {
        return FilenameUtils.equalsNormalizedOnSystem(absPath, file
                .getAbsolutePath());
    }

    public File file() {
        return new File(fs.path);
    }

    public String basename() {
        return FilenameUtils.getName(rel);
    }

    public String dirname() {
        return FilenameUtils.getFullPath(rel);
    }

    public String fullname() {
        return rel;
    }

    public String sha1() {
        return fs.sha1();
    }

    public long length() {
        return fs.length();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + this.rel;
    }

    /**
     * File type wrapper for actual OS files.
     */
    public static class FsFile {

        final public String path;
        final public String name;

        FsFile(String path) {
            this.path = FilenameUtils.normalize(path);
            this.name = FilenameUtils.getName(this.path);
        }

        FsFile(File file) {
            this(file.getAbsolutePath());
        }

        public long length() {
            return new File(path).length();
        }

        public String sha1() {
            return Utils.bytesToHex(Utils.pathToSha1(path));
        }

        @Override
        public String toString() {
            return super.toString() + ":" + this.path;
        }

    }
}

