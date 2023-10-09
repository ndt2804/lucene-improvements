package edu.vt.cs.ir.examples;

import edu.vt.cs.ir.utils.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import vn.pipeline.*;
import java.io.*;

import java.io.File;
import java.util.ArrayList;

/**
 * This is an example of accessing corpus statistics and corpus-level term statistics.
 *
 * @author DungNT
 * @version 2021-01-01
 */
public class LuceneSearchExample {
//	public static final int OUR_SEARCH_TASK = 5;

    public static void main( String[] args ) {
        try {

            String pathIndex = "D:\\Java_indexi_lucene\\LuceneTutorial-master\\1000_vb_index";

            // Just like building an index, we also need an Analyzer to process the query strings
            Analyzer analyzer = new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents( String fieldName ) {
                    // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                    TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                    // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                    ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
                    return ts;
                }
            };

            String field = "paragraph"; // the truy van
            QueryParser parser = new QueryParser( field, analyzer ); //(paragraph: hạn_hán) AND (paragraph: lãnh_thổ) OR (paragraph: việt_nam)
            
            //tách pos tagging theo vncore nlp
            String[] annotators = {"wseg", "pos", "ner", "parse"}; 
            VnCoreNLP pipeline = new VnCoreNLP(annotators);             
                        
            String qstr = "điều kiện trở thành giảng viên"; // tu khoa truy van: thiên tai cảnh báo lãnh thổ việt nam //điều_kiển trở_thành giảng_viên// hạn_hán lãnh_thổ việt_nam

//            String qstr = "điều kiện trở thành giảng viên";
            //xử lý annotation
            Annotation annotation = new Annotation(qstr); 
            pipeline.annotate(annotation);
            System.out.println(annotation.toString());                      ﻿
            // Tạo mảng chưa các danh từ, System.out.println(w.getForm()); System.out.println(w.getPosTag());
            String qstr1 = "(";
            ArrayList<String> Danhtu = new ArrayList<String>(); 
            ArrayList<Integer> TruocDongTu1= new ArrayList <Integer>(); 
            ArrayList<String> TruocDongTu= new ArrayList<String>(); 
            ArrayList<Integer> TruocDongTu2 = new ArrayList <Integer>(); 
            ArrayList<String> TruocDongTu3= new ArrayList<String>(); 
            ArrayList<String> TruocDongTu4= new ArrayList<String>();

            for (Sentence i : annotation.getSentences()) {
                System.out.println(i);
                for (Word w : i.getWords()) {   
                	if ("M".equals(w.getPosTag())) {
                        TruocDongTu1.add((w.getIndex()-1));
                        TruocDongTu1.add((w.getIndex()+1));
                        TruocDongTu.add(w.getForm());
                    }
                }
                for (Word w : i.getWords()) {   
                    if ("N".equals(w.getPosTag())) {
                        for(int a:TruocDongTu1){
                            if(a==(w.getIndex())) {
                                TruocDongTu2.add(w.getIndex());
                                TruocDongTu3.add(w.getForm());
                            }
                        }
                	}        
                }
                for (Word w : i.getWords()) {   
                	if ("M".equals(w.getPosTag())) {
                        for(int a:TruocDongTu2) {
                            if(a == w.getIndex()+1) {
                                for(int h = 0; h<TruocDongTu3.size(); h++) {
                                    if(w.getForm() == TruocDongTu.get(h)) {
                                        TruocDongTu4.add(w.getForm() + "_" TruocDongTu3.get(h));
                                    }
                                }
                            }
                            if(a == w.getIndex() -1 ) {
                                for(int h = 0; h<TruocDongTu3.size(); h++) {
                                    if(w.getForm() == TruocDongTu3.get(h)) {
                                        TruocDongTu4.add(TruocDongTu3.get(h) + "_" w.getForm());
                                    }
                                }
                            }
                        }

                      
                    }
                }            	        	               	                	            
            } 
            for(Sentence i : annotation.getSentences()) {
                for(Word w : i.getWords()) {
                    if("N".equals(w.getPosTag())) {
                        for( String string:TruocDongTu3) {
                            if(w.getForm().equals(string)) {
                                for(int h = 0; h<TruocDongTu4.size(); h++) {
                                    String a = TruocDongTu4.get(h);
                                    Danhtu.add("paragraph:" + a);
                                }
                            }
                            Danhtu.add("paragraph:" + w.getForm());
                        }
                    }
                    if("V".equals(w.getPosTag())) {
                        Danhtu.add("paragraph:" + w.getForm());
                    }
                }
            }
            // Gán điều kiện AND cho mảng Danhtu và ghép chuỗi
            int count = 0;
            for(String str:Danhtu) {
            	qstr1 = qstr1 + str;
            	count ++;
            	if (count < Danhtu.size()) {
            		qstr1 =  qstr1 + " AND ";
            	}else {
            		qstr1 = qstr1 + ")";
            	}
            }
            for(String str:TruocDongTu4) {
                qstr1 = qstr1 + "OR" + "paragraph:" + str;
            }
            //Ghép chuỗi mới và chuỗi ban đầu khi chưa phân loại từ
            for (Sentence i : annotation.getSentences()) {
                System.out.println(i);
                for (Word w : i.getWords()) {
                		qstr1 = qstr1 + " OR " + "paragraph:" + w.getForm();
              }
            }
             System.out.println(qstr1);
                                     
            //thực hiện query chuỗi mới sau tiền xử lý
            Query query = parser.parse( qstr1 );
            
            Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
            IndexReader index = DirectoryReader.open( dir );
            IndexSearcher searcher = new IndexSearcher( index );
            searcher.setSimilarity( new BM25Similarity() );

            int top = 5;
            TopDocs docs = searcher.search( query, top ); 

            System.out.printf( "%-5s%-150s%-10s%s\n", "Rank", "Description", "Score", "Title" );
            int rank = 1;
                        
            // Đánh dấu tập tài liệu relevance (liên quan), Empty = False, 1 = True
            
//            boolean boolean_relevance = docs.equals(query);
//            String relevance = "";
//    		if (boolean_relevance && docs.hashCode() == OUR_SEARCH_TASK)
//    			relevance = "1";

            //Đếm số lượng tài liệu và số tài liệu liên quan
    		
    		    		    		            
            for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
                int id = scoreDoc.doc;
                double score = scoreDoc.score;
                String description = LuceneUtils.getDocno( index, "description", id );
                String title = LuceneUtils.getDocno( index, "title", id );
                System.out.printf( "%-5d%-150s%-10.4f%s\n", rank, description, score, title );
                rank++;
            }

            index.close();
            dir.close();
            
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

