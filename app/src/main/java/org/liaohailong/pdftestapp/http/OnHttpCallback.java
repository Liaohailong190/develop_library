package org.liaohailong.pdftestapp.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.liaohailong.pdftestapp.util.Utility;
import org.liaohailong.pdftestapp.json.JsonInterface;
import org.liaohailong.pdftestapp.json.JsonUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * HTTP请求返回
 * Created by LHL on 2017/9/4.
 */

public abstract class OnHttpCallback<T> {

    public void setResponse(int code, String data) {
        if (code == HttpURLConnection.HTTP_OK) {
            T rawData = getRawData(data);
            onSuccess(rawData);
        } else {
            onFailure(code, data);
        }
    }

    public T getRawData(String data) {
        try {
            Type type = Utility.getClassTypeParameter(getClass());
            if (type == String.class) {
                return (T) data;
            }

            JsonObject jsonObject = JsonUtil.stringToJson(data);
            if (jsonObject == null) {
                jsonObject = new JsonObject();
            }
            if (type == JsonObject.class) {
                return (T) jsonObject;
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                Type rawType = pType.getRawType();
                Type[] arguments = pType.getActualTypeArguments();

                // 如果Result的类型为 List<? extends JsonInterface>，则解析结果中的list字段
                if ((rawType instanceof Class) && ((Class<?>) rawType).isAssignableFrom(List.class)
                        && arguments.length == 1 && (arguments[0] instanceof Class)
                        && JsonInterface.class.isAssignableFrom((Class<?>) arguments[0])) {
                    JsonElement jsonElement = jsonObject.get(getResultListKey());
                    JsonArray jsonArray = null;
                    if (jsonElement != null && !jsonElement.isJsonNull()) {
                        jsonArray = jsonElement.getAsJsonArray();
                    }
                    //noinspection unchecked
                    return (T) JsonUtil.jsonArrayToGenericList(jsonArray, arguments[0]);
                }
            }

            return JsonUtil.jsonToGenericObject(jsonObject, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private String getResultListKey() {
        return "list";
    }

    public abstract void onSuccess(T result);

    public abstract void onFailure(int code, String info);
}
