/*
 * org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.data.views.calls;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
* Command to classify or declassify Images.
* This command can be created to either add Images to a given Category or
* remove them. The object returned in the <code>DSCallOutcomeEvent</code>
* will be <code>null</code>, as the type of the underlying calls is
* <code>void</code>.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">
* 					a.falconi@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
public class ClassificationSaver
 	extends BatchCallTree
{
	
	/** Classify/declassify call. */
	private BatchCall       saveCall;

	/** The result of the save action. */
	private Object          result;

	/**
	 * Creates a {@link BatchCall} to add the specified image to the
	 * given categories.
	 * 
	 * @param images        The images to classify.
	 * @param categories    The categories to add to. 
	 * @return              The {@link BatchCall}.
	 */
	private BatchCall classify(final Set images, final Set categories)
	{
		return new BatchCall("Saving classification tree.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				result = os.classify(images, categories);
			}
		};
	}

	/**
	 * Creates a {@link BatchCall} to add the specified image to the
	 * given categories.
	 * 
	 * @param containers    The folder containing the images to classify.
	 * @param categories    The categories to add to. 
	 * @return              The {@link BatchCall}.
	 */
	private BatchCall classifyChildren(final Set containers, 
			final Set categories)
	{
		return new BatchCall("Saving classification tree.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				result = os.classifyChildren(containers, categories);
			}
		};
	}

	/**
	 * Creates a {@link BatchCall} to add the specified image to the
	 * given categories.
	 * 
	 * @param ref			The time reference.
	 * @param categories    The categories to add to. 
	 * @return              The {@link BatchCall}.
	 */
	private BatchCall classifyChildren(final TimeRefObject ref, 
			final Set categories)
	{
		return new BatchCall("Saving classification tree.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				long userID = ((ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS)).getId();
				Set images =  os.getImagesPeriod(ref.getStartTime(), 
										ref.getEndTime(), userID);
				if (images != null) {
					result = os.classify(images, categories);
				} else result = new HashSet();
			}
		};
	}

	/**
	 * Creates a {@link BatchCall} to remove the specified image from the
	 * given categories.
	 * 
	 * @param images        The images to declassify.
	 * @param categories    The categories to add to.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall declassify(final Set images, final Set categories)
	{
		return new BatchCall("Declassifying images.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				result = os.declassify(images, categories);
			}
		};
	}

	/**
	 * Creates a {@link BatchCall} to create categories and to add
	 * the image to the newly created categories .
	 * 
	 * @param imageIDs      The images to classify.
	 * @param categories    The categories to create.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall createAndClassify(final Set<Long> imageIDs, 
			final Set<CategoryData> categories)
	{
		return new BatchCall("Classifying images.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				Iterator i = categories.iterator();
				CategoryData cat;
				Set<CategoryData> newOnes = 
					new HashSet<CategoryData>(categories.size());
				while (i.hasNext()) {
					cat = (CategoryData) 
					os.createDataObject((CategoryData) i.next(), null, null);
					newOnes.add(cat);
				}
				Set<ImageData> images = new HashSet<ImageData>(imageIDs.size());
				i = imageIDs.iterator();
				ImageData img;
				while (i.hasNext()) {
					img = new ImageData();
					img.setId((Long) i.next());
					images.add(img);
				}
				result = os.classify(images, newOnes); 
			}
		};
	}

	/**
	 * Adds the {@link #saveCall} to the computation tree.
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree() { add(saveCall); }

	/**
	 * Returns <code>null</code>, as the return type of the underlying call
	 * <code>void</code>.
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return result; }

	/**
	 * Classifies or declassifies the specified images.
	 * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param images        The images to handle.
	 * @param categories    The categories to add the image to or remove the
	 *                      image from.
	 * @param classify      Passed <code>true</code> to classify, 
	 *                      <code>false</code> to declassify.
	 */
	public ClassificationSaver(Set<ImageData> images, 
			Set<CategoryData> categories,
			boolean classify)
	{
		if (images == null)
			throw new IllegalArgumentException("No images to classify or " +
			"declassify.");
		if (categories == null)
			throw new NullPointerException("No category to add to or remove " +
			"from.");
		if (classify) saveCall = classify(images, categories);
		else saveCall = declassify(images, categories);
	}

	/**
	 * Classifies the images contained in the specified folders.
	 * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param containers    The images to handle.
	 * @param categories    The categories to add the image to or remove the
	 *                      image from.
	 * @param classify      Passed <code>true</code> to classify, 
	 *                      <code>false</code> to declassify.
	 */
	public ClassificationSaver(Set<DataObject> containers, 
			Set<CategoryData> categories)
	{
		if (containers == null)
			throw new IllegalArgumentException("No images to classify or " +
			"declassify.");
		if (categories == null)
			throw new NullPointerException("No category to add to or remove " +
			"from.");
		saveCall = classifyChildren(containers, categories);
	}

	/**
	 * Classifies the images imported during a period of time.
	 * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param ref    		The time reference object.
	 * @param categories    The categories to add the images to.
	 */
	public ClassificationSaver(TimeRefObject ref, 
			Set<CategoryData> categories)
	{
		if (ref == null)
			throw new IllegalArgumentException("No Period specified.");
		if (categories == null)
			throw new NullPointerException("No category to add to or remove " +
			"from.");
		saveCall = classifyChildren(ref, categories);
	}

}
