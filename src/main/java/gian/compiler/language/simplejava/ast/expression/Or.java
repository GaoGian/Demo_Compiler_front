package gian.compiler.language.simplejava.ast.expression;


/**
 * Created by tingyun on 2018/7/20.
 */
public class Or extends Logical {

    public Or(String tok, Expr x1, Expr x2){
        super(tok, x1, x2);
    }

}
