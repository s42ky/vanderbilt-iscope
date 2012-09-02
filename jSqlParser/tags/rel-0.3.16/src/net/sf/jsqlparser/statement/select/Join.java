/* ================================================================
 * JSQLParser : java based sql parser 
 * ================================================================
 *
 * Project Info:  http://jsqlparser.sourceforge.net
 * Project Lead:  Leonardo Francalanci (leoonardoo@yahoo.it);
 *
 * (C) Copyright 2004, by Leonardo Francalanci
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
 
package net.sf.jsqlparser.statement.select;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;

/**
 * A join clause
 */
public class Join {
	private boolean outer = false;
	private FromItem rightItem;
	private Expression onExpression;
	private List usingColumns; 
	
	/**
	 * Whether is a "LEFT OUTER" join or an "INNER" join
	 * @return true if is a "LEFT OUTER" join, false if is an "INNER" join
	 */
	public boolean isOuter() {
		return outer;
	}

	public void setOuter(boolean b) {
		outer = b;
	}

	/**
	 * Returns the right item of the join
	 */
	public FromItem getRightItem() {
		return rightItem;
	}

	public void setRightItem(FromItem item) {
		rightItem = item;
	}

	/**
	 * Returns the "ON" expression (if any)
	 */
	public Expression getOnExpression() {
		return onExpression;
	}

	public void setOnExpression(Expression expression) {
		onExpression = expression;
	}

	/**
	 * Returns the "USING" list of {@link net.sf.jsqlparser.schema.Column}s (if any)
	 */
	public List getUsingColumns() {
		return usingColumns;
	}

	public void setUsingColumns(List list) {
		usingColumns = list;
	}

	public String toString() {
		return ((isOuter())?"LEFT OUTER":"INNER")+" JOIN " +
		rightItem+
		((onExpression!=null)?" ON "+onExpression+"":"")+
		PlainSelect.getFormatedList(usingColumns, "USING", true, true);
	}
}
