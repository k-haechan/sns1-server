package com.mysite.sns1_server.domain.image.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.image.entity.Image;
import com.mysite.sns1_server.domain.image.repository.ImageRepository;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	@Transactional
	public List<ImageResponse> saveByPost(Post post, int imagesLength) {
		if(imagesLength<=0){
			return null;
		}


		// 이미지 path 생성
		List<String> imagePaths = IntStream.range(0, imagesLength)
			.mapToObj(idx ->
				s3Service.getPostImagePath(post.getAuthor().getId(), post.getId(), idx))
			.toList();

		List<Image> images = imagePaths.stream().map(
			imagePath -> new Image(post, imagePath)
		).toList();

		// todo: 성능 이슈 시 다중 insert 쿼리로 최적화 필요
		imageRepository.saveAll(images);

		// presignedUrl 생성 및 반환
		return imagePaths.stream()
			.map(
			imagePath -> s3Service.generatePresignedUploadUrl(imagePath).toString()
			)
			.map(
				ImageResponse::from
			)
			.toList();
	}

	// todo: 성능 이슈 시 프로젝션 이용하여 최적화 필요
	public List<Image> findByPost(Post post) {
		return imageRepository.findByPost(post);
	}

	// todo: 성능 이슈 시 조회 대신 조인, 삭제 쿼리로  최적화 필요
	public void deleteImagesByPost(Post post) {
		List<Image> images = imageRepository.findByPost(post);
		if(images.isEmpty()){
			return;
		}
		for(Image image : images){
			s3Service.deleteObject(image.getPath());
		}

		imageRepository.deleteAll(images);
	}
}
