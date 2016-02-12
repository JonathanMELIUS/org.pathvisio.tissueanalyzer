package org.pathvisio.tissueanalyzer.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.GdbProvider;
import org.bridgedb.rdb.IDMapperRdb;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.OntologyTag;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.data.DataException;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.data.DBConnDerby;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;
import org.pathvisio.tissueanalyzer.plugin.TissueResult;


public class ProtCalc {

	public static void main(String[] args) throws ConverterException{
//		File gpmlDir = new File (args[0]);
//		File pgex = new File(args[1]);		
		File gpml = new File (args[0]);
		IDMapper mapper=null;
		onto(gpml);

//		try {
//			Class.forName("org.bridgedb.rdb.IDMapperRdb");
//			DataSourceTxt.init();
//			mapper = BridgeDb.connect("idmapper-pgdb:"+args[2]);
//		} 
//		catch (IDMapperException e) {
//			e.printStackTrace();
//		} 
//		catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		String path = args[3];
//
//		List<File> fileList = FileUtils.getFiles(gpmlDir, "gpml", false);
//
//
//		for (File gpml : fileList){
//			String name = gpml.getName();
//			if (name.startsWith("WP")){
//				System.out.println(name);
//				name = args[4]+"_"+FileUtils.removeExtension(name);
//			}
//			System.out.println("hey");
//			//tissueAnalyser(gpml, pgex, path, mapper, name);
//		}
	}
	public static void onto(File gpml) throws ConverterException{
		Pathway p = new Pathway();
		p.readFromXml(gpml, true);
		for ( OntologyTag o : p.getOntologyTags()){
			System.out.println(o.getId()+o.getOntology()+o.getTerm());
		}
//		System.out.println(p.getOntologyTag().size()				);
	}
	public static void tissueAnalyser(File gpml, File pgex, String path, IDMapper mapper, String name){
		GexManager gex = new GexManager();
		DBConnector connector = new DBConnDerby();
		Pathway p = new Pathway();
		Map <Xref,String> labelMap = new HashMap<Xref,String>();

		//		gpml = new File ("/home/mael/Pathy/hs/Hs_ACE_Inhibitor_Pathway_WP554_70881.gpml");

		CachedData cache = null;
		Set<Xref> setRefs = null;
		connector.setDbType(DBConnector.TYPE_GEX);

		IDMapperStack currentGdb = new IDMapperStack();
		currentGdb.addIDMapper(mapper);


		try {
			SimpleGex simple = new SimpleGex (pgex.getAbsolutePath(), false, connector);
			gex.setCurrentGex(simple);
			p.readFromXml(gpml, true);
			for (PathwayElement e : p.getDataObjects()){
				labelMap.put(e.getXref(), e.getTextLabel());
//				e.getOntologyID();
			}
			cache = gex.getCachedData();		
			List<Xref> refs = p.getDataNodeXrefs();			
			setRefs = new HashSet<Xref>(refs);
			cache.setMapper(mapper);
			cache.preSeed(setRefs);			
		} 
		catch (DataException  e1) {
			e1.printStackTrace();
		}
		catch (ConverterException e) {
			e.printStackTrace();
		}
		read(gex, cache, setRefs, path, name, currentGdb,labelMap);
	}



