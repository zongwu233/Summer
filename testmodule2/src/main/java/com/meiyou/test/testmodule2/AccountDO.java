package com.meiyou.test.testmodule2;

import com.meiyou.framework.summer.Event;

/**
 * Created by hxd on 16/6/20.
 */
@Event("Account")
public class AccountDO {
    String nick;
    long userId;

    public AccountDO(String nick, long userId) {
        this.nick = nick;
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
