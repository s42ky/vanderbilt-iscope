package test.jsqlp;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;

public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CCJSqlParserManager pm = new CCJSqlParserManager();
		String sql = "INSERT INTO blogdata SET user_id='1', subject='ZDqwxJCppv', message='wshrrS kItLoV TcRu rrLWVsA NfyYgyPeCHu qqawsXrsStn tNWSDrE HtDHKkQQnRohkSHXe RX BB uVs totQg n iOgu kzLPTBAxvdzvnaPzAj QfAgNUdwJad qAJKHRwShHlqvuIbBX klxXLsyXwJUbWZ ImPABZpeI BlUbG lQwXnrBPo HVsaAdXtySPLczwBH huHY ATNcbaOBpHMZZM qDLVisqQZ', datetime='2012-06-05 13:11:11'" ;
		net.sf.jsqlparser.statement.Statement statement;
		try {
			statement = pm.parse(new StringReader(sql));
			/* 
			now you should use a class that implements StatementVisitor to decide what to do
			based on the kind of the statement, that is SELECT or INSERT etc. but here we are only
			interested in SELECTS
			*/
			if (statement instanceof Select) {
				Select selectStatement = (Select) statement;
				TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
				List tableList = tablesNamesFinder.getTableList(selectStatement);
				for (Iterator iter = tableList.iterator(); iter.hasNext();) {
					System.out.println(iter.next());
				}
			}
			
			if (statement instanceof Insert) {
				Insert iS = (Insert) statement;
				
				@SuppressWarnings("unchecked")
				List<Column> cols = iS.getColumns();
				ItemsList vals = iS.getItemsList();
				
				System.out.println("#cols:" + cols.size());
				
				for(Iterator<Column> iter = cols.iterator(); iter.hasNext(); ) {
					System.out.println(iter.next().getColumnName());
				}
			}
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
