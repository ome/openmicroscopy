package org.openmicroscopy.shoola.env.data;

// Java imports
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClientLite;


/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class XmlRpcGateway {
    
    static final String procedureNameServer = "dispatch";
    XmlRpcClientLite    server;
    
    XmlRpcGateway() {
        // retrieve info from configuration host -port OMEDS
        // from registry 
        try {
            server = new XmlRpcClientLite("");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    RemoteCall doCall(RemoteCall rc) {
        Vector  params = new Vector();
        // prepare statement for XML-RPC call
        params.add(rc.getSessionRef());
        params.add(rc.getProcedureName());
        params.add(rc.getParameters()); // check Perl code 
        try {
            rc.setOutput(server.execute(procedureNameServer, params));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return rc;
    }
 
    
}
