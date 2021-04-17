package com.itis.infosearch;

import java.util.regex.Pattern;

public class Constants {
    public static final Pattern FILTER_FORMAT =
            Pattern.compile("^.*(\\.(css|js|bmp|gif|jpe?g|png|" +
                    "tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|" +
                    "m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    public static final int FILE_LIMIT = 100;
    public static final int WORD_LIMIT = 1000;
    public static final int BILLION = 1_000_000_000;
}
