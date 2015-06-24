import java.util.Map;
import java.util.HashMap;
import omero.LockTimeout;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * Passes a non-empty options map to specify
 * that all Annotations should be kept.
 */
public class Options {

    public static void main(String[] args) throws CannotCreateSessionException,
            PermissionDeniedException, ServerError {

        omero.client c = new omero.client();
        ServiceFactoryPrx s = c.createSession();

        try {
            IDeletePrx deleteServicePrx = s.getDeleteService();

            // The path here can either be relative or absolute, e.g.
            // /Image/ImageAnnotationLink/Annotation. "KEEP" specifies
            // that no delete should take place. "SOFT" specifies that
            // if a delete is not possible no error should be signaled.
            Map<String, String> options = new HashMap<String, String>();
            options.put("/Annotation", "KEEP");
            DeleteCommand dc = new DeleteCommand("/Image", 1, options);

            DeleteHandlePrx deleteHandlePrx = deleteServicePrx
                    .queueDelete(new DeleteCommand[] { dc });
            DeleteCallbackI cb = new DeleteCallbackI(c, deleteHandlePrx);
            try {

                cb.loop(10, 500);

                DeleteReport[] reports = deleteHandlePrx.report();
                DeleteReport r = reports[0]; // We only sent one command
                System.out.println(String.format(
                        "Report:error=%s,warning=%s,deleted=%s", r.error,
                        r.warning, r.actualDeletes));

            } catch (LockTimeout lt) {
                System.out.println("Not finished in 5 seconds. Cancelling...");
                if (!deleteHandlePrx.cancel()) {
                    System.out.println("ERROR: Failed to cancel");
                }
            } finally {
                cb.close();
            }

        } finally {
            c.closeSession();
        }

    }

}
