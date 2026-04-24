package com.expense.tracker.controller;

import com.expense.tracker.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/rates/{base}")
    public ResponseEntity<Map<String, Object>> getRates(
            @PathVariable String base) {
        return ResponseEntity.ok(currencyService.getRates(base));
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam Double amount) {
        return ResponseEntity.ok(currencyService.convert(from, to, amount));
    }
}