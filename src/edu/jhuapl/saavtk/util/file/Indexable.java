package edu.jhuapl.saavtk.util.file;

/**
 * Basic interface describing a list of indexed records.
 * 
 * @author F.S.Turner
 * 
 * @param <R> the type of the record required by the implementation
 */
public interface Indexable<R> extends Sizeable {

  /**
   * Retrieve a particular record from the list
   * 
   * @param index the index of interest
   * 
   * @return the value retrieved
   * 
   * @throws IndexOutOfBoundsException if index lies outside the acceptable range supported by the
   *         instance: [0, {@link Sizeable#size()}-1].
   */
  R get(int index);

}