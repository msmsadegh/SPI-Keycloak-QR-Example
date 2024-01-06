package com.msmsadegh.keycloak.service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomPasswordGenerator {

    Random random = new SecureRandom();

    private Stream<Character> getRandomAlphabets(int count, boolean upperCase) {
        IntStream characters = null;
        if (upperCase) {
            characters = random.ints(count, 65, 90);
        } else {
            characters = random.ints(count, 97, 122);
        }
        return characters.mapToObj(data -> (char) data);
    }

    private Stream<Character> getRandomNumbers(int count) {
        IntStream numbers = random.ints(count, 48, 57);
        return numbers.mapToObj(data -> (char) data);
    }

    private Stream<Character> getRandomSpecialChars(int count) {
        IntStream specialChars = random.ints(count, 33, 45);
        return specialChars.mapToObj(data -> (char) data);
    }

    public String generateSecureRandomPassword() {
        Stream<Character> passwordStream = Stream.concat(getRandomNumbers(2),
                Stream.concat(getRandomSpecialChars(2),
                        Stream.concat(getRandomAlphabets(2, true), getRandomAlphabets(4, false))));
//        List<Character> passwordChars = passwordStream.toList(); this usage is immutable so can not be shuffled!
        List<Character> passwordChars = passwordStream.collect(Collectors.toList());

        /* shuffle password characters */
        Collections.shuffle(passwordChars);
        return passwordChars.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

}