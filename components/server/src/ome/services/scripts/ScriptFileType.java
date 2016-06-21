/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.services.scripts;

import ome.model.core.OriginalFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

/**
 * Definition of the script types that will be accepted by the server.
 * Every instance of {@link ScriptFileType} defined in the Spring
 * context will be registered with the {@link ScriptRepoHelper}.
 *
 * @since 5.0.0
 */
public class ScriptFileType {

    /**
     * Pattern that will be used by the default
     * {@link #getFileFilter()} implementation. Glob style such
     * as "*.py" is acceptable.
     */
    private final String pattern;

    /**
     * Mimetype that's required in the OriginalFile table
     * for the script to match this file type.
     */
    private final String mimetype;

    /**
     * Non-null but possibly empty launcher string. If empty, the
     * default behavior is chosen by the launching process itself.
     * For example, processor.py would chose "sys.executable". Values
     * are configured in Spring (e.g. "${omero.launcher.python}") and
     * can be modified by users via `bin/omero config set`.
     */
    private final String launcher;

    /**
     * String representing the class of the launcher that is to be
     * used. This will be loaded by the backend and given the same
     * arguments as would be given to the default processor (e.g.
     * "omero.processor.ProcessI").
     */
    private final String process;

    public ScriptFileType(String pattern, String mimetype) {
        this(pattern, mimetype, "", "");
    }

    public ScriptFileType(String pattern, String mimetype,
            String launcher, String process) {
        this.pattern = pattern;
        this.mimetype = mimetype;
        this.launcher = launcher;
        this.process = process;
    }

    public boolean isInert()
    {
        return StringUtils.isBlank(launcher) && StringUtils.isBlank(process);
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
     *  Return the name of the launcher ("./run.exe") that is used for
     *  scripts of this type. This is a fairly easy way to modify what
     *  is called by the backend.
     */
    public String getLauncher() {
        return launcher;
    }

    /**
     * Return the import name of the process class which will be used
     * to invoke scripts of this type. This permits developers to inject
     * completely different process handling.
     */
    public String getProcess() {
        return process;
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
