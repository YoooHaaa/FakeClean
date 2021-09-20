package com.monitor;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import android.os.FileObserver;
import android.util.Log;

/*
FileObserver.ACCESS，即文件被访问
FileObserver.MODIFY，文件被修改
FileObserver.ATTRIB，文件属性被修改，
FileObserver.CLOSE_WRITE，可写文件被 close
FileObserver.CLOSE_NOWRITE，不可写文件被 close
FileObserver.OPEN，文件被 open
FileObserver.MOVED_FROM，文件被移走,
FileObserver.MOVED_TO，文件被移来，
FileObserver.CREATE，创建新文件
FileObserver.DELETE，文件被删除，
FileObserver.DELETE_SELF，自删除，即一个可执行文件在执行时删除自己
FileObserver.MOVE_SELF，自移动，即一个可执行文件在执行时移动自己
FileObserver.CLOSE，文件被关闭，等同于(IN_CLOSE_WRITE | IN_CLOSE_NOWRITE)
FileObserver.ALL_EVENTS，包括上面的所有事件
*/


public class RecursiveFileObserver extends FileObserver {
    public static int CHANGES_ONLY = CREATE | DELETE | CLOSE_WRITE | MOVE_SELF | MOVED_FROM | MOVED_TO;
    List<SingleFileObserver> mObservers;
    String mPath;
    public String MONITOR = "yooha-monitor";
    int mMask;

    public RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    @Override
    public void startWatching() {
        if (mObservers != null) return;

        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files) continue;
            for (File f : files)
            {
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (SingleFileObserver sfo : mObservers) {
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }


    @Override
    public void onEvent(int event, String path) {
        int e = event & FileObserver.ALL_EVENTS;
        switch (event)
        {
            case FileObserver.ACCESS:
                break;
            case FileObserver.ATTRIB:
                break;
            case FileObserver.CLOSE_NOWRITE:
                break;
            case FileObserver.CLOSE_WRITE:
                break;
            case FileObserver.CREATE:
                break;
            case FileObserver.DELETE:
                Log.d(MONITOR, "DELETE:" + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.d(MONITOR, "DELETE_SELF:" + path);
                break;
            case FileObserver.MODIFY:
                break;
            case FileObserver.MOVE_SELF:
                break;
            case FileObserver.MOVED_FROM:
                break;
            case FileObserver.MOVED_TO:
                break;
            case FileObserver.OPEN:
                break;
            default:
                break;
        }
    }


    class SingleFileObserver extends FileObserver {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }
}


