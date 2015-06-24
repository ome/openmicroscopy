/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.testing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;

/**
 * setUp and tearDown must be called properly to make these work.
 * 
 * @author josh
 * 
 */
public class CreatePojosFixture {
    /**
     * creates a new fixture logged in as a newly created user. requires an
     * admin service factory in order to create user and should NOT be used from
     * the server side.
     */
    public static CreatePojosFixture withNewUser(ServiceFactory sf) {
        CreatePojosFixture fixture = new CreatePojosFixture();

        IAdmin rootAdmin = sf.getAdminService();
        String G_NAME = UUID.randomUUID().toString();
        fixture.g = new ExperimenterGroup();
        fixture.g.setName(G_NAME);
        fixture.g.setLdap(false);
        fixture.g = new ExperimenterGroup(rootAdmin.createGroup(fixture.g),
                false);

        fixture.TESTER = "TESTER-" + UUID.randomUUID().toString();
        fixture.e = new Experimenter();
        fixture.e.setOmeName(fixture.TESTER);
        fixture.e.setFirstName("Mr.");
        fixture.e.setLastName("Allen");
        fixture.e.setLdap(false);
        fixture.e = new Experimenter(rootAdmin.createUser(fixture.e, G_NAME),
                false);

        Login testLogin = new Login(fixture.TESTER, "ome", G_NAME, "Test");
        ServiceFactory factory = new ServiceFactory(testLogin);
        fixture.setServices(factory);

        fixture.init = true;

        return fixture;
    }

    private CreatePojosFixture() {
    }

    /** requires an admin service factory in order to create user. */
    public CreatePojosFixture(ServiceFactory factory) {
        setServices(factory);
        EventContext ec = iAdmin.getEventContext();
        e = new Experimenter(ec.getCurrentUserId(), false);
        g = new ExperimenterGroup(ec.getCurrentGroupId(), false);
        TESTER = ec.getCurrentUserName();
        init = true;
    }

    private void setServices(ServiceFactory factory) {
        iAdmin = factory.getAdminService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
    }

    protected IAdmin iAdmin;

    protected IQuery iQuery;

    protected IUpdate iUpdate;

    protected boolean init = false;

    protected List<IObject> toAdd = new ArrayList<IObject>(),
            needId = new ArrayList<IObject>();

    public void createAllPojos() throws Exception {
        init();
        projects();
        datasets();
        pdlinks();
        images();
        dilinks();
        annotations();
    }

    public void deleteAllPojos() throws Exception {
        for (int i = toAdd.size() - 1; i >= 0; i--) {
            iUpdate.deleteObject(toAdd.get(i));
        }
        iAdmin.deleteExperimenter(e);
        // TODO iAdmin.deleteGroup(g);
    }

    public void init() {
        if (!init) {
            // moved to ctor
        }
    }

    public void pdi() {
        projects();
        datasets();
        images();
        pdlinks();
        dilinks();
    }

    public void projects() {
        init();
        pr9090 = project(null, "root project without links");
        pr9091 = project(null, "root project with own annotations");
        pr9092 = project(null, "root project with foreign annotations");
        pu9990 = project(e, "user project without links");
        pu9991 = project(e, "user project with own annotations");
        pu9992 = project(e, "user project with foreign annotations");
        saveAndClear();
    }

    public void datasets() {
        init();
        dr7070 = dataset(null, "root dataset without links");
        dr7071 = dataset(null, "root dataset with own annotations");
        dr7072 = dataset(null, "root dataset with foreign annotations");
        du7770 = dataset(e, "user dataset without links");
        du7771 = dataset(e, "user dataset with own annotations");
        du7772 = dataset(e, "user dataset with foreign annotations");
        saveAndClear();
    }

    // TODO we aren't passing in Experimenter here
    public void pdlinks() {
        init();
        pdlink(pr9091, dr7071);
        pdlink(pr9092, dr7071);
        pdlink(pr9091, dr7072);
        pdlink(pr9092, dr7072);

        pdlink(pu9991, du7771);
        pdlink(pu9992, du7771);
        pdlink(pu9991, du7772);
        pdlink(pu9992, du7772);
        saveAndClear();
    }

