/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.services.messages.ContextMessage;
import ome.system.OmeroContext;

import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.GraphModify;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;

/**
 * Permits performing multiple operations
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class DoAllI extends DoAll implements IRequest {

    private static final long serialVersionUID = -323423435135556L;

    //
    // Mapping from steps to subrequests
    //

    /**
     * Pointer-like object which is saved for each
     * sub-request. The logic for properly mapping
     * from the global step number to the individual
     * substep number is done here. In order to find
     * the proper {@link X} instance, use the current
     * index:
     *
     * <pre>
     * X x = substeps.get(current);
     * </pre>
     */
    private static class X {

        /**
         * Number of steps that should be deducted from the global step
         * count in order to have the proper substep number
         */
        final int offset;

        /**
         * Sub-{@link Helper} instance for the {@link #r}
         */
        final Helper h;

        /**
         * Sub-{@link Request} instance which is to be run.
         */
        final IRequest r;

        final OmeroContext ctx;

        /**
         * Calculated context which should be in effect for {@link #r}.
         */
        Map<String, String> c = null;

        X(int offset, Helper h, IRequest r, OmeroContext ctx) {
            this.offset = offset;
            this.h = h;
            this.r = r;
            this.ctx = ctx;
        }

        /**
         * Run the {@link IRequest#step(int)} passing in the proper substep
         * value after being calculated via {@link #offset}
         */
        Object step(int step) {
            return r.step(step - offset);
        }

        /**
         * Run the {@link IRequest#buildResponse(int, Object)} passing in the
         * proper substep value after being calculated via {@link #offset}
         */
        void buildResponse(int step, Object object) {
            r.buildResponse(step - offset, object);
        }

        /**
         * Fill in the call context for this instance ignoring nulls and empty
         * maps.
         * @param classContext The return value of {@link IRequest#getCallContext()}
         * @param callContext The corresponding instance from {@link DoAll#contexts}
         */
        void calculateContext(Map<String, String> classContext,
            Map<String, String> callContext) {
            putAll(classContext);
            putAll(callContext);
        }

        /**
         * Helper
         */
        private void putAll(Map<String, String> context) {
            if (context != null && context.size() > 0) {
                if (c == null) {
                    c = new HashMap<String, String>();
                }
                c.putAll(context);
            }
        }

        /**
         * Send a {@link PushContextMessage} to apply this context if not null.
         */
        void login() throws Throwable {
            if (c !=  null) {
                h.debug("Login: %s", c);
                ctx.publishMessage(new ContextMessage.Push(this, c));
            }
        }

        /**
         * If a {@link PushContextMessage} was sent, send a {@link PopContextMessage}
         * so that the context for following actions are not polluted.
         */
        void logout() throws Throwable {
            if (c != null) {
                ctx.publishMessage(new ContextMessage.Pop(this ,c));
            }
        }
    }

    /**
     * State-objects for each subrequest
     */
    private final List<X> substeps = new ArrayList<X>();

    /**
     * current substep.
     */
    private int current = -1;

    /**
     * step at which we flip to the next current.
     */
    private int nextAt = 0;

    /**
     * Looks up the current substep based on the total step count using
     * {@link #nextAt} to determine if {@link #current} needs to be incremented.
     * If login is true, then {@link X#login()} and {@link X#logout} will be
     * called as appropriate.
     *
     * @param step
     * @param login
     * @return
     */
    private X substep(final int step, final boolean login) {
        X x = null;
        try {
            if (step == 0) {
                // Restart
                x = substeps.get(0);
                current = 0;
                nextAt = x.offset + x.h.getSteps();
                if (login) {
                    x.login();
                }
            } else if (step == nextAt) {
                // Flip to next substep. We should never have
                // a step which makes current >= substeps.size()

                X prev = substeps.get(current);
                if (login) {
                    prev.logout();
                }

                current += 1;
                x = substeps.get(current);
                nextAt = x.offset + x.h.getSteps();
                if (login) {
                    x.login();
                }
            } else {
                x = substeps.get(current);
            }
            return x;
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "substep-lookup-failed", "step",
                ""+step, "req", (x==null ? "null" : ""+x.r));
        }
    }

    //
    // Primary state
    //

    private final List<Status> statuses = new ArrayList<Status>();

    private final List<Response> responses = new ArrayList<Response>();

    /**
     * Helper instance for this class. Will create a number of sub-helper
     * instances for each request.
     */
    private Helper helper;

    //
    // For publishing messages
    //
    //

    private final OmeroContext ctx;

    public DoAllI(OmeroContext ctx) {
        this.ctx = ctx;
    }

    //
    // IRequest methods
    //

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        int steps = 0;
        try {
            preprocessRequests();
            for (int i = 0; i < this.requests.size(); i++) {
                final Request req = requests.get(i);
                final Status substatus = new Status();
                final Helper subhelper = helper.subhelper(req, substatus);
                if (req instanceof IRequest) {
                    IRequest ireq = (IRequest) req;
                    final X x = new X(steps, subhelper, ireq, ctx);
                    try {
                        x.calculateContext(ireq.getCallContext(),
                                (contexts == null || contexts.length <= i)
                                    ? null : contexts[i]);
                        x.login();
                        try {
                            ireq.init(subhelper);
                            statuses.add(substatus);
                            substeps.add(x);
                            long intermediate = substatus.steps;
                            if ((intermediate + steps) > Integer.MAX_VALUE) {
                                throw helper.cancel(new ERR(), null, "too-many-steps",
                                    "Steps", ""+intermediate,
                                    "Message", "Too many steps found! Try fewer actions in one command");
                            }
                            steps += intermediate;
                        } finally {
                            x.logout();
                        }
                    } catch (Cancel c) {
                        throw subcancel(c, x);
                    }
                }
                else {
                    throw helper.cancel(new ERR(), null, "bad-request",
                        "type", req.ice_id());
                }
            }
        }  catch (Cancel c) {
            throw c; // just re-throw
        } catch (Throwable t) {
            helper.cancel(new ERR(), t, "bad-init");
        }
        helper.setSteps(steps);
    }

    public Object step(int step) {
        helper.assertStep(step);
        final X x = substep(step, true);
        try {
            return x.step(step);
        }
        catch (Cancel c) {
            throw subcancel(c, x);
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        final X x = substep(step, false);
        x.buildResponse(step, object);

        if (helper.isLast(step)) {
            for (Request subreq : requests) {
                // Again, must be an irequest
                IRequest ireq = (IRequest) subreq;
                responses.add(ireq.getResponse());
            }
            DoAllRsp rsp = new DoAllRsp(responses, statuses);
            helper.setResponseIfNull(rsp);
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    protected Cancel subcancel(Cancel c, X x) {
        final Response subrsp = x.h.getResponse();
        final Status substatus = x.h.getStatus();
        final Status status = helper.getStatus();
        helper.setResponseIfNull(subrsp);
        status.flags.addAll(substatus.flags);
        if (status.parameters == null) {
            status.parameters = new HashMap<String, String>();
        }
        if (substatus.parameters != null) {
            status.parameters.putAll(substatus.parameters);
        }
        status.parameters.put("subrequest", ""+subrsp);
        status.category = ice_id();
        status.name = "subcancel";
        throw c;
    }

    //
    // Specializations
    //

    @SuppressWarnings("unchecked")
    private void preprocessRequests() {

        if (requests.size() == 0) {
            return; // EARLY EXIT
        }

        // These cached values will be used for each operation type.
        final Map<Long, Set<Long>> filesetIdToImageIds = new HashMap<Long, Set<Long>>();
        final Map<Long, Long> imageIdToFilesetId = new HashMap<Long, Long>();

        for (@SuppressWarnings("rawtypes") Class op : new Class[]{Chgrp.class, Delete.class}) {

            // Targets for possible optimization.
            final Set<Request> targets = new HashSet<Request>();

            // Known IDs
            final Set<Long> knownImageIds = new HashSet<Long>();

            // 1. Lookup all filesets for the given images.
            for (Request request : requests) {
                if (op.isAssignableFrom(request.getClass())) {
                    GraphModify gm = (GraphModify) request;
                    String type = gm.type;
                    long id = gm.id;
                    if ("/Image".equals(type)) {
                        targets.add(request);
                        knownImageIds.add(id);
                        lookupFilesetForImage(id, imageIdToFilesetId, filesetIdToImageIds);
                    }
                }
            }    

            // 2. For those filesets which have all their images selected,
            // reduce the entries found by inserting the fileset operation
            // at the location of the last contained image.
            for (Map.Entry<Long, Set<Long>> entry : filesetIdToImageIds.entrySet()) {
                final Long filesetId = entry.getKey();
                final Set<Long> imageIds = entry.getValue();

                if (imageIds.size() < 2) {
                    helper.debug("Skipping on Image count " + imageIds.size());
                    continue; // SKIP
                }

                Request lastRequest = null;
                int lastIndex = -1;
                for (int i = requests.size() - 1; i >= 0; i--) {
                    Request request = requests.get(i);
                    if (!(targets.contains(request))) {
                        continue; // SKIP
                    }
                    if (knownImageIds.containsAll(imageIds)) {
                        Request popped = requests.remove(i);
                        if (lastRequest == null) {
                            lastRequest = popped; 
                            lastIndex = i;
                        }
                    }
                }

                if (lastIndex >= 0) {
                    // FIXME: this does not look into modifying the options
                    // set by the user.
                    GraphModify gm = ((GraphModify) lastRequest);
                    gm.type = "/Fileset";
                    gm.id = filesetId;
                    requests.add(lastIndex, lastRequest);
                }
            }
        }
    }

    private void lookupFilesetForImage(long imageId1,
            Map<Long, Long> imageIdToFilesetId,
            Map<Long, Set<Long>> filesetIdToImageIds) {

        if (imageIdToFilesetId.containsKey(imageId1)) {
            return; // EARLY EXIT
        }

        helper.debug("Loading filesets for Image:" + imageId1);
        List<Object[]> rv =
        helper.getServiceFactory().getQueryService().projection(
                "select i.fileset.id, i2.id from Image i, Image i2 " +
                "where i.fileset.id = i2.fileset.id and i.id = " + imageId1, null);

        if (rv.size() <= 1) {
           // Only perform optimization for multi-image filesets (MIF)
            return; // EARLY EXIT
        }

        for (Object[] ids : rv) {
            Long filesetId = (Long) ids[0];
            Long imageId2 = (Long) ids[1];
            Set<Long> imageIds = filesetIdToImageIds.get(filesetId);
            if (imageIds == null) {
                imageIds = new HashSet<Long>();
                filesetIdToImageIds.put(filesetId, imageIds);
            }
            imageIds.add(imageId2);
            imageIdToFilesetId.put(imageId2, filesetId);
            helper.debug("Registered Image:%s=>Fileset:%s", imageId2, filesetId);
        }
        
    }

}
