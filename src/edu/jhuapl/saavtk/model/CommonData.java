package edu.jhuapl.saavtk.model;

import edu.jhuapl.saavtk.util.Preferences;

public class CommonData
{
        private int[] selectionColor;

        public CommonData()
        {
            selectionColor = Preferences.getInstance().getAsIntArray(Preferences.SELECTION_COLOR, new int[]{0, 0, 255});
        }

        public int[] getSelectionColor()
        {
            return selectionColor;
        }

        public void setSelectionColor(int[] selectionColor)
        {
            this.selectionColor = selectionColor.clone();
        }
}
