package com.gvaneyck.ggengine.server.dto.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ClientMessageDto {
    @NonNull private String type;
    @NonNull private String target;
    @NonNull private String msg;
}
