package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * <pre>
 * 发布订阅 - 订阅
 * EmitLog 发布消息
 * 
 * 多启动几个实例，每个实例都将收到同样的消息
 * </pre>
 */
public class ReceiveLogs {
	private static final String EXCHANGE_NAME = "logs-exchange";

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();

		/**
		 * 定义交换，指定名称和类型
		 */
		channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
		
		/**
		 * 获取随机队列名称
		 */
		String queueName = channel.queueDeclare().getQueue();
		System.out.println("random queue name : "+queueName);
		
		/**
		 * 绑定队列和交换
		 */
		channel.queueBind(queueName, EXCHANGE_NAME, "");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}
