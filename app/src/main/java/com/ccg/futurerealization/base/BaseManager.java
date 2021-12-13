package com.ccg.futurerealization.base;

import java.util.List;

/**
 * @author：cgaopeng on 2021/10/15 14:17
 */
public interface BaseManager<T> {

    Boolean insert(T info);

    Integer deleteById(long id);

    Integer updateInfo(T info);

    List<T> queryAll();

    T queryById(long id);

}
