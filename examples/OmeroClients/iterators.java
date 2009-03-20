import omero.model.ImageI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;

import java.util.*;

public class iterators {

    public static void main(String args[]) {

        ImageI image = new ImageI();
        Dataset dataset = new DatasetI();
        DatasetImageLink link = dataset.linkImage(image);

        Iterator<DatasetImageLinkI> it = image.iterateDatasetLinks();
        while (it.hasNext()) {
            it.next().getChild().getName();
        }

    }

}
