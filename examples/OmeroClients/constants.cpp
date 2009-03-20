#include <iostream>
#include <omero/Constants.h>
using namespace omero::constants;
int main() {
    std::cout << "By default, no method call can pass more than ";
    std::cout << MESSAGESIZEMAX << "kb" << std::endl;

    std::cout << "By default, client.createSession() will wait ";
    std::cout << (CONNECTTIMEOUT / 1000) << " seconds for a connection" << std::endl;
}
