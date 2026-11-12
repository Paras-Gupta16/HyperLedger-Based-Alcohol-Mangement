
package com.supplychain.model;

public class TransferModel {

    private String transferId;
    private String batchId;

    private String fromOwnerId;
    private String toOwnerId;

    private String transferDate;
    private String transferStatus;

    private String remarks;

    public TransferModel() {}

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getFromOwnerId() {
        return fromOwnerId;
    }

    public void setFromOwnerId(String fromOwnerId) {
        this.fromOwnerId = fromOwnerId;
    }

    public String getToOwnerId() {
        return toOwnerId;
    }

    public void setToOwnerId(String toOwnerId) {
        this.toOwnerId = toOwnerId;
    }

    public String getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(String transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
