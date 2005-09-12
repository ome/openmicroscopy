package ome;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.TransactionManager;

import ome.model.core.*;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ome.dynamic.BuildRunner;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class ModelTest extends AbstractDependencyInjectionSpringContextTests {

	static {
		BuildRunner.load("build.xml");
		//BuildRunner.run("build.xml"); 
		//BuildRunner.launch("build.xml");
	}
	
	PlatformTransactionManager tx;
	
	TransactionTemplate tt;
	
	HibernateTemplate ht;
	
	SessionFactory s;
	
	public void setSessionFactory(SessionFactory sessions){
		s=sessions;
	}

	public void setTransactionManager(PlatformTransactionManager mgr){
		this.tx=mgr;
	}
	
	@Override
	protected void onSetUp() throws Exception {
		ht = new HibernateTemplate(s);
		tt = new TransactionTemplate(tx);
		
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
	
	public void testMyType(){
		Eg e = new Eg();
		e.setList(new int[]{1,2,3});
		persist(e);
		
		Eg g = (Eg) ht.get(Eg.class,1);
		int[] ids = g.getList();
		ids[0]=9;
		persist(g);
	}
	
	public void testEvents(){
		Event e = new Event();
		EventDiff d1 = new EventDiff();
		EventDiff d2 = new EventDiff();
		Set s = new HashSet();
		EventLog l = new EventLog();
		
		e.setName("test");
		
		l.setEvent(e);
		l.setExperimenter(1);
		l.setDiffs(s);
		
		s.add(d1);
		s.add(d2);
		
		d1.setAction("CREATE");
		d1.setType("Image");
		d1.setIds(new int[]{1,2,3});
		d1.setEventLog(l);
		
		d2.setAction("EDIT");
		d2.setType("Dataset");
		d2.setIds(new int[]{4,5,6});
		d2.setEventLog(l);
		
		persist(e,l,d1,d2);
		
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
		
		persist(img, ds, link);
		
	}
	
	public void testVersionShouldIncreate(){
		Image i = (Image) ht.find("from Image i where i.version is not null").get(0);
		i.setName("new name for version"+i.getVersion());
		ht.update(i);
		Image i2 = (Image) ht.get(Image.class,i.getId());
		assertTrue(i.getVersion()!=i2.getVersion());
	}
	
	public void testCreateHyperCubeRoi(){
		Roi5D r5 = new Roi5D();
		Roi4DChannelRange r4 = new Roi4DChannelRange();
		Roi3DTimeRange r3 = new Roi3DTimeRange();
		Roi2DZRange r2 = new Roi2DZRange();
		ShapeSquare s = new ShapeSquare();

		// Channel 1-2 are a copy of this Roi4D (the union of the attached roi3d stacks)
		r4.setRoi5d(r5);
		r4.setMinC(1);r4.setMaxC(2);
		
		// Time points 0-100 are a copy of this Roi3D (the union of the attached planes)
		r3.setRoi4d(r4);
		r3.setMinT(0); r3.setMaxT(100);
		
		// Z-planes 0-5 are a copy of this this Roi2D (which points to a single shape)
		r2.setRoi3d(r3);
		r2.setShape(s);
		r2.setMinZ(0); r2.setMaxZ(5);
		
		// The shape is a square from (1,2) to (10,11)
		s.setX0(1);s.setX1(10);
		s.setY0(2);s.setY1(11);
		
		persist(r5,r4,r3,r2,s);
		
	}
	
	public void testCreateIndexedRoi(){
		Map m = new Map();
		Pixel p = new Pixel();
		RoiSet set = new RoiSet();
		Roi5D r5 = new Roi5D();
		Roi4DChannelRange r4 = new Roi4DChannelRange();
		//
		Roi3DTimeIndex r3_1 = new Roi3DTimeIndex();
		Roi3DTimeIndex r3_2 = new Roi3DTimeIndex();
		Roi3DTimeIndex r3_3 = new Roi3DTimeIndex();
		Roi3DTimeIndex r3_4 = new Roi3DTimeIndex();
		
		Roi2DZIndex r2_1 = new Roi2DZIndex();
		Roi2DZIndex r2_2 = new Roi2DZIndex();
		Roi2DZIndex r2_3 = new Roi2DZIndex();
		Roi2DZRange r2_4 = new Roi2DZRange();
		
		ShapeSquare s_1 = new ShapeSquare();
		ShapeEllipse s_2 = new ShapeEllipse();
		// easy square
		s_1.setX0(1);
		s_1.setX1(10);
		s_1.setY0(3);
		s_1.setY1(15);
		// Using a traditional def. of ellipses, your mileage may vary
		s_2.setP1x(4);s_2.setP2x(10);
		s_2.setP1y(5);s_2.setP2y(5);
		s_2.setLength(8);
		// We'll reuse these because it's a pain to create hundres.

		// Mappings
		m.setRoi5d(r5);
		m.setRoiSet(set);
		r5.setPixel(p);
		
		// Channel 1-3 are composed of r4 (this shows how you can mix-n-match indexes and ranges)
		r4.setRoi5d(r5);
		r4.setRoi5d(r5);
		r4.setMinC(1);r4.setMaxC(3);
		
		// Times 1-4 make up r4
		r3_1.setRoi4d(r4);
		r3_1.setIndexT(1);
		r3_2.setRoi4d(r4);
		r3_2.setIndexT(2);
		r3_3.setRoi4d(r4);
		r3_3.setIndexT(3);
		r3_4.setRoi4d(r4);
		r3_4.setIndexT(4);

		// r3_1 has two z planes 
		r2_1.setRoi3d(r3_1);
		r2_1.setIndexZ(1);
		r2_1.setShape(s_1);
		r2_2.setRoi3d(r3_1);
		r2_1.setIndexZ(2);
		r2_1.setShape(s_2);

		// r3_2 is a union of an index and a range
		r2_3.setRoi3d(r3_2);
		r2_3.setShape(s_1);
		r2_3.setIndexZ(0);
		r2_4.setRoi3d(r3_2);
		r2_4.setMinZ(1);
		r2_4.setMinZ(10);
		// here we need to do the same thing for r3_3 and r3_4
		
		persist(m,p,set,r5,r4,r3_1,r3_2,r3_3,r3_4,r2_1,r2_2,r2_3,r2_4,s_1,s_2);

	}
	
	List getAllRoi5Ds(){
		return (List) ht.execute(new HibernateCallback(){
			public Object doInHibernate(Session session) 
				throws org.hibernate.HibernateException ,java.sql.SQLException {
				
				List l = session.createQuery(
						"from Roi5D r5 " +
						"left outer join r5.roi4ds as r4 " +
						"left outer join r4.roi3ds as r3 " +
						"left outer join r3.roi2ds as r2 " +
						"left outer join r2.shape as s").list();
				
				return l;
			}
		});
		
	}

	void persist(final Object... args) {
		tt.execute(new TransactionCallbackWithoutResult(){
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ht.execute(new HibernateCallback(){
					public Object doInHibernate(Session session){
						
						for (Object arg: args) {
							session.saveOrUpdate(arg);
						}
						
						return null;
					}
				});
			}
		});
	}

	// http://www.javaranch.com/newsletter/200404/Lucene.html
	public void testSearch() throws IOException, ParseException{
        IndexSearcher is = new IndexSearcher("/tmp/j");
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("class", analyzer);
        Query query = parser.parse("Roi");
        Hits hits = is.search(query);
        for (int i=0; i<hits.length(); i++) {
            Document doc = hits.doc(i);
            // display the articles that were found to the user
        }
        is.close();
	}
	
}
