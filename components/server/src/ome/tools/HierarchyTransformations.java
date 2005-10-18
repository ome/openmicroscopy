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

import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;

//Java imports

//Third-party libraries

//Application-internal dependencies
public class HierarchyTransformations {

	public static Set invertPDI(Set imagesAll) {
		
		Set hierarchies = new HashSet();
        Iterator i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) i.next();
            Set datasets = img.getDatasets();

            if (datasets == null || datasets.size() < 1) {
                hierarchies.add(img);
            } else {
                Iterator d = datasets.iterator();
                while (d.hasNext()) {
                    Dataset ds = (Dataset) d.next();

                    if (!(ds.getImages() instanceof HashSet))
                        ds.setImages(new HashSet());
                    ds.getImages().add(img);

                    Set projects = ds.getProjects();
                    if (projects == null || projects.size() < 1) {
                        hierarchies.add(ds);
                    } else {
                        Iterator p = projects.iterator();
                        while (p.hasNext()) {
                            Project prj = (Project) p.next();

                            if (!(prj.getDatasets() instanceof HashSet))
                                prj.setDatasets(new HashSet());
                            prj.getDatasets().add(ds);

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
            Set classifications = img.getClassifications();

            if (classifications == null || classifications.size() < 1) {
                hierarchies.add(img);
            } else {
                Iterator c = classifications.iterator();
                while (c.hasNext()) {
                    Classification cla = (Classification) c.next();

                    cla.setImage(img);

                    Category ca = cla.getCategory();
                    if (null == ca) {
                        hierarchies.add(cla);
                    } else {
                        if (!(ca.getClassifications() instanceof HashSet))
                            ca.setClassifications(new HashSet());
                        ca.getClassifications().add(cla);
                        
                        CategoryGroup cg = ca.getCategoryGroup();
                        if (cg == null) {
                            hierarchies.add(ca);
                        } else {
                            if (!(cg.getCategories() instanceof HashSet))
                                cg.setCategories(new HashSet());
                            cg.getCategories().add(ca);
                            hierarchies.add(cg);
                        }
                    }
                }
            }
        }
		return hierarchies;
	}

	
}
