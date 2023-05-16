package com.mitmws.tester;

import com.mitmws.configuration.ApplicationConfig;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetLocator {

    private ApplicationConfig applicationConfig = new ApplicationConfig();

    public TargetLocator() {

    }

    public ArrayList<TestTarget> getAllTargets(String input) {
        ArrayList<TestTarget> testTargets = new ArrayList<>();
        testTargets.addAll(getTargets(input,applicationConfig.getProperty("tests.autotarget_regex_xml"), "xml"));
        testTargets.addAll(getTargets(input,applicationConfig.getProperty("tests.autotarget_regex_json"), "json"));
        testTargets.addAll(getTargets(input,applicationConfig.getProperty("tests.autotarget_regex_kvp"),"kvp"));
        testTargets.addAll(getTargets(input,applicationConfig.getProperty("tests.autotarget_regex_urlenckvp"),"kvpenc"));
        return testTargets;
    }
    public ArrayList<TestTarget> getTargets(String input, String regexStr, String namePrefix ) {
        ArrayList<TestTarget> testTargets = new ArrayList<>();
        Pattern p = Pattern.compile(regexStr);
        Matcher m = p.matcher(input);
        while ( m.find() ) {
            if ( m.groupCount() > 1 ) {
                if ( m.group(1) != null && m.group(2) != null ) {
                    if ( m.group(2).trim().length() > 0 ) {
                        TestTarget newTarget = new TestTarget(0,m.start(2),m.end(2));
                        newTarget.setTargetName(String.format("%s_%s", namePrefix, m.group(1)));
                        testTargets.add(newTarget);
                    }
                }
            }
        }
        return  testTargets;
    }

}
