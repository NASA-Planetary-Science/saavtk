package edu.jhuapl.saavtk.util;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public abstract class FactoryWithMetadataSupport<T>
{

    protected FactoryWithMetadataSupport()
    {
        super();
    }

    public T of(Metadata md)
    {
        initializeSerializationProxy();
        return instanceGetter().providesGenericObjectFromMetadata(getTargetKey()).provide(md);
    }

    public Metadata toMetadata(T t)
    {
        initializeSerializationProxy();
        return instanceGetter().providesMetadataFromGenericObject(getTargetType()).provide(t);
    }

    public final void initializeSerializationProxy()
    {
        Key<T> mdKey = getTargetKey();

        InstanceGetter instanceGetter = instanceGetter();

        synchronized (getTargetType())
        {
            if (!instanceGetter.isProvidableFromMetadata(mdKey))
            {
                doInitProxy();
            }
        }
    }

    protected abstract Key<T> getTargetKey();

    /**
     * Returned type is used to lock during initialization, so this should always
     * return the same exact {@link Class} instance.
     * 
     * @return
     */
    protected abstract Class<?> getTargetType();

    protected abstract void doInitProxy();

    protected InstanceGetter instanceGetter()
    {
        return InstanceGetter.defaultInstanceGetter();
    }

    protected <T1> T1 get(Key<T1> key, Metadata md, T1 defaultValue) {
        T1 t = defaultValue;
        if (md.hasKey(key)) {
         t = md.get(key);   
        }
        
        return t;
    }

    protected <T1> void set(Key<T1> key, T1 value, SettableMetadata md) {
        md.put(key, value);
    }

}
