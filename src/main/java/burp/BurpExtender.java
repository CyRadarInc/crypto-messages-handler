/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import com.cyradar.ui.CyInterceptor;
import java.awt.Component;

/**
 *
 * @author phinc27
 */
public class BurpExtender implements IBurpExtender, IHttpListener, ITab {

    public static IBurpExtenderCallbacks callbacks;
    private static final String EXTENSION_NAME = "CyInterceptor";
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
