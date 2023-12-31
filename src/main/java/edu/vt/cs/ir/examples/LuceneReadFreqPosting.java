package edu.vt.cs.ir.examples;

import edu.vt.cs.ir.utils.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;

/**
 * This is an example for accessing a term-document-frequency posting list from a Lucene index.
 *
 * @author DungNT
 * @version 2021-01-01
 */
public class LuceneReadFreqPosting {
	
	public static void main( String[] args ) {
		try {
			
			String pathIndex = "D:\\Java_indexi_lucene\\LuceneTutorial-master\\example_index_lucene";
			
			// Let's just retrieve the posting list for the term "reformulation" in the "text" field
			String field = "text";
			String term = "reformulation";
			
			Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
			IndexReader index = DirectoryReader.open( dir );
			
			// The following line reads the posting list of the term in a specific index field.
			// You need to encode the term into a BytesRef object,
			// which is the internal representation of a term used by Lucene.
			System.out.printf( "%-10s%-15s%-6s\n", "DOCID", "DOCNO", "FREQ" );
			PostingsEnum posting = MultiFields.getTermDocsEnum( index, field, new BytesRef( term ), PostingsEnum.FREQS );
			if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
				int docid;
				// Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position
				// and returns the docid of the current entry (document). Note that this is an internal Lucene docid.
				// It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
				while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {
					String docno = LuceneUtils.getDocno( index, "docno", docid );
					int freq = posting.freq(); // get the frequency of the term in the current document
					System.out.printf( "%-10d%-15s%-6d\n", docid, docno, freq );
				}
			}
			
			index.close();
			dir.close();
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
}
