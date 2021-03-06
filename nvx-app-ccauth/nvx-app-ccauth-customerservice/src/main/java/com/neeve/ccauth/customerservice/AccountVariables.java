package com.neeve.ccauth.customerservice;

import java.util.Random;

final public class AccountVariables {
    String ActNumber;
    String CustomerId;
    String CustomerVar1String;
    String CustomerVar2String;
    String CustomerVar3String;
    String CustomerVar4String;
    String CustomerVar5String;
    String CustomerVar6String;
    String CustomerVar7String;
    String CustomerVar8String;
    String CustomerVar9String;
    String CustomerVar10String;
    String CustomerVar11String;
    String CustomerVar12String;
    int CustomerVar1Int;
    int CustomerVar2Int;
    int CustomerVar3Int;
    int CustomerVar4Int;
    int CustomerVar5Int;
    int CustomerVar6Int;
    int CustomerVar7Int;
    int CustomerVar8Int;
    long CustomerVar1Long;
    long CustomerVar2Long;
    long CustomerVar3Long;
    long CustomerVar4Long;
    long CustomerVar5Long;
    long CustomerVar6Long;
    long CustomerVar7Long;
    long CustomerVar8Long;
    double CustomerVar1double;
    double CustomerVar2double;
    double CustomerVar3double;
    double CustomerVar4double;
    double CustomerVar5double;
    double CustomerVar6double;
    double CustomerVar7double;
    double CustomerVar8double;
    final private Random rand = new Random(System.currentTimeMillis());

    final AccountVariables init(final String CustomerId, final String cm15) {
        this.CustomerId = CustomerId;
        this.ActNumber = cm15;
        this.CustomerVar1String = Application.randomString(rand.nextInt(5) + 1);
        this.CustomerVar2String = Application.randomString(rand.nextInt(6) + 1);
        this.CustomerVar3String = Application.randomString(rand.nextInt(7) + 1);
        this.CustomerVar4String = Application.randomString(rand.nextInt(8) + 1);
        this.CustomerVar5String = Application.randomString(rand.nextInt(9) + 1);
        this.CustomerVar6String = Application.randomString(rand.nextInt(10) + 1);
        this.CustomerVar7String = Application.randomString(rand.nextInt(11) + 1);
        this.CustomerVar8String = Application.randomString(rand.nextInt(12) + 1);
        this.CustomerVar9String = Application.randomString(rand.nextInt(12) + 1);
        this.CustomerVar10String = Application.randomString(rand.nextInt(12) + 1);
        this.CustomerVar11String = Application.randomString(rand.nextInt(12) + 1);
        this.CustomerVar12String = Application.randomString(rand.nextInt(12) + 1);
        this.CustomerVar1Int = rand.nextInt(1000000) + 1;
        this.CustomerVar2Int = rand.nextInt(1000000) + 1;
        this.CustomerVar3Int = rand.nextInt(1000000) + 1;
        this.CustomerVar4Int = rand.nextInt(1000000) + 1;
        this.CustomerVar5Int = rand.nextInt(1000000) + 1;
        this.CustomerVar6Int = rand.nextInt(1000000) + 1;
        this.CustomerVar7Int = rand.nextInt(1000000) + 1;
        this.CustomerVar8Int = rand.nextInt(1000000) + 1;
        this.CustomerVar1Long = rand.nextLong() + 1;
        this.CustomerVar2Long = rand.nextLong() + 1;
        this.CustomerVar3Long = rand.nextLong() + 1;
        this.CustomerVar4Long = rand.nextLong() + 1;
        this.CustomerVar5Long = rand.nextLong() + 1;
        this.CustomerVar6Long = rand.nextLong() + 1;
        this.CustomerVar7Long = rand.nextLong() + 1;
        this.CustomerVar8Long = rand.nextLong() + 1;
        this.CustomerVar1double = rand.nextDouble() + 1;
        this.CustomerVar2double = rand.nextDouble() + 1;
        this.CustomerVar3double = rand.nextDouble() + 1;
        this.CustomerVar4double = rand.nextDouble() + 1;
        this.CustomerVar5double = rand.nextDouble();
        this.CustomerVar6double = rand.nextDouble();
        this.CustomerVar7double = rand.nextDouble();
        this.CustomerVar8double = rand.nextDouble();
        return this;
    }
}
