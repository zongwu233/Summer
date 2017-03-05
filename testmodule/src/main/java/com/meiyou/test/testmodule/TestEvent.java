package com.meiyou.test.testmodule;

import android.content.Context;

import com.meiyou.framework.summer.OnEvent;
import com.meiyou.sdk.core.LogUtils;
import com.meiyou.sdk.core.ToastUtils;

/**
 * Created by hxd on 16/6/20.
 */
public class TestEvent {
    private static final String TAG = "TestEvent";
    Context mContext;
    public TestEvent(Context context){
        mContext=context;
    }
    @OnEvent("Account")
    public void process(MyAccount account) {
        ToastUtils.showToast(mContext,"module test get: "+account.getNick() + "," + account.getUserId());
    }

    @OnEvent(value = "Account",exec = OnEvent.Thread.BACK_GROUND)
    public void process2(MyAccount account) {
        LogUtils.d(TAG,"module test background get: "+account.getNick() + "," + account.getUserId());
    }

}
