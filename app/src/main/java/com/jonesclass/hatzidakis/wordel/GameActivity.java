package com.jonesclass.hatzidakis.wordel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import pl.droidsonroids.gif.GifImageView;

public class GameActivity extends AppCompatActivity {
    /*
    TO-DO:
    -no duplicate letters
    -validate guesses
     */
    StringBuilder stringBuilder = new StringBuilder();
    GifImageView confettiGifImageView;
    TextView messageTextView;
    private static final String TAG = "GameActivityTag";

    ArrayList<String> possibleWords = new ArrayList<>();
    ArrayList<String> validGuesses = new ArrayList<>();
    String[] letterArray = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    EditText[][] boxes = new EditText[6][5];
    Button[] letters = new Button[26];

    // game state variables
    int round = 0;
    boolean gameOver = false;
    String word;

    // colors
    Color keyboardColor = Color.valueOf(Color.argb(255, 181, 159, 218));
    Color blankColor = Color.valueOf(Color.argb(255, 221, 207, 235));
    Color disabledColor = Color.valueOf(Color.argb(255, 148, 139, 153));
    Color incorrectColor = Color.valueOf(Color.argb(255, 82, 77, 87));
    Color partialCorrectColor = Color.valueOf(Color.argb(255, 246, 220, 143));
    Color correctColor = Color.valueOf(Color.argb(255, 155, 241, 136));
    Color invalidActionColor = Color.valueOf(Color.argb(255, 246, 79, 79));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setNavigationBarColor(Color.argb(255, 181, 159, 218));

        confettiGifImageView = findViewById(R.id.gifImageView_confetti);

        messageTextView = findViewById(R.id.textView_message);

        // BUTTONS
        Button submitButton = findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitGuess(round);
            }
        });

        Button newGameButton = findViewById(R.id.button_newGame);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });

        Button clearButton = findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver) {
                    clearRow(round);
                }
            }
        });

        // BOXES
        boxes[0][0] = findViewById(R.id.editText_00);
        boxes[0][1] = findViewById(R.id.editText_01);
        boxes[0][2] = findViewById(R.id.editText_02);
        boxes[0][3] = findViewById(R.id.editText_03);
        boxes[0][4] = findViewById(R.id.editText_04);

        boxes[1][0] = findViewById(R.id.editText_10);
        boxes[1][1] = findViewById(R.id.editText_11);
        boxes[1][2] = findViewById(R.id.editText_12);
        boxes[1][3] = findViewById(R.id.editText_13);
        boxes[1][4] = findViewById(R.id.editText_14);

        boxes[2][0] = findViewById(R.id.editText_20);
        boxes[2][1] = findViewById(R.id.editText_21);
        boxes[2][2] = findViewById(R.id.editText_22);
        boxes[2][3] = findViewById(R.id.editText_23);
        boxes[2][4] = findViewById(R.id.editText_24);

        boxes[3][0] = findViewById(R.id.editText_30);
        boxes[3][1] = findViewById(R.id.editText_31);
        boxes[3][2] = findViewById(R.id.editText_32);
        boxes[3][3] = findViewById(R.id.editText_33);
        boxes[3][4] = findViewById(R.id.editText_34);

        boxes[4][0] = findViewById(R.id.editText_40);
        boxes[4][1] = findViewById(R.id.editText_41);
        boxes[4][2] = findViewById(R.id.editText_42);
        boxes[4][3] = findViewById(R.id.editText_43);
        boxes[4][4] = findViewById(R.id.editText_44);

        boxes[5][0] = findViewById(R.id.editText_50);
        boxes[5][1] = findViewById(R.id.editText_51);
        boxes[5][2] = findViewById(R.id.editText_52);
        boxes[5][3] = findViewById(R.id.editText_53);
        boxes[5][4] = findViewById(R.id.editText_54);

        for (int row = 0; row < boxes.length; row++) {
            for (int column = 0; column < boxes[0].length; column++) {
                boxes[row][column].setText("");
                boxes[row][column].setEnabled(false);
            }
        }

        // ANDROID KEYBOARD
