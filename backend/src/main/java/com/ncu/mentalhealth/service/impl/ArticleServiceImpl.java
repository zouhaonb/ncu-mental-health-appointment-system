package com.ncu.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.Article;
import com.ncu.mentalhealth.mapper.ArticleMapper;
import com.ncu.mentalhealth.service.ArticleService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleMapper articleMapper;

    @Override
    public Result<List<Article>> getArticles(String type) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Article::getType, type);
        }
        wrapper.orderByDesc(Article::getCreatedAt);
        return Result.success(articleMapper.selectList(wrapper));
    }

    @Override
    public Result<Article> getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        return article != null ? Result.success(article) : Result.error("文章不存在");
    }
}
