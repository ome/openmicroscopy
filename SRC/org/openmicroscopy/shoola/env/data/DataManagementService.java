package org.openmicroscopy.shoola.env.data;


/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public interface DataManagementService {
    
    public DataTreeDTO retrieveDataTree();
    public ProjectDTO retrieveProject(int id);
    public DatasetDTO retrieveDataset(int id);
    public ImageDTO retriveImage(int id);
    public void saveProject(ProjectDTO projectDTO);
    public void saveDataset(DatasetDTO datasetDTO);
    public void saveImage(ImageDTO imageDTO);
    
    
}
