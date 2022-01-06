package org.bytepoet.shopifysolo.manager.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bytepoet.shopifysolo.manager.models.SocialMediaPost;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/posts")
@RestController
public class PostsController {
	
	@RequestMapping(method=RequestMethod.GET)
	public List<SocialMediaPost> getPosts(
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("start") Date start,
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("end") Date end) {
		SocialMediaPost post = new SocialMediaPost();
		post.content = "This is some content";
		post.date = new Date();
		post.id = 1L;
		post.number = 1;
		
		
		return Arrays.asList(post);
	}
	
	
	
}
