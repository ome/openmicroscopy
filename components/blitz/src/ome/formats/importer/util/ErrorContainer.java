/*
 * ome.formats.importer.util.ErrorContainer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package ome.formats.importer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for errors
 *
 * @author Brian W. Loranger
 *
 */
public class ErrorContainer
{
    private String uploadUrl = "";
    private String java_version = "";
    private String java_classpath = "";
    private String os_name = "";
    private String os_arch = "";
    private String os_version = "";
    private String extra = "";
    private Throwable error;
    private String comment_type = "";
    private String app_version = "";
    private String comment = "";
    private String email = "";
    private String session_id = "";
    private File selected_file = null;
    private String file_type = "";
    private String file_format = "";
    private List<String> filesArray = new ArrayList<String>();
    private String absolute_path;
    private int status = -1;
    private int index = 0;

    /**
     * @param s - file to add to container
     */
    public void addFile(String s)
    {
        filesArray.add(s);
    }

    /**
     * @return Returns the files in this container.
     */
    public String[] getFiles()
    {
        String[] files = new String[filesArray.size()];
        return filesArray.toArray(files);
    }

    /**
     * @param files - files to set.
     */
    public void setFiles(String[] files)
    {
        for (String f : files)
        {
            filesArray.add(f);
        }
    }

    /** Clear the list of files to upload. */
    public void clearFiles()
    {
        filesArray.clear();
    }

    /**
     * @return Returns the java_version.
     */
    public String getJavaVersion()
    {
        return java_version;
    }

    /**
     * @param java_version The java_version to set.
     */
    public void setJavaVersion(String java_version)
    {
        if (java_version == null)
            java_version = "";

        this.java_version = java_version;
    }

    /**
     * @return Returns the java_classpath.
     */
    public String getJavaClasspath()
    {
        return java_classpath;
    }

    /**
     * @param java_classpath The java_classpath to set.
     */
    public void setJavaClasspath(String java_classpath)
    {
        if (java_classpath == null)
            java_classpath = "";

        this.java_classpath = java_classpath;
    }

    /**
     * @return Returns the os_name.
     */
    public String getOSName()
    {
        return os_name;
    }

    /**
     * @param os_name The os_name to set.
     */
    public void setOSName(String os_name)
    {
        if (os_name == null)
            os_name = "";

        this.os_name = os_name;
    }

    /**
     * @return Returns the os_arch.
     */
    public String getOSArch()
    {
        return os_arch;
    }

    /**
     * @param os_arch The os_arch to set.
     */
    public void setOSArch(String os_arch)
    {
        if (os_arch == null)
            os_arch = "";

        this.os_arch = os_arch;
    }

    /**
     * @return Returns the os_version.
     */
    public String getOSVersion()
    {
        return os_version;
    }

    /**
     * @param os_version The os_version to set.
     */
    public void setOSVersion(String os_version)
    {
        if (os_version == null)
            os_version = "";

        this.os_version = os_version;
    }

    /**
     * @return Returns the extra.
     */
    public String getExtra()
    {
        return extra;
    }

    /**
     * @param extra The extra to set.
     */
    public void setExtra(String extra)
    {
        if (extra == null)
            extra = "";

        this.extra = extra;
    }

    /**
     * @return Returns the error.
     */
    public Throwable getError()
    {
        return error;
    }

    /**
     * @param error The error to set.
     */
    public void setError(Throwable error)
    {
        this.error = error;
    }

    /**
     * @return Returns the comment_type.
     */
    public String getCommentType()
    {
        return comment_type;
    }

    /**
     * @param comment_type The comment_type to set.
     */
    public void setCommentType(String comment_type)
    {
        if (comment_type == null)
            comment_type = "";

        this.comment_type = comment_type;
    }

    /**
     * @return Returns the app_version.
     */
    public String getAppVersion()
    {
        return app_version;
    }

    /**
     * @param app_version The app_version to set.
     */
    public void setAppVersion(String app_version)
    {
        if (app_version == null)
            app_version = "";

        this.app_version = app_version;
    }

    /**
     * @return Returns the comment.
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment)
    {
        if (comment == null)
            comment = "";

        this.comment = comment;
    }

    /**
     * @return Returns the email.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email The email to set.
     */
    public void setEmail(String email)
    {
        if (email == null)
            email = "";

        this.email = email;
    }

    /**
     * @return Returns the session_id.
     */
    public String getToken()
    {
        return session_id;
    }

    /**
     * @param session_id The session_id to set.
     */
    public void setToken(String session_id)
    {
        if (session_id == null)
            session_id = "";

        this.session_id = session_id;
    }

    /**
     * @return Returns the selected_file.
     */
    public File getSelectedFile()
    {
        return selected_file;
    }

    /**
     * @param selected_file The selected_file to set.
     */
    public void setSelectedFile(File selected_file)
    {
        if (selected_file != null)
        {
            this.selected_file = selected_file;
            String parentPath = selected_file.getParent();
            setAbsolutePath(parentPath);
        }
    }

    /**
     * @return Returns the file_type.
     */
    public String getFileType()
    {
        return file_type;
    }

    /**
     * @param file_type The file_type to set.
     */
    public void setFileType(String file_type)
    {
        if (file_type == null)
            file_type = "";

        this.file_type = file_type;
    }


    /**
     * @return Returns the uploadURL.
     */
    public String getUploadUrl()
    {
        return uploadUrl;
    }


    /**
     * @param uploadURL The uploadURL to set.
     */
    public void setUploadUrl(String uploadURL)
    {
        this.uploadUrl = uploadURL;
    }

    /**
     * set file format for import
     *
     * @param file_format the file format
     */
    public void setReaderType(String file_format)
    {
        this.file_format = file_format;
    }

    /**
     * get the file format for import
     *
     * @return the file format
     */
    public String getFileFormat()
    {
        return file_format;
    }

    /**
     * set path for file
     *
     * @param absolute_path the file's absolute path
     */
    public void setAbsolutePath(String absolute_path)
    {
	this.absolute_path = absolute_path;

    }

    /**
     * get path for file
     * @return the file's absolute path
     */
    public String getAbsolutePath()
    {
        return absolute_path;
    }

	/**
	 * return the error status
	 *
	 * @return the error status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * set the status for this error
	 * @param status the error status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * get the index for this error container
	 * @return the error container's index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set index
	 * @param index the error container's index
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
