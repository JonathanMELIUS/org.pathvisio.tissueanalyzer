package org.pathvisio.tissueanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.tissueanalyzer.plugin.TissueResult;
import org.pathvisio.tissueanalyzer.utils.ObserverSidePanel;
import org.pathvisio.visualization.plugins.ColorByExpression;
import org.pathvisio.visualization.plugins.DataNodeLabel;
import org.pathvisio.visualization.plugins.LegendPanel;

/**
 * Create a side panel to change dynamically the list of visualized tissues.
 * @author Jonathan Melius
 * @see ObserverSidePanel
 */

public class TissueSidePanel extends JPanel 
	implements ActionListener,GexManagerListener, TableModelListener
	//,ApplicationEventListener
{

	private JPanel panel;
	private PvDesktop standaloneEngine;
	private Vector<String> vT;
	private Vector<Boolean> vB;
	private LegendPanel legendPane;
	private JButton calcul;
	private MyTableModel dtm;
	private JTable table;

	public TissueSidePanel(PvDesktop standaloneEngine){
		this.standaloneEngine = standaloneEngine;
		this.standaloneEngine.getGexManager().addListener(this);
		//this.standaloneEngine.getSwingEngine().getEngine().addApplicationEventListener(this);
		
		legendPane = new LegendPanel(standaloneEngine.getVisualizationManager());
		//legendPane.setSize(200, 200);
		legendPane.setPreferredSize(new Dimension(150,150));
		legendPane.setBorder(null);		
		
		calcul = new JButton("Calcul");
		calcul.addActionListener(this);

		dtm = new MyTableModel();	
		table = new JTable(dtm);
		table.getModel().addTableModelListener(this);
		table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
		
//        panel = new JPanel();
//		panel.setLayout (new GridLayout(0,1));
//		panel.add(calcul);
//        panel.add(new JScrollPane(table));
//        panel.add(legendPane);
//        add(panel);
        
//		add(calcul, BorderLayout.PAGE_START);        
//        setLayout( new GridLayout(0,1));
//		add(new JScrollPane(table), BorderLayout.PAGE_END);
//		add(legendPane, BorderLayout.EAST);
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
      //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        add(legendPane, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.1;
        c.ipady = 10;
        add(calcul, c);
        
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2; 
        add(new JScrollPane(table), c);
       
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {		
		List<Xref> xrefs = standaloneEngine.getSwingEngine().getEngine().getActivePathway().getDataNodeXrefs();
		Set<Xref> setRefs = new HashSet<Xref>(xrefs);

		DataInterface gex = standaloneEngine.getGexManager().getCurrentGex();
		CachedData cache = standaloneEngine.getGexManager().getCachedData();
		Collection<? extends ISample> names = null;


		Map<String,List<TissueResult>> data = 
				new TreeMap<String,List<TissueResult>>();
		try {			
			names = gex.getOrderedSamples();

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
								Double value =  (double) ir.getSampleData(is);
								String dd = ir.getXref().getId();
								TissueResult tr = new TissueResult(dd,value);								
								data.get(is.getName().trim()).add(tr);
							}
						}
					}					
				}
			}
		} catch (DataException | IDMapperException e) {
			e.printStackTrace();
		}

		Vector<Double> vP = new Vector<Double>();
		Vector<Double> vM = new Vector<Double>();

		for(Entry<String, List<TissueResult>> entry : data.entrySet()) {
			int length = entry.getValue().size();
			double i = 0;
			double tmp = 0;
			for( TissueResult tr: entry.getValue()){
				if (tr.getExpression() >= (2/ Math.log10(2)) ){
					i++;

				}
				tmp += tr.getExpression();
			}
			vP.add(i/length*100);
			vM.add(tmp/length);		
		}
		dtm.addCollum(vP, 2);
		dtm.addCollum(vM, 3);		
	}
	public void createContent(){		
		ArrayList<String> listOfTissues = new ArrayList<String>() ;		
		try {
			List<? extends ISample> names = standaloneEngine.getGexManager().
					getCurrentGex().getOrderedSamples();			
			for ( ISample iSample : names){
				if ( !iSample.getName().trim().equals("Gene Name")){
					listOfTissues.add(iSample.getName().trim());
				}
			}
		} catch (DataException e){
			e.printStackTrace();
		}
		vT = new Vector<String>();
		vB = new Vector<Boolean>();
		Vector<Double> tmp = new Vector<Double>();
		for (String tissue : listOfTissues){					
			vT.add(tissue);
			vB.add(new Boolean(false));
			tmp.add(0.0);
		}

		dtm.addCollum(vT, 0);
		dtm.addCollum(vB, 1);
		dtm.addCollum(tmp, 2);
		dtm.addCollum(tmp, 3);
	}

	@Override
	public void gexManagerEvent(GexManagerEvent e)
	{
		switch (e.getType())
		{
		case GexManagerEvent.CONNECTION_OPENED:
			createContent();
			break;
		case GexManagerEvent.CONNECTION_CLOSED:
			//panel.removeAll();
			break;
		default:
			assert (false); // Shouldn't occur.
		}
	}

