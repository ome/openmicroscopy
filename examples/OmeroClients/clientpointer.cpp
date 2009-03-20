#include <omero/client.h>
int main(int argc, char* argv[])
{
    // Either
    omero::client client(argc, argv);
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
        omero::client_ptr client = new omero::client(argc, argv);

        // Do something like
        // client->createSession();
    }
    // Client was destroyed via RAII

}

