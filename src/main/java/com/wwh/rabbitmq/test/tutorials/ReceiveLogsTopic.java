package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <pre>
 * 主题（通配符路由模式）
 * 用.（点）分隔多个单词
 * *（星号）可以替代一个单词
 * #（井号）可以替换零个或多个单词 
 * 
 * 消息生产者：EmitLogTopic
 * </pre>
 */
public class ReceiveLogsTopic {

	private static final String EXCHANGE_NAME = "topic_logs";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
		String queueName = channel.queueDeclare().getQueue();

		if (argv.length < 1) {
			System.err.println("Usage: ReceiveLogsTopic [binding_key]...");

			System.out.println("输入队列需要绑定的路由key，* 代替一个单词，# 代替多个单词");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = br.readLine();

			argv = line.split("\\s+");
		}

		for (String bindingKey : argv) {
			channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
			System.out.println("队列：" + queueName + " 绑定：" + bindingKey);
		}

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}
