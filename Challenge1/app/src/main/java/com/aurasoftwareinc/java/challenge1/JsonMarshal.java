package com.aurasoftwareinc.java.challenge1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JsonMarshal
{
    public static JSONObject marshalJSON(Object object)
    {
        JSONObject json = new JSONObject();

        //get all declared fields for the object, including private fields
        Field[] fields = object.getClass().getDeclaredFields();

        //iterate over all fields
        for (Field field : fields) {

            //get field name
            String fieldName = field.getName();

            //ignore these two fields as they should not be marshaled but are returned by getDeclaredFields() method
            if (fieldName.equals("$change")) continue;
            if (fieldName.equals("serialVersionUID")) continue;

            //allow access to private fields
            if (Modifier.isPrivate(field.getModifiers())) field.setAccessible(true);

            //declare and initialize to null the fieldValue object, this object will store the value for the field
            Object fieldValue = null;
            try {
                //read field value
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            //check if this field implements JsonMarshalInterface
            boolean implementsInterface = JsonMarshalInterface.class.isAssignableFrom(field.getType());
            if (implementsInterface) {
                try {
                    //Get the method marshalJSON
                    Method marshalJsonMethod = field.getType().getMethod("marshalJSON");
                    //Invoke the method marshalJSON (recursively), store the returned JSONObject
                    JSONObject jsonObject = (JSONObject) marshalJsonMethod.invoke(fieldValue);
                    //Put the returned JSONObject against the key (name of field) in json
                    json.put(fieldName, jsonObject);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (field.getType().isArray()) {
                //check if the field is of type array
                try {
                    JSONArray jsonArray = null;
                    if (fieldValue != null) {
                        //instantiate a new JSONArray
                        jsonArray = new JSONArray();
                        int length = Array.getLength(fieldValue);
                        //fill it with the array values from field
                        for (int i = 0; i < length; i++) {
                            Object obj = Array.get(fieldValue, i);
                            jsonArray.put(obj);
                        }
                    }
                    //if jsonArray is null put JSONObject.NULL against the key fieldName in json, otherwise put the jsonArray
                    json.put(fieldName, jsonArray == null ? JSONObject.NULL : jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //the field is either an object type or a primitive type
                try {
                    //put the value of object or primitive type against the key fieldName in json, if this value is null put JSONObject.NULL
                    json.put(fieldName, fieldValue == null ? JSONObject.NULL : fieldValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    public static boolean unmarshalJSON(Object object, JSONObject json)
    {
        //get all declared fields for the object, including private fields
        Field[] fields = object.getClass().getDeclaredFields();

        //iterate over all fields
        for (Field field : fields) {

            //get field name
            String fieldName = field.getName();

            //ignore these two fields as they should not be marshaled but are returned by getDeclaredFields() method
            if (fieldName.equals("$change")) continue;
            if (fieldName.equals("serialVersionUID")) continue;

            //allow access to private fields
            if (Modifier.isPrivate(field.getModifiers())) field.setAccessible(true);

            //declare and initialize to null the fieldValue object, this object will store the value for the field
            Object fieldValue = null;
            try {
                //read field value
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            //check if this field implements JsonMarshalInterface
            boolean implementsInterface = JsonMarshalInterface.class.isAssignableFrom(field.getType());
            if (implementsInterface) {
                try {
                    //instantiate the field if not already instantiated
                    if (fieldValue == null) {
                        //store the reference to the instantiated object in fieldValue
                        fieldValue = field.getType().newInstance();
                        //set the field to the instantiated object
                        field.set(object, fieldValue);
                    }
                    //get the method unmarshalJSON from the instantiated field
                    Method unmarshalJsonMethod = field.getType().getMethod("unmarshalJSON", JSONObject.class);
                    //Invoke the method unmarshalJSON (recursively) and pass it the respective json object
                    JSONObject jsonObject = (JSONObject) json.get(fieldName);
                    unmarshalJsonMethod.invoke(fieldValue, jsonObject);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            } else if (field.getType().isArray()) {
                //check if the field is of type array
                try {
                    //get the array stored in json against this field's name
                    Object jsonObject = json.get(fieldName);
                    //if this array is not null - proceed, otherwise do nothing as for all reference types, the default value is null
                    if (!jsonObject.equals(JSONObject.NULL)) {
                        //cast to JSONArray
                        JSONArray jsonArray = (JSONArray) jsonObject;
                        //instantiate a new array of type - field.getType().getComponentType(), with length - jsonArray.length()
                        Object array = Array.newInstance(field.getType().getComponentType(), jsonArray.length());
                        //fill it with the values from jsonArray
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Object obj = jsonArray.get(i);
                            Array.set(array, i, obj);
                        }
                        try {
                            //set the field value to this array
                            field.set(object, array);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //the field is either an object type or a primitive type
                try {
                    //get the object stored in json against this field's name
                    Object jsonObject = json.get(fieldName);
                    if (!jsonObject.equals(JSONObject.NULL)) {
                        //if the object is not null set the field to object, otherwise do nothing as for all reference types, the default value is null
                        field.set(object, jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}