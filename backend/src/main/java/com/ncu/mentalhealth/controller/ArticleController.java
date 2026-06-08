package com.ncu.mentalhealth.controller;

import com.ncu.mentalhealth.service.ArticleService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;

    @GetMapping
    public Result getArticles(@RequestParam(required = false) String type) {
        return articleService.getArticles(type);
    }

    @GetMapping("/{id}")
    public Result getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }
}
