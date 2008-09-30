import omero.model.ImageI;
import omero.model.PixelsI;

public class lists {

    public static void main(String args[]) {

        ImageI image = new ImageI();
        PixelsI pixels1 = new PixelsI();
        PixelsI pixels2 = new PixelsI();

        image.addPixels(pixels1);
        image.addPixels(pixels2);
        image.getPixels(0);
        image.setPrimaryPixels(pixels2);

    }

}
