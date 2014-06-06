package org.pathvisio.tissueanalyzer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdb.construct.DBConnector;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.data.DataException;
import org.pathvisio.desktop.data.DBConnDerby;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;
import org.pathvisio.tissueanalyzer.plugin.TissueResult;

class Wp {


	public static void main(String[] args){
		File gpmlDir = new File (args[0]);
		File pgex = new File(args[1]);		
		IDMapper mapper=null;
		try {
			Class.forName("org.bridgedb.rdb.IDMapperRdb");
			mapper = BridgeDb.connect("idmapper-pgdb:"+args[2]);
		} catch (IDMapperException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		String path = args[3];
		List<File> fileList = FileUtils.getFiles(gpmlDir, "gpml", false);
		for (File gpml : fileList)tissueAnalyser(gpml, pgex, path, mapper);		
	}
	
	public static void tissueAnalyser(File gpml, File pgex, String path, IDMapper mapper){
		GexManager gex = new GexManager();
		DBConnector connector = new DBConnDerby();
		Pathway p = new Pathway();
		CachedData cache = null;
		Set<Xref> setRefs = null;
		connector.setDbType(DBConnector.TYPE_GEX);
		try {
			SimpleGex simple = new SimpleGex (pgex.getAbsolutePath(), false, connector);
			gex.setCurrentGex(simple);
			p.readFromXml(gpml, true);
			cache = gex.getCachedData();		
			List<Xref> refs = p.getDataNodeXrefs();	
			setRefs = new HashSet<Xref>(refs);
			cache.setMapper(mapper);
			cache.preSeed(setRefs);			
		} catch (DataException | ConverterException  e1) {
			e1.printStackTrace();
		}
		String name = gpml.getName();
		TissueResult.read(gex, cache, setRefs, path, name);
	}
}
