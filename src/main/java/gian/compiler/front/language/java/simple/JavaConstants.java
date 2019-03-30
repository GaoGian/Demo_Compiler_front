package gian.compiler.front.language.java.simple;

import gian.compiler.front.language.java.simple.bean.VariableType;

/**
 * Created by Gian on 2019/3/27.
 */
public class JavaConstants {

    public static String PACKAGE_NAME = "packageName";
    public static String CLAZZ_MAP = "clazzMap";
    public static String IMPORT_LIST = "importList";
    public static String IMPORT_MAP = "importMap";
    public static String MODIFIER = "modifier";
    public static String EXTEND_INFO = "extendInfo";
    public static String CLAZZ_NAME = "extendClazzName";
    public static String FIELD_LIST = "fieldList";
    public static String FIELD_NAME = "fieldName";
    public static String VARIABLE_TYPE = "variableType";
    public static String CONSTRUCTOR_LIST = "constructorList";
    public static String METHOD_LIST = "methodList";
    public static String PARAM_LIST = "paramList";
    public static String CODE = "code";
    public static String ENV = "env";

    public static String IMPORT_CLAZZ_ALL_NAME = "importClazzAllName";
    public static String VARIABLE_INIT_INFO = "variableInitInfo";
    // 当前作用域
    public static String CURRENT_ENV = "currentEnv";
    // 类实例作用域，用于 this.xxx 查找变量
    public static String CLASS_ENV = "classEnv";
    // 类静态作用域，用于查找静态变量
    public static String CLASS_STATIC_ENG = "classStaticEnv";

    // 数据类型
    public static String VARIABLE_TYPE_INT = "int";
    public static String VARIABLE_TYPE_LONG = "long";
    public static String VARIABLE_TYPE_SHORT = "short";
    public static String VARIABLE_TYPE_FLOAT = "float";
    public static String VARIABLE_TYPE_DOUBLE = "double";
    public static String VARIABLE_TYPE_CHAR = "char";
    public static String VARIABLE_TYPE_BYTE = "byte";
    public static String VARIABLE_TYPE_BOOLEAN = "boolean";
    public static String VARIABLE_TYPE_VOID = "void";
    public static String VARIABLE_TYPE_CLAZZ = "clazz";     // 说明是class类型

    // 关键词
    public static String JAVA_KEYWORD_PACKAGE = "package";
    public static String JAVA_KEYWORD_IMPORT = "import";
    public static String JAVA_KEYWORD_CLASS = "class";
    public static String JAVA_KEYWORD_EXTENDS = "extends";
    public static String JAVA_KEYWORD_SUPER = "super";
    public static String JAVA_KEYWORD_THIS = "this";
    public static String JAVA_KEYWORD_NEW = "new";
    public static String JAVA_KEYWORD_TRUE = "true";
    public static String JAVA_KEYWORD_FALSE = "false";
    public static String JAVA_KEYWORD_PUBLIC = "public";
    public static String JAVA_KEYWORD_PROTECTED = "protected";
    public static String JAVA_KEYWORD_PRIVATE = "private";
    public static String JAVA_KEYWORD_STATIC = "static";
    public static String JAVA_KEYWORD_IF = "if";
    public static String JAVA_KEYWORD_ELSE = "else";
    public static String JAVA_KEYWORD_FOR = "for";
    public static String JAVA_KEYWORD_DO = "do";
    public static String JAVA_KEYWORD_WHILE = "while";
    public static String JAVA_KEYWORD_SWITCH = "switch";
    public static String JAVA_KEYWORD_RETURN = "return";
    public static String JAVA_KEYWORD_BREAK = "break";
    public static String JAVA_KEYWORD_CONTINUE = "continue";
    public static String JAVA_KEYWORD_CASE = "case";
    public static String JAVA_KEYWORD_DEFAULT = "default";

    // SimpleJava数据类型
    public static VariableType INT = new VariableType(JavaConstants.VARIABLE_TYPE_INT, true, false);
    public static VariableType LONG = new VariableType(JavaConstants.VARIABLE_TYPE_LONG, true, false);
    public static VariableType SHORT = new VariableType(JavaConstants.VARIABLE_TYPE_SHORT, true, false);
    public static VariableType FLOAT = new VariableType(JavaConstants.VARIABLE_TYPE_FLOAT, true, false);
    public static VariableType DOUBLE = new VariableType(JavaConstants.VARIABLE_TYPE_DOUBLE, true, false);
    public static VariableType CHAR = new VariableType(JavaConstants.VARIABLE_TYPE_CHAR, true, false);
    public static VariableType BYTE = new VariableType(JavaConstants.VARIABLE_TYPE_BYTE, true, false);
    public static VariableType BOOLEAN = new VariableType(JavaConstants.VARIABLE_TYPE_BOOLEAN, true, false);
    public static VariableType VOID = new VariableType(JavaConstants.VARIABLE_TYPE_VOID, true, false);
    public static VariableType CLAZZ = new VariableType(JavaConstants.VARIABLE_TYPE_CLAZZ, false, false);

}
