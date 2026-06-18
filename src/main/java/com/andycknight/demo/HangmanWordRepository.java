package com.andycknight.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface HangmanWordRepository extends JpaRepository<HangmanWord, Long>
{
    Optional<HangmanWord> findByGameKey(String key);
}