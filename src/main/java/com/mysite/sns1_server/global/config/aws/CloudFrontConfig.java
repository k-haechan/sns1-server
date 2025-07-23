package com.mysite.sns1_server.global.config.aws;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;

@Configuration
public class CloudFrontConfig {

	@Getter
	@Value("${cloud.aws.cloudfront.key-pair-id}")
	private String keyPairId;

	@Getter
	@Value("${cloud.aws.cloudfront.cdn-host}")
	private String cdnHost;

	@Value("${cloud.aws.cloudfront.private_key_path}")
	private String PRIVATE_KEY_PATH; // = "src/main/resources/key/private_key.pem";

	public static final long VALID_SECONDS = 5*60;

	@Bean
	public PrivateKey loadPrivateKey() throws IOException, RuntimeException {
		Path path = Paths.get(PRIVATE_KEY_PATH);
		if (!Files.exists(path)) {
			throw new IOException("PEM 파일이 지정된 경로에 존재하지 않습니다: " + PRIVATE_KEY_PATH);
		}

		try (Reader reader = new FileReader(path.toFile())) {
			PEMParser pemParser = new PEMParser(reader);
			Object object = pemParser.readObject();

			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			if (object instanceof PrivateKeyInfo) {
				return converter.getPrivateKey((PrivateKeyInfo) object);
			} else if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
				org.bouncycastle.openssl.PEMKeyPair pemKeyPair = (org.bouncycastle.openssl.PEMKeyPair) object;
				return converter.getKeyPair(pemKeyPair).getPrivate();
			}
			throw new RuntimeException("PEM 파일에서 지원하는 PrivateKey 형식을 찾을 수 없습니다: " + object.getClass().getName());

		} catch (IOException e) {
			throw e; // 파일 읽기 오류는 그대로 던짐
		} catch (Exception e) { // PEM 파싱 또는 키 변환 중 발생할 수 있는 기타 예외
			throw new RuntimeException("PEM 파일에서 PrivateKey를 파싱하는 데 실패했습니다: " + e.getMessage(), e);
		}
	}

	@Bean
	public CloudFrontUtilities cloudFrontUtilities() {
		return CloudFrontUtilities.create();
	}
}
