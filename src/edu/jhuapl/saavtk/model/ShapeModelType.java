package edu.jhuapl.saavtk.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * This class was originally an enumeration (ShapeModelAuthor), but was later
 * modified to allow new members to be added dynamically. Enum-like methods were
 * provided for the benefit of existing code. The code prevents two shape model
 * types with the same identifier string.
 * 
 * This class is not thread-safe.
 */
public final class ShapeModelType
{
    private static final Map<String, ShapeModelType> SHAPE_MODEL_IDENTIFIERS = new HashMap<>();

    /**
     * Return the ShapeModelType object associated with the provided identifier
     * parameter.
     * 
     * Deprecated in favor of the provide method, which creates a new ShapeModelType
     * if it is missing.
     * 
     * @param identifier of the ShapeModelType object
     * @return the ShapeModelType object
     * @throws IllegalArgumentException if there is no associated ShapeModelType.
     */
    @Deprecated
    public static ShapeModelType valueOf(String identifier)
    {
        Preconditions.checkArgument(SHAPE_MODEL_IDENTIFIERS.containsKey(identifier), "Cannot find a ShapeModelType object for identifier " + identifier);
        return SHAPE_MODEL_IDENTIFIERS.get(identifier);
    }

    /**
     * Create and return a new ShapeModelType object identified with the provided
     * identifier string.
     * 
     * @param identifier of the ShapeModelType object
     * @return the ShapeModelType object
     * @throws IllegalArgumentException if there is already a ShapeModelType object
     *             associated with the identifier
     */
    private static ShapeModelType create(String identifier)
    {
        Preconditions.checkNotNull(identifier);
        Preconditions.checkArgument(!SHAPE_MODEL_IDENTIFIERS.containsKey(identifier), "Already have a ShapeModelType object for identifier " + identifier);

        ShapeModelType result = new ShapeModelType(identifier);
        SHAPE_MODEL_IDENTIFIERS.put(identifier, result);

        return result;
    }

    /**
     * Provide a ShapeModelType object for the given identifier parameter. If a
     * ShapeModelType object already exists for this identity, it is simply
     * returned, otherwise it is created first.
     * 
     * @param identifier of the ShapeModelType object
     * @return the ShapeModelType object
     */
    public static ShapeModelType provide(String identifier)
    {
        Preconditions.checkNotNull(identifier);

        ShapeModelType result = SHAPE_MODEL_IDENTIFIERS.get(identifier);
        if (result == null)
        {
            result = new ShapeModelType(identifier);
            SHAPE_MODEL_IDENTIFIERS.put(identifier, result);
        }

        return result;
    }

    public static boolean contains(String identifier)
    {
        Preconditions.checkNotNull(identifier);
        return SHAPE_MODEL_IDENTIFIERS.containsKey(identifier);
    }

