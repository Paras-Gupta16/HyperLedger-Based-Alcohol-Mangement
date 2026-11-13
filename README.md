# Hyperledger Fabric Alcohol Supply Chain Chaincode

This Java chaincode tracks batches from manufacturer to distributor to retailer.
It stores actor registrations, batch ownership, pending transfers, and transfer
history in Fabric world state.

## Fabric identity requirements

Each actor's enrolled Fabric certificate must contain these attributes:

- `actorId`: the actor's unique ID
- `actorRole`: exactly `MANUFACTURER`, `DISTRIBUTOR`, or `RETAILER`

The Fabric CA/enrollment authority must issue these attributes; transaction
arguments cannot choose or override the caller's identity. An actor can register
only the ID and role in its own certificate. Actor IDs are globally unique.
Actors registered under the old, inconsistent key prefixes must register again
under this contract; existing batches stored under their original IDs remain
readable and can be transferred.

## Transaction flow

1. Register actors with `RegisterContract:registerManufacturer`,
   `registerDistributor`, or `registerRetailer`, passing the certificate's
   `actorId` and the actor's name.
2. A registered manufacturer calls `AlcoholContract:createBatch` with JSON
   containing `batchId`, `productName`, `manufacturerId`, ISO dates
   (`productionDate`, `expiryDate`), positive `quantity`, and `unit`.
   The contract sets the owner and initial status itself.
3. The current owner calls `initiateTransfer` with JSON containing
   `transferId`, `batchId`, `toOwnerId`, and optionally `remarks`. The chain
   permits manufacturer-to-distributor and distributor-to-retailer transfers.
   The batch remains with its sender while the transfer is pending.
4. Only the intended receiver can call `acceptTransfer(transferId)` or
   `rejectTransfer(transferId)`. Acceptance changes ownership; rejection
   restores the previous batch status.
5. Use `readBatch`, `readTransfer`, `getBatchTransfers`, and `readActor` to
   query ledger state. Batch and transfer queries are public ledger reads.

The contract uses statuses `MANUFACTURED`, `TRANSFER_PENDING`,
`WITH_DISTRIBUTOR`, and `AT_RETAILER`; transfers use `PENDING`, `ACCEPTED`, and
`REJECTED`.
