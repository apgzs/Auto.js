package com.stardust.scriptdroid.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stardust.app.Fragment;
import com.stardust.scriptdroid.DocumentActivity;
import com.stardust.scriptdroid.R;
import com.stardust.scriptdroid.droid.Droid;
import com.stardust.scriptdroid.droid.runtime.action.ActionPerformService;
import com.stardust.view.ViewBinder;
import com.stardust.view.ViewBinding;
import com.stardust.view.accessibility.AccessibilityServiceUtils;

import static com.stardust.scriptdroid.droid.runtime.action.ActionPerformService.ACTION_CHANGE_ASSIST_MODE;

/**
 * Created by Stardust on 2017/1/30.
 */

public class SlideMenuFragment extends Fragment {


    public static void init(AppCompatActivity activity, int viewId) {
        SlideMenuFragment fragment = new SlideMenuFragment();
        activity.getSupportFragmentManager().beginTransaction().replace(viewId, fragment).commit();
    }

    private SwitchCompat mAutoOperateServiceSwitch, mAssistServiceSwitch, mAssistServiceNotificationSwitch;
    private Receiver mReceiver = new Receiver();

    @Nullable
    @Override
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_slide_menu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAutoOperateServiceSwitch == null) {
            setUpSwitchCompat();
            ViewBinder.bind(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_CHANGE_ASSIST_MODE);
            getContext().registerReceiver(mReceiver, filter);
        }
        syncSwitchState();
    }

    private void syncSwitchState() {
        mAutoOperateServiceSwitch.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAutoOperateServiceSwitch.setChecked(ActionPerformService.isEnable());
            }
        }, 450);
        mAssistServiceSwitch.setChecked(ActionPerformService.isAssistModeEnable());
        mAssistServiceNotificationSwitch.setChecked(AssistModeSwitchNotification.isEnable());
    }

    private void setUpSwitchCompat() {
        mAutoOperateServiceSwitch = $(R.id.sw_auto_operate_service);
        mAssistServiceSwitch = $(R.id.sw_assist_service);
        mAssistServiceNotificationSwitch = $(R.id.sw_assist_service_notification);
    }

    @ViewBinding.Click(R.id.syntax_and_api)
    private void startSyntaxHelpActivity() {
        startActivity(new Intent(getContext(), DocumentActivity.class));
    }

    @ViewBinding.Click(R.id.auto_operate_service)
    private void clickAutoOperateServiceSwitch() {
        mAutoOperateServiceSwitch.toggle();
    }

    @ViewBinding.Check(R.id.sw_auto_operate_service)
    private void setAutoOperateServiceEnable(boolean enable) {
        if (enable && !ActionPerformService.isEnable()) {
            AccessibilityServiceUtils.goToPermissionSetting(getContext());
        } else if (!enable && ActionPerformService.isEnable()) {
            ActionPerformService.disable();
        }
    }

    @ViewBinding.Check(R.id.sw_assist_service)
    private void setAssistServiceEnable(boolean enable) {
        ActionPerformService.setAssistModeEnable(enable);
        AssistModeSwitchNotification.notifyAssistModeChanged();
    }

    @ViewBinding.Click(R.id.assist_service)
    private void toggleAssistServiceSwitch() {
        mAssistServiceSwitch.toggle();
    }

    @ViewBinding.Check(R.id.sw_assist_service_notification)
    private void setAssistServiceNotificationEnable(boolean enable) {
        AssistModeSwitchNotification.setEnable(enable);
    }

    @ViewBinding.Click(R.id.assist_service_notification)
    private void toggleAssistServiceNotificationSwitch() {
        mAssistServiceNotificationSwitch.toggle();
    }

    @ViewBinding.Click(R.id.stop_all_running_scripts)
    private void stopAllRunningScripts() {
        int n = Droid.getInstance().stopAll();
        if (n > 0)
            Snackbar.make(getActivityContentView(), "已停止" + n + "个正在运行的脚本", Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(getActivityContentView(), "没有正在运行的脚本", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private class Receiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CHANGE_ASSIST_MODE)) {
                AssistModeSwitchNotification.notifyAssistModeChanged();
                mAssistServiceSwitch.setChecked(intent.getBooleanExtra("enable", false));
            }
        }
    }


}