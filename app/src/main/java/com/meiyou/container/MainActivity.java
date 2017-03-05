package com.meiyou.container;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.meiyou.framework.summer.ProtocolInterpreter;
import com.meiyou.moduleprotocl.R;
import com.meiyou.test.testmodule.TestEvent;
import com.meiyou.test.testmodule2.AccountDO;
import com.meiyou.test.testmodule2.ModuleStub;

/**
 * container  module
 * Created by hxd on 16/3/24.
 */
public class MainActivity extends Activity {
    TestEvent testEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        testEvent = new TestEvent(MainActivity.this);
        final TextView textView = (TextView) findViewById(R.id.text_test_shadow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProtocolInterpreter.getDefault().
                        create(ModuleStub.class)
                        .testMethod("oh this from mainActivity!",
                                getApplicationContext(), textView);
            }
        });

        ((TextView) findViewById(R.id.test_test_event_bus)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProtocolInterpreter.getDefault().register(testEvent);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ProtocolInterpreter.getDefault().post(new AccountDO("bg", 2222));
                            }
                        });
                        thread.start();
                        ProtocolInterpreter.getDefault().post(new AccountDO("main", 2333));
                    }
                });

    }

}
