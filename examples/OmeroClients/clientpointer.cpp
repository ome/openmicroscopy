#include <omero/client.h>
int main(int argc, char* argv[])
{
    // Duplicating the argument list. ticket:1246
    Ice::StringSeq args1 = Ice::argsToStringSeq(argc, argv);
    Ice::StringSeq args2(args1);
    Ice::InitializationData id1, id2;
    id1.properties = Ice::createProperties(args1);
    id2.properties = Ice::createProperties(args2);

    // Either
    omero::client client(id1);
    try {
        // Do something like
        // client.createSession();
    } catch (...) {
        client.closeSession();
    }

    //
    // Or
    //
    {
        omero::client_ptr client = new omero::client(id2);

        // Do something like
        // client->createSession();
    }
    // Client was destroyed via RAII

}

