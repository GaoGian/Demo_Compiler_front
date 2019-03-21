import gian.compiler.practice.lexical.parser.LexExpression;
import gian.compiler.practice.lexical.parser.LexicalParser;
import gian.compiler.practice.lexical.parser.Token;
import gian.compiler.practice.lexical.transform.LexConstants;
import gian.compiler.practice.syntactic.SyntacticLLParser;
import gian.compiler.practice.syntactic.SyntacticLRParser;
import gian.compiler.practice.syntactic.element.ItemCollection;
import gian.compiler.practice.syntactic.element.SyntaxProduct;
import gian.compiler.practice.syntactic.element.SyntaxSymbol;
import gian.compiler.practice.syntactic.element.SyntaxTree;
import gian.compiler.utils.ParseUtils;
import lex.test.LexUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaojian on 2019/3/20.
 */
public class SyntaxDirectedTest {

    public static LexUtils.UniversalTreeNode.UniversalTreeNodeMatch treeNodeMatcher = new LexUtils.UniversalTreeNode.UniversalTreeNodeMatch<SyntaxTree.SyntaxTreeNode>(){
        @Override
        public List<LexUtils.UniversalTreeNode> getChildTreeNode(SyntaxTree.SyntaxTreeNode targetNode) {
            List<SyntaxTree.SyntaxTreeNode> originSubTreeNodeList = targetNode.getSubProductNodeList();
            List<SyntaxSymbol> product = targetNode.getProductNode().getProduct();
            List<LexUtils.UniversalTreeNode> subTreeNode = new ArrayList<LexUtils.UniversalTreeNode>();

            int originSubTreeIndex=0;
            int productIndex=0;

            for(; productIndex < product.size(); productIndex++){
                if(originSubTreeNodeList.size() ==0
                        || originSubTreeIndex >= originSubTreeNodeList.size()
                        || !product.get(productIndex).equals(originSubTreeNodeList.get(originSubTreeIndex).getProductNode().getHead())){

                    SyntaxProduct tempProductNode = new SyntaxProduct(product.get(productIndex), new ArrayList<SyntaxSymbol>());
                    SyntaxTree.SyntaxTreeNode tempTreeNode = new SyntaxTree.SyntaxTreeNode(targetNode.getNumber()*1000+originSubTreeIndex, true ,tempProductNode);
                    subTreeNode.add(new LexUtils.UniversalTreeNode(tempTreeNode, this, false));

                }else{
                    subTreeNode.add(new LexUtils.UniversalTreeNode(originSubTreeNodeList.get(originSubTreeIndex), this, true));
                    originSubTreeIndex++;
                }
            }

            return subTreeNode;
        }
    };

    @Test
    public void testSyntaxTree(){
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token("id", LexExpression.TokenType.ID));
        tokens.add(new Token("*", LexExpression.TokenType.OPERATOR));
        tokens.add(new Token("id", LexExpression.TokenType.ID));
        tokens.add(new Token("+", LexExpression.TokenType.OPERATOR));
        tokens.add(new Token("id", LexExpression.TokenType.ID));
        tokens.add(new Token(LexConstants.SYNTAX_END, LexExpression.TokenType.END));

        List<String> syntaxs = new ArrayList<>();
        syntaxs.add("E → E + T | T ");
        syntaxs.add("T → T * F | F ");
        syntaxs.add("F → ( E ) | id ");

        List<SyntaxSymbol> syntaxSymbols = SyntacticLLParser.parseSyntaxSymbol(syntaxs);

        System.out.println("-------------------------------LR ItemCollectionNode----------------------------------");
        Map<Integer, ItemCollection> allLRItemCollectionMap = SyntacticLRParser.getLRItemCollectionMap(syntaxSymbols);

        System.out.println("-------------------------------Create LR PredictMap----------------------------------");
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap = SyntacticLLParser.syntaxFirst(syntaxSymbols);
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap = SyntacticLLParser.syntaxFollow(syntaxSymbols, syntaxFirstMap);
        Map<ItemCollection, Map<String, Map<SyntaxSymbol, List<Map<String, Object>>>>> predictLRMap = SyntacticLRParser.predictLRMap(allLRItemCollectionMap.get(0), syntaxSymbols, syntaxFirstMap, syntaxFollowMap);

        System.out.println("------------------------------LRPredictMap----------------------------------");
        SyntaxTree syntaxTree = SyntacticLRParser.syntaxParseLR(allLRItemCollectionMap.get(0), tokens, predictLRMap);

        System.out.println("------------------------------SyntaxTree----------------------------------");
        LexUtils.outputUniversalTreeEchart(new LexUtils.UniversalTreeNode(syntaxTree.getSyntaxTreeRoot(), treeNodeMatcher, true));

    }

    @Test
    public void testSyntaxTree1(){
        // 读取文法文件
        List<String> syntaxs = ParseUtils.getFile("syntaxContentFile.txt", true);

        // 解析目标语言文件生成词法单元数据
        List<Token> tokens = LexicalParser.parser(ParseUtils.getFile("compilerCode.txt", true), LexExpression.expressions);

        List<SyntaxSymbol> syntaxSymbols = SyntacticLLParser.parseSyntaxSymbol(syntaxs);

        System.out.println("-------------------------------LR ItemCollectionNode----------------------------------");
        Map<Integer, ItemCollection> allLRItemCollectionMap = SyntacticLRParser.getLRItemCollectionMap(syntaxSymbols);

        System.out.println("-------------------------------Create LR PredictMap----------------------------------");
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap = SyntacticLLParser.syntaxFirst(syntaxSymbols);
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap = SyntacticLLParser.syntaxFollow(syntaxSymbols, syntaxFirstMap);
        Map<ItemCollection, Map<String, Map<SyntaxSymbol, List<Map<String, Object>>>>> predictLRMap = SyntacticLRParser.predictLRMap(allLRItemCollectionMap.get(0), syntaxSymbols, syntaxFirstMap, syntaxFollowMap);

        System.out.println("------------------------------LRPredictMap----------------------------------");
        SyntaxTree syntaxTree = SyntacticLRParser.syntaxParseLR(allLRItemCollectionMap.get(0), tokens, predictLRMap);

        System.out.println("------------------------------SyntaxTree----------------------------------");
        LexUtils.outputUniversalTreeEchart(new LexUtils.UniversalTreeNode(syntaxTree.getSyntaxTreeRoot(), treeNodeMatcher, true), 300, 500);

    }

}