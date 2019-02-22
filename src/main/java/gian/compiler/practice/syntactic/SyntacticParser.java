package gian.compiler.practice.syntactic;

import gian.compiler.practice.lexical.transform.LexConstants;
import gian.compiler.practice.syntactic.symbol.SyntaxSymbol;

import java.util.*;

/**
 * Created by gaojian on 2019/1/25.
 */
public class SyntacticParser {

    /**
     * 解析简单文法：
     *      stmt → if expr then stmt else stmt
     *            | if stmt then stmt
     *            | begin stmtList end
     *  stmtList → stmt ; stmtList | stmt | ε
     * @param syntaxs
     * @return
     */
    public static List<SyntaxSymbol> parseSyntaxSymbol(List<String> syntaxs){
        // 解析成终结符/非终结符
        Map<String, List<List<String>>> syntaxMap = new LinkedHashMap<>();
        for(String syntax : syntaxs){
            String head = syntax.split("→")[0];
            String bodys = syntax.split("→")[1];

            String[] products = bodys.split("\\|");
            for(String body : products){
                String[] symbols = body.trim().split(" ");
                List<String> symbolList = new ArrayList<>();
                if(symbols != null && symbols.length >0){
                    symbolList.addAll(Arrays.asList(symbols));
                }else{
                    // 如果是空转换则加入空字符串
                    symbolList.add(LexConstants.SYNTAX_EMPTY);
                }

                if(syntaxMap.get(head.trim()) == null){
                    List<List<String>> bodyList = new ArrayList<>();
                    bodyList.add(Arrays.asList(symbols));
                    syntaxMap.put(head.trim(), bodyList);
                }else{
                    syntaxMap.get(head.trim()).add(Arrays.asList(symbols));
                }
            }

        }

        // 如果在syntaxMap中，则是非终结符号
        Map<String, SyntaxSymbol> exitSymbolMap = new LinkedHashMap<>();
        List<SyntaxSymbol> syntaxSymbolList = new ArrayList<>();
        for(String head : syntaxMap.keySet()){
            SyntaxSymbol headSymbol = getSymbol(head, syntaxMap, exitSymbolMap);
            syntaxSymbolList.add(headSymbol);
        }

        return syntaxSymbolList;
    }

    // 如果产生体中有非终结符并且未解析过，优先解析子非终结符
    public static SyntaxSymbol getSymbol(String head, Map<String, List<List<String>>> syntaxMap, Map<String, SyntaxSymbol> exitSymbolMap){
        List<List<SyntaxSymbol>> productList = new ArrayList<>();
        List<List<String>> productExpressionList = syntaxMap.get(head);
        for(List<String> symbols : productExpressionList){
            List<SyntaxSymbol> productSymbols = new ArrayList<>();
            for(String symbol : symbols) {
                if(exitSymbolMap.get(symbol) == null) {
                    SyntaxSymbol bodySymbol = null;
                    if (syntaxMap.keySet().contains(symbol)) {
                        // 说明是非终结符
                        bodySymbol = new SyntaxSymbol(symbol, false);
                    } else {
                        // 说明是终结符
                        bodySymbol = new SyntaxSymbol(symbol, true);
                    }
                    exitSymbolMap.put(symbol, bodySymbol);
                    productSymbols.add(bodySymbol);
                }else{
                    productSymbols.add(exitSymbolMap.get(symbol));
                }
            }
            productList.add(productSymbols);
        }

        SyntaxSymbol headSymbol = null;
        if(exitSymbolMap.get(head) == null){
            headSymbol = new SyntaxSymbol(head, false);
        }else{
            headSymbol = exitSymbolMap.get(head);
        }
        headSymbol.setBody(productList);

        return headSymbol;
    }

