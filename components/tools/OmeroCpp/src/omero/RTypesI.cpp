/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <typeinfo>

#include <algorithm>
#include <omero/RTypesI.h>
#include <omero/ClientErrors.h>
#include <omero/ObjectFactoryRegistrar.h>

::Ice::LocalObject* IceInternal::upCast(::omero::rtypes::ObjectFactory* p) { return p; }

namespace omero {

    namespace rtypes {

        // Omitting rtype() for the moment since it seems of less
        // value in C++

        // Static factory methods (primitives)
        // =========================================================================

        const omero::RBoolPtr rbool(bool val) {
            static const omero::RBoolPtr rtrue = new RBoolI(true);
            static const omero::RBoolPtr rfalse = new RBoolI(false);
            if (val) {
                return rtrue;
            } else {
                return rfalse;
            }
        }

        const omero::RDoublePtr rdouble(Ice::Double val) {
            return new RDoubleI(val);
        }

        const omero::RFloatPtr rfloat(Ice::Float val) {
            return new RFloatI(val);
        }

        const omero::RIntPtr rint(Ice::Int val) {
            static const omero::RIntPtr rint0 = new RIntI(0);
            if (val == 0) {
                return rint0;
            }
            return new RIntI(val);
        }

        const omero::RLongPtr rlong(Ice::Long val) {
            static const omero::RLongPtr rlong0 = new RLongI(0);
            if (val == 0) {
                return rlong0;
            }
            return new RLongI(val);
        }

        const omero::RTimePtr rtime(Ice::Long val){
            return new RTimeI(val);
        }

        // Static factory methods (objects)
        // =========================================================================

        const omero::RInternalPtr rinternal(const omero::InternalPtr& val) {
            static const omero::RInternalPtr rnullinternal = new RInternalI(omero::InternalPtr());
            if (! val) {
                return rnullinternal;
            }
            return new RInternalI(val);
        }

        const omero::RObjectPtr robject(const omero::model::IObjectPtr& val) {
            static const omero::RObjectPtr rnullobject = new RObjectI(omero::model::IObjectPtr());
            if (! val) {
                return rnullobject;
            }
            return new RObjectI(val);
        }

        const omero::RClassPtr rclass(const std::string& val) {
            static const omero::RClassPtr remptyclass = new RClassI("");
            if (val.empty()) {
                return remptyclass;
            }
            return new RClassI(val);
        }

        const omero::RStringPtr rstring(const std::string& val) {
            static const omero::RStringPtr remptystr = new RStringI("");
            if (val.empty()) {
                return remptystr;
            }
            return new RStringI(val);
        }

        // Static factory methods (collections)
        // =========================================================================

        const omero::RArrayPtr rarray() {
            return new RArrayI();
        }

        const omero::RListPtr rlist() {
            return new RListI();
        }

        const omero::RSetPtr rset() {
            return new RSetI();
        }

        const omero::RMapPtr rmap() {
            return new RMapI();
        }


        // Implementations (primitives)
        // =========================================================================

        template<typename T, typename P>
        Ice::Int compareRTypes(const T& lhs, const omero::RTypePtr& rhs) {

            T rhsCasted(T::dynamicCast(rhs));
            if (!rhsCasted) {
                throw std::bad_cast();
            }

            P val = lhs->getValue();
            P valR = rhsCasted->getValue();

            if (val == valR) {
                return 0;
            } else {
                return val > valR? 1 : -1;
            }
        }

        Ice::Int compareRTypes(const RTypeSeq& lhs, const omero::RTypeSeq& rhs) {

            RTypeSeq val(lhs);
            RTypeSeq valR(rhs);

            bool reversed(false);
            if (valR.size() < val.size()) {
                reversed = true;
                RTypeSeq tmp(val);
                valR = val;
                val = tmp;
            }

            std::pair<RTypeSeq::iterator, RTypeSeq::iterator> idx = std::mismatch(
                    val.begin(), val.end(), valR.begin());

            if (idx.first == val.end() &&
                    idx.second == valR.end()) {
                return 0;
            }

            bool lessThan = std::lexicographical_compare(
                    val.begin(), val.end(),
                    valR.begin(), valR.end());
            if (reversed) {
                return lessThan ? 1 : -1;
            } else {
                return lessThan ? -1 : 1;
            }
        }

        // RBOOL

        RBoolI::RBoolI(bool val) : omero::RBool() {
            this->val = val;
        }

        RBoolI::~RBoolI() { }

