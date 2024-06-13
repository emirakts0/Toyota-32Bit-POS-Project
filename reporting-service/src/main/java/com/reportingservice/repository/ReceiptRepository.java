package com.reportingservice.repository;

import com.reportingservice.model.Receipt;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends CrudRepository<Receipt, String> {
}
