/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/RTypesI.h>
#include <omero/ClientErrors.h>


namespace omero {

    namespace rtypes {

	// Omitting rtype() for the moment since it seems of less
	// value in C++

	// Static factory methods (primitives)
	// =========================================================================

	const omero::RBoolPtr rbool(bool val) {
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
	    if (val == 0) {
		return rint0;
	    }
	    return new RIntI(val);
	}

	const omero::RLongPtr rlong(Ice::Long val) {
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
	    if (! val) {
		return rnullinternal;
	    }
	    return new RInternalI(val);
	}

	const omero::RObjectPtr robject(const omero::model::IObjectPtr& val) {
	    if (! val) {
		return rnullobject;
	    }
	    return new RObjectI(val);
	}

	const omero::RClassPtr rclass(const std::string& val) {
	    if (val.empty()) {
		return remptyclass;
	    }
	    return new RClassI(val);
	}

	const omero::RStringPtr rstring(const std::string& val) {
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

	// RBOOL

	RBoolI::RBoolI(bool val) : omero::RBool() {
	    this->val = val;
	}

	RBoolI::~RBoolI() { }

	bool RBoolI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RBoolI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RDOUBLE

	RDoubleI::RDoubleI(Ice::Double val) {
	this->val = val;
	}

	RDoubleI::~RDoubleI() {}

	Ice::Double RDoubleI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RDoubleI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RFLOAT

	RFloatI::~RFloatI() {}

	RFloatI::RFloatI(Ice::Float val) {
	    this->val = val;
	}

	Ice::Float RFloatI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RFloatI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RINT

	RIntI::RIntI(Ice::Int val) {
	    this->val = val;
	}

	RIntI::~RIntI() {}

	Ice::Int RIntI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RIntI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RLONG

	RLongI::RLongI(Ice::Long val) {
	    this->val = val;
	}

	RLongI::~RLongI() {}

	Ice::Long RLongI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RLongI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RTIME

	RTimeI::RTimeI(Ice::Long val) {
	    this->val = val;
	}

	RTimeI::~RTimeI() {}

	Ice::Long RTimeI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RTimeI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// Implementations (objects)
	// =========================================================================

	// RINTERNAL

	RInternalI::RInternalI(const omero::InternalPtr& val) {
	    this->val = val;
	}

	RInternalI::~RInternalI() {}

	omero::InternalPtr RInternalI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RInternalI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RObject

	RObjectI::RObjectI(const omero::model::IObjectPtr& val) {
	    this->val = val;
	}

	RObjectI::~RObjectI() {}

	omero::model::IObjectPtr RObjectI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RObjectI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RSTRING

	RStringI::RStringI(const std::string& val) {
	    this->val = val;
	}

	RStringI::~RStringI() {}

	std::string RStringI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RStringI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// RCLASS

	RClassI::RClassI(const std::string& val) {
	    this->val = val;
	}

	RClassI::~RClassI() {}

	std::string RClassI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RClassI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}


	// Implementations (collections)
	// =========================================================================

	// RARRAY

	RArrayI::RArrayI(const omero::RTypePtr& value) {
	    this->val = omero::RTypeSeq();
	    this->val.push_back(value);
	}

	RArrayI::RArrayI(const omero::RTypeSeq& values) {
	    this->val = values;
	}

	RArrayI::~RArrayI() {}

	RTypeSeq RArrayI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RArrayI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// Collection methods
	omero::RTypePtr RArrayI::get(Ice::Int idx, const Ice::Current& current) {
	    return this->val[idx];
	}

	Ice::Int RArrayI::size(const Ice::Current& current) {
	    return this->val.size();
	}

	void RArrayI::add(const omero::RTypePtr& val, const Ice::Current& current) {
	    this->val.push_back(val);
	}

	void RArrayI::addAll(const omero::RTypeSeq& values, const Ice::Current& current) {
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

	RTypeSeq RListI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RListI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// Collection methods
	omero::RTypePtr RListI::get(Ice::Int idx, const Ice::Current& current) {
	    return this->val[idx];
	}

	Ice::Int RListI::size(const Ice::Current& current) {
	    return this->val.size();
	}

	void RListI::add(const omero::RTypePtr& val, const Ice::Current& current) {
	    this->val.push_back(val);
	}

	void RListI::addAll(const omero::RTypeSeq& values, const Ice::Current& current) {
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

	RTypeSeq RSetI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RSetI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// Collection methods
	omero::RTypePtr RSetI::get(Ice::Int idx, const Ice::Current& current) {
	    return this->val[idx];
	}

	Ice::Int RSetI::size(const Ice::Current& current) {
	    return this->val.size();
	}

	void RSetI::add(const omero::RTypePtr& val, const Ice::Current& current) {
	    this->val.push_back(val);
	}

	void RSetI::addAll(const omero::RTypeSeq& values, const Ice::Current& current) {
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

	RTypeDict RMapI::getValue(const Ice::Current& current) { return this->val; }

	Ice::Int RMapI::compare(const omero::RTypePtr& rhs, const Ice::Current& current) {
	    throw new omero::ClientError(__FILE__,__LINE__,"Not implemented");
	}

	// Collection methods
	omero::RTypePtr RMapI::get(const std::string& key, const Ice::Current& current) {
	    return this->val[key];
	}

	void RMapI::put(const std::string& key, const omero::RTypePtr& value, const Ice::Current& current) {
	    this->val[key] = value;
	}

	Ice::Int RMapI::size(const Ice::Current& current) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
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
	    Ice::ObjectPtr create(const std::string& id) {
		return new RMapI();
	    }
	};


	// Shared state (flyweight)
	// =========================================================================

	// Defined in header

	// Object factories
	// =========================================================================

	std::map<std::string, ObjectFactoryPtr> objectFactories() {
	    static std::map<std::string, ObjectFactoryPtr> of;
	    if (of.size() == 0) {
		of[RBoolI::ice_staticId()] = new RBoolIFactory();
		of[RDoubleI::ice_staticId()] = new RDoubleIFactory();
		of[RFloatI::ice_staticId()] = new RFloatIFactory();
		of[RIntI::ice_staticId()] = new RIntIFactory();
		of[RLongI::ice_staticId()] = new RLongIFactory();
		of[RTimeI::ice_staticId()] = new RTimeIFactory();
		of[RClassI::ice_staticId()] = new RClassIFactory();
		of[RStringI::ice_staticId()] = new RStringIFactory();
		of[RInternalI::ice_staticId()] = new RInternalIFactory();
		of[RObjectI::ice_staticId()] = new RObjectIFactory();
		of[RArrayI::ice_staticId()] = new RArrayIFactory();
		of[RListI::ice_staticId()] = new RListIFactory();
		of[RSetI::ice_staticId()] = new RSetIFactory();
		of[RMapI::ice_staticId()] = new RMapIFactory();
	    }
	    return of;
	}

    }

}
