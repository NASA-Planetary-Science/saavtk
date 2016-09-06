package edu.jhuapl.saavtk.model;

public enum SpectralMode
{
    MONO {
        public String toString()
        {
            return "Monospectral";
        }
    },
    MULTI {
        public String toString()
        {
            return "Multispectral";
        }
    },
    HYPER {
        public String toString()
        {
            return "Hyperspectral";
        }
    },
}