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
		
		Set hierarchies = new HashSet();
        Iterator i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) i.next();
            Set datasets = img.getDatasetLinks();

            if (datasets == null || datasets.size() < 1) {
                hierarchies.add(img);
            } else {
                Iterator d = datasets.iterator();
                while (d.hasNext()) {
                    DatasetImageLink dil = (DatasetImageLink) d.next();
                    Dataset ds = dil.getDataset();

                    if (!(ds.getImageLinks() instanceof HashSet))
                        ds.setImageLinks(new HashSet());
                    DatasetImageLink idl = new DatasetImageLink();
                    idl.setImage(img);
                    idl.setDataset(ds);
                    ds.getImageLinks().add(idl); // TODO addXXX meth.

                    Set projects = ds.getProjectLinks();
                    if (projects == null || projects.size() < 1) {
                        hierarchies.add(ds);
                    } else {
                        Iterator p = projects.iterator();
                        while (p.hasNext()) {
                            ProjectDatasetLink pdl = 
                                (ProjectDatasetLink) p.next();
                            Project prj = pdl.getProject();

                            if (!(prj.getDatasetLinks() instanceof HashSet))
                                prj.setDatasetLinks(new HashSet());
                            ProjectDatasetLink dpl = new ProjectDatasetLink();
                            dpl.setDataset(ds);
                            dpl.setProject(prj);
                            prj.getDatasetLinks().add(dpl); // TODO addXXX meth

                            hierarchies.add(prj);
                        }
                    }

                }
            }
        }
		return hierarchies;
	}
	
	public static Set invertCGCI(Set imagesAll) {

        Set hierarchies = new HashSet();
        Iterator i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) i.next();
            Set categories = img.getCategoryLinks();

            if (categories == null || categories.size() < 1) {
                hierarchies.add(img);
            } else {
                Iterator c = categories.iterator();
                while (c.hasNext()) {
                    CategoryImageLink cil = (CategoryImageLink) c.next();
                    Category ca = cil.getCategory();

                    if (!(ca.getImageLinks() instanceof HashSet))
                        ca.setImageLinks(new HashSet());
                    CategoryImageLink icl = new CategoryImageLink();
                    icl.setCategory(ca);
                    icl.setImage(img);
                    ca.getImageLinks().add(icl);
                    
                    Set cgroups = ca.getCategoryGroupLinks();
                    if (cgroups == null || cgroups.size() < 1) {
                        hierarchies.add(ca);
                    } else {
                        Iterator g = cgroups.iterator();
                        while (g.hasNext()){
                            CategoryGroupCategoryLink cgcl =
                                (CategoryGroupCategoryLink) g.next();
                            CategoryGroup cg = cgcl.getCategorygroup();
                            
                            if (!(cg.getCategoryLinks() instanceof HashSet))
                                cg.setCategoryLinks(new HashSet());
                            CategoryGroupCategoryLink ccgl =
                                new CategoryGroupCategoryLink();
                            ccgl.setCategory(ca);
                            ccgl.setCategorygroup(cg);
                            cg.getCategoryLinks().add(ccgl);
                            
                        }
                    }
                }
            }
        }
		return hierarchies;
	}

	
}
