/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import static omero.rtypes.rlist;
import static omero.rtypes.rmap;

import java.util.List;
import java.util.Map;

import ome.api.IAdmin;
import ome.services.blitz.util.BlitzExecutor;
import omero.RString;
import omero.ServerError;
import omero.api.AMD_IAdmin_addGroupOwners;
import omero.api.AMD_IAdmin_addGroups;
import omero.api.AMD_IAdmin_canUpdate;
import omero.api.AMD_IAdmin_changeExpiredCredentials;
import omero.api.AMD_IAdmin_changeGroup;
import omero.api.AMD_IAdmin_changeOwner;
import omero.api.AMD_IAdmin_changePassword;
import omero.api.AMD_IAdmin_changePasswordWithOldPassword;
import omero.api.AMD_IAdmin_changePermissions;
import omero.api.AMD_IAdmin_changeUserPassword;
import omero.api.AMD_IAdmin_containedExperimenters;
import omero.api.AMD_IAdmin_containedGroups;
import omero.api.AMD_IAdmin_createExperimenter;
import omero.api.AMD_IAdmin_createExperimenterWithPassword;
import omero.api.AMD_IAdmin_createGroup;
import omero.api.AMD_IAdmin_createRestrictedSystemUser;
import omero.api.AMD_IAdmin_createRestrictedSystemUserWithPassword;
import omero.api.AMD_IAdmin_createSystemUser;
import omero.api.AMD_IAdmin_createUser;
import omero.api.AMD_IAdmin_deleteExperimenter;
import omero.api.AMD_IAdmin_deleteGroup;
import omero.api.AMD_IAdmin_getCurrentAdminPrivileges;
import omero.api.AMD_IAdmin_getAdminPrivileges;
import omero.api.AMD_IAdmin_getAdminsWithPrivileges;
import omero.api.AMD_IAdmin_getDefaultGroup;
import omero.api.AMD_IAdmin_getEventContext;
import omero.api.AMD_IAdmin_getExperimenter;
import omero.api.AMD_IAdmin_getGroup;
import omero.api.AMD_IAdmin_getLeaderOfGroupIds;
import omero.api.AMD_IAdmin_getMemberOfGroupIds;
import omero.api.AMD_IAdmin_getSecurityRoles;
import omero.api.AMD_IAdmin_getMyUserPhotos;
import omero.api.AMD_IAdmin_lookupExperimenter;
import omero.api.AMD_IAdmin_lookupExperimenters;
import omero.api.AMD_IAdmin_lookupGroup;
import omero.api.AMD_IAdmin_lookupGroups;
import omero.api.AMD_IAdmin_lookupLdapAuthExperimenter;
import omero.api.AMD_IAdmin_lookupLdapAuthExperimenters;
import omero.api.AMD_IAdmin_moveToCommonSpace;
import omero.api.AMD_IAdmin_removeGroupOwners;
import omero.api.AMD_IAdmin_removeGroups;
import omero.api.AMD_IAdmin_reportForgottenPassword;
import omero.api.AMD_IAdmin_setAdminPrivileges;
import omero.api.AMD_IAdmin_setDefaultGroup;
import omero.api.AMD_IAdmin_setGroupOwner;
import omero.api.AMD_IAdmin_synchronizeLoginCache;
import omero.api.AMD_IAdmin_unsetGroupOwner;
import omero.api.AMD_IAdmin_updateExperimenter;
import omero.api.AMD_IAdmin_updateExperimenterWithPassword;
import omero.api.AMD_IAdmin_updateGroup;
import omero.api.AMD_IAdmin_updateSelf;
import omero.api.AMD_IAdmin_uploadMyUserPhoto;
import omero.api._IAdminOperations;
import omero.model.AdminPrivilege;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Permissions;
import omero.util.IceMapper;
import Ice.Current;
import Ice.UserException;

/**
 * Implementation of the {@link omero.api.IAdmin} service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IAdmin
 */
public class AdminI extends AbstractAmdServant implements _IAdminOperations {

