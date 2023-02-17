package com.wsproxy.trafficanalysis.models;

import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.tester.TargetLocator;
import com.wsproxy.tester.TestTarget;
import com.wsproxy.util.AnalyzerUtil;
import com.wsproxy.util.HashUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class AnalyzerMessageModel {

    private String id;
    private Pattern pattern;
    private String parameterizedMessage;
    private TargetLocator targetLocator = new TargetLocator();
    private HashMap<String,AnalyzerParameterModel> parameters = new HashMap<>();
    public AnalyzerMessageModel( WebsocketTrafficRecord websocketTrafficRecord ) throws NoSuchAlgorithmException {
        this.parameterizedMessage = getPatternKey(new String(websocketTrafficRecord.getFrame().getPayloadUnmasked()));
        pattern = Pattern.compile(
                AnalyzerUtil.escapeRegexChars(parameterizedMessage).replaceAll(
                        "__ZZ_PARAM_VALUE_ZZ__",
                        "[a-z0-9]+"),
                Pattern.DOTALL|Pattern.MULTILINE|Pattern.CASE_INSENSITIVE
        );
        id = HashUtils.md5sum(parameterizedMessage.getBytes(StandardCharsets.UTF_8));
        addSample(websocketTrafficRecord);
    }

    public String getPatternKey( String text ) {
        String patternKey = null;
        ArrayList<TestTarget> testTargets = targetLocator.getAllTargets(text);
        if ( testTargets != null && testTargets.size() > 0 ) {
            patternKey = text;
            for ( int i = 0; i < testTargets.size(); i++ ) {
                int start = testTargets.get(testTargets.size() - 1 - i).getStartPos();
                int end = testTargets.get(testTargets.size() - 1 - i).getEndPos();
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(patternKey.substring(0, start));
                    sb.append("__ZZ_PARAM_VALUE_ZZ__");
                    sb.append(patternKey.substring(end, patternKey.length()));
                    patternKey = sb.toString();
                }
                catch ( StringIndexOutOfBoundsException e ) {
                    System.out.println(String.format("%d-%d - %s", start, end, patternKey));
                }
            }
        }
        return patternKey;
    }

    public void addSample( WebsocketTrafficRecord websocketTrafficRecord ) {
        String payloadStr = new String(websocketTrafficRecord.getFrame().getPayloadUnmasked());
        ArrayList<TestTarget> targets = targetLocator.getAllTargets(payloadStr);

        for (TestTarget target : targets) {
            if (parameters.get(target.getTargetName()) == null) {
                parameters.put(target.getTargetName(), new AnalyzerParameterModel(target.getTargetName()));
            }

            // add the value variant
            parameters.get(target.getTargetName()).addValueVariant(
                    payloadStr.substring(target.getStartPos(), target.getEndPos())
            );
        }
    }

    public String getId() {
        return id;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getParameterizedMessage() {
        return parameterizedMessage;
    }

    public void setParameterizedMessage(String parameterizedMessage) {
        this.parameterizedMessage = parameterizedMessage;
    }

    public HashMap<String, AnalyzerParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, AnalyzerParameterModel> parameters) {
        this.parameters = parameters;
    }
}
