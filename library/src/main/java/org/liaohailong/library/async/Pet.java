package org.liaohailong.library.async;

/**
 * 观察者与被观察者通用相关属性
 * Created by LHL on 2017/9/7.
 */

public class Pet {
    private int thread = Schedulers.UI_THREAD;

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }
}
