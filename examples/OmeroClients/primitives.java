import omero.RString;

public class primitives {
    public static void main(String[] args) {

        omero.RString nulled = null;
        omero.RString empty = new omero.RString(); // Ice will send as ""
        omero.RString initialized = new omero.RString("value");

    }
}
