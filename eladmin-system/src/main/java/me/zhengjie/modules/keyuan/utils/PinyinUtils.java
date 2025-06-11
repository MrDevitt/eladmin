package me.zhengjie.modules.keyuan.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.regex.Pattern;

public class PinyinUtils {

    /**
     * 获取汉字的拼音首字母（大写）
     *
     * @param chinese 汉字字符串
     * @return 首字母字符串（如 "陈国强" -> "CGQ"）
     */
    public static String getFirstLetters(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            // 判断是否为汉字
            if (isChinese(c)) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    // 取第一个拼音的首字母
                    String pinyin = pinyinArray[0];
                    result.append(pinyin.charAt(0));
                }
            }  // 非汉字字符直接忽略
        }
        return result.toString().toUpperCase();
    }

    /**
     * 判断字符是否为汉字
     *
     * @param c 字符
     * @return 是否为汉字
     */
    private static boolean isChinese(char c) {
        return Pattern.matches("[\\u4E00-\\u9FA5]", String.valueOf(c));
    }
}