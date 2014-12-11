import omero.LockTimeout;
import omero.ServerError;
import omero.client;
import omero.api.ServiceFactoryPrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.api.delete.DeleteReport;
import omero.grid.DeleteCallbackI;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * Subclasses {@link DeleteCallbackI}
 */
public class Subclass extends DeleteCallbackI {

    public Subclass(client client, DeleteHandlePrx handle) throws ServerError {
        super(client, handle);
    }

    @Override
    public void finished(int errors) {
        super.finished(errors);
        System.out.println("Finished. Error count=" + errors);
        try {
            DeleteReport[] reports = handle.report();
            for (DeleteReport r : reports) {
                System.out.println(String.format(
                        "Report:error=%s,warning=%s,deleted=%s", r.error,
                        r.warning, r.actualDeletes));
            }
        } catch (ServerError se) {
            System.out.println("Something happened to the handle?!?");
        }
    }

    public static void main(String[] args) throws CannotCreateSessionException,
            PermissionDeniedException, ServerError {

        omero.client c = new omero.client();
        ServiceFactoryPrx s = c.createSession();

        try {
            IDeletePrx deleteServicePrx = s.getDeleteService();
            DeleteCommand dc = new DeleteCommand("/Image", 1, null);
            DeleteHandlePrx deleteHandlePrx = deleteServicePrx
                    .queueDelete(new DeleteCommand[] { dc });
            Subclass cb = new Subclass(c, deleteHandlePrx);
            try {

                cb.loop(10, 500);
                // If we reach here, finished() was called.

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
