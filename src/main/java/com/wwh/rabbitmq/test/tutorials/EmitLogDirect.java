package com.wwh.rabbitmq.test.tutorials;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * <pre>
 * 路由
 * 使用直接交换（BuiltinExchangeType.DIRECT）
 * 根据消息路由键（routing key）将消息发送到不同的队列
 * 
 * 消息消费者类：ReceiveLogsDirect
 * </pre>
 *
 */
public class EmitLogDirect {

	private static final String EXCHANGE_NAME = "direct_logs";

	public static void main(String[] argv) throws Exception {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			// 定义一个直接交换
			channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

			System.out.println("请输入消息的路由键和消息内容");
			System.out.println("如：");
			System.out.println("info hello");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
					break;
				}
				String[] sl = line.split("\\s+", 2);
				if (sl.length != 2) {
					continue;
				}

				// 发送消息到直接交换，并指定消息的路由键
				channel.basicPublish(EXCHANGE_NAME, sl[0], null, sl[1].getBytes("UTF-8"));

				System.out.println(" [x] Sent '" + sl[0] + "':'" + sl[1] + "'");
			}

		}
	}

}
