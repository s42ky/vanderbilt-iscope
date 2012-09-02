package net.sf.jsqlparser.expression;

public class IntervalExpression implements Expression {
	Expression num;
	private String units;
	
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public IntervalExpression() {}
	
	public IntervalExpression(Expression setnum, String setunits) {
		num = setnum;
		units = setunits;
	}
	
	
	/**
	 * @return the num
	 */
	public Expression getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(Expression num) {
		this.num = num;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	public String toString() {
		return "{ti: INTERVAL "+num.toString()+" "+units+"}";		
	}	
}
