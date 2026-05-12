package com.Hand_Cricket.ads;

import com.Hand_Cricket.BuildConfig;

class AdUnitProvider {
    private static final AdUnits realAdUnits;
    private static final AdUnits testAdUnits;

    static  {
        realAdUnits = new AdUnits(
                "ca-app-pub-5909989608288926/6359617889",
                "ca-app-pub-5909989608288926/3733454545",
                "ca-app-pub-5909989608288926/2420372875",
                "ca-app-pub-5909989608288926/9217715516"
        );

        testAdUnits = new AdUnits(
                "ca-app-pub-3940256099942544/9257395921",
                "ca-app-pub-3940256099942544/6300978111",
                "ca-app-pub-3940256099942544/1033173712",
                "ca-app-pub-3940256099942544/5224354917"
        );
    }

    private AdUnitProvider() {}

    public static AdUnits getAdUnits() {
        return BuildConfig.DEBUG ? testAdUnits : realAdUnits;
    }
}
