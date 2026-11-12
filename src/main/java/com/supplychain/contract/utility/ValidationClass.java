package com.supplychain.contract.utility;

import org.hyperledger.fabric.contract.Context;

public class ValidationClass {

    public boolean isValidManufacturer(Context ctx, String manufacturerId) {

        if (manufacturerId == null || manufacturerId.isBlank()) {
            return false;
        }

        String manufacturerKey = "Manufacturer" .trim()+ manufacturerId.trim();

        String manufacturerState =
                ctx.getStub().getStringState(manufacturerKey);

        return manufacturerState != null && !manufacturerState.isBlank();
    }

    public boolean isValidDistributor(Context cxt,String distributorId){
        if(distributorId==null||distributorId.isBlank()){
            return false;
        }

        String distributorKey = "Distributor".trim()+distributorId.trim();

        String distributorState = cxt.getStub().getStringState(distributorKey);

        return distributorState!=null && !distributorState.isBlank();
    }

    public boolean isValidRetailer(Context cxt,String retailerId){

        if(retailerId==null||retailerId.isBlank()){
            return false;
        }

        String retailerKey = "Retailer".trim()+retailerId.trim();

        String retailerState = cxt.getStub().getStringState(retailerKey);

        return retailerState!=null && !retailerState.isBlank();
    }
}

