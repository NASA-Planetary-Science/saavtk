package edu.jhuapl.saavtk.util;

public class DateTimeUtil
{
    /**
     * Convert Bob Gaskell's date time format as used in his sumfiles to
     * yyyy-MM-dd'T'HH:mm:ss.SSS format. Java does not seem to support
     * this format.
     *
     * @param datetime
     * @return
     */
    public static String convertDateTimeFormat(String datetime)
    {
        String[] tokens = datetime.trim().split("\\s+");

        String year  = tokens[0];
        String month = tokens[1];
        String day   = tokens[2];
        String time  = tokens[3];

        if (month.equals("JAN"))
            month = "01";
        else if (month.equals("FEB"))
            month = "02";
        else if (month.equals("MAR"))
            month = "03";
        else if (month.equals("APR"))
            month = "04";
        else if (month.equals("MAY"))
            month = "05";
        else if (month.equals("JUN"))
            month = "06";
        else if (month.equals("JUL"))
            month = "07";
        else if (month.equals("AUG"))
            month = "08";
        else if (month.equals("SEP"))
            month = "09";
        else if (month.equals("OCT"))
            month = "10";
        else if (month.equals("NOV"))
            month = "11";
        else if (month.equals("DEC"))
            month = "12";

        return year + "-" + month + "-" + day + "T" + time;
    }
}
