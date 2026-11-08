/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

class ScoreKeeper {
    // A ScoreKeeper tracks an alliance's score during a match, so its score
    // needs to be able to change over time. Declare a `private int` field
    // named `score` below (don't initialize it here, the constructors will
    // handle that).
    private int score;

    // A team's score can never drop below zero. Declare a
    // constant named `MIN_SCORE`, equal to `0`, to represent
    // that floor.
    private static final int MIN_SCORE = 0;

    // Write a no-argument constructor `ScoreKeeper()` that starts the score
    // at `MIN_SCORE`. Instead of repeating the constructor logic, use
    // `this(...)` to call the other constructor below, passing `MIN_SCORE`
    // as the argument.
    public ScoreKeeper() {
        this(MIN_SCORE);
    }

    // Write a constructor `ScoreKeeper(int startingScore)` that sets `score`
    // to `startingScore`.
    public ScoreKeeper(int startingScore) {
        this.score = startingScore;
    }

    // Write a method `addPoints(int points)` that adds `points` to `score`,
    // then prints "Score is now: " followed by the new score. This method
    // doesn't need to give anything back to its caller, so its return type
    // should be `void`.
    public void addPoints(int points) {
        this.score = this.score + points;
        System.out.println("Score is now: " + this.score);
    }

    // Write a method `applyPenalty(int points)` that subtracts `points` from
    // `score`, but never lets the score drop below `MIN_SCORE`. Use a local
    // variable named `newScore` to store the result of `score - points`.
    // If `newScore` is less than `MIN_SCORE`, set `score` to `MIN_SCORE`;
    // otherwise, set `score` to `newScore`. Either way, finish by printing
    // "Penalty applied. Score is now: " followed by the new score. This
    // method's return type should also be `void`.
    public void applyPenalty(int points) {
        int newScore = this.score - points;
        if (newScore < MIN_SCORE) {
            this.score = MIN_SCORE;
        } else {
            this.score = newScore;
        }
        System.out.println("Penalty applied. Score is now: " + this.score);
    }

    // Because `score` is private, write a getter method `getScore()` that
    // returns its current value as an `int`.
    public int getScore() {
        return this.score;
    }

    // Write a method `hasWon(int opponentScore)` that returns `true` if this
    // ScoreKeeper's `score` is greater than `opponentScore`, and `false`
    // otherwise.
    public boolean hasWon(int opponentScore) {
        return this.score > opponentScore;
    }

    // Write a method `reset()` that sets `score` back to `MIN_SCORE`,
    // reusing the constant rather than writing `0` directly.
    public void reset() {
        this.score = MIN_SCORE;
    }
}

void main() {
    // Create a ScoreKeeper named `redAlliance` using the no-argument
    // constructor. Its score should start at `0`.
    ScoreKeeper redAlliance = new ScoreKeeper();

    // Call `redAlliance.addPoints(10)`, then `redAlliance.addPoints(46)`.
    // Each call should print the running total, ending with
    // "Score is now: 56".
    redAlliance.addPoints(10);
    redAlliance.addPoints(46);

    // Print `redAlliance`'s current score by calling `getScore()`, e.g.
    // `System.out.println(redAlliance.getScore());`
    System.out.println(redAlliance.getScore());

    // Call `redAlliance.applyPenalty(20)`. Since 56 - 20 equals 36, it is still above
    // or equal to `MIN_SCORE`, this should print "Penalty applied. Score is now: 36".
    redAlliance.applyPenalty(20);

    // Call `redAlliance.applyPenalty(100)`. Since 36 - 100 is below
    // `MIN_SCORE`, the score should be clamped instead of going negative,
    // printing "Penalty applied. Score is now: 0".
    redAlliance.applyPenalty(100);

    // Create a second ScoreKeeper named `blueAlliance`, this time using the
    // `ScoreKeeper(int startingScore)` constructor, passing `30`.
    ScoreKeeper blueAlliance = new ScoreKeeper(30);

    // Print the result of `redAlliance.hasWon(blueAlliance.getScore())`.
    // Since `redAlliance`'s score is currently 0, this should print `false`.
    // After running, change `blueAlliance`'s starting score to `-5`; the code
    // should now print `true` instead (recall `applyPenalty` isn't involved
    // here, so a starting score is allowed to be negative if passed in
    // directly through the constructor).
    System.out.println(redAlliance.hasWon(blueAlliance.getScore()));

    // Call `redAlliance.reset()`, then print `redAlliance.getScore()` again.
    // It should now print `0`, even though `redAlliance` never had any
    // penalties applied after its last penalty above.
    redAlliance.reset();
    System.out.println(redAlliance.getScore());
}