    public static final ShapeModelType GASKELL = create("Gaskell");
    public static final ShapeModelType THOMAS = create("Thomas");
    public static final ShapeModelType STOOKE = create("Stooke");
    public static final ShapeModelType HUDSON = create("Hudson");
    public static final ShapeModelType DUXBURY = create("Duxbury");
    public static final ShapeModelType OSTRO = create("Ostro");
    public static final ShapeModelType JORDA = create("Jorda");
    public static final ShapeModelType NOLAN = create("Nolan");
    public static final ShapeModelType CUSTOM = create("Custom");
    public static final ShapeModelType EROSNAV = create("NAV");
    public static final ShapeModelType EROSNLR = create("NLR");
    public static final ShapeModelType EXPERIMENTAL = create("Experimental");
    public static final ShapeModelType LORRI = create("LORRI");
    public static final ShapeModelType MVIC = create("MVIC");
    public static final ShapeModelType CARRY = create("Carry");
    public static final ShapeModelType DLR = create("DLR");
    public static final ShapeModelType BLENDER = create("Zimmerman");
    public static final ShapeModelType JAXA_SFM_v20180714 = create("JAXA-SFM-v20180714");
    public static final ShapeModelType JAXA_SFM_v20180627 = create("JAXA-SFM-v20180627");
    public static final ShapeModelType JAXA_SFM_v20180725_2 = create("JAXA-SFM-v20180725-2");
    public static final ShapeModelType JAXA_SFM_v20180804 = create("JAXA-SFM-v20180804");
    public static final ShapeModelType JAXA_SPC_v20180705 = create("JAXA-SPC-v20180705");
    public static final ShapeModelType JAXA_SPC_v20180717 = create("JAXA-SPC-v20180717");
    public static final ShapeModelType JAXA_SPC_v20180719_2 = create("JAXA-SPC-v20180719-2");
    public static final ShapeModelType JAXA_SPC_v20180731 = create("JAXA-SPC-v20180731");
    public static final ShapeModelType JAXA_SPC_v20180810 = create("JAXA-SPC-v20180810");
    public static final ShapeModelType JAXA_SPC_v20180816 = create("JAXA-SPC-v20180816");
    public static final ShapeModelType JAXA_SPC_v20180829 = create("JAXA-SPC-v20180829");
    public static final ShapeModelType JAXA_SPC_v20181014 = create("JAXA-SPC-v20181014");
    public static final ShapeModelType NASA_001 = create("NASA-001");
    public static final ShapeModelType NASA_002 = create("NASA-002");
    public static final ShapeModelType NASA_003 = create("NASA-003");
    public static final ShapeModelType NASA_004 = create("NASA-004");
    public static final ShapeModelType NASA_005 = create("NASA-005");
    public static final ShapeModelType NASA_006 = create("NASA-006");
    public static final ShapeModelType OREX = create("OSIRIS-REx");
    public static final ShapeModelType TRUTH = create("Truth");
    public static final ShapeModelType ALTWG_SPC_v20181109b = create("ALTWG-SPC-v20181109b");
    public static final ShapeModelType ALTWG_SPC_v20181115 = create("ALTWG-SPC-v20181115");
    public static final ShapeModelType ALTWG_SPC_v20181116 = create("ALTWG-SPC-v20181116");
    public static final ShapeModelType ALTWG_SPC_v20181123b = create("ALTWG-SPC-v20181123b");
    public static final ShapeModelType ALTWG_SPC_v20181202 = create("ALTWG-SPC-v20181202");
    public static final ShapeModelType ALTWG_SPC_v20181206 = create("ALTWG-SPC-v20181206");
    public static final ShapeModelType ALTWG_SPC_v20181217 = create("ALTWG-SPC-v20181217");
    public static final ShapeModelType ALTWG_SPC_v20181227 = create("ALTWG-SPC-v20181227");
    public static final ShapeModelType ALTWG_SPC_v20190105 = create("ALTWG-SPC-v20190105");
    public static final ShapeModelType ALTWG_SPC_v20190114 = create("ALTWG-SPC-v20190114");
    public static final ShapeModelType ALTWG_SPC_v20190117 = create("ALTWG-SPC-v20190117");
    public static final ShapeModelType ALTWG_SPC_v20190121 = create("ALTWG-SPC-v20190121");
    public static final ShapeModelType ALTWG_SPC_v20190207a = create("ALTWG-SPC-v20190207a");
    public static final ShapeModelType ALTWG_SPC_v20190207b = create("ALTWG-SPC-v20190207b");
    public static final ShapeModelType ALTWG_SPC_v20190414 = create("ALTWG-SPC-v20190414");
    public static final ShapeModelType ALTWG_SPO_v20190612 = create("ALTWG-SPO-v20190612");
    public static final ShapeModelType MU69_TEST5H_1_FINAL_ORIENTED = create("mu69_test5h_1_final_oriented");
    public static final ShapeModelType NIMMO = create("Nimmo");
    public static final ShapeModelType WEAVER = create("Weaver");
    public static final ShapeModelType HANUS = create("Hanus");
    public static final ShapeModelType VIIKINKOSKI = create("Viikinkoski");
    //***********************************************************************************
    //***********************************************************************************
    // DO NOT ADD ANY MORE STATIC FIELDS HERE FOR NEW AUTHORS. INSTEAD, PROVIDE THEM
    // DYNAMICALLY BY CALLING THE ShapeModelType.provide(String) METHOD.
    //
    // Example:
    // 
    // c.author = ShapeModelType.provide("Gonzalez");
    //
    //***********************************************************************************
    //***********************************************************************************

    private final String identifier;

    private ShapeModelType(String identifier)
    {
        this.identifier = identifier;
    }

    public String name()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return identifier;
    }
}