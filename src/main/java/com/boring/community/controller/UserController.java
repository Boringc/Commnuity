package com.boring.community.controller;

import com.boring.community.annotation.LoginRequired;
import com.boring.community.entity.User;
import com.boring.community.service.UserService;
import com.boring.community.until.CommunityUtil;
import com.boring.community.until.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domian;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(suffix) || suffix !="png" || suffix !="jpg" ||suffix !="jpeg"){
            model.addAttribute("error","文件格式不正确!");
            return "/site/setting";
        }

        //生成随机文件命名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!" + e);
        }

        //更新当前用户头像路径(web访问路径)
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domian + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer , 0 , b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/update" , method = RequestMethod.POST)
    public String updatePassword( String oldpassword , String newpassword , Model model,@CookieValue("ticket") String ticket){
        User user = hostHolder.getUser();
        oldpassword = CommunityUtil.md5(oldpassword+user.getSalt());

        if (StringUtils.isBlank(oldpassword) || StringUtils.isBlank(newpassword)){
            model.addAttribute("error","请输入密码!");
            return "/site/setting";
        }if (!user.getPassword().equals(oldpassword)){
            model.addAttribute("passwordMsg","原密码为空或不正确!");
            return "/site/setting";
        }else {
            newpassword = CommunityUtil.md5(newpassword+user.getSalt());
            userService.updatPassword(user.getId(),newpassword);
        }

        userService.logout(ticket);
        return "redirect:/login";
    }

}
