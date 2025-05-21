package org.zensnorlax.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zensnorlax
 * @version 2.0
 * @description: 混合文本分词工具（带自动去重）
 * @date 2025/3/21 9:44
 */
public class LanguageTokenizer {

    /**
     * 分词主入口（自动去重，保持顺序）
     * @param text 输入文本
     * @return 去重后的token列表（保持原始出现顺序）
     */
    public static List<String> tokenizeMixedText(String text) {
        Set<String> tokenSet = new LinkedHashSet<>();  // 使用LinkedHashSet自动去重并保持顺序
        List<String> segments = splitChineseAndNonChinese(text);

        for (String segment : segments) {
            if (isChineseSegment(segment)) {
                // 中文分词后加入集合（自动去重）
                tokenSet.addAll(segmentChinese(segment));
            } else {
                // 英文提取后加入集合（自动去重）
                tokenSet.addAll(extractEnglishWords(segment));
            }
        }
        return new ArrayList<>(tokenSet);
    }

    /**
     * 分割中/非中文片段
     */
    private static List<String> splitChineseAndNonChinese(String text) {
        List<String> segments = new ArrayList<>();
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+)|([^\\u4e00-\\u9fa5]+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String chinese = matcher.group(1);
            String nonChinese = matcher.group(2);
            if (chinese != null) {
                segments.add(chinese);
            } else if (nonChinese != null) {
                segments.add(nonChinese.trim());  // 增加trim去除两端空白
            }
        }
        return segments;
    }

    /**
     * 判断是否为纯中文段落
     */
    private static boolean isChineseSegment(String segment) {
        return !segment.isEmpty() && segment.matches("[\\u4e00-\\u9fa5]+");
    }

    /**
     * 中文分词处理
     */
    private static List<String> segmentChinese(String text) {
        List<Term> termList = HanLP.segment(text);
        return termList.stream()
                .map(term -> term.word)
                .filter(word -> !word.trim().isEmpty())  // 过滤空词
                .collect(Collectors.toList());
    }

    /**
     * 英文单词提取（统一小写格式）
     */
    private static List<String> extractEnglishWords(String text) {
        Set<String> wordSet = new LinkedHashSet<>();  // 局部去重
        Pattern pattern = Pattern.compile("[a-zA-Z0-9_+#'-]+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String rawWord = matcher.group();
            String normalized = rawWord.toLowerCase();  // 统一转为小写

            // 有效性校验：必须包含至少一个字母或数字
            if (normalized.matches(".*[a-zA-Z0-9].*")) {
                wordSet.add(normalized);  // 自动去重
            }
        }
        return new ArrayList<>(wordSet);
    }
}