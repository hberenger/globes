package com.bureau.nocomment.globes.common;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.bureau.nocomment.globes.activity.DetailActivity;

import java.io.UnsupportedEncodingException;


public class ClassicNfcTextRecordParser implements DetailActivity.NfcTagMessageParser {

    final private static String GLOBES_PREFIX = "globes-";

    @Override
    public int readProjectIdFromNdefMessage(NdefMessage message) {
        int projectId = -1;
        NdefRecord[] records = message.getRecords();
        if (records.length > 0) {
            byte[] payloadBytes = records[0].getPayload();

            boolean isUTF8 = (payloadBytes[0] & 0x080) == 0;  //status byte: bit 7 indicates encoding (0 = UTF-8, 1 = UTF-16)
            int languageLength = payloadBytes[0] & 0x03F;     //status byte: bits 5..0 indicate length of language code
            int textLength = payloadBytes.length - 1 - languageLength;

            try {
                String contents = new String(payloadBytes, 1 + languageLength, textLength, isUTF8 ? "UTF-8" : "UTF-16");
                if (contents.indexOf(GLOBES_PREFIX) == 0) {
                    String suffix = contents.substring(GLOBES_PREFIX.length());
                    projectId = Integer.parseInt(suffix);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return projectId;
    }
}
