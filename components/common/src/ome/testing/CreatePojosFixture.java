package ome.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;


/** 
 * setUp and tearDown must be called properly to make these work.
 * @author josh
 *
 */
public class CreatePojosFixture
{
	public CreatePojosFixture( ServiceFactory factory )
	{
		iAdmin = factory.getAdminService();
		iQuery = factory.getQueryService();
		iUpdate = factory.getUpdateService();
	}
	
	protected IAdmin iAdmin;
	
	protected IQuery iQuery;
	
	protected IUpdate iUpdate;
	
	protected boolean init = false;

	protected List<IObject> 
		toAdd = new ArrayList<IObject>(),
		needId = new ArrayList<IObject>();

	public void createAllPojos() throws Exception
	{		
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

	public void deleteAllPojos() throws Exception
	{
		for (int i = toAdd.size()-1; i >= 0; i--) {
			iUpdate.deleteObject(toAdd.get(i));
		}
		iAdmin.deleteExperimenter(e);
	}
	
	public void init() {
		if (!init)
		{
//		ExperimenterGroup group = new ExperimenterGroup();
//		group.setName("foo");
//		group = push(iAdmin.createGroup(group));
//		
			e = new Experimenter();
			e.setOmeName(UUID.randomUUID().toString());
			e.setFirstName("Mr.");
			e.setLastName("Allen");
			e = new Experimenter( iAdmin.createUser(e), false );
			init = true;
		}
	}
	
	public void pdi()
	{
		projects();
		datasets();
		images();
		pdlinks();
		dilinks();
	}
	
	public void projects()
	{
		init();
		pr9090 = project(null,"root project without links");
		pr9091 = project(null,"root project with own annotations");
		pr9092 = project(null,"root project with foreign annotations");
		pu9990 = project(e,"user project without links");
		pu9991 = project(e,"user project with own annotations");
		pu9992 = project(e,"user project with foreign annotations");
		saveAndClear();
	}
	
	public void datasets()
	{
		init();
		dr7070 = dataset(null,"root dataset without links");
		dr7071 = dataset(null,"root dataset with own annotations");
		dr7072 = dataset(null,"root dataset with foreign annotations");
		du7770 = dataset(e,"user dataset without links");
		du7771 = dataset(e,"user dataset with own annotations");
		du7772 = dataset(e,"user dataset with foreign annotations");
		saveAndClear();
	}

	// TODO we aren't passing in Experimenter here
	public void pdlinks()
	{
		init();
		pdlink(pr9091,dr7071);
		pdlink(pr9092,dr7071);
		pdlink(pr9091,dr7072);
		pdlink(pr9092,dr7072);
		
		pdlink(pu9991,du7771);
		pdlink(pu9992,du7771);
		pdlink(pu9991,du7772);
		pdlink(pu9992,du7772);
		saveAndClear();
	}

	public void images()
	{
		init();
		ir5050 = image(null,"");
		ir5051 = image(null,"");
		ir5052 = image(null,"");
		iu5550 = image(e,"");
		iu5551 = image(e,"");
		iu5552 = image(e,"");
		// cgcpaths
		iu5580 = image(e,"");
		iu5581 = image(e,"");
		iu5582 = image(e,"");
		iu5583 = image(e,"");
		iu5584 = image(e,"");
		iu5585 = image(e,"");
		iu5586 = image(e,"");
		iu5587 = image(e,"");
		iu5588 = image(e,"");
		saveAndClear();
	}

	public void dilinks()
	{
		init();
		dilink(null,dr7071,ir5051);
		dilink(null,dr7071,ir5052);
		dilink(e,dr7072,ir5051);
		dilink(e,dr7072,ir5052);
		
		dilink(null,du7771,iu5551);
		dilink(null,du7771,iu5552);
		dilink(e,du7772,iu5551);
		dilink(e,du7772,iu5552);
		saveAndClear();
	}

	public void annotations()
	{
		init();
		datasetann(null,dr7071,"roots annotation");
		datasetann(e,dr7072,"users annotation");
		datasetann(null,du7771,"roots annotation");
		datasetann(e,du7772,"users annotation");

		imageann(null,ir5051,"roots annotation");
		imageann(e,ir5052,"users annotation");
		imageann(null,iu5551,"roots annotation");
		imageann(e,iu5552,"users annotation");
		saveAndClear();
	}

	public void categorygroups()
	{
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

	public void categories()
	{
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
	
	public void cgclinks()
	{
		init();
		cgclink(null,cgr9091,cr7071);
		cgclink(null,cgr9091,cr7072);
		cgclink(null,cgr9092,cr7071);
		cgclink(null,cgr9092,cr7072);
		cgclink(null,cgu9991,cu7771);
		cgclink(null,cgu9991,cu7772);
		cgclink(null,cgu9992,cu7771);
		cgclink(null,cgu9992,cu7772);
		//cgcpaths
		cgclink(null,cgu9981,cu7781);
		cgclink(null,cgu9982,cu7782);
		cgclink(null,cgu9983,cu7783);
		cgclink(null,cgu9983,cu7784);
		cgclink(null,cgu9984,cu7785);
		cgclink(null,cgu9984,cu7786);
		cgclink(null,cgu9985,cu7787);
		cgclink(null,cgu9985,cu7788);
		saveAndClear();
	}
	
	public void cilinks()
	{
		init();
		cilink(null,cr7071,ir5051);
		cilink(e,cr7072,ir5051);
		cilink(null,cr7071,ir5052);
		cilink(e,cr7072,ir5052);
		cilink(null,cu7771,iu5551);
		cilink(e,cu7772,iu5551);
		cilink(null,cu7771,iu5552);
		cilink(e,cu7772,iu5552);
		// cgcpaths
		cilink(e,cu7782,iu5580);
		cilink(e,cu7782,iu5581);
		cilink(e,cu7782,iu5582);
		cilink(e,cu7782,iu5583);
		cilink(e,cu7782,iu5584);
		cilink(e,cu7782,iu5585);
		cilink(e,cu7782,iu5586);
		cilink(e,cu7782,iu5587);
		
		cilink(e,cu7783,iu5580);
		cilink(e,cu7783,iu5581);
		cilink(e,cu7784,iu5582);
		cilink(e,cu7784,iu5583);
		cilink(e,cu7785,iu5584);
		cilink(e,cu7785,iu5585);
		cilink(e,cu7786,iu5586);
		cilink(e,cu7786,iu5587);
		cilink(e,cu7786,iu5580);
		cilink(e,cu7787,iu5588);
		saveAndClear();
	}
	
	// ~ Helpers
	// =========================================================================
	
	protected <T extends IObject> T push( T obj )
	{
		toAdd.add(obj);
		T copy = new ShallowCopy().copy(obj);
		copy.unload();
		needId.add(copy);
		return copy;
	}
	
	protected void saveAndClear()
	{
		IObject[] retVal = iUpdate.saveAndReturnArray( 
				toAdd.toArray(new IObject[toAdd.size()]));
		IObject[] unloaded =
				needId.toArray(new IObject[needId.size()]);
		for (int i = 0; i < retVal.length; i++) {
				unloaded[i].setId( retVal[i].getId() );
		}
		toAdd.clear();
		needId.clear();
	}
	
	protected Project project(Experimenter owner, String name)
	{
		Project p = new Project();
		p.getDetails().setOwner(owner);
		p.setName(name);
		p = push(p);
		return p;
	}
	
	protected Dataset dataset(Experimenter owner, String name)
	{
		Dataset d = new Dataset();
		d.getDetails().setOwner(owner);
		d.setName(name);
		d = push(d);
		return d;
	}
	
	protected ProjectDatasetLink pdlink(Project prj, Dataset ds)
	{
		ProjectDatasetLink link = new ProjectDatasetLink();
		link.link(prj, ds);
		link = push(link);
		return link;
	}
	
	protected Image image(Experimenter e, String name)
	{
		Image i = new Image();
		i.getDetails().setOwner(e);
		i.setName(name);
		i = push(i);
		return i;
	}
	
	protected DatasetImageLink dilink(Experimenter user, Dataset ds, Image i)
	{
		DatasetImageLink link = new DatasetImageLink();
		link.link(ds, i);
		link.getDetails().setOwner(user);
		link = push(link);
		return link;
	}

	protected DatasetAnnotation datasetann(Experimenter user, Dataset d, String name)
	{
		DatasetAnnotation dann = new DatasetAnnotation();
		dann.setDataset(d);
		dann.setContent(name);
		dann.getDetails().setOwner(user);
		dann = push(dann);
		return dann;
	}

	protected ImageAnnotation imageann(Experimenter user, Image i, String name)
	{
		ImageAnnotation iann = new ImageAnnotation();
		iann.setImage(i);
		iann.setContent(name);
		iann.getDetails().setOwner(user);
		iann = push(iann);
		return iann;
	}
	
	protected CategoryGroup catgroup(Experimenter owner, String name)
	{
		CategoryGroup cg = new CategoryGroup();
		cg.getDetails().setOwner(owner);
		cg.setName(name);
		cg = push(cg);
		return cg;
	}

	protected Category category(Experimenter owner, String name)
	{
		Category c = new Category();
		c.getDetails().setOwner(owner);
		c.setName(name);
		c = push(c);
		return c;
	}
	
	protected CategoryGroupCategoryLink cgclink(Experimenter user, CategoryGroup cg, Category c)
	{
		CategoryGroupCategoryLink link = new CategoryGroupCategoryLink();
		link.link(cg, c);
		link.getDetails().setOwner(user);
		link = push(link);
		return link;
	}
	
	protected CategoryImageLink cilink(Experimenter user, Category c, Image i)
	{
		CategoryImageLink link = new CategoryImageLink();
		link.link(c,i);
		link.getDetails().setOwner(user);
		link = push(link);
		return link;
	}
	
	//static class Data {
		public Experimenter e;
		public Project pr9090, pr9091, pr9092, pu9990, pu9991, pu9992;
		public Dataset dr7070, dr7071, dr7072, du7770, du7771, du7772;
		public Image
			ir5050, ir5051, ir5052, iu5550, iu5551, iu5552,
			iu5580, iu5581, iu5582, iu5583, iu5584, iu5585,
			iu5586, iu5587, iu5588;
		public CategoryGroup
			cgr9090, cgr9091, cgr9092, cgu9990, cgu9991, cgu9992,
			cgu9980, cgu9981, cgu9982, cgu9983, cgu9984, cgu9985;
		public Category
			cr7070, cr7071, cr7072, cu7770, cu7771, cu7772,
			cu7780, cu7781, cu7782, cu7783, cu7784, cu7785,
			cu7786, cu7787, cu7788;
	//}
	
	public List<Long> asIdList(IObject...iobjs)
	{
		List<Long> list = new ArrayList<Long>();
		for (IObject i : iobjs) {
			list.add( i.getId() );
		}
		return list;
	}
}
