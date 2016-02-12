package org.pathvisio.tissueanalyzer.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.rdb.construct.DBConnector;
import org.pathvisio.core.model.ConverterException;
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

class Wp implements Comparable<Object>{

	private String path_id;
	private Double mean;
	private static File pgex;
	private static String path;
	private static IDMapper mapper;
	private static String name;
	private static int cutoff;
	
	public Wp(String path_id, Double mean){
		this.path_id= path_id;
		this.mean=mean;
	}

	public String getPath_id() {
		return path_id;
	}
	public Double getMean() {
		return mean;
	}

	@Override
	public int compareTo(Object obj) {
		if (! (obj instanceof Wp)) {  
			throw new ClassCastException(  
					"compared object must be instance of Wp");  
		}  
		return this.getMean().compareTo(((Wp) obj).getMean());  
	}
	public String toString(){
		return getPath_id()+"\t"+getMean();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, IDMapperException{
		

		Properties prop = new Properties();
//		InputStream input = new FileInputStream(args[0]);
		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Hs_2836_R");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Hs_2836_32");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Hs_2919_53");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Hs_3358_56");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Mm_3579_35");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Bt_2798_9");
//		InputStream input = new FileInputStream("/home/bigcat-jonathan/Desktop/TissueAnalyzer/config_TA_Cell");
		prop.load(input);
		pgex = new File(prop.getProperty("pgex"));
		path = prop.getProperty("pathwayOutput");
		File gpmlDir = new File (prop.getProperty("pathwaysInput"));
		if (!Files.isDirectory(Paths.get(path))) {
			new File(path).mkdir();
		}
		cutoff = Integer.parseInt(prop.getProperty("threshold"));

		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		DataSourceTxt.init();
		mapper = BridgeDb.connect("idmapper-pgdb:"+prop.getProperty("mapper"));

		
		List<File> gpmlList = FileUtils.getFiles(gpmlDir, "gpml", false);
		for (File gpml : gpmlList){
			name = gpml.getName();
			tissueAnalyser(gpml);
		}
		
//		List<List<Wp>> tot = new ArrayList<List<Wp>>();
//		File txtDir= new File (prop.getProperty("tissuesOutput"));
//		List<File> fileList = FileUtils.getFiles(txtDir, "txt", false);
//		for (File txt : fileList){
//			List<Wp> top = topTen(txt);
//			tot.add(top);
//		}
//		matching(tot);
	}

	public static void tissueAnalyser(File gpml){
		GexManager gex = new GexManager();
		DBConnector connector = new DBConnDerby();
		Pathway p = new Pathway();
		Map <Xref,String> labelMap = new HashMap<Xref,String>();

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
		read(gex, cache, setRefs, currentGdb,labelMap);
	}

	public static void read(GexManager gex, CachedData cache, Set<Xref> setRefs,
			IDMapperStack currentGdb,Map <Xref,String> labelMap){

		Collection<? extends ISample> names = null;
		Map<String,List<TissueResult>> data = 
				new TreeMap<String,List<TissueResult>>();

		try {			
			names = gex.getCurrentGex().getOrderedSamples();
			for ( ISample is : names){
				if ( !is.getName().equals(" Gene Name")){
//					System.out.println(is.getName().trim());
					data.put(is.getName().trim(),new ArrayList<TissueResult>());
				}
			}
			for (Xref ref : labelMap.keySet()){
				List<? extends IRow> pwData = cache.syncGet(ref);
				if (!pwData.isEmpty()){
					for ( ISample is : names) {
						for (IRow ir : pwData){
							if ( !is.getName().equals(" Gene Name")){
								Double value = 0.0;
								try {
									value = (Double) ir.getSampleData(is);
								} catch (ClassCastException e) {
									System.out.println(ir.getSampleData(is));
									e.getStackTrace();
								}								
//								Map<String, Set<String>> attributes = null;
//								attributes = currentGdb.getAttributes(ir.getXref());
								String label = labelMap.get(ref);
								label = label.replaceAll("\\n",".");
								label = label.replaceAll(" ",".");
								@SuppressWarnings("deprecation")
								String dd = ir.getXref().getId()+" "+label.trim()+" "+ir.getXref().getUrl().trim();
								TissueResult tr = new TissueResult(dd,value);								
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
		write(data);
	}
	public static void write(Map<String,List<TissueResult>> data){
		PrintWriter pw = null;
		name = FileUtils.removeExtension(name);
		File fileName =  new File(path + File.separator + name + ".txt");
		try {
			fileName.createNewFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			pw = new PrintWriter(new FileOutputStream(fileName));			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(Map.Entry<String, List<TissueResult>> entry : data.entrySet()) {
			int length = entry.getValue().size();
			Double i = 0.0;
			double sum = 0;
			String gene = "";
			String measured = "";
			ArrayList<Double> tmp = new ArrayList<Double>();
			for( TissueResult tr: entry.getValue()){
				if (tr.getExpression() >= (cutoff) ){
					i++;
					if (gene.equals("")){
						gene += tr.getGene();
					}
					else {
						gene += ","+tr.getGene();
					}
				}
				else{
					if (measured.equals("")){
						measured += tr.getGene();
					}
					else {
						measured += ","+tr.getGene();
					}
				}
				//filtered 				
				sum += tr.getExpression();
				tmp.add(tr.getExpression());
			}

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


			new File(path + File.separator + "Tissue").mkdir();
//			System.out.println(entry.getKey());
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
						sb.append(name+"\t"+mean+"\t"+perc+"\t"+median+"\t"+i.intValue()+"/"+length+"\n");
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

	public static List<Wp> topTen(File txt){
		BufferedReader br = null ;
		List<Wp> subby = null ;
		List<Wp> list = new ArrayList<Wp>();
		try {
			br = new BufferedReader(new FileReader(txt));
			if (br != null) {
				String line = br.readLine();
				while ( line!=null){
					String[] splitty = line.split("\t");
					list.add(new Wp(splitty[0],Double.parseDouble(splitty[3])));
					line = br.readLine();
				}
			}
			br.close();
//			System.out.println(list+"\n");
			Collections.sort(list,Collections.reverseOrder());
//			System.out.println("Sorted"+list+"\n");
			subby = list.subList(0, 9);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return subby;
	}
	public static void matching(List<List<Wp>>  li){
		HashMap<String,Integer> setty = new HashMap<String,Integer>();
		for(List<Wp>  lw : li){
			for(Wp wp : lw){
				int i = 0;
				for(List<Wp>  lw2 : li){
					for(Wp wp2 : lw2){
						if ( wp.getPath_id().equals(wp2.getPath_id()) ){
							i += 1;
						}
					}
				}
//				if (i>10)
					setty.put(wp.getPath_id(), i);
				
			}
		}
		List<Map.Entry<String, Integer>> keys =
				new LinkedList<Map.Entry<String, Integer>>( setty.entrySet());
		
		Collections.sort( keys, new Comparator<Map.Entry<String, Integer>>(){
			public int compare( Map.Entry<String,Integer> o1,
								Map.Entry<String,Integer> o2 )
			{
				return Integer.compare(o2.getValue(),o1.getValue());
			}
		} );
		for (Entry<String, Integer> entry : keys){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
//		for(String ss : setty)
//			System.out.println(ss);
	}
}
