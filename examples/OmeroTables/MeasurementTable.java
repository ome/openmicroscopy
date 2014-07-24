import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.LongColumn;
import omero.grid.SharedResourcesPrx;
import omero.grid.TablePrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;

/**
 * Example of working with the {@link omero.grid.TablePrx OMERO.tables API}. A
 * Table is created, attached to an {@link Image} and then queried.
 */
public class MeasurementTable {

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
            Long imageId = null;
            if (args.length > 0) {
                imageId = Long.valueOf(args[0]);
            }

            MeasurementTable table = new MeasurementTable(client, imageId);
            table.query();

        } finally {
            client.closeSession();
            System.exit(0);
        }
    }

    public final Long imageId;
    TablePrx table;
    OriginalFile file;
    ServiceFactoryPrx factory;
    SharedResourcesPrx resources;

    /**
     * Creates a new Table service and saves some sample data in it.
     */
    MeasurementTable(omero.client client, Long imageId) throws ServerError {
        factory = client.getSession();
        resources = factory.sharedResources();

        if (imageId != null) {
            System.out.println("Using image " + imageId);
        } else {
            imageId = makeImage(client);
            System.out.println("Created image " + imageId);
        }
        this.imageId = imageId;

        table = resources.newTable(1, "/examples/test.h5");
        try {

            attachAnnotation(factory, imageId, table);

            // Nulls here mean that column has no data
            // We first pass a truncated column to prevent unnecessary network
            // communication
            LongColumn lc = new LongColumn("a1", "desc", null);
            Column[] cols = new Column[] { lc };

            table.initialize(cols);

            // To send data, we need to initialize the array and set values.
            lc.values = new long[10];
            for (int i = 0; i < 10; i++) {
                lc.values[i] = i;
            }

            // addData can be called as many times as necessary.
            table.addData(cols);

        } finally {
            table.close();
        }
    }

    /**
     * Creates a simple Image with no attached binary data for the example.
     */
    Long makeImage(omero.client client) throws ServerError {
        Image image = new ImageI();
        image.setName(omero.rtypes.rstring("MeasurementTable Example"));
        image = (Image) factory.getUpdateService().saveAndReturnObject(image);
        return image.getId().getValue();
    }

    /**
     * Creates a {@link FileAnnotation} and attaches it to the given image id.
     */
    void attachAnnotation(ServiceFactoryPrx factory, long imageId,
            TablePrx table) throws ServerError {

        file = table.getOriginalFile();
        FileAnnotation annotation = new FileAnnotationI();
        annotation.setNs(omero.rtypes
                .rstring(omero.constants.namespaces.NSMEASUREMENT.value));
        annotation.setFile(file);
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(imageId, false), annotation);
        link = (ImageAnnotationLink) factory.getUpdateService()
                .saveAndReturnObject(link);

    }

    /**
     * A few example queries are made on the table.
     */
    void query() throws ServerError {

        table = resources.openTable(file);

        try {

            System.out.println("Rows found:"
                    + table.getWhereList("(a1==5)", null, 0, -1, 0).length);

            Data data = table.readCoordinates(new long[] { 0, 1, 2, 3, 4, 5, 6,
                    7, 8, 9 });
            LongColumn lc = (LongColumn) data.columns[0];
            for (int i = 0; i < lc.values.length; i++) {
                System.out.println(i + ":" + lc.values[i]);
            }

        } finally {

            table.close();

        }
    }
}
