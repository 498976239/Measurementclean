package com.measurement.www.measurement.dbmanager;

import android.content.Context;

import com.bean.dao.BeanDao;
import com.bean.entity.Bean;

import java.util.Date;
import java.util.List;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;

/**完成对某一张表的具体操作，其实操作的是对象
 * Created by SS on 17-2-26.
 */
public class CommonUtils {
    private DaoManager manager;
    private Context mContext;
    public CommonUtils(Context mContext){
        this.mContext = mContext;
        manager = DaoManager.getInstance(mContext);
    }

    /**完成对数据库的插入
     * @param bean
     * @return
     */
    public boolean insertBean(Bean bean){
        boolean flag = false;
        flag = manager.getDaoSession().insert(bean) != -1?true:false;
        return flag;
    }

    /**完成对数据库的批量插入，这里使用try-catch是因为批量插入方法没有返回值
     * 所以在try的末尾将flag = true，来告知插入成功
     * @param list
     * @return
     */
    public boolean insertMultBean(final List<Bean> list){
        boolean flag = false;
        try{
            manager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for(Bean bean : list){
                        manager.getDaoSession().insertOrReplace(bean);
                    }
                }
            });
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**删除某一个对象
     * @param bean
     * @return
     */
    public boolean deleteBean(Bean bean){
        boolean flag = false;
        try{
            manager.getDaoSession().delete(bean);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**返回所有的数据
     * @return
     */
    public List<Bean> queryListAll(){
        return manager.getDaoSession().loadAll(Bean.class);
    }

    /**根据所给的key来查询数据
     * @param key
     * @return
     */
    public Bean queryOneBean(String key){
        return manager.getDaoSession().load(Bean.class,key);
    }

    /**按时间和str一起查询
     * @param str
     * @param
     * @param
     */
    public  List<Bean> queryCondition(String str,Date d1,Date d2){
        QueryBuilder<Bean> beanQueryBuilder = manager.getDaoSession().queryBuilder(Bean.class);
        List<Bean> list = beanQueryBuilder.where(BeanDao.Properties.Name.eq(str),BeanDao.Properties.Now.between(d1,d2)).list();
        return list;
    }
    public List<Bean> queryCondition(Date d1,Date d2){
        QueryBuilder<Bean> beanQueryBuilder = manager.getDaoSession().queryBuilder(Bean.class);
        List<Bean> list = beanQueryBuilder.where(BeanDao.Properties.Now.between(d1, d2)).list();
        return list;
    }
    public void closeDatebase(){
        manager.closeConnection();
    }
}
