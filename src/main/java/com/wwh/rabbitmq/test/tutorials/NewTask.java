package com.wwh.rabbitmq.test.tutorials;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * <pre>
 * 发布消息
 * 一个发布者，多个消费者
 * 
 * 使用Worker类消费消息
 * 
 * 队列将自动创建
 * </pre>
 * 
 */
public class NewTask {

	private static final String TASK_QUEUE_NAME = "task_queue";

	public static void main(String[] argv) throws Exception {
		if (argv == null || argv.length == 0) {
			argv = new String[] { "aaa...", "bbb....", "ccc.....", "ddd......", "eee....", "fff......", "ggg......" };
		}

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			// 队列持久化
			channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

			for (String message : argv) {
				// 消息持久化
				channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
				System.out.println(" [x] Sent '" + message + "'");
			}

		}
	}

}
