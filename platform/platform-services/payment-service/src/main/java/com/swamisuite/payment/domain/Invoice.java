package com.swamisuite.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "subscription_id", nullable = false, columnDefinition = "uuid")
    private UUID subscriptionId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "gst_paise", nullable = false)
    private long gstPaise;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    /** Stored directly as bytea for this phase - see pom.xml note on deferring object storage. */
    @Lob
    @Column(name = "pdf_bytes")
    private byte[] pdfBytes;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    public Invoice(UUID tenantId, UUID subscriptionId, String invoiceNumber, long amountPaise, long gstPaise, long totalPaise) {
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.invoiceNumber = invoiceNumber;
        this.amountPaise = amountPaise;
        this.gstPaise = gstPaise;
        this.totalPaise = totalPaise;
    }
}
