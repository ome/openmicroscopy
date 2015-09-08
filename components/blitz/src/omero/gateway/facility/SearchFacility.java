/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.facility;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ome.util.search.LuceneQueryBuilder;
import omero.ApiUsageException;
import omero.InternalException;
import omero.api.SearchPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.SearchParameters;
import omero.gateway.model.SearchResult;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.SearchScope;
import omero.model.Details;
import omero.model.DetailsI;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.gateway.model.DataObject;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.util.PojoMapper;

/**
 * A {@link Facility} for performing searches
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class SearchFacility extends Facility {

    /** Reference to the {@link BrowseFacility} */
    private BrowseFacility browse;

    /**
     * Creates a new instance
     * @param gateway Reference to the {@link Gateway}
     */
    SearchFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Searches for data.
     *
     * @param ctx
     *            The security context.
     * @param context
     *            The context of search (if context.groupId == -1 the scope of
     *            the search will be all groups, otherwise the scope of the
     *            search will be the group set in the security context)
     * @return The found objects.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public SearchResultCollection search(SecurityContext ctx, SearchParameters context)
            throws DSOutOfServiceException, DSAccessException {

        SearchResultCollection result = new SearchResultCollection();

        if (context.getTypes().isEmpty()) {
            return result;
        }

        SearchPrx service = gateway.getSearchService(ctx);

        int batchSize = context.getTypes().size() == 1 ? 1000 : context
                .getTypes().size() * 500;

        // if search for Plates automatically include Plate Runs
        if (context.getTypes().contains(PlateData.class))
            context.getTypes().add(PlateAcquisitionData.class);

        for (Class<? extends DataObject> type : context.getTypes()) {
            try {
                // set general parameters
                service.clearQueries();
                service.setAllowLeadingWildcard(true);
                service.setCaseSentivice(false);
                String searchForClass = PojoMapper.convertTypeForSearch(type);
                service.onlyType(searchForClass);
                service.setBatchSize(batchSize);

                // set the owner/group restriction
                if (context.getUserId() >= 0) {
                    Details ownerRestriction = new DetailsI();
                    Experimenter exp = (Experimenter) browse.findIObject(ctx,
                            Experimenter.class.getName(), context.getUserId());
                    ownerRestriction.setOwner(exp);
                    service.onlyOwnedBy(ownerRestriction);
                }

                // set time
                Date from = null;
                Date to = null;
                String dateType = null;
                if (context.getDateType() != -1) {
                    Timestamp start = context.getStart();
                    Timestamp end = context.getEnd();
                    from = start != null ? new Date(start.getTime()) : null;
                    to = end != null ? new Date(end.getTime()) : null;
                    if (context.getDateType() == SearchParameters.DATE_ACQUISITION)
                        dateType = LuceneQueryBuilder.DATE_ACQUISITION;
                    else
                        dateType = LuceneQueryBuilder.DATE_IMPORT;
                }

                Map<String, String> m = new HashMap<String, String>();
                if (context.getGroupId() == SearchParameters.ALL_GROUPS_ID) {
                    m.put("omero.group", "-1");
                } else {
                    m.put("omero.group", "" + ctx.getGroupID());
                }

                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                String fields = SearchScope.getStringRepresentation(context.getScope());
                String dFrom = from != null ? df.format(from) : null;
                String dTo = to != null ? df.format(to) : null;
                try {
                    service.byLuceneQueryBuilder(fields, dFrom, dTo, dateType,
                            context.getQuery(), m);
                } catch (ApiUsageException e) {
                    result.setError(SearchResultCollection.GENERAL_ERROR);
                    return result;
                }

                try {
                    if (service.hasNext(m)) {
                        List<IObject> l = service.results(m);
                        Iterator<IObject> k = l.iterator();
                        IObject object;
                        while (k.hasNext()) {
                            object = k.next();
                            if (searchForClass.equals(object.getClass()
                                    .getName())) {
                                SearchResult sr = new SearchResult();
                                sr.setObject(PojoMapper.asDataObject(object));
                                if (!result.contains(sr))
                                    result.add(sr);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof InternalException) {
                        if (e.toString().contains("TooManyClauses"))
                            result.setError(SearchResultCollection.TOO_MANY_CLAUSES);
                        else
                            result.setError(SearchResultCollection.GENERAL_ERROR);
                    } else {
                        result.setError(SearchResultCollection.TOO_MANY_RESULTS_ERROR);
                    }

                    gateway.closeService(ctx, service);

                    return result;
                }

                service.clearQueries();

            } catch (Throwable e) {
                handleException(this, e, "Could not load hierarchy");
            }
        }

        if (service != null)
            gateway.closeService(ctx, service);

        return result;
    }
}
