package com.twitter.client.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Contains Miscellaneous utility methods
 *
 * @author tejalpar
 */
public class MiscUtils {

    /**
     * Helper method shared at link https://gist.github.com/nesquena/f786232f5ef72f6e10a7
     *
     * @param rawJsonDate
     *          raw date String from json response
     * @return String with relative timestamp
     */
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();

            if (relativeDate.contains(" sec")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" sec"));
                relativeDate += "s";
            } else if (relativeDate.contains(" hour")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" hour"));
                relativeDate += "h";
            } else if (relativeDate.contains(" min")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" min"));
                relativeDate += "m";
            } else if (relativeDate.contains(" day")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" day"));
                relativeDate += "d";
            } else if (relativeDate.contains(" week")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" week"));
                relativeDate += "w";
            } else if (relativeDate.contains(" year")) {
                relativeDate = relativeDate.substring(0, relativeDate.indexOf(" year"));
                relativeDate += "y";
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
