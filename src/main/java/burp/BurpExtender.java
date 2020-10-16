/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import com.cyradar.common.Utils;
import com.cyradar.core.InterceptProcessor;
import com.cyradar.models.AppConfiguration;
import com.cyradar.models.InterceptStage;
import com.cyradar.models.InterceptTargetParameter;
import com.cyradar.models.InterceptTargetUrl;
import com.cyradar.ui.CyInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author phinc27
 */
public class BurpExtender implements IBurpExtender, IHttpListener, ITab {

    public static IBurpExtenderCallbacks callbacks;
    private static final String EXTENSION_NAME = "CyInterceptor";
    private static final Logger logger = LogManager.getLogger("com.cyradar.ui.CyInterceptor");
    private CyInterceptor ui;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        BurpExtender.callbacks = callbacks;

        // register UI tab
        ui = new CyInterceptor();
        callbacks.addSuiteTab(this);

        // register http listener. Burp will invoke processHttpMessage whenever a request/response go through
        callbacks.registerHttpListener(this);
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        AppConfiguration configuration = ui.getConfiguration();
        // Check if message came from enabled tools
        if (!configuration.getToolScopes().getOrDefault(toolFlag, Boolean.FALSE)) {
            return;
        }
        IRequestInfo info = BurpExtender.callbacks.getHelpers().analyzeRequest(messageInfo);
        String msg = String.format("START - %s: %s: ", messageIsRequest ? "Request" : "Response", info.getUrl().toString());
        logger.info(msg);
        logger.debug(String.format("orignal message:\n"
                + "==================================================================================\n%s\n"
                + "==================================================================================",
                new String(messageIsRequest ? messageInfo.getRequest() : messageInfo.getResponse()))
        );
        // filter tabs and do process
        configuration.getInterceptConfigurations().stream().filter(tab -> isTargetInScopes(messageIsRequest, messageInfo, tab.getTarget())).forEachOrdered(tab -> {
            InterceptTargetParameter param = tab.getParameter();
            byte[] originalMessage = messageIsRequest ? messageInfo.getRequest() : messageInfo.getResponse();
            try {
                byte[] newMessage = processMessage(messageIsRequest, originalMessage, param, tab.getStages());
                if (messageIsRequest) {
                    messageInfo.setRequest(newMessage);
                } else {
                    messageInfo.setResponse(newMessage);
                }
            } catch (Exception e) {
                logger.error("cannot modify message", e);
            }
        });
        msg = String.format("END - %s: %s: ", messageIsRequest ? "Request" : "Response", info.getUrl().toString());
        logger.info(msg);
        logger.debug(String.format("processed message:\n"
                + "==================================================================================\n%s\n"
                + "==================================================================================",
                new String(messageIsRequest ? messageInfo.getRequest() : messageInfo.getResponse()))
        );
    }

    private boolean isTargetInScopes(boolean messageIsRequest, IHttpRequestResponse messageInfo, InterceptTargetUrl target) {
        // message is a request but tab setting is applied for responses only
        if (messageIsRequest && target.getMessageType() == InterceptTargetUrl.MESSAGE_TYPE_RESPONSE) {
            return false;
        }
        // message is a response but tab setting is applied for requests only
        if (!messageIsRequest && target.getMessageType() == InterceptTargetUrl.MESSAGE_TYPE_REQUEST) {
            return false;
        }
        // URL is not in Burp scope
        IRequestInfo analyzeRequest = BurpExtender.callbacks.getHelpers().analyzeRequest(messageInfo);
        if (target.getUrlType() == InterceptTargetUrl.URL_TYPE_INSCOPE && !BurpExtender.callbacks.isInScope(analyzeRequest.getUrl())) {
            return false;
        }
        if (target.getUrlType() == InterceptTargetUrl.URL_TYPE_CUSTOM) {
            if (target.isUsingRegex()) {
                Pattern p = Pattern.compile(target.getUrl(), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(analyzeRequest.getUrl().toString());
                return m.find();
            }
            return analyzeRequest.getUrl().toString().toLowerCase().contains(target.getUrl());
        }
        return true;
    }

    private byte[] processMessage(boolean messageIsRequest, byte[] originalMessage, InterceptTargetParameter paramOption, List<InterceptStage> stages) throws Exception {
        byte[] processedMessage = messageIsRequest ? processRequestMessage(originalMessage, paramOption, stages) : processResponseMessage(originalMessage, paramOption, stages);
        return processedMessage;
    }

    private String getParamTypeInString(int type) {
        switch (type) {
            case IParameter.PARAM_BODY:
                return "BODY";
            case IParameter.PARAM_COOKIE:
                return "COOKIE";
            case IParameter.PARAM_JSON:
                return "JSON";
            case IParameter.PARAM_MULTIPART_ATTR:
                return "MULTIPART_ATTR";
            case IParameter.PARAM_URL:
                return "URL";
            case IParameter.PARAM_XML:
                return "XML";
            case IParameter.PARAM_XML_ATTR:
                return "XML_ATTR";
            default:
                return "UNKNOWN";
        }
    }

    private byte[] processRequestMessage(byte[] originalMessage, InterceptTargetParameter paramOption, List<InterceptStage> stages) throws Exception {
        String paramName = paramOption.getParamName();
        // Quick search for parameter in the message
        IParameter originalParameter = BurpExtender.callbacks.getHelpers().getRequestParameter(originalMessage, paramName);
        if (originalParameter == null) {
            return originalMessage;
        }
        int paramType = originalParameter.getType();
        String paramTypeInString = getParamTypeInString(paramType);
        if (paramType == IParameter.PARAM_MULTIPART_ATTR || paramType == IParameter.PARAM_XML || paramType == IParameter.PARAM_XML_ATTR) {
            // currently not supports these kind of parameters
            logger.warn(String.format("parameter %s is not supported", paramTypeInString));
            return originalMessage;
        }
        if (paramType == IParameter.PARAM_JSON) {
            // json parameters are handled in a different way
            logger.info(String.format("found parameter %s in json body", paramName));
            int bodyOffset = BurpExtender.callbacks.getHelpers().analyzeRequest(originalMessage).getBodyOffset();
            int bodyOffsetEnd = originalMessage.length;
            byte[] body = Arrays.copyOfRange(originalMessage, bodyOffset, bodyOffsetEnd);
            body = processJSONBody(body, paramOption, stages);
            byte[] headerBytes = Arrays.copyOfRange(originalMessage, 0, bodyOffset);
            byte[] newReq = updateContentLength(Utils.concat(headerBytes, body), true);
            return newReq;
        }
        logger.info(String.format("found parameter %s. type: %s, value: %s", paramName, paramTypeInString, originalParameter.getValue()));
        String newValue = processValue(originalParameter.getValue(), stages, paramOption.isUrlDecodeBefore(), paramOption.isUrlEncodeAfter());
        IParameter newParam = BurpExtender.callbacks.getHelpers().buildParameter(originalParameter.getName(), newValue, originalParameter.getType());
        byte[] newReq = BurpExtender.callbacks.getHelpers().updateParameter(originalMessage, newParam);
        return newReq;
    }

    private byte[] processResponseMessage(byte[] originalMessage, InterceptTargetParameter paramOption, List<InterceptStage> stages) throws Exception {
        IResponseInfo respInfo = BurpExtender.callbacks.getHelpers().analyzeResponse(originalMessage);
        // only support replacing value if body is in json format
        if (!respInfo.getInferredMimeType().equalsIgnoreCase("JSON")) {
            logger.warn("modifying response message support json format only, got: " + respInfo.getInferredMimeType());
            return originalMessage;
        }
        int bodyOffset = BurpExtender.callbacks.getHelpers().analyzeResponse(originalMessage).getBodyOffset();
        int bodyOffsetEnd = originalMessage.length;
        byte[] body = Arrays.copyOfRange(originalMessage, bodyOffset, bodyOffsetEnd);
        body = processJSONBody(body, paramOption, stages);
        byte[] headerBytes = Arrays.copyOfRange(originalMessage, 0, bodyOffset);
        byte[] newResp = updateContentLength(Utils.concat(headerBytes, body), true);
        return newResp;
    }

    private String processValue(String value, List<InterceptStage> stages, boolean urlDecodeBefore, boolean urlEncodeAfter) throws Exception {
        Gson g = new Gson();
        String original = value;
        if (urlDecodeBefore) {
            String newValue = BurpExtender.callbacks.getHelpers().urlDecode(value);
            logger.debug(String.format("%s => %s. URL decode", value, newValue));
            value = newValue;
        }
        for (InterceptStage stage : stages) {
            String newValue = InterceptProcessor.doProcess(value, stage);
            logger.debug(String.format("%s => %s. Stage = %s", value, newValue, g.toJson(stage)));
            value = newValue;
        }
        if (urlEncodeAfter) {
            String newValue = BurpExtender.callbacks.getHelpers().urlEncode(value);
            logger.debug(String.format("%s => %s. URL encode", value, newValue));
            value = newValue;
        }
        logger.info(String.format("%s => %s", original, value));
        return value;
    }

    private byte[] processJSONBody(byte[] body, InterceptTargetParameter paramOption, List<InterceptStage> stages) throws Exception {
        Gson g = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        String strBody = new String(body);
        logger.info(String.format("original json body: \n%s", strBody));
        JsonElement jsonBody = g.fromJson(strBody, JsonElement.class);
        JsonElement transformed = updateJsonParam(jsonBody, paramOption, stages);
        String newStrBody = g.toJson(transformed);
        logger.info(String.format("processed json body: \n%s", newStrBody));
        return newStrBody.getBytes();
    }

    private JsonElement updateJsonParam(JsonElement element, InterceptTargetParameter paramOption, List<InterceptStage> stages) throws Exception {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            Set<String> keySet = jsonObject.keySet();
            for (String key : keySet) {
                JsonElement child = jsonObject.get(key);
                if (child.isJsonNull()) {
                    continue;
                }
                if (child.isJsonPrimitive()) {
                    if (key.equalsIgnoreCase(paramOption.getParamName())) {
                        String value = child.getAsString();
                        String newValue = processValue(value, stages, paramOption.isUrlDecodeBefore(), paramOption.isUrlEncodeAfter());
                        try {
                            JsonElement jsonNewEl = new Gson().fromJson(newValue, JsonElement.class);
                            if (jsonNewEl.isJsonPrimitive()) {
                                jsonObject.addProperty(key, newValue);
                            } else {
                                jsonObject.add(key, jsonNewEl);
                            }
                        } catch (JsonSyntaxException e) {
                            jsonObject.addProperty(key, newValue);
                        }
                        // TODO log something here?
                    }
                    continue;
                }
                jsonObject.add(key, updateJsonParam(child, paramOption, stages));
            }
        }
        if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonArray.set(i, updateJsonParam(jsonArray.get(i), paramOption, stages));
            }
        }
        return element;
    }

    private byte[] updateContentLength(byte[] messageBytes, boolean messageIsRequest) {
        IExtensionHelpers helpers = BurpExtender.callbacks.getHelpers();
        int contentLength = messageBytes.length
                - ((messageIsRequest)
                        ? helpers.analyzeRequest(messageBytes).getBodyOffset()
                        : helpers.analyzeResponse(messageBytes).getBodyOffset());
        String msgAsString = helpers.bytesToString(messageBytes);
        msgAsString = msgAsString.replaceFirst("Content-Length: \\d+\\r\\n", String.format("Content-Length: %d\r\n", contentLength));
        return helpers.stringToBytes(msgAsString);
    }

    @Override
    public String getTabCaption() {
        return EXTENSION_NAME;
    }

    @Override
    public Component getUiComponent() {
        return this.ui;
    }

}
