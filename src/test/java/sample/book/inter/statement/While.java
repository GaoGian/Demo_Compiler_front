package sample.book.inter.statement;

import sample.book.inter.expression.Expr;
import sample.book.symbols.Type;

/**
 * Created by tingyun on 2018/7/20.
 */
public class While extends Stmt {

    public Expr expr;
    public Stmt stmt;

    public While(){
        expr = null;
        stmt = null;
    }

    public void init(Expr x, Stmt s){
        expr = x;
        stmt = s;
        if(expr.type != Type.Bool){
            expr.error("boolean required in while");
        }
    }

    public void gen(int b, int a){
        after = a;
        expr.jumping(0, a);
        int label = newlabel();
        emitlabel(label);
        stmt.gen(label, b);
        emit("goto L" + b);
    }

}
