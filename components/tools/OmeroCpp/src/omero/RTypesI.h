/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPESI_H
#define OMERO_RTYPESI_H

#include <omero/Scripts.h>
#include <omero/RTypes.h>
#include <Ice/Ice.h>
#include <string>
#include <map>

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

	// Static factory methods (primitives)
	// =========================================================================

	const omero::RBoolPtr rbool(bool val);
	const omero::RDoublePtr rdouble(Ice::Double val);
	const omero::RFloatPtr rfloat(Ice::Float val);
	const omero::RIntPtr rint(Ice::Int val);
	const omero::RLongPtr rlong(Ice::Long val);
	const omero::RTimePtr rtime(Ice::Long val);

	// Static factory methods (objects)
	// =========================================================================

	const omero::RInternalPtr rinternal(const omero::InternalPtr& val);
	const omero::RObjectPtr robject(const omero::model::IObjectPtr& val);
	const omero::RClassPtr rclass(const std::string& val);
	const omero::RStringPtr rstring(const std::string& val);

	// Static factory methods (collections)
	// =========================================================================

	const omero::RArrayPtr rarray();
	const omero::RListPtr rlist();
	const omero::RSetPtr rset();
	const omero::RMapPtr rmap();

	// Implementations (primitives)
	// =========================================================================

	class RBoolI : virtual public omero::RBool {
	protected:
	    virtual ~RBoolI(); // protected as outlined in Ice Docs
	public:
	    RBoolI(bool value);
	    virtual bool getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	class RDoubleI : virtual public omero::RDouble {
	protected:
	    virtual ~RDoubleI(); // as above
	public:
	    RDoubleI(Ice::Double value);
	    virtual Ice::Double getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	class RFloatI : virtual public omero::RFloat {
	protected:
	    virtual ~RFloatI(); // as above
	public:
	    RFloatI(Ice::Float value);
	    virtual Ice::Float getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());

	};

	class RIntI : virtual public omero::RInt {
	protected:
	    virtual ~RIntI(); // as above
	public:
	    RIntI(Ice::Int value);
	    virtual Ice::Int getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());

	};

	class RLongI : virtual public omero::RLong {
	protected:
	    virtual ~RLongI(); // as above
	public:
	    RLongI(Ice::Long value);
	    virtual Ice::Long getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());

	};

	class RTimeI : virtual public omero::RTime {
	protected:
	    virtual ~RTimeI(); // as above
	public:
	    RTimeI(Ice::Long value);
	    virtual Ice::Long getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	// Implementations (objects)
	// =========================================================================

	class RInternalI : virtual public omero::RInternal {
	protected:
	    virtual ~RInternalI(); // as above
	public:
	    RInternalI(const omero::InternalPtr& value);
	    virtual omero::InternalPtr getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	class RObjectI : virtual public omero::RObject {
	protected:
	    virtual ~RObjectI(); // as above
	public:
	    RObjectI(const omero::model::IObjectPtr& value);
	    virtual omero::model::IObjectPtr getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	class RStringI : virtual public omero::RString {
	protected:
	    virtual ~RStringI(); // as above
	public:
	    RStringI(const std::string& value);
	    virtual std::string getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	class RClassI : virtual public omero::RClass {
	protected:
	    virtual ~RClassI(); // as above
	public:
	    RClassI(const std::string& value);
	    virtual std::string getValue(const Ice::Current& current = Ice::Current());
	    virtual Ice::Int compare(const RTypePtr& rhs, const Ice::Current& current = Ice::Current());
	};

	// Implementations (collections)
	// =========================================================================

	/**
	 * Guaranteed to never contain an empty list.
	 */
	class RArrayI : virtual public omero::RArray {
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

	/**
	 * Guaranteed to never contain an empty list.
	 */
	class RListI : virtual public omero::RList {
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

	/**
	 * Guaranteed to never contain an empty list.
	 */
	class RSetI : virtual public omero::RSet {
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

	class RMapI : virtual public omero::RMap {
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

	class ObjectFactory : virtual public Ice::ObjectFactory {
	protected:
	    std::string id;
	    virtual ~ObjectFactory(); // as above
	public:
	    virtual void register_(const Ice::CommunicatorPtr& ic);
	    virtual Ice::ObjectPtr create(const std::string& id) = 0;
	    virtual void destroy() { } // No-op
	};

	typedef IceUtil::Handle<ObjectFactory> ObjectFactoryPtr;

	// Shared state (flyweight)
	// =========================================================================

	const omero::RBoolPtr rtrue = new RBoolI(true);

	const omero::RBoolPtr rfalse = new RBoolI(false);

	const omero::RLongPtr rlong0 = new RLongI(0);

	const omero::RIntPtr rint0 = new RIntI(0);

	const omero::RStringPtr remptystr = new RStringI("");

	const omero::RClassPtr remptyclass = new RClassI("");

	const omero::RInternalPtr rnullinternal = new RInternalI(omero::InternalPtr());

	const omero::RObjectPtr rnullobject = new RObjectI(omero::model::IObjectPtr());

	// Object factories
	// =========================================================================

	std::map<std::string, ObjectFactoryPtr> objectFactories();

    }

}

#endif // OMERO_RTYPESI_H
