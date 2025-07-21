package com.mysite.sns1_server.global.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
@ConfigurationProperties(prefix = "cloud.aws.credentials")
public class AwsConfig {
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Bean
	public AwsCredentialsProvider awsCredentialsProvider() {
		return StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey));
		}
}
