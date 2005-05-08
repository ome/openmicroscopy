/*
 * Created on Apr 20, 2005
*/
package org.ome.hibernate.old;

import java.util.HashSet;
import java.util.Set;

/**
 * @author josh
 */
public class Dataset {

    private Long id;
    private Set projects = new HashSet();
    private Set images = new HashSet();
    
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
    /**
     * @return Returns the images.
     */
    public Set getImages() {
        return images;
    }
    /**
     * @param images The images to set.
     */
    public void setImages(Set images) {
        this.images = images;
    }
    /**
     * @return Returns the projects.
     */
    public Set getProjects() {
        return projects;
    }
    /**
     * @param projects The projects to set.
     */
    public void setProjects(Set projects) {
        this.projects = projects;
    }
}
