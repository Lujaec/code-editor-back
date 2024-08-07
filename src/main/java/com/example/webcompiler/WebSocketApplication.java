package com.example.webcompiler;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.*;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class WebSocketApplication {

	@Value("${ec2.info.host}")
	private String host;
	@Value("${ec2.info.username}")
	private String username;

	@Value("${ec2.info.password}")
	private String password;

	@Value("${ec2.info.privateKeyPath}")
	private String privateKeyPath;
	public static void main(String[] args) {
		SpringApplication.run(WebSocketApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper;
	}

	@Bean
	DockerClient dockerClient() throws IOException {
		File tempDir = Files.createTempDirectory("dockerCert").toFile();

		// Extract dockerCert contents from classpath to the temporary directory
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath:dockerCert/*");

		for (Resource resource : resources) {
			if (resource.isReadable()) {
				String filename = resource.getFilename();
				if (filename != null) {
					File targetFile = new File(tempDir, filename);
					try (InputStream inputStream = resource.getInputStream();
						 FileOutputStream outputStream = new FileOutputStream(targetFile)) {
						FileCopyUtils.copy(inputStream, outputStream);
					}
				}
			}
		}

		String dockerCertPath = tempDir.getAbsolutePath();
		SSLConfig sslConfig =  new LocalDirectorySSLConfig(dockerCertPath);

		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://"+host+":2376")
				.withDockerTlsVerify(true)
				.withCustomSslConfig(sslConfig)
				.build();

		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}

	@Bean
	Session session() throws JSchException {
		JSch jsch = new JSch();
		Session session = null;
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session = jsch.getSession(username, host, 22);
		session.setConfig(config);

		session.setPassword(password);
		session.connect(60000);
		Channel channel = session.openChannel("shell");
		channel.connect();

		return session;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOriginPatterns("*")
						.allowedMethods("GET", "POST")
						.allowCredentials(true); // 쿠키 인증 요청 허용
			}
		};
	}

	//@AuthenticationPrincipal 등록 코드
//	@Override
//	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//		argumentResolvers.add(authenticationPrincipalArgumentResolver);
//	}
}
