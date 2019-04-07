package gian.compiler.language.simplejava.action;

import gian.compiler.language.simplejava.JavaConstants;
import gian.compiler.language.simplejava.bean.Clazz;
import gian.compiler.front.lexical.transform.LexConstants;
import gian.compiler.front.syntactic.element.SyntaxTree;
import gian.compiler.front.syntaxDirected.SyntaxDirectedContext;
import gian.compiler.front.syntaxDirected.SyntaxDirectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaojian on 2019/3/27.
 */
public class JavaLanguageDirectAction {

    public static String product = "javaLanguage → packageDeclaration importDeclaration classDeclaration";

    // 包声明
    public static class PackageDeclarationListener extends SyntaxDirectedListener{

        public PackageDeclarationListener(){
            this.matchProductTag = product;
            this.matchSymbol = "packageDeclaration";
            this.matchIndex = 0;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            // key: 类全名, value: clazz
            Map<String, Clazz> clazzMap = new HashMap<>();
            context.getGlobalPropertyMap().put(JavaConstants.CLAZZ_MAP, clazzMap);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            String packageName = (String) currentTreeNode.getPropertyMap().get(LexConstants.SYNTAX_DIRECT_PROPERTY_SYN).get(JavaConstants.PACKAGE_NAME);
            context.getGlobalPropertyMap().put(JavaConstants.PACKAGE_NAME, packageName);

            return null;
        }

    }

    // 引入包列表声明
    public static class ImportDeclarationListener extends SyntaxDirectedListener{

        public ImportDeclarationListener(){
            this.matchProductTag = product;
            this.matchSymbol = "importDeclaration";
            this.matchIndex = 1;
            this.isLeaf = false;
        }

        @Override
        public String enterSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            List<String> importList = new ArrayList<>();
            Map<String, String> importMap = new HashMap<>();
            context.getGlobalPropertyMap().put(JavaConstants.IMPORT_LIST, importList);
            context.getGlobalPropertyMap().put(JavaConstants.IMPORT_MAP, importMap);

            return null;
        }

        @Override
        public String exitSyntaxSymbol(SyntaxDirectedContext context, SyntaxTree.SyntaxTreeNode currentTreeNode, Integer currentIndex) {
            return null;
        }

    }

}