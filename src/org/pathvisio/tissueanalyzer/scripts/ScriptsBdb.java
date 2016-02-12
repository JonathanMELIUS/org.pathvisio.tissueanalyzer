package org.pathvisio.tissueanalyzer.scripts;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.DataSourceTxt;

public class ScriptsBdb {

	public static void main(String[] args) throws IDMapperException, ClassNotFoundException  {
		BioDataSource.init();
		mapping_translating();
//		mapping_translating2();
//		combining();
////		searching();
//		guessing();
	}
	public static void mapping_translating() throws IDMapperException, ClassNotFoundException{
//		Class.forName("org.bridgedb.webservice.bridgerest.BridgeRest");
		// now we connect to the driver and create a IDMapper instance.
//		IDMapper mapper = BridgeDb.connect ("idmapper-bridgerest:http://webservice.bridgedb.org/Human");
		
		Class.forName("org.bridgedb.webservice.biomart.IDMapperBiomart");
		IDMapper mapper = BridgeDb.connect("idmapper-biomart:http://www.biomart.org/biomart/martservice?mart=ensembl&dataset=hsapiens_gene_ensembl");

		// We create an Xref instance for the identifier that we want to look up.
		// In this case we want to look up Entrez gene 3643.
//		Xref src = new Xref ("3643", BioDataSource.ENTREZ_GENE);
		Xref src = new Xref( "ENSG00000171105", DataSource.getBySystemCode("En") );//BioDataSource.ENSEMBL

		// let's see if there are cross-references to Ensembl Human
		//		Set<Xref> dests = mapper.mapID(src, DataSource.getBySystemCode("En"));
		Set<Xref> dests = mapper.mapID(src,BioDataSource.ENTREZ_GENE);
//		Set<Xref> dests = mapper.mapID(src);

		// and print the results.
		// with getURN we obtain valid MIRIAM urn's if possible.
		System.out.println (src.getMiriamURN() + " maps to:");
		for (Xref dest : dests)
			System.out.println("  " + dest.getMiriamURN());
	}
	public static void mapping_translating2() throws IDMapperException, ClassNotFoundException{
		Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
		IDMapper mapper = BridgeDb.connect("idmapper-bridgerest:http://webservice.bridgedb.org/Human");

		// Start with defining the Chebi identifier for
		// Methionine, id 16811 DL-Methionine 
		// L-methionine (CHEBI:16643) 
		Xref src = new Xref("16811", BioDataSource.CHEBI);   
//		Xref src = new Xref("16643", BioDataSource.CHEBI);   
//		Xref src = new Xref( "ENSG00000171105", DataSource.getExistingBySystemCode("En"));
		
//		System.out.println(src.getKnownUrl()+ " "+src.getMiriamURN());
//		Xref met = new Xref("6137", BioDataSource.PUBCHEM_COMPOUND);
//		System.out.println(met.getKnownUrl()+ " "+met.getMiriamURN());
		// the method returns a set, but in actual fact there is only one result
		for (Xref dest : mapper.mapID(src))
		{
			// this should print 6137, the pubchem identifier for Methionine.
			System.out.println ("" + dest.getId());
		}
	}
	public static void combining() throws ClassNotFoundException, IDMapperException{
		IDMapperStack geneMapper; 

		// load libs for derby files
		Class.forName("org.bridgedb.rdb.IDMapperRdb");  
		Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
//		BioDataSource.init();
//		DataSourceTxt.init();
		
		// register two mappers, in this case data-derby mappers given by a properties file
		IDMapper mouseMapper = BridgeDb.connect("idmapper-pgdb:" +"/home/mael/Pathy/Mm_Derby_20130701.bridge");
//		IDMapper humanMapper = BridgeDb.connect("idmapper-pgdb:" + "/home/mael/Pathy/Hs_Derby_20130701.bridge");
//		IDMapper mouseMapper = BridgeDb.connect("idmapper-bridgerest:http://webservice.bridgedb.org/Mouse");
		IDMapper humanMapper = BridgeDb.connect("idmapper-bridgerest:http://webservice.bridgedb.org/Human");

		// create mapper stack
		geneMapper = new IDMapperStack();
		// add mappers to the stack
		geneMapper.addIDMapper(mouseMapper);
		geneMapper.addIDMapper(humanMapper);
		              
		// the mapper stack can now be used for mapping mouse and also human ids
//		Xref mouseRef = new Xref( "ENSMUSG00000017167", DataSource.getBySystemCode("En") );
		Xref mouseRef = new Xref( "ENSMUSG00000017167", BioDataSource.ENSEMBL );
		Set<Xref> mouseResults = geneMapper.mapID( mouseRef );
//		Xref humanRef = new Xref( "ENSG00000171105", DataSource.getBySystemCode("En"));
		Xref humanRef = new Xref( "ENSG00000171105",BioDataSource.ENSEMBL );
		Set<Xref> humanResults = geneMapper.mapID( humanRef );
		for (Xref dest : mouseResults)
			System.out.println("  " + dest.getMiriamURN());
		for (Xref dest : humanResults)
			System.out.println("  " + dest.getId());

	}
	public static void searching() throws IDMapperException, ClassNotFoundException{
		Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
		IDMapper mapper = BridgeDb.connect("idmapper-bridgerest:http://webservice.bridgedb.org/Human");
		String query = "3643";
        
		// let's do a free search without specifying the input type:
		Set<Xref> hits = mapper.freeSearch(query, 100);

		// Now print the results.
		// with getURN we obtain valid MIRIAM urn's if possible.
		System.out.println (query + " search results:");
		for (Xref hit : hits)
		        System.out.println("  " + hit);
	}
	public static void guessing() throws IDMapperException, ClassNotFoundException{
		//String query = "NP_036430";
		String query = "ENSG00000171105";
		System.out.println ("Which patterns match " + query + "?");

		// DataSourcePatterns holds a registry of patterns
		Map<DataSource,	Pattern> patterns =	DataSourcePatterns.getPatterns();

		// loop over all patterns
		for (DataSource key : patterns.keySet())
		{
		        // create a matcher for this pattern
		        Matcher matcher = patterns.get(key).matcher(query);
		        
		        // see if the input matches, and print a message
		        if (matcher.matches())
		        {
		                System.out.println (key.getFullName() + " matches!");
		        }
		}  
	
	}
}
