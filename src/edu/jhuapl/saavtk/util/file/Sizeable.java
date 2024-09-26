package edu.jhuapl.saavtk.util.file;

import picante.data.list.Retrievable;

/**
 * Simple expression of the size retrieval method.
 * <p>
 * This interface exists purely to allow functionality from the {@link Indexable} and
 * {@link Retrievable} inheritance trees to share code.
 * </p>
 * 
 * @author F.S.Turner
 * 
 */
public interface Sizeable {

  /**
   * Get the size of the list
   * 
   * @return the number of records in the list
   */
  public int size();

}