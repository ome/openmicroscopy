import omero.model.Image;
import omero.model.ImageI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;

public class iterators {

    public static void main(String args[]) {

        Image image = new ImageI();
        Dataset dataset = new DatasetI();
        /*DatasetImageLink link = */dataset.linkImage(image);

        //Iterator<DatasetI> it = image.iterateDatasetLinks();
        //while (it.hasNext()) {
        //    it.next().getName();
        //}

    }

}
