package cz.cube.nkd.filemanager.job;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import cz.cube.nkd.filemanager.util.Util;

public abstract class Job {

    private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new JobThreadFactory(), new JobRejectHandler());
    private static final Handler sUiHandler = new UiHandler();

    private CallableWithParams mCallable;
    private  FutureTask<Object[]> mFuture;

    
    public final void execute(final Object... params) {
        if (onPreExecute(params)) {
            mCallable = new CallableWithParams() {
                @Override
                public final Object[] call() throws Exception {
                    try {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        Object[] result = doInBackground(params);
                        return isCancelled() ? null : result;
                    } catch (Throwable e) {
                        Util.logE(Util.convertException(e));
                        mFuture.cancel(true);
                        return null;
                    }
                }
            };
            mFuture = new FutureTask<Object[]>(mCallable) {
                @Override
                protected final void done() {
                    Object[] result = new Object[0];
                    try {
                        result = get();
                    } catch (InterruptedException e) {
                        sUiHandler.obtainMessage(UiHandler.WHAT_CANCEL, new MessageObj(Job.this, null)).sendToTarget();
                        return;
                    } catch (ExecutionException e) {
                        throw new RuntimeException("An error occured while executing doInBackground()", e.getCause());
                    } catch (CancellationException e) {
                        sUiHandler.obtainMessage(UiHandler.WHAT_CANCEL, new MessageObj(Job.this, null)).sendToTarget();
                        return;
                    }
                    sUiHandler.obtainMessage(UiHandler.WHAT_RESULT, new MessageObj(Job.this, result)).sendToTarget();
                }
            };
            mCallable.params = params;
            sExecutor.execute(mFuture);
        }
    }

    public final void cancel() {
       if (!sExecutor.remove(mFuture)){
           mFuture.cancel(true);
       }
    }

    public boolean onPreExecute(Object... params) {
        return true;
    }

    protected abstract Object[] doInBackground(Object... params);

    protected final void publishProgress(Object... values) {
        sUiHandler.obtainMessage(UiHandler.WHAT_PROGRESS, new MessageObj(Job.this, values)).sendToTarget();
    }

    public void onProgressUpdate(Object... values) {
        //for override
    }

    public abstract void onPostExecute(Object... result);

    public void onCancel() {
        //for override
    }

    public final boolean isCancelled() {
        return mFuture == null ? false : mFuture.isCancelled() ;
    }

    private static abstract class CallableWithParams implements Callable<Object[]> {
        protected Object[] params;
    }

    private static final class MessageObj {
        private final Job job;
        private final Object[] values;

        private MessageObj(Job job, Object[] values) {
            this.job = job;
            this.values = values;
        }
    }

    private static final class UiHandler extends Handler {
        private static final int WHAT_RESULT = 0x1;
        private static final int WHAT_PROGRESS = 0x2;
        private static final int WHAT_CANCEL = 0x3;

        @Override
        public final void handleMessage(final Message msg) {
            MessageObj result = (MessageObj) msg.obj;
            switch (msg.what) {
                case WHAT_PROGRESS:
                    result.job.onProgressUpdate(result.values);
                case WHAT_RESULT:
                    result.job.onPostExecute(result.values);
                    break;
                case WHAT_CANCEL:
                    result.job.onCancel();
                    break;
            }
        }
    }

    private static final class JobThreadFactory implements ThreadFactory {
        private final AtomicInteger mJobNumber = new AtomicInteger(0);

        public final Thread newThread(final Runnable r) {
            return new Thread(r, "Job#" + mJobNumber.incrementAndGet());
        }
    }

    private static final class JobRejectHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Util.logW("Job rejected");
        }

    }

}
