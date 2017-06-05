public class primitives {
    public static void main(String[] args) {
        omero.RString a = omero.rtypes.rstring("value");
        omero.RBool b = omero.rtypes.rbool(true);
        omero.RLong l = omero.rtypes.rlong(1l);
        omero.RInt i = omero.rtypes.rint(1);
    }
}
