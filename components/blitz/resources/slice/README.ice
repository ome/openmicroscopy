/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#undef  README_ICE
#ifdef  README_ICE

The *.ice files that are included here are slice definitions 
which are used to generate much of the code (headers, C++, and
Java classes) which compose blitz. 

OMERO
=====

The OMERO/ ice files are the central definitions of the blitz
implementation. They provide an Ice facade over the existing
OMERO.server architecture. Because of the increased overhead of
creating implementations in multiple languages, the mappings are
simplified and work mostly as simple data transfer objects. For more
on the individual classes, see the slice definition files.

OMERO/Model *.ice
=================

The slice definitions under OMERO/Model were themselves code-generated
and are largely undocumented. Instead, this README helps one to
understand just what those files mean.

Before each class definition, an Ice sequence is created of the form
"FieldNameSeq". The annotation which precedes the sequence definition
["java:type:java.util.ArrayList"] changes the default mapping from a
native Java array, to a List with generics. In C++, the sequence will
become a typedef for vector<type>. 

The class itself will either subclass omero::model::IObject or another
subclass of omero::model::IObject (defined in OMERO/IObject.ice).
Otherwise, only fields are defined which will be made into public
fields in all the language bindings. Note the "sequenceNameLoaded" fields 
which are used strictly to allow for marking the collection fields as
"null" since Ice automatically converts nulls to empty collections.

It should be noted that the classes generated from this definition
will be abstract because of the "unload()" method in the IObject
superclass. When a method is defined on a class in slice, the
resulting object is abstract, and a concrete implementation must be
provided. This is done via the types ending in "I" (for
implementation) in the omero::model package. An ObjectFactory is
required to tell an Ice communicator how to map type names to
concrete implementations.

#endif //README_ICE
