package net.liplum.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class ReflectU {
    public static final HashMap<Class<?>, Entry> AllClasses = new HashMap<>();

    public static <T> T get(Object obj, String name) {
        return get(obj.getClass(), obj, name);
    }

    public static <T> T get(Class<?> clz, Object obj, String name) {
        return getEntry(clz).get(obj, name);
    }

    public static void set(Object obj, String name, Object value) {
        set(obj.getClass(), obj, name, value);
    }

    public static void set(Class<?> clz, Object obj, String name, Object value) {
        getEntry(clz).set(obj, name, value);
    }

    public static <TFrom, TTO extends TFrom> void copyFields(TFrom from, TTO to) {
        Class<?> fromClz = from.getClass();
        for (Field field : allFieldsIncludeParents(fromClz)) {
            try {
                set(field.getDeclaringClass(), to, field.getName(), field.get(from));
            } catch (Exception e) {
                throw new RuntimeException("from:[" + fromClz.getName() +
                        "]to:[" + fromClz.getName() + "]"
                        + field.getName(), e);
            }
        }
    }

    public static Collection<Field> allFieldsIncludeParents(Class<?> clz) {
        Entry entry = getEntry(clz);
        if (entry.allFieldsIncludeParents != null) {
            return entry.allFieldsIncludeParents;
        }
        LinkedList<Field> fields = new LinkedList<>();
        for (Class<?> curClz = clz.isAnonymousClass() ? clz.getSuperclass() : clz;
             curClz != null;
             curClz = curClz.getSuperclass()) {
            Field[] allFields = getEntry(curClz).getAllFields();
            for (Field f : allFields) {
                f.setAccessible(true);
                fields.add(f);
            }
        }
        entry.allFieldsIncludeParents = fields;
        return fields;
    }

    private static Entry getEntry(Class<?> clz) {
        return AllClasses.computeIfAbsent(clz, Entry::new);
    }

    @SuppressWarnings("unchecked")
    public static class Entry {
        @NotNull
        public final Class<?> clz;
        @NotNull
        public final HashMap<String, Field> fields = new HashMap<>();
        @Nullable
        public Field[] allFields;
        @Nullable
        public Collection<Field> allFieldsIncludeParents;

        public Entry(@NotNull Class<?> clz) {
            this.clz = clz;
        }

        private Field getField(String name) {
            return fields.computeIfAbsent(name, n -> {
                try {
                    Field field = clz.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public <T> T get(Object obj, String name) {
            Field field = getField(name);
            try {
                return (T) field.get(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object obj, String name, Object value) {
            Field field = getField(name);
            try {
                field.set(obj, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Field[] getAllFields() {
            allFields = clz.getDeclaredFields();
            return allFields;
        }
    }
}
