public class configuration {

  public static void main(String[] args) throws Exception {

    // All configuration in file pointed to by
    // --Ice.Config=file.config
    // No username, password entered
    omero.client client1 = new omero.client(args);
    try {
        client1.createSession();
    } catch (Ice.ConnectionRefusedException cre) {
        // Bad address or port?
    } finally {
        client1.closeSession();
    }

    // Most basic configuration.
    // Uses default port 4064
    // createSession needs username and password
    omero.client client2 = new omero.client("localhost");
    try {
        client2.createSession("root", "ome");
    } catch (Ice.ConnectionRefusedException cre) {
        // Bad address or port?
    } finally {
        client2.closeSession();
    }

    // Configuration with port information
    omero.client client3 = new omero.client("localhost", 24064);
    try {
        client3.createSession("root", "ome");
    } catch (Ice.ConnectionRefusedException cre) {
        // Bad address or port?
    } finally {
        client3.closeSession();
    }

    // Advanced configuration can also be done
    // via an InitializationData instance.
    Ice.InitializationData data = new Ice.InitializationData();
    data.properties = Ice.Util.createProperties();
    data.properties.setProperty("omero.host", "localhost");
    omero.client client4 = new omero.client(data);
    try {
        client4.createSession("root", "ome");
    } catch (Ice.ConnectionRefusedException cre) {
        // Bad address or port?
    } finally {
        client4.closeSession();
    }

    // Or alternatively via a java.util.Map instance
    java.util.Map<String, String> map = new java.util.HashMap<String, String>();
    map.put("omero.host", "localhost");
    map.put("omero.user", "root");
    map.put("omero.pass", "ome");
    omero.client client5 = new omero.client(map);
    // Again, no username or password needed
    // since present in the map. But they *can*
    // be overridden.
    try {
        client5.createSession();
    } catch (Ice.ConnectionRefusedException cre) {
        // Bad address or port?
    } finally {
        client5.closeSession();
    }

  }

}
