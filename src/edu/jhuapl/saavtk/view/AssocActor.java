package edu.jhuapl.saavtk.view;

/**
 * Interface that declares that the actor is tightly associated with a model.
 *
 * @author lopeznr1
 */
public interface AssocActor
{
	/**
	 * Returns the associated model. The returned value should never change.
	 * <P>
	 * The associated model should be set at construction time to allow an object to
	 * be associated with this actor.
	 * <P>
	 * If the associated model does not match that of the specified type then null
	 * will be returned.
	 */
	public <G1> G1 getAssocModel(Class<G1> aType);

}
