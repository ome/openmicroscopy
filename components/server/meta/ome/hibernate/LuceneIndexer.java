package ome.hibernate;

import java.io.File;
import java.io.IOException;

import ome.NewModel;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

public class LuceneIndexer {

	private File index;
	
	public void setFile(File index){
		this.index=index;
	}
	
    public void doIt(EventInterceptor.Events events){
    	try {
    		for (Class c: events.inserts.keySet()){
    			for (NewModel o: events.inserts.get(c)){
    				add(o,o.getId());
    			}
    		}
    		for (Class c: events.updates.keySet()){
    			for (NewModel o: events.updates.get(c)){
    				drop(o,o.getId());
    				add(o,o.getId());
    			}
    		}
    		for (Class c: events.deletes.keySet()){
    			for (NewModel o: events.deletes.get(c)){
    				drop(o,o.getId());
    			}
    		}

    	} catch (Exception e){
    		throw new RuntimeException(e);
    	}
    }
    
    //http://www.jroller.com/page/wakaleo/?anchor=lucene_a_tutorial_introduction_to
    
    // http://www.hibernate.org/138.html
    /**
     * Drop object from Lucene index
     */
    public void drop(NewModel entity, Integer id) throws IOException {
       IndexReader reader = IndexReader.open(index);// TODO entity.getIndexReader();
       reader.delete(new Term("id", id.toString()));
    }

    /**
     * Add object to Lucene index
     */
    public void add(NewModel entity, Integer id) throws IOException {
       Document doc = new Document();// TODO entity.getDocument();
       doc.add(Field.Keyword("id", id.toString()));
       doc.add(Field.Keyword("string", entity.toString()));
       doc.add(Field.Keyword("classname", entity.getClass().getName()));
       IndexWriter writer = new IndexWriter(index, new StandardAnalyzer(), true); // TODO entity.getIndexWriter(); false ???
       writer.addDocument(doc);
       writer.close();
    }
}