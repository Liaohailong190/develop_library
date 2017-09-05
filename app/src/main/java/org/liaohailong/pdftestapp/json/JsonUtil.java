package org.liaohailong.pdftestapp.json;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Json相关的使用方法
 * Created by qumiao on 14-1-15.
 */
public class JsonUtil {

    private static final Gson GSON = new Gson();
    private static final JsonParser PARSER = new JsonParser();
    private static JsonErrorhook sJsonErrorhook; // SUPPRESS CHECKSTYLE
    public static final String PREF_SHOW_JSON_TOAST = "pref_show_json_toast";

    private JsonUtil() {

    }

    /**
     * 普通的String集合转JSON格式
     *
     * @param list
     * @return
     */
    public static String stringListToJsonString(List<String> list) {
        return GSON.toJson(list);
    }

    /**
     * Json转换成普通类对象
     *
     * @param json
     * @param objCls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> T jsonToObject(JsonElement json, @NonNull Class<T> objCls) {
        return jsonToGenericObject(json, objCls);
    }

    /**
     * Json字串转换成普通类对象
     *
     * @param jsonStr
     * @param objCls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> T jsonStringToObject(String jsonStr, @NonNull Class<T> objCls) {
        return jsonStringToGenericObject(jsonStr, objCls);
    }

    /**
     * Json转换成泛型类对象（如泛型List、Array等）
     *
     * @param json JsonElement
     * @param type 泛型对象类型，一般需要配合TypeToken，
     *             比如ArrayList&lt;String&gt;，
     *             则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return 泛型类对象（如泛型List、Array等）
     */
    public static <T> T jsonToGenericObject(JsonElement json, @NonNull Type type) {
        JsonInterfaceCheck.assetType(type);
        try {
            return GSON.fromJson(json, type);
        } catch (Exception e) {
            jsonErrorHandle(json.toString(), e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Json字串转换成泛型类对象（如泛型List、Array等）
     *
     * @param jsonStr Json字串
     * @param type    泛型对象类型，一般需要配合TypeToken，
     *                比如ArrayList&lt;String&gt;，
     *                则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return 泛型类对象（如泛型List、Array等）
     */
    public static <T> T jsonStringToGenericObject(String jsonStr, @NonNull Type type) {
        JsonInterfaceCheck.assetType(type);
        try {
            return GSON.fromJson(jsonStr, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * JsonArray字串转换成普通List对象
     *
     * @param jsonArrayStr
     * @param elementCls
     * @param <T>
     * @return
     */
    @NonNull
    public static <T extends JsonInterface> List<T> jsonStringToList(
            String jsonArrayStr, @NonNull Class<T> elementCls) {
        return jsonStringToGenericList(jsonArrayStr, elementCls);
    }

    /**
     * JsonArray字串转换成String List对象
     *
     * @param jsonArrayStr
     * @return
     */
    @NonNull
    public static List<String> jsonStringToStringList(String jsonArrayStr) {
        return jsonStringToGenericList(jsonArrayStr, String.class);
    }

    /**
     * JsonArray字串转换成泛型List对象
     *
     * @param jsonArrayStr
     * @param elementType  泛型对象类型，一般需要配合TypeToken，
     *                     比如ArrayList&lt;String&gt;，
     *                     则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @param <T>
     * @return
     */
    @NonNull
    public static <T> List<T> jsonStringToGenericList(String jsonArrayStr, @NonNull Type elementType) {
        JsonArray array = stringToJsonArray(jsonArrayStr);
        return jsonArrayToGenericList(array, elementType);
    }


    /**
     * JsonArray转换成普通List对象
     *
     * @param array
     * @param elementCls
     * @param <T>
     * @return
     */
    @NonNull
    public static <T extends JsonInterface> List<T> jsonArrayToList(
            JsonArray array, @NonNull Class<T> elementCls) {
        return jsonArrayToGenericList(array, elementCls);
    }

    /**
     * JsonArray转换成String List对象
     *
     * @param array
     * @return
     */
    @NonNull
    public static List<String> jsonArrayToStringList(JsonArray array) {
        return jsonArrayToGenericList(array, String.class);
    }

    /**
     * JsonArray字串转换成泛型List对象
     *
     * @param array
     * @param elementType 泛型对象类型，一般需要配合TypeToken，
     *                    比如ArrayList&lt;String&gt;，
     *                    则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @param <T>
     * @return
     */
    @NonNull
    public static <T> List<T> jsonArrayToGenericList(JsonArray array, @NonNull Type elementType) {
        JsonInterfaceCheck.assetType(elementType);
        List<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }

        try {
            for (JsonElement element : array) {
                //noinspection unchecked
                result.add((T) GSON.fromJson(element, elementType));
            }
        } catch (Exception e) {
            jsonErrorHandle(array.toString(), e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 普通对象转换成Json字串
     *
     * @param obj
     * @param objCls
     * @return
     */
    public static <T extends JsonInterface> String objectToJsonString(
            T obj, @NonNull Class<? extends T> objCls) {
        return genericObjectToJsonString(obj, objCls);
    }

    /**
     * 泛型对象转换成Json字串
     *
     * @param obj
     * @param type 一般需要配合TypeToken，
     *             比如ArrayList&lt;String&gt;，
     *             则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return
     */
    public static String genericObjectToJsonString(Object obj, @NonNull Type type) {
        JsonInterfaceCheck.assetType(type);
        try {
            return GSON.toJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 普通对象列表转换成Json字串
     *
     * @param objectList 对象列表
     * @param elementCls 对象Class
     * @return
     */
    @NonNull
    public static <T extends JsonInterface> String listToJsonString(
            List<T> objectList, @NonNull Class<T> elementCls) {
        return genericListToJsonString(objectList, elementCls);
    }

    /**
     * 泛型对象列表转换成Json字串
     *
     * @param objectList  泛型对象列表
     * @param elementType 泛型对象类型，一般需要配合TypeToken，
     *                    比如ArrayList&lt;String&gt;，
     *                    则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return
     */
    @NonNull
    public static <T> String genericListToJsonString(
            List<T> objectList, @NonNull Type elementType) {
        JsonInterfaceCheck.assetType(elementType);
        JsonArray array = new JsonArray();
        try {
            for (T object : objectList) {
                array.add(genericObjectToJson(object, elementType));
            }
            return jsonArrayToString(array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 普通对象列表转换成Json字串列表
     *
     * @param objectList 对象列表
     * @param elementCls 对象类型
     * @return
     */
    @NonNull
    public static <T extends JsonInterface> ArrayList<String> listToJsonStringList(
            List<T> objectList, @NonNull Class<T> elementCls) {
        return genericListToJsonStringList(objectList, elementCls);
    }

    /**
     * 泛型对象列表转换成Json字串列表
     *
     * @param objectList  对象列表
     * @param elementType 泛型对象类型，一般需要配合TypeToken，
     *                    比如ArrayList&lt;String&gt;，
     *                    则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return
     */
    @NonNull
    public static <T> ArrayList<String> genericListToJsonStringList(
            List<T> objectList, @NonNull Type elementType) {
        JsonInterfaceCheck.assetType(elementType);
        ArrayList<String> result = new ArrayList<>();
        try {
            for (T object : objectList) {
                result.add(GSON.toJson(object));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * json字串列表转换成普通对象列表
     *
     * @param jsonStringList json字串列表
     * @param itemClass      对象类型
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> List<T> jsonStringListToObjectList(
            List<String> jsonStringList, @NonNull Class<T> itemClass) {
        return jsonStringListToGenericList(jsonStringList, itemClass);
    }

    /**
     * json字串列表转换成泛型对象列表
     *
     * @param jsonStringList json字串列表
     * @param elementType    泛型对象类型，一般需要配合TypeToken，
     *                       比如ArrayList&lt;String&gt;，
     *                       则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonStringListToGenericList(
            List<String> jsonStringList, @NonNull Type elementType) {
        JsonInterfaceCheck.assetType(elementType);
        List<T> result = new ArrayList<>();
        if (jsonStringList == null) {
            return result;
        }

        String currentStr = "";
        try {
            for (String str : jsonStringList) {
                currentStr = str;
                //noinspection unchecked
                result.add((T) jsonStringToGenericObject(str, elementType));
            }
        } catch (Exception e) {
            jsonErrorHandle(currentStr, e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Json字串转换成JsonObject
     *
     * @param str
     * @return
     */
    public static JsonObject stringToJson(String str) {
        try {
            return PARSER.parse(str).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 普通object转换成JsonObject
     *
     * @param obj
     * @param objCls
     * @return
     */
    public static <T extends JsonInterface> JsonObject objectToJson(
            T obj, @NonNull Class<? extends T> objCls) {
        return genericObjectToJson(obj, objCls);
    }

    /**
     * 泛型object转换成JsonObject
     *
     * @param obj
     * @param type 泛型对象类型，一般需要配合TypeToken，
     *             比如ArrayList&lt;String&gt;，
     *             则需要传new TypeToken&lt;ArrayList&lt;String&gt;&gt; { }.getType()
     * @return
     */
    public static JsonObject genericObjectToJson(Object obj, @NonNull Type type) {
        try {
            return stringToJson(genericObjectToJsonString(obj, type));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * JsonArray字串转换成JsonArray
     *
     * @param str
     * @return
     */
    public static JsonArray stringToJsonArray(String str) {
        try {
            return PARSER.parse(str).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * JsonObject转换成字串
     *
     * @param jsonObject
     * @return
     */
    public static String jsonToString(@NonNull JsonObject jsonObject) {
        return jsonObject.toString();
    }

    /**
     * JsonArray转换成字串
     *
     * @param jsonArray
     * @return
     */
    public static String jsonArrayToString(@NonNull JsonArray jsonArray) {
        return jsonArray.toString();
    }

    /**
     * 移除JsonObject中值为null的属性
     *
     * @param object
     */
    public static void removeNullValueProperties(JsonObject object) {
        ArrayList<String> removeList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement element = entry.getValue();
            if (element == null || element.isJsonNull()) {
                removeList.add(entry.getKey());
            } else if (element.isJsonObject()) {
                removeNullValueProperties(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                int count = array.size();
                if (count == 0) {
                    removeList.add(entry.getKey());
                } else {
                    for (int i = 0; i < count; i++) {
                        JsonElement e = array.get(i);
                        if (e instanceof JsonObject) {
                            removeNullValueProperties(e.getAsJsonObject());
                        }
                    }
                }
            }
        }

        for (String property : removeList) {
            object.remove(property);
        }
    }

    /**
     * 将JsonInterface数据put到intent extra
     *
     * @param intent
     * @param key
     * @param data
     * @param <T>
     */
    public static <T extends JsonInterface> void putData(Intent intent, String key, @Nullable T data) {
        if (data == null) {
            return;
        }
        intent.putExtra(key, JsonUtil.objectToJsonString(data, data.getClass()));
    }

    /**
     * 将JsonInterface数据put到bundle
     *
     * @param bundle
     * @param key
     * @param data
     * @param <T>
     */
    public static <T extends JsonInterface> void putData(Bundle bundle, String key, @Nullable T data) {
        if (data == null) {
            return;
        }
        bundle.putString(key, JsonUtil.objectToJsonString(data, data.getClass()));
    }

    /**
     * 从intent extra获取JsonInterface数据
     *
     * @param intent
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> T getData(Intent intent, String key, Class<? extends T> cls) {
        return JsonUtil.jsonStringToObject(intent.getStringExtra(key), cls);
    }

    /**
     * 从bundle获取JsonInterface数据
     *
     * @param bundle
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> T getData(Bundle bundle, String key, Class<? extends T> cls) {
        return JsonUtil.jsonStringToObject(bundle.getString(key), cls);
    }

    /**
     * 将JsonInterface列表数据put到intent extra
     *
     * @param intent
     * @param key
     * @param list
     * @param cls
     * @param <T>
     */
    public static <T extends JsonInterface> void putList(
            Intent intent, String key, @Nullable List<T> list, Class<T> cls) {
        if (list == null) {
            return;
        }
        intent.putStringArrayListExtra(key, JsonUtil.listToJsonStringList(list, cls));
    }

    /**
     * 将JsonInterface列表数据put到bundle
     *
     * @param bundle
     * @param key
     * @param list
     * @param cls
     * @param <T>
     */
    public static <T extends JsonInterface> void putList(
            Bundle bundle, String key, @Nullable List<T> list, Class<T> cls) {
        if (list == null) {
            return;
        }
        bundle.putStringArrayList(key, JsonUtil.listToJsonStringList(list, cls));
    }

    /**
     * 从intent extra获取JsonInterface列表数据
     *
     * @param intent
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> List<T> getList(Intent intent, String key, Class<T> cls) {
        return JsonUtil.jsonStringListToObjectList(intent.getStringArrayListExtra(key), cls);
    }

    /**
     * 从bundle获取JsonInterface列表数据
     *
     * @param bundle
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public static <T extends JsonInterface> List<T> getList(Bundle bundle, String key, Class<T> cls) {
        return JsonUtil.jsonStringListToObjectList(bundle.getStringArrayList(key), cls);
    }

    /**
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getJsonFromAsset(Context mContext, String fileName) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String next;
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

    /**
     * 从asset路径下读取对应文件转JsonArray输出
     *
     * @param mContext 上下文
     * @param fileName asset文件下的json文件名称
     * @return JsonArray
     */
    public static JsonArray getJsonArrayFromAsset(Context mContext, String fileName) {
        String json = getJsonFromAsset(mContext, fileName);
        return stringToJsonArray(json);
    }

    /**
     * 判断字符串是否为json数组
     *
     * @param str 检验的字符串
     * @return true表示是，false表示否
     */
    public static boolean isJsonArray(String str) {
        boolean result = true;
        if (TextUtils.isEmpty(str)) {
            result = false;
        }
        try {
            PARSER.parse(str).getAsJsonArray();
        } catch (Exception e) {
            //日志太多，不输出日志了。
//            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * 判断字符串是否为json数组
     *
     * @param str 检验的字符串
     * @return true表示是，false表示否
     */
    public static boolean isJsonObject(String str) {
        boolean result = true;
        if (TextUtils.isEmpty(str)) {
            result = false;
        }
        try {
            PARSER.parse(str).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return result;
    }


    public static void setJsonErrorhook(JsonErrorhook jsonErrorhook) {
        JsonUtil.sJsonErrorhook = jsonErrorhook;
    }

    /**
     * 在接口返回数据时，字段类型不匹配时，Gson解析报错的信息是比较有规律的，
     * 用正则匹配找出类型不匹配的字段，可以及时发现问题
     *
     * @param jsonStr
     * @param e
     */
    public static void jsonErrorHandle(String jsonStr, Exception e) {
        if (!JsonInterfaceCheck.DEBUG) {
            return;
        }
        String a = e.getMessage();
        String regex = "java.lang.IllegalStateException: " +
                "Expected (a )*(\\w+) but was (\\w+)(?: at line (\\d+) column (\\d+))*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(a);
        JsonTypeError jsonTypeError = new JsonTypeError();
        jsonTypeError.rawString = a;

        if (matcher.find()) {
            String[] errorKeyWords = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                errorKeyWords[i] = matcher.group(i + 1);
            }

            jsonTypeError.expectedType = errorKeyWords[1];
            jsonTypeError.actualType = errorKeyWords[2];
            if (matcher.groupCount() > 4) { // SUPPRESS CHECKSTYLE
                String posString = errorKeyWords[4]; // SUPPRESS CHECKSTYLE
                try {
                    int position = Integer.parseInt(posString);
                    jsonTypeError.position = position;
                    String subError = jsonStr.substring(0, position);
                    String keyRegex = "\"(\\w+)\":";
                    Pattern keyPattern = Pattern.compile(keyRegex);
                    Matcher keyMatcher = keyPattern.matcher(subError);
                    while (keyMatcher.find()) {
                        jsonTypeError.keyName = keyMatcher.group(keyMatcher.groupCount());
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        if (sJsonErrorhook != null) {
            sJsonErrorhook.track(jsonTypeError);
        }
    }

    public interface JsonErrorhook {

        void track(JsonTypeError jsonTypeError);
    }


    public static class JsonTypeError {
        public int position;
        public String expectedType;
        public String actualType;
        public String keyName;
        public String rawString = "";
    }
}
