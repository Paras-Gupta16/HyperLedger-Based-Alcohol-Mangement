package com.supplychain.contract.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.hyperledger.fabric.contract.Context;

public final class ValidationClass {

    public static final String MANUFACTURER = "MANUFACTURER";
    public static final String DISTRIBUTOR = "DISTRIBUTOR";
    public static final String RETAILER = "RETAILER";
    public static final String ACTOR_ID_ATTRIBUTE = "actorId";
    public static final String ACTOR_ROLE_ATTRIBUTE = "actorRole";

    private final Gson gson = new Gson();

    public boolean isValidManufacturer(Context ctx, String manufacturerId) {
        return isValidActor(ctx, manufacturerId, MANUFACTURER);
    }

    public boolean isValidDistributor(Context ctx, String distributorId) {
        return isValidActor(ctx, distributorId, DISTRIBUTOR);
    }

    public boolean isValidRetailer(Context ctx, String retailerId) {
        return isValidActor(ctx, retailerId, RETAILER);
    }

    public boolean isValidActor(Context ctx, String actorId, String role) {
        if (actorId == null || actorId.isBlank() || !isSupportedActorRole(role)) {
            return false;
        }

        String actorState = ctx.getStub().getStringState(actorKey(role, actorId.trim()));
        if (actorState == null || actorState.isBlank()) {
            return false;
        }
        try {
            JsonObject actor = gson.fromJson(actorState, JsonObject.class);
            return actor != null
                    && actor.has("actorRole")
                    && actor.get("actorRole").isJsonPrimitive()
                    && role.equals(actor.get("actorRole").getAsString())
                    && actor.has("status")
                    && actor.get("status").isJsonPrimitive()
                    && "ACTIVE".equals(actor.get("status").getAsString());
        } catch (JsonParseException | IllegalStateException exception) {
            return false;
        }
    }

    public String requireCaller(Context ctx, String expectedRole) {
        String actorId = ctx.getClientIdentity().getAttributeValue(ACTOR_ID_ATTRIBUTE);
        String actorRole = ctx.getClientIdentity().getAttributeValue(ACTOR_ROLE_ATTRIBUTE);

        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("The caller certificate must contain an actorId attribute");
        }
        if (!isSupportedActorRole(expectedRole) || !expectedRole.equals(actorRole)) {
            throw new IllegalArgumentException("Caller must have the " + expectedRole + " role");
        }
        if (!isValidActor(ctx, actorId, expectedRole)) {
            throw new IllegalArgumentException("Caller is not a registered active " + expectedRole);
        }

        return actorId.trim();
    }

    public void requireActorRegistrationIdentity(Context ctx, String requestedId, String expectedRole) {
        String actorId = ctx.getClientIdentity().getAttributeValue(ACTOR_ID_ATTRIBUTE);
        String actorRole = ctx.getClientIdentity().getAttributeValue(ACTOR_ROLE_ATTRIBUTE);

        if (actorId == null || !requestedId.equals(actorId.trim())) {
            throw new IllegalArgumentException("You may only register the actor ID in your certificate");
        }
        if (!isSupportedActorRole(expectedRole) || !expectedRole.equals(actorRole)) {
            throw new IllegalArgumentException("Caller certificate must have the " + expectedRole + " role");
        }
    }

    public static boolean isSupportedActorRole(String role) {
        return MANUFACTURER.equals(role) || DISTRIBUTOR.equals(role) || RETAILER.equals(role);
    }

    public static String actorKey(String role, String actorId) {
        return "ACTOR:" + role + ":" + actorId;
    }
}
