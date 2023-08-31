import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashMap<K, V> implements Iterable<HashMap.Entity<K, V>> {
    private static final int INIT_BUCKET_COUNT = 16;
    private static final double LOAD_FACTOR = 0.5;

    private Bucket<K, V>[] buckets;
    private int size;

    public HashMap() {
        this(INIT_BUCKET_COUNT);
    }

    public HashMap(int initCount) {
        buckets = new Bucket[initCount];
        size = 0;
    }

    @Override
    public Iterator<Entity<K, V>> iterator() {
        return new HashMapIterator();
    }

    class HashMapIterator implements Iterator<Entity<K, V>> {
        int currentBucketIndex = 0;
        Bucket.Node<K, V> currentNode = null;

        @Override
        public boolean hasNext() {
            if (currentNode != null && currentNode.next != null) {
                return true;
            }
            for (int i = currentBucketIndex; i < buckets.length; i++) {
                if (buckets[i] != null && buckets[i].head != null) {
                    if (i == currentBucketIndex && currentNode != null) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public Entity<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            while (currentBucketIndex < buckets.length) {
                if (buckets[currentBucketIndex] == null || buckets[currentBucketIndex].head == null) {
                    currentBucketIndex++;
                    continue;
                }

                if (currentNode == null) {
                    currentNode = buckets[currentBucketIndex].head;
                    if (currentNode != null) {
                        Entity<K, V> result = currentNode.value;
                        if (currentNode.next != null) {
                            currentNode = currentNode.next;
                        } else {
                            currentNode = null;
                            currentBucketIndex++;
                        }
                        return result;
                    }
                } else {
                    if (currentNode.next != null) {
                        currentNode = currentNode.next;
                        return currentNode.value;
                    }
                    currentNode = null;
                    currentBucketIndex++;
                }
            }

            throw new NoSuchElementException();
        }

    }


    /**
     * TODO: Вывести все элементы хеш-таблицы на экран через toString()
     * @return
     */
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("{");
//        for (Entity<K, V> e : this) {
//            sb.append("[").append(e.key).append(": ").append(e.value).append("], ");
//        }
//        if (sb.length() > 1) {
//            sb.delete(sb.length() - 2, sb.length());
//        }
//        sb.append("}");
//        return sb.toString();
//    }




    /**
     * Элемент хеш-таблицы
     */
    static class Entity<K, V> {
        K key;
        V value;

        public Entity(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Бакет, связный список
     */

    static class Bucket<K, V> {
        Node<K, V> head;

        static class Node<K, V> {
            Node<K, V> next;
            Entity<K, V> value;

            public Node(Entity<K, V> value) {
                this.value = value;
            }
        }


        public V add(Entity<K, V> entity) {
            Node<K, V> newNode = new Node<>(entity);
            if (head == null) {
                head = newNode;
                return null;
            }

            Node<K, V> currentNode = head;
            while (true) {
                if (currentNode.value.key.equals(entity.key)) {
                    V oldValue = currentNode.value.value;
                    currentNode.value.value = entity.value;
                    return oldValue;
                }
                if (currentNode.next != null) {
                    currentNode = currentNode.next;
                } else {
                    currentNode.next = newNode;
                    return null;
                }
            }
        }

        public V remove(K key) {
            if (head == null) {
                return null;
            }
            if (head.value.key.equals(key)) {
                V oldValue = head.value.value;
                head = head.next;
                return oldValue;
            } else {
                Node<K, V> currentNode = head;
                while (currentNode.next != null) {
                    if (currentNode.next.value.key.equals(key)) {
                        V oldValue = currentNode.next.value.value;
                        currentNode.next = currentNode.next.next;
                        return oldValue;
                    }
                    currentNode = currentNode.next;
                }
                return null;
            }
        }

        public V get(K key) {
            Node<K, V> currentNode = head;
            while (currentNode != null) {
                if (currentNode.value.key.equals(key)) {
                    return currentNode.value.value;
                }
                currentNode = currentNode.next;
            }
            return null;
        }

    }

    private int calculateBucketIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }


    private void recalculate() {
        size = 0;
        Bucket<K, V>[] old = buckets;
        buckets = new Bucket[old.length * 2];
        for (Bucket<K, V> bucket : old) {
            if (bucket != null) {
                Bucket.Node<K, V> node = bucket.head;
                while (node != null) {
                    put(node.value.key, node.value.value);
                    node = node.next;
                }
            }
        }
    }

    public V put(K key, V value) {
        if (size >= buckets.length * LOAD_FACTOR) {
            recalculate();
        }
        int index = calculateBucketIndex(key);
        Bucket<K, V> bucket = buckets[index];
        if (bucket == null) {
            bucket = new Bucket<>();
            buckets[index] = bucket;
        }

        Entity<K, V> entity = new Entity<>(key, value);

        V oldValue = bucket.add(entity);
        if (oldValue == null) {
            size++;
        }
        return oldValue;
    }


    public V get(K key) {
        int index = calculateBucketIndex(key);
        Bucket<K, V> bucket = buckets[index];
        if (bucket == null) {
            return null;
        }
        return bucket.get(key);
    }

    public V remove(K key) {
        int index = calculateBucketIndex(key);
        Bucket<K, V> bucket = buckets[index];
        if (bucket == null) {
            return null;
        }
        V oldValue = bucket.remove(key);
        if (oldValue != null) {
            size--;
        }
        return oldValue;
    }

//    public HashMap(){
//        buckets = new HashMap.Bucket[INIT_BUCKET_COUNT];
//    }
//
//    public HashMap(int initCount){
//        buckets = new HashMap.Bucket[initCount];
//    }


}
