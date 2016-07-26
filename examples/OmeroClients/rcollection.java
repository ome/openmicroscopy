

public class rcollection {
    public static void main(String[] args) {
        // Sets and Lists may be interpreted differently on the server
        omero.RList list = omero.rtypes.rlist(omero.rtypes.rstring("a"), omero.rtypes.rstring("b"));
        omero.RSet set = omero.rtypes.rset(rint(1), rint(2));
    }
}
