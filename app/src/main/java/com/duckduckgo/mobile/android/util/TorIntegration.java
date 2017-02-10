package com.duckduckgo.mobile.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebView;
import com.duckduckgo.mobile.android.network.DDGNetworkConstants;
import info.guardianproject.onionkit.ui.OrbotHelper;
import info.guardianproject.onionkit.web.WebkitProxy;

/**
 * This class implements methods for Tor integration, such as setting and resetting proxy.
 */
public class TorIntegration {

    public static final int JELLY_BEAN_MR2 = 18;
    private final Activity context;
    private final OrbotHelper orbotHelper;
    private Dialog dialogOrbotInstall = null;
    private Dialog dialogOrbotStart = null;


    public TorIntegration(Activity context){
        this.context = context;
        orbotHelper = new OrbotHelper(this.context);
    }

    public boolean prepareTorSettings(){
        return prepareTorSettings(isTorSettingEnabled());
    }

    public boolean prepareTorSettings(boolean enableTor){
        if(!isTorSupported()){
            return false;
        }
        //DDGNetworkConstants.initializeMainClient(context.getApplication(), enableTor);
        if(enableTor){
            DDGNetworkConstants.initializeMainClient(context.getApplication(), enableTor);
            enableOrbotProxy();
            requestOrbotInstallAndStart();
        }
        else{
            resetProxy();
        }
        return true;
    }

    private void resetProxy() {
        try {
            WebkitProxy.resetProxy("com.duckduckgo.mobile.android.DDGApplication", DDGNetworkConstants.getWebView().getContext().getApplicationContext());
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void resetProxy(WebView webView) {
        try {
            WebkitProxy.resetProxy("com.duckduckgo.mobile.android.DDGApplication", webView.getContext().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableOrbotProxy() {
        try {
            WebkitProxy.setProxy("com.duckduckgo.mobile.android.DDGApplication", DDGNetworkConstants.getWebView().getContext().getApplicationContext(), DDGNetworkConstants.getWebView(), DDGNetworkConstants.PROXY_HOST, DDGNetworkConstants.PROXY_HTTP_PORT);
        } catch (Exception e) {
            // what should we do here? Discuss!
            e.printStackTrace();
        }
    }

    public void enableOrbotProxy(WebView webView) {
        try {
            WebkitProxy.setProxy("com.duckduckgo.mobile.android.DDGApplication", webView.getContext().getApplicationContext(), webView, DDGNetworkConstants.PROXY_HOST, DDGNetworkConstants.PROXY_HTTP_PORT);
        } catch (Exception e) {
            // what should we do here? Discuss!
            e.printStackTrace();
        }
    }

    private void requestOrbotInstallAndStart() {
        if (!orbotHelper.isOrbotInstalled()){
            promptToInstall();
        }
        else if (!orbotHelper.isOrbotRunning()){
            requestOrbotStart();
        }
    }

    /**
     * if showing this will dismiss and release all the dialogs generated by TorIntegration
     */
    public void dismissDialogs(){
        dismissOrbotStartDialog();
        dismissOrbotPromptDialog();
    }
    /**
     * This method will dismiss prompt dialog if visible.
     */
    public void dismissOrbotPromptDialog(){
        if(dialogOrbotInstall != null && dialogOrbotInstall.isShowing()){
            dialogOrbotInstall.dismiss();
            dialogOrbotInstall = null;
        }
    }

    /**
     * This method is same as OrbotHelper.promptToInstall except dismisses the previous dialogs and stores the reference of new one.
     */
    public void promptToInstall()
    {
        String uriMarket = context.getString(info.guardianproject.onionkit.R.string.market_orbot);
        // show dialog - install from market, f-droid or direct APK
        dialogOrbotInstall = showDownloadDialog(context.getString(info.guardianproject.onionkit.R.string.install_orbot_),
                context.getString(info.guardianproject.onionkit.R.string.you_must_have_orbot),
                context.getString(info.guardianproject.onionkit.R.string.yes), context.getString(info.guardianproject.onionkit.R.string.no), uriMarket);
    }

    /**
     * This method is taken from OrbotHelper it uses same resource strings but returns the dialog instead.
     * @return AlertDialog instance which can be used later to dismiss when activity is destroying
     */
    private AlertDialog showDownloadDialog(CharSequence stringTitle, CharSequence stringMessage, CharSequence stringButtonYes,
                                           CharSequence stringButtonNo, final String uriString) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(context);
        downloadDialog.setTitle(stringTitle);
        downloadDialog.setMessage(stringMessage);
        downloadDialog.setPositiveButton(stringButtonYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(stringButtonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }
    /**
     * This method will dismiss prompt dialog if visible.
     */
    public void dismissOrbotStartDialog(){
        if(dialogOrbotStart != null && dialogOrbotStart.isShowing()){
            dialogOrbotStart.dismiss();
            dialogOrbotStart = null;
        }
    }
    public void requestOrbotStart()
    {
        dismissOrbotStartDialog();
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(context);
        downloadDialog.setTitle(info.guardianproject.onionkit.R.string.start_orbot_);
        downloadDialog
                .setMessage(info.guardianproject.onionkit.R.string.orbot_doesn_t_appear_to_be_running_would_you_like_to_start_it_up_and_connect_to_tor_);
        downloadDialog.setPositiveButton(info.guardianproject.onionkit.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.startActivityForResult(orbotHelper.getOrbotStartIntent(), 1);
            }
        });
        downloadDialog.setNegativeButton(info.guardianproject.onionkit.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialogOrbotStart = downloadDialog.show();
    }
    public boolean isTorSettingEnabled() {
        return PreferencesManager.getEnableTor();
    }

    public boolean isOrbotRunningAccordingToSettings() {
        return !isTorSettingEnabled() || isTorEnabledAndOrbotRunning();
    }

    private boolean isTorEnabledAndOrbotRunning(){
        return isTorSettingEnabled() &&
                orbotHelper.isOrbotInstalled() &&
                orbotHelper.isOrbotRunning();
    }

    public boolean isTorSupported() {
        return true; //Build.VERSION.SDK_INT <= JELLY_BEAN_MR2;
    }
}