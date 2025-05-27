package com.rabbiter.fm.controller;

import com.rabbiter.fm.service.SearchHistoryService;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@CrossOrigin
@RestController
@RequestMapping("/search-history")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @GetMapping("/recent")
    public ResultVo getRecentHistory(
            @CookieValue("shUserId") @NotNull(message = "登录异常") @NotEmpty(message = "登录异常") String shUserId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResultVo.success(searchHistoryService.getRecentHistory(Long.parseLong(shUserId), limit));
    }

    @PostMapping("/add")
    public ResultVo addHistory(
            @CookieValue("shUserId") @NotNull(message = "登录异常") @NotEmpty(message = "登录异常") String shUserId,
            @RequestParam String keyword) {
        searchHistoryService.addHistory(Long.parseLong(shUserId), keyword);
        return ResultVo.success();
    }

    @DeleteMapping("/{id}")
    public ResultVo deleteHistory(
            @CookieValue("shUserId") @NotNull(message = "登录异常") @NotEmpty(message = "登录异常") String shUserId,
            @PathVariable Long id) {
        return ResultVo.success(searchHistoryService.deleteHistory(Long.parseLong(shUserId), id));
    }

    @DeleteMapping("/clear")
    public ResultVo clearHistory(
            @CookieValue("shUserId") @NotNull(message = "登录异常") @NotEmpty(message = "登录异常") String shUserId) {
        return ResultVo.success(searchHistoryService.clearHistory(Long.parseLong(shUserId)));
    }
}