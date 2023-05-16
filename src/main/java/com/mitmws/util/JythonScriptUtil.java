package com.mitmws.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JythonScriptUtil {

    public static boolean regexMatches( String regex, String text ) {
        Pattern p = Pattern.compile(regex,Pattern.DOTALL|Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if ( m.find() ) {
            return true;
        }
        return false;
    }
}
