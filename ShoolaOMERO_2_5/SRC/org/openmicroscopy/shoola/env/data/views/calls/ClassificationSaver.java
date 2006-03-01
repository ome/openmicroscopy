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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

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

    /** The category to add to or remove from. */
    private Set             categories;
    
    /** The image to classifiy or declassify. */
    private int             imageID;
    
    /** Classify/declassify call. */
    private BatchCall       saveCall;
    
    /** The result of the save action. */
    private Object          result;
    
    //Tempo method: To remove when we use OMERO to write 
    private CategoryData transformPojoCategoryData(pojos.CategoryData data)
    {
        return new CategoryData(data.getId(), data.getName(),
                                data.getDescription());
    }
    
    /**
     * Creates a {@link BatchCall} to add the specified Images to the
     * given Category.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall classify()
    {
        return new BatchCall("Saving classification tree.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(1);
                list.add(new Integer(imageID));
                Iterator i = categories.iterator();
                CategoryData data;
                while (i.hasNext()) {
                    data = transformPojoCategoryData(
                                (pojos.CategoryData) i.next());
                    sts.updateCategory(data, null, list);
                }
                result = categories;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to remove the specified Images from the
     * given Category.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall declassify()
    {
        return new BatchCall("Declassifying images.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                ArrayList list = new ArrayList(1);
                list.add(new Integer(imageID));
                Iterator i = categories.iterator();
                CategoryData data;
                while (i.hasNext()) {
                    data = transformPojoCategoryData(
                                (pojos.CategoryData) i.next());
                    sts.updateCategory(data, list, null);
                }
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
     * Classifies or declassifies the specified image.
     * 
     * @param imageID       The id of the image.
     * @param categories    The categories to add the image to or remove the
     *                      image from.
     * @param classify      Passed <code>true</code> to classify, 
     *                      <code>false</code> to declassify.
     */
    public ClassificationSaver(int imageID, Set categories, boolean classify)
    {
        if (imageID < 0)
            throw new IllegalArgumentException("Image id not valid.");
        if (categories == null)
            throw new NullPointerException("No category.");
        try {
            categories.toArray(new pojos.CategoryData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "categories has only CategoryData elements.");
        }
        this.imageID = imageID;
        this.categories = categories;
        if (classify) saveCall = classify();
        else saveCall = declassify();
    }
    
}
