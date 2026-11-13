package com.supplychain.contract;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.supplychain.contract.utility.ValidationClass;
import com.supplychain.model.AlcoholBatch;
import com.supplychain.model.TransferModel;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

@Contract(name = "AlcoholContract")
public class AlcoholContract implements ContractInterface {

    private static final String BATCH_PREFIX = "BATCH:";
    private static final String TRANSFER_PREFIX = "TRANSFER:";
    private static final String TRANSFER_INDEX = "TransferByBatch";
    private static final String MANUFACTURED = "MANUFACTURED";
    private static final String TRANSFER_PENDING = "TRANSFER_PENDING";

    private final Gson gson = new Gson();
    private final ValidationClass validation = new ValidationClass();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String createBatch(Context ctx, String batchJson) {
        AlcoholBatch batch = gson.fromJson(batchJson, AlcoholBatch.class);
        if (batch == null) {
            throw new IllegalArgumentException("Invalid batch data");
        }

        String manufacturerId = validation.requireCaller(ctx, ValidationClass.MANUFACTURER);
        String batchId = requireValue(batch.getBatchId(), "Batch ID");
        if (!manufacturerId.equals(requireValue(batch.getManufacturerId(), "Manufacturer ID"))) {
            throw new IllegalArgumentException("Batch manufacturer must match the caller");
        }
        batch.setProductName(requireValue(batch.getProductName(), "Product name"));
        batch.setUnit(requireValue(batch.getUnit(), "Quantity unit"));
        if (!Double.isFinite(batch.getQuantity()) || batch.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive finite number");
        }
        batch.setProductionDate(requireValue(batch.getProductionDate(), "Production date"));
        batch.setExpiryDate(requireValue(batch.getExpiryDate(), "Expiry date"));
        validateBatchDates(batch.getProductionDate(), batch.getExpiryDate());

        if (readBatchState(ctx, batchId) != null) {
            throw new IllegalArgumentException("Batch already exists");
        }

        String timestamp = ctx.getStub().getTxTimestamp().toString();
        batch.setBatchId(batchId);
        batch.setManufacturerId(manufacturerId);
        batch.setCurrentOwner(manufacturerId);
        batch.setStatus(MANUFACTURED);
        batch.setPendingTransferId(null);
        batch.setCreatedAt(timestamp);
        batch.setUpdatedAt(timestamp);

        String json = gson.toJson(batch);
        ctx.getStub().putStringState(BATCH_PREFIX + batchId, json);
        return json;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String readBatch(Context ctx, String batchId) {
        String normalizedId = requireValue(batchId, "Batch ID");
        String batch = readBatchState(ctx, normalizedId);
        if (batch == null || batch.isBlank()) {
            throw new IllegalArgumentException("Batch does not exist");
        }
        return batch;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String initiateTransfer(Context ctx, String transferJson) {
        TransferModel transfer = gson.fromJson(transferJson, TransferModel.class);
        if (transfer == null) {
            throw new IllegalArgumentException("Invalid transfer data");
        }

        String callerRole = ctx.getClientIdentity().getAttributeValue(ValidationClass.ACTOR_ROLE_ATTRIBUTE);
        if (!ValidationClass.isSupportedActorRole(callerRole)) {
            throw new IllegalArgumentException("Caller certificate must contain a supported actorRole attribute");
        }
        String fromOwnerId = validation.requireCaller(ctx, callerRole);
        String transferId = requireValue(transfer.getTransferId(), "Transfer ID");
        String batchId = requireValue(transfer.getBatchId(), "Batch ID");
        String toOwnerId = requireValue(transfer.getToOwnerId(), "Recipient ID");
        String transferKey = TRANSFER_PREFIX + transferId;
        if (hasState(ctx, transferKey)) {
            throw new IllegalArgumentException("Transfer already exists");
        }

        String toOwnerRole = getRegisteredOwnerRole(ctx, toOwnerId, callerRole);
        if (!isAllowedTransfer(callerRole, toOwnerRole)) {
            throw new IllegalArgumentException("Transfers must follow manufacturer -> distributor -> retailer");
        }

        String batchKey = findBatchKey(ctx, batchId);
        if (batchKey == null) {
            throw new IllegalArgumentException("Batch does not exist");
        }
        AlcoholBatch batch = gson.fromJson(ctx.getStub().getStringState(batchKey), AlcoholBatch.class);
        if (!fromOwnerId.equals(batch.getCurrentOwner())) {
            throw new IllegalArgumentException("Only the current batch owner may initiate a transfer");
        }
        if (batch.getPendingTransferId() != null && !batch.getPendingTransferId().isBlank()) {
            throw new IllegalArgumentException("Batch already has a pending transfer");
        }
        if (fromOwnerId.equals(toOwnerId)) {
            throw new IllegalArgumentException("Sender and recipient must be different actors");
        }

        String previousStatus = requireValue(batch.getStatus(), "Batch status");
        transfer.setTransferId(transferId);
        transfer.setBatchId(batchId);
        transfer.setFromOwnerId(fromOwnerId);
        transfer.setFromOwnerRole(callerRole);
        transfer.setToOwnerId(toOwnerId);
        transfer.setToOwnerRole(toOwnerRole);
        transfer.setTransferDate(ctx.getStub().getTxTimestamp().toString());
        transfer.setTransferStatus("PENDING");
        transfer.setPreviousBatchStatus(previousStatus);
        transfer.setResolvedAt(null);

        batch.setPendingTransferId(transferId);
        batch.setStatus(TRANSFER_PENDING);
        batch.setUpdatedAt(ctx.getStub().getTxTimestamp().toString());

        String transferState = gson.toJson(transfer);
        ctx.getStub().putStringState(batchKey, gson.toJson(batch));
        ctx.getStub().putStringState(transferKey, transferState);
        String indexKey = ctx.getStub()
                .createCompositeKey(TRANSFER_INDEX, batchId, transferId)
                .toString();
        ctx.getStub().putStringState(indexKey, transferKey);
        return transferState;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String acceptTransfer(Context ctx, String transferId) {
        return resolveTransfer(ctx, transferId, true);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String rejectTransfer(Context ctx, String transferId) {
        return resolveTransfer(ctx, transferId, false);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String readTransfer(Context ctx, String transferId) {
        String normalizedId = requireValue(transferId, "Transfer ID");
        String transfer = ctx.getStub().getStringState(TRANSFER_PREFIX + normalizedId);
        if (transfer == null || transfer.isBlank()) {
            throw new IllegalArgumentException("Transfer does not exist");
        }
        return transfer;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getBatchTransfers(Context ctx, String batchId) {
        String normalizedBatchId = requireValue(batchId, "Batch ID");
        if (findBatchKey(ctx, normalizedBatchId) == null) {
            throw new IllegalArgumentException("Batch does not exist");
        }

        List<TransferModel> transfers = new ArrayList<>();
        try (QueryResultsIterator<KeyValue> results =
                     ctx.getStub().getStateByPartialCompositeKey(TRANSFER_INDEX, normalizedBatchId)) {
            for (KeyValue result : results) {
                String transferKey = result.getStringValue();
                String transferState = ctx.getStub().getStringState(transferKey);
                if (transferState != null && !transferState.isBlank()) {
                    transfers.add(gson.fromJson(transferState, TransferModel.class));
                }
            }
        }
        transfers.sort(Comparator.comparing((TransferModel transfer) -> Instant.parse(transfer.getTransferDate()))
                .thenComparing(TransferModel::getTransferId));
        return gson.toJson(transfers);
    }

    private String resolveTransfer(Context ctx, String transferId, boolean accepted) {
        String normalizedTransferId = requireValue(transferId, "Transfer ID");
        String transferKey = TRANSFER_PREFIX + normalizedTransferId;
        String transferState = ctx.getStub().getStringState(transferKey);
        if (transferState == null || transferState.isBlank()) {
            throw new IllegalArgumentException("Transfer does not exist");
        }

        TransferModel transfer = gson.fromJson(transferState, TransferModel.class);
        if (!"PENDING".equals(transfer.getTransferStatus())) {
            throw new IllegalArgumentException("Transfer is not pending");
        }
        String receiverId = validation.requireCaller(ctx, transfer.getToOwnerRole());
        if (!receiverId.equals(transfer.getToOwnerId())) {
            throw new IllegalArgumentException("Only the intended recipient may resolve this transfer");
        }

        String batchKey = findBatchKey(ctx, transfer.getBatchId());
        if (batchKey == null) {
            throw new IllegalArgumentException("Batch does not exist");
        }
        AlcoholBatch batch = gson.fromJson(ctx.getStub().getStringState(batchKey), AlcoholBatch.class);
        if (!transfer.getTransferId().equals(batch.getPendingTransferId())
                || !transfer.getFromOwnerId().equals(batch.getCurrentOwner())) {
            throw new IllegalArgumentException("Transfer no longer matches the batch's pending ownership");
        }

        if (accepted) {
            batch.setCurrentOwner(transfer.getToOwnerId());
            batch.setStatus(statusForOwnerRole(transfer.getToOwnerRole()));
            transfer.setTransferStatus("ACCEPTED");
        } else {
            batch.setStatus(transfer.getPreviousBatchStatus());
            transfer.setTransferStatus("REJECTED");
        }

        batch.setPendingTransferId(null);
        String timestamp = ctx.getStub().getTxTimestamp().toString();
        batch.setUpdatedAt(timestamp);
        transfer.setResolvedAt(timestamp);
        String updatedTransfer = gson.toJson(transfer);
        ctx.getStub().putStringState(batchKey, gson.toJson(batch));
        ctx.getStub().putStringState(transferKey, updatedTransfer);
        return updatedTransfer;
    }

    private String getRegisteredOwnerRole(Context ctx, String actorId, String callerRole) {
        String expectedRole;
        if (ValidationClass.MANUFACTURER.equals(callerRole)) {
            expectedRole = ValidationClass.DISTRIBUTOR;
        } else if (ValidationClass.DISTRIBUTOR.equals(callerRole)) {
            expectedRole = ValidationClass.RETAILER;
        } else {
            throw new IllegalArgumentException("Retailers cannot transfer batches further");
        }
        if (!validation.isValidActor(ctx, actorId, expectedRole)) {
            throw new IllegalArgumentException("Recipient must be a registered active " + expectedRole);
        }
        return expectedRole;
    }

    private static boolean isAllowedTransfer(String fromRole, String toRole) {
        return (ValidationClass.MANUFACTURER.equals(fromRole)
                    && ValidationClass.DISTRIBUTOR.equals(toRole))
                || (ValidationClass.DISTRIBUTOR.equals(fromRole)
                    && ValidationClass.RETAILER.equals(toRole));
    }

    private static String statusForOwnerRole(String role) {
        if (ValidationClass.DISTRIBUTOR.equals(role)) {
            return "WITH_DISTRIBUTOR";
        }
        if (ValidationClass.RETAILER.equals(role)) {
            return "AT_RETAILER";
        }
        throw new IllegalArgumentException("Unsupported recipient role");
    }

    private String readBatchState(Context ctx, String batchId) {
        String batchKey = findBatchKey(ctx, batchId);
        return batchKey == null ? null : ctx.getStub().getStringState(batchKey);
    }

    private String findBatchKey(Context ctx, String batchId) {
        String namespacedKey = BATCH_PREFIX + batchId;
        if (hasState(ctx, namespacedKey)) {
            return namespacedKey;
        }
        return hasState(ctx, batchId) ? batchId : null;
    }

    private static boolean hasState(Context ctx, String key) {
        String state = ctx.getStub().getStringState(key);
        return state != null && !state.isBlank();
    }

    private static String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static void validateBatchDates(String productionDate, String expiryDate) {
        String normalizedProductionDate = requireValue(productionDate, "Production date");
        String normalizedExpiryDate = requireValue(expiryDate, "Expiry date");
        LocalDate production;
        LocalDate expiry;
        try {
            production = LocalDate.parse(normalizedProductionDate);
            expiry = LocalDate.parse(normalizedExpiryDate);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("Production and expiry dates must use ISO format (yyyy-MM-dd)", exception);
        }
        if (!expiry.isAfter(production)) {
            throw new IllegalArgumentException("Expiry date must be after production date");
        }
    }
}
