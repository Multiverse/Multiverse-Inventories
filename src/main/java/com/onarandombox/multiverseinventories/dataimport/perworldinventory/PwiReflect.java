package com.onarandombox.multiverseinventories.dataimport.perworldinventory;

import com.onarandombox.multiverseinventories.dataimport.DataImportException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PwiReflect {

    static  <C, R> R invokeMethod(C classInstance, Method method, Object...parameters) throws DataImportException {
        try {
            return (R) method.invoke(classInstance, parameters);
        } catch (Exception e) {
            throw new DataImportException("Unable to get " + method.getName()).setCauseException(e);
        }
    }

    static <C> Method getMethodFromClass(C classInstance, String methodName, Class<?>... parameterTypes) throws DataImportException {
        try {
            Method method = classInstance.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new DataImportException("Unable to get method " + methodName).setCauseException(e);
        }
    }

    static <T, C> T getFieldFromClass(C classInstance, String fieldName) throws DataImportException {
        try {
            Field field = classInstance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            T fieldInstance = (T) field.get(classInstance);
            if (fieldInstance == null) {
                throw new DataImportException(fieldName + " is null!");
            }
            return fieldInstance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataImportException("Unable to get field " + fieldName).setCauseException(e);
        }
    }
}
