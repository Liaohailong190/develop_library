package org.liaohailong.library.async;

/**
 * 观察者
 * Created by LHL on 2017/9/7.
 */

public abstract class Cat<Params> extends Pet {
    private Params params;

    public abstract void chase(Params params);

    public void setParams(Params params) {
        this.params = params;
    }

    public Params getParams() {
        return params;
    }
}
