/*
 * Created on Apr 17, 2005
*/
package org.ome.hibernate;

import java.util.Set;

/**
 * @author josh
 */
public class Container {

    private Long id;
    private User owner;
    private Set childContainers;
    private String description;
    
    

    /**
     * @return Returns the childContainers.
     */
    public Set getChildContainers() {
        return childContainers;
    }
    /**
     * @param childContainers The childContainers to set.
     */
    public void setChildContainers(Set childContainers) {
        this.childContainers = childContainers;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
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
    /**
     * @return Returns the owner.
     */
    public User getOwner() {
        return owner;
    }
    /**
     * @param owner The owner to set.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }
}
