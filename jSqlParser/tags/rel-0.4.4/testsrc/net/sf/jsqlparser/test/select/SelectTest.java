package net.sf.jsqlparser.test.select;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ColumnIndex;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class SelectTest extends TestCase {
	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public SelectTest(String arg0) {
		super(arg0);
	}

	public void testLimit() throws JSQLParserException {
		String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(3, ((PlainSelect) select.getSelectBody()).getLimit().getOffset());
		assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());

		// toString uses standard syntax
		statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
		assertEquals(statement, ""+select);

		statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(0, ((PlainSelect) select.getSelectBody()).getLimit().getRowCount());
		assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());
		assertEquals(statement, ""+select);

		statement =
			"(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
				+ "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
		select = (Select) parserManager.parse(new StringReader(statement));
		Union union = (Union) select.getSelectBody();
		assertEquals(3, union.getLimit().getOffset());
		assertEquals(4, union.getLimit().getRowCount());

		// toString uses standard syntax
		statement =
			"(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
				+ "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
		assertEquals(statement, ""+select);
	}

	public void testTop() throws JSQLParserException {
		String statement = "SELECT TOP 3 * FROM mytable WHERE mytable.col = 9";

		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(3, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());
		
		statement = "select top 5 foo from bar";
		select = (Select) parserManager.parse(new StringReader(statement));
		assertEquals(5, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());
		

	}

	
	public void testSelectItems() throws JSQLParserException {
		String statement =
			"SELECT myid AS MYID, mycol, tab.*, schema.tab.*, mytab.mycol2, myschema.mytab.mycol, myschema.mytab.* FROM mytable WHERE mytable.col = 9";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();

		assertEquals("MYID", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias());
		assertEquals("mycol", ((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
		assertEquals("tab", ((AllTableColumns) plainSelect.getSelectItems().get(2)).getTable().getName());
		assertEquals("schema", ((AllTableColumns) plainSelect.getSelectItems().get(3)).getTable().getSchemaName());
		assertEquals("schema.tab", ((AllTableColumns) plainSelect.getSelectItems().get(3)).getTable().getWholeTableName());
		assertEquals(
			"mytab.mycol2",
			((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(4)).getExpression()).getWholeColumnName());
		assertEquals(
			"myschema.mytab.mycol",
			((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(5)).getExpression()).getWholeColumnName());
		assertEquals("myschema.mytab", ((AllTableColumns) plainSelect.getSelectItems().get(6)).getTable().getWholeTableName());
		assertEquals(statement, ""+plainSelect);

		statement = "SELECT myid AS MYID, (SELECT MAX(ID) AS myid2 FROM mytable2) AS myalias FROM mytable WHERE mytable.col = 9";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("myalias", ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getAlias());
		assertEquals(statement, ""+plainSelect);

		statement = "SELECT (myid + myid2) AS MYID FROM mytable WHERE mytable.col = 9";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("MYID", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias());
		assertEquals(statement, ""+plainSelect);
	}

	public void testUnion() throws JSQLParserException {
		String statement =
			"SELECT * FROM mytable WHERE mytable.col = 9 UNION "
				+ "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION "
				+ "SELECT * FROM mytable2 LIMIT 3,4";
		
		Union union = (Union) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(3, union.getPlainSelects().size());
		assertEquals("mytable", ((Table) ((PlainSelect) union.getPlainSelects().get(0)).getFromItems().get(0)).getName());
		assertEquals("mytable3", ((Table) ((PlainSelect) union.getPlainSelects().get(1)).getFromItems().get(0)).getName());
		assertEquals("mytable2", ((Table) ((PlainSelect) union.getPlainSelects().get(2)).getFromItems().get(0)).getName());
		assertEquals(3, ((PlainSelect) union.getPlainSelects().get(2)).getLimit().getOffset());
		
		//use brakets for toString
		//use standard limit syntax
		String statementToString =
			"(SELECT * FROM mytable WHERE mytable.col = 9) UNION "
				+ "(SELECT * FROM mytable3 WHERE mytable3.col = ?) UNION "
				+ "(SELECT * FROM mytable2 LIMIT 4 OFFSET 3)";
		assertEquals(statementToString, ""+union);
	}

	public void testDistinct() throws JSQLParserException {
		String statement = "SELECT DISTINCT ON (myid) myid, mycol FROM mytable WHERE mytable.col = 9";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(
			"myid",
			((Column) ((SelectExpressionItem) plainSelect.getDistinct().getOnSelectItems().get(0)).getExpression()).getColumnName());
		assertEquals("mycol", ((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());
	}

	public void testFrom() throws JSQLParserException {
		String statement =
			"SELECT * FROM mytable as mytable0, mytable1 alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";
		String statementToString =
			"SELECT * FROM mytable as mytable0, mytable1 as alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";

		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(4, plainSelect.getFromItems().size());
		assertEquals("mytable0", ((Table) plainSelect.getFromItems().get(0)).getAlias());
		assertEquals("alias_tab1", ((Table) plainSelect.getFromItems().get(1)).getAlias());
		assertEquals("alias_tab2", ((Table) plainSelect.getFromItems().get(2)).getAlias());
		assertEquals("mytable4", ((SubSelect) plainSelect.getFromItems().get(3)).getAlias());
		assertEquals(statementToString.toUpperCase(), plainSelect.toString().toUpperCase());
		
	}

	public void testJoin() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 LEFT outer JOIN tab2 ON tab1.id = tab2.id";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(1, plainSelect.getJoins().size());
		assertEquals("tab2", ((Table) ((Join) plainSelect.getJoins().get(0)).getRightItem()).getWholeTableName());
		assertEquals(
			"tab1.id",
			((Column) ((EqualsTo) ((Join) plainSelect.getJoins().get(0)).getOnExpression()).getLeftExpression()).getWholeColumnName());
		assertTrue(((Join) plainSelect.getJoins().get(0)).isOuter());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

		statement = "SELECT * FROM tab1 LEFT outer JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(2, plainSelect.getJoins().size());
		assertEquals("tab3", ((Table) ((Join) plainSelect.getJoins().get(1)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(1)).isOuter());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

		statement = "SELECT * FROM tab1 LEFT outer JOIN tab2 ON tab1.id = tab2.id JOIN tab3";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(2, plainSelect.getJoins().size());
		assertEquals("tab3", ((Table) ((Join) plainSelect.getJoins().get(1)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(1)).isOuter());
		
		// implicit INNER 
		statement = "SELECT * FROM tab1 LEFT outer JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

		statement = "SELECT * FROM tab1 INNER JOIN tab2 USING (id, id2)";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(1, plainSelect.getJoins().size());
		assertEquals("tab2", ((Table) ((Join) plainSelect.getJoins().get(0)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(0)).isOuter());
		assertEquals(2, ((Join) plainSelect.getJoins().get(0)).getUsingColumns().size());
		assertEquals("id2", ((Column) ((Join) plainSelect.getJoins().get(0)).getUsingColumns().get(1)).getWholeColumnName());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

	}

	public void testFunctions() throws JSQLParserException {
		String statement = "SELECT MAX(id) as max FROM mytable WHERE mytable.col = 9";
		PlainSelect select = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("max", ((SelectExpressionItem) select.getSelectItems().get(0)).getAlias());
		assertEquals(statement.toUpperCase(), select.toString().toUpperCase());

		statement = "SELECT MAX(id), AVG(pro) as myavg FROM mytable WHERE mytable.col = 9 GROUP BY pro";
		select = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("myavg", ((SelectExpressionItem) select.getSelectItems().get(1)).getAlias());
		assertEquals(statement.toUpperCase(), select.toString().toUpperCase());

		statement = "SELECT MAX(a, b, c), COUNT(*), D FROM tab1 GROUP BY D";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		Function fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertEquals("MAX", fun.getName());
		assertEquals("b", ((Column)fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).isAllColumns());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

		statement = "SELECT {fn MAX(a, b, c)}, COUNT(*), D FROM tab1 GROUP BY D";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertTrue(fun.isEscaped());
		assertEquals("MAX", fun.getName());
		assertEquals("b", ((Column)fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).isAllColumns());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

		statement = "SELECT ab.MAX(a, b, c), cd.COUNT(*), D FROM tab1 GROUP BY D";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertEquals("ab.MAX", fun.getName());
		assertEquals("b", ((Column)fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression();
		assertEquals("cd.COUNT", fun.getName());
		assertTrue(fun.isAllColumns());
		assertEquals(statement.toUpperCase(), plainSelect.toString().toUpperCase());

	}

	public void testWhere() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE ";
		String where = "(a+b+c/d+e*f)*(a/b*(a+b)) > ?";
		String whereToString = "(a + b + c / d + e * f) * (a / b * (a + b)) > ?";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + where))).getSelectBody();
		assertTrue(plainSelect.getWhere() instanceof GreaterThan);
		assertTrue(((GreaterThan) plainSelect.getWhere()).getLeftExpression() instanceof Multiplication);
		assertEquals(statement+whereToString, ""+plainSelect);

		ExpressionDeParser expressionDeParser = new ExpressionDeParser();
		StringBuffer stringBuffer = new StringBuffer();
		expressionDeParser.setBuffer(stringBuffer);
		plainSelect.getWhere().accept(expressionDeParser);
		assertEquals(where, stringBuffer.toString());

		where = "(7*s+9/3) NOT BETWEEN 3 AND ?";
		whereToString = "(7 * s + 9 / 3) NOT BETWEEN 3 AND ?";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + where))).getSelectBody();

		stringBuffer = new StringBuffer();
		expressionDeParser.setBuffer(stringBuffer);
		plainSelect.getWhere().accept(expressionDeParser);

		assertEquals(where, stringBuffer.toString());
		assertEquals(statement+whereToString, ""+plainSelect);

		where = "a/b NOT IN (?, 's''adf', 234.2)";
		whereToString = "a / b NOT IN (?, 's''adf', 234.2)";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + where))).getSelectBody();

		stringBuffer = new StringBuffer();
		expressionDeParser.setBuffer(stringBuffer);
		plainSelect.getWhere().accept(expressionDeParser);

		assertEquals(where, stringBuffer.toString());
		assertEquals(statement+whereToString, ""+plainSelect);

		where = " NOT 0 = 0";
		whereToString = "NOT 0 = 0";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + where))).getSelectBody();
		
		stringBuffer = new StringBuffer();
		expressionDeParser.setBuffer(stringBuffer);
		plainSelect.getWhere().accept(expressionDeParser);

		assertEquals(where, stringBuffer.toString());
		assertEquals(statement+whereToString, ""+plainSelect);
	}

	public void testGroupBy() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(1, plainSelect.getGroupByColumnReferences().size());
		assertEquals("tab1.b", ((Column) plainSelect.getGroupByColumnReferences().get(0)).getWholeColumnName());
		assertEquals(statement, ""+plainSelect);

		statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY 2, 3";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(2, plainSelect.getGroupByColumnReferences().size());
		assertEquals(2, ((ColumnIndex) plainSelect.getGroupByColumnReferences().get(0)).getIndex());
		assertEquals(3, ((ColumnIndex) plainSelect.getGroupByColumnReferences().get(1)).getIndex());
		assertEquals(statement, ""+plainSelect);
	}

	public void testHaving() throws JSQLParserException {
		String statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 GROUP BY tab1.b HAVING MAX(tab1.b) > 56";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertTrue(plainSelect.getHaving() instanceof GreaterThan);
		assertEquals(statement, ""+plainSelect);

		statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 HAVING MAX(tab1.b) IN (56, 32, 3, ?)";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertTrue(plainSelect.getHaving() instanceof InExpression);
		assertEquals(statement, ""+plainSelect);
	}

	public void testExists() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE EXISTS (SELECT * FROM tab2)";
		Statement parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);
	}

	public void testOrderBy() throws JSQLParserException {
		//TODO: should there be a DESC marker in the OrderByElement class?
		String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
		String statementToString = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(2, plainSelect.getOrderByElements().size());
		assertEquals("tab1.a", ((Column) ((OrderByElement) plainSelect.getOrderByElements().get(0)).getColumnReference()).getWholeColumnName());
		assertEquals("b", ((Column) ((OrderByElement) plainSelect.getOrderByElements().get(1)).getColumnReference()).getColumnName());
		assertTrue(((OrderByElement) plainSelect.getOrderByElements().get(1)).isAsc());
		assertFalse(((OrderByElement) plainSelect.getOrderByElements().get(0)).isAsc());
		assertEquals(statementToString, ""+plainSelect);
		
		ExpressionDeParser expressionDeParser = new ExpressionDeParser();
		StringBuffer stringBuffer = new StringBuffer();
		SelectDeParser deParser = new SelectDeParser(expressionDeParser, stringBuffer);
		expressionDeParser.setSelectVisitor(deParser);
		expressionDeParser.setBuffer(stringBuffer);
		plainSelect.accept(deParser);
		assertEquals(statement, stringBuffer.toString());
		
		statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a, 2";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals(2, plainSelect.getOrderByElements().size());
		assertEquals("a", ((Column) ((OrderByElement) plainSelect.getOrderByElements().get(0)).getColumnReference()).getColumnName());
		assertEquals(2, ((ColumnIndex) ((OrderByElement) plainSelect.getOrderByElements().get(1)).getColumnReference()).getIndex());
		assertEquals(statement, ""+plainSelect);
	}

	public void testTimestamp() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > {ts '2004-04-30 04:05:34.56'}";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("2004-04-30 04:05:34.56", ((TimestampValue)((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue().toString());
		assertEquals(statement, ""+plainSelect);
	}

	public void testTime() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > {t '04:05:34'}";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("04:05:34", (((TimeValue)((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertEquals(statement, ""+plainSelect);
	}

	public void testCase() throws JSQLParserException {
		String statement = "SELECT a, CASE b WHEN 1 THEN 2 END FROM tab1";
		Statement parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);

		statement = "SELECT a, (CASE WHEN (a > 2) THEN 3 END) AS b FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);

		statement = "SELECT a, (CASE WHEN a > 2 THEN 3 ELSE 4 END) AS b FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);

		
		statement = "SELECT a, (CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END) FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);
		
		statement = "SELECT a, (CASE " +
				"WHEN b > 1 THEN 'BBB' " +
				"WHEN a = 3 THEN 'AAA' " +
				"END) FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);
		
		statement = "SELECT a, (CASE " +
		"WHEN b > 1 THEN 'BBB' " +
		"WHEN a = 3 THEN 'AAA' " +
		"END) FROM tab1 " +
		"WHERE c = (CASE " +
		"WHEN d <> 3 THEN 5 " +
		"ELSE 10 " +
		"END)";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);

		statement = "SELECT a, CASE a " +
		"WHEN 'b' THEN 'BBB' " +
		"WHEN 'a' THEN 'AAA' " +
		"END AS b FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);

		statement = "SELECT a, (CASE " +
		"WHEN (CASE a WHEN 1 THEN 10 ELSE 20 END) > 15 THEN 'BBB' " +
		"WHEN (SELECT c FROM tab2 WHERE d = 2) = 3 THEN 'AAA' " +
		"END) FROM tab1";
		parsed = parserManager.parse(new StringReader(statement));
		//System.out.println(""+statement);
		//System.out.println(""+parsed);
		assertEquals(statement, ""+parsed);
		
		
	}

	public void testReplaceAsFunction() throws JSQLParserException {
		String statement = "SELECT REPLACE(a, 'b', c) FROM tab1";
		Statement parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);
	}

	public void testLike() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a LIKE 'test'";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("test", (((StringValue)((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertEquals(statement, ""+plainSelect);

		statement = "SELECT * FROM tab1 WHERE a LIKE 'test' ESCAPE 'test2'";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
		assertEquals("test", (((StringValue)((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertEquals("test2", (((LikeExpression) plainSelect.getWhere()).getEscape()));
		assertEquals(statement, ""+plainSelect);
	}

	public void testSelectOrderHaving() throws JSQLParserException {
		String statement = "SELECT units, count(units) AS num FROM currency GROUP BY units HAVING count(units) > 1 ORDER BY num";
		Statement parsed = parserManager.parse(new StringReader(statement));
		assertEquals(statement, ""+parsed);
	}

	public void testDouble() throws JSQLParserException {
		String statement = "SELECT 1e2, * FROM mytable WHERE mytable.col = 9";
		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1e2, ((DoubleValue)((SelectExpressionItem)((PlainSelect) select.getSelectBody()).getSelectItems().get(0)).getExpression()).getValue(), 0);

		statement = "SELECT * FROM mytable WHERE mytable.col = 1.e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1e2, ((DoubleValue)((BinaryExpression)((PlainSelect) select.getSelectBody()).getWhere()).getRightExpression()).getValue(), 0);

		statement = "SELECT * FROM mytable WHERE mytable.col = 1.2e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1.2e2, ((DoubleValue)((BinaryExpression)((PlainSelect) select.getSelectBody()).getWhere()).getRightExpression()).getValue(), 0);

		statement = "SELECT * FROM mytable WHERE mytable.col = 2e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(2e2, ((DoubleValue)((BinaryExpression)((PlainSelect) select.getSelectBody()).getWhere()).getRightExpression()).getValue(), 0);
	}


	
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(SelectTest.class);
	}

}
