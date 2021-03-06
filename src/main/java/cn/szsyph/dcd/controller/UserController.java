package cn.szsyph.dcd.controller;


import cn.szsyph.dcd.common.ResultUtil;
import cn.szsyph.dcd.common.ResultVo;
import cn.szsyph.dcd.enums.ErrorEnum;
import cn.szsyph.dcd.exception.BusinessException;
import cn.szsyph.dcd.repository.domain.*;
import cn.szsyph.dcd.service.*;
import cn.szsyph.dcd.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 用户基本信息
 * @Author sunzhishang
 **/
@Slf4j
@RestController
@RequestMapping(value = "/motor/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserGradeService userGradeService;

    @Autowired
    private UserPinService userPinService;

    @Autowired
    private SessionUtil sessionUtil;


    @PostMapping(value = "exit")
    public ResultVo exit(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        session.removeAttribute("user_id");
        return ResultUtil.success();
    }

    /**
     * To login by password, we first check the username and password is blank or not
     * then we will get the user object to judge whether the user is legal through its id
     */
    @PostMapping(value = "/login")
    public ResultVo login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        log.info("UserController#loginByPwd: parameter info. username={}, password={}", username, password);
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            // 参数为空
            log.error("UserController#loginByPwd: param is null. username={}, password={}", username, password);
            throw new BusinessException(ErrorEnum.PARAM_IS_NULL);
        }
        User user = userService.getUserByUserNameAndPassword(username, password);
        if (user == null || user.getId() == 0) {
            return ResultUtil.error(ErrorEnum.PASSWORD_ERROR);
        } else {
            // 写入用户id
            SessionUtil.putUserId(request, user.getId());
            return ResultUtil.success();
        }
    }

    @PostMapping(value = "/is_login")
    public ResultVo isLogin(HttpServletRequest request) {
        User user = sessionUtil.getUser(request);
        Map<String, Object> result = new HashMap<>();
        if (user.getId() != 0) {
            result.put("is_login", true);
        } else {
            result.put("is_login", false);
        }
        return ResultUtil.success(result);
    }

    /**
     * 注冊用戶
     */
    @PostMapping(value = "/register")
    public ResultVo register(@RequestBody User user, HttpServletRequest request) {

        log.info("UserController#register: param info. user={}", user);
        if (user == null) {
            log.error("UserController#register: param is null. user={}", user);
            throw new BusinessException(ErrorEnum.PARAM_IS_NULL);
        }
        userService.register(user);
        SessionUtil.putUserId(request, user.getId());
        return ResultUtil.success();
    }

    @GetMapping(value = "/click")
    public ResultVo click(HttpServletRequest request) {
        User user = sessionUtil.getUser(request);
        if (user.getId() == 0) {
            return ResultUtil.error(ErrorEnum.NO_USER);
        }
        List<ArticleApi> result = new ArrayList<>();
        List<Article> clickedArticleByUserId = userBehaviorService.getClickedArticleByUserId(user.getId());
        for (Article article : clickedArticleByUserId) {
            ArticleApi articleApi = new ArticleApi(article);
            UserGrade userGradeByUserIdAndArticleId = userGradeService.getUserGradeByUserIdAndArticleId(user.getId(), article.getId());
            if (userGradeByUserIdAndArticleId != null) {
                articleApi.setGrade(userGradeByUserIdAndArticleId.getGrade());
            }
            UserPin userPin = userPinService.getUserPinByUserIdAndArticleId(user.getId(), article.getId());
            if (userPin != null) {
                articleApi.setPined(true);
            }

            result.add(articleApi);
        }
        return ResultUtil.success(result);
    }

    @GetMapping(value = "/pin")
    public ResultVo pin(HttpServletRequest request) {
        User user = sessionUtil.getUser(request);
        if (user.getId() == 0) {
            return ResultUtil.error(ErrorEnum.NO_USER);
        }
        List<ArticleApi> result = new ArrayList<>();
        List<Article> clickedArticleByUserId = userBehaviorService.getPinedArticleByUserId(user.getId());
        for (Article article : clickedArticleByUserId) {
            ArticleApi articleApi = new ArticleApi(article);
            UserGrade userGradeByUserIdAndArticleId = userGradeService.getUserGradeByUserIdAndArticleId(user.getId(), article.getId());
            if (userGradeByUserIdAndArticleId != null) {
                articleApi.setGrade(userGradeByUserIdAndArticleId.getGrade());
            }
            articleApi.setPined(true);
            result.add(articleApi);
        }
        return ResultUtil.success(result);
    }

    @GetMapping(value = "/grade")
    public ResultVo grade(HttpServletRequest request) {
        User user = sessionUtil.getUser(request);
        if (user.getId() == 0) {
            return ResultUtil.error(ErrorEnum.NO_USER);
        }
        List<ArticleApi> result = new ArrayList<>();
        List<UserGrade> userGrades = userBehaviorService.getGradedArticleByUserId(user.getId());
        for (UserGrade userGrade : userGrades) {
            ArticleApi articleApi = new ArticleApi(articleService.getArticleById(userGrade.getArticleId()));
            articleApi.setGrade(userGrade.getGrade());
            UserPin userPin = userPinService.getUserPinByUserIdAndArticleId(user.getId(), userGrade.getArticleId());
            if (userPin != null) {
                articleApi.setPined(true);
            }
            result.add(articleApi);
        }
        return ResultUtil.success(result);
    }
}
