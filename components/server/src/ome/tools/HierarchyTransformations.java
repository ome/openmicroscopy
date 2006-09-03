/* ome.tools.HierarchyTransformations
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.tools;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.containers.Project;
import ome.util.CBlock;


public class HierarchyTransformations {

	/**
	 * 
	 * @param imagesAll
	 * @param block
	 * @return a Set with {@link Image}, {@link Dataset}, and {@link Project},
	 * 		instances.
	 */
	public static <T extends IObject> Set<T> invertPDI(Set<Image> imagesAll, 
			CBlock<T> block) {

        Set<T> cleared = new HashSet<T>();
		Set<T> hierarchies = new HashSet<T>();
        Iterator<Image> i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) block.call( i.next() );

            // Copy needed to prevent ConcurrentModificationExceptions
            List<Dataset> d_list = img.linkedDatasetList();
            Iterator<Dataset> d = d_list.iterator();
            if ( ! d.hasNext() ) {
                hierarchies.add((T)img);
            } else {
                while (d.hasNext()) {
                    Dataset ds = (Dataset) block.call(d.next());
                    
                    if ( ! cleared.contains( ds ))
                    {
                        //ds.clearImageLinks();
                    	ds.putAt(Dataset.IMAGELINKS, new HashSet());
                        cleared.add( (T)ds );
                    }
                    ds.linkImage( img );

                    // Copy needed to prevent ConcurrentModificationExceptions
                    List<Project> p_list = ds.linkedProjectList();
                    Iterator<Project> p = p_list.iterator();
                    if ( ! p.hasNext() ) {
                        hierarchies.add( (T)ds );
                    } else {
                        while (p.hasNext()) {
                            Project prj = (Project) block.call(p.next());

                            if ( ! cleared.contains( p ))
                            {
                                //prj.clearDatasetLinks();
                            	prj.putAt(Project.DATASETLINKS, new HashSet());
                                cleared.add( (T)prj );
                            }
                            prj.linkDataset( ds );

                            hierarchies.add( (T)prj);
                        }
                    }

                }
            }
        }
		return hierarchies;
	}
	
	public static <T extends IObject> Set<T> invertCGCI(Set<Image> imagesAll, 
			CBlock<T> block) {

        Set<T> cleared = new HashSet<T>();
        Set<T> hierarchies = new HashSet<T>();
        Iterator<Image> i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) block.call(i.next());
            
            // Copy needed to prevent ConcurrentModificationExceptions
            List<Category> c_list = img.linkedCategoryList();
            Iterator<Category> c = c_list.iterator();
            if ( ! c.hasNext() ) {
                hierarchies.add( (T)img);
            } else {
                while (c.hasNext()) {
                    Category ca = (Category) block.call(c.next());

                    if ( ! cleared.contains( ca ))
                    {
                        //ca.clearImageLinks();
                    	ca.putAt(Category.IMAGELINKS, new HashSet());
                        cleared.add( (T)ca );
                    }
                    ca.linkImage( img );

                    // Copy needed to prevent ConcurrentModificationExceptions
                    List<CategoryGroup> cg_list = ca.linkedCategoryGroupList();
                    Iterator<CategoryGroup> g = cg_list.iterator();
                    if ( ! g.hasNext() ) {
                        hierarchies.add( (T)ca);
                    } else {
                        while (g.hasNext()){
                            CategoryGroup cg = (CategoryGroup) block.call(g.next());
                            
                            if ( ! cleared.contains( cg ))
                            {
                                //cg.clearCategoryLinks();
                            	cg.putAt(CategoryGroup.CATEGORYLINKS, new HashSet());
                                cleared.add( (T)cg );
                            }
                            cg.linkCategory( ca );
                            
                            hierarchies.add( (T)cg );
                        }
                    }
                }
            }
        }
		return hierarchies;
	}

}
