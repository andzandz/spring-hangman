package com.andycknight.demo.records;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayRecord(
        String word_so_far,
        String word_so_far_spaced,
        String message,
        String wrong_letters,
        String[] man,
        Integer attempts_left
) { }