//        for (int row = 0; row < boxes.length; row++) {
//            for (int column = 0; column < boxes[0].length; column++) {
//                advanceCursorListener(row, column);
//                boxes[row][column].setFilters(new InputFilter[] {
//                        new InputFilter.AllCaps()
//                });
//            }
//        }

        // WORDEL KEYBOARD
        // KEYBOARD BUTTONS
        Button backspaceButton = findViewById(R.id.button_backspace);
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver) {
                    backspace();
                }
            }
        });

        letters[0] = findViewById(R.id.button_a);
        letters[1] = findViewById(R.id.button_b);
        letters[2] = findViewById(R.id.button_c);
        letters[3] = findViewById(R.id.button_d);
        letters[4] = findViewById(R.id.button_e);
        letters[5] = findViewById(R.id.button_f);
        letters[6] = findViewById(R.id.button_g);
        letters[7] = findViewById(R.id.button_h);
        letters[8] = findViewById(R.id.button_i);
        letters[9] = findViewById(R.id.button_j);
        letters[10] = findViewById(R.id.button_k);
        letters[11] = findViewById(R.id.button_l);
        letters[12] = findViewById(R.id.button_m);
        letters[13] = findViewById(R.id.button_n);
        letters[14] = findViewById(R.id.button_o);
        letters[15] = findViewById(R.id.button_p);
        letters[16] = findViewById(R.id.button_q);
        letters[17] = findViewById(R.id.button_r);
        letters[18] = findViewById(R.id.button_s);
        letters[19] = findViewById(R.id.button_t);
        letters[20] = findViewById(R.id.button_u);
        letters[21] = findViewById(R.id.button_v);
        letters[22] = findViewById(R.id.button_w);
        letters[23] = findViewById(R.id.button_x);
        letters[24] = findViewById(R.id.button_y);
        letters[25] = findViewById(R.id.button_z);

        for (Button letter : letters) {
            letter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.button_submit).setBackgroundColor(keyboardColor.toArgb());
                    for (int column = 0; column < boxes[0].length; column++) {
                       if (boxes[round][column].getText().toString().isBlank()) {
                           boxes[round][column].setText(letter.getText().toString());
                           Log.d(TAG, letter.getText().toString());
                           return;
                       }
                    }
                }
            });
        }

        readPossibleGuesses();
        readPossibleAnswers();

        newGame();

    }

    public void readPossibleGuesses() {
        String fileContents = "";
        InputStream inputStream = getResources().openRawResource(R.raw.possible_guesses);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while ((fileContents = reader.readLine()) != null) {
                validGuesses.add(fileContents);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readPossibleAnswers() {
        String fileContents = "";
        InputStream inputStream = getResources().openRawResource(R.raw.possible_answers);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while ((fileContents = reader.readLine()) != null) {
                possibleWords.add(fileContents);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidGuess(String guess) {
        if (validGuesses.contains(guess)) {
            return true;
        }
        return false;
    }

    public void invalidGuess() {
        var submitButton = findViewById(R.id.button_submit);
        submitButton.setBackgroundColor(invalidActionColor.toArgb());
    }

    public int getAlphabetPosition(String letter) {
        for (int i = 0; i < letterArray.length; i++) {
            if (letter.equalsIgnoreCase(letterArray[i])) {
                return i;
            }
        }
        return  -1;
    }

    public void newGame() {
        findViewById(R.id.button_submit).setBackgroundColor(keyboardColor.toArgb());
        gameOver = false;
        round = 0;
        confettiGifImageView.setVisibility(View.INVISIBLE);
        messageTextView.setVisibility(View.INVISIBLE);
        word = possibleWords.get(new Random().nextInt(possibleWords.size()));
        Log.d(TAG, "Word: " + word);
        disableInput();
        clearBoxes();
        enableRow(0);
    }

    public void clearBoxes() {
        for (int row = 0; row < boxes.length; row++) {
            for (int column = 0; column < boxes[0].length; column++) {
                boxes[row][column].setText("");
            }
        }
        for (Button letter: letters) {
            letter.setBackgroundColor(keyboardColor.toArgb());
        }
    }

    public void disableInput() {
        for (int row = 0; row < boxes.length; row++) {
            for (int column = 0; column < boxes[0].length; column++) {
                boxes[row][column].setEnabled(false);
                boxes[row][column].setBackgroundColor(disabledColor.toArgb());
            }
        }
    }

    public void enableRow(int row) {
        for (int column = 0; column < boxes[0].length; column++) {
//            boxes[row][column].setEnabled(true);
            boxes[row][column].setBackgroundColor(blankColor.toArgb());
        }
    }

    public void disableRow(int row) {
        for (int column = 0; column < boxes[0].length; column++) {
            boxes[row][column].setEnabled(false);
        }
    }

    public void clearRow(int row) {
        findViewById(R.id.button_submit).setBackgroundColor(keyboardColor.toArgb());
        for (int column = 0; column < boxes[0].length; column++) {
            boxes[row][column].setText("");
        }
    }

    public void submitGuess(int row) {
        String guess = "";
        var submitButton = findViewById(R.id.button_submit);

        submitButton.setBackgroundColor(keyboardColor.toArgb());

        for (int column = 0; column < boxes[0].length; column++) {
            if (!boxes[row][column].getText().toString().isBlank()) {
                guess += boxes[row][column].getText().toString().toLowerCase();
            } else {
                invalidGuess();
                return;
            }

        }

        if (!isValidGuess(guess)) {
            invalidGuess();
            return;
        }

        // CORRECT GUESS
        if (guess.equalsIgnoreCase(word)) {
            for (int column = 0; column < boxes[0].length; column++) {
                boxes[row][column].setBackgroundColor(correctColor.toArgb());
                letters[getAlphabetPosition(boxes[row][column].getText().toString())].setBackgroundColor(correctColor.toArgb());
            }
            winGame();
            return;
        }

        for (int column = 0; column < boxes[0].length; column++) {
            if (boxes[row][column].getText().toString().equalsIgnoreCase(String.valueOf(word.charAt(column)))) {
                // CORRECT LETTER
                boxes[row][column].setBackgroundColor(correctColor.toArgb());
                letters[getAlphabetPosition(boxes[row][column].getText().toString())].setBackgroundColor(correctColor.toArgb());
            } else {
                // INCORRECT LETTER
                boxes[row][column].setBackgroundColor(incorrectColor.toArgb());
                letters[getAlphabetPosition(boxes[row][column].getText().toString())].setBackgroundColor(incorrectColor.toArgb());


                for (int i = 0; i < word.length(); i++) {
                    if (boxes[row][column].getText().toString().equalsIgnoreCase(String.valueOf(word.charAt(i)))) {
                        // PARTIAL CORRECT LETTER
                        boxes[row][column].setBackgroundColor(partialCorrectColor.toArgb());
                        letters[getAlphabetPosition(boxes[row][column].getText().toString())].setBackgroundColor(partialCorrectColor.toArgb());
                        break;
                    }
                }
            }
        }

        disableRow(round);
        if (round < boxes.length - 1) {
            round += 1;
            enableRow(round);
        } else {
            loseGame();
        }
    }

    public void displayMessage(String message) {
        messageTextView.setVisibility(View.VISIBLE);
        messageTextView.setText(message);
    }

    public void winGame() {
        Log.d(TAG, "yay!");
        gameOver = true;
        confettiGifImageView.setVisibility(View.VISIBLE);
        displayMessage("You win!\nClick \"NEW GAME\" to play again");
    }

    public void loseGame() {
        Log.d(TAG, "boo!");
        gameOver = true;
        displayMessage("You lose!\nThe word was \"" + word + "\"\nClick \"NEW GAME\" to try again");
    }

    // KEYBOARD
    public void backspace() {
        findViewById(R.id.button_submit).setBackgroundColor(keyboardColor.toArgb());
        for (int column = boxes[0].length - 1; column >= 0; column--) {
            if (boxes[round][column].getText().length() != 0) {
                boxes[round][column].setText("");
                boxes[round][column].requestFocus();
                return;
            }
        }
    }

    public void advanceCursorListener(int row, int column) {
        boxes[row][column].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (stringBuilder.length() == 1) {
                    stringBuilder.deleteCharAt(0);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (stringBuilder.length() == 0 && boxes[row][column].length() == 1) {
                    stringBuilder.append(charSequence);
                    if (row == boxes.length - 1 && column == boxes[0].length - 1) {
                        boxes[row][column].clearFocus();
                        return;
                    }
                    if (column < boxes[0].length - 1) {
                        boxes[row][column].clearFocus();
                        boxes[row][column + 1].requestFocus();
                        boxes[row][column + 1].selectAll();
                        boxes[row][column + 1].setCursorVisible(true);
                    } else {
                        boxes[row][column].clearFocus();
                        boxes[row][0].requestFocus();
                        boxes[row][0].selectAll();
                        boxes[row][0].setCursorVisible(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (stringBuilder.length() == 0) {
                    boxes[row][column].requestFocus();
                    boxes[row][column].selectAll();
                }
            }
        });
    }

}