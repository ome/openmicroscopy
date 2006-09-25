/*
 * org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
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
                os.classify(images, categories);
                result = categories;
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
                os.declassify(images, categories);
                result = categories;
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
     * 
     * @param images        The images to handle.
     * @param categories    The categories to add the image to or remove the
     *                      image from.
     * @param classify      Passed <code>true</code> to classify, 
     *                      <code>false</code> to declassify.
     */
    public ClassificationSaver(Set images, Set categories,
                                boolean classify)
    {
        if (images == null)
            throw new IllegalArgumentException("No images to classify or " +
                    "declassify.");
        if (categories == null)
            throw new NullPointerException("No category to add to or remove " +
                    "from.");
        try {
            categories.toArray(new CategoryData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "categories only contains CategoryData elements.");
        }
        try {
            images.toArray(new ImageData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "images only contains ImageData elements.");
        }
        if (classify) saveCall = classify(images, categories);
        else saveCall = declassify(images, categories);
    }
    
}