    public AdminI(IAdmin service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void addGroups_async(AMD_IAdmin_addGroups __cb, Experimenter user,
            List<ExperimenterGroup> groups, Current __current)
            throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_ARRAY);
        Object u = mapper.reverse(user);
        ome.model.meta.ExperimenterGroup[] array;
        if (groups == null) {
            array = new ome.model.meta.ExperimenterGroup[0];
        } else {
            array = new ome.model.meta.ExperimenterGroup[groups.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (ome.model.meta.ExperimenterGroup) mapper
                        .reverse(groups.get(i));
            }
        }
        callInvokerOnMappedArgs(mapper, __cb, __current, u, array);
    }

    public void canUpdate_async(
            AMD_IAdmin_canUpdate __cb, IObject obj, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj);
    }

    public void changeExpiredCredentials_async(
            AMD_IAdmin_changeExpiredCredentials __cb, String name,
            String oldCred, String newCred, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, name, oldCred, newCred);
    }

    public void changeGroup_async(AMD_IAdmin_changeGroup __cb, IObject obj,
            String omeName, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, omeName);
    }

    public void changeOwner_async(AMD_IAdmin_changeOwner __cb, IObject obj,
            String omeName, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj, omeName);
    }

    public void changePassword_async(AMD_IAdmin_changePassword __cb,
            RString newPassword, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, newPassword);
    }

    public void changePasswordWithOldPassword_async(AMD_IAdmin_changePasswordWithOldPassword __cb,
            RString oldPassword, RString newPassword, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, oldPassword, newPassword);
    }

    public void changePermissions_async(AMD_IAdmin_changePermissions __cb,
            IObject obj, Permissions perms, Current __current)
            throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.VOID);
        Object o = mapper.reverse(obj);
        Object p = mapper.convert(perms);
        callInvokerOnMappedArgs(mapper, __cb, __current, o, p);
    }

    public void changeUserPassword_async(AMD_IAdmin_changeUserPassword __cb,
            String omeName, RString newPassword, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, omeName, newPassword);
    }

    public void containedExperimenters_async(
            AMD_IAdmin_containedExperimenters __cb, long groupId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, groupId);
    }

    public void containedGroups_async(AMD_IAdmin_containedGroups __cb,
            long experimenterId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenterId);
    }

    public void createExperimenter_async(AMD_IAdmin_createExperimenter __cb,
            Experimenter user, ExperimenterGroup defaultGroup,
            List<ExperimenterGroup> groups, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user, defaultGroup, groups);
    }

    public void createExperimenterWithPassword_async(
            AMD_IAdmin_createExperimenterWithPassword __cb, Experimenter user,
            RString password, ExperimenterGroup defaultGroup,
            List<ExperimenterGroup> groups, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user, password, defaultGroup,
                groups);
    }
    
    public void createGroup_async(AMD_IAdmin_createGroup __cb,
            ExperimenterGroup group, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group);
    }

    public void createSystemUser_async(AMD_IAdmin_createSystemUser __cb,
            Experimenter experimenter, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter);
    }

    @Override
    public void createRestrictedSystemUser_async(AMD_IAdmin_createRestrictedSystemUser __cb,
            Experimenter experimenter, List<AdminPrivilege> privileges,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter, privileges);
    }

    @Override
    public void createRestrictedSystemUserWithPassword_async(AMD_IAdmin_createRestrictedSystemUserWithPassword __cb,
            Experimenter experimenter, List<AdminPrivilege> privileges,
            RString password, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter, privileges, password);
    }

    public void createUser_async(AMD_IAdmin_createUser __cb,
            Experimenter experimenter, String group, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter, group);
    }

    public void deleteExperimenter_async(AMD_IAdmin_deleteExperimenter __cb,
            Experimenter user, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user);
    }

    public void deleteGroup_async(AMD_IAdmin_deleteGroup __cb,
            ExperimenterGroup group, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group);
    }

    public void getDefaultGroup_async(AMD_IAdmin_getDefaultGroup __cb,
            long experimenterId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenterId);
    }

    public void getEventContext_async(AMD_IAdmin_getEventContext __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getExperimenter_async(AMD_IAdmin_getExperimenter __cb, long id,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id);
    }

    public void getGroup_async(AMD_IAdmin_getGroup __cb, long id,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id);
    }

    public void getSecurityRoles_async(AMD_IAdmin_getSecurityRoles __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void lookupExperimenter_async(AMD_IAdmin_lookupExperimenter __cb,
            String name, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, name);
    }

    public void lookupExperimenters_async(AMD_IAdmin_lookupExperimenters __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void lookupGroup_async(AMD_IAdmin_lookupGroup __cb, String name,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, name);
    }

    public void lookupGroups_async(AMD_IAdmin_lookupGroups __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void lookupLdapAuthExperimenter_async(
            AMD_IAdmin_lookupLdapAuthExperimenter __cb, long id,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id);
    }

    @SuppressWarnings("unchecked")
    public void lookupLdapAuthExperimenters_async(
            AMD_IAdmin_lookupLdapAuthExperimenters __cb, Current __current)
            throws ServerError {
        IceMapper mapper = new IceMapper(new IceMapper.ReturnMapping() {

            public Object mapReturnValue(IceMapper mapper, Object value)
                    throws UserException {

                if (value == null) {
                    return null;
                }

                List<Map<String, Object>> rv = (List<Map<String, Object>>) value;
                omero.RList list = rlist();
                for (Map<String, Object> item : rv) {
                    if (item == null || item.keySet().size() == 0) {
                        throw new IllegalArgumentException(value.toString());
                    }
                    omero.RMap map = rmap();
                    String key = (String) item.get("dn");
                    omero.RType val = mapper.toRType(item.get("experimenter_id"));
                    map.put(key, val);
                    if (key == null || val == null) {
                        throw new IllegalArgumentException(String.format(
                                "Nulls in map! %s=>%s", key, val));
                    }
                    list.add(map);
                }
                return list;
            }
        });
        callInvokerOnMappedArgs(mapper, __cb, __current);
    }

    public void removeGroups_async(AMD_IAdmin_removeGroups __cb,
            Experimenter user, List<ExperimenterGroup> groups, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user, groups);
    }

    public void reportForgottenPassword_async(
            AMD_IAdmin_reportForgottenPassword __cb, String name, String email,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, name, email);
    }

    public void setDefaultGroup_async(AMD_IAdmin_setDefaultGroup __cb,
            Experimenter user, ExperimenterGroup group, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user, group);
    }

    public void setGroupOwner_async(AMD_IAdmin_setGroupOwner __cb,
            ExperimenterGroup group, Experimenter owner, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group, owner);
    }

    public void unsetGroupOwner_async(AMD_IAdmin_unsetGroupOwner __cb,
            ExperimenterGroup group, Experimenter owner, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group, owner);
    }

    public void synchronizeLoginCache_async(
            AMD_IAdmin_synchronizeLoginCache __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void updateExperimenter_async(AMD_IAdmin_updateExperimenter __cb,
            Experimenter experimenter, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter);
    }

    public void updateExperimenterWithPassword_async(
            AMD_IAdmin_updateExperimenterWithPassword __cb,
            Experimenter experimenter, RString password, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter, password);
    }

    public void updateGroup_async(AMD_IAdmin_updateGroup __cb,
            ExperimenterGroup group, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group);
    }

    public void updateSelf_async(AMD_IAdmin_updateSelf __cb,
            Experimenter experimenter, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, experimenter);
    }

    public void addGroupOwners_async(AMD_IAdmin_addGroupOwners __cb,
            ExperimenterGroup group, List<Experimenter> owners,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group, owners);
    }

    public void removeGroupOwners_async(AMD_IAdmin_removeGroupOwners __cb,
            ExperimenterGroup group, List<Experimenter> owners,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, group, owners);
    }

    public void getLeaderOfGroupIds_async(AMD_IAdmin_getLeaderOfGroupIds __cb,
            Experimenter exp, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        callInvokerOnRawArgs(__cb, __current, exp);        
    }

    public void getMemberOfGroupIds_async(AMD_IAdmin_getMemberOfGroupIds __cb,
            Experimenter exp, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, exp);                
    }

    public void getMyUserPhotos_async(AMD_IAdmin_getMyUserPhotos __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void uploadMyUserPhoto_async(AMD_IAdmin_uploadMyUserPhoto __cb,
            String filename, String format, byte[] data, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, filename, format, data);
    }

    public void moveToCommonSpace_async(AMD_IAdmin_moveToCommonSpace __cb, List<IObject> objects,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, objects);
    }

    @Override
    public void getCurrentAdminPrivileges_async(AMD_IAdmin_getCurrentAdminPrivileges __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    @Override
    public void getAdminPrivileges_async(AMD_IAdmin_getAdminPrivileges __cb,
            Experimenter user,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user);
    }

    @Override
    public void getAdminsWithPrivileges_async(AMD_IAdmin_getAdminsWithPrivileges __cb,
            List<AdminPrivilege> privileges,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, privileges);
    }

    @Override
    public void setAdminPrivileges_async(AMD_IAdmin_setAdminPrivileges __cb,
            Experimenter user, List<AdminPrivilege> privileges,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, user, privileges);
    }
}
