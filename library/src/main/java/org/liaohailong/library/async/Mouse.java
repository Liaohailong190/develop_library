package org.liaohailong.library.async;

/**
 * 被观察者
 * Created by LHL on 2017/9/7.
 */

public abstract class Mouse<Params, Result> extends Pet {
    private Params mParams;

    public Mouse() {
        setThread(Schedulers.IO_THREAD);
    }

    public void feed(Params mParams) {
        this.mParams = mParams;
    }

    public abstract Result run(Params params);

    public Result run() {
        return run(mParams);
    }
}
