package sample.book.inter.statement;

import sample.book.inter.Node;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Stmt extends Node {

    public static Stmt Null = new Stmt();
    public int after = 0;
    public static Stmt Enclosing = Stmt.Null;

    public Stmt(){}

    public void gen(int b, int a){}

}
