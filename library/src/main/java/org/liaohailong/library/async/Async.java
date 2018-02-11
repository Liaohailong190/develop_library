package org.liaohailong.library.async;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 异步处理：猫捉老鼠
 * Created by LHL on 2017/9/7.
 */

public class Async<Params, Result> {
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(Schedulers.HUMAN_ATTENTION_NUMBER);
    private static final Handler HANDLER = new AsyncHandler();
    private static final Set<Future> TASK = new HashSet<>();
    private Mouse<Params, Result> mouse;
    private Cat<Result> cat;

    public static <T, V> Async<T, V> create(@NonNull Mouse<T, V> mouse) {
        Async<T, V> async = new Async<>();
        async.watch(mouse);
        return async;
    }

    private Async() {

    }

    public Async<Params, Result> subscribe(@NonNull Cat<Result> cat) {
        this.cat = cat;
        return this;
    }

    private void watch(Mouse<Params, Result> mouse) {
        this.mouse = mouse;
    }

    /**
     * @param thread {@link Schedulers}
     */
    public Async<Params, Result> catOn(int thread) {
        if (cat != null) {
            cat.setThread(thread);
        }
        return this;
    }

    /**
     * @param thread {@link Schedulers}
     */
    public Async<Params, Result> mouseOn(int thread) {
        if (mouse != null) {
            mouse.setThread(thread);
        }
        return this;
    }

    public void execute(Params params) {
        mouse.feed(params);
        Cage<Params, Result> cage = new Cage<>(mouse, cat, null);
        Result result = mouseTime(cage);
        cage.setResult(result);
        catTime(cage);
    }

    private Result mouseTime(Cage<Params, Result> cage) {
        int mouseThread = mouse.getThread();
        Result result = null;
        switch (mouseThread) {
            case Schedulers.UI_THREAD://主线程
                result = mouse.run();
                break;
            case Schedulers.IO_THREAD://子线程
                PetRunnable<Params, Result> petRunnable = new PetRunnable<>(cage, HANDLER, PetRunnable.MOUSE_TIME);
                submitTask(petRunnable);
                break;
        }
        return result;
    }

    private void catTime(Cage<Params, Result> cage) {
        Result result = cage.getResult();
        if (result == null) {
            return;
        }
        int catThread = cat.getThread();
        switch (catThread) {
            case Schedulers.UI_THREAD://主线程
                cat.chase(result);
                break;
            case Schedulers.IO_THREAD://子线程
                PetRunnable<Params, Result> petRunnable = new PetRunnable<>(cage, HANDLER, PetRunnable.CAT_TIME);
                submitTask(petRunnable);
                break;
        }
    }

    private void submitTask(Runnable runnable) {
        if (EXECUTOR == null || EXECUTOR.isShutdown()) {
            EXECUTOR = Executors.newFixedThreadPool(Schedulers.HUMAN_ATTENTION_NUMBER);
        }
        Future submit = EXECUTOR.submit(runnable);
        TASK.add(submit);
    }

    public static void clearTask() {
        //清空当前运行的任务
        for (Future future : TASK) {
            if (future != null) {
                future.cancel(true);
            }
        }
        TASK.clear();
        //清空线程池，预备下次快速接受新的任务
        if (EXECUTOR != null) {
            if (!EXECUTOR.isShutdown()) {
                EXECUTOR.shutdownNow();
            }
            EXECUTOR = null;
        }
    }

    private static class PetRunnable<Params, Result> implements Runnable {
        private static final int MOUSE_TIME = 0;
        private static final int CAT_TIME = 1;
        private WeakReference<Cage<Params, Result>> cageWeakReference;
        private WeakReference<Handler> handlerWeakReference;
        private int mode = MOUSE_TIME;

        private PetRunnable(Cage<Params, Result> cage, Handler handler, int mode) {
            this.cageWeakReference = new WeakReference<>(cage);
            this.handlerWeakReference = new WeakReference<>(handler);
            this.mode = mode;
        }

        @Override
        public void run() {
            Handler handler = handlerWeakReference.get();
            Cage<Params, Result> cage = cageWeakReference.get();
            if (handler == null || cage == null) {
                return;
            }
            Cat<Result> cat;
            switch (mode) {
                case MOUSE_TIME:
                    Mouse<Params, Result> mouse = cage.getMouse();
                    if (mouse == null) {
                        return;
                    }
                    Result result = null;
                    try {
                        result = mouse.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    cage.setResult(result);
                    //判断观察者运行线程
                    cat = cage.getCat();
                    if (cat == null) {
                        return;
                    }
                    if (cat.getThread() == Schedulers.IO_THREAD) {
                        //观察者也在子线程运行，直接操作
                        cat.chase(result);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = AsyncHandler.MOUSE;
                        message.obj = cage;
                        message.sendToTarget();
                    }

                    break;
                case CAT_TIME:
                    cat = cage.getCat();
                    if (cat == null) {
                        return;
                    }
                    Result rawResult = cage.getResult();
                    if (rawResult == null) {
                        return;
                    }
                    try {
                        cat.chase(rawResult);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }
    }

    private static class Cage<Params, Result> {
        private WeakReference<Mouse<Params, Result>> mouse;
        private WeakReference<Cat<Result>> cat;
        private WeakReference<Result> result;

        private Cage(Mouse<Params, Result> mouse, Cat<Result> cat, Result result) {
            this.mouse = new WeakReference<>(mouse);
            this.cat = new WeakReference<>(cat);
            this.result = new WeakReference<>(result);
        }

        private void setResult(Result result) {
            if (this.result != null) {
                this.result.clear();
            }
            this.result = new WeakReference<>(result);
        }

        private Mouse<Params, Result> getMouse() {
            return mouse == null || mouse.get() == null ? null : mouse.get();
        }

        private Cat<Result> getCat() {
            return cat == null || cat.get() == null ? null : cat.get();
        }

        public Result getResult() {
            return result == null || result.get() == null ? null : result.get();
        }
    }

    private static class AsyncHandler extends Handler {
        private static final int MOUSE = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MOUSE://被观察者完毕
                    Cage cage = (Cage) msg.obj;
                    Cat cat = cage.getCat();
                    Object result = cage.getResult();
                    if (cat != null && result != null) {
                        cat.chase(result);
                    }
                    break;
            }
        }
    }
}
