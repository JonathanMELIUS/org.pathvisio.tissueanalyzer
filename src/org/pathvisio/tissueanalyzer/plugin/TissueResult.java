package org.pathvisio.tissueanalyzer.plugin;

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
}
