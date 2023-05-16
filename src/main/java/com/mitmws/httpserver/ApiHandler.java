package com.mitmws.httpserver;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.environment.Environment;
import com.mitmws.environment.EnvironmentItemScope;
import com.mitmws.environment.EnvironmentItemType;
import com.mitmws.environment.EnvironmentVariable;
import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.util.HttpMessageUtil;
import com.mitmws.version.VERSION;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiHandler {
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private Environment environment = new Environment();
    public ApiHandler() {

    }

    public boolean checkAuth( HttpMessage httpMessage ) {
        String apiKey = httpMessage.getHeaderValue("X-API-KEY");
        if ( apiKey != null ) {
            if ( apiKey.equals(applicationConfig.getProperty("api.api-key"))) {
                return true;
            }
        }
        return false;
    }

    public HttpMessage processApiRequest(HttpMessage httpRequest ) {
        HttpMessage httpResponse = HttpMessageUtil.buildResponse(401, "Permission denied","Authentication required");
        if ( checkAuth(httpRequest)) {
            httpResponse = HttpMessageUtil.buildResponse(500, "Internal server error","API endpoint not found");
            String path = httpRequest.getPath().trim().split("\\?")[0];

            // Version
            if ( path.startsWith("/api/version")) {
                httpResponse = HttpMessageUtil.buildResponse(200, "OK", VERSION.getVersionStr());
            }

            // Environment
            if ( path.startsWith("/api/environment/")) {
                String varName = null;
                Pattern regex = Pattern.compile("^/api/environment/(.*?)$");
                Matcher m = regex.matcher(path);
                if (m.find()) {
                    varName = m.group(1);
                }
                if ( varName != null ) {
                    environment.loadEnvironment();
                    EnvironmentVariable variable = environment.getVariable(varName);
                    switch ( httpRequest.getHttpMethod() ) {
                        // Get the value of a variable
                        case "GET":
                            if ( variable != null ) {
                                httpResponse = HttpMessageUtil.buildResponse(200, "OK",variable.getStringReplacementText());
                            }
                            else {
                                httpResponse = HttpMessageUtil.buildResponse(500, "Internal server error","Environment variable does not exist");
                            }
                            break;
                        // Add/update a variable
                        case "POST":
                            String variableValue = "";
                            if ( httpRequest.getBodyBytes() != null ) {
                                variableValue = new String(httpRequest.getBodyBytes());
                            }
                            if ( variable == null ) {
                                variable = new EnvironmentVariable();
                                variable.setTemporary(true);
                                variable.setName(varName);
                                variable.setEnvironmentItemScope(EnvironmentItemScope.ALL);
                                variable.setEnvironmentItemType(EnvironmentItemType.VARIABLE_STRING_REPLACEMENT);
                                variable.setStringReplacementMatchText(varName);
                                environment.setVariable(variable);
                            }
                            variable.setStringReplacementText(variableValue);
                            httpResponse = HttpMessageUtil.buildResponse(200, "OK",null);
                            break;
                        // Delete a variable
                        case "DELETE":
                            environment.deleteVariable(varName);
                            break;
                        default:
                            httpResponse = HttpMessageUtil.buildResponse(500, "Internal server error","Method not supported");
                    }
                    environment.saveEnvironment();
                }
                else {
                    httpResponse = HttpMessageUtil.buildResponse(500, "Internal server error","Missing environment variable path parameter");
                }
            }
        }
        return httpResponse;
    }
}
