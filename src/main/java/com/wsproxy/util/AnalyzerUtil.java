package com.wsproxy.util;

import java.util.ArrayList;

public final class AnalyzerUtil {
    public static String escapeRegexChars( String str ) {
        char regexChars[] = "<([{\\^-=$!|]})?*+.>".toCharArray();
        char buff[] = str.toCharArray();
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for ( int i = 0; i < str.length(); i++ ) {
            char c = buff[i];
            for ( int j = 0; j < regexChars.length; j++ ) {
                if (regexChars[j] == c) {
                    positions.add(i);
                    break;
                }
            }
        }
        while ( positions.size() > 0 ) {
            int curPos = positions.get(0);
            str = str.substring(0,curPos) + "\\" + str.substring(curPos);
            positions.remove(0);
        }
        str = str.replaceAll("(?s).+",".+");
        return str;
    }
}
