package com.vignesh.library;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.Nullable;

import com.vignesh.library.model.EmvCard;
import com.vignesh.library.parser.EmvParser;
import com.vignesh.library.utils.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class NFCCardReader {

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    private final static String NFC_A_TAG = "TAG: Tech [android.nfc.tech.IsoDep, android.nfc.tech.NfcA]";
    private final static String NFC_B_TAG = "TAG: Tech [android.nfc.tech.IsoDep, android.nfc.tech.NfcB]";

    private Tag tag;
    private boolean isException;


    @Nullable
    public NFCCardResponse readCard() {
        if (tag != null) {
            NFCCardResponse nfcCardResponse = null;
            if (tag.toString().equals(NFC_A_TAG) || tag.toString().equals(NFC_B_TAG)) {
                try {
                    nfcCardResponse = getCardInfo();
                } catch (Exception e) {
                    Log.e(NFCCardReader.class.getName(), e.getMessage(), e);
                }

                if (!isException) {
                    if (nfcCardResponse != null && nfcCardResponse.getEmvCard() != null) {
                        EmvCard emvCard = nfcCardResponse.getEmvCard();
                        if (StringUtils.isNotBlank(emvCard.getCardNumber())) {
                            return NFCCardResponse.createResponse(emvCard);
                        } else if (emvCard.isNfcLocked()) {
                            return NFCCardResponse.createError(NFCCardError.CARD_LOCKED_WITH_NFC);
                        }
                    } else {
                        return NFCCardResponse.createError(NFCCardError.UNKNOWN_EMV_CARD);
                    }
                } else {
                    return NFCCardResponse.createError(NFCCardError.DONOT_MOVE_CARD_SO_FAST);
                }
            } else {
                return NFCCardResponse.createError(NFCCardError.UNKNOWN_EMV_CARD);
            }
        }
        return null;
    }

    private NFCCardResponse getCardInfo(){
        IsoDep isoDep = IsoDep.get(tag);
        Provider provider = new Provider();
        if (isoDep == null) {
            return NFCCardResponse.createError(NFCCardError.DONOT_MOVE_CARD_SO_FAST);
        }

        isException = false;

        try {
            // Open connection
            isoDep.connect();

            provider.setTagCom(isoDep);

            EmvParser parser = new EmvParser(provider, true);
            EmvCard card = parser.readEmvCard();
            if (card != null) {
                return NFCCardResponse.createResponse(card);
            }
        } catch (IOException e) {
            isException = true;
            Log.e(NFCCardReader.class.getName(), e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(isoDep);
        }
        return NFCCardResponse.createError(NFCCardError.UNKNOWN_EMV_CARD);
    }
}