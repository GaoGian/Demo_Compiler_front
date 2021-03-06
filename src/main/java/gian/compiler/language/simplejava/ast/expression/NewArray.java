package gian.compiler.language.simplejava.ast.expression;

import gian.compiler.language.simplejava.bean.Variable;
import gian.compiler.language.simplejava.bean.VariableArrayType;
import gian.compiler.language.simplejava.bean.VariableType;
import gian.compiler.language.simplejava.utils.JavaDirectUtils;

/**
 * Created by gaojian on 2019/4/4.
 */
public class NewArray extends Expr {

    public VariableType baseType;
    public VariableArrayType variableArrayType;

    public NewArray(VariableType baseType, VariableArrayType variableArrayType){
        super(variableArrayType);
        this.baseType = baseType;
        this.variableArrayType = variableArrayType;
    }

    @Override
    public String code(){
        StringBuilder str = new StringBuilder();
        str.append("new " + this.baseType.getName());
        VariableType elementType = this.variableArrayType;
        while (elementType instanceof VariableArrayType){
            VariableArrayType arrayType = (VariableArrayType) elementType;
            str.append("[" + arrayType.getSize() + "]");

            elementType = arrayType.getBaseVariableType();
        }

        return str.toString();
    }

}