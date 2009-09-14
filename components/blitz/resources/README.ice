/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef  README_ICE
#define  README_ICE

// The *.ice files that are included here are slice definitions
// which are used to generate much of the code (headers, C++, and
// Java classes) which compose blitz.
//
// The documentation which follows is used by the release-slice2html
// target to generate html under dist/docs/api/slice2html. The latest
// copy of which is available here:
//
// http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/slice2html/

/**

Main module.


<h1>OMERO</h1>

<p>
The omero/ ice files are the central definitions of the blitz
implementation. They provide an Ice facade over the existing
OMERO.server architecture. Because of the increased overhead of
creating implementations in multiple languages, the mappings are
simplified and work mostly as simple data transfer objects. For more
on the individual classes, see the slice definition files.
</p>

<h2>omero/model *.ice</h2>

<p>
The slice definitions under omero/model were themselves code-generated
and are largely undocumented. Instead, this README helps one to
understand just what those files mean.
</p>

<p>
Before each class definition, an Ice sequence is created of the form
"FieldNameSeq". The annotation which precedes the sequence definition
["java:type:java.util.ArrayList"] changes the default mapping from a
native Java array, to a List with generics. In C++, the sequence will
become a typedef for vector<type>.
</p>

<p>
The class itself will either subclass omero::model::IObject or another
subclass of omero::model::IObject (defined in OMERO/IObject.ice).
Otherwise, only fields are defined which will be made into public
fields in all the language bindings. Note the "sequenceNameLoaded" fields
which are used strictly to allow for marking the collection fields as
"null" since Ice automatically converts nulls to empty collections.
</p>

<p>
It should be noted that the classes generated from this definition
will be abstract because of the "unload()" method in the IObject
superclass. When a method is defined on a class in slice, the
resulting object is abstract, and a concrete implementation must be
provided. This is done via the types ending in "I" (for
implementation) in the omero::model package. An ObjectFactory is
required to tell an Ice communicator how to map type names to
concrete implementations.
</p>

<h2>RTypes</h2>

<p>
RType-sub["protected"] classes permit both the passing of null values to
OMERO.blitz, since the Ice protocol maps null values to default
(the empty string, 0.0, etc.), and a simple implementation of an
"Any" value.
</p>


<p>
Usage (C++):
</p>

<pre>

    omero::RBoolPtr b1 = new omero::RBool(true);
    omero::RBoolPtr b2 = someObjPtr->getBool();
    if (b2 && b2.val) { ... };
    // the first test, checks if the pointer is null
</pre>

<p>
 Usage (Java):
</p>

<pre>
    omero.RBool b1 = new omero.RBool(true);
    omero.RBool b2 = someObj.getBool();
    if (b2!=null && b2.val) { ... };
    // no operator overloading; check for null directly.
</pre>


**/
module omero {


/**

Interfaces and types running the backend facilities.

**/
module grid {

}; /* End grid */

/**
Code-generated types based on the <a href="http://www.ome-xml.org">OME Specification</a>.
**/
module model {};


/**

Various core types.

**/
module sys {};


}; /* End omero */

#endif
