package search;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.similar.MoreLikeThis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

// most code from Lucene demo
// each doc in search results is used to make a SearchResultHtml object. 
// List of these is returned

/** Simple command-line based search demo. */
public class SearchFiles {
	
	public static int HITS_PER_PAGE = 10;
	
	public static String index = "index";
	public static String field = "contents";

  /** Use the norms from one field for all fields.  Norms are read into memory,
   * using a byte of memory per document per searched field.  This can cause
   * search of large collections with a large number of fields to run out of
   * memory.  If all of the fields contain only a single token, then the norms
   * are all identical, then single norm vector may be shared. */
  private static class OneNormsReader extends FilterIndexReader {
    private String field;

    public OneNormsReader(IndexReader in, String field) {
      super(in);
      this.field = field;
    }

    public byte[] norms(String field) throws IOException {
      return in.norms(this.field);
    }
  }

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void search(String searchString, ArrayList<SearchResultHtml> results) throws Exception {
	  
	  Hits hits = null;  
	  
    String queries = null;
    //int repeat = 0;
    //boolean raw = false;
    String normsField = null;

    IndexReader reader = IndexReader.open(index);


    if (normsField != null)
      reader = new OneNormsReader(reader, normsField);

    Searcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();

    BufferedReader in = null;
    if (queries != null) {
      in = new BufferedReader(new FileReader(queries));
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    }
      QueryParser parser = new QueryParser(field, analyzer);
    
      
      Query query = parser.parse(searchString);
      System.out.println("Searching for: " + query.toString(field));

      hits = searcher.search(query);
      
      System.out.println(hits.length() + " total matching documents");

      addHitsToList(hits, results, searchString);
    
    reader.close();
  }
  
  public static void search (File findMoreLikeThis, ArrayList<SearchResultHtml> results) throws Exception {
	  Hits hits = null;
	  
	  IndexReader reader = IndexReader.open(index);
	  Searcher searcher = new IndexSearcher(reader);
	  
	  MoreLikeThis mlt = new MoreLikeThis(reader);
	//  Reader target = new FileReader(findMoreLikeThis);  // orig source of doc you want to find similarities to
	  Query query = null;
	  try {
		  query = mlt.like(findMoreLikeThis);
		  hits = searcher.search(query);
		// now the usual iteration thru 'hits' - the only thing to watch for is to make sure
		// you ignore the doc if it matches your 'target' document, as it should be similar to itself 

		  addHitsToList(hits, results, findMoreLikeThis.getName());
	  } catch (FileNotFoundException ex) {
		  // findMoreLikeThis file not found
		  ex.printStackTrace();
	  }
	  
	  reader.close();
  }
  
  public static void addHitsToList(Hits hits, ArrayList<SearchResultHtml> results, String searchString) {
	  for (int start = 0; start < hits.length(); start += HITS_PER_PAGE) {
	        int end = Math.min(hits.length(), start + HITS_PER_PAGE);
	        for (int i = start; i < end; i++) {

	  //      	float score = hits.score(i);

	  //      System.out.println("doc="+hits.id(i)+" score="+ score);


	        	Document doc = null;
	        	try {
	        		doc = hits.doc(i);
	        	} catch (CorruptIndexException e) {
	        		// TODO Auto-generated catch block
	        		e.printStackTrace();
	        	} catch (IOException e) {
	        		// TODO Auto-generated catch block
	        		e.printStackTrace();
	        	}
	          
	        	results.add(new SearchResultHtml(doc, searchString));
	        }
	      
	      }
  }
}

