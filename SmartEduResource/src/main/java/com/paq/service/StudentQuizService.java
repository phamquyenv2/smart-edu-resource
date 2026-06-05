/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service;

import com.paq.pojo.request.ReqSubmitQuizDTO;
import com.paq.pojo.response.ResQuizDTO;
import com.paq.pojo.response.ResQuizResultDTO;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface StudentQuizService {

    List<ResQuizDTO> getQuizzes(String username);

    ResQuizDTO getQuizById(String username, int id);

    ResQuizResultDTO submitQuiz(String username, int quizId, ReqSubmitQuizDTO request);

    List<ResQuizResultDTO> getMyQuizResults(String username);

}
