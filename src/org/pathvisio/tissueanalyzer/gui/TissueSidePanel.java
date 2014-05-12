package org.pathvisio.tissueanalyzer.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.tissueanalyzer.utils.ObserverSidePanel;
import org.pathvisio.visualization.plugins.ColorByExpression;
import org.pathvisio.visualization.plugins.DataNodeLabel;
import org.pathvisio.visualization.plugins.LegendPanel;

/**
* Create a side panel to change dynamically the list of visualized tissues.
* @author Jonathan Melius
* @see ObserverSidePanel
*/

public class TissueSidePanel extends JPanel implements ObserverSidePanel{
	//private JComboBox<String> tissueCB;
	private JPanel panel;
	private JScrollPane scroll;
	private PvDesktop standaloneEngine;
	private ArrayList<JCheckBox> cbList;
	private LegendPanel legendPane;

	public TissueSidePanel(PvDesktop standaloneEngine){

		this.standaloneEngine = standaloneEngine;
		legendPane = new LegendPanel(standaloneEngine.getVisualizationManager());
		legendPane.setSize(200, 200);
		legendPane.setBorder(null);
		panel = new JPanel();
		panel.setLayout (new GridLayout(0,1));
		add(legendPane);
		add(panel);
	}

	@Override
	public void update(ArrayList<String> progress,final ArrayList<String> selectedTissues) {
		panel.removeAll();
		//panel.add (new JLabel ("Hello SideBar"), BorderLayout.CENTER);	
		//System.out.println("obs--"+progress);
		//DefaultComboBoxModel model = new DefaultComboBoxModel(progress.toArray());
		//tissueCB.setModel(model);
		cbList = new ArrayList<JCheckBox>();
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)  {
				VisualizationManager visMgr = standaloneEngine.getVisualizationManager(); 
				ColorSetManager csmgr = visMgr.getColorSetManager();
				ColorSet cs = new ColorSet(csmgr);
				csmgr.addColorSet(cs);

				ColorGradient gradient = new ColorGradient();
				cs.setGradient(gradient);

				double lowerbound = 0; 
				double upperbound = 250;

				gradient.addColorValuePair(new ColorValuePair(Color.BLUE, lowerbound));
				gradient.addColorValuePair(new ColorValuePair(Color.RED, upperbound));

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
					for (JCheckBox cb : cbList){
						if ( cb.isSelected() && cb.getActionCommand().equals(tissues) ){
							//System.out.println(e.getID()+"found------------"+valeur.getName());
							cby.addUseSample(valeur);
						}
					}
					/*
					if ( e.getActionCommand().equals(tissues)){
						System.out.println(e.getID()+"found------------"+valeur.getName());
						cby.addUseSample(valeur);
					}
					*/
				}
				cby.setSingleColorSet(cs);
				v.addMethod(cby);
				
				DataNodeLabel dnl = new DataNodeLabel();
				v.addMethod(dnl);
				
				visMgr.removeVisualization(v);
				visMgr.addVisualization(v);
				visMgr.setActiveVisualization(v);
			}
		};
		for (String tissue : progress){
			JCheckBox cb = new JCheckBox(tissue);
			cbList.add(cb);
			cb.setActionCommand(tissue);
			if (selectedTissues.contains(tissue)){
				cb.setSelected(true);
			}
			cb.addActionListener(listener);
			panel.add(cb);
			panel.revalidate();
			panel.repaint();			
		}
	}
}
