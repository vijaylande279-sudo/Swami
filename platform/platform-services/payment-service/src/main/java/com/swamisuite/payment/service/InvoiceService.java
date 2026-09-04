package com.swamisuite.payment.service;

import com.swamisuite.payment.domain.Invoice;
import com.swamisuite.payment.repository.InvoiceRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

/** Minimal one-page GST invoice, per doc §7.3/§15.8. PDF rendering is deliberately plain - a template/branding pass is a later polish item. */
@Service
public class InvoiceService {

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice generate(UUID tenantId, UUID subscriptionId, String appKey, long amountPaise, long gstPaise, long totalPaise) {
        String invoiceNumber = "INV-" + Instant.now().atZone(ZoneOffset.UTC).getYear() + "-" + SEQUENCE.getAndIncrement();
        Invoice invoice = new Invoice(tenantId, subscriptionId, invoiceNumber, amountPaise, gstPaise, totalPaise);
        invoice.setPdfBytes(renderPdf(invoiceNumber, appKey, amountPaise, gstPaise, totalPaise, invoice.getIssuedAt()));
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> listForTenant(UUID tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    private byte[] renderPdf(String invoiceNumber, String appKey, long amountPaise, long gstPaise, long totalPaise, Instant issuedAt) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font font = PDType1Font.HELVETICA;
                PDType1Font boldFont = PDType1Font.HELVETICA_BOLD;
                float y = 750;

                content.beginText();
                content.setFont(boldFont, 16);
                content.newLineAtOffset(50, y);
                content.showText("Swami Suite - Tax Invoice");
                content.endText();
                y -= 40;

                for (String line : List.of(
                        "Invoice Number: " + invoiceNumber,
                        "Issued: " + DateTimeFormatter.ISO_INSTANT.format(issuedAt),
                        "App: " + appKey,
                        "Amount: Rs " + toRupees(amountPaise),
                        "GST (18%): Rs " + toRupees(gstPaise),
                        "Total: Rs " + toRupees(totalPaise)
                )) {
                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();
                    y -= 20;
                }
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render invoice PDF", e);
        }
    }

    private String toRupees(long paise) {
        return String.format("%,.2f", paise / 100.0);
    }
}
