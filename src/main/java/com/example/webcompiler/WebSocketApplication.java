package com.example.webcompiler;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
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

import java.util.Properties;

@SpringBootApplication
public class WebSocketApplication {

	@Value("${ec2.info.host}")
	private String host;
	@Value("${ec2.info.username}")
	private String username;

	@Value("${ec2.info.password}")
	private String password;
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
	DockerClient dockerClient(){
		DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory();

		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://"+host+":2375")
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
		session.setPassword(password);
		session.setConfig(config);
		session.connect(60000);
		Channel channel = session.openChannel("shell");
		channel.connect();

		return session;
	}

}
