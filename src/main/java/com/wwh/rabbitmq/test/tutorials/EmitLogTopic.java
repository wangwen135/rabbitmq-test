package com.wwh.rabbitmq.test.tutorials;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * <pre>
 * 主题（通配符路由模式）
 * 
 * 用.（点）分隔多个单词
 * 有效的路由key：“ stock.usd.nyse ”，“ nyse.vmw ”，“ quick.orange.rabbit ”
 * 路由key中可以包含任意数量的单词，最多可达255个字节
 * 
 * *（星号）可以替代一个单词
 * #（井号）可以替换零个或多个单词 
 * 
 * 消息消费者：ReceiveLogsTopic
 * </pre>
 */
public class EmitLogTopic {

	private static final String EXCHANGE_NAME = "topic_logs";

	public static void main(String[] argv) {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			// 定义非自动删除、非持久交换
			channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

			System.out.println("请输入消息的路由键和消息内容");
			System.out.println("如：");
			System.out.println("log.info.user The user is modified");

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

				// 发送消息到主题交换，并指定消息的路由键
				channel.basicPublish(EXCHANGE_NAME, sl[0], null, sl[1].getBytes("UTF-8"));

				System.out.println(" [x] Sent '" + sl[0] + "':'" + sl[1] + "'");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
