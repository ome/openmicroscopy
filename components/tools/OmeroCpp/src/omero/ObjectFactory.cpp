/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/ObjectFactoryRegistrar.h>
#include <omero/ObjectFactory.h>
#include <omero/clientF.h>
#include <omero/model/DetailsI.h>

namespace omero {

    class DetailsObjectFactory : virtual public Ice::ObjectFactory {
    protected:
    // This must be stored as a raw pointer to prevent circular ref with client
        const omero::client* client;
    public:
        DetailsObjectFactory(
            const omero::client* client = NULL)
                : Ice::ObjectFactory(), client(client) { }

        ~DetailsObjectFactory() {}

        Ice::ObjectPtr create(const std::string&) {
            return new omero::model::DetailsI(client);
        }

        void destroy() {}

    };

  void registerObjectFactory(const Ice::CommunicatorPtr& ic,
        const omero::client* client) {
    conditionalAdd("::omero::model::Details", ic, new DetailsObjectFactory(client));
  }

} // End omero