    /**
     * 消除左递归（P134）
     * @param originSyntaxSymbolList
     * @return
     */
    public static void eliminateLeftRecursion(List<SyntaxSymbol> originSyntaxSymbolList){
        // TODO 将原文法按照上下级关系排序

        for(int i=0; i<originSyntaxSymbolList.size(); i++){

            SyntaxSymbol currSyntaxSymbol = originSyntaxSymbolList.get(i);
            List<List<SyntaxSymbol>> currProductBodys = currSyntaxSymbol.getBody();

            // 消除对上级的左递归
            for(int j=0; j<i-1; j++){
                // 判断当前文法是否依赖上级（所有上级）
                SyntaxSymbol preSyntaxSymbol = originSyntaxSymbolList.get(j);
                for(int k=0; k<currProductBodys.size(); k++){
                    List<SyntaxSymbol> currProductBody = currProductBodys.get(k);
                    // 不处理ε产生体
                    // TODO 确认ε产生体是什么样的，是长度为1，并且symbol为""
                    if(currProductBody.size() >= 1 && !currProductBody.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)) {
                        // 判断产生体首位是否和上级相同
                        if (currProductBody.get(0).getSymbol().equals(preSyntaxSymbol.getSymbol())) {
                            // 清除该产生式，后面需要进行替换
                            currProductBodys.remove(k);
                            // 修正遍历位置
                            k--;

                            // 将产生体首位替换成所有上级的所有产生体  TODO 需要处理环和ε产生体
                            // 去掉依赖的上级头部
                            List<SyntaxSymbol> tempProductBody = currProductBody.subList(1, currProductBody.size());
                            List<List<SyntaxSymbol>> preProductBodys = preSyntaxSymbol.getBody();
                            for (int l = 0; l < preProductBodys.size(); l++) {
                                List<SyntaxSymbol> preProductBody = preProductBodys.get(l);
                                // TODO 确认ε产生体是什么样的，是长度为1，并且symbol为""
                                if (preProductBody.size() >= 1 && !preProductBody.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)) {
                                    List<SyntaxSymbol> newCurrProductBody = new ArrayList<>();
                                    // 将产生体首位替换成所有上级的所有产生体
                                    newCurrProductBody.addAll(preProductBody);
                                    newCurrProductBody.addAll(tempProductBody);

                                    // 将替换的产生式加入到产生式列表
                                    currProductBodys.add(newCurrProductBody);
                                }
                            }
                        }
                    }
                }
            }

