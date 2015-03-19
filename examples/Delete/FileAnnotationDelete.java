import omero.ServerError;
import omero.cmd.CmdCallbackI;
import omero.cmd.Delete2;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.api.ServiceFactoryPrx;
import omero.model.*;
import static omero.rtypes.*;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses a {@link Delete2} request instance
 * to delete a FileAnnotation along with its associated
 * OriginalFile and any annotation links.
 */
public class FileAnnotationDelete {

    public static void main(String[] args) throws CannotCreateSessionException,
            PermissionDeniedException, ServerError, java.io.IOException {

        omero.client c = new omero.client();
        String ice_config = c.getProperty("Ice.Config");

        try {
            ServiceFactoryPrx s = c.createSession();

            Dataset d = new DatasetI();
            d.setName(rstring("FileAnnotationDelete"));
            FileAnnotation fa = new FileAnnotationI();
            OriginalFile file = c.upload(new java.io.File(ice_config));
            fa.setFile(file);
            d.linkAnnotation(fa);
            d = (Dataset) s.getUpdateService().saveAndReturnObject(d);
            fa = (FileAnnotation) d.linkedAnnotationList().get(0);


            Delete2 deleteCmd = new Delete2();
            List<Long> ids = Collections.singletonList(fa.getId().getValue());
            deleteCmd.targetObjects = new HashMap<String, List<Long>>();
            deleteCmd.targetObjects.put("Annotation", ids);
            Map<String, String> callContext = new HashMap<String, String>();
            CmdCallbackI cb = null;
            try {
                cb = new CmdCallbackI(c, s.submit(deleteCmd, callContext));
                cb.loop(10, 500);
                Response rsp = cb.getResponse();
                if (rsp instanceof OK) {
                    System.out.println("OK");
                }
            } catch (InterruptedException lt) {
                System.out.println("Not finished in 5 seconds. Cancelling...");
                if (!cb.isCancelled())
                    System.out.println("ERROR: Failed to cancel");
            } finally {
                if (cb != null) cb.close(true);
            }

        } finally {
            c.closeSession();
        }

    }

}
