package ome.formats.testclient;

import java.util.List;
import java.util.Map;

import ome.api.IAdmin;
import ome.conditions.AuthenticationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.system.Roles;


public class TestAdminService implements IAdmin
{

    public void addGroups(Experimenter arg0, ExperimenterGroup... arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changeExpiredCredentials(String arg0, String arg1, String arg2)
            throws AuthenticationException
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changeGroup(IObject arg0, String arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changeOwner(IObject arg0, String arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changePassword(String arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changePermissions(IObject arg0, Permissions arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void changeUserPassword(String arg0, String arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public Experimenter[] containedExperimenters(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ExperimenterGroup[] containedGroups(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public long createExperimenter(Experimenter arg0, ExperimenterGroup arg1,
            ExperimenterGroup... arg2)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public long createExperimenterWithPassword(Experimenter arg0, String arg1,
            ExperimenterGroup arg2, ExperimenterGroup... arg3)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public long createGroup(ExperimenterGroup arg0)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public long createSystemUser(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public long createUser(Experimenter arg0, String arg1)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public void deleteExperimenter(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void deleteGroup(ExperimenterGroup arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public ExperimenterGroup getDefaultGroup(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public EventContext getEventContext()
    {
        return null;
    }

    public Experimenter getExperimenter(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ExperimenterGroup getGroup(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Roles getSecurityRoles()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Experimenter lookupExperimenter(String arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public List<Experimenter> lookupExperimenters()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ExperimenterGroup lookupGroup(String arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public List<ExperimenterGroup> lookupGroups()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String lookupLdapAuthExperimenter(long arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public List<Map<String, Object>> lookupLdapAuthExperimenters()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void removeGroups(Experimenter arg0, ExperimenterGroup... arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void reportForgottenPassword(String arg0, String arg1)
            throws AuthenticationException
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setDefaultGroup(Experimenter arg0, ExperimenterGroup arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setGroupOwner(ExperimenterGroup arg0, Experimenter arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void synchronizeLoginCache()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean[] unlock(IObject... arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void updateExperimenter(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void updateExperimenterWithPassword(Experimenter arg0, String arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void updateGroup(ExperimenterGroup arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void updateSelf(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

}
