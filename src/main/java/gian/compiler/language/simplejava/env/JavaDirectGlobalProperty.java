package gian.compiler.language.simplejava.env;

import gian.compiler.front.lexical.transform.MyStack;
import gian.compiler.front.syntaxDirected.SyntaxDirectedListener;
import gian.compiler.language.simplejava.bean.ClazzConstructor;
import gian.compiler.language.simplejava.bean.ClazzField;
import gian.compiler.language.simplejava.bean.ClazzMethod;
import gian.compiler.language.simplejava.bean.VariableType;
import gian.compiler.language.simplejava.ast.statement.Stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaojian on 2019/3/31.
 */
public class JavaDirectGlobalProperty {

    public static JavaEnvironment topEnv;
    public static Integer lexline = SyntaxDirectedListener.lexline;
    public static Integer lexindex = SyntaxDirectedListener.lexindex;
    public static AtomicInteger lable = new AtomicInteger(0);
    public static AtomicInteger tempCout = new AtomicInteger(0);

    public static List<ClazzField> fieldList = new ArrayList<>();
    public static List<ClazzConstructor> constructorList = new ArrayList<>();
    public static List<ClazzMethod> methodList = new ArrayList<>();

    // 记录循环体作，用于break及continue调到当前循环位置
    public static MyStack<Stmt> cycleEnclosingStack = new MyStack<>();

    // 记录方法声明的返回数据类型，用于return判断
    public static VariableType methodVariableType;

}