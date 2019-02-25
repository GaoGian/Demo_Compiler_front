import gian.compiler.practice.syntactic.SyntacticParser;
import gian.compiler.practice.syntactic.symbol.SyntaxSymbol;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Gian on 2019/2/19.
 */
public class SyntaxTest {

    @Test
    public void test(){
        List<String> syntaxs = new ArrayList<>();

        syntaxs.add("stmt → if expr then stmt else stmt | if stmt then stmt | begin stmtList end");
        syntaxs.add("stmtList → stmt ; stmtList | stmt | ");

        List<SyntaxSymbol> syntaxSymbols = SyntacticParser.parseSyntaxSymbol(syntaxs);

        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

    }

    @Test
    public void testEliminateLeftRecursion(){

        List<String> syntaxs = new ArrayList<>();

        syntaxs.add("E → E + T | T ");
        syntaxs.add("T → T + F | F ");
        syntaxs.add("F → ( E ) | id ");

        List<SyntaxSymbol> syntaxSymbols = SyntacticParser.parseSyntaxSymbol(syntaxs);

        // 消除前
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("----------------------------------------------------------------------");

        SyntacticParser.eliminateLeftRecursion(syntaxSymbols);

        // 消除后
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }
    }

    @Test
    public void testMergeCommonFactor(){

        List<String> syntaxs = new ArrayList<>();

//        syntaxs.add("stmt → if expr then stmt else stmt | if expr then stmt");
        syntaxs.add("T → a * F | a * F - F");

        List<SyntaxSymbol> syntaxSymbols = SyntacticParser.parseSyntaxSymbol(syntaxs);

        // 提取前
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("----------------------------------------------------------------------");

        SyntacticParser.mergeCommonFactor(syntaxSymbols);

        // 提取后
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }
    }

    @Test
    public void testMergeCommonFactor2(){

        List<String> syntaxs = new ArrayList<>();

        syntaxs.add("T → T * F | T * F - F ");
        syntaxs.add("F → id ");

        List<SyntaxSymbol> syntaxSymbols = SyntacticParser.parseSyntaxSymbol(syntaxs);

        // 消除前
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("-----------------------------左递归-------------------------------------");

        SyntacticParser.eliminateLeftRecursion(syntaxSymbols);

        // 消除后
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("-----------------------------公因式-------------------------------------");

        // TODO 不能正确处理提取公因式后的文法
        SyntacticParser.mergeCommonFactor(syntaxSymbols);

        // 提取公因式
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

    }


        @Test
    public void testSyntaxFirstAndFolloew(){

        List<String> syntaxs = new ArrayList<>();

        syntaxs.add("E → E + T | T ");
        syntaxs.add("T → T * F | F ");
        syntaxs.add("F → ( E ) | id ");

        List<SyntaxSymbol> syntaxSymbols = SyntacticParser.parseSyntaxSymbol(syntaxs);

        // 消除前
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("----------------------------------------------------------------------");

        SyntacticParser.eliminateLeftRecursion(syntaxSymbols);

        // 提取后
        for(SyntaxSymbol syntaxSymbol : syntaxSymbols) {
            System.out.println(syntaxSymbol);
        }

        System.out.println("-------------------------------FIRST-----------------------------------");
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap = SyntacticParser.syntaxFirst(syntaxSymbols);

        for(SyntaxSymbol symbol : syntaxFirstMap.keySet()) {
            System.out.println(symbol.getSymbol() + " -------- " + SyntacticParser.getSyntaxFirst(symbol, syntaxFirstMap));
        }

        System.out.println("-------------------------------FOLLOW----------------------------------");

        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> followMap = SyntacticParser.syntaxFollow(syntaxSymbols, syntaxFirstMap);

        for(SyntaxSymbol syntaxSymbol : syntaxSymbols){
            System.out.println(syntaxSymbol.getSymbol() + " -------- " + SyntacticParser.getSyntaxFollow(syntaxSymbol, followMap));
        }

    }


}
