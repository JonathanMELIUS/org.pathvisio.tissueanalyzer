package org.pathvisio.tissueanalyzer.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.data.DataException;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager;

public class TissueResult {

	private String gene;
	private double expression;

	public TissueResult(String gene, double expression){
		this.gene=gene;
		this.expression=expression;
	}
	public String toString(){
		return gene+" "+String.valueOf(expression);			
	}
	public String getGene() {
		return gene;
	}
	public double getExpression() {
		return expression;
	}
	public void setGene(String gene) {
		this.gene = gene;
	}
	public void setExpression(double expression) {
		this.expression = expression;
	}

	public static void read(GexManager gex, CachedData cache, Set<Xref> setRefs, String path, String name){

		Collection<? extends ISample> names = null;
		Map<String,List<TissueResult>> data = 
				new TreeMap<String,List<TissueResult>>();

		try {			
			names = gex.getCurrentGex().getOrderedSamples();
			for ( ISample is : names){
				if ( !is.getName().equals(" Gene Name")){
					data.put(is.getName().trim(),new ArrayList<TissueResult>());
				}
			}			
			for (Xref ref : setRefs){
				List<? extends IRow> pwData = cache.syncGet(ref);
				if (!pwData.isEmpty()){
					for ( ISample is : names) {
						for (IRow ir : pwData){
							if ( !is.getName().equals(" Gene Name")){
								Double value = 0.0;
								try {
									value = (double) ir.getSampleData(is);
								} catch (ClassCastException e) {
									System.out.println(ir.getSampleData(is));
									// TODO: handle exception
									e.getStackTrace();
								}
								String dd = ir.getXref().getId();
								TissueResult tr = new TissueResult(dd,value);								
								data.get(is.getName().trim()).add(tr);
							}
						}
					}					
				}
			}
		} catch (DataException | IDMapperException  e) {
			e.printStackTrace();
		}
		calcul(data,path,name);
	}
	public static void calcul(Map<String,List<TissueResult>> data, String path, String name){
		PrintWriter pw = null;
		name = FileUtils.removeExtension(name);
		String fileName = path + File.separator + name + ".txt";

		try {
			pw = new PrintWriter(new FileOutputStream(fileName));			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(Entry<String, List<TissueResult>> entry : data.entrySet()) {
			//			System.out.println(entry.getKey()+"  "+entry.getValue());
			int length = entry.getValue().size();
			double i = 0;
			double tmp = 0;
			String gene = "";
			for( TissueResult tr: entry.getValue()){
				if (tr.getExpression() >= (2/ Math.log10(2)) ){
					i++;
					if (gene.equals("")){
						gene += tr.getGene();
					}
					else {
						gene += ","+tr.getGene();
					}
				}
				//filtered 				
				tmp += tr.getExpression();
			}
			double mean = tmp/length;
			//			System.out.println(i/length*100);
			pw.println(entry.getKey()+"\t"+mean+"\t"+gene);
			new File(path + File.separator + "Tissue").mkdir();
			File tissueFile = new File(path + File.separator + "Tissue" +File.separator  + entry.getKey() +".txt");
			try {
				tissueFile.createNewFile();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}


			String everything="";
			try(BufferedReader br = new BufferedReader(new FileReader(tissueFile))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
//				System.out.println("line: "+line);
				boolean flag = true;
				while (line != null) {
					if (line.startsWith(name)){
						sb.append(line+" *"+"\n");
						flag = false;
					}
					else {
						sb.append(line+"\n");
					}
					line = br.readLine();					
				}
				if (line == null & flag ){
//					System.out.println("vide");
					sb.append(name+"\t"+mean+"\n");
				}
//				System.out.println(sb);
				everything = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			PrintWriter tissueWriter= null;
			try {
				tissueWriter = new PrintWriter(new FileOutputStream(tissueFile));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			tissueWriter.print(everything);
			tissueWriter.close();
		}
		pw.close();
	}
}
