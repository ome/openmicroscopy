/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.IShare;
import ome.services.blitz.util.BlitzExecutor;
import omero.RTime;
import omero.ValidationException;
import omero.api.AMD_IShare_activate;
import omero.api.AMD_IShare_addComment;
import omero.api.AMD_IShare_addGuest;
import omero.api.AMD_IShare_addGuests;
import omero.api.AMD_IShare_addObject;
import omero.api.AMD_IShare_addObjects;
import omero.api.AMD_IShare_addReply;
import omero.api.AMD_IShare_addUser;
import omero.api.AMD_IShare_addUsers;
import omero.api.AMD_IShare_closeShare;
import omero.api.AMD_IShare_createShare;
import omero.api.AMD_IShare_deleteComment;
import omero.api.AMD_IShare_getAllGuests;
import omero.api.AMD_IShare_getAllMembers;
import omero.api.AMD_IShare_getAllShares;
import omero.api.AMD_IShare_getAllUsers;
import omero.api.AMD_IShare_getComments;
import omero.api.AMD_IShare_getContentMap;
import omero.api.AMD_IShare_getContentSize;
import omero.api.AMD_IShare_getContentSubList;
import omero.api.AMD_IShare_getContents;
import omero.api.AMD_IShare_getMemberShares;
import omero.api.AMD_IShare_getMemberSharesFor;
import omero.api.AMD_IShare_getOwnShares;
import omero.api.AMD_IShare_getShare;
import omero.api.AMD_IShare_getSharesOwnedBy;
import omero.api.AMD_IShare_removeGuest;
import omero.api.AMD_IShare_removeGuests;
import omero.api.AMD_IShare_removeObject;
import omero.api.AMD_IShare_removeObjects;
import omero.api.AMD_IShare_removeUser;
import omero.api.AMD_IShare_removeUsers;
import omero.api.AMD_IShare_setActive;
import omero.api.AMD_IShare_setDescription;
import omero.api.AMD_IShare_setExpiration;
import omero.api._IShareOperations;
import omero.model.Annotation;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.TextAnnotation;
import Ice.Current;

/**
 * Implementation of the IShare service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IShare
 */
public class ShareI extends AbstractAmdServant implements _IShareOperations {

    public ShareI(IShare service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void activate_async(AMD_IShare_activate __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void addComment_async(AMD_IShare_addComment __cb, long shareId,
            String comment, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, comment);

    }

    public void addGuest_async(AMD_IShare_addGuest __cb, long shareId,
            String emailAddress, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, emailAddress);

    }

    public void addGuests_async(AMD_IShare_addGuests __cb, long shareId,
            String[] emailAddresses, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, emailAddresses);

    }

    public void addObject_async(AMD_IShare_addObject __cb, long shareId,
            IObject iobject, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, iobject);

    }

    public void addObjects_async(AMD_IShare_addObjects __cb, long shareId,
            List<IObject> iobjects, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, iobjects);

    }

    public void addReply_async(AMD_IShare_addReply __cb, long shareId,
            String comment, TextAnnotation replyTo, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, comment, replyTo);

    }

    public void addUser_async(AMD_IShare_addUser __cb, long shareId,
            Experimenter exp, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, exp);

    }

    public void addUsers_async(AMD_IShare_addUsers __cb, long shareId,
            List<Experimenter> exps, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, exps);

    }

    public void closeShare_async(AMD_IShare_closeShare __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void createShare_async(AMD_IShare_createShare __cb,
            String description, RTime expiration, List<IObject> items,
            List<Experimenter> exps, String[] guests, boolean enabled,
            Current __current) {
        serviceInterfaceCall(__cb, __current, description, expiration, items,
                exps, guests, enabled);

    }

    public void deleteComment_async(AMD_IShare_deleteComment __cb,
            Annotation comment, Current __current) {
        serviceInterfaceCall(__cb, __current, comment);

    }

    public void getAllGuests_async(AMD_IShare_getAllGuests __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getAllMembers_async(AMD_IShare_getAllMembers __cb,
            long shareId, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getAllShares_async(AMD_IShare_getAllShares __cb,
            boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, active);

    }

    public void getAllUsers_async(AMD_IShare_getAllUsers __cb, long shareId,
            Current __current) throws ValidationException {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getComments_async(AMD_IShare_getComments __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getContentMap_async(AMD_IShare_getContentMap __cb,
            long shareId, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getContentSize_async(AMD_IShare_getContentSize __cb,
            long shareId, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getContentSubList_async(AMD_IShare_getContentSubList __cb,
            long shareId, int start, int finish, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, start, finish);

    }

    public void getContents_async(AMD_IShare_getContents __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getMemberSharesFor_async(AMD_IShare_getMemberSharesFor __cb,
            Experimenter user, boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, user, active);

    }

    public void getMemberShares_async(AMD_IShare_getMemberShares __cb,
            boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, active);

    }

    public void getOwnShares_async(AMD_IShare_getOwnShares __cb,
            boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, active);

    }

    public void getShare_async(AMD_IShare_getShare __cb, long shareId,
            Current __current) {
        serviceInterfaceCall(__cb, __current, shareId);

    }

    public void getSharesOwnedBy_async(AMD_IShare_getSharesOwnedBy __cb,
            Experimenter user, boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, user, active);

    }

    public void removeGuest_async(AMD_IShare_removeGuest __cb, long shareId,
            String emailAddress, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, emailAddress);

    }

    public void removeGuests_async(AMD_IShare_removeGuests __cb, long shareId,
            String[] emailAddresses, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, emailAddresses);

    }

    public void removeObject_async(AMD_IShare_removeObject __cb, long shareId,
            IObject iobject, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, iobject);

    }

    public void removeObjects_async(AMD_IShare_removeObjects __cb,
            long shareId, List<IObject> iobjects, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, iobjects);

    }

    public void removeUser_async(AMD_IShare_removeUser __cb, long shareId,
            Experimenter exp, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, exp);

    }

    public void removeUsers_async(AMD_IShare_removeUsers __cb, long shareId,
            List<Experimenter> exps, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, exps);

    }

    public void setActive_async(AMD_IShare_setActive __cb, long shareId,
            boolean active, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, active);

    }

    public void setDescription_async(AMD_IShare_setDescription __cb,
            long shareId, String description, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, description);

    }

    public void setExpiration_async(AMD_IShare_setExpiration __cb,
            long shareId, RTime expiration, Current __current) {
        serviceInterfaceCall(__cb, __current, shareId, expiration);

    }

}
