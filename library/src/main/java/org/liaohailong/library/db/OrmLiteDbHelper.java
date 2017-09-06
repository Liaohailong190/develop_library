package org.liaohailong.library.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.liaohailong.library.RootApplication;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class OrmLiteDbHelper extends OrmLiteSqliteOpenHelper {
    private final Class[] TABLE_CLASSES = RootApplication.TABLE_CLASSES();

    private static final String TABLE_NAME = RootApplication.DB_NAME();
    private Map<String, Dao> daos = new HashMap<>();

    private OrmLiteDbHelper(Context context) {
        super(context, TABLE_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connsectionSource) {
        try {
            for (int i = 0; i < TABLE_CLASSES.length; i++) {
                Log.i("ormlite", "create table " + TABLE_CLASSES[i].getName());
                TableUtils.createTable(connectionSource, TABLE_CLASSES[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            for (int i = 0; i < TABLE_CLASSES.length; i++) {
                TableUtils.dropTable(connectionSource, TABLE_CLASSES[i], true);
            }

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static OrmLiteDbHelper instance;

    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized OrmLiteDbHelper getHelper(Context context) {
        if (instance == null) {
            synchronized (OrmLiteDbHelper.class) {
                if (instance == null)
                    instance = new OrmLiteDbHelper(context);
            }
        }

        return instance;
    }

    /**
     * 获得userDao
     *
     * @return
     * @throws SQLException
     */
    public synchronized Dao getUserDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();

        for (String key : daos.keySet()) {
            Dao dao = daos.get(key);
            dao = null;
        }

        daos.clear();
    }

}
