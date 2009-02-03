/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_METADATASTORE_ICE
#define OMERO_METADATASTORE_ICE

#include <Ice/BuiltinSequences.ice>
#include <omero/API.ice>
#include <omero/ServerErrors.ice>

module omero {

    module constants {

	const string METADATASTORE = "omero.api.MetadataStore";

    };

	module metadatastore {
	
	class IObjectContainer { 
		string LSID;
		omero::api::StringIntMap indexes;
		omero::model::IObject sourceObject;
	};

	sequence<IObjectContainer> IObjectContainerArray; 
	
	};

    module api {

	["ami","amd"] interface MetadataStore extends StatefulServiceInterface
	    {
		void createRoot() throws ServerError;
		void updateObjects(omero::metadatastore::IObjectContainerArray objects) throws ServerError;
		void updateReferences(omero::api::StringStringMap references) throws ServerError;
		void setChannelGlobalMinMax(int channelIdx, double globalMin, double globalMax, int pixelsIndex) throws ServerError;
		PixelsList saveToDB() throws ServerError;
		void populateMinMax(RLong id, RInt i) throws ServerError;
	    };
    };

};
#endif
