
package com.supplychain.model;

public class TransferModel {

    private String transferId;
    private String batchId;

    private String fromOwnerId;
    private String fromOwnerRole;
    private String toOwnerId;
    private String toOwnerRole;

    private String transferDate;
    private String transferStatus;
    private String previousBatchStatus;
    private String resolvedAt;

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

    public String getFromOwnerRole() {
        return fromOwnerRole;
    }

    public void setFromOwnerRole(String fromOwnerRole) {
        this.fromOwnerRole = fromOwnerRole;
    }

    public String getToOwnerId() {
        return toOwnerId;
    }

    public void setToOwnerId(String toOwnerId) {
        this.toOwnerId = toOwnerId;
    }

    public String getToOwnerRole() {
        return toOwnerRole;
    }

    public void setToOwnerRole(String toOwnerRole) {
        this.toOwnerRole = toOwnerRole;
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

    public String getPreviousBatchStatus() {
        return previousBatchStatus;
    }

    public void setPreviousBatchStatus(String previousBatchStatus) {
        this.previousBatchStatus = previousBatchStatus;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
