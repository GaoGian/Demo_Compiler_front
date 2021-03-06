package sample.book.inter.statement;

import sample.book.inter.expression.Expr;
import sample.book.symbols.Type;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Do extends Stmt {

    public Expr expr;
    public Stmt stmt;

    public Do(){
        expr = null;
        stmt = null;
    }

    public void init(Stmt s, Expr x){
        expr = x;
        stmt = s;
        if(expr.type != Type.Bool){
            expr.error("boolean required in do");
        }
    }

    public void gen(int b, int a){
        after = a;
        int label = newlabel();
        stmt.gen(b, label);
        emitlabel(label);
        expr.jumping(b, 0);
    }

}
