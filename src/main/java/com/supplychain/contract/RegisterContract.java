package com.supplychain.contract;

import java.util.Map;

import com.google.gson.Gson;
import com.supplychain.contract.utility.ValidationClass;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(name = "RegisterContract")
public class RegisterContract implements ContractInterface {

    private final Gson gson = new Gson();
    private final ValidationClass validation = new ValidationClass();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerManufacturer(Context ctx, String manufacturerId, String manufacturerName) {
        return registerActor(ctx, manufacturerId, manufacturerName, ValidationClass.MANUFACTURER);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerDistributor(Context ctx, String distributorId, String distributorName) {
        return registerActor(ctx, distributorId, distributorName, ValidationClass.DISTRIBUTOR);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerRetailer(Context ctx, String retailerId, String retailerName) {
        return registerActor(ctx, retailerId, retailerName, ValidationClass.RETAILER);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String readActor(Context ctx, String role, String actorId) {
        if (!ValidationClass.isSupportedActorRole(role)) {
            throw new IllegalArgumentException("Role must be MANUFACTURER, DISTRIBUTOR, or RETAILER");
        }
        String normalizedId = requireValue(actorId, "Actor ID");
        String actor = ctx.getStub().getStringState(ValidationClass.actorKey(role, normalizedId));
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Actor does not exist");
        }
        return actor;
    }

    private String registerActor(Context ctx, String actorId, String actorName, String role) {
        String normalizedId = requireValue(actorId, "Actor ID");
        String normalizedName = requireValue(actorName, "Actor name");
        validation.requireActorRegistrationIdentity(ctx, normalizedId, role);

        for (String registeredRole : new String[] {
                ValidationClass.MANUFACTURER,
                ValidationClass.DISTRIBUTOR,
                ValidationClass.RETAILER
        }) {
            String registeredActor = ctx.getStub()
                    .getStringState(ValidationClass.actorKey(registeredRole, normalizedId));
            if (registeredActor != null && !registeredActor.isBlank()) {
                throw new IllegalArgumentException("Actor ID is already registered");
            }
        }

        String actorKey = ValidationClass.actorKey(role, normalizedId);
        String timestamp = ctx.getStub().getTxTimestamp().toString();
        String actorJson = gson.toJson(Map.of(
                "actorId", normalizedId,
                "actorName", normalizedName,
                "actorRole", role,
                "status", "ACTIVE",
                "createdAt", timestamp
        ));
        ctx.getStub().putStringState(actorKey, actorJson);
        return actorJson;
    }

    private static String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

}
