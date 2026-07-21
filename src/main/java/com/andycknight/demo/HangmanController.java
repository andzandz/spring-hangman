package com.andycknight.demo;

import com.andycknight.demo.records.CreateRecord;
import com.andycknight.demo.records.HomeRecord;
import com.andycknight.demo.records.PlayRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Optional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

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
    public ResponseEntity<?> create(@RequestParam(defaultValue = "example") String word) throws Exception {
        if(! word.matches("[a-z ]+")) {
            return ResponseEntity.badRequest()
                    .body( Map.of("error", "The word can only be lowercase letters and spaces") );
        }

        HangmanWord hangmanWord = new HangmanWord(word);
        repository.save(hangmanWord);

        String playUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/play")
                .queryParam("key", hangmanWord.getGameKey())
                .queryParam("guess", "")
                .toUriString();

        String qrUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/play-qr")
                .queryParam("key", hangmanWord.getGameKey())
                .toUriString();

        return ResponseEntity.ok(
                new CreateRecord(
                    hangmanWord.getWordSoFar(),
                    hangmanWord.getGameKey(),
                    "Now go to: /play?key=" + hangmanWord.getGameKey(),
                    playUrl,
                    qrUrl
            )
        );
    }

    @GetMapping(value = "/play-qr", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> playQr(@RequestParam(defaultValue = "") String key) throws Exception {
        if (key.isEmpty()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "key is required"));
        }

        String playUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/play")
                .queryParam("key", key)
                .queryParam("guess", "")
                .toUriString();

        BitMatrix matrix = new QRCodeWriter()
                .encode(playUrl, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(out.toByteArray());
    }

    @GetMapping("/play")
    public ResponseEntity<?> play(
            @RequestParam(defaultValue = "") String key,
            @RequestParam(defaultValue = "") String guess,
            @RequestParam(defaultValue = "") String reset
    ) {
        Optional<HangmanWord> found = repository.findByGameKey(key);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "key not found"));
        }
        HangmanWord hangmanWord = found.get();

        String message = null;

        if(reset.equals("1")) {
            hangmanWord.reset();
            repository.save(hangmanWord);
        }

        if(guess.isEmpty()) {
            message = "put &guess=x on the end of the URL above to guess the letter X";
        } else if(guess.length() > 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "The guess must be one letter"));
        } else if( ! guess.matches("[a-z]") ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "The guess must be a lowercase letter"));
        } else {
            hangmanWord.guessLetter(guess.charAt(0));
            repository.save(hangmanWord);
        }

        Integer attemptsLeft = null;
        if(hangmanWord.attemptsLeft() <= 4) {
            attemptsLeft = hangmanWord.attemptsLeft();
        }

        return ResponseEntity.ok(
                new PlayRecord(
                hangmanWord.getWordSoFar(),
                hangmanWord.getWordSoFar()
                        .replace("-", "_ ")
                        .replaceAll("([a-z])", "$1 ")
                        .trim(),
                message,
                hangmanWord.getWrongLetters(),
                attemptsLeft,
                null
            )
        );
    }
}
