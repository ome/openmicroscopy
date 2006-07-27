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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.containers.Project;

//Java imports

//Third-party libraries

//Application-internal dependencies
public class HierarchyTransformations {

	public static Set invertPDI(Set imagesAll) {

        Set cleared = new HashSet();
		Set hierarchies = new HashSet();
        Iterator i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) i.next();

            // Copy needed to prevent ConcurrentModificationExceptions
            List<Dataset> d_list = img.linkedDatasetList();
            Iterator d = d_list.iterator();
            if ( ! d.hasNext() ) {
                hierarchies.add(img);
            } else {
                while (d.hasNext()) {
                    Dataset ds = (Dataset) d.next();
                    
                    if ( ! cleared.contains( ds ))
                    {
                        ds.clearImageLinks();
                        cleared.add( ds );
                    }
                    ds.linkImage( img );

                    // Copy needed to prevent ConcurrentModificationExceptions
                    List<Project> p_list = ds.linkedProjectList();
                    Iterator p = p_list.iterator();
                    if ( ! p.hasNext() ) {
                        hierarchies.add( ds );
                    } else {
                        while (p.hasNext()) {
                            Project prj = (Project) p.next();

                            if ( ! cleared.contains( p ))
                            {
                                prj.clearDatasetLinks();
                                cleared.add( prj );
                            }
                            prj.linkDataset( ds );

                            hierarchies.add(prj);
                        }
                    }

                }
            }
        }
		return hierarchies;
	}
	
	public static Set invertCGCI(Set imagesAll) {

        Set cleared = new HashSet();
        Set hierarchies = new HashSet();
        Iterator i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) i.next();
            
            // Copy needed to prevent ConcurrentModificationExceptions
            List<Category> c_list = img.linkedCategoryList();
            Iterator c = c_list.iterator();
            if ( ! c.hasNext() ) {
                hierarchies.add(img);
            } else {
                while (c.hasNext()) {
                    Category ca = (Category) c.next();

                    if ( ! cleared.contains( ca ))
                    {
                        ca.clearImageLinks();
                        cleared.add( ca );
                    }
                    ca.linkImage( img );

                    // Copy needed to prevent ConcurrentModificationExceptions
                    List<CategoryGroup> cg_list = ca.linkedCategoryGroupList();
                    Iterator g = cg_list.iterator();
                    if ( ! g.hasNext() ) {
                        hierarchies.add(ca);
                    } else {
                        while (g.hasNext()){
                            CategoryGroup cg = (CategoryGroup) g.next();
                            
                            if ( ! cleared.contains( cg ))
                            {
                                cg.clearCategoryLinks();
                                cleared.add( cg );
                            }
                            cg.linkCategory( ca );
                            
                            hierarchies.add( cg );
                        }
                    }
                }
            }
        }
		return hierarchies;
	}

}
