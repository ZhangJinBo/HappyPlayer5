package com.zlm.hp.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;

import com.zlm.hp.entity.StorageInfo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * 获取手机可用的外置内存卡和内置的内存卡
 *
 * @author Administrator
 */
public class StorageUtil {
    private Activity mActivity;
    private StorageManager mStorageManager;
    private Method mMethodGetPaths;

    public StorageUtil(Activity activity) {
        mActivity = activity;
        if (mActivity != null) {
            mStorageManager = (StorageManager) mActivity
                    .getSystemService(Activity.STORAGE_SERVICE);
            try {
                mMethodGetPaths = mStorageManager.getClass().getMethod(
                        "getVolumePaths");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getVolumePaths() {
        String[] paths = null;
        try {
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * @Description: 获取可用储存列表
     * @param:
     * @return:
     * @throws
     * @author: zhangliangming
     * @date: 2018-08-05 11:42
     */
    @SuppressLint("NewApi")
    public static List<StorageInfo> listAvaliableStorage(Context context) {
        ArrayList<StorageInfo> storagges = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod(
                    "getVolumeList", paramClasses);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager,
                    params);
            if (invokes != null) {
                StorageInfo info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath"
                    );
                    String path = (String) getPath.invoke(obj);
                    info = new StorageInfo(path);
                    File file = new File(info.getPath());
                    if ((file.exists()) && (file.isDirectory())
                            && (file.canWrite())) {
                        Method isRemovable = obj.getClass().getMethod(
                                "isRemovable");
                        String state = null;
                        try {
                            Method getVolumeState = StorageManager.class
                                    .getMethod("getVolumeState", String.class);
                            state = (String) getVolumeState.invoke(
                                    storageManager, info.getPath());

                            info.setState(state);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (info.isMounted()) {

                            boolean isRemoveable = ((Boolean) isRemovable.invoke(
                                    obj)).booleanValue();
                            storagges.add(info);
                            info.setRemoveable(isRemoveable);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        storagges.trimToSize();

        return storagges;
    }
}