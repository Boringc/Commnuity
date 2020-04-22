package com.boring.community.until;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private  TrieNote rootNode = new TrieNote();

    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while((keyword = reader.readLine())!= null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNote tempNode = rootNode;
        for (int i=0 ; i<keyword.length() ; i++){
            char c = keyword.charAt(i);
            TrieNote subNode = tempNode.getSubNote(c);

            if(subNode == null){
                //初始化子节点
                subNode = new TrieNote();
                tempNode.addSubNote(c,subNode);
            }

            //指向子节点,进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNote tempNote = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果,让指针2向下走一步
                if (tempNote == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            //检查下级节点
            tempNote = tempNote.getSubNote(c);
            if (tempNote == null){
                //以begin为开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++ begin;
                //重新指向根节点
                tempNote = rootNode;
            }else if (tempNote.isKeywordEnd()){
                //发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNote = rootNode;
            }else {
                //检查下一个字符
                position++;
            }

        }

        //将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNote{

        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNote> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNote(Character c, TrieNote node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNote getSubNote(Character c) {
            return subNodes.get(c);
        }

    }
}
