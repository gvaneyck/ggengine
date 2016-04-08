package com.gvaneyck.ggengine.server

public abstract class Room {

    String type
    String name
    List<User> users
    List<Message> messages

    public Room(String type, String name) {
        this.type = type
        this.name = name
        users = Collections.synchronizedList([])
        messages = Collections.synchronizedList([])
    }

    public void join(User user) {
        // TODO synchronize
        if (!users.contains(user)) {
            users.add(user)
            user.rooms.add(this)

            users.each {
                if (it != user) {
                    it.send([cmd: 'roomJoin', type: type, name: name, member: user.name])
                }
            }
            user.send([cmd: 'roomJoin', type: type, name: name, members: users.name, messages: messages.takeRight(10)])
        }
    }

    public void leave(User user) {
        if (users.contains(user)) {
            users.remove(user)
            user.rooms.remove(this)

            users.each {
                it.send([cmd: 'roomLeave', type: type, name: name, member: user.name])
            }
        }
    }

    public void send(Message msg) {
        messages.add(msg)
        users.each {
            it.sendMsg(type, name, msg)
        }
    }
}
