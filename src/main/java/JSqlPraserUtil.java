import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linzhipeng on 2018/3/29.
 */
public class JSqlPraserUtil {
    private final static Logger logger = LoggerFactory.getLogger(JSqlPraserUtil.class);

    //获取sql条件中where的列名和对应值
    public static Map<String,String> getWhereMap(String sql){
        Map<String,String> map = null;
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            logger.error(e.getMessage(),e);
            return map;
        }
        map = new HashMap<>();
        if (stmt instanceof Select){
            Select select = (Select)stmt;
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect)selectBody;
            Expression expression = plainSelect.getWhere();
            handleExpression(expression,map);
        }
        return map;
    }
    private static void handleExpression(Expression expression,Map<String,String> map){
        if (expression instanceof BinaryExpression){
            BinaryExpression binaryExpression = (BinaryExpression)expression;
            Expression left = binaryExpression.getLeftExpression();
            Expression right = binaryExpression.getRightExpression();
            if (left instanceof Column){
                Column column = (Column)left;
                ExpressionDeParser expressionDeParser = new ExpressionDeParser();
                right.accept(expressionDeParser);
                String value = expressionDeParser.getBuffer().toString();
                map.put(column.getColumnName(),value);
            }else{
                if (left instanceof BinaryExpression){
                    handleExpression(left,map);
                }
                if (right instanceof BinaryExpression){
                    handleExpression(right,map);
                }
            }
        }
    }
}
