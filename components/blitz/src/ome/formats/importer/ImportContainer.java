/*
 * ome.formats.importer.gui.GuiCommonElements
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

package ome.formats.importer;

import java.io.File;
import java.util.List;

import omero.model.Annotation;
import omero.model.IObject;
import omero.model.Pixels;

public class ImportContainer
{
	private String reader;
    private String[] usedFiles;
    private Boolean isSPW;
	private File file;
    private Double[] userPixels;
    private String customImageName;
    private String customImageDescription;
    private String customPlateName;
    private String customPlateDescription;
    private boolean doThumbnails = true;
    private boolean useMetadataFile = true;
    private List<Annotation> customAnnotationList;
    private IObject target;

	public ImportContainer(File file,
			IObject target,
			Double[] userPixels, String reader, String[] usedFiles, Boolean isSPW)
	{
		this.file = file;
		this.target = target;
		this.userPixels = userPixels;
		this.reader = reader;
		this.usedFiles = usedFiles;
		this.isSPW = isSPW;
	}

	// Various Getters and Setters //

    /**
     * Retrieves whether or not we are performing thumbnail creation upon
     * import completion.
     * return <code>true</code> if we are to perform thumbnail creation and
     * <code>false</code> otherwise.
     * @since OMERO Beta 4.3.0.
     */
    public boolean getDoThumbnails()
    {
        return doThumbnails;
    }

    /**
     * Sets whether or not we are performing thumbnail creation upon import
     * completion.
     * @param v <code>true</code> if we are to perform thumbnail creation and
     * <code>false</code> otherwise.
     * @since OMERO Beta 4.3.0.
     */
    public void setDoThumbnails(boolean v)
    {
        doThumbnails = v;
    }

    /**
     * Retrieves the current custom image name string.
     * @return As above. <code>null</code> if it has not been set.
     */
    public String getCustomImageName()
    {
        return customImageName;
    }

    /**
     * Sets the custom image name for import. If this value is left
     * null, the image description supplied by Bio-Formats will be used.
     * @param v A custom image name to use for all images represented
     * by this container.
     */
    public void setCustomImageName(String v)
    {
        customImageName = v;
    }

    /**
     * Retrieves the current custom image description string.
     * @return As above. <code>null</code> if it has not been set.
     * @since OMERO Beta 4.2.1.
     */
    public String getCustomImageDescription()
    {
        return customImageDescription;
    }

    /**
     * Sets the custom image description for import. If this value is left
     * null, the image description supplied by Bio-Formats will be used.
     * @param v A custom image description to use for all images represented
     * by this container.
     * @since OMERO Beta 4.2.1.
     */
    public void setCustomImageDescription(String v)
    {
        customImageDescription = v;
    }

    /**
     * Retrieves the current custom plate name string.
     * @return As above. <code>null</code> if it has not been set.
     * @since OMERO Beta 4.2.1.
     */
    public String getCustomPlateName()
    {
        return customPlateName;
    }

    /**
     * Sets the custom plate name for import. If this value is left
     * null, the plate description supplied by Bio-Formats will be used.
     * @param v A custom plate name to use for all plates represented
     * by this container.
     * @since OMERO Beta 4.2.1.
     */
    public void setCustomPlateName(String v)
    {
        customPlateName = v;
    }

    /**
     * Retrieves the current custom plate description string.
     * @return As above. <code>null</code> if it has not been set.
     * @since OMERO Beta 4.2.1.
     */
    public String getCustomPlateDescription()
    {
        return customPlateDescription;
    }

    /**
     * Sets the custom plate description for import. If this value is left
     * null, the plate description supplied by Bio-Formats will be used.
     * @param v A custom plate description to use for all plates represented
     * by this container.
     * @since OMERO Beta 4.2.1.
     */
    public void setCustomPlateDescription(String v)
    {
        customPlateDescription = v;
    }

    /**
     * Whether or not we're using an original metadata file.
     * @return See above.
     * @since OMERO Beta 4.2.1.
     */
    public boolean getUseMetadataFile()
    {
        return useMetadataFile;
    }

    /**
     * Sets whether or not we're using an original metadata file.
     * @since OMERO Beta 4.2.1.
     */
    public void setUseMetadataFile(boolean v)
    {
        useMetadataFile = v;
    }

    /**
     * The list of custom, user specified, annotations to link to all images
     * represented by this container.
     * @return See above.
     * @since OMERO Beta 4.2.1.
     */
    public List<Annotation> getCustomAnnotationList()
    {
        return customAnnotationList;
    }

    /**
     * Sets the list of custom, user specified, annotations to link to all
     * images represented by this container.
     * @param v The list of annotations to use.
     * @since OMERO Beta 4.2.1.
     */
    public void setCustomAnnotationList(List<Annotation> v)
    {
        customAnnotationList = v;
    }

    public String getReader() {
		return reader;
	}

	public void setReader(String reader) {
		this.reader = reader;
	}

	public String[] getUsedFiles() {
		return usedFiles;
	}

	public void setUsedFiles(String[] usedFiles) {
		this.usedFiles = usedFiles;
	}

	public Boolean getIsSPW() {
		return isSPW;
	}

	public void setIsSPW(Boolean isSPW) {
		this.isSPW = isSPW;
	}

	/**
	 * @return the File
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Package-private setter added during the 4.1 release to fix name ordering.
	 * A better solution would be to have a copy-constructor which also takes
	 * a chosen file.
	 */
	void setFile(File file)
	{
	    this.file = file;
	}

	public IObject getTarget()
	{
	    return target;
	}

	public void setTarget(IObject obj) {
	    this.target = obj;
	}

	public Double[] getUserPixels()
	{
        return userPixels;
    }

    public void setUserPixels(Double[] userPixels)
    {
        this.userPixels = userPixels;
    }

}
