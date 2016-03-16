// TissueAnalyzer plugin for Pathvisio
// Copyright 2014 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.pathvisio.tissueanalyzer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationManager;
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
	implements ActionListener,GexManagerListener, TableModelListener,ApplicationEventListener {

	private PvDesktop standaloneEngine;
	private Vector<String> vT;
	private Vector<Boolean> vB;
	private LegendPanel legendPane;
	private JButton calcul;
	private MyTableModel dtm;
	private JTable table;
	private JLabel average;
	private JLabel tissue;
	private JLabel measured;
	private JLabel total;

	public TissueSidePanel(PvDesktop standaloneEngine){
		this.standaloneEngine = standaloneEngine;
		this.standaloneEngine.getGexManager().addListener(this);
		this.standaloneEngine.getSwingEngine().getEngine().addApplicationEventListener(this);
		
		JLabel labelAverage = new JLabel("Overall average tissues expression:");
		JLabel labelMeasured = new JLabel("Measured datanodes:");
		JLabel labelTotal = new JLabel("Total number of datanodes:");
		JLabel labelTissues = new JLabel("Number of tissue in the dataset:");
		
		average = 	new JLabel("0.0");
		tissue = new JLabel("0");
		measured = new JLabel("0");
		total = new JLabel("0");
		
		legendPane = new LegendPanel(standaloneEngine.getVisualizationManager());
		legendPane.setPreferredSize(new Dimension(100,100));
		legendPane.setBorder(null);	
		legendPane.setBackground(this.getBackground());
		
		calcul = new JButton("Calculate");
		calcul.addActionListener(this);

		dtm = new MyTableModel();	
		table = new JTable(dtm);
		table.getModel().addTableModelListener(this);
		table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setBackground(this.getBackground());
        
//        table.getColumn("Overall tissues expression").setPreferredWidth(200);
        table.getColumn("Median tissue expression").setPreferredWidth(200);
        JScrollPane sPane = new JScrollPane(table);
        sPane.setBackground(this.getBackground());
        
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = c.LINE_START;
        add(labelTissues, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = c.LINE_START;
        add(tissue, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = c.LINE_START;
        add(labelAverage, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = c.LINE_START;
        add(average, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = c.LINE_START;
        add(labelMeasured, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = c.LINE_START;
        add(measured, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = c.LINE_START;
        add(labelTotal, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.anchor = c.LINE_START;
        add(total, c);
        
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2; 
        add(sPane, c);
        
        c = new GridBagConstraints();
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 5;
        add(legendPane, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 0.5;
        c.anchor = c.PAGE_START;
        add(calcul, c);       
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {	
		if (standaloneEngine.getSwingEngine().getEngine().getActivePathway()==null){
//			JOptionPane.showMessageDialog(null, "No pathway opened");
			JOptionPane.showMessageDialog(null, "No pathway opened", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else if (standaloneEngine.getGexManager().getCurrentGex()==null){
			JOptionPane.showMessageDialog(null, "No expression dataset selected");
		}
		else  calcul();
	}

	public void calcul() {	
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
//					System.out.println(is.getName().trim());
					data.put(is.getName().trim(),new ArrayList<TissueResult>());
				}
			}			
			for (Xref ref : setRefs){
				List<? extends IRow> pwData = cache.syncGet(ref);
				if (!pwData.isEmpty()){
					for ( ISample is : names) {
						for (IRow ir : pwData){
							if ( !is.getName().equals(" Gene Name")){
								Double value =  (Double) ir.getSampleData(is);
								String dd = ir.getXref().getId();
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

		Vector<Double> vD = new Vector<Double>();	
		double average = 0.0;
		int length = 0;
		
		for(Entry<String, List<TissueResult>> entry : data.entrySet()) {
//			int length = entry.getValue().size();
			length = entry.getValue().size();
			int i = 0;
			ArrayList<Double> foo = new ArrayList<Double>();
			for( TissueResult tr: entry.getValue()){
//				if (tr.getExpression() >= (2/ Math.log10(2)) ){
//					i++;
//				}
//				tmp += tr.getExpression();
				foo.add(tr.getExpression());
				i++;
			}
//			System.out.println(entry.getKey()+" "+i);
			double median;			
			Collections.sort(foo);
			int s = foo.size() ;
			if (s>0){
				if (s% 2 == 0){				
					median = ((double)foo.get(s/2) + (double)foo.get( (s/2)-1))/2;
				}
				else
					median = foo.get(s/2);
				median = Math.round(median*100.0)/100.0;
			}
			else{
				median = 0.0;
			}
			vD.add(median);
			average += median;
		}
		
		Double vA  = average/data.size();
		average(vA);
		
		String s = String.valueOf(data.size());
		tissue.setText(s);
		s = String.valueOf(length);
		measured.setText(s);
		s = String.valueOf(setRefs.size());
		total.setText(s);
		
		dtm.addCollum(vD, 2);
		dtm.addCollum(vA, 3);
	}
	
	private void createContent(){
		table.setBackground(Color.WHITE);
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
//		dtm.addCollum(0.0, 3);
//		dtm.addCollum(tmp, 4);
	}

	@Override
	public void gexManagerEvent(GexManagerEvent e)
	{
		switch (e.getType())
		{
		case GexManagerEvent.CONNECTION_OPENED:
			VisualizationManager visMgr = standaloneEngine.getVisualizationManager();
			Visualization v = new Visualization("TissueAnalyzer");
			visMgr.removeVisualization(v);
			createContent();
			break;
		case GexManagerEvent.CONNECTION_CLOSED:
			break;
		default:
			assert (false);
		}
	}
	
	public void average(Double value){
		NumberFormat formatter = new DecimalFormat("#0.00");  
		String s = String.valueOf(formatter.format(value));
		average.setText(s);		
	}
	
	
	private class MyTableModel extends AbstractTableModel {
//		private String[] m_colNames = { "Tissues", "Visualisation", "Median", "Overall tissues expression",
//				"Percentage" };
//		, "Overall tissues expression"
//		private Class[] m_colTypes = 
//			{ String.class, Boolean.class, Double.class, Double.class, Double.class};
		private String[] m_colNames = { "Tissues", "Visualisation", "Median tissue expression"};

		private Class[] m_colTypes = 
			{ String.class, Boolean.class, Double.class};

		private Vector<String> name;
		private Vector<Boolean> vizu;
//		private Vector<Double> perc;
//		private Double average;
		private Vector<Double> median;

		public MyTableModel() {
			super();
			name = new Vector<String>();
			vizu = new Vector<Boolean>();
			median = new Vector<Double>();
//			average = 0.0;
//			median =  new Vector<Double>();
		}


		private void addCollum(Object data, int col){
			switch (col) {
			case 0:
				this.name = (Vector<String>) data;
				break;
			case 1:
				this.vizu = (Vector<Boolean>) data;
				break;

			case 2:
				this.median = (Vector<Double>) data;
				break;
			case 3:
				average((Double) data);
				break;
//			case 3:
//				this.average = (Double) data;
//				break;			
				//			case 4:
				//				this.perc = data;
				//				break;
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
				median.set(row, (Double) value);
				break;
//			case 3:
//				average((Double) value);
//			case 3:
//				average = (Double) value;
//				break;
//			case 4:
//				perc.set(row, (Double) value);
//				break;
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
				return median.elementAt(row);
//			case 3:
//				String s = average.getText();
//				return Double.parseDouble(s);				
//			case 3:
//				return average;
//			case 4:
//				return perc.elementAt(row);
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

		double lowerbound = 3; 
		double upperbound = 10;

		gradient.addColorValuePair(new ColorValuePair(new Color(218, 242, 249), lowerbound));
		gradient.addColorValuePair(new ColorValuePair(new Color(0, 0, 255), upperbound));

		Visualization v = new Visualization("TissueAnalyzer");

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

	@Override
	public void applicationEvent(ApplicationEvent e) {
		// TODO Auto-generated method stub
		if (standaloneEngine.getGexManager().isConnected()){
			VisualizationManager visMgr = standaloneEngine.getVisualizationManager();
			Visualization v = new Visualization("TissueAnalyzer");
			visMgr.removeVisualization(v);
			
			average.setText("0.0");
			tissue.setText("0");
			measured.setText("0");
			total.setText("0");
			createContent();
		}
	}
}


