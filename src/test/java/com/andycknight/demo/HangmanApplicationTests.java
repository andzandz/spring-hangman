package com.andycknight.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void createPersistsWordCorrectly() throws Exception {
        mockMvc.perform(get("/create").param("word", "foobar"))
                .andExpect(status().isOk());

        assertThat(repository.findAll())
                .extracting(HangmanWord::getWord)
                .contains("foobar");
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
        ).andExpect(status().isOk());

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
        ).andExpect(status().isOk());

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

        mockMvc.perform(
                get("/play")
                        .param("key", hangmanWord.getGameKey())
                        .param("guess", "f")
        ).andExpect(status().isOk());

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
        ).andExpect(status().isOk());

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
