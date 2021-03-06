package gian.compiler.front.syntactic;

import gian.compiler.front.exception.ParseException;
import gian.compiler.front.lexical.parser.LexExpression;
import gian.compiler.front.lexical.parser.LexicalParser;
import gian.compiler.front.lexical.parser.Token;
import gian.compiler.front.lexical.transform.LexConstants;
import gian.compiler.front.lexical.transform.MyStack;
import gian.compiler.front.syntactic.element.SyntaxProduct;
import gian.compiler.front.syntactic.element.SyntaxSymbol;
import gian.compiler.utils.ParseUtils;

import java.util.*;

/**
 * Created by gaojian on 2019/1/25.
 */
public class SyntacticLLParser {

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
            if(i>0) {
                for (int j = 0; j < i - 1; j++) {
                    // 判断当前文法是否依赖上级（所有上级）
                    SyntaxSymbol preSyntaxSymbol = originSyntaxSymbolList.get(j);
                    for (int k = 0; k < currProductBodys.size(); k++) {
                        List<SyntaxSymbol> currProductBody = currProductBodys.get(k);
                        // 不处理ε产生体
                        // TODO 确认ε产生体是什么样的，是长度为1，并且symbol为""
                        if (currProductBody.size() >= 1 && !currProductBody.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)) {
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

                // 说明所有产生体都是左递归，加入转换后的文法符号
                if(unLeftRecursionList.size() == 0){
                    List<SyntaxSymbol> tempProduct = new ArrayList<>();
                    tempProduct.add(eliminateSyntaxSymbol);
                    unLeftRecursionList.add(tempProduct);
                }

                // 替换原来的左递归产生体
                currSyntaxSymbol.setBody(unLeftRecursionList);
            }
        }

    }

