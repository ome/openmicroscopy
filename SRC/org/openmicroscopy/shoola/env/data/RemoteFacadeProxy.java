package org.openmicroscopy.shoola.env.data;

// Java imports
import java.util.Vector;
/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
class RemoteFacadeProxy  
    implements DataManagementService {
    
    private XmlRpcGateway   xmlRpxGW;
    private String          sessionRef;
    
    RemoteFacadeProxy() {
    }
    
/** Implemented as specified by {@linkDataManagementService}.
 */ 
    public DataTreeDTO retrieveDataTree() {
        // grab sessionRef 
        RemoteCall rc = new RemoteCall("retrieveDataTree", sessionRef);
        xmlRpxGW.doCall(rc);
        return null;
    }
/** Implemented as specified by {@linkDataManagementService}.
 */
    public ProjectDTO retrieveProject(int id) {
        return null;
    }
/** Implemented as specified by {@linkDataManagementService}.
 */
    public DatasetDTO retrieveDataset(int id) {
        return null;
    }
    public ImageDTO retriveImage(int id) {
        return null;
    }
/** Implemented as specified by {@linkDataManagementService}.
 */ 
    public void saveProject(ProjectDTO projectDTO) {
    }
/** Implemented as specified by {@linkDataManagementService}.
 */ 
    public void saveDataset(DatasetDTO datasetDTO) {
    }
/** Implemented as specified by {@linkDataManagementService}.
 */    
    public void saveImage(ImageDTO imageDTO) {
    }
    
    
}
