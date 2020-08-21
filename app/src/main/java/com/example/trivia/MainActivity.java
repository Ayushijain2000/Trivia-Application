package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView questionTextView;
    private TextView questionCounterTextView;
    private Button trueButton;
    private Button falseButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private TextView highestScoreTextView;
    private int currentQuestinIndex =0;
    private List<Question> questionList;
    private TextView scoreTextView;


    private int scoreCounter =0;
    private Score score;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();

        prefs = new Prefs(MainActivity.this);

        highestScoreTextView = findViewById(R.id.highest_score);
        scoreTextView = findViewById(R.id.score);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionCounterTextView = findViewById(R.id.counter_text);
        questionTextView = findViewById(R.id.question_textView);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);

        scoreTextView.setText(MessageFormat.format("Current Score:{0}", String.valueOf(score.getScore())));

        currentQuestinIndex = prefs.getState();
        highestScoreTextView.setText(MessageFormat.format("Highest Score: {0}",String.valueOf(prefs.getHighScore())));
        questionList =  new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
           @Override
           public void processFinished(ArrayList<Question> questionArrayList) {

               questionTextView.setText(questionArrayList.get(currentQuestinIndex).getAnswer());
               questionCounterTextView.setText(MessageFormat.format("{0} / {1}", currentQuestinIndex, questionArrayList.size())); // 0/234
              // Log.d("Inside", "onCreate: " + questionArrayList);
           }
       });

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.prev_button :
                if(currentQuestinIndex >0)
                {
                    currentQuestinIndex= (currentQuestinIndex-1)%questionList.size();
                    updateQuestion();
                }
                break;
            case R.id.next_button :
                currentQuestinIndex= (currentQuestinIndex+1)%questionList.size();
                updateQuestion();
                break;
            case R.id.true_button :
                checkAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button :
                checkAnswer(false);
                updateQuestion();
                break;

        }
    }
     public void updateQuestion()
     {
         String question = questionList.get(currentQuestinIndex).getAnswer();
         questionTextView.setText(question);
         questionCounterTextView.setText(MessageFormat.format("{0} / {1}", currentQuestinIndex, questionList.size()));
     }

     public void checkAnswer(boolean userChoosecorrect)
     {
        Boolean answerIsTrue = questionList.get(currentQuestinIndex).isAnswerTrue();
        int toastMessageId = 0;
        if(userChoosecorrect == answerIsTrue)
        {
            fadeView();
            addPoints();
            toastMessageId = R.string.correct_answer;
        }
        else
        {
            shakeAnimation();
            deductPoints();
            toastMessageId = R.string.wrong_answer;
        }
         Toast.makeText(MainActivity.this,toastMessageId,Toast.LENGTH_SHORT).show();
     }

     private void addPoints()
     {
         scoreCounter +=100;
         score.setScore(scoreCounter);
         scoreTextView.setText(MessageFormat.format("Current Score:{0}", String.valueOf(score.getScore())));

         //Log.d("Score", "addPoints: "+ score.getScore());
     }

    private void deductPoints()
    {
        scoreCounter -=100;
        if(scoreCounter > 0){
            score.setScore(scoreCounter);
            scoreTextView.setText(MessageFormat.format("Current Score:{0}", String.valueOf(score.getScore())));
        }
        else
        {
            scoreCounter =0;
            score.setScore(scoreCounter);
            scoreTextView.setText(MessageFormat.format("Current Score:{0}", String.valueOf(score.getScore())));
           // Log.d("bad Score", "deductPoints: "+ score.getScore());
        }

    }


    private void fadeView()
     {
         final CardView cardView = findViewById(R.id.cardView);
         AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

         alphaAnimation.setDuration(350);
         alphaAnimation.setRepeatCount(1);
         alphaAnimation.setRepeatMode(Animation.REVERSE);

         cardView.setAnimation(alphaAnimation);
         alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
                 cardView.setCardBackgroundColor(Color.GREEN);
             }

             @Override
             public void onAnimationEnd(Animation animation) {
                 cardView.setCardBackgroundColor(Color.WHITE);
                 currentQuestinIndex= (currentQuestinIndex+1)%questionList.size();
                 updateQuestion();
             }

             @Override
             public void onAnimationRepeat(Animation animation) {

             }
         });
     }

     private void shakeAnimation()
     {
         Animation shake = AnimationUtils.loadAnimation(MainActivity.this,R.anim.shake_animation);

         final CardView cardView = findViewById(R.id.cardView);
         cardView.setAnimation(shake);

         shake.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
                 cardView.setCardBackgroundColor(Color.RED);
             }

             @Override
             public void onAnimationEnd(Animation animation) {
                 cardView.setCardBackgroundColor(Color.WHITE);
                 currentQuestinIndex= (currentQuestinIndex+1)%questionList.size();
                 updateQuestion();
             }

             @Override
             public void onAnimationRepeat(Animation animation) {

             }
         });
     }


    @Override
    protected void onPause() {
        prefs.savedHighScore(score.getScore());
        prefs.setState(currentQuestinIndex);
        super.onPause();
    }
}
