package com.meiyou.test.testmodule2;

import android.content.Context;
import android.widget.TextView;

import com.meiyou.framework.summer.ProtocolShadow;

/**
 * Created by hxd on 16/3/28.
 */
@ProtocolShadow("ModuleBarStub")
public interface ModuleStub {

    public void testMethod(String msg, Context context, TextView textView);
}
