import static omero.rtypes.*;

public class primitives {
    public static void main(String[] args) {
        omero.RString a = rstring("value");
        omero.RBool b = rbool(true);
        omero.RLong l = rlong(1l);
        omero.RInt i = rint(1);
    }
}
