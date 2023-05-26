package edu.jhuapl.saavtk.util;

/**
 * This interface lists all properties that get fired in firePropertyChange calls.
 * All such properties should be listed here rather than in the
 * class that does the firing.
 */
public interface Properties
{
    final String MODEL_CHANGED = "model-changed";
    final String MODEL_PICKED = "model-picked";
    final String MODEL_REMOVED = "model-removed";

    final String MSI_IMAGE_BACKPLANE_GENERATION_UPDATE = "msi-image-backplane-generation-update";
    final String MODEL_RESOLUTION_CHANGED = "model-resolution-changed";
    
    final String MODEL_POSITION_CHANGED = "model-position-changed";

    final String SPECTRUM_REGION_CHANGED = "spectrum-region-changed";

    final String CUSTOM_MODEL_ADDED = "custom-model-added";
    final String CUSTOM_MODEL_DELETED = "custom-model-deleted";
    final String CUSTOM_MODEL_EDITED = "custom-model-edited";
}