        bool RBoolI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RBoolI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
        return compareRTypes<RBoolPtr, bool>(this, rhs);
        }

        bool operator==(const RBoolPtr& lhs, const RBoolPtr& rhs) {
            return compareRTypes<RBoolPtr, bool>(lhs, rhs) == 0;
        }

        bool operator<(const RBoolPtr& lhs, const RBoolPtr& rhs) {
            return compareRTypes<RBoolPtr, bool>(lhs, rhs) < 0;
        }

        bool operator>(const RBoolPtr& lhs, const RBoolPtr& rhs) {
            return compareRTypes<RBoolPtr, bool>(lhs, rhs) > 0;
        }

        // RDOUBLE

        RDoubleI::RDoubleI(Ice::Double val) {
        this->val = val;
        }

        RDoubleI::~RDoubleI() {}

        Ice::Double RDoubleI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RDoubleI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
        return compareRTypes<RDoublePtr, Ice::Double>(this, rhs);
        }

        bool operator==(const RDoublePtr& lhs, const RDoublePtr& rhs) {
            return compareRTypes<RDoublePtr, Ice::Double>(lhs, rhs) == 0;
        }

        bool operator<(const RDoublePtr& lhs, const RDoublePtr& rhs) {
            return compareRTypes<RDoublePtr, Ice::Double>(lhs, rhs) < 0;
        }

        bool operator>(const RDoublePtr& lhs, const RDoublePtr& rhs) {
            return compareRTypes<RDoublePtr, Ice::Double>(lhs, rhs) > 0;
        }

        // RFLOAT

        RFloatI::~RFloatI() {}

        RFloatI::RFloatI(Ice::Float val) {
            this->val = val;
        }

        Ice::Float RFloatI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RFloatI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RFloatPtr, Ice::Float>(this, rhs);
        }

        bool operator==(const RFloatPtr& lhs, const RFloatPtr& rhs) {
            return compareRTypes<RFloatPtr, Ice::Float>(lhs, rhs) == 0;
        }

        bool operator<(const RFloatPtr& lhs, const RFloatPtr& rhs) {
            return compareRTypes<RFloatPtr, Ice::Float>(lhs, rhs) < 0;
        }

        bool operator>(const RFloatPtr& lhs, const RFloatPtr& rhs) {
            return compareRTypes<RFloatPtr, Ice::Float>(lhs, rhs) > 0;
        }

        // RINT

        RIntI::RIntI(Ice::Int val) {
            this->val = val;
        }

        RIntI::~RIntI() {}

        Ice::Int RIntI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RIntI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RIntPtr, Ice::Int>(this, rhs);
        }

        bool operator==(const RIntPtr& lhs, const RIntPtr& rhs) {
            return compareRTypes<RIntPtr, Ice::Int>(lhs, rhs) == 0;
        }

        bool operator<(const RIntPtr& lhs, const RIntPtr& rhs) {
            return compareRTypes<RIntPtr, Ice::Int>(lhs, rhs) < 0;
        }

        bool operator>(const RIntPtr& lhs, const RIntPtr& rhs) {
            return compareRTypes<RIntPtr, Ice::Int>(lhs, rhs) > 0;
        }

        // RLONG

        RLongI::RLongI(Ice::Long val) {
            this->val = val;
        }

        RLongI::~RLongI() {}

        Ice::Long RLongI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RLongI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RLongPtr, Ice::Long>(this, rhs);
        }

        bool operator==(const RLongPtr& lhs, const RLongPtr& rhs) {
            return compareRTypes<RLongPtr, Ice::Long>(lhs, rhs) == 0;
        }

        bool operator<(const RLongPtr& lhs, const RLongPtr& rhs) {
            return compareRTypes<RLongPtr, Ice::Long>(lhs, rhs) < 0;
        }

        bool operator>(const RLongPtr& lhs, const RLongPtr& rhs) {
            return compareRTypes<RLongPtr, Ice::Long>(lhs, rhs) > 0;
        }

        // RTIME

        RTimeI::RTimeI(Ice::Long val) {
            this->val = val;
        }

        RTimeI::~RTimeI() {}

        Ice::Long RTimeI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RTimeI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RTimePtr, Ice::Long>(this, rhs);
        }

        bool operator==(const RTimePtr& lhs, const RTimePtr& rhs) {
            return compareRTypes<RTimePtr, Ice::Long>(lhs, rhs) == 0;
        }

        bool operator<(const RTimePtr& lhs, const RTimePtr& rhs) {
            return compareRTypes<RTimePtr, Ice::Long>(lhs, rhs) < 0;
        }

        bool operator>(const RTimePtr& lhs, const RTimePtr& rhs) {
            return compareRTypes<RTimePtr, Ice::Long>(lhs, rhs) > 0;
        }

        // Implementations (objects)
        // =========================================================================

        // RINTERNAL

        RInternalI::RInternalI(const omero::InternalPtr& val) {
            this->val = val;
        }

        RInternalI::~RInternalI() {}

        omero::InternalPtr RInternalI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RInternalI::compare(const omero::RTypePtr& /* rhs */, const Ice::Current& /* current */) {
            throw omero::ClientError(__FILE__,__LINE__,"Not implemented");
        }

        // RObject

        RObjectI::RObjectI(const omero::model::IObjectPtr& val) {
            this->val = val;
        }

        RObjectI::~RObjectI() {}

        omero::model::IObjectPtr RObjectI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RObjectI::compare(const omero::RTypePtr& /* rhs */, const Ice::Current& /* current */) {
            throw omero::ClientError(__FILE__,__LINE__,"Not implemented");
        }

        // RSTRING

        RStringI::RStringI(const std::string& val) {
            this->val = val;
        }

        RStringI::~RStringI() {}

        std::string RStringI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RStringI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RStringPtr, std::string>(this, rhs);
        }

        bool operator==(const RStringPtr& lhs, const RStringPtr& rhs) {
            return compareRTypes<RStringPtr, std::string>(lhs, rhs) == 0;
        }

        bool operator<(const RStringPtr& lhs, const RStringPtr& rhs) {
            return compareRTypes<RStringPtr, std::string>(lhs, rhs) < 0;
        }

        bool operator>(const RStringPtr& lhs, const RStringPtr& rhs) {
            return compareRTypes<RStringPtr, std::string>(lhs, rhs) > 0;
        }

        // RCLASS

        RClassI::RClassI(const std::string& val) {
            this->val = val;
        }

        RClassI::~RClassI() {}

        std::string RClassI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RClassI::compare(const omero::RTypePtr& /* rhs */, const Ice::Current& /* current */) {
            throw omero::ClientError(__FILE__,__LINE__,"Not implemented");
        }


        // Implementations (collections)
        // =========================================================================

        bool operator==(const RTypeSeq& lhs, const RTypeSeq& rhs) {
            return compareRTypes(lhs, rhs) == 0;
        }

        bool operator<(const RTypeSeq& lhs, const RTypeSeq& rhs) {
            return compareRTypes(lhs, rhs) < 0;
        }

        bool operator>(const RTypeSeq& lhs, const RTypeSeq& rhs) {
            return compareRTypes(lhs, rhs) > 0;
        }

        // RARRAY

        RArrayI::RArrayI(const omero::RTypePtr& value) {
            this->val = omero::RTypeSeq();
            this->val.push_back(value);
        }

        RArrayI::RArrayI(const omero::RTypeSeq& values) {
            this->val = values;
        }

        RArrayI::~RArrayI() {}

        RTypeSeq RArrayI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RArrayI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RArrayPtr, RTypeSeq>(this, rhs);
        }

        bool operator==(const RArrayPtr& lhs, const RArrayPtr& rhs) {
            return compareRTypes<RArrayPtr, RTypeSeq>(lhs, rhs) == 0;
        }

        bool operator<(const RArrayPtr& lhs, const RArrayPtr& rhs) {
            return compareRTypes<RArrayPtr, RTypeSeq>(lhs, rhs) < 0;
        }

        bool operator>(const RArrayPtr& lhs, const RArrayPtr& rhs) {
            return compareRTypes<RArrayPtr, RTypeSeq>(lhs, rhs) > 0;
        }


        // Collection methods
        omero::RTypePtr RArrayI::get(Ice::Int idx, const Ice::Current& /* current */) {
            return this->val[idx];
        }

        Ice::Int RArrayI::size(const Ice::Current& /* current */) {
            return this->val.size();
        }

        void RArrayI::add(const omero::RTypePtr& val, const Ice::Current& /* current */) {
            this->val.push_back(val);
        }

        void RArrayI::addAll(const omero::RTypeSeq& values, const Ice::Current& /* current */) {
            omero::RTypeSeq::const_iterator itr;
            for (itr = values.begin(); itr != values.end(); itr++) {
                this->val.push_back(*itr);
            }
        }

        // RLIST

        RListI::RListI(const omero::RTypePtr& value)  {
            this->val = omero::RTypeSeq();
            this->val.push_back(value);
        }

        RListI::RListI(const omero::RTypeSeq& values) {
            this->val = values;
        }

        RListI::~RListI() {}

        RTypeSeq RListI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RListI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
        return compareRTypes<RListPtr, RTypeSeq>(this, rhs);
        }

        bool operator==(const RListPtr& lhs, const RListPtr& rhs) {
            return compareRTypes<RListPtr, RTypeSeq>(lhs, rhs) == 0;
        }

        bool operator<(const RListPtr& lhs, const RListPtr& rhs) {
            return compareRTypes<RListPtr, RTypeSeq>(lhs, rhs) < 0;
        }

        bool operator>(const RListPtr& lhs, const RListPtr& rhs) {
            return compareRTypes<RListPtr, RTypeSeq>(lhs, rhs) > 0;
        }

        // Collection methods
        omero::RTypePtr RListI::get(Ice::Int idx, const Ice::Current& /* current */) {
            return this->val[idx];
        }

        Ice::Int RListI::size(const Ice::Current& /* current */) {
            return this->val.size();
        }

        void RListI::add(const omero::RTypePtr& val, const Ice::Current& /* current */) {
            this->val.push_back(val);
        }

        void RListI::addAll(const omero::RTypeSeq& values, const Ice::Current& /* current */) {
            omero::RTypeSeq::const_iterator itr;
            for (itr = values.begin(); itr != values.end(); itr++) {
                this->val.push_back(*itr);
            }
        }

        // RSET

        RSetI::RSetI(const omero::RTypePtr& value) {
            this->val = omero::RTypeSeq();
            this->val.push_back(value);
        }

        RSetI::RSetI(const omero::RTypeSeq& values) {
            this->val = values;
        }

        RSetI::~RSetI() {}

        RTypeSeq RSetI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RSetI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RSetPtr, RTypeSeq>(this, rhs);
        }

        bool operator==(const RSetPtr& lhs, const RSetPtr& rhs) {
            return compareRTypes<RSetPtr, RTypeSeq>(lhs, rhs) == 0;
        }

        bool operator<(const RSetPtr& lhs, const RSetPtr& rhs) {
            return compareRTypes<RSetPtr, RTypeSeq>(lhs, rhs) < 0;
        }

        bool operator>(const RSetPtr& lhs, const RSetPtr& rhs) {
            return compareRTypes<RSetPtr, RTypeSeq>(lhs, rhs) > 0;
        }

        // Collection methods
        omero::RTypePtr RSetI::get(Ice::Int idx, const Ice::Current& /* current */) {
            return this->val[idx];
        }

        Ice::Int RSetI::size(const Ice::Current& /* current */) {
            return this->val.size();
        }

        void RSetI::add(const omero::RTypePtr& val, const Ice::Current& /* current */) {
            this->val.push_back(val);
        }

        void RSetI::addAll(const omero::RTypeSeq& values, const Ice::Current& /* current */) {
            omero::RTypeSeq::const_iterator itr;
            for (itr = values.begin(); itr != values.end(); itr++) {
                this->val.push_back(*itr);
            }
        }

        // RMAP

        RMapI::RMapI(const std::string& key, const omero::RTypePtr& value) {
            this->val = omero::RTypeDict();
            this->val[key] = value;
        }

        RMapI::RMapI(const omero::RTypeDict& values) {
            this->val = values;
        }

        RMapI::~RMapI() {}

        RTypeDict RMapI::getValue(const Ice::Current& /* current */) { return this->val; }

        Ice::Int RMapI::compare(const omero::RTypePtr& rhs, const Ice::Current& /* current */) {
            return compareRTypes<RMapPtr, RTypeDict>(this, rhs);
        }

        // Collection methods
        omero::RTypePtr RMapI::get(const std::string& key, const Ice::Current& /* current */) {
            return this->val[key];
        }

        void RMapI::put(const std::string& key, const omero::RTypePtr& value, const Ice::Current& /* current */) {
            this->val[key] = value;
        }

        Ice::Int RMapI::size(const Ice::Current& /* current */) {
            return this->val.size();
        }

        // Helpers
        // ========================================================================

        // Conversion classes are for omero.model <--> ome.model only (no C++)

        void ObjectFactory::register_(const Ice::CommunicatorPtr& ic) {
            ic->addObjectFactory(this, this->id);
        }

        ObjectFactory::~ObjectFactory() {}

        class RBoolIFactory : virtual public ObjectFactory {
        protected:
            ~RBoolIFactory() {}
        public:
            RBoolIFactory() {
                this->id = RBoolI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RBoolI(false);
            }
        };

        class RDoubleIFactory : virtual public ObjectFactory {
        protected:
            ~RDoubleIFactory() {}
        public:
            RDoubleIFactory() {
                this->id = RDoubleI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RDoubleI(0.0);
            }
        };

        class RFloatIFactory : virtual public ObjectFactory {
        protected:
            ~RFloatIFactory() {}
        public:
            RFloatIFactory() {
                this->id = RFloatI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RFloatI(0.0);
            }
        };

        class RLongIFactory : virtual public ObjectFactory {
        protected:
            ~RLongIFactory() {}
        public:
            RLongIFactory() {
                this->id = RLongI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RLongI(0);
            }
        };

        class RIntIFactory : virtual public ObjectFactory {
        protected:
            ~RIntIFactory() {}
        public:
            RIntIFactory() {
                this->id = RIntI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RIntI(0);
            }
        };

        class RTimeIFactory : virtual public ObjectFactory {
        protected:
            ~RTimeIFactory() {}
        public:
            RTimeIFactory() {
                this->id = RTimeI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RTimeI(0);
            }
        };

        class RStringIFactory : virtual public ObjectFactory {
        protected:
            ~RStringIFactory() {}
        public:
            RStringIFactory() {
                this->id = RStringI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RStringI("");
            }
        };

        class RClassIFactory : virtual public ObjectFactory {
        protected:
            ~RClassIFactory() {}
        public:
            RClassIFactory() {
                this->id = RClassI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RClassI("");
            }
        };

        class RInternalIFactory : virtual public ObjectFactory {
        protected:
            ~RInternalIFactory() {}
        public:
            RInternalIFactory() {
                this->id = RInternalI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RInternalI(omero::InternalPtr());
            }
        };

        class RObjectIFactory : virtual public ObjectFactory {
        protected:
            ~RObjectIFactory() {}
        public:
            RObjectIFactory() {
                this->id = RObjectI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RObjectI(omero::model::IObjectPtr());
            }
        };

        class RArrayIFactory : virtual public ObjectFactory {
        protected:
            ~RArrayIFactory() {}
        public:
            RArrayIFactory() {
                this->id = RArrayI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RArrayI();
            }
        };

        class RListIFactory : virtual public ObjectFactory {
        protected:
            ~RListIFactory() {}
        public:
            RListIFactory() {
                this->id = RListI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RListI();
            }
        };

        class RSetIFactory : virtual public ObjectFactory {
        protected:
            ~RSetIFactory() {}
        public:
            RSetIFactory() {
                this->id = RSetI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RSetI();
            }
        };

        class RMapIFactory : virtual public ObjectFactory {
        protected:
            ~RMapIFactory() {}
        public:
            RMapIFactory() {
                this->id = RMapI::ice_staticId();
            }
            Ice::ObjectPtr create(const std::string& /* id */) {
                return new RMapI();
            }
        };


        // Shared state (flyweight)
        // =========================================================================

        // Defined in header

        // Object factories
        // =========================================================================

        void registerObjectFactory(const Ice::CommunicatorPtr ic) {
            omero::conditionalAdd(RBoolI::ice_staticId(), ic, new RBoolIFactory());
            omero::conditionalAdd(RDoubleI::ice_staticId(), ic, new RDoubleIFactory());
            omero::conditionalAdd(RFloatI::ice_staticId(), ic, new RFloatIFactory());
            omero::conditionalAdd(RIntI::ice_staticId(), ic, new RIntIFactory());
            omero::conditionalAdd(RLongI::ice_staticId(), ic, new RLongIFactory());
            omero::conditionalAdd(RTimeI::ice_staticId(), ic, new RTimeIFactory());
            omero::conditionalAdd(RClassI::ice_staticId(), ic, new RClassIFactory());
            omero::conditionalAdd(RStringI::ice_staticId(), ic, new RStringIFactory());
            omero::conditionalAdd(RInternalI::ice_staticId(), ic, new RInternalIFactory());
            omero::conditionalAdd(RObjectI::ice_staticId(), ic, new RObjectIFactory());
            omero::conditionalAdd(RArrayI::ice_staticId(), ic, new RArrayIFactory());
            omero::conditionalAdd(RListI::ice_staticId(), ic, new RListIFactory());
            omero::conditionalAdd(RSetI::ice_staticId(), ic, new RSetIFactory());
            omero::conditionalAdd(RMapI::ice_staticId(), ic, new RMapIFactory());
        }
    }

}
