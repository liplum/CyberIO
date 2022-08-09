package net.liplum.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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

    public static <T> T get(Class<?> clz, String name) {
        return getEntry(clz).get(null, name);
    }

    public static void set(Object obj, String name, Object value) {
        set(obj.getClass(), obj, name, value);
    }

    public static void set(Class<?> clz, Object obj, String name, Object value) {
        getEntry(clz).set(obj, name, value);
    }

    public static <T> T call(Class<?> clz, Object obj, String name, Object[] args, Class<?>[] argClz) {
        return getEntry(clz).call(obj, name, args, argClz);
    }

    public static <T> T call(Object obj, String name, Object[] args, Class<?>[] argClz) {
        return getEntry(obj.getClass()).call(obj, name, args, argClz);
    }

    /**
     * Dynamically call a method.
     *
     * @param obj  the object
     * @param name method name.
     * @param args the args and will decide the argument's types.
     * @param <T>  return type
     * @return return value. If the method returns void, null will be returned instead.
     */
    public static <T> T call(Object obj, String name, Object... args) {
        return getEntry(obj.getClass()).call(obj, name, args,
            Arrays.stream(args).map(Object::getClass).toArray(Class[]::new)
        );
    }

    public static Method getMethod(Class<?> clz, String name, Class<?>... argClz) {
        return getEntry(clz).getMethod(name, argClz);
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

    public static Entry getEntry(Class<?> clz) {
        Entry entry = AllClasses.get(clz);
        if (entry == null) {
            entry = new Entry(clz);
            AllClasses.put(clz, entry);
        }
        return entry;
    }

    @SuppressWarnings("unchecked")
    public static class Entry {
        @NotNull
        public final Class<?> clz;
        @NotNull
        public final HashMap<String, Field> fields = new HashMap<>();
        @NotNull
        public final HashMap<Object, Method> methods = new HashMap<>();
        @Nullable
        public Field[] allFields;
        @Nullable
        public Collection<Field> allFieldsIncludeParents;

        public Entry(@NotNull Class<?> clz) {
            this.clz = clz;
        }

        private Field getField(String name) {
            Field field = fields.get(name);
            if (field == null) {
                try {
                    field = clz.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (Exception e) {
                    throw new RuntimeException(name + "doesn't exist in " + clz.getName(), e);
                }
            }
            return field;
        }

        private Object getKey(String name, Object[] args) {
            return name.hashCode() ^ Arrays.hashCode(args);
        }

        public Method getMethod(String name, Class<?>[] classes) {
            Method method = methods.get(getKey(name, classes));
            if (method == null) {
                try {
                    method = clz.getDeclaredMethod(name, classes);
                    method.setAccessible(true);
                    return method;
                } catch (Exception e) {
                    throw new RuntimeException(name + "doesn't exist in " + clz.getName(), e);
                }
            }
            return method;
        }

        public <T> T call(Object obj, String name, Object[] args, Class<?>[] clz) {
            Method method = getMethod(name, clz);
            try {
                return (T) method.invoke(obj, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
