package sample.book.parser;

import sample.book.inter.*;
import sample.book.inter.element.Id;
import sample.book.inter.expression.Expr;
import sample.book.inter.expression.Unary;
import sample.book.inter.operator.*;
import sample.book.inter.statement.*;
import sample.book.lexer.*;
import sample.book.symbols.Array;
import sample.book.symbols.Env;
import sample.book.symbols.Type;

import java.io.IOException;

/**
 * Created by tingyun on 2018/7/20.
 */
public class Parser {

    private Lexer lex;
    private Token look;
    Env top = null;
    int used = 0;

    public Parser(Lexer l) throws IOException{
        lex = l;
        move();
    }

    void move() throws IOException{
        look = lex.scan();
    }

    void error(String s){
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) throws IOException{
        if(look.tag == t){
            move();
        }else{
            error("syntax error");
        }
    }

    public void program() throws IOException{
        // 解析语法树
        Stmt s = block();

        // 输出三地址码
        int begin = s.newlabel();
        int after = s.newlabel();
        s.emitlabel(begin);
        s.gen(begin, after);    // 从根节点逐个遍历子节点，输出三地址码
        s.emitlabel(after);
    }

    Stmt block() throws IOException{
        match('{');
        Env savedEnv = top;
        top = new Env(top);     // 当前标识符作用域
        decls();        // 处理变量声明
        Stmt s = stmts();   // 结构语句成分
        match('}');
        top = savedEnv;
        return s;
    }

    void decls() throws IOException{
        while (look.tag == Tag.BASIC){
            Type p = type();
            Token tok = look;
            match(Tag.ID);
            match(';');
            Id id = new Id((Word)tok, p, used);
            top.put(tok, id);
            used = used + p.width;
        }
    }

    Type type() throws IOException{
        Type p = (Type)look;
        match(Tag.BASIC);
        if(look.tag != '['){
            return p;
        }else{
            return dims(p);
        }
    }

    Type dims(Type p) throws IOException{
        match('[');
        Token tok = look;
        match(Tag.NUM);
        match(']');
        if(look.tag == '['){
            p = dims(p);
        }
        return new Array(((Num)tok).value, p);
    }

    Stmt stmts() throws IOException{
        if(look.tag == '}'){
            return Stmt.Null;
        }else{
            return new Seq(stmt(), stmts());    // 这里通过递归，逐个解析后续语句
        }
    }

    Stmt stmt() throws IOException{
        Expr x = null;
        Stmt s = null, s1 = null, s2 = null;
        Stmt savedStmt = null;

        switch (look.tag) {
            case ';':
                move();
                return Stmt.Null;
            case Tag.IF:
                match(Tag.IF); match('('); x = bool(); match(')');
                s1 = stmt();
                if(look.tag != Tag.ELSE){
                    return new If(x, s1);
                }
                match(Tag.ELSE);
                s2 = stmt();
                return new Else(x, s1, s2);
            case Tag.WHILE:
                While whilenode = new While();
                savedStmt = Stmt.Enclosing; Stmt.Enclosing = whilenode;
                match(Tag.WHILE); match('('); x = bool(); match(')');
                s1 = stmt();
                whilenode.init(x, s1);
                Stmt.Enclosing = savedStmt;
                return whilenode;
            case Tag.DO:
                Do donode = new Do();
                savedStmt = Stmt.Enclosing; Stmt.Enclosing = donode;
                match(Tag.DO);
                s1 = stmt();
                match(Tag.WHILE); match('('); x = bool(); match(')'); match(';');
                donode.init(s1, x);
                Stmt.Enclosing = savedStmt;
                return donode;
            case Tag.BREAK:
                match(Tag.BREAK); match(';');
                return new Break();
            case '{':
                return block();
            default:
                return assign();
        }
    }

/**-----------------------------下面是运算符优先级---------------------------------**/

    Stmt assign() throws IOException{
        Stmt stmt;
        Token t = look;
        match(Tag.ID);
        Id id = top.get(t);
        if(id == null){
            error(t.toString() + " undeclared");
        }
        if(look.tag == '='){
            move();
            stmt = new Set(id, bool());
        }else{
            Access x = offset(id);
            match('=');
            stmt = new SetElem(x, bool());
        }
        match(';');
        return stmt;
    }

    Expr bool() throws IOException{     // 判断布尔值表达式
        Expr x = join();
        while(look.tag == Tag.OR){
            Token tok = look;
            move();
            x = new Or(tok, x, join());
        }
        return x;
    }

    Expr join() throws IOException{
        Expr x = equality();
        while (look.tag == Tag.AND){
            Token tok = look;
            move();
            x = new And(tok, x, equality());
        }
        return x;
    }

    Expr equality() throws IOException{
        Expr x = rel();
        while(look.tag == Tag.EQ || look.tag == Tag.NE){
            Token tok = look;
            move();
            x = new Rel(tok, x, rel());
        }
        return x;
    }

    Expr rel() throws IOException{
        Expr x = expr();
        switch (look.tag){
            case '<': case Tag.LE: case Tag.GE: case '>':
                Token tok = look;
                move();
                return new Rel(tok, x, expr());
            default:
                return x;
        }
    }

    Expr expr() throws IOException{
        Expr x = term();
        while(look.tag == '+' || look.tag == '-'){
            Token tok = look;
            move();
            x = new Arith(tok, x, term());
        }
        return x;
    }

    Expr term() throws IOException{
        Expr x = unary();
        while(look.tag == '*' || look.tag == '/'){
            Token tok = look;
            move();
            x = new Arith(tok, x, unary());
        }
        return x;
    }

    Expr unary() throws IOException{
        if(look.tag == '-'){
            move();
            return new Unary(Word.minus, unary());
        }else if(look.tag == '!'){
            Token tok = look;
            move();
            return new Not(tok, unary());
        }else{
            return factor();
        }
    }

    Expr factor() throws IOException{
        Expr x = null;
        switch (look.tag) {
            case '(':
                move(); x = bool(); match(')');
                return x;
            case Tag.NUM:
                x = new Constant(look, Type.Int);
                move();
                return x;
            case Tag.REAL:
                x = new Constant(look, Type.Float);
                move();
                return x;
            case Tag.TRUE:
                x = Constant.True;
                move();
                return x;
            case Tag.FALSE:
                x = Constant.False;
                move();
                return x;
            case Tag.ID:
                String s = look.toString();
                Id id = top.get(look);
                if(id == null){
                    error(look.toString() + " undeclared");
                }
                move();
                if(look.tag != '['){
                    return id;
                }else{
                    return offset(id);
                }
            default:
                error("syntax error");
                return x;
        }
    }

    Access offset(Id a) throws IOException{     // 数组节点
        Expr i; Expr w; Expr t1, t2; Expr loc;
        Type type = a.type;
        match('[');
        i = bool();
        match(']');
        type = ((Array)type).of;
        w = new Constant(type.width);
        t1 = new Arith(new Token('*'), i, w);
        loc = t1;
        while(look.tag == '['){
            match('['); i = bool(); match(']');
            type = ((Array)type).of;
            w = new Constant(type.width);
            t1 = new Arith(new Token('*'), i, w);
            t2 = new Arith(new Token('+'), loc, t1);
            loc = t2;
        }
        return new Access(a, loc, type);
    }

}
