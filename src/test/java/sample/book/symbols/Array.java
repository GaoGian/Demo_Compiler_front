package sample.book.symbols;

import sample.book.lexer.Tag;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Array extends Type {

    public Type of;
    public int size = 1;

    public Array(int sz, Type p){
        super("[]", Tag.INDEX, sz*p.width);
        size = sz;
        of = p;
    }

    public String toString(){
        return "[" + size + "]" + of.toString();
    }

}
