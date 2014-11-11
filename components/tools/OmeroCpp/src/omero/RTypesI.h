/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPESI_H
#define OMERO_RTYPESI_H

#include <omero/IceNoWarnPush.h>
#include <omero/RTypes.h>
#include <omero/model/RTypes.h>
#include <omero/Scripts.h>
#include <Ice/Ice.h>
#include <omero/IceNoWarnPop.h>

#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <string>
#include <map>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

/**
 * Header which is responsible for creating rtypes from static
 * factory methods. Where possible, factory methods return cached values
 * (the fly-weight pattern) such that <code>rbool(true) == rbool(true)</code>
 * might hold true.

 * This module is meant to be kept in sync with the abstract Java class
 * omero.rtypes as well as the omero/rtypes.py module.
 */

namespace omero {
    namespace rtypes {
        class ObjectFactory;
    }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::LocalObject* upCast(::omero::rtypes::ObjectFactory*);
}

namespace omero {

    namespace rtypes {

        // Static factory methods (primitives)
        // =========================================================================

        OMERO_CLIENT const omero::RBoolPtr rbool(bool val);
        OMERO_CLIENT const omero::RDoublePtr rdouble(Ice::Double val);
        OMERO_CLIENT const omero::RFloatPtr rfloat(Ice::Float val);
        OMERO_CLIENT const omero::RIntPtr rint(Ice::Int val);
        OMERO_CLIENT const omero::RLongPtr rlong(Ice::Long val);
        OMERO_CLIENT const omero::RTimePtr rtime(Ice::Long val);

        // Static factory methods (objects)
        // =========================================================================

        OMERO_CLIENT const omero::RInternalPtr rinternal(const omero::InternalPtr& val);
        OMERO_CLIENT const omero::RObjectPtr robject(const omero::model::IObjectPtr& val);
        OMERO_CLIENT const omero::RClassPtr rclass(const std::string& val);
        OMERO_CLIENT const omero::RStringPtr rstring(const std::string& val);

        // Static factory methods (collections)
        // =========================================================================

        OMERO_CLIENT const omero::RArrayPtr rarray();
        OMERO_CLIENT const omero::RListPtr rlist();
        OMERO_CLIENT const omero::RSetPtr rset();
        OMERO_CLIENT const omero::RMapPtr rmap();

        // Implementations (primitives)
        // =========================================================================

        class OMERO_CLIENT RBoolI : virtual public omero::RBool {
        protected:
            virtual ~RBoolI(); // protected as outlined in Ice Docs
        public:
            RBoolI(bool value);
            virtual bool getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RBoolPtr& lhs,
                       const RBoolPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RBoolPtr& lhs,
                       const RBoolPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RBoolPtr& lhs,
                        const RBoolPtr& rhs);

        class OMERO_CLIENT RDoubleI : virtual public omero::RDouble {
        protected:
            virtual ~RDoubleI(); // as above
        public:
            RDoubleI(Ice::Double value);
            virtual Ice::Double getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RDoublePtr& lhs,
                       const RDoublePtr& rhs);
        OMERO_CLIENT
        bool operator>(const RDoublePtr& lhs,
                       const RDoublePtr& rhs);
        OMERO_CLIENT
        bool operator==(const RDoublePtr& lhs,
                        const RDoublePtr& rhs);

        class OMERO_CLIENT RFloatI : virtual public omero::RFloat {
        protected:
            virtual ~RFloatI(); // as above
        public:
            RFloatI(Ice::Float value);
            virtual Ice::Float getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RFloatPtr& lhs,
                       const RFloatPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RFloatPtr& lhs,
                       const RFloatPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RFloatPtr& lhs,
                        const RFloatPtr& rhs);

        class OMERO_CLIENT RIntI : virtual public omero::RInt {
        protected:
            virtual ~RIntI(); // as above
        public:
            RIntI(Ice::Int value);
            virtual Ice::Int getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RIntPtr& lhs,
                       const RIntPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RIntPtr& lhs,
                       const RIntPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RIntPtr& lhs,
                        const RIntPtr& rhs);

        class OMERO_CLIENT RLongI : virtual public omero::RLong {
        protected:
            virtual ~RLongI(); // as above
        public:
            RLongI(Ice::Long value);
            virtual Ice::Long getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RLongPtr& lhs,
                       const RLongPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RLongPtr& lhs,
                       const RLongPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RLongPtr& lhs,
                        const RLongPtr& rhs);

        class OMERO_CLIENT RTimeI : virtual public omero::RTime {
        protected:
            virtual ~RTimeI(); // as above
        public:
            RTimeI(Ice::Long value);
            virtual Ice::Long getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RTimePtr& lhs,
                       const RTimePtr& rhs);
        OMERO_CLIENT
        bool operator>(const RTimePtr& lhs,
                       const RTimePtr& rhs);
        OMERO_CLIENT
        bool operator==(const RTimePtr& lhs,
                        const RTimePtr& rhs);

        // Implementations (objects)
        // =========================================================================

        class OMERO_CLIENT RInternalI : virtual public omero::RInternal {
        protected:
            virtual ~RInternalI(); // as above
        public:
            RInternalI(const omero::InternalPtr& value);
            virtual omero::InternalPtr getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };

        class OMERO_CLIENT RObjectI : virtual public omero::RObject {
        protected:
            virtual ~RObjectI(); // as above
        public:
            RObjectI(const omero::model::IObjectPtr& value);
            virtual omero::model::IObjectPtr getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };

