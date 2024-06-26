package com.saleservice.exception;

public class CampaignAlreadyExistException extends RuntimeException {
    public CampaignAlreadyExistException(String message){ super (message); }
}
