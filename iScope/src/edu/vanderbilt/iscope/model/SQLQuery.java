package edu.vanderbilt.iscope.model;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class SQLQuery {
        public final static int SQL_QUERY_TYPE_SELECT = 0;
        public final static int SQL_QUERY_TYPE_INSERT = 1;
        public final static int SQL_QUERY_TYPE_UPDATE = 2;
        public final static int SQL_QUERY_TYPE_DELETE = 3;
        public final static int SQL_QUERY_TYPE_REPLACE = 4;
        public final static int SQL_QUERY_TYPE_ADMINISTRATIVE = -2;
        public final static int SQL_QUERY_TYPE_UNDEFINED = -1;

        private int type;      // 0: SELECT; 1: INSERT; 2: UPDATE;  3: DELETE; 
        private SortedSet<String> tables;      // the first/default table.
        private SortedSet<String> selectFields;

		private SortedSet<String> fields;
        private HashMap<String, String> values;  // the observed values.

        public SQLQuery() {
                type = SQLQuery.SQL_QUERY_TYPE_UNDEFINED;
                tables = new TreeSet<String>();
                fields = new TreeSet<String>();
                selectFields = new TreeSet<String>();
                values = new HashMap<String, String>();
        }

        public void clear(){
                type = SQLQuery.SQL_QUERY_TYPE_UNDEFINED;
                tables.clear();
                fields.clear();
                selectFields.clear();
                values.clear();
        }

        public int getType() {
                return type;
        }

        public void setType(int type) {
                this.type = type;
        }

        public SortedSet<String> getTables() {
                return tables;
        }

        public void setTables(SortedSet<String> tables) {
                this.tables = tables;
        }

        public SortedSet<String> getFields() {
                return fields;
        }

        public void setFields(SortedSet<String> fields) {
                this.fields = fields;
        }

        public HashMap<String, String> getValues() {
                return values;
        }

        public void setValues(HashMap<String, String> values) {
                this.values = values;
        }
        
		public SortedSet<String> getSelectFields() {
			return selectFields;
		}

		public void setSelectFields(SortedSet<String> selectFields) {
			this.selectFields = selectFields;
		}
}