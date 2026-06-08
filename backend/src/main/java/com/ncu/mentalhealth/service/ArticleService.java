package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.Article;
import com.ncu.mentalhealth.utils.Result;

import java.util.List;

public interface ArticleService {

    Result<List<Article>> getArticles(String type);

    Result<Article> getArticleById(Long id);
}
