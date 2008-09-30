import omero.model.Image;
import omero.model.ImageI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;

public class sizeof {

    public static void main(String args[]) {

        Image image = new ImageI();

        assert image.sizeOfDatasetLinks() == 0;

        Dataset dataset = new DatasetI();
        /*FIXME DatasetImageLink link = */image.linkDataset(dataset);

        assert image.sizeOfDatasetLinks() == 1;

        // FIXME
        ((ImageI)image).unloadDatasetLinks();

        assert image.sizeOfDatasetLinks() < 0;

        try {
            image.linkDataset( new DatasetI() );
        } catch (Exception e) {
            // Can't access an unloaded collection
        }

    }

}
