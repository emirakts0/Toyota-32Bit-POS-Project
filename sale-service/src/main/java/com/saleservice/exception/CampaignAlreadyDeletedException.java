package com.saleservice.exception;

public class CampaignAlreadyDeletedException extends RuntimeException {
    public CampaignAlreadyDeletedException(String message){ super (message); }
}
