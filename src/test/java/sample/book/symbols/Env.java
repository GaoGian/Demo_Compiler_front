package sample.book.symbols;

import sample.book.inter.element.Id;
import sample.book.inter.Node;
import sample.book.lexer.Token;

import java.util.Hashtable;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Env extends Node {

    private Hashtable<Token, Id> table;
    protected Env prev;

    public Env(Env n){
        table = new Hashtable<Token, Id>();
        prev = n;
    }

    public void put(Token w, Id i){
        table.put(w, i);
    }

    public Id get(Token w){
        for(Env e = this; e != null; e = e.prev){
            Id found = (Id)(e.table.get(w));
            if(found != null){
                return found;
            }
        }
        return null;
    }

}