	public static void read(GexManager gex, CachedData cache, Set<Xref> setRefs,
			String path, String name, IDMapperStack currentGdb,Map <Xref,String> labelMap){

		Collection<? extends ISample> names = null;
		Map<String,List<ProtResult>> data = 
				new TreeMap<String,List<ProtResult>>();

		try {			
			names = gex.getCurrentGex().getOrderedSamples();
			for ( ISample is : names){
				if ( !is.getName().equals(" Gene Name")){
					data.put(is.getName().trim(),new ArrayList<ProtResult>());
				}
			}
			for (Xref ref : labelMap.keySet()){
				List<? extends IRow> pwData = cache.syncGet(ref);
				if (!pwData.isEmpty()){
					for ( ISample is : names) {
						for (IRow ir : pwData){
							if ( !is.getName().equals(" Gene Name")){
								String value = "";
								try {
									value = (String) ir.getSampleData(is);
								} catch (ClassCastException e) {
									System.out.println(ir.getSampleData(is));
									e.getStackTrace();
								}								
								Map<String, Set<String>> attributes = null;
								attributes = currentGdb.getAttributes(ir.getXref());
								String label = labelMap.get(ref);
								label = label.replaceAll("\\n",".");
								label = label.replaceAll(" ",".");
								String dd = ir.getXref().getId()+" "+label.trim()+" "+ir.getXref().getUrl().trim();
								ProtResult tr = new ProtResult(dd,value);								
								data.get(is.getName().trim()).add(tr);
							}
						}
					}					
				}
			}
		} catch (DataException e) {
			e.printStackTrace();
		} catch (IDMapperException e) {
			e.printStackTrace();
		}
		write(data,path,name);
	}
	public static void write(Map<String,List<ProtResult>> data, String path, String name){
		PrintWriter pw = null;
		name = FileUtils.removeExtension(name);
		String fileName = path + File.separator + name + ".txt";

		try {
			pw = new PrintWriter(new FileOutputStream(fileName));			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(Map.Entry<String, List<ProtResult>> entry : data.entrySet()) {
			int length = entry.getValue().size();
			Double i = 0.0;
			double sum = 0;
			double sum2 = 0;
			String gene = "";
			String measured = "";
			ArrayList<Double> tmp = new ArrayList<Double>();
			for( ProtResult tr: entry.getValue()){
				//				if (tr.getExpression() >= (2/ Math.log10(2)) ){
				//					i++;
				//					if (gene.equals("")){
				//						gene += tr.getGene();
				//					}
				//					else {
				//						gene += ","+tr.getGene();
				//					}
				//				}
				//				else{
				//					if (measured.equals("")){
				//						measured += tr.getGene();
				//					}
				//					else {
				//						measured += ","+tr.getGene();
				//					}
				//				}
				//				//filtered 				
				//				sum += tr.getExpression();
				if (tr.getExpression().equals("High")){
					sum += 50.0;
					sum2 += 50.0;
				}
				else if (tr.getExpression().equals("Medium")){
					sum += 25.0;
					sum2 += 10.0;
				}
				else if (tr.getExpression().equals("Low")){
					sum += 5.0;
					sum2 += 1.0;
				}
				else {
					sum += 0.0;
					sum2 += 0.0;
				}
			}
			double mean = sum/length;
			mean = Math.round(mean*100.0)/100.0;
			double mean2 = sum2/length;
			mean2 = Math.round(mean2*100.0)/100.0;
//			System.out.println(name+ ":  "+entry.getKey()+ ":  "+median+ ":  "+mean);
			pw.println(entry.getKey()+"\t"+mean+"\t"+"\t"+mean2);
			/*
			double median;			
			Collections.sort(tmp);
			int s = tmp.size() ;
			if (s>0){
				if (s% 2 == 0){				
					median = ((double)tmp.get(s/2) + (double)tmp.get( (s/2)-1))/2;
				}
				else
					median = tmp.get(s/2);
				median = Math.round(median*100.0)/100.0;
			}
			else{
				median = 0.0;
			}
			double mean = sum/length;
			mean = Math.round(mean*100.0)/100.0;
			double perc = i/length*100;
			perc = Math.round(perc*100.0)/100.0;
			pw.println(entry.getKey()+"\t"+mean+"\t"+perc+"\t"+median+"\t"+gene+"\t"+measured);
*/	

			new File(path + File.separator + "Tissue").mkdir();
			File tissueFile = new File(path + File.separator + "Tissue" +File.separator  + entry.getKey() +".txt");
			try {
				tissueFile.createNewFile();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			String everything="";			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(tissueFile));
				if (br != null) {
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
					boolean flag = false;
					boolean present = true;
					while (line != null) {
						String[] path_fullName = line.split("\t");
						int index = path_fullName[0].indexOf("WP");
						String path_Name = path_fullName[0].substring(index);
						String[] path_id = path_Name.split("_");
						if (name.contains(path_id[0]) & !name.contains(path_id[1])) {							
							//						(line.contains(name)){
							//sb.append(name+"\t"+mean+"\t"+perc+"\t"+median+"\n");
							//sb.append(line+"\n");
							//							sb.append(line+" *"+"\n");
							flag = true;
						}
						else if (line.contains(name)) {
							present = false;
							sb.append(line+"\n");
						}
						else {
							sb.append(line+"\n");
						}
						line = br.readLine();					
					}
					if (line == null & (flag | present) ){
						sb.append(name+"\t"+mean+"\t"+mean2);
					}
					everything = sb.toString();
				} 
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
			try {
				PrintWriter tissueWriter = new PrintWriter(new FileOutputStream(tissueFile));
				tissueWriter.print(everything);
				tissueWriter.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}		
		}
		pw.close();
	}
}
