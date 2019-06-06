package com.gpufast.recorder.file;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.gpufast.utils.ELog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriter extends Thread {

    private WriteHandler mHandler;
    private FileOutputStream os;
    private FileChannel dstChannel;

    private String dstPath;

    public FileWriter(String dstPath) {
        this.dstPath = dstPath;
    }

    public void startWrite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel_up26(dstPath);
        } else {
            createChannel_low26(dstPath);
        }
        if (dstChannel != null) {
            start();
        }
    }

    /**
     * 检查文件合法性
     *
     * @param file file
     * @return true :不合法，false:合法
     */
    private boolean checkoutFile(File file) {

        if (file == null) return true;

        if (file.isDirectory()) return true;

        if (file.exists()) return false;

        File parentFile = file.getParentFile();
        if (parentFile.isFile()) return true;

        if (parentFile.exists()) return false;

        return !parentFile.mkdirs();

    }

    private void createChannel_low26(String dstPath) {
        try {
            File file = new File(dstPath);
            if (checkoutFile(file)) {
                ELog.e(this, "can't get file :" + dstPath);
                return;
            }
            os = new FileOutputStream(file);
            dstChannel = os.getChannel();
        } catch (FileNotFoundException e) {
            ELog.e(this, "create channel failed");
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel_up26(String dstPath) {
        try {
            File file = new File(dstPath);
            if (checkoutFile(file)) {
                ELog.e(this, "can't get file :" + dstPath);
                return;
            }
            dstChannel = FileChannel.open(Paths.get(dstPath), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            ELog.e(this, "create channel failed");
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new WriteHandler(this);
        ELog.d(this, "fileWriter start");
        Looper.loop();
        release();
    }

    private void release() {
        ELog.i(this, "fileWriter stop");
        if (dstChannel != null) {
            try {
                dstChannel.close();
                dstChannel = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (os != null) {
            try {
                os.close();
                os = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mHandler = null;
    }

    public void writeToFile(ByteBuffer data) {
        if (mHandler != null) {
            mHandler.sendToWriteFile(data);
        }
    }

    public void stopWrite() {
        if (mHandler != null) {
            mHandler.stopWrite();
        }
    }


    private static class WriteHandler extends Handler {

        private static final int MSG_WRITE_FILE = 0x001;
        private static final int MSG_STOP_WRITE = 0x002;


        private WeakReference<FileWriter> mWeakFileWriter;

        WriteHandler(FileWriter writer) {
            mWeakFileWriter = new WeakReference<>(writer);
        }

        void sendToWriteFile(ByteBuffer data) {
            sendMessage(obtainMessage(MSG_WRITE_FILE, data));
        }

        void stopWrite() {
            sendMessage(obtainMessage(MSG_STOP_WRITE));
        }

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MSG_WRITE_FILE) {
                FileWriter fileWriter = mWeakFileWriter.get();
                if (fileWriter == null) return;
                ByteBuffer data = (ByteBuffer) msg.obj;
                try {
                    ELog.d(FileWriter.class, "write data =" + data.limit());
                    fileWriter.dstChannel.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (msg.what == MSG_STOP_WRITE) {
                getLooper().quit();
            }
        }
    }

}
