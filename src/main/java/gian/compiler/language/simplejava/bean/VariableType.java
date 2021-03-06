package gian.compiler.language.simplejava.bean;

import gian.compiler.language.simplejava.JavaConstants;

/**
 * 数据类型
 * 基本数据类型：
 * Class类型
 * 数组类型：
 * Created by gaojian on 2019/3/28.
 */
public class VariableType {

    protected String name;
    protected int width;

    public VariableType(String name, int width) {
        this.name = name;
        this.width = width;
    }

    // TODO 数值类型是否等价
    public static boolean numeric(VariableType type){
        if(type == VariableType.CHAR || type == VariableType.INT || type == VariableType.FLOAT){
            return true;
        }else{
            return false;
        }
    }

    // TODO 类型转换
    public static VariableType max(VariableType p1, VariableType p2){
        if(!numeric(p1) || !numeric(p2)){
            return null;
        }else if(p1 == VariableType.FLOAT || p2 == VariableType.FLOAT){
            return VariableType.FLOAT;
        }else if(p1 == VariableType.INT || p2 == VariableType.INT){
            return VariableType.INT;
        }else{
            return VariableType.CHAR;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString(){
        return this.getName();
    }

    public static Integer getVariableTypeWidth(String typeName){
        if(JavaConstants.VARIABLE_TYPE_INT.equals(typeName)){
            return INT.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_INT.equals(typeName)){
            return LONG.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_SHORT.equals(typeName)){
            return SHORT.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_FLOAT.equals(typeName)){
            return FLOAT.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_DOUBLE.equals(typeName)){
            return DOUBLE.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_CHAR.equals(typeName)){
            return CHAR.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_BYTE.equals(typeName)){
            return BYTE.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_BOOLEAN.equals(typeName)){
            return BOOLEAN.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_CLAZZ.equals(typeName)){
            return CLAZZ.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_VOID.equals(typeName)){
            return VOID.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_NUMBER.equals(typeName)){
            return FLOAT.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_DIGIT.equals(typeName)){
            return INT.getWidth();
        }else if(JavaConstants.VARIABLE_TYPE_STRING.equals(typeName)){
            return STRING.getWidth();
        }

        return null;
    }

    // SimpleJava数据类型
    public static VariableType INT = new VariableType(JavaConstants.VARIABLE_TYPE_INT, 8);
    public static VariableType LONG = new VariableType(JavaConstants.VARIABLE_TYPE_LONG, 16);
    public static VariableType SHORT = new VariableType(JavaConstants.VARIABLE_TYPE_SHORT, 4);
    public static VariableType FLOAT = new VariableType(JavaConstants.VARIABLE_TYPE_FLOAT, 16);
    public static VariableType DOUBLE = new VariableType(JavaConstants.VARIABLE_TYPE_DOUBLE, 32);
    public static VariableType CHAR = new VariableType(JavaConstants.VARIABLE_TYPE_CHAR, 4);
    public static VariableType BYTE = new VariableType(JavaConstants.VARIABLE_TYPE_BYTE, 1);
    public static VariableType BOOLEAN = new VariableType(JavaConstants.VARIABLE_TYPE_BOOLEAN, 2);
    public static VariableType VOID = new VariableType(JavaConstants.VARIABLE_TYPE_VOID, 0);
    public static VariableType CLAZZ = new VariableType(JavaConstants.VARIABLE_TYPE_CLAZZ, 2);      // 指针引用
    public static VariableType STRING = new VariableType(JavaConstants.VARIABLE_TYPE_STRING, 2);    // 指针引用

}