package com.gvaneyck.ggengine.server.dto.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ClientGameDto {
    @NonNull private String name;
    @NonNull private String game;
    private String password;
}
