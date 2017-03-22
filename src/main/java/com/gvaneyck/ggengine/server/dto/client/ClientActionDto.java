package com.gvaneyck.ggengine.server.dto.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ClientActionDto {
    @NonNull String actionRef;
    @NonNull Object[] args;
}
