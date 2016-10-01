package com.chan.selavukanakku.sheets;

public class SheetContent
{
    public final String id;
    public final String content;
    public final String details;

    public SheetContent(String id, String content, String details)
    {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString()
    {
        return content;
    }
}