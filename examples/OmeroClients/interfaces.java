import omero.model.IObject;
import omero.model.EventI;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMapI;

public class interfaces {

    public static void main(String args[]) {

        assert ! new EventI().isMutable();
        assert new ExperimenterI().isMutable();
        assert new ExperimenterI().isGlobal();
        assert new ExperimenterI().isAnnotated();
        assert new GroupExperimenterMapI().isLink();

        IObject someObject = new ExperimenterI();
        // Some method call and you no longer know what someObject is
        if ( ! someObject.isMutable()) {
            // No need to update
        } else if (someObject.isAnnotated()) {
           // deleteAnnotations(someObject);
        }
    }

}