    /**
     * 提取左公因式
     * TODO 需要优化
     * FIXME 不能正确处理提取公因式后的文法
     * @param originSyntaxSymbolList
     */
    public static void mergeCommonFactor(List<SyntaxSymbol> originSyntaxSymbolList){

        for(int i=0; i<originSyntaxSymbolList.size(); i++){

            SyntaxSymbol syntaxSymbol = originSyntaxSymbolList.get(i);
            List<List<SyntaxSymbol>> originProductBodys = syntaxSymbol.getBody();

            boolean hasEmpty = false;
            for(List<SyntaxSymbol> product : originProductBodys){
                if(product.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)){
                    hasEmpty = true;
                    break;
                }
            }

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
            if(hasCommonFactor) {

                // TODO 需要考虑多个产生式递增公因式的情况，例如：abc | abdd | abde      暂时不处理
                // 继续分组
                Map<List<SyntaxSymbol>, List<List<SyntaxSymbol>>> result = new LinkedHashMap<>();
                while (group.size() > 0) {
                    Map<List<SyntaxSymbol>, List<List<SyntaxSymbol>>> tranGroup = new LinkedHashMap<>();
                    Iterator<List<SyntaxSymbol>> iterator = group.keySet().iterator();
                    while (iterator.hasNext()) {
                        // 对每个分组再进行分组
                        List<SyntaxSymbol> groupKey = iterator.next();
                        List<List<SyntaxSymbol>> groupElement = group.get(groupKey);
                        if (groupElement.size() > 1) {
                            // 判断之前同一组的元素是否有后继公因式
                            Map<SyntaxSymbol, List<List<SyntaxSymbol>>> tempGroup = new LinkedHashMap<>();
                            // FIXME 暂时这样处理公因式长度超过产生式长度
                            SyntaxSymbol nullSymbol = new SyntaxSymbol("null", true);
                            for (List<SyntaxSymbol> product : groupElement) {

                                if (product.size() > groupKey.size()) {
                                    SyntaxSymbol tempGroupKey = product.get(groupKey.size());
                                    if (tempGroup.get(tempGroupKey) == null) {
                                        List<List<SyntaxSymbol>> tempGroupElement = new ArrayList<>();
                                        tempGroupElement.add(product);
                                        tempGroup.put(tempGroupKey, tempGroupElement);
                                    } else {
                                        tempGroup.get(tempGroupKey).add(product);
                                    }

                                } else {
                                    // FIXME 暂时这样处理公因式长度超过产生式长度
                                    List<List<SyntaxSymbol>> tempGroupElement = new ArrayList<>();
                                    tempGroupElement.add(product);
                                    if (tempGroup.get(nullSymbol) == null) {
                                        tempGroup.put(nullSymbol, tempGroupElement);
                                    } else {
                                        if (!tempGroup.get(nullSymbol).contains(product)) {
                                            tempGroup.get(nullSymbol).add(product);
                                        }
                                    }
                                }
                            }

                            // 判断是否还有相同的公因式
                            if (tempGroup.size() == 1) {
                                // 还有相同的公因式，更新公因式
                                groupKey.addAll(tempGroup.keySet());
                                tranGroup.put(groupKey, groupElement);
                            } else {
                                // TODO 需要考虑多个产生式递增公因式的情况，例如：abc | abdd | abde      暂时不处理
                                // 没有后续的公因子，返回最长公因式
                                result.put(groupKey, groupElement);
                                iterator.remove();
                            }
                        }else{
                            // 说明只有一个产生式，不需要在进行分组，直接加入到结果
                            result.put(groupKey, groupElement);
                            iterator.remove();
                        }
                    }

                    group = tranGroup;
                }

                // 根据左公因式转换文法
                originProductBodys.clear();

                for (List<SyntaxSymbol> groupKey : result.keySet()) {

                    // 处理非公共部分
                    List<List<SyntaxSymbol>> groupElement = result.get(groupKey);
                    if (groupElement.size() > 1) {
                        SyntaxSymbol commonFactorSymbol = new SyntaxSymbol(syntaxSymbol.getSymbol() + "~", false);
                        for (List<SyntaxSymbol> originSyntaxSymbol : groupElement) {
                            for(int m=0; m<groupKey.size(); m++) {
                                if(groupKey.get(m).equals(originSyntaxSymbol.get(0))) {
                                    originSyntaxSymbol.remove(0);
                                }
                            }
                            if (originSyntaxSymbol.size() > 0) {
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
                        if (i < (originSyntaxSymbolList.size() - 1)) {
                            List<SyntaxSymbol> newPreSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(0, i + 1));
                            List<SyntaxSymbol> newSubSyntaxSymbolList = new ArrayList<>(originSyntaxSymbolList.subList(i + 1, originSyntaxSymbolList.size()));
                            originSyntaxSymbolList.clear();
                            originSyntaxSymbolList.addAll(newPreSyntaxSymbolList);
                            originSyntaxSymbolList.add(commonFactorSymbol);
                            originSyntaxSymbolList.addAll(newSubSyntaxSymbolList);
                        } else {
                            originSyntaxSymbolList.add(commonFactorSymbol);
                        }

                    } else {
                        originProductBodys.add(groupKey);
                    }

                }

                if(hasEmpty){
                    List<SyntaxSymbol> emptyProduct = new ArrayList<>();
                    emptyProduct.add(new SyntaxSymbol(LexConstants.SYNTAX_EMPTY, true));
                    if(!originProductBodys.contains(emptyProduct)) {
                        originProductBodys.add(emptyProduct);
                    }
                }

            }
        }

    }

