/*
 * Created on Mar 1, 2005
*/
package org.ome.srv.net;

import java.util.List;

import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
import org.ome.model.LSID;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

/**
 * @author josh
 */
/**
 * JAX-RPC compliant RemoteAccountService implementation that simply delegates
 * to the AccountService implementation in the root web application context.
 *
 * This wrapper class is necessary because JAX-RPC requires working with
 * RMI interfaces. If an existing service needs to be exported, a wrapper that
 * extends ServletEndpointSupport for simple application context access is
 * the simplest JAX-RPC compliant way.
 *
 * This is the class registered with the server-side JAX-RPC implementation.
 * In the case of Axis, this happens in "server-config.wsdd" respectively via
 * deployment calls. The Web Service tool manages the life-cycle of instances
 * of this class: A Spring application context can just be accessed here.
 */
public class ImageServiceEndpoint extends ServletEndpointSupport implements ImageService {
    ImageService is;
    protected void onInit() {
        this.is = (ImageService) getWebApplicationContext().getBean("imageService");
    }
    public IImage retrieveImage(LSID lsid) {
        return is.retrieveImage(lsid);
    }

    // ===============================
    
//    public void insertAccount(Account acc) throws RemoteException {
//        biz.insertAccount(acc);
//    }
  
    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID)
     */
    public List retrieveImagesByExperimenter(LSID arg0) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
     */
    public List retrieveImagesByExperimenter(LSID arg0, LSID arg1) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }


    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#retrieveImage(org.ome.model.LSID, org.ome.model.LSID)
     */
    public IImage retrieveImage(LSID arg0, LSID arg1) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#retrieveImagesByDataset(org.ome.model.LSID)
     */
    public List retrieveImagesByDataset(LSID arg0) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#retrieveImagesByProject(org.ome.model.LSID)
     */
    public List retrieveImagesByProject(LSID arg0) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#updateImage(org.ome.model.IImage)
     */
    public void updateImage(IImage arg0) {
        // TODO Auto-generated method stub
        /*  */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.ome.interfaces.ImageService#setImage(org.ome.model.IImage)
     */
    public void setImage(IImage arg0) {
        // TODO Auto-generated method stub
        /*  */
        throw new RuntimeException("implement me");
    }
  
}	
