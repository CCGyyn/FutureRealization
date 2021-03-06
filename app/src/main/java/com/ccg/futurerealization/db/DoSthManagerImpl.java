package com.ccg.futurerealization.db;

import com.ccg.futurerealization.bean.DoSth;

import org.litepal.LitePal;

import java.util.List;

/**
 * @author：cgaopeng on 2021/10/15 14:02
 */
public class DoSthManagerImpl implements DoSthManager {

    private static volatile DoSthManager mDoSthManagerImpl;

    private DoSthManagerImpl() {
    }

    public static DoSthManager getInstance() {
        if (mDoSthManagerImpl == null) {
            synchronized (DoSthManagerImpl.class) {
                if (mDoSthManagerImpl == null) {
                    mDoSthManagerImpl = new DoSthManagerImpl();
                }
            }
        }
        return mDoSthManagerImpl;
    }

    /**
     * LitePal 里面默认以加锁，不用自己再加锁
     * @param info DoSth信息
     * @return
     */
    @Override
    public Boolean insert(DoSth info) {
        boolean save = info.save();
        return save;
    }

    @Override
    public Integer deleteById(long id) {
        int delete = LitePal.delete(DoSth.class, id);
        return delete;
    }

    @Override
    public Integer updateInfo(DoSth info) {
        int update = info.update(info.getId());
        return update;
    }

    @Override
    public List<DoSth> queryAll() {
        List<DoSth> all = LitePal.findAll(DoSth.class);
        return all;
    }

    @Override
    public DoSth queryById(long id) {
        DoSth doSth = LitePal.find(DoSth.class, id);
        return doSth;
    }

    @Override
    public Integer deleteAll() {
        int i = LitePal.deleteAll(DoSth.class);
        return i;
    }
}
