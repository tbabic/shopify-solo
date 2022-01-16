package org.bytepoet.shopifysolo.manager.controllers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.SocialMediaPost;
import org.bytepoet.shopifysolo.manager.repositories.SocialMediaPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("manager/posts")
@RestController
public class PostsController {
	
	@Autowired
	private SocialMediaPostRepository postRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<SocialMediaPost> getPosts(
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("start") Date start,
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam("end") Date end) {
		
		
		return postRepository.getByDateBetween(start, end);
	}
	
	@RequestMapping(path="/{id}",method=RequestMethod.DELETE)
	public void delete(@PathVariable("id") Long id) {
		postRepository.deleteById(id);
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	@Transactional
	public SocialMediaPost savePost(
			@RequestBody SocialMediaPost post) {
		Optional<SocialMediaPost> optional = Optional.empty();
		if (post.id != null) {
			optional = postRepository.findById(post.id);
			if (!optional.isPresent()) {
				throw new RuntimeException("No resource with id: " + post.id);
			}
		}
		
		if (post.date == null) {
			throw new RuntimeException("No date set");
		}
		
		if (StringUtils.isBlank(post.content)) {
			throw new RuntimeException("No content");
		}
		
		SocialMediaPost savedPost = postRepository.save(post);
		return savedPost;
	}


	private void preSaveHandling(SocialMediaPost post, Optional<SocialMediaPost> optional) {
		Date start = getStartOfDay(post.date);
		Date end = getEndOfDay(post.date);
		
		if (optional.isPresent()) {
			SocialMediaPost oldPost = optional.get();
			if (sameDay(oldPost.date, post.date)) {
				if (oldPost.orderPosition == post.orderPosition) {
					return;
				}
			} else {
				preUpdateHandling(oldPost, post);
			}

		}
		
		
		List<SocialMediaPost> posts = postRepository.getByDateBetween(start, end);
		if (post.orderPosition <= 0) {
			post.orderPosition = posts.size()+1;
			return;
		}
		
		if (post.orderPosition > posts.size()+1) {
			post.orderPosition = posts.size()+1;
			return;
		}
		
		if (post.orderPosition == posts.size()+1) {
			return;
		}
		

		for (SocialMediaPost existingPost : posts) {
			if (!existingPost.id.equals(post.id) && existingPost.orderPosition >= post.orderPosition) {
				existingPost.orderPosition++;
				postRepository.save(existingPost);
			}
		}
		
	}
	
	
	
	
	private void preUpdateHandling(SocialMediaPost oldPost, SocialMediaPost newPost) {

		
		Date oldStart = getStartOfDay(oldPost.date);
		Date oldEnd = getEndOfDay(oldPost.date);
		
		List<SocialMediaPost> oldDayPosts = postRepository.getByDateBetween(oldStart, oldEnd);
		for (SocialMediaPost post : oldDayPosts) {
			if (!post.id.equals(oldPost.id) && post.orderPosition > oldPost.orderPosition) {
				post.orderPosition--;
				postRepository.save(post);
			}
		}
		
	}
	
	private boolean sameDay(Date date1, Date date2) {
		
		if (date1.equals(date2)) {
			return true;
		}
		
		
		ZonedDateTime zdt1 = date1.toInstant().atZone(ZoneId.of("CET"));
		ZonedDateTime zdt2 = date1.toInstant().atZone(ZoneId.of("CET"));
		if (zdt1.getYear() == zdt2.getYear() && zdt1.getDayOfYear() == zdt2.getDayOfYear()) {
			return true;
		}
		return false;
	}
	
	
	private Date getStartOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setTimeZone(TimeZone.getTimeZone("CET"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
	private Date getEndOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setTimeZone(TimeZone.getTimeZone("CET"));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		
		return cal.getTime();
	}
	
	
}
