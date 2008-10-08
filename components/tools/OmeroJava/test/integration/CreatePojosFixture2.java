/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ome.system.Login;
import omero.RLong;
import omero.RString;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Category;
import omero.model.CategoryGroup;
import omero.model.CategoryGroupCategoryLink;
import omero.model.CategoryGroupCategoryLinkI;
import omero.model.CategoryGroupI;
import omero.model.CategoryI;
import omero.model.CategoryImageLink;
import omero.model.CategoryImageLinkI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.TextAnnotation;
import omero.model.TextAnnotationI;
import omero.sys.EventContext;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;


/**
 * setUp and tearDown must be called properly to make these work.
 * Copied from testing/src/ome/testing/CreatePojosFixture for ticket1106
 */
public class CreatePojosFixture2 {
    /**
     * creates a new fixture logged in as a newly created user. requires an
     * admin service factory in order to create user and should NOT be used from
     * the server side.
     * @throws ServerError 
     * @throws PermissionDeniedException 
     * @throws CannotCreateSessionException 
     */
    public static CreatePojosFixture2 withNewUser(omero.client root) throws ServerError, CannotCreateSessionException, PermissionDeniedException {
        CreatePojosFixture2 fixture = new CreatePojosFixture2();

        ServiceFactoryPrx sf = root.getSession();
        IAdminPrx rootAdmin = sf.getAdminService();
        String G_NAME = UUID.randomUUID().toString();
        fixture.g = new ExperimenterGroupI();
        fixture.g.setName( new RString( G_NAME ));
        fixture.g = new ExperimenterGroupI(rootAdmin.createGroup(fixture.g),
                false);

        fixture.TESTER = "TESTER-" + UUID.randomUUID().toString();
        fixture.e = new ExperimenterI();
        fixture.e.setOmeName( new RString(fixture.TESTER) );
        fixture.e.setFirstName(new RString("Mr.") );
        fixture.e.setLastName( new RString("Allen") );
        fixture.e = new ExperimenterI(rootAdmin.createUser(fixture.e, G_NAME),
                false);

        java.util.Map<String, String> m = new HashMap<String, String>();
        m.put("omero.user", fixture.TESTER);
        m.put("omero.pass", "ome");
        m.put("omero.group", G_NAME);
        m.put("Ice.Default.Router", root.getProperty("Ice.Default.Router"));
        omero.client client = new omero.client(m);
        ServiceFactoryPrx factory = client.createSession();
        fixture.setServices(factory);

        fixture.init = true;

        return fixture;
    }

    private CreatePojosFixture2() {
    }

    /** requires an admin service factory in order to create user. 
     * @throws ServerError */
    public CreatePojosFixture2(ServiceFactoryPrx factory) throws ServerError {
        setServices(factory);
        EventContext ec = iAdmin.getEventContext();
        e = new ExperimenterI(ec.userId, false);
        g = new ExperimenterGroupI(ec.groupId, false);
        TESTER = ec.userName;
        init = true;
    }

    private void setServices(ServiceFactoryPrx factory) throws ServerError {
        iAdmin = factory.getAdminService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
    }

    protected IAdminPrx iAdmin;

    protected IQueryPrx iQuery;

    protected IUpdatePrx iUpdate;

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
        categorygroups();
        categories();
        cgclinks();
        cilinks();
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

    public void pdi() throws Exception {
        projects();
        datasets();
        images();
        pdlinks();
        dilinks();
    }

    public void projects() throws Exception {
        init();
        pr9090 = project(null, "root project without links");
        pr9091 = project(null, "root project with own annotations");
        pr9092 = project(null, "root project with foreign annotations");
        pu9990 = project(e, "user project without links");
        pu9991 = project(e, "user project with own annotations");
        pu9992 = project(e, "user project with foreign annotations");
        saveAndClear();
    }

