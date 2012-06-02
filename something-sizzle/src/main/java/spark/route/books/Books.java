/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.route.books;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A simple RESTful example showing howto create, get, update and delete book
 * resources.
 * 
 * @author Per Wendel
 */
public class Books {

	/**
	 * Map holding the books
	 */
	private static Map<String, Book> books = new HashMap<String, Book>();

	public static void main(String[] args) {

		// Creates a new book resource, will return the ID to the created
		// resource
		// author and title are sent as query parameters e.g.
		// /books?author=Foo&title=Bar
		post(new Route("/books") {
			Random random = new Random();

			@Override
			public Object handle(Request request, Response response) {
				int id = random.nextInt(Integer.MAX_VALUE);
				String author = "author" + id;// request.queryParams("author");
				String title = "title" + id;// request.queryParams("title");
				Book book = new Book(author, title);

				books.put(String.valueOf(id), book);

				// response.status(201); // 201 Created
				return id;
			}
		});

		// Gets the book resource for the provided id
		get(new Route("/books/:id") {
			@Override
			public Object handle(Request request, Response response) {
				String id = request.params(":id");
				Book book = books.get(id);
				if (book != null) {
					return "Title: " + book.getTitle() + ", Author: " + book.getAuthor() +", Content:"+book.getContent();
				} else {
					String author = "author" + id;// request.queryParams("author");
					String title = "title" + id;// request.queryParams("title");
					String content = id
							+ " content=[Netty Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。Netty 提供异步的、事件驱动的网络应用程序框架和工具，" +
									"用以快速开发高性能、高可靠性的网络服务器和客户端程序。]";// request.queryParams("title");
					book = new Book(author, title,content+content+content);
					books.put(String.valueOf(id), book);
					// response.status(404); // 404 Not found
					return "Book not found";
				}
			}
		});

		// Updates the book resource for the provided id with new information
		// author and title are sent as query parameters e.g.
		// /books/<id>?author=Foo&title=Bar
		put(new Route("/books/:id") {
			@Override
			public Object handle(Request request, Response response) {
				String id = request.params(":id");
				Book book = books.get(id);
				if (book != null) {
					String newAuthor = "author" + id;// request.queryParams("author");
					String newTitle = "title" + id;// request.queryParams("title");
					if (newAuthor != null) {
						book.setAuthor(newAuthor);
					}
					if (newTitle != null) {
						book.setTitle(newTitle);
					}
					return "Book with id '" + id + "' updated";
				} else {
					// response.status(404); // 404 Not found
					return "Book not found";
				}
			}
		});

		// Deletes the book resource for the provided id
		delete(new Route("/books/:id") {
			@Override
			public Object handle(Request request, Response response) {
				String id = request.params(":id");
				Book book = books.remove(id);
				if (book != null) {
					return "Book with id '" + id + "' deleted";
				} else {
					// response.status(404); // 404 Not found
					return "Book not found";
				}
			}
		});

		// Gets all available book resources (id's)
		get(new Route("/books") {
			@Override
			public Object handle(Request request, Response response) {
				String ids = "";
				for (String id : books.keySet()) {
					ids += id + " ";
				}
				return ids;
			}
		});

	}

}
