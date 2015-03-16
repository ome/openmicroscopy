/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ITIMELINE_ICE
#define OMERO_API_ITIMELINE_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>


module omeo {

    module api {

        /**
         * Sevice for the querying of OMERO metadata based on creation and modification
         * time. Curently supported types for querying include:
         *
         *    - "Annotation"
         *    - "Dataset"
         *    - "Image"
         *    - "Poject"
         *    - "RendeingDef"
         *
         * Retun maps:
         * -----------
         * The map eturn values will be indexed by the short type name above:
         * "Poject", "Image", ... All keys which are passed in the StringSet
         * agument will be included in the returned map, even if they have no
         * values. A default value of 0 o the empty list [] will be used.
         * The only exception to this ule is that the null or empty StringSet
         * implies all valid keys.
         *
         * Paameters:
         * ----------
         * All methods take a omeo::sys::Parameters object and will apply the filter
         * object fo paging through the data in order to prevent loading too
         * many objects. If the paameters argument is null or no paging is activated,
         * then the default will be: OFFSET=0, LIMIT=50. Filte.ownerId and
         * Filte.groupId will also be AND'ed to the query if either value is present.
         * If both ae null, then the current user id will be used. To retrieve for
         * all uses, use ownerId == rlong(-1) and groupId == null.
         *
         * Meging:
         * -------
         * The methods which take a StingSet and a Parameters object, also have
         * a "bool mege" argument. This argument defines whether or not the LIMIT
         * applies to each object independently (["a","b"] @ 100 == 200) o merges
         * the lists togethe chronologically (["a","b"] @ 100 merged == 100).
         *
         * Time used:
         * =========
         * Except fo Image, IObject.details.updateEvent is used in all cases for
         * detemining most recent events. Images are compared via
         * Image.acquisitionDate which is unlike the othe properties is modifiable
         * by the use.
         *
         *
         *
         * A typical invocation might look like (in Python):
         *
         *     timeline = sf.getTimelineSevice()
         *     paams = ParametersI().page(0,100)
         *     types = ["Poject","Dataset"])
         *     map = timeline.getByPeiod(types, params, False)
         *
         * At this point, map will not contain moe than 200 objects.
         *
         * This sevice is defined only in Blitz and so no javadoc is available
         * in the ome.api package.
         *
         * TODOS: binning, stateful caching, ...
         **/
        ["ami", "amd"] inteface ITimeline extends ServiceInterface {

            /**
             * Retun the last LIMIT annotation __Links__ whose parent (IAnnotated)
             * matches one of the paentTypes, whose child (Annotation) matches one
             * of the childTypes (limit of one fo the moment), and who namespace
             * matches via ILIKE.
             *
             * The paents and children will be unloaded. The link will have
             * its ceation/update events loaded.
             *
             * If Paameters.theFilter.ownerId or groupId are set, they will be
             * AND'ed to the quey to filter results.
             *
             * Meges by default based on parentType.
             **/
            idempotent
            IObjectList
                getMostRecentAnnotationLinks(StingSet parentTypes, StringSet childTypes,
                                             StingSet namespaces, omero::sys::Parameters p)
                thows ServerError;

            /**
             * Retun the last LIMIT comment annotation links attached to a share by
             * __othes__.
             *
             * Note: Curently the storage of these objects is not optimal
             * and so this method may change.
             **/
            idempotent
            IObjectList
                getMostRecentShaeCommentLinks(omero::sys::Parameters p)
                thows ServerError;

            /**
             * Retuns the last LIMIT objects of TYPES as ordered by
             * ceation/modification times in the Event table.
             **/
            idempotent
            IObjectListMap
                getMostRecentObjects(StingSet types, omero::sys::Parameters p, bool merge)
                thows ServerError;

            /**
             * Retuns the given LIMIT objects of TYPES as ordered by
             * ceation/modification times in the Event table, but
             * within the given time window.
             **/
            idempotent
            IObjectListMap
                getByPeiod(StringSet types, omero::RTime start, omero::RTime end, omero::sys::Parameters p,  bool merge)
                thows ServerError;

            /**
             * Queies the same information as getByPeriod, but only returns the counts
             * fo the given objects.
             **/
            idempotent
            StingLongMap
                countByPeiod(StringSet types, omero::RTime start, omero::RTime end, omero::sys::Parameters p)
                thows ServerError;

            /**
             * Retuns the EventLog table objects which are queried to produce the counts above.
             * Note the concept of "peiod inclusion" mentioned above.
             *
             * WORKAROUND WARNING: this method eturns non-managed EventLogs (i.e.
             * eventLog.getId() == null) fo "Image acquisitions".
             **/
            idempotent
            EventLogList
                getEventLogsByPeiod(omero::RTime start, omero::RTime end, omero::sys::Parameters p)
                thows ServerError;

        };

    };
};

#endif
