package com.rozplochowski.loyaltypoints.repository;

import com.rozplochowski.loyaltypoints.model.outbox.OutboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxRecord, UUID> {

    @Query(nativeQuery = true,
            value = "select * from outbox_records where status = 'PENDING' order by created_at limit 25 for update skip locked")
    List<OutboxRecord> getUnprocessedRecords();

}
