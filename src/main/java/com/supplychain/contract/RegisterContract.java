package com.supplychain.contract;

import java.util.Map;

import com.google.gson.Gson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(name = "RegisterContract")
public class RegisterContract implements ContractInterface {

    private final Gson gson = new Gson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerManufacture(Context cxt,
                                       String manufacturerId,
                                       String manufacturerName) throws Exception {

        if(manufacturerId==null||manufacturerId.isBlank()){
            throw new Exception("Id invalid");
        }
        String manIdTransfer = "Manufacture"+manufacturerId;
        String existing = cxt.getStub().getStringState(manIdTransfer);

        if(existing!=null&&!existing.isEmpty()){
            throw new Exception("Manufacture already register");
        }
        String manufactureJson = gson.toJson(Map.of(
                "manufacturerId", manufacturerId,
                "manufacturerName", manufacturerName,
                "status", "ACTIVE"
        ));

        cxt.getStub().putStringState(manIdTransfer,manufactureJson);
        return "Manufacturer Register successfully";
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerDistributor(Context cxt,
                                      String distributorId,
                                      String distributorName ) throws Exception {

        if(distributorId==null||distributorId.isBlank()){
            throw new Exception("Id is invalid");
        }

    }
}
