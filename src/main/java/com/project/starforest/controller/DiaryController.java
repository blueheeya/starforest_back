package com.project.starforest.controller;



import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.starforest.dto.DiaryDTO;
import com.project.starforest.service.DiaryImageService;
import com.project.starforest.service.DiaryService;
import com.project.starforest.util.CustomFileUtil;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/diary")
@CrossOrigin(origins = "http://localhost:3000")
public class  DiaryController{

	private final DiaryService diaryService;
	private final CustomFileUtil customFileUtil;
	
//	@PostMapping("/test")
//	public String test() {
//		log.info("test");
//		return null;
//	}
	
	// 별숲기록 생성
	@PostMapping("/create")
	public ResponseEntity<?> createDiary(
			@RequestPart("content") String content,
			@RequestPart("allTags") String allTags,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
            ) {
		
		log.info("diary create request");
		log.info("Content: " + content);
		log.info("All Tags: " + allTags);
		log.info("Number of images: " + (images != null ? images.size() : 0));
//		log.info("!!!!!!!!!!!!!"+dto);
		
        try {
            DiaryDTO diaryDTO = new DiaryDTO();
            diaryDTO.setContent(content);
            diaryDTO.setAllTags(allTags);
            
            // CustomFileUtil을 사용하여 이미지 파일 저장
            if(images != null && !images.isEmpty()) {
            	List<String> savedFileNames = customFileUtil.saveFiles(images);
            	diaryDTO.setImage_url(savedFileNames);
            }
            
            DiaryDTO savedDiary = diaryService.createDiary(diaryDTO, images);
            return ResponseEntity.ok(savedDiary);

            
        } catch (Exception e) {
            log.error("Error creating diary", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
	
	// 모든 별숲기록 조회
	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> getDiaries(
			@RequestParam(value = "lastId", required = false) Long lastId,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		log.info("lastId: {}, size: {}", lastId, size);
		
		List<DiaryDTO> diaries = diaryService.getAllDiaries(lastId, size);
		log.info("Fetched diaries: {}", diaries);
		
		boolean hasMore = diaries.size() == size;
		
		Map<String, Object> response = Map.of(
				"diaries", diaries,
				"hasMore", hasMore
				);
		
		
//		Map<String, Object> response = new HashMap<>();
//		response.put("diaries", diaries);
//		response.put("hasMore", hasMore);
		
		return ResponseEntity.ok(response);
	}
	
	
	// 해당 별숲기록 자세히보기
	@GetMapping("/view/{id}")
	public ResponseEntity<DiaryDTO> getDiaryById(@PathVariable("id") Long Id) {
		try {
			DiaryDTO diaryDTO = diaryService.getDiaryById(Id);
			return ResponseEntity.ok(diaryDTO);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	// 이미지 조회
	@GetMapping("/image/{fileName}")
	public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
		return customFileUtil.getFile(fileName);
	}
	
	
	
	// 해당 유저의 별숲기록 조회
	@GetMapping("/user/{userEmail}") // 주소 확인 필요 현재 임의로 설정
	public List<DiaryDTO> getDiariesByUser(@PathVariable String userEmail) {
		return diaryService.getDiariesByUser(userEmail);
	}
	
	// 별숲기록 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteDiary(@PathVariable Long id) {
		try {
			DiaryDTO diary = diaryService.getDiaryById(id);
			diaryService.deleteDiary(id);
			
			// 연관된 이미지 파일 삭제
			if(diary.getImage_url() != null && !diary.getImage_url().isEmpty()) {
				customFileUtil.deleteFiles(diary.getImage_url());
			}
			
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("error delete diary", e);
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}