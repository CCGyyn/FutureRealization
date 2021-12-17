package com.ccg.futurerealization.base;

import com.ccg.futurerealization.utils.LogUtils;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author：cgaopeng on 2021/10/15 14:17
 */
public interface BaseManager<T extends LitePalSupport> {

    default Boolean insert(T info) {
        boolean save = info.save();
        return save;
    }

    Integer deleteById(long id);

    Integer updateInfo(T info);

    List<T> queryAll();

    T queryById(long id);

}
