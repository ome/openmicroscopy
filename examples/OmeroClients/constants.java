

public class constants {
    public static void main(String[] args) {

        System.out.println(String.format(
            "By default, no method call can pass more than %s kb",
            omero.constants.MESSAGESIZEMAX.value));
        System.out.println(String.format(
            "By default, client.createSession() will wait %s seconds for a connection",
            omero.constants.CONNECTTIMEOUT.value/1000));

    }
}
