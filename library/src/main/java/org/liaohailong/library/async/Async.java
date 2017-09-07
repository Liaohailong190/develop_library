package org.liaohailong.library.async;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 介于观察者和被观察者之上
 * Created by LHL on 2017/9/7.
 */

public class Async<Result> {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Schedulers.HUMAN_ATTENTION_NUMBER);
    private static final Handler HANDLER = new AsyncHandler();
    private static final Set<Future> TASK = new HashSet<>();
    private Mouse<Result> mouse;
    private Cat<Result> cat;

    public Async<Result> by(Cat<Result> cat) {
        this.cat = cat;
        return this;
    }

    public Async<Result> watch(Mouse<Result> mouse) {
        this.mouse = mouse;
        return this;
    }

    /**
     * @param thread {@link Schedulers}
     */
    public Async<Result> catOn(int thread) {
        if (cat != null) {
            cat.setThread(thread);
        }
        return this;
    }

    /**
     * @param thread {@link Schedulers}
     */
    public Async<Result> mouseOn(int thread) {
        if (mouse != null) {
            mouse.setThread(thread);
        }
        return this;
    }

    public void start() {
        Cage<Result> cage = new Cage<>(mouse, cat, null);
        Result result = mouseTime(cage);
        cage.setResult(result);
        catTime(cage);
    }

    private Result mouseTime(Cage<Result> cage) {
        int mouseThread = mouse.getThread();
        Result result = null;
        switch (mouseThread) {
            case Schedulers.UI_THREAD://主线程
                result = mouse.run();
                break;
            case Schedulers.IO_THREAD://子线程
                PetRunnable<Result> petRunnable = new PetRunnable<>(cage, HANDLER, PetRunnable.MOUSE_TIME);
                Future submit = EXECUTOR.submit(petRunnable);
                TASK.add(submit);
                break;
        }
        return result;
    }

    private void catTime(Cage<Result> cage) {
        Result result = cage.result;
        if (result == null) {
            return;
        }
        int catThread = cat.getThread();
        switch (catThread) {
            case Schedulers.UI_THREAD://主线程
                cat.chase(result);
                break;
            case Schedulers.IO_THREAD://子线程
                PetRunnable<Result> petRunnable = new PetRunnable<>(cage, HANDLER, PetRunnable.CAT_TIME);
                Future submit = EXECUTOR.submit(petRunnable);
                TASK.add(submit);
                break;
        }
    }

    public static void stopAll() {
        for (Future future : TASK) {
            if (future != null) {
                future.cancel(true);
            }
        }
        TASK.clear();
    }

    private static class PetRunnable<Result> implements Runnable {
        private static final int MOUSE_TIME = 0;
        private static final int CAT_TIME = 1;
        private WeakReference<Cage<Result>> cageWeakReference;
        private WeakReference<Handler> handlerWeakReference;
        private int mode = MOUSE_TIME;

        private PetRunnable(Cage<Result> cage, Handler handler, int mode) {
            this.cageWeakReference = new WeakReference<>(cage);
            this.handlerWeakReference = new WeakReference<>(handler);
            this.mode = mode;
        }

        @Override
        public void run() {
            Handler handler = handlerWeakReference.get();
            Cage<Result> cage = cageWeakReference.get();
            switch (mode) {
                case MOUSE_TIME:
                    Mouse<Result> mouse = cage.mouse;
                    Result result = null;
                    try {
                        result = mouse.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    cage.setResult(result);
                    //判断观察者运行线程
                    if (cage.cat.getThread() == Schedulers.IO_THREAD) {
                        //观察者也在子线程运行，直接操作
                        cage.cat.chase(result);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = AsyncHandler.MOUSE;
                        message.obj = cage;
                        message.sendToTarget();
                    }

                    break;
                case CAT_TIME:
                    Cat<Result> cat = cage.cat;
                    Result rawResult = cage.result;
                    try {
                        cat.chase(rawResult);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }
    }

    private static class Cage<Result> {
        private Mouse<Result> mouse;
        private Cat<Result> cat;
        private Result result;

        private Cage(Mouse<Result> mouse, Cat<Result> cat, Result result) {
            this.mouse = mouse;
            this.cat = cat;
            this.result = result;
        }

        private void setResult(Result result) {
            this.result = result;
        }
    }

    private static class AsyncHandler extends Handler {
        private static final int MOUSE = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MOUSE://被观察者完毕
                    Cage cage = (Cage) msg.obj;
                    Cat cat = cage.cat;
                    cat.chase(cage.result);
                    break;
            }
        }
    }
}
