
package com.supplychain.model;

public class AlcoholBatch {

    private String batchId;
    private String productName;
    private String manufacturerId;
    private String productionDate;
    private String expiryDate;
    private double quantity;
    private String unit;
    private String currentOwner;
    private String status;
    private String pendingTransferId;
    private String createdAt;
    private String updatedAt;
    private String ethereumTxHash;

    public AlcoholBatch() {}

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(String currentOwner) {
        this.currentOwner = currentOwner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPendingTransferId() {
        return pendingTransferId;
    }

    public void setPendingTransferId(String pendingTransferId) {
        this.pendingTransferId = pendingTransferId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEthereumTxHash() {
        return ethereumTxHash;
    }

    public void setEthereumTxHash(String ethereumTxHash) {
        this.ethereumTxHash = ethereumTxHash;
    }
}
