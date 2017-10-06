package org.liaohailong.library.async;

/**
 * 被观察者
 * Created by LHL on 2017/9/7.
 */

public abstract class Mouse<Result> extends Pet {
    public Mouse() {
        setThread(Schedulers.IO_THREAD);
    }

    public abstract Result run();
}
