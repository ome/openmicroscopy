/*
 * ome.util.tasks.admin.AddGroupTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks.admin;

// Java imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IAdmin;
import ome.api.IQuery;
import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.ServiceFactory;
import ome.util.CBlock;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.ListUsersAndGroupsTask.Keys.*;

/**
 * {@link SimpleTask} which prints {@link Experimenter} and
 * {@link ExperimenterGroup} information to standard out. Understands the
 * parameters:
 * <ul>
 * <li>users</li>
 * <li>groups</li>
 * </ul>
 * 
 * For each parameter, if the value is non-null, the output will get sent to a
 * {@link File} of that name <em>with the exception of the values</em>: out
 * and err. For "out" the output will be sent to {@link System#out} and for
 * "err" the output will be sent to {@link System#err}.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SimpleTask
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public class ListUsersAndGroupsTask extends SimpleTask {

    /**
     * If the value of a property equals "out", output will be sent to
     * {@link System#out}
     */
    public final static String OUT = "out";

    /**
     * If the value of a property equals "err", output will be sent to
     * {@link System#err}
     */
    public final static String ERR = "err";

    /**
     * Enumeration of the string values which will be used directly by
     * {@link ListUsersAndGroupsTask}.
     */
    public enum Keys {
        users, groups
    }

    /** Delegates to super */
    public ListUsersAndGroupsTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the actual {@link ExperimenterGroup} creation.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IQuery query = getServiceFactory().getQueryService();

        String u_format = "%s:%d:%s:%s:%s\n";

        String U = enumValue(users);
        PrintWriter u_pw = getWriter(U);
        if (u_pw != null) {
            List<Experimenter> exps = query.findAllByQuery(
                    "select e from Experimenter e "
                            + "left outer join fetch e.groupExperimenterMap m "
                            + "left outer join fetch m.parent g", null);
            for (Experimenter e : exps) {
                u_pw.format(u_format, e.getOmeName(), e.getId(), e
                        .getFirstName(), e.getLastName(), groupNames(e));
            }
            u_pw.flush();
            // TODO Need to close
        }

        String g_format = "%s:%d:%s:%s:%s\n";

        String G = enumValue(groups);
        PrintWriter g_pw = getWriter(G);

        if (g_pw != null) {
            List<ExperimenterGroup> grps = query.findAllByQuery(
                    "select g from ExperimenterGroup g "
                            + "left outer join fetch g.groupExperimenterMap m "
                            + "left outer join fetch m.child e "
                            + "left outer join fetch g.details.owner", null);
            for (ExperimenterGroup g : grps) {
                String desc = (null == g.getDescription() ? "" : g
                        .getDescription());
                Experimenter e = g.getDetails().getOwner();
                String owner = (null == e ? "null" : e.getOmeName());
                g_pw.format(g_format, g.getName(), g.getId(), desc, owner,
                        userNames(g));
            }
            g_pw.flush();
            // TODO Need to close
        }

    }

    // ~ Helpers
    // =========================================================================

    private PrintWriter getWriter(String name) {

        if (null == name)
            return null;

        PrintWriter pw;
        if (OUT.equals(name)) {
            pw = new PrintWriter(System.out);
        } else if (ERR.equals(name)) {
            pw = new PrintWriter(System.err);
        } else {
            File file = new File(name);
            FileWriter fw;
            try {
                fw = new FileWriter(file, true);
                pw = new PrintWriter(fw);
            } catch (IOException e) {
                System.out.println("Cannot create file " + name);
                pw = null;
            }
        }
        return pw;
    }

    private List<String> userNames(ExperimenterGroup g) {
        return g.collectGroupExperimenterMap(new CBlock<String>() {
            public String call(IObject object) {
                GroupExperimenterMap m = (GroupExperimenterMap) object;
                StringBuilder name = new StringBuilder(m.child().getOmeName());
                if (Boolean.TRUE.equals(m.getDefaultGroupLink())) {
                    name.append(" (=default)");
                }
                return name.toString();
            }
        });
    }

    private List<String> groupNames(Experimenter e) {
        return e.collectGroupExperimenterMap(new CBlock<String>() {
            public String call(IObject object) {
                GroupExperimenterMap m = (GroupExperimenterMap) object;
                StringBuilder name = new StringBuilder(m.parent().getName());
                if (Boolean.TRUE.equals(m.getDefaultGroupLink())) {
                    name.append(" (=default)");
                }
                return name.toString();
            }
        });
    }
}
