package sample.book.inter.expression;

import sample.book.inter.Node;
import sample.book.lexer.Token;
import sample.book.symbols.Type;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Expr extends Node {

    public Token op;
    public Type type;

    public Expr(Token tok, Type p){
        op = tok;
        type = p;
    }

    public Expr gen(){
        return this;
    }

    public Expr reduce(){
        return this;
    }

    public void jumping(int t, int f){
        emitjumps(toString(), t, f);
    }

    public void emitjumps(String test, int t, int f){
        if(t != 0 && f != 0){
            emit("if " + test + " goto L" + t);
            emit("goto L" + f);
        }else if(t != 0){
            emit("if " + test + " goto L" + t);
        }else if(f != 0){
            emit("ifFalse " + test + " goto L" + f);
        }else{

        }
    }

    public String toString(){
        return op.toString();
    }

}
