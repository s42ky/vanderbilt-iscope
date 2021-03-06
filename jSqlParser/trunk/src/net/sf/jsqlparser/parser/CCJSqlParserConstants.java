/* Generated By:JavaCC: Do not edit this line. CCJSqlParserConstants.java */
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


package net.sf.jsqlparser.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CCJSqlParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int K_AS = 5;
  /** RegularExpression Id. */
  int K_BY = 6;
  /** RegularExpression Id. */
  int K_DO = 7;
  /** RegularExpression Id. */
  int K_IS = 8;
  /** RegularExpression Id. */
  int K_IN = 9;
  /** RegularExpression Id. */
  int K_OR = 10;
  /** RegularExpression Id. */
  int K_ON = 11;
  /** RegularExpression Id. */
  int K_ALL = 12;
  /** RegularExpression Id. */
  int K_AND = 13;
  /** RegularExpression Id. */
  int K_ANY = 14;
  /** RegularExpression Id. */
  int K_KEY = 15;
  /** RegularExpression Id. */
  int K_NOT = 16;
  /** RegularExpression Id. */
  int K_SET = 17;
  /** RegularExpression Id. */
  int K_ASC = 18;
  /** RegularExpression Id. */
  int K_TOP = 19;
  /** RegularExpression Id. */
  int K_END = 20;
  /** RegularExpression Id. */
  int K_CAST = 21;
  /** RegularExpression Id. */
  int K_DESC = 22;
  /** RegularExpression Id. */
  int K_INTO = 23;
  /** RegularExpression Id. */
  int K_NULL = 24;
  /** RegularExpression Id. */
  int K_LIKE = 25;
  /** RegularExpression Id. */
  int K_DROP = 26;
  /** RegularExpression Id. */
  int K_JOIN = 27;
  /** RegularExpression Id. */
  int K_LEFT = 28;
  /** RegularExpression Id. */
  int K_FROM = 29;
  /** RegularExpression Id. */
  int K_OPEN = 30;
  /** RegularExpression Id. */
  int K_CASE = 31;
  /** RegularExpression Id. */
  int K_WHEN = 32;
  /** RegularExpression Id. */
  int K_THEN = 33;
  /** RegularExpression Id. */
  int K_ELSE = 34;
  /** RegularExpression Id. */
  int K_SOME = 35;
  /** RegularExpression Id. */
  int K_FULL = 36;
  /** RegularExpression Id. */
  int K_WITH = 37;
  /** RegularExpression Id. */
  int K_TABLE = 38;
  /** RegularExpression Id. */
  int K_WHERE = 39;
  /** RegularExpression Id. */
  int K_USING = 40;
  /** RegularExpression Id. */
  int K_UNION = 41;
  /** RegularExpression Id. */
  int K_GROUP = 42;
  /** RegularExpression Id. */
  int K_BEGIN = 43;
  /** RegularExpression Id. */
  int K_INDEX = 44;
  /** RegularExpression Id. */
  int K_INNER = 45;
  /** RegularExpression Id. */
  int K_LIMIT = 46;
  /** RegularExpression Id. */
  int K_OUTER = 47;
  /** RegularExpression Id. */
  int K_ORDER = 48;
  /** RegularExpression Id. */
  int K_RIGHT = 49;
  /** RegularExpression Id. */
  int K_DELETE = 50;
  /** RegularExpression Id. */
  int K_CREATE = 51;
  /** RegularExpression Id. */
  int K_SELECT = 52;
  /** RegularExpression Id. */
  int K_OFFSET = 53;
  /** RegularExpression Id. */
  int K_EXISTS = 54;
  /** RegularExpression Id. */
  int K_HAVING = 55;
  /** RegularExpression Id. */
  int K_INSERT = 56;
  /** RegularExpression Id. */
  int K_UPDATE = 57;
  /** RegularExpression Id. */
  int K_VALUES = 58;
  /** RegularExpression Id. */
  int K_ESCAPE = 59;
  /** RegularExpression Id. */
  int K_PRIMARY = 60;
  /** RegularExpression Id. */
  int K_NATURAL = 61;
  /** RegularExpression Id. */
  int K_REPLACE = 62;
  /** RegularExpression Id. */
  int K_BETWEEN = 63;
  /** RegularExpression Id. */
  int K_TRUNCATE = 64;
  /** RegularExpression Id. */
  int K_DISTINCT = 65;
  /** RegularExpression Id. */
  int K_INTERSECT = 66;
  /** RegularExpression Id. */
  int K_BINARY = 67;
  /** RegularExpression Id. */
  int K_INTERVAL = 68;
  /** RegularExpression Id. */
  int S_DOUBLE = 69;
  /** RegularExpression Id. */
  int S_INTEGER = 70;
  /** RegularExpression Id. */
  int DIGIT = 71;
  /** RegularExpression Id. */
  int LINE_COMMENT = 72;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 73;
  /** RegularExpression Id. */
  int S_IDENTIFIER = 74;
  /** RegularExpression Id. */
  int LETTER = 75;
  /** RegularExpression Id. */
  int SPECIAL_CHARS = 76;
  /** RegularExpression Id. */
  int S_CHAR_LITERAL = 77;
  /** RegularExpression Id. */
  int S_QUOTED_IDENTIFIER = 78;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\"",
    "\"\\n\"",
    "\"AS\"",
    "\"BY\"",
    "\"DO\"",
    "\"IS\"",
    "\"IN\"",
    "\"OR\"",
    "\"ON\"",
    "\"ALL\"",
    "\"AND\"",
    "\"ANY\"",
    "\"KEY\"",
    "\"NOT\"",
    "\"SET\"",
    "\"ASC\"",
    "\"TOP\"",
    "\"END\"",
    "\"CAST\"",
    "\"DESC\"",
    "\"INTO\"",
    "\"NULL\"",
    "\"LIKE\"",
    "\"DROP\"",
    "\"JOIN\"",
    "\"LEFT\"",
    "\"FROM\"",
    "\"OPEN\"",
    "\"CASE\"",
    "\"WHEN\"",
    "\"THEN\"",
    "\"ELSE\"",
    "\"SOME\"",
    "\"FULL\"",
    "\"WITH\"",
    "\"TABLE\"",
    "\"WHERE\"",
    "\"USING\"",
    "\"UNION\"",
    "\"GROUP\"",
    "\"BEGIN\"",
    "\"INDEX\"",
    "\"INNER\"",
    "\"LIMIT\"",
    "\"OUTER\"",
    "\"ORDER\"",
    "\"RIGHT\"",
    "\"DELETE\"",
    "\"CREATE\"",
    "\"SELECT\"",
    "\"OFFSET\"",
    "\"EXISTS\"",
    "\"HAVING\"",
    "\"INSERT\"",
    "\"UPDATE\"",
    "\"VALUES\"",
    "\"ESCAPE\"",
    "\"PRIMARY\"",
    "\"NATURAL\"",
    "\"REPLACE\"",
    "\"BETWEEN\"",
    "\"TRUNCATE\"",
    "\"DISTINCT\"",
    "\"INTERSECT\"",
    "\"BINARY\"",
    "\"INTERVAL\"",
    "<S_DOUBLE>",
    "<S_INTEGER>",
    "<DIGIT>",
    "<LINE_COMMENT>",
    "<MULTI_LINE_COMMENT>",
    "<S_IDENTIFIER>",
    "<LETTER>",
    "<SPECIAL_CHARS>",
    "<S_CHAR_LITERAL>",
    "<S_QUOTED_IDENTIFIER>",
    "\";\"",
    "\"=\"",
    "\",\"",
    "\"(\"",
    "\")\"",
    "\".\"",
    "\"*\"",
    "\"?\"",
    "\">\"",
    "\"<\"",
    "\">=\"",
    "\"<=\"",
    "\"<>\"",
    "\"!=\"",
    "\"@@\"",
    "\"||\"",
    "\"|\"",
    "\"&\"",
    "\"+\"",
    "\"-\"",
    "\"/\"",
    "\"^\"",
    "\"{d\"",
    "\"}\"",
    "\"{t\"",
    "\"{ts\"",
    "\"{fn\"",
  };

}
