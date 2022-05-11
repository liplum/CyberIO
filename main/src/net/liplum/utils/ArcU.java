package net.liplum.utils;

import arc.func.*;
import arc.struct.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ArcU {
    private static final Seq EmptySeq = new Seq() {

        @Override
        public Seq add(Seq array) {
            return this;
        }

        @Override
        public boolean addUnique(Object value) {
            return false;
        }

        @Override
        public Seq add(Object value) {
            return this;
        }

        @Override
        public Seq add(Object value1, Object value2) {
            return this;
        }

        @Override
        public Seq add(Object value1, Object value2, Object value3) {
            return this;
        }

        @Override
        public Seq add(Object value1, Object value2, Object value3, Object value4) {
            return this;
        }

        @Override
        public Seq add(Object[] array) {
            return this;
        }

        @Override
        public Seq addAll(Seq array) {
            return this;
        }

        @Override
        public Seq addAll(Seq array, int start, int count) {
            return this;
        }

        @Override
        public Seq addAll(Object[] array) {
            return this;
        }

        @Override
        public Seq addAll(Object[] array, int start, int count) {
            return this;
        }

        @Override
        public Seq addAll(Iterable items) {
            return this;
        }

        @Override
        public boolean remove(Object value) {
            return true;
        }

        @Override
        public boolean remove(Boolf value) {
            return true;
        }

        @Override
        public boolean remove(Object value, boolean identity) {
            return true;
        }

        @Override
        public Object remove(int index) {
            return null;
        }

        @Override
        public void removeRange(int start, int end) {
        }

        @Override
        public Seq removeAll(Boolf pred) {
            return this;
        }

        @Override
        public boolean removeAll(Seq array) {
            return true;
        }

        @Override
        public boolean removeAll(Seq array, boolean identity) {
            return true;
        }

        @Override
        public Seq clear() {
            return this;
        }

        @Override
        public boolean contains(Boolf predicate) {
            return false;
        }

        @Override
        public int indexOf(Object value) {
            return -1;
        }

        @Override
        public int indexOf(Object value, boolean identity) {
            return -1;
        }

        @Override
        public int indexOf(Boolf value) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object value, boolean identity) {
            return -1;
        }

        @Override
        public Seq copy() {
            return this;
        }

        @Override
        public Object find(Boolf predicate) {
            return null;
        }

        @Override
        public void each(Boolf pred, Cons consumer) {
        }

        @Override
        public void each(Cons consumer) {
        }

        @Override
        public boolean replace(Object from, Object to) {
            return false;
        }

        @Override
        public Object[] toArray() {
            return JavaU.emptyArray(Object.class);
        }

        @Override
        public Object[] toArray(Class type) {
            return JavaU.emptyArray(Object.class);
        }
    };
    private static final OrderedSet EmptyOrderedSet = new OrderedSet() {
        @Override
        public boolean add(Object key) {
            return true;
        }

        @Override
        public boolean add(Object key, int index) {
            return true;
        }

        @Override
        public void addAll(Seq array) {
        }

        @Override
        public void addAll(Seq array, int offset, int length) {
        }

        @Override
        public void addAll(Object[] array) {
        }

        @Override
        public void addAll(Object[] array, int offset, int length) {
        }

        @Override
        public void addAll(ObjectSet set) {
        }

        @Override
        public boolean remove(Object key) {
            return true;
        }

        @Override
        public Object removeIndex(int index) {
            return true;
        }

        @Override
        public void clear(int maximumCapacity) {

        }

        @Override
        public void clear() {
        }

        @Override
        public Object first() {
            return null;
        }

        @Override
        public Seq orderedItems() {
            return EmptySeq;
        }

        @Override
        public Seq toSeq() {
            return EmptySeq;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object key) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public ObjectSet select(Boolf predicate) {
            return this;
        }

        @Override
        public void each(Cons cons) {

        }
    };
    private static final OrderedMap EmptyMap = new OrderedMap() {
        @Override
        public Object put(Object key, Object value) {
            return value;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public Object removeIndex(int index) {
            return null;
        }

        @Override
        public void clear(int maximumCapacity) {

        }

        @Override
        public void clear() {
        }

        @Override
        public ObjectMap copy() {
            return this;
        }

        @Override
        public void putAll(ObjectMap map) {
        }

        @Override
        public void putAll(Object... values) {
        }

        @Override
        public Object get(Object key, Prov supplier) {
            return null;
        }

        @Override
        public Object getNull(Object key) {
            return null;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Object get(Object key, Object defaultValue) {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsValue(Object value, boolean identity) {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public Object findKey(Object value, boolean identity) {
            return null;
        }

        @Override
        public void each(Cons2 cons) {

        }

    };

    public static <T> Seq<T> emptySeq() {
        return EmptySeq;
    }

    public static <T> OrderedSet<T> emptySet() {
        return EmptyOrderedSet;
    }

    public static <TK, TV> OrderedMap<TK, TV> emptyMap() {
        return EmptyMap;
    }

}
