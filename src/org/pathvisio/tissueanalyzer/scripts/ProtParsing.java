package org.pathvisio.tissueanalyzer.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;



public class ProtParsing {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("/home/bigcat-jonathan/BiGCaT/HPA/normal_tissue.csv");
		String filename = "/home/bigcat-jonathan/BiGCaT/ProtTest2";
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		//adipose tissue, ,animal ovary ,bladder, heart ,thyroid gland"
		String tissues = "adrenal gland,appendix,bone marrow,cerebral cortex,colon,duodenum,endometrium,esophagus,gallbladder,heart muscle,kidney,liver,lung,lymph node,pancreas,placenta,prostate,salivary gland,skin,small intestine,spleen,stomach,testis,thyroid gland";
		ArrayList<String> tissuesList = new ArrayList<String>(Arrays.asList(tissues.split(",")));
		String line;
		boolean dataRow = false;
		String geneID = "";
		String tissueIndex = "";
		ArrayList<String> level = new ArrayList<String>();
		
		String header = "Gene\t";
		for (String tis : tissuesList){
			header += tis + "\t";
		}
		System.out.println(header);
		StringBuilder tmp = new StringBuilder();
		int i = 0;
		while ((line = br.readLine()) != null ){
			if (dataRow){
				line = line.replace("\"", "");
				ArrayList<String> data = new ArrayList<String>(Arrays.asList(line.split(",")));
				String aa = data.get(1).replace("\"", "");
				aa = aa.replace(" 1", "");
				aa = aa.replace(" 2", "");
				if (geneID.equals(data.get(0))){
					if (tissuesList.contains(aa) ){
//						System.out.println("yes!");
						if (tissueIndex.equals(aa) ){
							level.add(data.get(3));
//							tmp.append(" ");
//							tmp.append(data.get(2));
//							tmp.append(":");
//							tmp.append(data.get(3));
						}
						else {
							if (level.contains("High"))tmp.append("\tHigh");
							else if (level.contains("Medium"))tmp.append("\tMedium");
							else if (level.contains("Low"))tmp.append("\tLow");
							else tmp.append("\tNot detected");
							level.clear();
							level.add(data.get(3));
//							tmp.append("\t");
//							tmp.append(data.get(2));
//							tmp.append(":");
//							tmp.append(data.get(3));
//							tmp.append("\t");
						}
//						tmp.append(data.get(2));
//						tmp.append("\t");
//						tmp.append(data.get(3));
//						tmp.append("\t");
					}
//					tmp.append(data.get(1));
//					tmp.append("\t");
				
				}
				else {
//					System.out.println(tmp);
					if (!(tmp.length()==0)){						
						result.add(tmp.toString());
					}
					tmp = new StringBuilder();					
					tmp.append(data.get(0));
					tmp.append("\t");
//					tmp.append(data.get(1));
//					tmp.append("\t");
//					tmp.append(data.get(2));
//					tmp.append(":");
					tmp.append(data.get(3));
//					tmp.append("\t1");
				}

				geneID = data.get(0);
				tissueIndex = aa;
			}

			if ( line.contains("Gene") ) {
				dataRow = true;
			}

//			i++;
//			if (i==200) break;
		}
		PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
		pw.println(header);
		for (String club : result)
			pw.println(club);
		pw.close();
	}

}

