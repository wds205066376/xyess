package com.rabbiter.fm.controller;

import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.FavoriteModel;
import com.rabbiter.fm.service.FavoriteService;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public ResultVo addFavorite(@RequestBody FavoriteModel favoriteModel) {
        favoriteModel.setCreateTime(new Date());
        if (favoriteService.addFavorite(favoriteModel)) {
            return ResultVo.success(favoriteModel.getId());
        }
        return ResultVo.fail(ErrorMsg.FAVORITE_EXIT);
    }

    @GetMapping("/delete")
    public ResultVo deleteFavorite(@RequestParam Long id) {
        if (favoriteService.deleteFavorite(id)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/check")
    public ResultVo checkFavorite(@RequestParam Long userId, @RequestParam Long idleId) {
        return ResultVo.success(favoriteService.isFavorite(userId, idleId));
    }

    @GetMapping("/my")
    public ResultVo getMyFavorite(@RequestParam Long userId) {
        return ResultVo.success(favoriteService.getAllFavorite(userId));
    }
}
