package gian.compiler.language.simplejava.ast.expression;

/**
 * Created by tingyun on 2018/7/20.
 */
public class And extends Logical {

    public And(String tok, Expr x1, Expr x2){
        super(tok, x1, x2);
    }

//    @Override
//    public void jumping(int t, int f){
//        int label = f != 0 ? f : newlabel();
//        expr1.jumping(0, label);
//        expr2.jumping(t, f);
//        if(f == 0){
//            emitlabel(label);
//        }
//    }

}