Notification
============
[![Build Status](https://travis-ci.org/smoketurner/notification.svg?branch=master)](https://travis-ci.org/smoketurner/notification)
[![Coverage Status](https://coveralls.io/repos/smoketurner/notification/badge.svg)](https://coveralls.io/r/smoketurner/notification)

Notification is an implementation of an HTTP-based notification web service, based on Yammer's [Streamie](http://basho.com/posts/business/riak-and-scala-at-yammer/) service. This project was developed using:

- [Dropwizard](http://dropwizard.io)
- [Riak KV](http://basho.com/products/riak-kv/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers/)

Design
------
Similar to Yammer's implementation, the Notification service relies on monitonically increasing identifiers (using [Snowizard](https://github.com/GeneralElectric/snowizard) which is an implementation of Twitter's [Snowflake](https://github.com/twitter/snowflake/releases/tag/snowflake-2010) ID generation service) that are used to resolving conflicts within Riak. The ID's are also used to provide a sorting order for the notifications so the newest notifications always appear at the top of a user's notification list. Every unique username can store up to 1000 notifications before the older notifications are aged out of the system.

Notifications are stored in the `notifications` bucket in Riak. The service also stores a cursor representing the most recent seen notification for that user. Cursors are stored in the `cursors` bucket in Riak. A cursor looks like:

```
{"key": "test-notifications", "value": 625336317638742016}
```

Where the username is `test`, the cursor name is `notifications` and the value of the cursor is `625336317638742016`.

When a user retrieves their list of notifications, the service will update the value of their cursor to the most recent notification.

Installation
------------
```
git clone https://github.com/smoketurner/notification.git
cd notification
mvn package
cd notification-application
java -jar target/notification-application/notification-application-1.0.0-SNAPSHOT.jar server notification.yml
```

The Notification service should be listening on port `8080` for API requests, and Dropwizard's administrative interface is available on port `8180` (both of these ports can be changed in the `notification.yml` configuration file).

Usage
-----
The Notification service provides RESTful URL's when creating, retrieving and deleting notifications. All of the API paths are in the form of `/v1/notifications/<username>`. In the following examples, we'll be using `test` as the username.

### Creating a notification

To create a notification, you can execute a `POST` request to the API endpoint specifying `category` and `message` fields (both are required).

```
curl \
-X POST \
-H "Content-Type: application/json" \
-d '{"category": "new-follower", "message": "You have a new follower"}' \
http://localhost:8080/v1/notifications/test -i

HTTP/1.1 201 Created
Date: Sun, 26 Jul 2015 16:06:10 GMT
Location: http://localhost:8080/v1/notifications/test
Content-Type: application/json;charset=UTF-8
Request-Id: 9a3ec8d0-1e00-47de-bb78-609a499849c4
Content-Length: 157

{
  "id":625336317638742016,
  "id_str":"625336317638742016",
  "category":"new-follower",
  "message":"You have a new follower",
  "created_at":"2015-07-26T16:06:10.970Z"
}
```

The service will generate a globally unique ID and return it in the response along with the `created_at` timestamp.

### Retrieving notifications

```
curl -X GET http://localhost:8888/v1/notifications/test -i

HTTP/1.1 200 OK
Date: Sun, 26 Jul 2015 16:12:11 GMT
Last-Modified: Sun, 26 Jul 2015 16:06:10 GMT
Accept-Ranges: id
Content-Range: id 625336317638742016..625336317638742016
Content-Type: application/json;charset=UTF-8
Request-Id: ce32a162-483d-4c34-9524-02b7f667704f
Cache-Control: no-cache, no-store, no-transform, must-revalidate
Content-Length: 190

[
  {
    "id": 625336317638742016,
    "id_str": "625336317638742016",
    "category": "new-follower",
    "message": "You have a new follower",
    "created_at": "2015-07-26T16:06:10.970Z",
    "unseen": true,
    "properties": {}
  }
]
```

The service defaults to only returning the 20 most recent notifications at a time. To return more notifications, you can execute a request specifying the `Range` HTTP header:

```
curl -X GET -H "Range: id;max=100" http://localhost:8888/v1/notifications/test -i
```

To paginate through results, if there are more notifications available, the service will include a `Next-Range` HTTP response header that you can specify in a `Range` header on a subsequent request. This will allow you to paginate through all of the results, up to a 1000 notifications.

### Deleting individual notifications

To delete individual notifications, you can execute a `DELETE` request specifying the notification ID's to delete.

```
curl -X DELETE "http://localhost:8080/v1/notifications/test?ids=625336317638742016,625336317638742015" -i

HTTP/1.1 204 No Content
Date: Sun, 26 Jul 2015 16:34:15 GMT
Request-Id: d3b446ea-08b4-4e81-9c13-06c6c372ba46
```

This endpoint will always return a `204` response code even if the notification ID's don't exist.

### Deleting all notifications

To remove all of the notifications for a user, you can execute a `DELETE` request without specifying any individual notification IDs.

```
curl -X DELETE http://localhost:8080/v1/notifications/test -i

HTTP/1.1 204 No Content
Date: Sun, 26 Jul 2015 16:34:15 GMT
Request-Id: d3b446ea-08b4-4e81-9c13-06c6c372ba46
```

This will remove all of the `test` user's notifications, their cursor and will always return a `204` response code.

Enhancements
------------
1. Add rollup support

Support
-------

Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/notification/issues).


License
-------

Copyright (c) 2015 Justin Plock

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
