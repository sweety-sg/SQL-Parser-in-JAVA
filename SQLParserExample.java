class SQLParser {
    private String input;
    private int position;
    
    public SQLParser(String input) {
        this.input = input;
        this.position = 0;
        // this.tables = new ArrayList<>();
        // this.columns = new ArrayList<>();
        // this.primaryKeys = new ArrayList<>();
        // this.foreignKeys = new HashMap<>();
        // this.joins = new ArrayList<>();
    }
    public boolean parse(){ 
        return parseSelectStatement() || parseCreateTable();
    }
    
    public boolean parseSelectStatement() {
        if (match("SELECT") && parseSelectList() && match("FROM") && parseTableList() && parseJoin() &&
            parseWhereClause() && parseGroupByClause() && parseHavingClause()) {
            return true;
        }
        position=0;
        return false;
    }
    private boolean parseCreateTable() {
        return match("CREATE") && match("TABLE") && parseTableName() && match("(") && parseColumnDefList() && match(")");
    }
    // private boolean parseSelectList() {
    //     if (parseSelectList()) {
    //         return true;
    //     }
    //     return false;
    // }

    private boolean parseSelectList() {
        int currPos = position;
        if (parseColumn() && match(",") && parseSelectList()) {
            return true;
        } else{
            position = currPos;
            if (parseColumn()) {
                System.out.println("else if called" + input.substring(position, position+10));
                return true;
            }
        }
        return false;
    }
    
    private boolean parseColumn() {
        int currPos = position;
        if (parseTableName() && match(".") && parseColumnName()) {
            System.out.println("oho");
            return true;
        } else{
            position = currPos;
            if (parseColumnName()) {
                // System.out.println(input.substring(position, position+10));
                return true;
            }
        }
        return false;
    }
    
    private boolean parseTableList() {
        System.out.println("called");
        int currPos = position;
        if (parseTableName() && match(",") && parseTableList()) {
            return true;
        } else{
            position = currPos;
            if (parseTableName()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean parseWhereClause() {
        if (match("WHERE") && parseCondition()) {
            System.out.println("where matched");
            return true;
        } else {
            System.out.println("where not matched");
           return false;
        }
    }

    private boolean parseAggregateFunction() {
        if (match("SUM") || match("AVG") || match("MIN") || match("MAX") || match("COUNT")) {
            if (match("(") && parseColumn() && match(")")) {
                return true;
            }
        }
        return false;
    }

    private boolean parseCondition() {
        if (parseComparison()) {
            return true;
        } else if (match("(") && parseCondition() && match(")")) {
            return true;
        } else if (parseCondition() && match("AND") && parseCondition()) {
            return true;
        } else if (parseCondition() && match("OR") && parseCondition()) {
            return true;
        }
        return false;
    }
    
    private boolean parseComparison() {
        if (parseValue() && parseOperator() && parseValue()) {
            return true;
        }
        return false;
    }
    
    private boolean parseOperator() {
        if (match("=") || match("<>") || match(">") || match("<") || match(">=") || match("<=")) {
            return true;
        }
        return false;
    }
    
    private boolean parseValue() {
        if (parseColumn() || parseLiteral()) {
            return true;
        }
        return false;
    }
    
    private boolean parseLiteral() {
        if (match("'") && matchIdentifier() && match("'")) {
            return true;
        } else if (matchNumber()) {
            return true;
        }
        return false;
    }
    
    private boolean parseGroupByClause() {
        if (match("GROUP BY") && parseSelectList()) {
            return true;
        }
        return false;
    }
    
    private boolean parseHavingClause() {
        if (match("HAVING") && parseAggregateFunction() && parseOperator() && parseLiteral()) {
            return true;
        }
        return false;
    }

    private boolean parseJoin() {
        int currPos = position;
        if(!(parseInnerJoin() || parseOuterJoin() || parseCrossJoin())) {
            position= currPos;
            return true;
        }
        parseJoin();
        return true;
    }
    
    private boolean parseInnerJoin() {
        return  match("INNER JOIN") && parseTableName() && match("ON") && parseCondition();
    }
    
    private boolean parseOuterJoin() {
        return parseOuterJoinType() && parseTableName() && match("ON") && parseCondition();
    }
    
    private boolean parseOuterJoinType() {
        return match("LEFT OUTER JOIN") || match("RIGHT OUTER JOIN") || match("FULL OUTER JOIN");
    }
    
    private boolean parseCrossJoin() {
        return match("CROSS JOIN") && parseTableName();
    }

    private boolean parseOrderBy() {
        if (match("ORDER BY")) {
            if (parseColumn()) {
                if (match("ASC") || match("DESC")) {
                    return parseOrderByRest();
                }
                return true;
            }
            return false;
        }
        return true;
    }
    
    private boolean parseOrderByRest() {
        if (match(",")) {
            if (parseColumn()) {
                if (match("ASC") || match("DESC")) {
                    return parseOrderByRest();
                }
                return true;
            }
            return false;
        }
        return true;
    }

    
    private boolean parseTableName() {
        if (matchIdentifier()) {
            return true;
        }
        return false;
    }
    
    private boolean parseColumnName() {
        if (matchIdentifier()) {
            return true;
        }
        return false;
    }

    private boolean parseColumnDefList() {
        if (parseColumnDef()) {
            while (match(",")) {
                if (!parseColumnDef()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean parseColumnDef() {
        skipSpaces();
        return matchIdentifier() && parseDataType() && parseColumnConstraints();
    }
    
    private boolean parseDataType() {
        skipSpaces();
        return match("INT") || (match("VARCHAR(") && matchNumber() && match(")")) || match("TEXT");
    }
    
    private boolean parseColumnConstraints() {
        skipSpaces();
        if (match("PRIMARY KEY")) {
            return true;
        }
        if (match("NOT NULL")) {
            return true;
        }
        if (match("UNIQUE")) {
            return true;
        }
        return true;
    }
    
    private boolean match(String expected) {
        while(position < input.length() && input.charAt(position) == ' ') position++ ;
        if (position + expected.length() <= input.length() &&
            input.substring(position, position + expected.length()).equals(expected)) {
            position += expected.length();
            System.out.println("success" + expected);
            return true;
        }
        // System.out.println("match func failed " + expected + input.substring(position, position+expected.length() ));
        return false;
    }
    
    private boolean matchIdentifier() {
        while(position < input.length() && input.charAt(position) == ' ') position++ ;
        if (position < input.length() && Character.isLetter(input.charAt(position))) {
            position++;
            while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
                position++;
            }
            // System.out.println("success matching id.");
            return true;
        }
        System.out.println(input.substring(position, position+10));
        System.out.println("failed matching id." + position);
        return false;
    }
    
    private boolean matchNumber() {
        if (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
            while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
                position++;
            }
            System.out.println("success matching no.");
            return true;
        }
        System.out.println("failed matching no.");
        return false;
    }
    private void skipSpaces(){
        while(position < input.length() && input.charAt(position) == ' ') position++ ;
    }
    }
    
    public class SQLParserExample {
        public static void main(String[] args) {
            
            String sql = "SELECT customers.name, orders.order_date, order_items.quantity " +
                         "FROM customers " +
                         "INNER JOIN orders ON customers.id = orders.customer_id " +
                         "INNER JOIN order_items ON orders.id = order_items.order_id " +
                         "WHERE customers.country = 'USA' " +
                         "GROUP BY customers.name, orders.order_date " +
                         "HAVING SUM(order_items.quantity) > 10";

            String sql2 = "CREATE TABLE customers (" +
                "id INT PRIMARY KEY," +
                "name VARCHAR(50) NOT NULL," +
                "email VARCHAR(100) UNIQUE," +
                "address TEXT)"; 
            SQLParser parser = new SQLParser(sql2);
            boolean success = parser.parse();
            
            if (success) {
                // List<String> tables = parser.getTables();
                // List<String> columns = parser.getColumns();
                // List<String> primaryKeys = parser.getPrimaryKeys();
                // Map<String, List<String>> foreignKeys = parser.getForeignKeys();
                // List<Join> joins = parser.getJoins();
                
                // System.out.println("Tables: " + tables);
                // System.out.println("Columns: " + columns);
                // System.out.println("Primary keys: " + primaryKeys);
                // System.out.println("Foreign keys: " + foreignKeys);
                // System.out.println("Joins: " + joins);
            } else {
                System.out.println("Parsing failed");
            }
        }
    }

    
    
    
    
    
    
    
    
