/*
 * org.openmicroscopy.shoola.env.ui.SaveAsActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.io.File;
import java.util.Map;

//Third-party libraries
import org.apache.commons.io.FilenameUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import omero.gateway.SecurityContext;

import pojos.FileAnnotationData;

/** 
 * The activity associated to the Save as action i.e. save a collection of
 * images as JPEG.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SaveAsActivity
	extends ActivityComponent
{

	/** The description of the activity. */
	private static final String DESCRIPTION_CREATION = "Saving Images as ";
	
	/** The description of the activity when finished. */
	private static final String DESCRIPTION_CREATED = "Images saved in";

	/** The description of the activity when cancelled. */
	private static final String DESCRIPTION_CANCEL = "Images saving cancelled";

	/** The parameters hosting information about the images to save. */
    private SaveAsParam	parameters;

    /**
     * Returns the name of the file.
     *
     * @param name The name to handle.
     * @return See above.
     */
    private String getFileName(String name)
    {
        File directory = parameters.getFolder();
        File[] files = directory.listFiles();
        String dirPath = directory.getAbsolutePath() + File.separator;
        String extension = "."+FilenameUtils.getExtension(name);
        return getFileName(files, name, name, dirPath, 1, extension);
    }

    /**
     * Creates a new instance.
     *
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param parameters The parameters used to save the collection of images.
     */
	public SaveAsActivity(UserNotifier viewer,  Registry registry,
			SecurityContext ctx, SaveAsParam parameters)
	{
		super(viewer, registry, ctx);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		initialize(DESCRIPTION_CREATION+parameters.getIndexAsString(),
				parameters.getIcon());
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new SaveAsLoader(viewer, registry, ctx, parameters, this);
		return loader;
	}

	
	@Override
	public void endActivity(Object result) {
		if(parameters.isDeleteWhenFinished()) {
			if (result instanceof Map) {
				FileAnnotationData fa = null;
				Map<String, Object> m = (Map<String, Object>) result;
				for(Object obj : m.values()) {
					if(obj instanceof FileAnnotationData) {
						fa = (FileAnnotationData) obj;
						break;
					}
				}
				
				if(fa!=null && fa.isLoaded()) {
					download("", fa, new File(parameters.getFolder(), fa.getFileName()), parameters.isDeleteWhenFinished());
					// call super method to stop busy label
					super.endActivity(null);
				}
				else
					super.endActivity(result);
			}
		}
		else
			super.endActivity(result);
	}

	/**
	 * Modifies the text of the component.
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		if (!parameters.isDeleteWhenFinished()) {
			// Download the file.
			if (result instanceof FileAnnotationData) {
				FileAnnotationData data = (FileAnnotationData) result;
				String name = "";
				if (data.isLoaded())
					name = data.getFileName();
				else
					name = "Annotation_" + data.getId();
				name = getFileName(name);
				download("", result, new File(parameters.getFolder(), name),
						false);
			}

			type.setText(DESCRIPTION_CREATED + " "
					+ parameters.getFolder().getName());
		}
	}

	/**
	 * Modifies the text of the component.
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled()
	{
		type.setText(DESCRIPTION_CANCEL);
	}

	/** 
	 * No-operation in this case.
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}

}