//	public class TissueResult {
//		String gene;
//		double expression;
//		public TissueResult(String gene, double expression){
//			this.gene=gene;
//			this.expression=expression;
//		}
//		public String toString(){
//			return gene+" "+String.valueOf(expression);			
//		}
//		public String getGene() {
//			return gene;
//		}
//		public double getExpression() {
//			return expression;
//		}
//		public void setGene(String gene) {
//			this.gene = gene;
//		}
//		public void setExpression(double expression) {
//			this.expression = expression;
//		}
//	}

	class MyTableModel extends AbstractTableModel {
		public String[] m_colNames = { "Tissues", "Vizu", "Percent", "Mean" };

		public Class[] m_colTypes = 
			{ String.class, Boolean.class, Double.class, Double.class};

		public Vector<String> name;
		public Vector<Boolean> vizu;
		public Vector<Double> perc;
		public Vector<Double> mean;

		public MyTableModel() {
			super();
			name = new Vector<String>();
			vizu = new Vector<Boolean>();
			perc = new Vector<Double>();
			mean = new Vector<Double>();
		}


		public void addCollum(Vector data, int col){
			switch (col) {
			case 0:
				this.name = data;
				break;
			case 1:
				this.vizu = data;
				break;

			case 2:
				this.perc = data;
				break;

			case 3:
				this.mean = data;
				break;
			}
			fireTableDataChanged();
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 1) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int getColumnCount() {			
			return m_colNames.length;
		}

		@Override
		public int getRowCount() {

			return name.size();
		}

		public void setValueAt(Object value, int row, int col) {
			switch (col) {
			case 0:
				name.set(row, (String) value);
				break;
			case 1:
				vizu.set(row, (Boolean) value);
				break;
			case 2:
				perc.set(row, (Double) value);
				break;
			case 3:
				mean.set(row, (Double) value);
				break;
			}
			fireTableCellUpdated(row, col);
		}

		public String getColumnName(int col) {
			return m_colNames[col];
		}

		public Class getColumnClass(int col) {
			return m_colTypes[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return name.elementAt(row);
			case 1:
				return vizu.elementAt(row);
			case 2:
				return perc.elementAt(row);
			case 3:
				return mean.elementAt(row);
			}

			return new String();
		}
	}
	@Override
	public void tableChanged(TableModelEvent arg0) {
		VisualizationManager visMgr = standaloneEngine.getVisualizationManager(); 
		ColorSetManager csmgr = visMgr.getColorSetManager();
		ColorSet cs = new ColorSet(csmgr);
		csmgr.addColorSet(cs);

		ColorGradient gradient = new ColorGradient();
		cs.setGradient(gradient);

		double lowerbound = 0; 
		double upperbound = 14;

		gradient.addColorValuePair(new ColorValuePair(Color.RED, lowerbound));
		gradient.addColorValuePair(new ColorValuePair(Color.YELLOW, upperbound));

		Visualization v = new Visualization("auto-generated");

		ColorByExpression cby = new ColorByExpression(standaloneEngine.getGexManager(), 
				standaloneEngine.getVisualizationManager().getColorSetManager());
		DataInterface gex = standaloneEngine.getGexManager().getCurrentGex();

		Map<Integer, ? extends ISample> samplesMap = null;
		try {
			samplesMap = gex.getSamples();
		} catch (DataException e1) {
			e1.printStackTrace();
		}
		for(Entry<Integer, ? extends ISample> entry : samplesMap.entrySet()) {
			ISample valeur = entry.getValue();
			String tissues = valeur.getName().trim();			
			for (int i=0; i<vT.size();i++){
				if ( vB.get(i) && vT.get(i).equals(tissues) ){
					cby.addUseSample(valeur);
				}
			}
		}
		cby.setSingleColorSet(cs);
		v.addMethod(cby);

		DataNodeLabel dnl = new DataNodeLabel();
		v.addMethod(dnl);

		visMgr.removeVisualization(v);
		visMgr.addVisualization(v);
		visMgr.setActiveVisualization(v);

	}
