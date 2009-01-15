/*
 * ome.tools.HierarchyTransformations
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.CBlock;

public class HierarchyTransformations {

    /**
     * 
     * @param imagesAll
     * @param block
     * @return a Set with {@link Image}, {@link Dataset}, and {@link Project},
     *         instances.
     */
    public static <T extends IObject> Set<T> invertPDI(Set<Image> imagesAll,
            CBlock<T> block) {

        Set<T> cleared = new HashSet<T>();
        Set<T> hierarchies = new HashSet<T>();
        Iterator<Image> i = imagesAll.iterator();
        while (i.hasNext()) {
            Image img = (Image) block.call(i.next());

            // Copy needed to prevent ConcurrentModificationExceptions
            List<Dataset> d_list = img.linkedDatasetList();
            Iterator<Dataset> d = d_list.iterator();
            if (!d.hasNext()) {
                hierarchies.add((T) img);
            } else {
                while (d.hasNext()) {
                    Dataset ds = (Dataset) block.call(d.next());

                    if (!cleared.contains(ds)) {
                        // ds.clearImageLinks();
                        ds.putAt(Dataset.IMAGELINKS, new HashSet());
                        cleared.add((T) ds);
                    }
                    ds.linkImage(img);

                    // Copy needed to prevent ConcurrentModificationExceptions
                    List<Project> p_list = ds.linkedProjectList();
                    Iterator<Project> p = p_list.iterator();
                    if (!p.hasNext()) {
                        hierarchies.add((T) ds);
                    } else {
                        while (p.hasNext()) {
                            Project prj = (Project) block.call(p.next());

                            if (!cleared.contains(prj)) {
                                // prj.clearDatasetLinks();
                                prj.putAt(Project.DATASETLINKS, new HashSet());
                                cleared.add((T) prj);
                            }
                            prj.linkDataset(ds);

                            hierarchies.add((T) prj);
                        }
                    }

                }
            }
        }
        return hierarchies;
    }

}
