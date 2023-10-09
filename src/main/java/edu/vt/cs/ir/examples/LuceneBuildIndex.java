package edu.vt.cs.ir.examples;

//import org.apache.commons.compress.utils.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//import java.io.InputStream;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.zip.GZIPInputStream;
//import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

//-------------
import vn.pipeline.*;

//import java.io.FileInputStream;
import java.io.*;
import java.text.Annotation;

/**
 * This is an example of building a Lucene index for the example corpus.
 *
 * @author DungNT
 * @version 2021-01-01
 */

public class LuceneBuildIndex {
    public static void main( String[] args ) throws IOException {
    	//tiền xử lý dữ liệu thô trước khi đánh index
            // "wseg", "pos", "ner", and "parse" refer to as word segmentation, POS tagging, NER and dependency parsing, respectively. 
            String[] annotators = {"wseg", "pos", "ner", "parse"}; 
            VnCoreNLP pipeline = new VnCoreNLP(annotators); 
            JSONParser parser1 = new JSONParser();
            JSONArray b = null;
            //parser file dữ liệu thô
			try {
				b = (JSONArray) parser1.parse(new FileReader("C:\\Java_indexi_lucene\\LuceneTutorial-master\\data_thuvienphapluat.json"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String fileName = "data_thuvienphapluat_DTXL2.json";
    		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        	try {
        		if (b != null)
        			writer.write("[" + "\n");
		            for (Object o : b)
		            {
		            	JSONObject person = (JSONObject) o;	
		         //title
		            	String Title = (String) person.get("title");
		            	writer.write("{\"title\": \"");
		                System.out.println(Title);
		                
		                Annotation annotation = new Annotation(Title); 
		                pipeline.annotate(annotation); 
		                List<Sentence> lstSentences = annotation.getSentences();
		                for (Sentence s : lstSentences) {
		                	writer.write(s.getWordSegmentedSentence());
		                }
		                writer.write("\"" + "," + "\n" );              	
		        //description
		                String Description = (String) person.get("description");
		            	writer.write("\"description\": \"");
		                
		                Annotation annotation2 = new Annotation(Description); 
		                pipeline.annotate(annotation2); 
		                List<Sentence> lstSentences2 = annotation2.getSentences();
		                for (Sentence s1 : lstSentences2) {
		                	writer.write(s1.getWordSegmentedSentence());
		                }
		                writer.write("\""+ "," + "\n" );
		         //paragraph
		            	JSONArray paragraph = (JSONArray) person.get("paragraph");
		            	StringBuffer sb1 = new StringBuffer();
		            	for (Object p : paragraph)
		            	{
		            		sb1.append(p.toString());
		            	}
		            	writer.write("\"paragraph\": [\"");
		            	
		                Annotation annotation3 = new Annotation(sb1.toString()); 
		                pipeline.annotate(annotation3); 
		                List<Sentence> lstSentences3 = annotation3.getSentences();
		                for (Sentence s3 : lstSentences3) {
		                	writer.write(s3.getWordSegmentedSentence());
		                }
		                writer.write("\"]}," + "\n");
		            }
		            writer.write("\n"+"]");
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally{
				writer.close();
			}
    	//----------------Phần đánh index dữ liệu đã tiền xử lý-------------------
        	/**    
    	JSONParser parser = new JSONParser();
        try {

        	JSONArray a = (JSONArray) parser.parse(new FileReader("D:\\Java_indexi_lucene\\LuceneTutorial-master\\Data_1000_VB_DTXL.json"));
        	
            String pathIndex = "D:\\Java_indexi_lucene\\LuceneTutorial-master\\1000_vb_index";

            Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );

            // Analyzer specifies options for text processing
            Analyzer analyzer = new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents( String fieldName ) {
                    // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                    TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                    // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                    ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
                    // Step 3: whether to remove stop words
//                    ts = new TokenStreamComponents( ts.getTokenizer(), new StopFilter( ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET ) );
                    // Step 4: whether to apply stemming
//                    ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
//                    ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                    return ts;
                }
            };

            IndexWriterConfig config = new IndexWriterConfig( analyzer );
            // Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
            config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );

            IndexWriter ixwriter = new IndexWriter( dir, config );

            // This is the field setting for metadata field.
            FieldType fieldTypeMetadata = new FieldType();
            fieldTypeMetadata.setOmitNorms( true );
            fieldTypeMetadata.setIndexOptions( IndexOptions.DOCS );
            fieldTypeMetadata.setStored( true ); 
            fieldTypeMetadata.setTokenized( false );
            fieldTypeMetadata.freeze();

            // This is the field setting for normal text field.
            FieldType fieldTypeText = new FieldType();
            fieldTypeText.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
            fieldTypeText.setStoreTermVectors( true );
            fieldTypeText.setStoreTermVectorPositions( true );
            fieldTypeText.setTokenized( true );
            fieldTypeText.setStored( true );
            fieldTypeText.freeze();
           
            // đọc file json đã tiền xử lý
            
            for (Object o : a)
            {
              JSONObject person = (JSONObject) o;

              String title = (String) person.get("title");
              System.out.println(title);
              
              String description = (String) person.get("description");
              System.out.println(description);
              
//              String paragraph = (String) person.get("paragraph");
//              System.out.println(paragraph);
              
              JSONArray paragraph = (JSONArray) person.get("paragraph");
              StringBuffer sb = new StringBuffer();
              for (Object p : paragraph)
              {
            	sb.append(p.toString());
                System.out.println(p.toString());
              }

              Document d = new Document();
              // Add each field to the document with the appropriate field type options

              d.add( new Field( "title", title, fieldTypeText ) );
              d.add( new Field( "description",description , fieldTypeText ) );
//              d.add( new Field( "paragraph",paragraph , fieldTypeText ) );
              d.add( new Field( "paragraph",sb.toString() , fieldTypeText ) );

              // Add the document to index.
              ixwriter.addDocument( d );
              System.out.println(d);
            }

            // remember to close both the index writer and the directory
            ixwriter.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
        */
    }

}


