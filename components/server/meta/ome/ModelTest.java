package ome;

import ome.model.Dataset;
import ome.model.Image;
import ome.model.ImageDatasetLink;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmicroscopy.omero.logic.dynamic.BuildRunner;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class ModelTest extends AbstractDependencyInjectionSpringContextTests {

	static {
		BuildRunner.load("build.xml");
		//BuildRunner.run("build.xml");
		//BuildRunner.launch("build.xml");
	}
	
	HibernateTemplate ht;
	
	SessionFactory s;
	
	public void setSessionFactory(SessionFactory sessions){
		s=sessions;
	}
	
	@Override
	protected void onSetUp() throws Exception {
		ht = new HibernateTemplate(s);
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[]{
				"hibernate.xml"
		};
	}

	public void testSessionFactoryExists(){
		assertNotNull(s);
	}
	
	public void testRetrieveAll(){
		ht.find("from java.lang.Object");
	}
	
	public void testCreatingOneLink(){
		final Image img = new Image();
		final Dataset ds = new Dataset();
		final ImageDatasetLink link = new ImageDatasetLink();
		
		img.setName("george");
		ds.setName("fred");
		
		link.setImage(img);
		link.setDataset(ds);
		
		ht.execute(new HibernateCallback(){
			public Object doInHibernate(Session session){
				
				session.save(img);
				session.save(ds);
				session.save(link);
				
				return null;
			}
		});
		
	}
	
	public void testVersionShouldIncreate(){
		Image i = (Image) ht.find("from Image i where i.version is not null").get(0);
		i.setName("new name for version"+i.getVersion());
		ht.update(i);
		Image i2 = (Image) ht.get(Image.class,i.getImageId());
		assertTrue(i.getVersion()!=i2.getVersion());
	}
	
}
