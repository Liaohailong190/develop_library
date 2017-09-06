package org.liaohailong.library.db;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;


import org.liaohailong.library.RootApplication;
import org.liaohailong.library.json.JsonInterface;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrmDao<T> implements JsonInterface {
    protected Dao daoOpe;

    public OrmDao(Class clz) {
        try {
            OrmLiteDbHelper helper = OrmLiteDbHelper.getHelper(RootApplication.getInstance().getApplicationContext());
            daoOpe = helper.getDao(clz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Dao getDao() {
        return daoOpe;
    }

    /**
     * 增加一个
     *
     * @param
     */
    public int save(T t) {
        try {
            return daoOpe.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 增加一个
     *
     * @param
     */
    public int save(List<T> t) {
        try {
            return daoOpe.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 如果有，先删除，在增加
     *
     * @param t
     * @return
     */
    public int replace(T t) {
        if (t == null) {
            return 0;
        }
        ArrayList<T> ts = new ArrayList<>(1);
        ts.add(t);
        return replace(ts);
    }

    /**
     * 如果有，先删除，再增加
     *
     * @param
     * @return
     */
    public int replace(List<T> list) {
        int result = 0;
        for (T t : list) {
            try {
                Dao.CreateOrUpdateStatus status = daoOpe.createOrUpdate(t);
                if (status != null) {
                    result += status.getNumLinesChanged();
                }
            } catch (SQLException e) {
                Log.e("dao", " replace " + t + " failed, exception " + e.getMessage());
            }
        }

        return result;
    }


    public List<T> query(PreparedQuery<T> preparedQuery) throws SQLException {
        Dao<T, Integer> dao = daoOpe;
        return dao.query(preparedQuery);
    }

    public List<T> query(String attributeName, String attributeValue) {
        try {
            QueryBuilder<T, Integer> queryBuilder = daoOpe.queryBuilder();
            queryBuilder.where().eq(attributeName, attributeValue);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            return query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> query(String[] attributeNames, String[] attributeValues) throws SQLException,
            InvalidParameterException {
        if (attributeNames.length != attributeValues.length) {
            throw new InvalidParameterException("params size is not equal");
        }

        QueryBuilder<T, Integer> queryBuilder = daoOpe.queryBuilder();
        Where<T, Integer> wheres = queryBuilder.where();
        for (int i = 0; i < attributeNames.length; i++) {
            wheres.eq(attributeNames[i], attributeValues[i]);
        }

        PreparedQuery<T> preparedQuery = queryBuilder.prepare();

        return query(preparedQuery);
    }

    public List<T> queryAll() {
        try {
            // QueryBuilder<T, Integer> queryBuilder = daoOpe.queryBuilder();
            // PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            // return query(preparedQuery);
            Dao<T, Integer> dao = daoOpe;
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public T queryById(String idName, String idValue) {
        List<T> lst = query(idName, idValue);
        if (null != lst && !lst.isEmpty()) {
            return lst.get(0);
        } else {
            return null;
        }
    }

    public int delete(PreparedDelete<T> preparedDelete) throws SQLException {
        Dao<T, Integer> dao = daoOpe;
        return dao.delete(preparedDelete);
    }

    public int delete(T t) {
        try {
            Dao<T, Integer> dao = daoOpe;
            return dao.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int delete(List<T> lst) {
        try {
            Dao<T, Integer> dao = daoOpe;
            return dao.delete(lst);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int delete(String[] attributeNames, String[] attributeValues) throws SQLException,
            InvalidParameterException {
        List<T> lst = query(attributeNames, attributeValues);
        if (null != lst && !lst.isEmpty()) {
            return delete(lst);
        }
        return 0;
    }

    public int deleteById(String idName, String idValue) throws SQLException,
            InvalidParameterException {
        T t = queryById(idName, idValue);
        if (null != t) {
            return delete(t);
        }
        return 0;
    }

    public int update(T t) {
        try {
            Dao<T, Integer> dao = daoOpe;
            return dao.update(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isTableExsits() {
        try {
            return daoOpe.isTableExists();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long countOf() throws SQLException {
        return daoOpe.countOf();
    }

    public List<T> query(Map<String, Object> map) {
        try {
            QueryBuilder<T, Integer> queryBuilder = daoOpe.queryBuilder();
            if (!map.isEmpty()) {
                Where<T, Integer> wheres = queryBuilder.where();
                Set<String> keys = map.keySet();
                ArrayList<String> keyss = new ArrayList<String>();
                keyss.addAll(keys);
                for (int i = 0; i < keyss.size(); i++) {
                    if (i == 0) {
                        wheres.eq(keyss.get(i), map.get(keyss.get(i)));
                    } else {
                        wheres.and().eq(keyss.get(i), map.get(keyss.get(i)));
                    }
                }
            }
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            return query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<T> query(Map<String, Object> map, Map<String, Object> lowMap,
                         Map<String, Object> highMap) throws SQLException {
        QueryBuilder<T, Integer> queryBuilder = daoOpe.queryBuilder();
        Where<T, Integer> wheres = queryBuilder.where();
        if (!map.isEmpty()) {
            Set<String> keys = map.keySet();
            ArrayList<String> keyss = new ArrayList<String>();
            keyss.addAll(keys);
            for (int i = 0; i < keyss.size(); i++) {
                if (i == 0) {
                    wheres.eq(keyss.get(i), map.get(keyss.get(i)));
                } else {
                    wheres.and().eq(keyss.get(i), map.get(keyss.get(i)));
                }
            }
        }
        if (!lowMap.isEmpty()) {
            Set<String> keys = lowMap.keySet();
            ArrayList<String> keyss = new ArrayList<String>();
            keyss.addAll(keys);
            for (int i = 0; i < keyss.size(); i++) {
                if (map.isEmpty()) {
                    wheres.gt(keyss.get(i), lowMap.get(keyss.get(i)));
                } else {
                    wheres.and().gt(keyss.get(i), lowMap.get(keyss.get(i)));
                }
            }
        }

        if (!highMap.isEmpty()) {
            Set<String> keys = highMap.keySet();
            ArrayList<String> keyss = new ArrayList<String>();
            keyss.addAll(keys);
            for (int i = 0; i < keyss.size(); i++) {
                wheres.and().lt(keyss.get(i), highMap.get(keyss.get(i)));
            }
        }
        PreparedQuery<T> preparedQuery = queryBuilder.prepare();
        return query(preparedQuery);
    }
}