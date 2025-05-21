package org.zensnorlax.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zensnorlax.service.ReviewService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/4/5 17:22
 */

@Slf4j
@RestController
@RequestMapping("/review")
@CrossOrigin(origins = "http://localhost:1421", allowCredentials = "true")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
}
