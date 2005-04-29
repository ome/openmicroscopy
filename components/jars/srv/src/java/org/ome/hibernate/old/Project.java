/*
 * Created on Apr 20, 2005
*/
package org.ome.hibernate.old;

import java.util.HashSet;
import java.util.Set;

/**
 * @author josh
 */
public class Project {

    private Long id;
    private Set datasets = new HashSet();
    
    /**
     * @return Returns the datasets.
     */
    public Set getDatasets() {
        return datasets;
    }
    /**
     * @param datasets The datasets to set.
     */
    public void setDatasets(Set datasets) {
        this.datasets = datasets;
    }
    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }
}
