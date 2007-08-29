package xmlMVC;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/** Index all text files under a directory. */
public class IndexFiles {
  
  private IndexFiles() {}

  static final File INDEX_DIR = new File("index");
  
  static DateFormat fDateFormat = DateFormat.getDateInstance (DateFormat.LONG);
  static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
  
  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.demo.IndexFiles <root_directory>";
    if (args.length == 0) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    if (INDEX_DIR.exists()) {
    	System.out.println("Index already exists. Writing over old index...>!");
      //System.out.println("Cannot save index to '" +INDEX_DIR+ "' directory, please delete it first");
      //System.exit(1);
    }
    
    final File docDir = new File(args[0]);
    if (!docDir.exists() || !docDir.canRead()) {
      System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      IndexWriter writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer(), true);
      System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
      indexDocs(writer, docDir);
      System.out.println("Optimizing...");
      writer.optimize();
      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  static void indexDocs(IndexWriter writer, File file)
    throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        System.out.println("IndexFiles: adding " + file + "......");
        try {
        	
            // make a new, empty document
            Document doc = new Document();
            
//          Add the url as a field named "path".  Use a field that is 
    		// indexed (i.e. searchable), but don't tokenize the field into words.
    		doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
    				Field.Index.UN_TOKENIZED));
    		
    		doc.add(new Field("name", file.getName(), Field.Store.YES,
    				Field.Index.UN_TOKENIZED));

    		// Add the last modified date of the file a field named "modified".  
    		// Use a field that is indexed (i.e. searchable), but don't tokenize
    		// the field into words.
    		doc.add(new Field("modified",
            		fDateFormat.format(file.lastModified()) + " " + timeFormat.format(file.lastModified()),
                    Field.Store.YES, Field.Index.UN_TOKENIZED));
    		
//    		HashMap allAttributes = (new XMLMethods().getAllXmlFileAttributes(file));
//    		Iterator keyIterator = allAttributes.keySet().iterator();
//    		
//    		while (keyIterator.hasNext()) {
//    			String key = (String)keyIterator.next();
//    			String value = (String)allAttributes.get(key);
//    			
//    			if ((value != null) && (value.length() > 0)) {
//    				doc.add(new Field(key, value, Field.Store.YES, Field.Index.TOKENIZED));
//    				System.out.println("IndexFiles: Adding Field: name: " + key + ", value: " + value);
//    			}
//    		}
    		
    		doc.add(new Field("contents", new FileReader(file)));

            writer.addDocument(doc);
            
            System.out.println("IndexFiles.indexDocs: Added document to writer.");
        //  writer.addDocument(FileDocument.Document(file));
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
        	System.out.println("Exception - doc not added to writer.");
        }
      }
    }
  }
  
}
