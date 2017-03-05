package com.meiyou.test.testmodule;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.meiyou.framework.summer.Protocol;

/**
 * Created by hxd on 16/6/20.
 */
@Protocol("ModuleBarStub")
public class TestForServer {

    public void testMethod(String msg, Context context,
                           TextView textView) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}

