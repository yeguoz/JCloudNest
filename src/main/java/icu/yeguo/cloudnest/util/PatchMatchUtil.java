package icu.yeguo.cloudnest.util;

import java.util.regex.Pattern;

public class PatchMatchUtil {

    public static boolean isPathMatch(String requestURI, String excludePath) {
        // 匹配所有路径
        if (excludePath.equals("/"))
            return true;
        if (excludePath.equals("/*"))
            return true;
        // 精准路径匹配
        if (requestURI.equals(excludePath))
            return true;

        String regex = excludePath
                .replace("/**", "/.*")    // 替换 /** 为 /.* ，匹配多层子路径
                .replace("/*", "/[^/]+");  // 替换 /* 为 /[^/]+ ，匹配一层子路径
        Pattern pattern = Pattern.compile("^" + regex + "$");
        return pattern.matcher(requestURI).matches();
    }

}
