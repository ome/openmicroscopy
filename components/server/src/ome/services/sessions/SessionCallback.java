package ome.services.sessions;

public interface SessionCallback { // like new Element(key,object);
    
    String getName();

    Object getObject();

    void join(String session);// <--or in blitz use createSession() with

    // session name in context.

    void close();
}