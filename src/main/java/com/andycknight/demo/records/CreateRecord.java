package com.andycknight.demo.records;

public record CreateRecord(
        String word_so_far,
        String key,
        String message,
        String url,
        String qr_url
) { }
