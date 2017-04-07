package com.ck.taxoteam.taxoclient.model;

/**
 * Created by Sveta on 01.02.2017.
 */

public class ModelAddressLine {
    public static final int TEXT_TYPE = 0;
    public static final int EDIT_TYPE = 1;
    public int type;
    public String text;

    public ModelAddressLine(int type, String text)
    {
        this.type = type;
        this.text = text;
    }
}
