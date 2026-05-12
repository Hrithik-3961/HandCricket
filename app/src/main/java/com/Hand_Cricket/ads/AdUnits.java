package com.Hand_Cricket.ads;

class AdUnits {
    private final String appOpenId;
    private final String bannerId;
    private final String interstitialId;
    private final String rewardedId;

    public AdUnits(String appOpenId, String bannerId, String interstitialId, String rewardedId) {
        this.appOpenId = appOpenId;
        this.bannerId = bannerId;
        this.interstitialId = interstitialId;
        this.rewardedId = rewardedId;
    }

    public String getAppOpenId() {
        return appOpenId;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getInterstitialId() {
        return interstitialId;
    }

    public String getRewardedId() {
        return rewardedId;
    }
}
