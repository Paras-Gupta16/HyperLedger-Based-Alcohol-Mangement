
package com.supplychain.contract;

import com.google.gson.Gson;
import com.supplychain.model.AlcoholBatch;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(name = "AlcoholContract")
public class AlcoholContract implements ContractInterface {

    private final Gson gson = new Gson();

    // Create
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String createBatch(Context ctx, String batchJson)
            throws Exception {

        AlcoholBatch batch = gson.fromJson(batchJson, AlcoholBatch.class);

        if (batch == null) {
            throw new Exception("Invalid batch data");
        }

        String batchId = batch.getBatchId();

        if (batchId == null || batchId.isBlank()) {
            throw new Exception("Batch ID is required");
        }

        String existing = ctx.getStub().getStringState(batchId);

        if (existing != null && !existing.isEmpty()) {
            throw new Exception("Batch Already Exists");
        }

        batch.setStatus("MANUFACTURED");

        String timeStamp = ctx.getStub()
                              .getTxTimestamp().toString();

        batch.setCreatedAt(timeStamp);
        batch.setUpdatedAt(timeStamp);

        String json = gson.toJson(batch);

        ctx.getStub().putStringState(batchId, json);

        return json;
    }

    // Read
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String readBatch(Context ctx, String batchId)
            throws Exception {

        if (batchId == null || batchId.isBlank()) {
            throw new Exception("Batch ID is required");
        }

        String batch = ctx.getStub().getStringState(batchId);

        if (batch == null || batch.isBlank()) {
            throw new Exception("Batch does not exist");
        }

        return batch;
    }
}
