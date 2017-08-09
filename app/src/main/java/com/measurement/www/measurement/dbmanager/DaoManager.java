package com.measurement.www.measurement.dbmanager;

import android.content.Context;

import com.bean.dao.DaoMaster;
import com.bean.dao.DaoSession;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by SS on 17-2-26.
 */
public class DaoManager {
    private static final String TAG = DaoManager.class.getSimpleName();
    private static final String DB_NAME = "mydata.db";//数据库名称
    private volatile static DaoManager manager;//多线程访问
    private static DaoMaster.DevOpenHelper helper;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    private Context mContext;
    private DaoManager(Context mContext){
        this.mContext = mContext;
    }
    public static DaoManager getInstance(Context mContext){
        DaoManager instance = null;
        if(manager == null){
            synchronized (DaoManager.class){
                if(instance == null){
                    instance = new DaoManager(mContext);
                    manager = instance;
                }
            }
        }
        return manager;
    }

    /**判断是否存在数据库，如果没有就创建数据库
     * @return
     */
    public DaoMaster getDaoMaster(){
        if(daoMaster == null){
             helper = new DaoMaster.DevOpenHelper(mContext,DB_NAME,null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**获取一个操作数据库的接口，可以对数据库进行增删改查操作
     * @return
     */
    public DaoSession getDaoSession(){
        if(daoSession == null){
            if(daoMaster == null){
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }

        return daoSession;
    }
    /**
     * 打开输出日志的操作
     */
    public void setDebug(){
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }
    /**
     * 数据库使用之后需要关闭数据库
     */
    public void closeConnection(){
        closeHelper();
        closeDaoSession();
    }
    public void closeHelper(){
        if(helper != null){
            helper.close();
            helper = null;
        }
    }
    public void closeDaoSession(){
        if(daoSession != null){
            daoSession.clear();
            daoSession = null;
        }
    }
}
