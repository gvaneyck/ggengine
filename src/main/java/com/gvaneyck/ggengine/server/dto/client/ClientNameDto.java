package com.gvaneyck.ggengine.server.dto.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ClientNameDto {
    @NonNull private String name;
}
