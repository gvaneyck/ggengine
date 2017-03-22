package com.gvaneyck.ggengine.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gvaneyck.ggengine.server.commands.Command;
import com.gvaneyck.ggengine.server.commands.CommandRef;
import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.services.MessageService;
import com.gvaneyck.ggengine.server.services.RoomService;
import com.gvaneyck.ggengine.server.services.UserService;
import com.gvaneyck.ggengine.server.util.JSON;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GGServer extends WebSocketServer {

    private MessageService messageService;
    private RoomService roomService;
    private UserService userService;

    private Map<String, CommandRef> commands = new LinkedHashMap<>();

    public GGServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(new Draft_17()));

        messageService = new MessageService();
        roomService = new RoomService();
        userService = new UserService(roomService);

        try {
            Reflections reflections = new Reflections("com.gvaneyck.ggengine.server.commands");
            Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
            for (Class clazz : classes) {
                Object instance = clazz.newInstance();
                for (Method method : clazz.getMethods()) {
                    if (method.getName().startsWith("set")) {
                        // TODO: Inject services
                        System.out.println(method.getName());
                    }

                    Command commandAnnotation = method.getAnnotation(Command.class);
                    Parameter[] parameters = method.getParameters();
                    if (commandAnnotation != null
                            && commandAnnotation.value() != ClientCommand.NONE
                            && parameters.length == 3
                            && parameters[0].getType() == WebSocket.class
                            && parameters[1].getType() == User.class) {
                        String commandName = commandAnnotation.value().toString();
                        CommandRef commandRef = new CommandRef(commandName, instance, method, method.getParameterTypes()[2]);
                        commands.put(commandName, commandRef);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " CONNECTED");
        userService.createGuest(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " DISCONNECTED");
        userService.logout(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " " + s);

        Map<String, Object> args = JSON.readValue(s, new TypeReference<LinkedHashMap<String, Object>>() { });
        if (args == null) {
            throw new GGException("Invalid arguments");
        }

        String command = args.get("cmd").toString();
        if (command == null || !commands.containsKey(command)) {
            throw new GGException("Invalid command");
        }

        User user = userService.getUser(webSocket);
        if (user.isGuest() && !command.equals(ClientCommand.USER_LOGIN.toString())) {
            throw new GGException("Guests can only login");
        }

        commands.get(command).invoke(user, args);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " ERROR");
        throw new GGException("Websocket error", e);
    }
}
