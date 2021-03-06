package gian.compiler.language.simplejava.ast.statement;

import gian.compiler.language.simplejava.env.JavaDirectGlobalProperty;

/**
 * Created by gaojian on 2019/4/2.
 */
public class Continue extends Stmt {

    public Stmt stmt;

    public Continue(){
        if(JavaDirectGlobalProperty.cycleEnclosingStack.top() == null){
            error("unenclosed break");
        }
        stmt = JavaDirectGlobalProperty.cycleEnclosingStack.top();
    }

    @Override
    public void gen(String before, String after){
        emit("<goto> " + stmt.current);
    }

}