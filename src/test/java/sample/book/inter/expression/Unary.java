package sample.book.inter.expression;

import sample.book.lexer.Token;
import sample.book.symbols.Type;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Unary extends Op {

    public Expr expr;

    public Unary(Token tok, Expr x){
        super(tok, null);
        expr = x;
        type = Type.max(Type.Int, expr.type);
        if(type == null){
            error("type error");
        }
    }

    public Expr gen(){
        return new Unary(op, expr.reduce());
    }

    public String toString(){
        return op.toString() + " " + expr.toString();
    }

}
