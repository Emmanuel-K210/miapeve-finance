package com.miapeve.finance.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateConverter {
    
    // Formatters constants
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static final DateTimeFormatter DATE_FORMATTER2 = 
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    // LocalDate → String (pour SQLite)
    public static String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    // String (de SQLite) → LocalDate
    public static LocalDate stringToLocalDate(String dateString) {
        return dateString != null ? 
            LocalDate.parse(dateString, DATE_FORMATTER) : null;
    }
    
    public static String formatToStringForPrint(LocalDate date) {
    	return date == null ? null : date.format(DATE_FORMATTER2);
    }
    
    // LocalTime → String (pour SQLite)
    public static String localTimeToString(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }
    
    // String (de SQLite) → LocalTime
    public static LocalTime stringToLocalTime(String timeString) {
        return timeString != null ? 
            LocalTime.parse(timeString, TIME_FORMATTER) : null;
    }
    
    //String (Converti les numero en mois)
    public static String convertToMoisFrench(LocalDate date) {
    	return switch(date.getMonthValue()) {
    	case 1 -> "janvier";
    	case 2 -> "février";
    	case 3 -> "mars";
    	case 4 -> "avril";
    	case 5 -> "mai";
    	case 6 -> "juin";
    	case 7 -> "juillet";
    	case 8 -> "août";
    	case 9 -> "septembre";
    	case 10 -> "octobre";
    	case 11 -> "novembre";
    	case 12 -> "décembre";
    	default ->"decembre";
    	};
    }
}