    public void images() {
        init();
        ir5050 = image(null, "");
        ir5051 = image(null, "");
        ir5052 = image(null, "");
        iu5550 = image(e, "");
        iu5551 = image(e, "");
        iu5552 = image(e, "");
        // cgcpaths
        iu5580 = image(e, "");
        iu5581 = image(e, "");
        iu5582 = image(e, "");
        iu5583 = image(e, "");
        iu5584 = image(e, "");
        iu5585 = image(e, "");
        iu5586 = image(e, "");
        iu5587 = image(e, "");
        iu5588 = image(e, "");
        saveAndClear();
    }

    public void dilinks() {
        init();
        dilink(null, dr7071, ir5051);
        dilink(null, dr7071, ir5052);
        dilink(e, dr7072, ir5051);
        dilink(e, dr7072, ir5052);

        dilink(null, du7771, iu5551);
        dilink(null, du7771, iu5552);
        dilink(e, du7772, iu5551);
        dilink(e, du7772, iu5552);
        saveAndClear();
    }

    public void annotations() {
        init();
        datasetann(null, dr7071, "roots annotation");
        datasetann(e, dr7072, "users annotation");
        datasetann(null, du7771, "roots annotation");
        datasetann(e, du7772, "users annotation");

        imageann(null, ir5051, "roots annotation");
        imageann(e, ir5052, "users annotation");
        imageann(null, iu5551, "roots annotation");
        imageann(e, iu5552, "users annotation");
        saveAndClear();
    }


    // ~ Helpers
    // =========================================================================

    protected <T extends IObject> T push(T obj) {
        toAdd.add(obj);
        T copy = new ShallowCopy().copy(obj);
        copy.unload();
        needId.add(copy);
        return copy;
    }

    protected void saveAndClear() {
        IObject[] retVal = iUpdate.saveAndReturnArray(toAdd
                .toArray(new IObject[toAdd.size()]));
        IObject[] unloaded = needId.toArray(new IObject[needId.size()]);
        for (int i = 0; i < retVal.length; i++) {
            unloaded[i].setId(retVal[i].getId());
        }
        toAdd.clear();
        needId.clear();
    }

    protected Project project(Experimenter owner, String name) {
        Project p = new Project();
        p.getDetails().setOwner(owner);
        p.setName(name);
        p = push(p);
        return p;
    }

    protected Dataset dataset(Experimenter owner, String name) {
        Dataset d = new Dataset();
        d.getDetails().setOwner(owner);
        d.setName(name);
        d = push(d);
        return d;
    }

    protected ProjectDatasetLink pdlink(Project prj, Dataset ds) {
        ProjectDatasetLink link = new ProjectDatasetLink();
        link.link(prj, ds);
        link = push(link);
        return link;
    }

    protected Image image(Experimenter e, String name) {
        Image i = new Image();
        i.getDetails().setOwner(e);
        i.setName(name);
        i = push(i);
        return i;
    }

    protected DatasetImageLink dilink(Experimenter user, Dataset ds, Image i) {
        DatasetImageLink link = new DatasetImageLink();
        link.link(ds, i);
        link.getDetails().setOwner(user);
        link = push(link);
        return link;
    }

    protected DatasetAnnotationLink datasetann(Experimenter user, Dataset d,
            String name) {
        TextAnnotation dann = new TagAnnotation();
        dann.setNs(name);
        dann.getDetails().setOwner(user);
        DatasetAnnotationLink link = new DatasetAnnotationLink();
        link.link(d.proxy(), dann);
        link = push(link);
        return link;
    }

    protected ImageAnnotationLink imageann(Experimenter user, Image i,
            String name) {
        TextAnnotation iann = new TagAnnotation();
        iann.setNs(name);
        iann.getDetails().setOwner(user);
        ImageAnnotationLink link = new ImageAnnotationLink();
        link.link(i.proxy(), iann);
        link = push(link);
        return link;
    }

    // static class Data {
    public String TESTER;

    public Experimenter e;

    public ExperimenterGroup g;

    public Project pr9090, pr9091, pr9092, pu9990, pu9991, pu9992;

    public Dataset dr7070, dr7071, dr7072, du7770, du7771, du7772;

    public Image ir5050, ir5051, ir5052, iu5550, iu5551, iu5552, iu5580,
            iu5581, iu5582, iu5583, iu5584, iu5585, iu5586, iu5587, iu5588;

    public List<Long> asIdList(IObject... iobjs) {
        List<Long> list = new ArrayList<Long>();
        for (IObject i : iobjs) {
            list.add(i.getId());
        }
        return list;
    }
}
