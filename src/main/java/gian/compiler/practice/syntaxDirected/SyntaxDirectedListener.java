package gian.compiler.practice.syntaxDirected;

/**
 * 产生式嵌入的语义动作
 * Created by Gian on 2019/3/21.
 */
public abstract class SyntaxDirectedListener {

    // 匹配的产生式标识（产生式字符串，和输入格式保持一致）
    protected String matchProductTag;
    protected Integer matchIndex;
    protected String matchSymbol;
    // 是否是匹配叶子节点（终结符）
    protected Boolean isLeaf;

    public SyntaxDirectedListener(){}

    public SyntaxDirectedListener(String matchProductTag, Integer matchIndex, String matchSymbol, Boolean isLeaf){
        this.matchProductTag = matchProductTag;
        this.matchIndex = matchIndex;
        this.matchSymbol = matchSymbol;
        this.isLeaf = isLeaf;
    }

    public boolean isMatch(String matchProductTag, Integer matchIndex, String matchSymbol){
        if(!this.matchProductTag.equals(matchProductTag) || !this.matchIndex.equals(matchIndex) || !this.matchSymbol.equals(matchSymbol)){
            return false;
        }else{
            return true;
        }
    }

    // 遍历节点前执行
    public abstract void enterSyntaxSymbol(SyntaxDirectedContext context);

    // 离开节点时执行
    public abstract void exitSyntaxSymbol(SyntaxDirectedContext context);

}