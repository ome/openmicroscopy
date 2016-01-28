import omero.model.Image;
import omero.model.ImageI;
import omero.model.Dataset;
import omero.model.DatasetI;

public class constructors {

    public static void main(String args[]) {

        Image image = new ImageI();
        Dataset dataset = new DatasetI(1L, false);
        image.linkDataset(dataset);

    }

}
