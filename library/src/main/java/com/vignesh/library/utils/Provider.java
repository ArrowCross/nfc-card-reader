package com.vignesh.library.utils;

import android.nfc.tech.IsoDep;

import com.vignesh.library.parser.IProvider;

import java.io.IOException;

public class Provider implements IProvider{

    private IsoDep tagCom;

    public void setTagCom(final IsoDep tagCom) {
        this.tagCom = tagCom;
    }

    @Override
    public byte[] transceive(byte[] pCommand) {

        byte[] response = null;
        try {
            // send command to emv card
            response = tagCom.transceive(pCommand);
        } catch (IOException e) {
//            throw new CommunicationException(e.getMessage());
        }
        return response;
    }
}
