import omero.LockTimeout;
import omero.ServerError;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.api.ServiceFactoryPrx;
import omero.grid.DeleteCallbackI;
import omero.model.*;
import static omero.rtypes.*;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import java.util.List;
import java.util.ArrayList;
/**
 * Uses the default {@link DeleteCallbackI} instance
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


            String graph_spec = "/Annotation";
            // options = {}
            Long faId = fa.getId().getValue();
            Delete delCmd = new Delete(graph_spec, faId, null);

            List<Delete> dcs = new ArrayList<Delete>();
            DoAll doall = new DoAll();
            // doall.requests = dcs;


            // dcs = [delCmd]
            // doall = omero.cmd.DoAll()
            // doall.requests = dcs
            // handle = s.submit(doall)

            // callback = omero.callbacks.CmdCallbackI(c, handle)
            // loops = 10
            // delay = 500
            // callback.loop(loops, delay) # Throw LockTimeout
            // rsp = callback.getResponse()



            // IDeletePrx deleteServicePrx = s.getDeleteService();
            // DeleteCommand dc = new DeleteCommand("/Annotation", fa.getId().getValue(), null);
            // DeleteHandlePrx deleteHandlePrx = deleteServicePrx
            //         .queueDelete(new DeleteCommand[] { dc });
            // DeleteCallbackI cb = new DeleteCallbackI(c, deleteHandlePrx);
            // try {

            //     cb.loop(10, 500);

            //     DeleteReport[] reports = deleteHandlePrx.report();
            //     DeleteReport r = reports[0]; // We only sent one command
            //     System.out.println(String.format(
            //             "Report:error=%s,warning=%s,deleted=%s", r.error,
            //             r.warning, r.actualDeletes));

            // } catch (LockTimeout lt) {
            //     System.out.println("Not finished in 5 seconds. Cancelling...");
            //     if (!deleteHandlePrx.cancel()) {
            //         System.out.println("ERROR: Failed to cancel");
            //     }
            // } finally {
            //     cb.close();
            // }

        } finally {
            c.closeSession();
        }

    }

}
