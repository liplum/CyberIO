package net.liplum.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectU {
    public static final HashMap<Class<?>, Entry> AllClasses = new HashMap<>();

    public static  <T> T get(Object obj, String name) {
        return get(obj.getClass(), obj, name);
    }

    public static  <T> T get(Class<?> clz, Object obj, String name) {
        return getEntry(clz).get(obj, name);
    }

    public static  void set(Object obj, String name, Object value) {
        set(obj.getClass(), obj, name, value);
    }

    public static  void set(Class<?> clz, Object obj, String name, Object value) {
        getEntry(clz).set(obj, name, value);
    }

    private static  Entry getEntry(Class<?> clz) {
        return AllClasses.computeIfAbsent(clz, Entry::new);
    }

    @SuppressWarnings("unchecked")
    public static class Entry {
        @NotNull
        public final Class<?> clz;
        @NotNull
        public final HashMap<String, Field> fields = new HashMap<>();

        public Entry(@NotNull Class<?> clz) {
            this.clz = clz;
        }

        private Field getField(String name) {
            return fields.computeIfAbsent(name, n -> {
                try {
                    Field field = clz.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public <T> T get(Object obj, String name) {
            Field field = getField(name);
            try {
                return (T) field.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object obj, String name, Object value) {
            Field field = getField(name);
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
