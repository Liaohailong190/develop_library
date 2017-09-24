package org.liaohailong.library.db;

/**
 * Orm数据控制工厂类
 * Created by LHL on 2017/9/24.
 */

public class OrmFactory {
    private OrmFactory() {

    }

    public static synchronized <T> OrmDao<T> getDao(Class<T> tClass) {
        return new OrmDao<>(tClass);
    }

}
