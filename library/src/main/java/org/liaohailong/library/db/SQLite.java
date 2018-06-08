package org.liaohailong.library.db;

/**
 * Orm数据控制工厂类
 * Created by LHL on 2017/9/24.
 */

public final class SQLite {
    private SQLite() throws IllegalAccessException {
        throw new IllegalAccessException("no instance !");
    }

    public static synchronized <T> OrmDao<T> create(Class<T> tClass) {
        return new OrmDao<>(tClass);
    }
}
