package com.example.wordle

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class MainActivity : AppCompatActivity() {

    private var wordToGuess = ""
    private var guessCount = 0
    private var streak = 0
    private var isGameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val natureSwitch = findViewById<SwitchCompat>(R.id.sw_nature)
        wordToGuess = FourLetterWordList.getRandomFourLetterWord(natureSwitch.isChecked)

        val guessEditText = findViewById<EditText>(R.id.et_guess)
        val resetButton = findViewById<Button>(R.id.btn_reset)
        val targetWordTextView = findViewById<TextView>(R.id.targetWord)
        val starImageView = findViewById<ImageView>(R.id.iv_star)
        val streakTextView = findViewById<TextView>(R.id.tv_streak)
        val rootView = findViewById<ConstraintLayout>(R.id.main)

        val guess1Value = findViewById<TextView>(R.id.guess1Value)
        val guess1CheckValue = findViewById<TextView>(R.id.guess1CheckValue)
        val guess2Value = findViewById<TextView>(R.id.guess2Value)
        val guess2CheckValue = findViewById<TextView>(R.id.guess2CheckValue)
        val guess3Value = findViewById<TextView>(R.id.guess3Value)
        val guess3CheckValue = findViewById<TextView>(R.id.guess3CheckValue)

        natureSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Only allow changing theme if no guesses have been made
            if (guessCount == 0 && !isGameOver) {
                wordToGuess = FourLetterWordList.getRandomFourLetterWord(isChecked)
            }
        }

        fun submitGuess() {
            if (isGameOver || guessCount >= 3) return

            val guess = guessEditText.text.toString().uppercase().trim()
            
            if (guess.length != 4) {
                Toast.makeText(this, "Please enter exactly 4 letters", Toast.LENGTH_SHORT).show()
                return
            }

            if (!guess.all { it.isLetter() }) {
                Toast.makeText(this, "Please enter only alphabetical characters (A-Z)", Toast.LENGTH_SHORT).show()
                return
            }

            // Disable theme switching once a guess is made
            natureSwitch.isEnabled = false

            guessCount++
            val coloredCheck = getColoredCheck(guess)

            when (guessCount) {
                1 -> {
                    guess1Value.text = guess
                    guess1CheckValue.text = coloredCheck
                }
                2 -> {
                    guess2Value.text = guess
                    guess2CheckValue.text = coloredCheck
                }
                3 -> {
                    guess3Value.text = guess
                    guess3CheckValue.text = coloredCheck
                }
            }

            if (guess == wordToGuess) {
                isGameOver = true
                streak++
                streakTextView.text = "Streak: $streak"
                showWinUI(guessEditText, targetWordTextView, starImageView, rootView)
            } else if (guessCount == 3) {
                isGameOver = true
                streak = 0
                streakTextView.text = "Streak: $streak"
                showLossUI(guessEditText, targetWordTextView)
            }
            
            guessEditText.text.clear()
        }

        guessEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                submitGuess()
                true
            } else {
                false
            }
        }

        resetButton.setOnClickListener {
            // Reset game state
            guessCount = 0
            isGameOver = false
            natureSwitch.isEnabled = true
            wordToGuess = FourLetterWordList.getRandomFourLetterWord(natureSwitch.isChecked)
            
            // Clear UI
            guess1Value.text = ""
            guess1CheckValue.text = ""
            guess2Value.text = ""
            guess2CheckValue.text = ""
            guess3Value.text = ""
            guess3CheckValue.text = ""
            guessEditText.text.clear()
            targetWordTextView.text = ""
            targetWordTextView.visibility = View.GONE
            starImageView.visibility = View.GONE
            
            // Re-enable inputs
            guessEditText.isEnabled = true
            
            Toast.makeText(this, "Game Restarted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWinUI(
        et: EditText, 
        targetTv: TextView, 
        starIv: ImageView,
        root: ConstraintLayout
    ) {
        targetTv.text = wordToGuess
        targetTv.visibility = View.VISIBLE
        et.isEnabled = false
        
        starIv.visibility = View.VISIBLE
        starIv.alpha = 0f
        starIv.scaleX = 0f
        starIv.scaleY = 0f
        
        starIv.animate()
            .alpha(1f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .rotation(360f)
            .setDuration(1000)
            .withEndAction {
                starIv.animate().scaleX(1f).scaleY(1f).setDuration(500).start()
                triggerConfetti(root)
            }
            .start()

        Toast.makeText(this, "Congratulations!", Toast.LENGTH_SHORT).show()
    }

    private fun triggerConfetti(root: ConstraintLayout) {
        val random = Random()
        val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN)
        
        for (i in 0..50) {
            val confettiPiece = View(this)
            val size = random.nextInt(20) + 10
            confettiPiece.layoutParams = ViewGroup.LayoutParams(size, size)
            confettiPiece.setBackgroundColor(colors[random.nextInt(colors.size)])
            
            confettiPiece.x = random.nextInt(root.width.coerceAtLeast(1)).toFloat()
            confettiPiece.y = -50f
            
            root.addView(confettiPiece)
            
            val duration = random.nextInt(2000) + 2000L
            val fallAnim = ObjectAnimator.ofFloat(confettiPiece, "translationY", root.height.toFloat() + 50f)
            val rotationAnim = ObjectAnimator.ofFloat(confettiPiece, "rotation", random.nextInt(360).toFloat(), random.nextInt(720).toFloat())
            val xAnim = ObjectAnimator.ofFloat(confettiPiece, "translationX", confettiPiece.x + random.nextInt(200) - 100)
            
            val animSet = AnimatorSet()
            animSet.playTogether(fallAnim, rotationAnim, xAnim)
            animSet.duration = duration
            animSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    root.removeView(confettiPiece)
                }
            })
            animSet.start()
        }
    }

    private fun showLossUI(et: EditText, targetTv: TextView) {
        targetTv.text = wordToGuess
        targetTv.visibility = View.VISIBLE
        et.isEnabled = false
    }

    private fun getColoredCheck(guess: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(guess)
        val targetLettersCount = mutableMapOf<Char, Int>()
        for (char in wordToGuess) {
            targetLettersCount[char] = targetLettersCount.getOrDefault(char, 0) + 1
        }

        val styles = IntArray(4) { Color.RED } // Default to Red for no match

        // First pass: Find correct positions (Green)
        for (i in 0..3) {
            if (guess[i] == wordToGuess[i]) {
                styles[i] = Color.GREEN
                targetLettersCount[guess[i]] = targetLettersCount[guess[i]]!! - 1
            }
        }

        // Second pass: Find correct letters in wrong positions (Orange)
        for (i in 0..3) {
            if (styles[i] != Color.GREEN) {
                val count = targetLettersCount.getOrDefault(guess[i], 0)
                if (count > 0) {
                    styles[i] = Color.parseColor("#FFA500") // Orange
                    targetLettersCount[guess[i]] = count - 1
                }
            }
        }

        for (i in 0..3) {
            spannable.setSpan(
                ForegroundColorSpan(styles[i]),
                i,
                i + 1,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}
