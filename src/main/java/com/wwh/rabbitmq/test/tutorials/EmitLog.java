package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * <pre>
 * 订阅发布 
 * 
 * ReceiveLogs 订阅消息
 * </pre>
 */
public class EmitLog {

	private static final String EXCHANGE_NAME = "logs";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			/**
			 * 定义交换，指定交换名称和类型
			 * 主动声明非自动删除、非持久交换，不带任何额外参数
			 */
			channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

			String message = getMessage(argv);

			/**
			 * 发送消息到指定交换
			 */
			channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
		}
	}

	private static String getMessage(String[] strings) {
		if (strings.length < 1)
			return "info: Hello World!";
		return String.join(" ", strings);
	}

}
