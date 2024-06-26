package com.saleservice.exception;

public class CampaignIsNotActive extends RuntimeException{
    public CampaignIsNotActive(String message){ super (message); }
}