//	public void stat(){
//
//		List<Xref> xrefs = standaloneEngine.getSwingEngine().getEngine().getActivePathway().getDataNodeXrefs();
//		Set<Xref> setRefs = new HashSet<Xref>(xrefs);
//
//		DataInterface gex = standaloneEngine.getGexManager().getCurrentGex();
//		CachedData cache = standaloneEngine.getGexManager().getCachedData();
//		Collection<? extends ISample> names = null;
//
//
//		Map<String,List<TissueResult>> data = 
//				new TreeMap<String,List<TissueResult>>();
//		try {			
//			names = gex.getOrderedSamples();
//
//			for ( ISample is : names){
//				if ( !is.getName().equals(" Gene Name")){
//					data.put(is.getName().trim(),new ArrayList<TissueResult>());
//				}
//			}			
//			for (Xref ref : setRefs){
//				List<? extends IRow> pwData = cache.syncGet(ref);
//				if (!pwData.isEmpty()){
//					for ( ISample is : names) {
//						for (IRow ir : pwData){
//							if ( !is.getName().equals(" Gene Name")){
//								Double value =  (double) ir.getSampleData(is);
//								String dd = ir.getXref().getId();
//								TissueResult tr = new TissueResult(dd,value);								
//								data.get(is.getName().trim()).add(tr);
//							}
//						}
//					}					
//				}
//			}
//		} catch (DataException | IDMapperException e) {
//			e.printStackTrace();
//		}
//
//		Vector<Double> vP = new Vector<Double>();
//		Vector<Double> vM = new Vector<Double>();
//
//		for(Entry<String, List<TissueResult>> entry : data.entrySet()) {
//			System.out.println(entry.getKey()+"  "+entry.getValue());
//			int length = entry.getValue().size();
//			double i = 0;
//			double tmp = 0;
//			for( TissueResult tr: entry.getValue()){
//				if (tr.expression >= (2/ Math.log10(2)) ){
//					i++;
//
//				}
//				tmp += tr.expression;
//			}
//			vP.add(i/length*100);
//			vM.add(tmp/length);
//
//			System.out.println(i/length*100);			
//		}
//		dtm.addCollum(vP, 2);
//		dtm.addCollum(vM, 3);		
//	}
//
////	class pathListener implements ApplicationEventListener{
////
////		@Override
////		public void applicationEvent(ApplicationEvent e) {
////			if(e.getType() == ApplicationEvent.Type.PATHWAY_OPENED 
////					& standaloneEngine.getSwingEngine().getGdbManager().getCurrentGdb().isConnected()){				
////					stat();		
////			}
////		}
////	}
//	@Override
//	public void applicationEvent(ApplicationEvent e) {
//		if(e.getType() == ApplicationEvent.Type.PATHWAY_OPENED 
//				& standaloneEngine.getSwingEngine().getGdbManager().getCurrentGdb().isConnected()){				
//				stat();		
//		}		
//	}
}