    public void datasets() throws Exception {
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
    public void pdlinks() throws Exception {
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

    public void images() throws Exception {
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

    public void dilinks() throws Exception {
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

    public void annotations() throws Exception {
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

    public void categorygroups() throws Exception {
        init();
        cgr9090 = catgroup(null, "root categorygroup without links");
        cgr9091 = catgroup(null, "root categorygroup with own links");
        cgr9092 = catgroup(null, "root categorygroup with foreign links");
        cgu9990 = catgroup(e, "user categorygroup without links");
        cgu9991 = catgroup(e, "user categorygroup with own links");
        cgu9992 = catgroup(e, "user categorygroup with foreign links");
        // cgcpaths
        cgu9980 = catgroup(e, "empty category group");
        cgu9981 = catgroup(e, "categorygroup with one category");
        cgu9982 = catgroup(e, "categorygroup with another category");
        cgu9983 = catgroup(e, "categorygroup with two categories");
        cgu9984 = catgroup(e, "categorygroup with two different categories");
        cgu9985 = catgroup(e, "categorygroup with one empty category");
        saveAndClear();
    }

    public void categories() throws Exception {
        init();
        cr7070 = category(null, "root category without links");
        cr7071 = category(null, "root category with own links");
        cr7072 = category(null, "root category with foreign links");
        cu7770 = category(e, "user category without links");
        cu7771 = category(e, "user category with own links");
        cu7772 = category(e, "user category with foreign links");
        // cgcpaths
        cu7780 = category(e, "user category alone");
        cu7781 = category(e, "user category alone in cg");
        cu7782 = category(e, "user category alone in another cg");
        cu7783 = category(e, "user category paired in a cg I");
        cu7784 = category(e, "user category paired in a cg II");
        cu7785 = category(e, "user category paired in an another cg I");
        cu7786 = category(e, "user category paired in an another cg II");
        cu7787 = category(e, "user category WITH an image");
        cu7788 = category(e, "user category WITHOUT an image");
        saveAndClear();
    }

    public void cgclinks() throws Exception {
        init();
        cgclink(null, cgr9091, cr7071);
        cgclink(null, cgr9091, cr7072);
        cgclink(null, cgr9092, cr7071);
        cgclink(null, cgr9092, cr7072);
        cgclink(null, cgu9991, cu7771);
        cgclink(null, cgu9991, cu7772);
        cgclink(null, cgu9992, cu7771);
        cgclink(null, cgu9992, cu7772);
        // cgcpaths
        cgclink(null, cgu9981, cu7781);
        cgclink(null, cgu9982, cu7782);
        cgclink(null, cgu9983, cu7783);
        cgclink(null, cgu9983, cu7784);
        cgclink(null, cgu9984, cu7785);
        cgclink(null, cgu9984, cu7786);
        cgclink(null, cgu9985, cu7787);
        cgclink(null, cgu9985, cu7788);
        saveAndClear();
    }

    public void cilinks() throws Exception {
        init();
        cilink(null, cr7071, ir5051);
        cilink(e, cr7072, ir5051);
        cilink(null, cr7071, ir5052);
        cilink(e, cr7072, ir5052);
        cilink(null, cu7771, iu5551);
        cilink(e, cu7772, iu5551);
        cilink(null, cu7771, iu5552);
        cilink(e, cu7772, iu5552);
        // cgcpaths
        cilink(e, cu7782, iu5580);
        cilink(e, cu7782, iu5581);
        cilink(e, cu7782, iu5582);
        cilink(e, cu7782, iu5583);
        cilink(e, cu7782, iu5584);
        cilink(e, cu7782, iu5585);
        cilink(e, cu7782, iu5586);
        cilink(e, cu7782, iu5587);

        cilink(e, cu7783, iu5580);
        cilink(e, cu7783, iu5581);
        cilink(e, cu7784, iu5582);
        cilink(e, cu7784, iu5583);
        cilink(e, cu7785, iu5584);
        cilink(e, cu7785, iu5585);
        cilink(e, cu7786, iu5586);
        cilink(e, cu7786, iu5587);
        cilink(e, cu7786, iu5580);
        cilink(e, cu7787, iu5588);
        saveAndClear();
    }

    // ~ Helpers
    // =========================================================================

    protected <T extends IObject> T push(T obj) throws Exception {
        toAdd.add(obj);
        T copy = (T) obj.getClass().newInstance();
        copy.unload();
        needId.add(copy);
        return copy;
    }

    protected void saveAndClear() throws ServerError {
        List<IObject> retVal = iUpdate.saveAndReturnArray(toAdd);
        IObject[] unloaded = needId.toArray(new IObject[needId.size()]);
        for (int i = 0; i < retVal.size(); i++) {
            unloaded[i].setId(retVal.get(i).getId());
        }
        toAdd.clear();
        needId.clear();
    }

    protected Project project(Experimenter owner, String name) throws Exception {
        Project p = new ProjectI();
        p.getDetails().owner = owner;
        p.setName( new RString(name) );
        p = push(p);
        return p;
    }

    protected Dataset dataset(Experimenter owner, String name) throws Exception {
        Dataset d = new DatasetI();
        d.getDetails().owner = owner;
        d.setName( new RString(name) );
        d = push(d);
        return d;
    }

    protected ProjectDatasetLink pdlink(Project prj, Dataset ds) throws Exception {
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.link(prj, ds);
        link = push(link);
        return link;
    }

    protected Image image(Experimenter e, String name) throws Exception {
        Image i = new ImageI();
        i.getDetails().owner = e;
        i.setName( new RString(name) );
        i = push(i);
        return i;
    }

    protected DatasetImageLink dilink(Experimenter user, Dataset ds, Image i) throws Exception {
        DatasetImageLink link = new DatasetImageLinkI();
        link.link(ds, i);
        link.getDetails().owner = user;
        link = push(link);
        return link;
    }

    protected DatasetAnnotationLink datasetann(Experimenter user, Dataset d,
            String name) throws Exception {
        TextAnnotation dann = new TextAnnotationI();
        dann.setNs( new RString(name) );
        dann.getDetails().owner = user;
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.link(d.proxy(), dann);
        link = push(link);
        return link;
    }

    protected ImageAnnotationLink imageann(Experimenter user, Image i,
            String name) throws Exception {
        TextAnnotation iann = new TextAnnotationI();
        iann.setNs( new RString(name) );
        iann.getDetails().owner = user;
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(i.proxy(), iann);
        link = push(link);
        return link;
    }

    protected CategoryGroup catgroup(Experimenter owner, String name) throws Exception {
        CategoryGroup cg = new CategoryGroupI();
        cg.getDetails().owner = owner;
        cg.setName( new RString(name) );
        cg = push(cg);
        return cg;
    }

    protected Category category(Experimenter owner, String name) throws Exception {
        Category c = new CategoryI();
        c.getDetails().owner = owner;
        c.setName( new RString(name) );
        c = push(c);
        return c;
    }

    protected CategoryGroupCategoryLink cgclink(Experimenter user,
            CategoryGroup cg, Category c) throws Exception {
        CategoryGroupCategoryLink link = new CategoryGroupCategoryLinkI();
        link.link(cg, c);
        link.getDetails().owner = user;
        link = push(link);
        return link;
    }

    protected CategoryImageLink cilink(Experimenter user, Category c, Image i) throws Exception {
        CategoryImageLink link = new CategoryImageLinkI();
        link.link(c, i);
        link.getDetails().owner = user;
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

    public CategoryGroup cgr9090, cgr9091, cgr9092, cgu9990, cgu9991, cgu9992,
            cgu9980, cgu9981, cgu9982, cgu9983, cgu9984, cgu9985;

    public Category cr7070, cr7071, cr7072, cu7770, cu7771, cu7772, cu7780,
            cu7781, cu7782, cu7783, cu7784, cu7785, cu7786, cu7787, cu7788;

    // }

    public List<RLong> asIdList(IObject... iobjs) {
        List<RLong> list = new ArrayList<RLong>();
        for (IObject i : iobjs) {
            list.add(i.getId());
        }
        return list;
    }
}