            // 消除对自己的左递归
            // 判断是否有左递归
            boolean isLeftRecursion = false;
            List<List<SyntaxSymbol>> leftRecursionList = new ArrayList<>();
            List<List<SyntaxSymbol>> unLeftRecursionList = new ArrayList<>();
            for(int m=0; m<currProductBodys.size(); m++) {
                List<SyntaxSymbol> currProductBody = currProductBodys.get(m);
                // 判断产生体是否左递归
                if(currProductBody.size() > 0) {
                    if (currProductBody.get(0).getSymbol().equals(currSyntaxSymbol.getSymbol())) {
                        leftRecursionList.add(currProductBody);
                        // 标记有左递归
                        isLeftRecursion = true;
                    } else {
                        unLeftRecursionList.add(currProductBody);
                    }
                }else{
                    unLeftRecursionList.add(currProductBody);
                }
            }
            // 消除左递归
            if(isLeftRecursion){
                // 生成消除左递归的文法
                SyntaxSymbol eliminateSyntaxSymbol = new SyntaxSymbol(currSyntaxSymbol.getSymbol() + "'", false);
                List<List<SyntaxSymbol>> eliminateProductBodys = new ArrayList<>();
                for(List<SyntaxSymbol> leftRecursionBody : leftRecursionList){
                    // 消除左递归首位
                    leftRecursionBody = leftRecursionBody.subList(1, leftRecursionBody.size());
                    // 加上消除左递归文法
                    leftRecursionBody.add(eliminateSyntaxSymbol);
                    eliminateProductBodys.add(leftRecursionBody);
                }
                // 加上空表达式
                List<SyntaxSymbol> emptyBody = new ArrayList<>();
                emptyBody.add(new SyntaxSymbol(LexConstants.SYNTAX_EMPTY, true));
                eliminateProductBodys.add(emptyBody);
                // 设置消除左递归文法的产生体
                eliminateSyntaxSymbol.setBody(eliminateProductBodys);

                // 将消除的左递归文法符号加入文法列表
                if(i < (originSyntaxSymbolList.size()-1)) {
                    List<SyntaxSymbol> newPreSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(0, i + 1));
                    List<SyntaxSymbol> newSubSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(i + 1, originSyntaxSymbolList.size()));
                    originSyntaxSymbolList.clear();
                    originSyntaxSymbolList.addAll(newPreSyntaxSymbolList);
                    originSyntaxSymbolList.add(eliminateSyntaxSymbol);
                    originSyntaxSymbolList.addAll(newSubSyntaxSymbolList);
                }else{
                    originSyntaxSymbolList.add(eliminateSyntaxSymbol);
                }

                // 转化原来的左递归文法
                for(List<SyntaxSymbol> unLeftRecursionBody : unLeftRecursionList){
                    // 判断是否是ε产生体，先清空ε产生式
                    if(unLeftRecursionBody.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)){
                        unLeftRecursionBody.clear();
                    }
                    // 在原来没有左递归的产生体后面加上消除左递归的文法符号
                    unLeftRecursionBody.add(eliminateSyntaxSymbol);
                }
                // 替换原来的左递归产生体
                currSyntaxSymbol.setBody(unLeftRecursionList);
            }
        }

    }

    /**
     * 提取左公因式       TODO 需要优化
     * @param originSyntaxSymbolList
     */
    public static void mergeCommonFactor(List<SyntaxSymbol> originSyntaxSymbolList){

        for(int i=0; i<originSyntaxSymbolList.size(); i++){

            SyntaxSymbol syntaxSymbol = originSyntaxSymbolList.get(i);
            List<List<SyntaxSymbol>> originProductBodys = syntaxSymbol.getBody();

            // 记录相同左公因式的产生式，key：公因式，value：产生式
            Map<List<SyntaxSymbol>, List<List<SyntaxSymbol>>> group = new LinkedHashMap<>();
            // 初始分组
            boolean hasCommonFactor = false;
            for (int j = 0; j < originProductBodys.size(); j++) {
                List<SyntaxSymbol> productBody = originProductBodys.get(j);
                List<SyntaxSymbol> groupKey = new ArrayList<>();
                groupKey.add(productBody.get(0));
                if(group.get(groupKey) == null){
                    List<List<SyntaxSymbol>> groupElement = new ArrayList<>();
                    groupElement.add(productBody);
                    group.put(groupKey, groupElement);
                }else{
                    group.get(groupKey).add(productBody);
                    hasCommonFactor = true;
                }
            }

            // 如果本来就没有公因式则直接返回
            if(!hasCommonFactor){
                break;
            }

            // TODO 需要考虑多个产生式递增公因式的情况，例如：abc | abdd | abde      暂时不处理
            // 继续分组
            Map<List<SyntaxSymbol>, List<List<SyntaxSymbol>>> result = new LinkedHashMap<>();
            while (group.size() > 0) {
                Map<List<SyntaxSymbol>, List<List<SyntaxSymbol>>> tranGroup = new LinkedHashMap<>();
                Iterator<List<SyntaxSymbol>> iterator = group.keySet().iterator();
                while(iterator.hasNext()){
                    List<SyntaxSymbol> groupKey = iterator.next();
                    List<List<SyntaxSymbol>> groupElement = group.get(groupKey);
                    // 判断之前同一组的元素是否有后继公因式
                    Map<SyntaxSymbol, List<List<SyntaxSymbol>>> tempGroup = new LinkedHashMap<>();
                    // FIXME 暂时这样处理公因式长度超过产生式长度
                    SyntaxSymbol nullSymbol = new SyntaxSymbol("null", true);
                    for(List<SyntaxSymbol> product : groupElement){

                        if(product.size() > groupKey.size()){
                            SyntaxSymbol tempGroupKey = product.get(groupKey.size());
                            if(tempGroup.get(tempGroupKey) == null){
                                List<List<SyntaxSymbol>> tempGroupElement = new ArrayList<>();
                                tempGroupElement.add(product);
                                tempGroup.put(tempGroupKey, tempGroupElement);
                            }else{
                                tempGroup.get(tempGroupKey).add(product);
                            }

                        }else{
                            // FIXME 暂时这样处理公因式长度超过产生式长度
                            List<List<SyntaxSymbol>> tempGroupElement = new ArrayList<>();
                            tempGroupElement.add(product);
                            tempGroup.put(nullSymbol, tempGroupElement);
                        }
                    }

                    // 判断是否还有相同的公因式
                    if(tempGroup.size() == 1){
                        // 还有相同的公因式，更新公因式
                        groupKey.addAll(tempGroup.keySet());
                        tranGroup.put(groupKey, groupElement);
                    }else{
                        // TODO 需要考虑多个产生式递增公因式的情况，例如：abc | abdd | abde      暂时不处理
                        // 没有后续的公因子，返回最长公因式
                        result.put(groupKey, groupElement);
                        iterator.remove();
                    }
                }

                group = tranGroup;
            }

            // 根据左公因式转换文法
            originProductBodys.clear();
            for(List<SyntaxSymbol> groupKey : result.keySet()){

                // 处理非公共部分
                List<List<SyntaxSymbol>> groupElement = result.get(groupKey);
                if(groupElement.size() > 1){
                    SyntaxSymbol commonFactorSymbol = new SyntaxSymbol(syntaxSymbol.getSymbol() + "~", false);
                    for(List<SyntaxSymbol> originSyntaxSymbol : groupElement){
                        originSyntaxSymbol.removeAll(groupKey);
                        if(originSyntaxSymbol.size() > 0){
                            originSyntaxSymbol.add(syntaxSymbol);
                            commonFactorSymbol.getBody().add(originSyntaxSymbol);
                        }
                    }
                    // 加上空表达式
                    List<SyntaxSymbol> emptyBody = new ArrayList<>();
                    emptyBody.add(new SyntaxSymbol(LexConstants.SYNTAX_EMPTY, true));
                    commonFactorSymbol.getBody().add(emptyBody);

                    // 先加上公因式表达式
                    List<SyntaxSymbol> newCommonProduct = new ArrayList<>(groupKey);
                    newCommonProduct.add(commonFactorSymbol);
                    originProductBodys.add(newCommonProduct);

                    // 将提取的左公因式文法符号加入文法列表
                    if(i < (originSyntaxSymbolList.size()-1)) {
                        List<SyntaxSymbol> newPreSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(0, i + 1));
                        List<SyntaxSymbol> newSubSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(i + 1, originSyntaxSymbolList.size()));
                        originSyntaxSymbolList.clear();
                        originSyntaxSymbolList.addAll(newPreSyntaxSymbolList);
                        originSyntaxSymbolList.add(commonFactorSymbol);
                        originSyntaxSymbolList.addAll(newSubSyntaxSymbolList);
                    }else{
                        originSyntaxSymbolList.add(commonFactorSymbol);
                    }

                }else{
                    originProductBodys.add(groupKey);
                }

            }


        }

    }

    /**
     * 计算文法符号 FIRST 集合
     */
    public static Set<String> syntaxFirst(SyntaxSymbol syntaxSymbol){

        Set<String> firstCollection = new HashSet<>();
        if(syntaxSymbol.isTerminal()){
            // 如果是终结符，则直接返回对应的字符串
            firstCollection.add(syntaxSymbol.getSymbol());
        }else{
            // 1、如果是非终结符，则加入产生体首个文法符号的 FIRST 集合
            // 2、如果该文法符号能够推导出ε，则加入下一个文法符号的 FIRST 集合，以此类推知道末尾
            // 3、如果文法符号本身能够推导出ε，则加入ε
            List<List<SyntaxSymbol>> productList = syntaxSymbol.getBody();
            for(List<SyntaxSymbol> product : productList){
                if(!product.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)){
                    // 1、加入产生式文法符号的 FIRST 集合
                    for(SyntaxSymbol symbol : product){
                        Set<String> symbolFirst = syntaxFirst(symbol);
                        firstCollection.addAll(symbolFirst);
                        // 2、如果该文法符号能够推导出ε，则加入下一个文法符号的 FIRST 集合
                        if(!symbolFirst.contains(LexConstants.SYNTAX_EMPTY)){
                            break;
                        }
                    }
                }else{
                    // 3、说明是ε产生式，直接加入
                    firstCollection.add(LexConstants.SYNTAX_EMPTY);
                }
            }
        }

        return firstCollection;
    }

    /**
     * 计算文法符号 FOLLOW 集合
     */
    public static Map<SyntaxSymbol, Set<String>> syntaxFollow(SyntaxSymbol startSyntaxSymbol){
        Map<SyntaxSymbol, Set<String>> followCollectionMap = new HashMap<>();
        Set<String> startSyntaxSymbolFollow = new HashSet<>();
        startSyntaxSymbolFollow.add(LexConstants.SYNTAX_EMPTY);
        followCollectionMap.put(startSyntaxSymbol, startSyntaxSymbolFollow);


        return followCollectionMap;
    }

    private static Set<String> getProductFollow(List<SyntaxSymbol> product){
        Set<String> productFollow = new HashSet<>();

        if(product.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)){
            return productFollow;
        }

        for(int i=0; i<product.size(); i++){
            SyntaxSymbol preSyntaxSymbol = product.get(i);
            SyntaxSymbol subSyntaxSymbol = product.get(i+1);
        }

        return productFollow;
    }

}