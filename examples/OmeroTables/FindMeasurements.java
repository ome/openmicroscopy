import java.util.Collections;
import java.util.List;
import java.util.Map;

import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.ServiceFactoryPrx;
import omero.constants.namespaces.NSMEASUREMENT;
import omero.grid.SharedResourcesPrx;
import omero.grid.TablePrx;
import omero.model.FileAnnotation;
import omero.model.IObject;

/**
 * Take an image id and find any attached measurements. If no image id is
 * passed, then will use {@link MeasurementTable} to create one.
 */
public class FindMeasurements {

    /**
     * The main method takes an optional long from the command line which should
     * be the id of a valid Image for the current user. If no id is provided, a
     * test image is created.
     */
    public static void main(String[] args) throws Exception {

        // Configuration for the client object in this case comes
        // from the ICE_CONFIG environment variable.
        omero.client client = new omero.client();
        client.createSession();

        try {

            // Get or create a valid Image id.
            long imageId;
            if (args.length > 0) {
                imageId = Long.valueOf(args[0]);
            } else {
                MeasurementTable table = new MeasurementTable(client, null);
                imageId = table.imageId;
            }

            new FindMeasurements(client, imageId);

        } finally {
            client.closeSession();
            System.exit(0);
        }
    }

    /**
     * Finds all measurement tables attached to the given image.
     */
    FindMeasurements(omero.client client, long imageId) throws ServerError {

        final ServiceFactoryPrx factory = client.getSession();
        final IAdminPrx admin = factory.getAdminService();
        final SharedResourcesPrx resources = factory.sharedResources();
        final IMetadataPrx metadata = factory.getMetadataService();

        final long userId = admin.getEventContext().userId;
        final Map<Long, List<IObject>> annotations = metadata.loadAnnotations(
                "Image", Collections.singletonList(imageId),
                null, null, null);
                //Collections.singletonList("Annotation"),  // Ways to limit the annotations
                //Collections.singletonList(userId), null); // by type or user.

        if (!annotations.containsKey(imageId)) {
            System.out.println("No annotations.");
            return;
        }

        for (IObject obj : annotations.get(imageId)) {
            FileAnnotation ann = (FileAnnotation) obj;
            if (NSMEASUREMENT.value.equals(ann.getNs().getValue())) {
                TablePrx table = resources.openTable(ann.getFile());
                try {
                    System.out.println(String.format("Found measurement file %s: rows=%s", table
                            .getOriginalFile().getId().getValue(),
                            table.getNumberOfRows()));
                } finally {
                    table.close();
                }
            }
        }
    }
}