        class OMERO_CLIENT RStringI : virtual public omero::RString {
        protected:
            virtual ~RStringI(); // as above
        public:
            RStringI(const std::string& value);
            virtual std::string getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RStringPtr& lhs,
                       const RStringPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RStringPtr& lhs,
                       const RStringPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RStringPtr& lhs,
                        const RStringPtr& rhs);

        class OMERO_CLIENT RClassI : virtual public omero::RClass {
        protected:
            virtual ~RClassI(); // as above
        public:
            RClassI(const std::string& value);
            virtual std::string getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
        };

        // Implementations (collections)
        // =========================================================================

        OMERO_CLIENT
        bool operator<(const RTypeSeq& lhs,
                       const RTypeSeq& rhs);
        OMERO_CLIENT
        bool operator>(const RTypeSeq& lhs,
                       const RTypeSeq& rhs);
        OMERO_CLIENT
        bool operator==(const RTypeSeq& lhs,
                        const RTypeSeq& rhs);

        /**
         * Guaranteed to never contain an empty list.
         */
        class OMERO_CLIENT RArrayI : virtual public omero::RArray {
        protected:
            virtual ~RArrayI(); // as above
        public:
            RArrayI(const omero::RTypePtr& value);
            RArrayI(const omero::RTypeSeq& values = omero::RTypeSeq());
            virtual omero::RTypeSeq getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
            // Collection methods
            virtual omero::RTypePtr get(Ice::Int idx, const Ice::Current& current = Ice::Current());
            virtual Ice::Int size(const Ice::Current& current = Ice::Current());
            virtual void add(const omero::RTypePtr& val, const Ice::Current& current = Ice::Current());
            virtual void addAll(const omero::RTypeSeq& values, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RArrayPtr& lhs,
                       const RArrayPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RArrayPtr& lhs,
                       const RArrayPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RArrayPtr& lhs,
                        const RArrayPtr& rhs);

        /**
         * Guaranteed to never contain an empty list.
         */
        class OMERO_CLIENT RListI : virtual public omero::RList {
        protected:
            virtual ~RListI(); // as above
        public:
            RListI(const omero::RTypePtr& value);
            RListI(const omero::RTypeSeq& values = omero::RTypeSeq());
            virtual omero::RTypeSeq getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
            // Collection methods
            virtual omero::RTypePtr get(Ice::Int idx, const Ice::Current& current = Ice::Current());
            virtual Ice::Int size(const Ice::Current& current = Ice::Current());
            virtual void add(const omero::RTypePtr& val, const Ice::Current& current = Ice::Current());
            virtual void addAll(const omero::RTypeSeq& values, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RListPtr& lhs,
                       const RListPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RListPtr& lhs,
                       const RListPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RListPtr& lhs,
                        const RListPtr& rhs);

        /**
         * Guaranteed to never contain an empty list.
         */
        class OMERO_CLIENT RSetI : virtual public omero::RSet {
        protected:
            virtual ~RSetI(); // as above
        public:
            RSetI(const omero::RTypePtr& value);
            RSetI(const omero::RTypeSeq& values = omero::RTypeSeq());
            virtual omero::RTypeSeq getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
            // Collection methods
            virtual omero::RTypePtr get(Ice::Int idx, const Ice::Current& current = Ice::Current());
            virtual Ice::Int size(const Ice::Current& current = Ice::Current());
            virtual void add(const omero::RTypePtr& val, const Ice::Current& current = Ice::Current());
            virtual void addAll(const omero::RTypeSeq& values, const Ice::Current& current = Ice::Current());
        };
        OMERO_CLIENT
        bool operator<(const RSetPtr& lhs,
                       const RSetPtr& rhs);
        OMERO_CLIENT
        bool operator>(const RSetPtr& lhs,
                       const RSetPtr& rhs);
        OMERO_CLIENT
        bool operator==(const RSetPtr& lhs,
                        const RSetPtr& rhs);

        class OMERO_CLIENT RMapI : virtual public omero::RMap {
        protected:
            virtual ~RMapI(); // as above
        public:
            RMapI(const std::string& key, const omero::RTypePtr& value);
            RMapI(const omero::RTypeDict& values = omero::RTypeDict());
            virtual omero::RTypeDict getValue(const Ice::Current& current = Ice::Current());
            virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
            // Collection methods
            virtual omero::RTypePtr get(const std::string& key, const Ice::Current& current = Ice::Current());
            virtual void put(const std::string& key, const omero::RTypePtr& value, const Ice::Current& current = Ice::Current());
            virtual Ice::Int size(const Ice::Current& current = Ice::Current());
        };

        // Helpers
        // ========================================================================

        // Conversion classes are for omero.model <--> ome.model only (no python)
        typedef IceInternal::Handle<ObjectFactory> ObjectFactoryPtr;

        class ObjectFactory : virtual public Ice::ObjectFactory {
        protected:
            std::string id;
            virtual ~ObjectFactory(); // as above
        public:
            virtual void register_(const Ice::CommunicatorPtr& ic);
            virtual Ice::ObjectPtr create(const std::string& id) = 0;
            virtual void destroy() { } // No-op
        };

        Ice::Int compareRTypes(const RTypeSeq& lhs, const omero::RTypeSeq& rhs);

        // Object factories
        // =========================================================================

        OMERO_CLIENT void registerObjectFactory(const Ice::CommunicatorPtr ic);

    }

}

#endif // OMERO_RTYPESI_H