    /**
     * 计算文法符号 FIRST 集合
     *
     * TODO FIRST 集合是根据文法符号计算，跟产生式和位置无关
     * TODO FIRST 集合是产生式体的 FIRST 集合，如果一个文法符号有多个产生式体，则每个产生式体都有对应的FIRST集合
     *
     * TODO 需要保证文法已经处理过“左递归”、“抽取公因式”
     *
     * 一直执行下列规则，知道没有新的中介符号或ε加入到FIRST集合
     * 1、如果是非终结符，则加入产生体首个文法符号的 FIRST 集合
     * 2、如果该文法符号能够推导出ε，则加入下一个文法符号的 FIRST 集合，以此类推知道末尾
     * 3、如果文法符号本身能够推导出ε，则加入ε
     */
    public static Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirst(List<SyntaxSymbol> syntaxSymbols){

        // 存储每个文法符号的每个产生体的 FIRST 集合
        // key：文法符号；二级key：产生式，value：FIRST集合
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap = new LinkedHashMap<>();
        boolean hasNewFirstElement = true;
        while(hasNewFirstElement) {
            // 循环出口
            boolean newTag = false;
            for (int i=(syntaxSymbols.size()-1); i>=0; i--) {
                SyntaxSymbol syntaxSymbol = syntaxSymbols.get(i);
                Map<List<SyntaxSymbol>, Set<String>> syntaxProductFirstMap = getSyntaxProductFirstMap(syntaxSymbol, syntaxFirstMap);
                for (List<SyntaxSymbol> product : syntaxSymbol.getBody()) {
                    if(product.get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)){
                        // 3、说明是ε产生式，直接加入
                        if(addSyntaxProductFirst(syntaxSymbol, product, LexConstants.SYNTAX_EMPTY, syntaxFirstMap)){
                            newTag = true;
                        }
                    }else{
                        if(product.get(0).isTerminal()) {
                            // 1、如果是终结符，则直接返回对应的字符串
                            if(addSyntaxProductFirst(syntaxSymbol, product, product.get(0).getSymbol(), syntaxFirstMap)){
                                newTag = true;
                            }
                        }else{
                            // 2、加入产生式体首个文法符号的FIRST，如果产生式前面的文法符号能够推导出ε，则加入后面文法符号的 FIRST 集合
                            for(int j=0; j<product.size(); j++) {
                                SyntaxSymbol productSymbol = product.get(j);
                                if(!productSymbol.isTerminal()) {
                                    Set<String> targetSyntaxFirst = getSyntaxFirst(productSymbol, syntaxFirstMap);
                                    // 2、如果该文法符号能够推导出ε，则加入下一个文法符号的 FIRST 集合
                                    boolean hasEmpty = targetSyntaxFirst.contains(LexConstants.SYNTAX_EMPTY);
                                    if (hasEmpty) {
                                        // 如果产生式最后一个符号还能够推导出ε，则加入ε
                                        if (j < product.size() - 1) {
                                            targetSyntaxFirst.remove(LexConstants.SYNTAX_EMPTY);
                                        }
                                        if (addSourceSyntaxFirst(syntaxSymbol, product, targetSyntaxFirst, syntaxFirstMap)) {
                                            newTag = true;
                                        }
                                    } else {
                                        if (addSourceSyntaxFirst(syntaxSymbol, product, targetSyntaxFirst, syntaxFirstMap)) {
                                            newTag = true;
                                        }
                                        break;
                                    }
                                }else{
                                    // 1、如果是终结符，则直接返回对应的字符串
                                    if(addSyntaxProductFirst(syntaxSymbol, product, productSymbol.getSymbol(), syntaxFirstMap)){
                                        newTag = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            hasNewFirstElement = newTag;
        }

        return syntaxFirstMap;
    }

    public static Map<List<SyntaxSymbol>, Set<String>> getSyntaxProductFirstMap(SyntaxSymbol syntaxSymbol, Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){
        if(syntaxFirstMap.get(syntaxSymbol) == null){
            Map<List<SyntaxSymbol>, Set<String>> syntaxProductFirstMap = new LinkedHashMap<>();
            syntaxFirstMap.put(syntaxSymbol, syntaxProductFirstMap);
        }

        return syntaxFirstMap.get(syntaxSymbol);
    }

    public static Set<String> getSyntaxFirst(SyntaxSymbol targetSymbol, Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){
        if(syntaxFirstMap.get(targetSymbol) == null){
            Map<List<SyntaxSymbol>, Set<String>> productFirstMap = new LinkedHashMap<>();
            syntaxFirstMap.put(targetSymbol, productFirstMap);
        }

        Set<String> targetSyntaxFirst = new HashSet<>();
        for(List<SyntaxSymbol> sourceProduct : syntaxFirstMap.get(targetSymbol).keySet()) {
            targetSyntaxFirst.addAll(syntaxFirstMap.get(targetSymbol).get(sourceProduct));
        }

        return targetSyntaxFirst;
    }

    public static boolean addSourceSyntaxFirst(SyntaxSymbol targetSymbol, List<SyntaxSymbol> targetProduct, Set<String> sourceSymbolFirst,
                                 Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){

        boolean hasNewFirstElement = false;

        if(syntaxFirstMap.get(targetSymbol) == null){
            Map<List<SyntaxSymbol>, Set<String>> productFirstMap = new LinkedHashMap<>();
            syntaxFirstMap.put(targetSymbol, productFirstMap);
        }
        if(syntaxFirstMap.get(targetSymbol).get(targetProduct) == null){
            Set<String> first = new HashSet<>();
            syntaxFirstMap.get(targetSymbol).put(targetProduct, first);
        }

        // 递归出口
        if(!syntaxFirstMap.get(targetSymbol).get(targetProduct).containsAll(sourceSymbolFirst)){
            syntaxFirstMap.get(targetSymbol).get(targetProduct).addAll(sourceSymbolFirst);
            hasNewFirstElement = true;
        }

        return hasNewFirstElement;
    }

    public static boolean addSyntaxProductFirst(SyntaxSymbol syntaxSymbol, List<SyntaxSymbol> product, String firstSymbol,
                                       Map<SyntaxSymbol,Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){

        boolean hasNewFirstElement = false;

        if(syntaxFirstMap.get(syntaxSymbol) == null){
            Map<List<SyntaxSymbol>, Set<String>> productFirstMap = new LinkedHashMap<>();
            syntaxFirstMap.put(syntaxSymbol, productFirstMap);
        }
        if(syntaxFirstMap.get(syntaxSymbol).get(product) == null){
            Set<String> first = new HashSet<>();
            syntaxFirstMap.get(syntaxSymbol).put(product, first);
        }

        // 递归出口
        if(!syntaxFirstMap.get(syntaxSymbol).get(product).contains(firstSymbol)){
            syntaxFirstMap.get(syntaxSymbol).get(product).add(firstSymbol);
            hasNewFirstElement = true;
        }

        return hasNewFirstElement;
    }

    /**
     * 计算文法符号 FOLLOW 集合
     *
     * TODO 需要保证文法已经处理过“左递归”、“抽取公因式”
     * TODO FOLLOW 集合应该是根据某个文法在某个产生式的某个位置计算，但是由于以下原因不需要考虑
     *      A、处理过左递归和提取过公因式，同一文法符号的不同产生式的 FIRST 集合不相交
     *      B、
     *
     * FIXME 感觉龙书上的 FOLLOW 集合没有对产生式不同位置的同一文法符号的FOLLOW作区分（是所有集合额总集），
     * FIXME 应该像LALR那样，由上到下做传播，这样就能区分同一文法符号在不同位置不同状态具有不同的FOLLOW集合
     *
     * 1、起始文法符号 S 的 $ ∈ FOLLOW(S)
     * 2、产生式 A→αBβ，(FIRST(β)-ε) ∈ FOLLOW(B)
     * 3、产生式 A→αB (B在产生式尾部) 或 A→αBβ（其中ε∈ FIRST(β)），那么 FOLLOW(A) ∈ FOLLOW(B)
     */
    public static Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollow(List<SyntaxSymbol> syntaxSymbols,
                                                                                                     Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){

        // 修改文法列表，在首位加入 ^S → S，方便记录 FOLOW 集合
        List<SyntaxSymbol> tempProduct= new ArrayList<>();
        tempProduct.add(syntaxSymbols.get(0));
        List<List<SyntaxSymbol>> tempProductList = new ArrayList<>();
        tempProductList.add(tempProduct);
        List<SyntaxSymbol> tempSyntaxSymbols = new ArrayList<>();
        tempSyntaxSymbols.add(new SyntaxSymbol("^" + syntaxSymbols.get(0).getSymbol(), false, tempProductList));

        // 记录所有符号在不同产生式的不同位置的 FOLLOW 集合
        // key: 文法符号，二级key：产生式，三级key：产生式位置，value：FOLLOW集合
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap = new LinkedHashMap<>();

        // 1、起始文法符号 S 的 FOLLOW 加入 $
        recordSymbolFollowMap(syntaxFollowMap, syntaxSymbols.get(0), tempProduct, 0, LexConstants.SYNTAX_END);

        boolean hasNewFollowElement = true;
        while(hasNewFollowElement) {
            // 循环出口
            boolean newTag = false;
            for (int i = 0; i < syntaxSymbols.size(); i++) {
                SyntaxSymbol syntaxSymbol = syntaxSymbols.get(i);

                for(List<SyntaxSymbol> product : syntaxSymbol.getBody()){
                    for(int j=0; j<product.size(); j++){
                        SyntaxSymbol symbol = product.get(j);
                        if(!symbol.isTerminal()) {
                            if (j < product.size() - 1) {
                                // 2、产生式 A→αBβ，(FIRST(β)-ε) ∈ FOLLOW(B)
                                SyntaxSymbol nextSymbol = product.get(j + 1);
                                if (nextSymbol.isTerminal()) {
                                    if (recordSymbolFollowMap(syntaxFollowMap, symbol, product, j, nextSymbol.getSymbol())) {
                                        newTag = true;
                                    }
                                } else {
                                    Set<String> nextFirst = getSyntaxFirst(nextSymbol, syntaxFirstMap);
                                    boolean hasEmpty = nextFirst.contains(LexConstants.SYNTAX_EMPTY);
                                    nextFirst.remove(LexConstants.SYNTAX_EMPTY);
                                    if (recordSymbolFollowMap(syntaxFollowMap, symbol, product, j, nextFirst)) {
                                        newTag = true;
                                    }

                                    // 3、产生式A→αBβ（其中ε∈ FIRST(β)），那么 FOLLOW(A) ∈ FOLLOW(B)
                                    if(hasEmpty && j == product.size() - 2) {
                                        if (recordSymbolFollowMap(syntaxFollowMap, symbol, product, j, getSyntaxFollow(syntaxSymbol, syntaxFollowMap))) {
                                            newTag = true;
                                        }
                                    }
                                }
                            } else {
                                // 3、产生式 A→αB (B在产生式尾部)，那么 FOLLOW(A) ∈ FOLLOW(B)
                                if (recordSymbolFollowMap(syntaxFollowMap, symbol, product, j, getSyntaxFollow(syntaxSymbol, syntaxFollowMap))) {
                                    newTag = true;
                                }
                            }
                        }
                    }
                }

            }
            hasNewFollowElement = newTag;
        }

        return syntaxFollowMap;
    }

    public static Set<String> getSyntaxFollow(SyntaxSymbol syntaxSymbol, Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap){
        if(syntaxFollowMap.get(syntaxSymbol) == null){
            Map<List<SyntaxSymbol>, Map<Integer, Set<String>>> productFollowMap = new LinkedHashMap<>();
            syntaxFollowMap.put(syntaxSymbol, productFollowMap);
        }

        Set<String> follow = new HashSet<>();
        for(List<SyntaxSymbol> product : syntaxFollowMap.get(syntaxSymbol).keySet()){
            for(Integer index : syntaxFollowMap.get(syntaxSymbol).get(product).keySet()){
                // FIXME 其实这里是个超集，需要根据所处的推导位置计算可行的follow，类似LALR那样
                follow.addAll(syntaxFollowMap.get(syntaxSymbol).get(product).get(index));
            }
        }
        return follow;
    }

    public static boolean recordSymbolFollowMap(Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap,
                                                 SyntaxSymbol syntaxSymbol, List<SyntaxSymbol> product, Integer index, String followSymbol){
        boolean hasNewFollow = false;
        if(syntaxFollowMap.get(syntaxSymbol) == null){
            Map<List<SyntaxSymbol>, Map<Integer, Set<String>>> productFollowMap = new LinkedHashMap<>();
            syntaxFollowMap.put(syntaxSymbol, productFollowMap);
        }
        if(syntaxFollowMap.get(syntaxSymbol).get(product) == null){
            Map<Integer, Set<String>> indexFollowMap = new LinkedHashMap<>();
            syntaxFollowMap.get(syntaxSymbol).put(product, indexFollowMap);
        }
        if(syntaxFollowMap.get(syntaxSymbol).get(product).get(index) == null){
            Set<String> follow = new HashSet<>();
            syntaxFollowMap.get(syntaxSymbol).get(product).put(index, follow);
        }

        if(!syntaxFollowMap.get(syntaxSymbol).get(product).get(index).contains(followSymbol)){
            syntaxFollowMap.get(syntaxSymbol).get(product).get(index).add(followSymbol);
            hasNewFollow = true;
        }

        return hasNewFollow;
    }

    public static boolean recordSymbolFollowMap(Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap,
                                                 SyntaxSymbol syntaxSymbol, List<SyntaxSymbol> product, Integer index, Set<String> followSymbols){
        boolean hasNewFollow = false;
        if(syntaxFollowMap.get(syntaxSymbol) == null){
            Map<List<SyntaxSymbol>, Map<Integer, Set<String>>> productFollowMap = new LinkedHashMap<>();
            syntaxFollowMap.put(syntaxSymbol, productFollowMap);
        }
        if(syntaxFollowMap.get(syntaxSymbol).get(product) == null){
            Map<Integer, Set<String>> indexFollowMap = new LinkedHashMap<>();
            syntaxFollowMap.get(syntaxSymbol).put(product, indexFollowMap);
        }
        if(syntaxFollowMap.get(syntaxSymbol).get(product).get(index) == null){
            Set<String> follow = new HashSet<>();
            syntaxFollowMap.get(syntaxSymbol).get(product).put(index, follow);
        }

        if(!syntaxFollowMap.get(syntaxSymbol).get(product).get(index).containsAll(followSymbols)){
            syntaxFollowMap.get(syntaxSymbol).get(product).get(index).addAll(followSymbols);
            hasNewFollow = true;
        }

        return hasNewFollow;
    }


    /**
     * 构造预测分析表
     *
     * TODO 对于第二点的思考：
     * TODO 1、产生式 A→αBβ，FIRST(B)=>ε，是否存在FIRST(B)与FOLLOW(B)相交的情况（或者是产生式 A→αB，FIRST(B)与FOLLOW(A)相交），有的话如果出现交集中的符号该如何处理？？  可以相交，直接选取B作为扩展的符号
     *      首先FOLLOW\(B)=FIRST(β)，同样的FOLLOW(A)是FIRST(A_next)，由于最小文法符号单元标识不同的词法单元，因此文法符号归结到最小时只有不同的FIRST集合，
     *      如果FIRST(B)与FOLLOW(B)即FIRST(β)有交集的话，那只能说明符号B与β具有相同的首位最小单元文法符号，这在文法上市可以避免的：
     *          可以将符号B和β抽取公因式，
     *          如果是两个文法在同一产生体级别前后出现的话，可以合并成一个大的文法符号，类似stmtList
     *          如果两个文法实在两个上下产生体级别前后出现，例如（while stmt）stmt，一般来讲在子我们是需要stmt有意义的，不能推导出ε，如果为空就会拿后一条文法作为实体，那就没区别了，只有他的连续产生体stmts才会推导出ε，如果需要stmts那就必须通过分界符的方式将他与后续符号隔开
     *
     * SELECT集合：（TODO 注意这是针对产生式而言）
     * 1、选择一个产生式，只有当下一个输入符号a在FIRST(α)中时才选择产生式A→α。
     * 2、如果α=ε或者α=>ε时，如果当前输入符号在FOLLOW(A)中，或者已经到达输入的末尾符号 $，且 $ 结束符在FOLLOW(A)中，则任然可以选择产生式A→α（那么可以认为A已经被跳过）
     *
     * 预测分析表
     * 对于 文法G 中的每个产生式 A→α进行如下处理
     * 1、对于 FIRST(α) 中的每个终结符号a，将 A→α加入到 M[A,a]中
     * 2、如果 ε∈ FIRST(α)，那么对于 FOLLOW(A)中的每个终结符号 b，将 A→α加入到 M[A,b]中，如果 ε∈ FIRST(α)，且 $ ∈ FOLLOW(A)，那么也将 A→α加入到 M[A,$]中
     * 3、所有没有记录的 M[A,o] 的空目录都设置为 error
     *
     * TODO 必须是LL(1)文法，文法 A→α|β中，FIRST(α)| FIRST(β) 不相交，如果 ε∈ FIRST(β)，则 FIRST(α) 和 FOLLOW(A) 不相交
     *
     */
    public static Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> syntaxPredictMap(Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap, Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap){
        Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> syntaxProductSelect = new LinkedHashMap();

        for(SyntaxSymbol syntaxSymbol : syntaxFirstMap.keySet()){
            for(List<SyntaxSymbol> product : syntaxFirstMap.get(syntaxSymbol).keySet()) {
                SyntaxProduct syntaxProduct = new SyntaxProduct(syntaxSymbol, product);
                // 以下是SELECT集合：（注意这是针对产生式而言）
                // 1、对于 FIRST(α) 中的每个终结符号a，将 A→α加入到 M[A,a]中
                Set<String> productFirst = syntaxFirstMap.get(syntaxSymbol).get(product);
                for(String firstSymbol : productFirst) {
                    if(!firstSymbol.equals(LexConstants.SYNTAX_EMPTY)) {
                        setSyntaxProductSelect(syntaxProductSelect, syntaxSymbol, firstSymbol, syntaxProduct);
                    }
                }
                if(productFirst.contains(LexConstants.SYNTAX_EMPTY)) {
                    // 2、如果 ε∈ FIRST(α)，那么对于 FOLLOW(A)中的每个终结符号 b，将 A→α加入到 M[A,b]中，如果 ε∈ FIRST(α)，且 $ ∈ FOLLOW(A)，那么也将 A→α加入到 M[A,$]中
                    Set<String> syntaxFollow = getSyntaxFollow(syntaxSymbol, syntaxFollowMap);
                    for (String followSymbol : syntaxFollow) {
                        if (!followSymbol.equals(LexConstants.SYNTAX_EMPTY)) {
                            setSyntaxProductSelect(syntaxProductSelect, syntaxSymbol, followSymbol, syntaxProduct);
                        }
                    }
                }
            }
        }

        return syntaxProductSelect;
    }

    public static void setSyntaxProductSelect(Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> syntaxProductSelect,
                                              SyntaxSymbol head, String firstSymbol, SyntaxProduct product){

        if(syntaxProductSelect.get(head) == null){
            Map<String, Set<SyntaxProduct>> productSelectMap = new LinkedHashMap<>();
            syntaxProductSelect.put(head, productSelectMap);
        }
        if(syntaxProductSelect.get(head).get(firstSymbol) == null){
            Set<SyntaxProduct> selectProduct = new LinkedHashSet<>();
            syntaxProductSelect.get(head).put(firstSymbol, selectProduct);
        }

        syntaxProductSelect.get(head).get(firstSymbol).add(product);

    }

    public static Set<SyntaxSymbol> getAllNonTerminalSymbol(Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap){
        Set<SyntaxSymbol> allSyntaxSymbol = new LinkedHashSet<>();
        allSyntaxSymbol.addAll(syntaxFirstMap.keySet());
        return allSyntaxSymbol;
    }

    public static Set<String> getAllTerminalSymbol(Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap, Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap){
        Set<String> allTerminalSymbol = new LinkedHashSet<>();

        for(SyntaxSymbol symbol : syntaxFirstMap.keySet()) {
            allTerminalSymbol.addAll(SyntacticLLParser.getSyntaxFirst(symbol, syntaxFirstMap));
        }

        for(SyntaxSymbol syntaxSymbol : syntaxFirstMap.keySet()){
            allTerminalSymbol.addAll(SyntacticLLParser.getSyntaxFollow(syntaxSymbol, syntaxFollowMap));
        }

        return allTerminalSymbol;
    }

    /**
     * 给予预测分析表的语法分析 LL(1) 分析
     *
     * 1、初始时刻文法符号栈中加入结束符号 $
     * 2、根据预测分析表及当前输入符a选择合适的产生式压入栈中（原来的符号展开成产生式）
     * 3、如果当前输入符号a 与栈顶的文法符号X（终结符）匹配，则判定为识别成功，对应的终结符出栈
     *
     */
    public static void syntaxParseByLL(List<Token> lexTokens, SyntaxSymbol startSyntaxSymbol, Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> predictMap){
        MyStack<SyntaxSymbol> symbolStack = new MyStack<>();
        // 1、初始时刻文法符号栈中加入结束符号 $
        symbolStack.push(new SyntaxSymbol(LexConstants.SYNTAX_END, true));
        // 1、将开始文法符号压入栈中，从开始文法符号进行扩展
        symbolStack.push(startSyntaxSymbol);
        int index = 0;
        Token input = lexTokens.get(index);
        SyntaxSymbol syntaxSymbol = symbolStack.top();
        while(!syntaxSymbol.getSymbol().equals(LexConstants.SYNTAX_END)){
            if(syntaxSymbol.isTerminal()){
                boolean isMatch = false;
                if(!syntaxSymbol.isRegexTerminal()){
                    // 说明是直接终结符
                    if(syntaxSymbol.getSymbol().equals(input.getToken())){
                        isMatch = true;
                    }
                }else{
                    // 说明是正则表达式终结符，需要根据词法单元类型进行匹配
                    if(syntaxSymbol.getSymbol().equals(input.getType().getType())){
                        isMatch = true;
                    }
                }
                if(isMatch){
                    // 说明输入识别成功
                    System.out.println("匹配：" + syntaxSymbol.getSymbol() + ", 输入符: " + input);
                    symbolStack.pop();
                    // 指向下一个输入符
                    input = lexTokens.get(++index);
                }else{
                    throw new ParseException("当前文法符号：" + syntaxSymbol.getSymbol() + ", 输入符:" + input + " 没有匹配成功，文法符号扩展错误");
                }
            }else{
                Set<SyntaxProduct> selectProducts = movePredictSyntax(predictMap, syntaxSymbol, input);
                if(selectProducts == null){
                    throw new ParseException(" M[" + syntaxSymbol.getSymbol() + ", " + input + "] 是一个报错目录");
                }else if(selectProducts.size() > 1){
                    throw new ParseException("该文法不是LL(1)文法, 文法符号：" + syntaxSymbol.getSymbol() + ", 输入符：" + input);
                }else{
                    for(SyntaxProduct product : selectProducts){
                        // TODO 这里需不需要改造成ACTION动作
                        System.out.println("输出：" + product.toString());
                        // 现将原来的文法符号弹出栈，再将扩展的产生式压入栈中
                        symbolStack.pop();
                        if(!product.getProduct().get(0).getSymbol().equals(LexConstants.SYNTAX_EMPTY)) {
                            // 如果不是空产生式，就将扩展的产生式压入栈中
                            for (int i = (product.getProduct().size() - 1); i >= 0; i--) {
                                symbolStack.push(product.getProduct().get(i));
                            }
                        }
                    }
                }
            }
            syntaxSymbol = symbolStack.top();
        }

    }

    /**
     * 完整的 LL(1) 解析器
     * @param syntaxFile   文法文件
     * @param targetProgarmFile     目标程序文件
     * @param expressions   词法规则
     * @param isClassPath
     */
    public static void syntaxParseByLL(String syntaxFile, String targetProgarmFile, List<LexExpression.Expression> expressions, boolean isClassPath){
        // 读取文法文件
        List<String> syntaxs = ParseUtils.getFile(syntaxFile, isClassPath);
        // 解析词法文件
        List<SyntaxSymbol> syntaxSymbols = ParseUtils.parseSyntaxSymbol(syntaxs);
        // 消除左递归
        SyntacticLLParser.eliminateLeftRecursion(syntaxSymbols);
        // 提供公因式
        SyntacticLLParser.mergeCommonFactor(syntaxSymbols);
        // 生成 FIRST 集合
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Set<String>>> syntaxFirstMap = SyntacticLLParser.syntaxFirst(syntaxSymbols);
        // 生成 FOLOOW 集合
        Map<SyntaxSymbol, Map<List<SyntaxSymbol>, Map<Integer, Set<String>>>> syntaxFollowMap = SyntacticLLParser.syntaxFollow(syntaxSymbols, syntaxFirstMap);
        // 生成预测分析表
        Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> syntaxPredictMap = SyntacticLLParser.syntaxPredictMap(syntaxFirstMap, syntaxFollowMap);

        // 解析目标语言文件生成词法单元数据
        List<Token> tokens = LexicalParser.parser(ParseUtils.getFile(targetProgarmFile, isClassPath), expressions);
        // 根据预测分析表解析云烟
        SyntacticLLParser.syntaxParseByLL(tokens, syntaxSymbols.get(0), syntaxPredictMap);
    }

    /**
     * 根据预测分析表返回选择的产生式，如果是LL(1)文法则只会返回一个产生式
     *
     */
    public static Set<SyntaxProduct> movePredictSyntax(Map<SyntaxSymbol, Map<String, Set<SyntaxProduct>>> predictMap,
                                                  SyntaxSymbol syntaxSymbol, Token input){
        if(predictMap.get(syntaxSymbol) == null){
            return null;
        }

        // 判断是否是正则表达式词法单元
        if(!input.getType().isRexgexToken()) {
            // 是直接词法单元，按照字面量选择产生式
            return predictMap.get(syntaxSymbol).get(input.getToken());
        }else{
            // 是正则表达式词法单元，按照词法单元类型选择产生式
            return predictMap.get(syntaxSymbol).get(input.getType().getType());
        }

    }

}