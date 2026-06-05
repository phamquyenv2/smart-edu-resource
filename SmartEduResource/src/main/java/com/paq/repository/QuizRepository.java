/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.repository;

import com.paq.pojo.AnswerOption;
import com.paq.pojo.Question;
import com.paq.pojo.Quiz;
import com.paq.pojo.QuizAttempt;
import com.paq.pojo.StudentAnswer;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public interface QuizRepository {

    List<Quiz> getQuizzes();

    List<Quiz> getQuizzes(Map<String, String> params);

    Long countQuizzes(Map<String, String> params);

    Quiz getQuizById(int id);

    Quiz addOrUpdateQuiz(Quiz quiz);

    void deleteQuiz(int id);

    List<Question> getQuestionsByQuizId(int quizId);

    List<AnswerOption> getOptionsByQuestionId(int questionId);

    AnswerOption getAnswerOptionById(int id);

    QuizAttempt addQuizAttempt(QuizAttempt attempt);

    StudentAnswer addStudentAnswer(StudentAnswer answer);

    List<QuizAttempt> getAttemptsByUsername(String username);

    List<Integer> getSubmittedQuizIdsByStudentAndCourse(int studentId, int courseId);
}
