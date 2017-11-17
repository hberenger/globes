package com.bureau.nocomment.globes.common;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;

public class Tagger {
    private static final Tagger mInstance = new Tagger();

    private static String filename = "globe_activity.txt";
    private static String separator = ">";
    private FileOutputStream outputStream;
    private SimpleDateFormat formatter;
    private String outOfBandBuffer = "";

    public static Tagger getInstance() {
        return mInstance;
    }

    private Tagger() {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.FRANCE);
    }

    public void start(Context context) {
        try {
            File dir = context.getFilesDir();
            File file = new File(dir, filename);
            file.setReadable(true, false); // to ease file transfer
            outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                outputStream = null;
            }

        }
    }

    public void tag(String ctx, String msg) {
        String output = "";

        Date now = new Date();
        String date = formatter.format(now);
        date = date + separator;
        output = output + date;

        ctx = ctx + separator;
        output = output + ctx;

        output = output + msg;

        // eol
        String eol = "\n";
        output = output + eol;

        writeString(output);
    }

    private void writeString(String output) {
        if (outputStream == null) {
            outOfBandBuffer = outOfBandBuffer + output;
            return;
        }
        try {
            if (!outOfBandBuffer.isEmpty()) {
                outputStream.write("!!! OutOfBand begin\n".getBytes());
                outputStream.write(outOfBandBuffer.getBytes());
                outputStream.write("!!! OutOfBand end\n".getBytes());
                outOfBandBuffer = "";
            }
            outputStream.write(output.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
