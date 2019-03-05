package com.manolevski.kasovbon.Utils;

import java.util.Date;

/**
 * Created by Anastas Manolevski on 05.03.2019.
 */
public class ScannerResult {
    private String merchantId;
    private String receiptId;
    private Date date;
    private Date time;
    private String price;

    public ScannerResult(String merchantId, String receiptId, Date date, Date time, String price) {
        this.merchantId = merchantId;
        this.receiptId = receiptId;
        this.date = date;
        this.time = time;
        this.price = price;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public Date getDate() {
        return date;
    }

    public Date getTime() {
        return time;
    }

    public String getPrice() {
        return price;
    }
}
