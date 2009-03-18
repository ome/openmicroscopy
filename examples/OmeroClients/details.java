import omero.model.Image;
import omero.model.ImageI;
import omero.model.Details;
import omero.model.Permissions;
import omero.model.ExperimenterGroupI;

public class details {

    public static void main(String args[]) {

        Image image = new ImageI();
        Details details = image.getDetails();
        // Always available
        Permissions p = details.getPermissions();
        assert p.isUserRead();
        // Available when returned from server
        // Possibly modifiable
        details.getOwner();
        details.setGroup(new ExperimenterGroupI(1L, false));
        // Available when returned from server
        // Not modifiable
        details.getCreationEvent();
        details.getUpdateEvent();
    }

}
