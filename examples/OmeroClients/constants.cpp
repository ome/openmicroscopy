#include <iostream>
#include <omero/Constants.h>
int main() {
    std::cout << "By default, no method call can pass more than " << omero::constants::MESSAGESIZEMAX << "kb" << std::endl;
    std::cout << "By default, client.createSession() will wait " << CONNECTTIMEOUT/1000 << " seconds for a connection" << std::endl;
}
