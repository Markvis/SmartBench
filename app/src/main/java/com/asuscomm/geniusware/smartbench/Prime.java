package com.asuscomm.geniusware.smartbench;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markfavis on 8/24/15.
 */
public class Prime {
    List<Integer> primesUpTo(final int target) {
        final boolean[] nonPrime = new boolean[target + 1];

        for (int i = 2; i <= Math.sqrt(target); ++i) {
            if (!nonPrime[i]) {
                for (int j = i * 2; j <= target; j += i) {
                    nonPrime[j] = true;
                }
            }
        }

        final List<Integer> primes = new ArrayList<>();

        for (int i = 2; i <= target; ++i) {
            if (!nonPrime[i]) primes.add(i);
        }

        return primes;
    }

    int totalPrimesIn(int from, int to){
        int counter = 0;
        for(int i = from; i < to; i++){
            if(isPrime(i))
                counter++;
        }
        return counter;
    }

    private boolean isPrime(int number){
        for(int i=2; i<number; i++){
            if(number%i == 0){
                return false;
            }
        }
        return true;
    }
}
