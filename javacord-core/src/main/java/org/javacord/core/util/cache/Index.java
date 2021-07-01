package org.javacord.core.util.cache;

import io.vavr.collection.Set;

import java.util.Optional;
import java.util.function.Function;

public interface Index<K, E> {

    /**
     * Gets the mapping function to map elements to their key.
     *
     * <p>Compound indexes can easily be achieved by using a {@link io.vavr.Tuple} or {@link io.vavr.collection.Seq}
     * with all keys as the return value of the mapping function.
     *
     * @return The mapping function.
     */
    Function<E, K> getKeyMapper();

    /**
     * Adds an element to the index.
     *
     * <p>This method has an effective time complexity of {@code O(1)}.
     *
     * @param element The element to add.
     * @return The new index with the added element.
     */
    Index<K, E> addElement(E element);

    /**
     * Removes an element from the index.
     *
     * <p>This method has an effective time complexity of {@code O(1)}.
     *
     * @param element The element to remove.
     * @return The new index with the element removed.
     */
    Index<K, E> removeElement(E element);

    /**
     * Gets a set with all elements with the given key.
     *
     * <p>This method has an effective time complexity of {@code O(1)}.
     *
     * @param key The key of the elements.
     * @return The elements with the given key.
     */
    Set<E> find(K key);

    /**
     * Gets any element with the given key.
     *
     * <p>This method has an effective time complexity of {@code O(1)}.
     *
     * @param key The key of the element.
     * @return An element with the given key.
     */
    Optional<E> findAny(K key);

}
