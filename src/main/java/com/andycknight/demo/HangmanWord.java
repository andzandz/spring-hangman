package com.andycknight.demo;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.security.SecureRandom;

@Entity
public class HangmanWord
{
    private @Id
    @GeneratedValue Long id;

    private String gameKey;
    private String word;
    private String wordSoFar;
    private String wrongLetters;

    public HangmanWord(String word)
    {
        this.gameKey = this.generateRandomString(6);
        this.word = word;
        this.wordSoFar = word.replaceAll("[a-z]", "-");
        this.wrongLetters = "";
    }

    private String generateRandomString(int length)
    {
        String chars = "abcdefghijklmnopqrstuvwxyz";

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(new SecureRandom().nextInt(chars.length())));
        }
        return sb.toString();
    }

    public HangmanWord() {}

    public Long getId()
    {
        return this.id;
    }

    public String getGameKey()
    {
        return gameKey;
    }

    public String getWord()
    {
        return word;
    }

    public String getWordSoFar()
    {
        return wordSoFar;
    }

    public String getWrongLetters()
    {
        return wrongLetters;
    }

    public void setWrongLetters(String wrongLetters)
    {
        this.wrongLetters = wrongLetters;
    }

    public void guessLetter(char letter)
    {
        if(this.attemptsLeft() == 0) {
            return;
        }

        if(this.word.contains(Character.toString(letter))) {
            for (int i = 0; i < this.word.length(); i++){
                if( this.word.charAt(i) == letter ) {
                    StringBuilder sb = new StringBuilder(this.wordSoFar);
                    sb.setCharAt(i, letter);
                    this.wordSoFar = sb.toString();
                }
            }
        } else {
            if( ! this.wrongLetters.contains(Character.toString(letter))) {
                this.wrongLetters = this.wrongLetters + letter;
            }
        }
    }

    public int attemptsLeft()
    {
        return 8 - this.wrongLetters.length();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof HangmanWord))
            return false;
        HangmanWord hangmanWord = (HangmanWord) o;
        return Objects.equals(this.id, hangmanWord.id)
                && Objects.equals(this.gameKey, hangmanWord.gameKey)
                && Objects.equals(this.word, hangmanWord.word)
                && Objects.equals(this.wordSoFar, hangmanWord.wordSoFar)
                && Objects.equals(this.wrongLetters, hangmanWord.wrongLetters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.gameKey, this.word, this.wordSoFar, this.wrongLetters);
    }

    @Override
    public String toString() {
        return "HangmanWord{"
                + "id=" + this.id
                + ", gameKey='" + this.gameKey + '\''
                + ", word='" + this.word + '\''
                + ", wordSoFar='" + this.wordSoFar + '\''
                + ", wrongLetters='" + this.wrongLetters + '\''
                + '}';
    }
}
