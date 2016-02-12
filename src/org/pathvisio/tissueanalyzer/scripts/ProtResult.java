package org.pathvisio.tissueanalyzer.scripts;


public class ProtResult {

	private String gene;
	private String expression;

	public ProtResult(String gene, String expression){
		this.gene=gene;
		this.expression=expression;
	}
	public String toString(){
		return gene+" "+String.valueOf(expression);			
	}
	public String getGene() {
		return gene;
	}
	public String getExpression() {
		return expression;
	}
	public void setGene(String gene) {
		this.gene = gene;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
}
