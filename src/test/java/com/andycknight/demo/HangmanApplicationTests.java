package com.andycknight.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class HangmanApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HangmanWordRepository repository;

    @Test
    void contextLoads() {
    }

    @Test
    void createRejectsInvalidWord() throws Exception {
        mockMvc.perform(get("/create").param("word", "ABC123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The word can only be lowercase letters and spaces"));
    }

    @Test
    void createPersistsWordCorrectly() throws Exception {
        mockMvc.perform(get("/create")
                        .param("word", "foobar"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.word_so_far").value("------"));

        assertThat(repository.findAll())
                .extracting(HangmanWord::getWord)
                .contains("foobar");
    }

    @Test
    void playRejectsInvalidKey() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                get("/play")
                        .param("key", "invalidkey")
                        .param("guess", "b")
        ).andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("key not found"));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playRejectsMultipleLetterGuess() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                        get("/play")
                                .param("key", hangmanWord.getGameKey())
                                .param("guess", "ab")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The guess must be one letter"));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playRejectsUppercaseLetterGuess() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                        get("/play")
                                .param("key", hangmanWord.getGameKey())
                                .param("guess", "A")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The guess must be a lowercase letter"));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playGuessesLetterCorrectlyWhenLetterIsInTheWord() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                get("/play")
                        .param("key", hangmanWord.getGameKey())
                        .param("guess", "b")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.word_so_far").value("--- b--"))
                .andExpect(jsonPath("$.word_so_far_spaced").value("_ _ _  b _ _"))
                .andExpect(jsonPath("$.wrong_letters").value(""));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- b--");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playGuessesLetterCorrectlyWhenLetterIsNotInTheWord() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                get("/play")
                        .param("key", hangmanWord.getGameKey())
                        .param("guess", "x")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.word_so_far").value("--- ---"))
                .andExpect(jsonPath("$.word_so_far_spaced").value("_ _ _  _ _ _"))
                .andExpect(jsonPath("$.wrong_letters").value("x"));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("x");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playDoesNotGuessLetterWhenAlreadyHadTooManyGuesses() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        hangmanWord.guessLetter('x');
        hangmanWord.guessLetter('y');
        hangmanWord.guessLetter('c');
        hangmanWord.guessLetter('d');
        hangmanWord.guessLetter('e');
        hangmanWord.guessLetter('z');
        hangmanWord.guessLetter('g');
        hangmanWord.guessLetter('h');
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        String[] expectedMan = {
                " ____     ",
                "|    |    ",
                "|    o    ",
                "|   ~|~   ",
                "|    |    ",
                "|   /\\    ",
                "|         ",
                "=========="
        };

        mockMvc.perform(
                get("/play")
                        .param("key", hangmanWord.getGameKey())
                        .param("guess", "f")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.word_so_far").value("--- ---"))
                .andExpect(jsonPath("$.word_so_far_spaced").value("_ _ _  _ _ _"))
                .andExpect(jsonPath("$.wrong_letters").value("xycdezgh"))
                .andExpect(jsonPath("$.man[0]").value(expectedMan[0]))
                .andExpect(jsonPath("$.man[1]").value(expectedMan[1]))
                .andExpect(jsonPath("$.man[2]").value(expectedMan[2]))
                .andExpect(jsonPath("$.man[3]").value(expectedMan[3]))
                .andExpect(jsonPath("$.man[4]").value(expectedMan[4]))
                .andExpect(jsonPath("$.man[5]").value(expectedMan[5]))
                .andExpect(jsonPath("$.man[6]").value(expectedMan[6]))
                .andExpect(jsonPath("$.man[7]").value(expectedMan[7]));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("xycdezgh");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }

    @Test
    void playWithReset1ResetsTheGuessesSoFar() throws Exception {
        HangmanWord hangmanWord = new HangmanWord("foo bar");
        hangmanWord.guessLetter('a');
        hangmanWord.guessLetter('b');
        hangmanWord.guessLetter('c');
        hangmanWord.guessLetter('d');
        repository.save(hangmanWord);

        HangmanWord anotherHangmanWord = new HangmanWord("another word");
        repository.save(anotherHangmanWord);

        mockMvc.perform(
                get("/play")
                        .param("key", hangmanWord.getGameKey())
                        .param("reset", "1")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.word_so_far").value("--- ---"))
                .andExpect(jsonPath("$.word_so_far_spaced").value("_ _ _  _ _ _"))
                .andExpect(jsonPath("$.wrong_letters").value(""));

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("--- ---");

        assertThat(repository.findByGameKey(hangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWordSoFar)
                .isEqualTo("------- ----");

        assertThat(repository.findByGameKey(anotherHangmanWord.getGameKey()))
                .get()
                .extracting(HangmanWord::getWrongLetters)
                .isEqualTo("");
    }
}
