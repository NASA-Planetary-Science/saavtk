package edu.jhuapl.saavtk.util;

public class IdPair
{
    public int id1;
    public int id2;
    public IdPair(int id1, int id2)
    {
        this.id1 = id1;
        this.id2 = id2;
    }

    /**
     * Add a numbers to each id so that the next block of a specified size
     * is stored in this class.
     * @param val
     */
    public void nextBlock(int size)
    {
        id1 = id2;
        id2 += size;
    }

    /**
     * Subtract numbers from each id so that the previous block of a specified size
     * is stored in this class.
     * @param val
     */
    public void prevBlock(int size)
    {
        id2 = id1;
        id1 -= size;
    }

    @Override
    public boolean equals(Object o)
    {
        int otherId1 = ((IdPair)o).id1;
        int otherId2 = ((IdPair)o).id2;
        return id1 == otherId1 && id2 == otherId2;
    }

    @Override
    public String toString()
    {
        return id1 + " " + id2;
    }

    @Override
    public int hashCode() {
        // This hash seems to produce efficient look ups. Not sure what
        // the best hash is though.
        return id1;
    }
}
