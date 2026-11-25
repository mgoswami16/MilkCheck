package com.milkcheck.controller;

import com.milkcheck.dto.*;
import com.milkcheck.entity.BulkEnquiry;
import com.milkcheck.enums.EnquiryStatus;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.BulkEnquiryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class BulkEnquiryController {

    private final BulkEnquiryRepository enquiryRepository;

    @PostMapping("/bulk-enquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public BulkEnquiryResponse submit(@Valid @RequestBody BulkEnquiryRequest req) {
        BulkEnquiry e = new BulkEnquiry();
        e.setRefNumber("BLK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        e.setFullName(req.fullName().trim());
        e.setBusinessName(req.businessName().trim());
        e.setEmail(req.email().trim().toLowerCase());
        e.setPhone(req.phone().trim());
        e.setBusinessType(req.businessType());
        e.setCity(req.city().trim());
        e.setProducts((req.products() != null && !req.products().isEmpty() ? String.join(", ", req.products()) : "General Dairy"));
        e.setEstimatedQuantity(req.estimatedQuantity().trim());
        e.setFrequency(req.frequency());
        e.setDeliveryAddress(req.deliveryAddress() != null ? req.deliveryAddress().trim() : null);
        e.setMessage(req.message().trim());
        e.setStatus(EnquiryStatus.NEW);

        return BulkEnquiryResponse.from(enquiryRepository.save(e));
    }

    @GetMapping("/admin/enquiries")
    public PageResponse<BulkEnquiryResponse> listForAdmin(
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = enquiryRepository
                .search(status, keyword, PageRequest.of(page, size))
                .map(BulkEnquiryResponse::from);
        return PageResponse.from(result);
    }

    @PatchMapping("/admin/enquiries/{id}/status")
    public BulkEnquiryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnquiryStatusRequest req) {
        BulkEnquiry e = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
        e.setStatus(req.status());
        return BulkEnquiryResponse.from(enquiryRepository.save(e));
    }
}