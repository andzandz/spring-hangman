package com.andycknight.demo;

import com.andycknight.demo.records.CreateRecord;
import com.andycknight.demo.records.HomeRecord;
import com.andycknight.demo.records.PlayRecord;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class HangmanController {

    private final HangmanWordRepository repository;
    public HangmanController(HangmanWordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public HomeRecord home() {
        return new HomeRecord("Hello world. To get started, you'll need to create a word");
    }

    @GetMapping("/create")
    public CreateRecord create(@RequestParam(defaultValue = "example") String word) {
        if(! word.matches("[a-z ]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The word can only be lowercase letters and spaces");
        }

        HangmanWord hangmanWord = new HangmanWord(word);
        repository.save(hangmanWord);

        return new CreateRecord(
                hangmanWord.getWord(),
                hangmanWord.getGameKey(),
                "Now go to: /play?key=" + hangmanWord.getGameKey()
        );
    }

    @GetMapping("/play")
    public PlayRecord play(
            @RequestParam(defaultValue = "") String key,
            @RequestParam(defaultValue = "") String guess,
            @RequestParam(defaultValue = "") String reset
    ) {
        HangmanWord hangmanWord = repository.findByGameKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown key"));

        String message = null;

        if(reset.equals("1")) {
            hangmanWord.reset();
            repository.save(hangmanWord);
        }

        if(guess.isEmpty()) {
            message = "put ?guess=x on the end of the URL above to guess the letter X";
        } else if(guess.length() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The guess must be one letter");
        } else if( ! guess.matches("[a-z]") ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The guess must be a lowercase letter");
        } else {
            hangmanWord.guessLetter(guess.charAt(0));
            repository.save(hangmanWord);
        }

        Integer attemptsLeft = null;
        if(hangmanWord.attemptsLeft() <= 4) {
            attemptsLeft = hangmanWord.attemptsLeft();
        }

        return new PlayRecord(
                hangmanWord.getWordSoFar(),
                hangmanWord.getWordSoFar()
                        .replace("-", "_ ")
                        .replaceAll("([a-z])", "$1 ")
                        .trim(),
                message,
                hangmanWord.getWrongLetters(),
                attemptsLeft,
                null
        );
    }
}
