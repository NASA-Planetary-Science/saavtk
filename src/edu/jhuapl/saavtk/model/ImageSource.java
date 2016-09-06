package edu.jhuapl.saavtk.model;

public enum ImageSource
{
    SPICE {
        public String toString()
        {
            return "SPICE Derived";
        }
    },
    GASKELL {
        public String toString()
        {
            return "Gaskell Derived";
        }
    },
    LABEL {
        public String toString()
        {
            return "Label Derived";
        }
    },
    CORRECTED {
        public String toString()
        {
            return "Corrected";
        }
    },
    CORRECTED_SPICE {
        public String toString()
        {
            return "Corrected SPICE Derived";
        }
    },
    IMAGE_MAP {
        public String toString()
        {
            return "ImageMap";
        }
    },
    LOCAL_CYLINDRICAL {
        public String toString()
        {
            return "LocalCylindrical";
        }
    },
    LOCAL_PERSPECTIVE {
        public String toString()
        {
            return "LocalPerspective";
        }
    },
    FALSE_COLOR {
        public String toString()
        {
            return "FalseColor";
        }
    }
}