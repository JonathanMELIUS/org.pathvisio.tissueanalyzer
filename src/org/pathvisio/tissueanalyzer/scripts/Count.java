package org.pathvisio.tissueanalyzer.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;


public class Count {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		File e2836 = new File("/home/bigcat-jonathan/Desktop/TissueAnalyzer/E-MTAB-2836_tissues.txt"); 
		File e2919 = new File("/home/bigcat-jonathan/Desktop/TissueAnalyzer/E-MTAB-2919_tissues.txt");
		File e3358 = new File("/home/bigcat-jonathan/Desktop/TissueAnalyzer/E-MTAB-3358_tissues.txt"); 

		Map<String,Integer>  all = new HashMap<String,Integer>();
		HashSet<String>  s2836 = new HashSet<String>();
		HashSet<String>  s2919 = new HashSet<String>();
		HashSet<String>  s3358 = new HashSet<String>();
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(e2836));
		while ((line = br.readLine()) != null) {
//			System.out.println(line);
			
			if (all.get(line)==null)all.put(line,1);
			else all.put(line,all.get(line)+1);
			s2836.add(line);
		}

		BufferedReader br2 = new BufferedReader(new FileReader(e2919));
		while ((line = br2.readLine()) != null) {
			if (all.get(line)==null)all.put(line,1);
			else all.put(line,all.get(line)+1);
			s2919.add(line);
		}
		BufferedReader br3 = new BufferedReader(new FileReader(e3358));
		while ((line = br3.readLine()) != null) {
			if (all.get(line)==null)all.put(line,1);
			else all.put(line,all.get(line)+1);
			s3358.add(line);
		}
		System.out.println(all);
		for ( Entry<String, Integer> e : all.entrySet()){
			if (e.getValue()==1){
//				System.out.println(e.getKey());
				if (s2836.contains(e.getKey()))System.out.println(e.getKey()+"\t"+"s2836");
				if (s2919.contains(e.getKey()))System.out.println(e.getKey()+"\t"+"s2919");
				if (s3358.contains(e.getKey()))System.out.println(e.getKey()+"\t"+"s3358");
			}
		}
//		for (String t: s2836){
//			if (!all.contains(t))System.out.println(t);
//		}
	}
}


