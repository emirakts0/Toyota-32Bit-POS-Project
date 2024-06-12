package com.saleservice.exception;


public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException(String message){ super (message); }
}
