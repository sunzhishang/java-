package cn.szsyph.dcd.controller;


import cn.szsyph.dcd.common.ResultUtil;
import cn.szsyph.dcd.common.ResultVo;
import cn.szsyph.dcd.repository.domain.*;
import cn.szsyph.dcd.service.ArticleService;
import cn.szsyph.dcd.service.ESService.ArticleESService;
import cn.szsyph.dcd.service.UserBehaviorService;
import cn.szsyph.dcd.service.UserGradeService;
import cn.szsyph.dcd.service.UserPinService;
import cn.szsyph.dcd.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/motor/article")
public class ArticleController {

    @Autowired
    private ArticleESService articleESService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserPinService userPinService;

    @Autowired
    private UserGradeService userGradeService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private SessionUtil sessionUtil;

    @GetMapping("/search")
    public ResultVo search(
            @RequestParam(name = "keywords") String keywords,
            HttpServletRequest request) {
        User user = sessionUtil.getUser(request);
        List<ArticleES> search = articleESService.search(keywords);
        userBehaviorService.searchArticles(user.getId(), search);

        List<ArticleApi> result = new ArrayList<>();
        for (ArticleES articleES : search) {
            ArticleApi articleApi = new ArticleApi(articleES);
            articleApi.setIdStr(Long.toString(articleES.getId()));
            if (user.getId() != 0) {
                UserPin userPin = userPinService.getUserPinByUserIdAndArticleId(user.getId(), articleES.getId());
                UserGrade userGrade = userGradeService.getUserGradeByUserIdAndArticleId(user.getId(), articleES.getId());
                if (userPin != null && userPin.getArticleId() != 0) {
                    articleApi.setPined(true);
                }
                if (userGrade != null && userGrade.getArticleId() != 0) {
                    articleApi.setGrade(userGrade.getGrade());
                }
            }
            result.add(articleApi);
        }

        return ResultUtil.success(result);
    }

    @GetMapping("/addArticle")
    public ResultVo addArticle(@RequestParam(name = "count") int count) {
        articleService.addArticle(count);
        return ResultUtil.success();
    }


}
