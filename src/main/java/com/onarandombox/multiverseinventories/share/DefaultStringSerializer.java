package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.util.Logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class DefaultStringSerializer<T> implements SharableSerializer<T> {

    private Method valueOfMethod;
    private Class<T> clazz;

    DefaultStringSerializer(Class<T> clazz) {
        this.clazz = clazz;
        try {
            valueOfMethod = clazz.getMethod("valueOf", String.class);
            valueOfMethod.setAccessible(true);
            if (!valueOfMethod.getReturnType().equals(clazz) || !Modifier.isStatic(valueOfMethod.getModifiers())) {
                throw new IllegalArgumentException(clazz.getName() + " has no static valueOf(String) method!");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(clazz.getName() + " has no static valueOf(String) method!");
        }
    }

    @Override
    public T deserialize(Object obj) {
        try {
            return clazz.cast(valueOfMethod.invoke(null, obj.toString()));
        } catch (IllegalAccessException e) {
            Logging.severe(this.clazz.getName() + " has no accessible static valueOf(String) method!");
        } catch (InvocationTargetException e) {
            Logging.severe(this.clazz.getName() + ".valueOf(String) is throwing an exception:");
            e.printStackTrace();
        }
        throw new IllegalStateException(this.getClass().getName() + " was used illegally!  Contact dumptruckman!");
    }

    @Override
    public Object serialize(T t) {
        return t.toString();
    }
}
